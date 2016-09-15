package inverter;

import java.util.Date;

import smaconn.Ethernet;
import smaconn.SmaConnection;
import smajava.SmaLogger;
import smajava.misc;
import smajava.misc.DEBUG;
import inverterdata.InverterData;
import inverterdata.InverterDataType;
import eth.ethPacket;

public class Inverter extends SmaConnection
{
	public InverterData Data;
	
	/**
	 * Creates a new inverter.
	 * NOTE: This method is used by the SMALogger and should not be uses manually.
	 * Use {@link smajava.SmaLogger#CreateInverter(String)} to create an inverter instead.
	 * @param ip The ip address of the inverter.
	 * @param ethernet The Ethernet connection.
	 */
	public Inverter(String ip, Ethernet ethernet)
	{		
		//Each inverters has his own connection but uses the same ethernet socket.
		//Sma connection constructor
		super(ethernet, ip);
		Data = new InverterData();
	}
	
	/**
	 * Returns the ip adress of this inverter.
	 * @return A string containing the IP adress of the inverter.
	 */
	public String GetIP()
	{
		return super.ip;
	}
	
	/**
	 * Sends a logon request to the inverter which creates a connection.
	 * Use this before getting data from the inverter but after the main connection was created.
	 * NOTE: Only able to log on as a user now!
	 * @param password The password used to login to the inverter.
	 * @return Returns 0 if everything went ok.
	 */
	public int Logon(String password)
	{
		//The api needs the password in chararray form of a fixed size. 
		//We convert it here so the user doesn't have to hassle with it and can just input a string.
		char[] passArray = new char[13];
		for(int ch = 0; ch < password.length(); ch++)
    	{
			passArray[ch] = password.charAt(ch);
    	}
		return Logon(SmaLogger.UG_USER, passArray);
	}
	
	/**
	 * Sends a logon request to the inverter which creates a connection.
	 * Use this before getting data from the inverter but after the main connection was created.
	 * @param userGroup The usergroup: (USER or INSTALLER)
	 * @param password The password used to login to the inverter.
	 * @return Returns 0 if everything went ok.
	 */
	private int Logon(long userGroup, char[] password)
	{
		int rc = 0;
		//First initialize the connection.
		InitConnection();
		if ((rc = GetPacket()) == E_SBFSPOT.E_OK)
	    {
	    	ethPacket pckt = new ethPacket(ethernet.pcktBuf);
	    	Data.SUSyID = pckt.Source.SUSyID;
	    	Data.Serial = pckt.Source.Serial;
	    	//SMALogoff();
	    }
		else
		{
			System.err.println("ERROR: Connection to inverter failed!\n");
			System.err.println("Is " + ip + " the correct IP?\n");
			System.err.println("Please check IP_Address in SBFspot.cfg!\n");
			return E_SBFSPOT.E_INIT;
		}
		//Then login.
		SMALogin(userGroup, password);
		int validPcktID = 0;
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

		if (rc != E_SBFSPOT.E_OK)
	    {
	        System.err.printf("Logon failed. Check '%s' Password\n", userGroup == SmaLogger.UG_USER? "USER":"INSTALLER");
	        SMALogoff();
	        return E_SBFSPOT.E_INVPASSW;
	    }
		return rc;
	}
	
	/**
	 * Sends a logoff request to the inverter to shut down the connection.
	 * Use this after your done getting data but before shutting down the main connection.
	 */
	public void Logoff()
	{
		SMALogoff();
	}
	
	/**
	 * Requests data from the inverter which gets stored in it's Data attribute.
	 * Uses Data.(Name of the value you requested) to get the actual value this method requested.
	 * @param invDataType The type of data you want to retrieve from the inverter.
	 * @return Returns 0 if everything went ok.
	 */
	public int GetInverterData(InverterDataType invDataType)
	{
		if (DEBUG.Normal()) 
	    	System.out.printf("getInverterData(%s)\n", invDataType.toString());

	    int rc = E_SBFSPOT.E_OK;

	    int recordsize = 0;
	    int validPcktID = 0;
	    int value = 0;
	    validPcktID = 0;
	    
	    RequestInverterData(invDataType);
	    
        do
        {
            rc = GetPacket();

            if (rc != E_SBFSPOT.E_OK) 
            	return rc;

            short rcvpcktID = (short) (ethernet.pcktBuf[27] & 0x7FFF);
            if (ethernet.pcktID == rcvpcktID)
            {
            	//Check if we received the package from the right inverter, not sure if
            	//this works with multiple inverters.
            	//We do this by checking if the susyd and serial is equal to this inverter object's susyd and serial.
            	boolean rightOne = Data.SUSyID == misc.get_short(ethernet.pcktBuf, 15) && Data.Serial == misc.get_long(ethernet.pcktBuf, 17);

            	if (rightOne)
                {
                    validPcktID = 1;

                    for (int ix = 41; ix < ethernet.packetposition - 3; ix += recordsize)
                    {                   	
                    	int code = misc.get_long(ethernet.pcktBuf, ix);
                    	
                        //Check this if something doesn't work, int to enum conversion. Should be good now
                        LriDef lri = LriDef.intToEnum((code & 0x00FFFF00));

                        char dataType = (char) (code >> 24);
                        //Not sure if java uses same long date, well it doesn't
                        //Multiply by 1000 cause java uses milliseconds and the inverter uses seconds since epoch.
                        Date datetime = new Date(misc.get_long(ethernet.pcktBuf, ix + 4) * 1000l);                        
                        
                        if ((dataType != 0x10) && (dataType != 0x08))	//Not TEXT or STATUS, so it should be DWORD
                        {
                        	//All data that needs an int value
                            value = misc.get_long(ethernet.pcktBuf, ix + 8);
                            if ((value == misc.NaN_S32) || (value == misc.NaN_U32)) value = 0;
                        }                       
                        // fix: We can't rely on dataType because it can be both 0x00 or 0x40 for DWORDs
                        if ((lri == LriDef.MeteringDyWhOut) || (lri == LriDef.MeteringTotWhOut) || (lri == LriDef.MeteringTotFeedTms) || (lri == LriDef.MeteringTotOpTms))	//QWORD
                        {
                        	//All data that needs a long value
                        	long value64 = misc.get_longlong(ethernet.pcktBuf, ix + 8);
                            if ((value64 == misc.NaN_S64) || (value64 == misc.NaN_U64)) value64 = 0;                      
                            
                            recordsize = 16;
                            Data.SetInverterData64(lri, value64, datetime);
                        }
                        else if(lri == LriDef.NameplateLocation)
                        {
                        	//INV_NAME
                        	recordsize = 40;
                            char[] dvcName = new char[Data.DeviceName.length];
                            for(int ch = 0; ch < Data.DeviceName.length - 1; ch++)
                            {
                            	dvcName[ch] = (char)ethernet.pcktBuf[ix + 8 + ch];
                            }
                            Data.SetInverterDataINVNAME(dvcName, datetime);
                        }                       
                        else if(lri == LriDef.NameplatePkgRev)
                        {
                        	//INV_SWVER
                        	recordsize = 40;
                        	char Vtype = (char) ethernet.pcktBuf[ix + 24];
                            String ReleaseType;
                            if (Vtype > 5)
                            	ReleaseType = String.format("%c", Vtype);
                            else
                            	ReleaseType = String.format("%c", "NEABRS".charAt(Vtype));//NOREV-EXPERIMENTAL-ALPHA-BETA-RELEASE-SPECIAL
                            char Vbuild = (char) ethernet.pcktBuf[ix + 25];
                            char Vminor = (char) ethernet.pcktBuf[ix + 26];
                            char Vmajor = (char) ethernet.pcktBuf[ix + 27];
                            //Vmajor and Vminor = 0x12 should be printed as '12' and not '18' (BCD)
                            String version = String.format("%c%c.%c%c.%02d.%s", '0'+(Vmajor >> 4), '0'+(Vmajor & 0x0F), '0'+(Vminor >> 4), '0'+(Vminor & 0x0F), (int)Vbuild, ReleaseType);  
                            Data.SetInverterDataSWVER(version, datetime);
                        }
                        else if(lri == LriDef.OperationHealth || lri == LriDef.OperationGriSwStt || lri == LriDef.NameplateMainModel || lri == LriDef.NameplateModel)
                        {
                        	//All cases which need the attribute value
                        	//INV_STATUS
                        	//INV_GRIDRELAY
                        	//INV_CLASS
                        	//INV_TYPE
                        	recordsize = 40;
                        	for (int idx = 8; idx < recordsize; idx += 4)
            		        {
            		            int attribute = misc.get_long(ethernet.pcktBuf, ix + idx) & 0x00FFFFFF;
            		            char attValue = (char) ethernet.pcktBuf[ix + idx + 3];
            		            if (attribute == 0xFFFFFE) break;	//End of attributes
            		            if (attValue == 1)
            		            {
            		                Data.SetInverterDataAttribute(lri, attribute, datetime);
            		            }
            		        }
                        }
                        else if(lri == LriDef.DcMsWatt || lri == LriDef.DcMsAmp || lri == LriDef.DcMsVol)
                        {
                        	//All cases which need the cls var
                        	//SPOT_PDC1 / SPOT_PDC2
                        	//SPOT_UDC1 / SPOT_UDC2
                        	//SPOT_IDC1 / SPOT_IDC2
                        	recordsize = 28;
                        	long cls = code & 0xFF;
                        	Data.SetInverterDataCls(lri, value, cls, datetime);
                        }
                        else
                        {	
                        	//All other cases go here
                        	if(lri == null)
                        	{
                        		if(recordsize == 0) recordsize = 12;
                        	}
                        	else
                        	{
                            	recordsize = 28;
                            	Data.SetInverterData(lri, value, datetime);
                        	}
                        }
                    }
                }
            	else
            	{
            		if(DEBUG.Normal())
            			System.out.printf("We received data from the wrong inverter... Expected susyd: %d, received: %d", Data.SUSyID, misc.get_short(ethernet.pcktBuf, 15));
            	}
            }
            else
            {
                if (DEBUG.Highest()) 
                	System.out.printf("Packet ID mismatch. Expected %d, received %d\n", ethernet.pcktID, rcvpcktID);
            }
        }
        while (validPcktID == 0);
        return rc;
	}
	
	/**
	 * Calculates the missing DC Spot Values
	 */
	public void CalcMissingSpot()
	{
		if (Data.Pdc1 == 0) Data.Pdc1 = (Data.Idc1 * Data.Udc1) / 100000;
		if (Data.Pdc2 == 0) Data.Pdc2 = (Data.Idc2 * Data.Udc2) / 100000;

		if (Data.Pac1 == 0) Data.Pac1 = (Data.Iac1 * Data.Uac1) / 100000;
		if (Data.Pac2 == 0) Data.Pac2 = (Data.Iac2 * Data.Uac2) / 100000;
		if (Data.Pac3 == 0) Data.Pac3 = (Data.Iac3 * Data.Uac3) / 100000;

	    if (Data.TotalPac == 0) Data.TotalPac = Data.Pac1 + Data.Pac2 + Data.Pac3;
	}
	
	public void GetDayData()
	{
		
	}
	
	public void GetMonthData()
	{
		
	}
	
	public void GetEventData()
	{
		
	}
}
