package smajava;

import inverter.Inverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import smaconn.Ethernet;
import smajava.misc.DEBUG;
import smajava.misc.VERBOSE;

public class SmaLogger 
{
	public static final String VERSION = "0.2 Remaster";
	
	public final static long UG_USER = 0x07L;
	public final static long UG_INSTALLER = 0x0AL;
	
	private String AppPath;
	private final String IP_Broadcast = "239.12.255.254";
	private Ethernet ethernet;
	
	public static short AppSUSyID;
	public static long AppSerial;
	public static int quiet = 0;
	
	/**
	 * Creates a new instance of the SMALogger and it's ethernet connection.
	 */
	public SmaLogger()
	{
		ethernet = new Ethernet();
	}
	
	/**
	 * Initializes the SMALogger, also intializes and creates the ethernet
	 * connection.
	 * @param args The uses args for this API:
	 * -v[1-5] To set the verbose logging level.
	 * -d[1-5] To set the debug messaging level.
	 * @return Returns 0 if everything went right.
	 */
	public int Initialize(String[] args)
	{
		//The path where this application is.
		AppPath = new File(".").getAbsolutePath();
		int pos = AppPath.lastIndexOf('\\');
		if (pos != -1)
			AppPath = AppPath.substring(++pos);
		else
			AppPath = "";

		int rc = 0;
		
		//Read the command line
		rc = parseCmdline(args);
	    if (rc == -1) return 1;	//Invalid commandline - Quit, error
	    if (rc == 1) return 0;	//Nothing to do - Quit, no error

	    /*	Config file usage removed.
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
		*/
	    //Config struct contains fullpath to config file
	    if (rc != 0) return rc;
	    
	    //Lets just use the english taglist for now.
	    int status = TagDefs.GetInstance().readall(AppPath, "en-US");
		if (status != TagDefs.READ_OK)
		{
			System.err.print("Error reading tags\n");
			return(2);
		}
		
		//So the port was hardcoded in the config so why not hardcode it here, for now...
		rc = ethernet.Connect((short)9522);
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
	
	/**
	 * Shuts down the SMALogger and closes it's ethernet connection.
	 */
	public void ShutDown()
	{
		ethernet.Close();
	}
	
	/**
	 * Used to manually create an inverter with a given ip adres, this method
	 * gives the inverter a socket connection used for communication.
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
			if(inv.GetIP().equals(ip))
			{
				res = true;
				break;
			}
		}
		return res;
	}
	
	public int parseCmdline(String[] argv)
	{
		DEBUG.SetDebug(0);				// debug level - 0=none, 5=highest
		VERBOSE.SetVerbose(0);			// verbose level - 0=none, 5=highest

	    // ? forceInq = 0;			// Inquire inverter also during the night
	    // ? userGroup = SmaLogger.UG_USER;  
	    
		quiet = 0;
	   
	    //Set quiet mode
	    for (int i = 1; i < argv.length; i++)
		{
	        if (argv[i].equals("-q"))
	        {
	            quiet = 1;
	            break;
	        }
		}
	    
	    char pEnd = 0;
	    int lValue = 0;

	    if (quiet == 0)
	    {
	        SayHello(0);
	        System.out.println("Commandline Args:\n");
	        for (int i = 0; i < argv.length; i++)
	        	System.out.print(argv[i]);

	        System.out.println("\n");
	    }

	    for (int i = 0; i < argv.length; i++)
	    {
	        if (argv[i] ==  "/")
	            argv[i] = "-";

	        //Set debug level
	        if(argv[i].startsWith("-d"))
	        {
	            lValue = Integer.parseInt(argv[i].substring(2, argv[i].length()));
	            if (argv[i].length() == 2) lValue = 2;	// only -d sets medium debug level
	            if ((lValue < 0) || (lValue > 5) || (pEnd != 0))
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                DEBUG.SetDebug(lValue);
	        }

	        //Set verbose level
	        else if (argv[i].startsWith("-v"))
	        {
	        	if (argv[i].length() == 2) 
	        		lValue = 2;	// only -v sets medium verbose level
	        	else
	        		lValue = Integer.parseInt(argv[i].substring(2, argv[i].length()));
	            if (lValue < 0 || lValue > 5 || pEnd != 0)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                VERBOSE.SetVerbose(lValue);
	        }

	        /*	Should this be handled by the API or the user ???
	        //Set inquiryDark flag
	        else if (argv[i].equals("-finq"))
	            forceInq = 1;
         	*/	                   
	        
	        /*
	        else if (argv[i].equals("-installer"))
	            userGroup = SmaLogger.UG_INSTALLER;

	        else if (argv[i].startsWith("-password:"))
	            if (argv[i].length() == 10)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	            {
	            	//SMA_Password = argv[i]+10;
	            	String argPass = argv[i].substring(10, argv[i].length());
	            	for(int ch = 0; ch < argPass.length(); ch++)
	            	{
	            		SMA_Password[ch] = argPass.charAt(ch);
	            	}
	            	//SMA_Password = argv[i].substring(10, argv[i].length());
	                //strncpy(cfg->SMA_Password, argv[i]+10, sizeof(cfg->SMA_Password));
	            }
          	*/

	        //Show Help
	        else if (argv[i].equals("-?"))
	        {
	            SayHello(1);
	            return 1;	// Caller should terminate, no error
	        }

	        else if (quiet == 0)
	        {
	            InvalidArg(argv[i]);
	            return -1;
	        }

	    }

	    //Disable verbose/debug modes when silent
	    if (quiet == 1)
	    {
	        VERBOSE.SetVerbose(0);
	        DEBUG.SetDebug(0);
	    }

	    return 0;
	}
	
	private void InvalidArg(String arg)
	{
	    System.out.printf("Invalid argument: %s\nUse -? for help\n", arg);
	}
	
	private void SayHello(int ShowHelp)
	{
		System.out.println("SmaLogger Java v" + SmaLogger.VERSION + "\n");
		System.out.println("Based on yet another tool to read power production of SMA solar inverters by SBFspot (https://sbfspot.codeplex.com)\n");
		System.out.println("(c) 2015, Hoogterp\n");
	    System.out.println("Compiled for " + System.getProperty("os.name") + " in Java (" + System.getProperty("os.arch") + ")");
	    if (ShowHelp != 0)
	    {
	    	System.out.println("SBFspot [-options]\n");
	    	//bluetooth disabled
	    	//System.out.println(" -scan          Scan for bluetooth enabled SMA inverters.\n");
	    	System.out.println(" -d#            Set debug level: 0-5 (0=none, default=2)\n");
	    	System.out.println(" -v#            Set verbose output level: 0-5 (0=none, default=2)\n");
	    	System.out.println(" -finq          Force Inquiry (Inquire inverter also during the night)\n");
	    	System.out.println(" -q             Quiet (No output)\n");	    	
	    	//System.out.println(" -installer     Login as installer\n");
	    	//System.out.println(" -password:xxxx Installer password\n");
	    }
	}
}
