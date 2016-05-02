package smajava;

import java.util.Date;

import smajava.misc.DEBUG;
import eth.ethPacket;
import eth.ethPacketHeaderL1;
import eth.ethPacketHeaderL1L2;

public class SmaConnection 
{
	public class E_SBFSPOT
	{
		public final static int E_OK			= 0;
		public final static int E_NODATA		= -1;	// Bluetooth buffer empty
		public final static int E_BADARG		= -2;	// Unknown command line argument
		public final static int E_CHKSUM		= -3;	// Invalid Checksum
		public final static int E_BUFOVRFLW		= -4;	// Buffer overflow
		public final static int E_ARCHNODATA	= -5;	// No archived data found for given timespan
		public final static int E_INIT			= -6;	// Unable to initialize
		public final static int E_INVPASSW		= -7;	// Invalid password
		public final static int E_RETRY			= -8;	// Retry the last action
		public final static int E_EOF			= -9;	// End of data
	}
	
	private final short anySUSyID = (short)0xFFFF;
	private final long anySerial = 0xFFFFFFFF;
	private final int COMMBUFSIZE = 1024;
	
	protected Ethernet ethernet;
	
	protected String ip;
	protected byte[] CommBuf;
	
	public SmaConnection(Ethernet eth, String inverterIP)
	{
		this.ethernet = eth;
		this.ip = inverterIP;
		this.CommBuf = new byte[COMMBUFSIZE];
	}
	
	protected void InitConnection()
	{
		ethernet.writePacketHeader();
	    ethernet.writePacket((char)0x09, (char)0xA0, (short)0, anySUSyID, anySerial);
	    ethernet.writeLong(0x00000200);
	    ethernet.writeLong(0);
	    ethernet.writeLong(0);
	    ethernet.writeLong(0);
	    ethernet.writePacketLength();

	    //Send packet to first inverter
	    ethernet.Send(ip);    
	}
	
	protected void Close()
	{
		
	}
	
	protected int SMALogin(long userGroup, char[] password)
	{
		final int MAX_PWLENGTH = 12;
	    char pw[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	    if (DEBUG.Normal()) 
	    	System.out.println("SMALogin()");

	    char encChar = (char) ((userGroup == SmaLogger.UG_USER)? 0x88:0xBB);
	    //Encode password
	    int idx;
	    for (idx = 0; (password[idx] != 0) && (idx <= pw.length); idx++)
	    {
	        pw[idx] = (char) (password[idx] + encChar);
	    }
	    for (; idx < MAX_PWLENGTH; idx++)
	        pw[idx] = encChar;

	    int rc = E_SBFSPOT.E_OK;
	    int validPcktID = 0;

	    Date now;

        now = new Date();
        ethernet.writePacketHeader();
        ethernet.writePacket((char)0x0E, (char)0xA0, (short)0x0100, anySUSyID, anySerial);
        ethernet.writeLong(0xFFFD040C);
        ethernet.writeLong(userGroup);
        ethernet.writeLong(0x00000384);
        ethernet.writeLong(now.getTime());
        ethernet.writeLong(0);
        ethernet.writeArray(pw, pw.length);
        ethernet.writePacketTrailer();
        ethernet.writePacketLength();

        //TODO: make this work for multiple inverters, kinda did this with the whole api thing.
        ethernet.Send(ip);

        validPcktID = 0;
        do
        {
            if ((rc = GetPacket()) == E_SBFSPOT.E_OK)
            {
                ethPacket pckt = new ethPacket(ethernet.pcktBuf);
                if (ethernet.pcktID == ((pckt.PacketID) & 0x7FFF))   // Valid Packet ID
                {
                    validPcktID = 1;
                    //rc = (pckt->ErrorCode == 0) ? E_OK : E_INVPASSW;
					// Fix Issue CP5 - Logon problem Sunny Island
					//rc = (btohs(pckt->ErrorCode) == 0x0100) ? E_INVPASSW : E_OK;
                    rc = (misc.shortSwap(pckt.ErrorCode) == 0x0100) ? E_SBFSPOT.E_INVPASSW : E_SBFSPOT.E_OK;
				}
                else
                    if (DEBUG.Highest()) 
                    	System.out.printf("Packet ID mismatch. Expected %d, received %d\n", ethernet.pcktID, (misc.shortSwap(pckt.PacketID) & 0x7FFF));
            }
            else
            	return rc;
        } while (validPcktID == 0);

	    return rc;
	}
	
	protected int SMALogoff()
	{
		if (DEBUG.Normal()) 
	    	System.out.println("SMALogoff()");
        
        ethernet.writePacketHeader();
        ethernet.writePacket((char)0x08, (char)0xA0, (short)0x0300, anySUSyID, anySerial);
        ethernet.writeLong(0xFFFD010E);
        ethernet.writeLong(0xFFFFFFFF);
        ethernet.writePacketTrailer();
        ethernet.writePacketLength();

        //TODO: make this work for multiple inverters
        ethernet.Send(ip);

	    return E_SBFSPOT.E_OK;
	}
	
	protected int GetPacket()
	{
		//CommBuf = new byte[COMMBUFSIZE];
		boolean retry = false;
	    if (DEBUG.Normal()) 
	    	System.out.printf("ethGetPacket()\n");
	    int rc = E_SBFSPOT.E_OK;
	    
	    do 
	    {
	    	retry = false;
	    	int bib = ethernet.Read(CommBuf);

	    	if (bib <= 0)
	        {
	            if (DEBUG.Normal()) 
	            	System.out.printf("No data!\n");
	            rc = E_SBFSPOT.E_NODATA;
	        }
	        else
	        {
	        	ethPacketHeaderL1L2 pkHdr = new ethPacketHeaderL1L2(CommBuf);
	        	int pkLen = ((pkHdr.pcktHdrL1.hiPacketLen << 8) + pkHdr.pcktHdrL1.loPacketLen) & 0xff;	//0xff to convert it to unsigned?

	            //More data after header?
	            if (pkLen > 0)
	            {
	            	if (DEBUG.High()) 
		        		misc.HexDump(CommBuf, bib, 10);

	                if (misc.intSwap(pkHdr.pcktHdrL2.MagicNumber) == ethernet.ETH_L2SIGNATURE)
	                {
	                    // Copy CommBuf to packetbuffer
	                    // Dummy byte to align with BTH (7E)
	                    ethernet.pcktBuf[0]= 0;
	                    // We need last 6 bytes of ethPacketHeader too
	                    System.arraycopy(CommBuf, ethPacketHeaderL1.getSize(), ethernet.pcktBuf, 1, bib - ethPacketHeaderL1.getSize());
	                    
	                    // Point packetposition at last byte in our buffer
						// This is different from BTH
	                    ethernet.packetposition = bib - ethPacketHeaderL1.getSize();

	                    if (DEBUG.High())
	                    {
	                        System.out.printf("<<<====== Content of pcktBuf =======>>>\n");
	                        misc.HexDump(ethernet.pcktBuf, ethernet.packetposition, 10);
	                        System.out.printf("<<<=================================>>>\n");
	                    }
	                }
	                else
	                {
	                	if (DEBUG.Normal())  
	                		System.out.printf("L2 header not found.\n");
	                    retry = true;
	                }
	            }
	            else
	                rc = E_SBFSPOT.E_NODATA;
	                
	    	}
		} while (retry == true);
	    return rc;
	}
}
