package smajava;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import smajava.misc.DEBUG;
import smajava.misc.VERBOSE;

public class SmaLogger 
{
	public static final String VERSION = "0.2 Remaster";
	
	public final static long UG_USER = 0x07L;
	public final static long UG_INSTALLER = 0x0AL;
	
	private final String IP_Broadcast = "239.12.255.254";
	private Ethernet ethernet;
	private Config config;
	
	public static short AppSUSyID;
	public static long AppSerial;
	public static int quiet = 0;
	
	public SmaLogger(Config cfg)
	{
		ethernet = new Ethernet();
		config = cfg;
	}
	
	public int Initialize(String[] args)
	{
		int rc = 0;
		
		//Read the command line and store settings in config struct
	    rc = config.parseCmdline(args);
	    if (rc == -1) return 1;	//Invalid commandline - Quit, error
	    if (rc == 1) return 0;	//Nothing to do - Quit, no error

	    //Read config file and store settings in config struct
	    try 
	    {
			rc = config.ReadConfigFile();
		} 
	    catch (IOException e) 
	    {
			e.printStackTrace();
			return rc;
		}	
	    //Config struct contains fullpath to config file
	    if (rc != 0) return rc;

	    //Copy some config settings to public variables
	    quiet = config.quiet;
	    DEBUG.SetDebug(config.debug);
	    VERBOSE.SetVerbose(config.verbose);
	    //quiet = cfg.quiet;
		
		rc = ethernet.Connect(config.IP_Port);
		if (rc != 0)
		{
			System.err.println("Failed to set up socket connection.");
			return rc;
		}
		
		//Generate a Serial Number for application
	    AppSUSyID = 125;
	    Random r = new Random(System.currentTimeMillis());
	    AppSerial = 900000000 + ((r.nextInt() << 16) + r.nextInt()) % 100000000;
	    ethernet.AppSUSyID = AppSUSyID;
	    ethernet.AppSerial = AppSerial;
	    if (VERBOSE.Normal()) 
	    	System.out.printf("SUSyID: %d - SessionID: %d (0x%08X)\n", AppSUSyID, AppSerial, AppSerial);
	    
		return rc;
	}
	
	public void ShutDown()
	{
		ethernet.Close();
	}
	
	/**
	 * Used to manually create an inverter with a given ip adres, this method
	 * gives the inverter a socket connection uses for communication.
	 * @param ip The ip adress of the inverter.
	 * @return An inverter with the ip adress and a socket connection.
	 */
	public Inverter CreateInverter(String ip)
	{
		//Create a new inverter and give it the socket connection.
		Inverter inv = new Inverter(ip, ethernet);
		return inv;
	}
	
	/**
	 * Sends a broadcast message over the network the detect inverters.
	 * @return A list of found inverters. If no inverters are found, the list is empty.
	 */
	public List<Inverter> DetectDevices()
	{
		List<Inverter> inverters = new ArrayList<Inverter>();
		boolean foundOne = false;
		
		// Start with UDP broadcast to check for SMA devices on the LAN
		SendBroadcastMessage();
		
		//SMA inverter announces it´s presence in response to the discovery request packet
    	int bytesRead = 1;
    	byte[] CommBuf = new byte[1024];
    	
    	//Untested, the idea is to keep listening if there are multiple inverters.
    	while(bytesRead > 0)
    	{
    		// if bytesRead < 0, a timeout has occurred
    		// if bytesRead == 0, no data was received
    		bytesRead = ethernet.Read(CommBuf);
    		
    		//Only do this if we actually got some data
    		if(bytesRead > 0)
    		{
	    		//Retrieve the ip adress from the received package.
	    		String ip = String.format("%d.%d.%d.%d", (CommBuf[38] & 0xFF), (CommBuf[39] & 0xFF), (CommBuf[40] & 0xFF), (CommBuf[41] & 0xFF));
	    		
	    		if (quiet == 0) 
	            	System.out.printf("Inverter IP address: %s found via broadcastidentification\n", ip);
	
	    		//Check if the inverter isn't already in the list.
	    		if(!ContainsInverter(inverters, ip))
	    		{
	    			//Create a new inverter with the ip and add it to the result list.
	    			inverters.add(CreateInverter(ip));
	    		}	
	    		foundOne = true;
    		}
    	}

		
		if (!foundOne)
		{
			System.err.println("ERROR: No inverter responded to identification broadcast.\n");
			System.err.println("Try to set IP_Address in SBFspot.cfg!\n");
			return inverters;
		}

    	if (DEBUG.Normal()) 
    		misc.HexDump(CommBuf, bytesRead, 10);
    	
    	return inverters;
	}
	
	private void SendBroadcastMessage()
	{
		//Clear the buffer and set packet position to 0.
		ethernet.ClearBuffer();
		
    	ethernet.writeLong(0x00414D53);  //Start of SMA header
    	ethernet.writeLong(0xA0020400);  //Unknown
    	ethernet.writeLong(0xFFFFFFFF);  //Unknown
    	ethernet.writeLong(0x20000000);  //Unknown
    	ethernet.writeLong(0x00000000);  //Unknown

    	ethernet.Send(IP_Broadcast);
	}
	
	private boolean ContainsInverter(List<Inverter> inverters, String ip)
	{
		boolean res = false;
		
		for(Inverter inv : inverters)
		{
			if(inv.GetIP() == ip)
			{
				res = true;
				break;
			}
		}
		return res;
	}
}
