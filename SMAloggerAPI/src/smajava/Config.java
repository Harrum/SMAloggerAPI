package smajava;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Config 
{
	final int MAX_CFG_AD = 300; // Days
	final int MAX_CFG_AM = 300; // Months
	final int MAX_CFG_AE = 300; // Months
	
	private final int MAX_PATH = 260;
	
	public String	ConfigFile;			//Fullpath to configuration file
	public String	AppPath;
	public String	BT_Address;			//Inverter bluetooth address 12:34:56:78:9A:BC
    public String	IP_Address;			//Inverter IP address 192.168.178.123 (for Speedwirecommunication )
    public int		BT_Timeout;
    public int		BT_ConnectRetries;
	public short   IP_Port;
	public char[]	SMA_Password = new char[13];
    public float	latitude;
    public float	longitude;
    public Date	archdata_from;
    public Date	archdata_to;
    public char	delimiter;			//CSV field delimiter
    public int		precision;			//CSV value precision
    public char	decimalpoint;		//CSV decimal point
    public String	outputPath;
    public String	outputPath_Events;
    public String	plantname;
    public String sqlDatabase;
    public String sqlHostname;
    public String sqlUsername;
    public String sqlUserPassword;
	public int		synchTime;				// 1=Synch inverter time with computer time (default=0)
	public float	sunrise;
	public float	sunset;
	public int		isLight;
	public int		calcMissingSpot;		// 0-1
	public String	DateTimeFormat;
	public String	DateFormat;
	public String	TimeFormat;
	public int		CSV_Export;
	public int		CSV_Header;
	public int		CSV_ExtendedHeader;
	public int		CSV_SaveZeroPower;
	public int		SunRSOffset;			// Offset to start before sunrise and end after sunset
	public long		userGroup;				// USER|INSTALLER
	public String	prgVersion;
//	VOLT_LOGGING VoltLogging;
	public int		SpotTimeSource;			// 0=Use inverter time; 1=Use PC time in Spot CSV
	public int		SpotWebboxHeader;		// 0=Use standard Spot CSV hdr; 1=Webbox style hdr
	public String	locale;		// default en-US
	//int		PVoutput;				// 0-1
	//int		PVoutput_SID;
	//char		PVoutput_Key[42];
	//int		PVoutput_InvTemp;		// Upload Inverter Temperature to PVoutput
	//int		PVoutput_InvTempMapTo;	// Upload Inverter Temperature to V5 or if in donation mode to v7..v12
	//int		PVoutput_CumulNRG;		// Cumulative Flag (0=Today's Energy or 1=Total Energy)
	public int		MIS_Enabled;			// Multi Inverter Support
	public String	timezone;
	public TimeZone tz;

	//Commandline settings
	public int		debug;				// -d			Debug level (0-5)
	public int		verbose;			// -v			Verbose output level (0-5)
	public int		archDays;			// -ad			Number of days back to get Archived DayData (0=disabled, 1=today, ...)
	public int		archMonths;			// -am			Number of months back to get Archived MonthData (0=disabled, 1=this month, ...)
	public int		archEventMonths;	// -ae			Number of months back to get Archived Events (0=disabled, 1=this month, ...)
	//int		upload;				// -u			Upload to online monitoring systems (PVOutput, ...)
	public int		forceInq;			// -finq		Inquire inverter also during the night
	public int		wsl;				// -wsl			WebSolarLog support (http://www.websolarlog.com/index.php/tag/sma-spot/)
	public int		quiet;				// -q			Silent operation (No output except for -wsl)
	public int		nocsv;				// -nocsv		Disables CSV export (Overrules CSV_Export in config)
	public int		nospot;				// -sp0			Disables Spot CSV export
	public int		nosql;				// -nosql		Disables SQL export
	public int		loadlive;			// -loadlive	Force settings to prepare for live loading to http://pvoutput.org/loadlive.jsp

	public int parseCmdline(String[] argv)
	{
		debug = 0;				// debug level - 0=none, 5=highest
		verbose = 0;			// verbose level - 0=none, 5=highest
	    archDays = 1;			// today only
	    archMonths = 1;			// this month only
		archEventMonths = 1;	// this month only
//	    upload = 0;				// upload to PVoutput and others (See config file)
	    forceInq = 0;			// Inquire inverter also during the night
	    userGroup = SmaLogger.UG_USER;
	    // WebSolarLog support (http://www.websolarlog.com/index.php/tag/sma-spot/)
	    // This is an undocumented feature and should only be used for WebSolarLog
	    wsl = 0;
	    quiet = 0;
	    nocsv = 0;
	    nospot = 0;
		nosql = 0;
	    // 123Solar Web Solar logger support(http://www.123solar.org/)
	    // This is an undocumented feature and should only be used for 123solar
	    //s123 = S123_NOP;
		loadlive = 0;	//force settings to prepare for live loading to http://pvoutput.org/loadlive.jsp

	    //Set quiet mode
	    for (int i = 1; i < argv.length; i++)
		{
	        if (argv[i].equals("-q"))
	        {
	            quiet = 1;
	            break;
	        }
		}

		AppPath = new File(".").getAbsolutePath();
		int pos = AppPath.lastIndexOf('\\');
		if (pos != -1)
			AppPath = AppPath.substring(++pos);
		else
			AppPath = "";

		//Build fullpath to config file (SBFspot.cfg should be in same folder as SBFspot.exe)
		ConfigFile = AppPath + "\\support_files\\SBFspot.cfg";

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

	        //Set #days (archived daydata)
	        if (argv[i].startsWith("-ad"))
	        {
				if (argv[i].length() > 6)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
				lValue = Integer.parseInt(argv[i].substring(3, argv[i].length()));
	            if ((lValue < 0) || (lValue > MAX_CFG_AD) || (pEnd != 0))
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                archDays = lValue;

	        }

	        //Set #months (archived monthdata)
	        else if (argv[i].startsWith("-am"))
	        {
				if (argv[i].length() > 6)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            lValue = Integer.parseInt(argv[i].substring(3, argv[i].length()));
	            if ((lValue < 0) || (lValue > MAX_CFG_AM) || (pEnd != 0))
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                archMonths = lValue;
	        }

			//Set #days (archived events)
			else if (argv[i].startsWith("-ae"))
			{
				if (argv[i].length() > 6)
				{
					InvalidArg(argv[i]);
					return -1;
				}
				lValue = Integer.parseInt(argv[i].substring(3, argv[i].length()));
				if ((lValue < 0) || (lValue > MAX_CFG_AE) || (pEnd != 0))
				{
					InvalidArg(argv[i]);
					return -1;
				}
				else
					archEventMonths = lValue;

			}

	        //Set debug level
	        else if(argv[i].startsWith("-d"))
	        {
	            lValue = Integer.parseInt(argv[i].substring(2, argv[i].length()));
	            if (argv[i].length() == 2) lValue = 2;	// only -d sets medium debug level
	            if ((lValue < 0) || (lValue > 5) || (pEnd != 0))
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
	                debug = lValue;
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
	                verbose = lValue;
	        }

			//force settings to prepare for live loading to http://pvoutput.org/loadlive.jsp
			else if (argv[i].equals("-liveload") || argv[i].equals("-loadlive"))
				loadlive = 1;

			//Set upload flag
	        //else if (stricmp(argv[i], "-u") == 0)
	        //    cfg->upload = 1;

	        //Set inquiryDark flag
	        else if (argv[i].equals("-finq"))
	            forceInq = 1;

	        //Set WebSolarLog flag (Undocumented - For WSL usage only)
	        else if (argv[i].equals("-wsl"))
	            wsl = 1;

	        //Set 123Solar command value (Undocumented - For WSL usage only)
	        /*disabled
	        else if (strnicmp(argv[i], "-123s", 5) == 0)
	        {
	            if (strlen(argv[i]) == 5)
	                cfg->s123 = S123_DATA;
	            else if (strnicmp(argv[i]+5, "=DATA", 5) == 0)
	                cfg->s123 = S123_DATA;
	            else if (strnicmp(argv[i]+5, "=INFO", 5) == 0)
	                cfg->s123 = S123_INFO;
	            else if (strnicmp(argv[i]+5, "=SYNC", 5) == 0)
	                cfg->s123 = S123_SYNC;
	            else if (strnicmp(argv[i]+5, "=STATE", 6) == 0)
	                cfg->s123 = S123_STATE;
	            else
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	        }
			*/
	        //Set NoCSV flag (Disable CSV export - Overrules Config setting)
	        else if (argv[i].equals("-nocsv"))
	            nocsv = 1;

	        //Set NoSQL flag (Disable SQL export)
	        else if (argv[i].equals("-nosql"))
	            nosql = 1;

			//Set NoSpot flag (Disable Spot CSV export)
	        else if (argv[i].equals("-sp0"))
	            nospot = 1;

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

	        //look for alternative config file
	        else if (argv[i].startsWith("-cfg"))
	        {
	            if (argv[i].length() == 4)
	            {
	                InvalidArg(argv[i]);
	                return -1;
	            }
	            else
				{
					//Fix Issue G90 (code.google.com)
					//If -cfg arg has no '\' it's only a filename and should be in the same folder as SBFspot executable
					ConfigFile = argv[i].substring(4, argv[i].length());
					if (ConfigFile.indexOf("\\") == -1)
						ConfigFile = AppPath + argv[i].substring(3, argv[i].length());
				}
	        }

	        //Scan for bluetooth devices
	        else if (argv[i].equals("-scan"))
	        {
	        	InvalidArg(argv[i] + " bluetooth not supported.");
                return -1;
	        }

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
	        verbose = 0;
	        debug = 0;
	    }

	    return 0;
	}
	
	/* read Config from file */
	public int ReadConfigFile() throws IOException
	{
	    //Initialise config structure and set default values
		prgVersion = SmaLogger.VERSION;
	    //memset(BT_Address, 0, sizeof(BT_Address));
	    //memset(IP_Address, 0, sizeof(IP_Address));
		BT_Address = "";
		IP_Address = "";
	    outputPath = "";
		outputPath_Events = "";
	    if (userGroup == SmaLogger.UG_USER) SMA_Password[0] = 0;
	    plantname = "";
	    latitude = 0.0f;
	    longitude = 0.0f;
	    archdata_from = null;
	    archdata_to = null;
	    delimiter = ';';
	    precision = 3;
	    decimalpoint = ',';
	    BT_Timeout = 5;
	    BT_ConnectRetries = 10;

	    calcMissingSpot = 0;
	    DateTimeFormat = "d/m/Y H:M:S";
	    DateFormat = "d/m/Y";
	    TimeFormat = "H:M:S";
	    synchTime = 1;
	    CSV_Export = 1;
	    CSV_ExtendedHeader = 1;
	    CSV_Header = 1;
	    CSV_SaveZeroPower = 1;
	    SunRSOffset = 900;
//	    PVoutput = 0;
//	    VoltLogging = VL_AC_MAX;
	    SpotTimeSource = 0;
	    SpotWebboxHeader = 0;
	    MIS_Enabled = 0;
	    locale = "en-US";
//		PVoutput_InvTemp=0;
//		PVoutput_InvTempMapTo=5;
//		PVoutput_CumulNRG = 0;

	    final String CFG_Boolean = "(0-1)";
	    final String CFG_InvalidValue = "Invalid value for '%s' %s\n";

	    FileReader fr;
	    //BufferedReader br = new BufferedReader(fr);

	    try
	    {
	    	fr = new FileReader(ConfigFile);
	    }
	    catch(Exception e)
	    {
	    	System.err.println("Error! Could not open file " + ConfigFile + "\n" + e.getMessage());
	    	return -1;
	    }

		if (verbose >= 2)
			System.out.println("Reading config '" + ConfigFile + "'");

	    char pEnd = 0;
	    int lValue = 0;
	    String line = "";
	    int rc = 0;
	    
	    BufferedReader br = new BufferedReader(fr);
	    
	    while((line = br.readLine()) != null)
	    {
	        if (line.length() > 0 && line.charAt(0) != '#' && line.charAt(0) != 0 && line.charAt(0) != 10)
	        {
	        	int index = line.indexOf("=");
	        	String variable = line.substring(0, index);
	        	String value = line.substring(index + 1, line.length());
        		
	            if ((value != null) && (value.trim() != ""))
	            {
					//if(stricmp(variable, "BTaddress") == 0) strncpy(BT_Address, value, sizeof(BT_Address));
	                if(variable.equals("IP_Address")) 
	                	IP_Address = value;
					else if(variable.equals("Password"))
					{
	                    if(userGroup == SmaLogger.UG_USER) 
	                    {
	                    	for(int ch = 0; ch < value.length(); ch++)
	                    	{
	                    		SMA_Password[ch] = value.charAt(ch);
	                    	}
	                    	//SMA_Password = value;
	                    }
					}
					else if(variable.equals("OutputPath")) 
						outputPath = value;
					else if(variable.equals("OutputPathEvents")) 
						outputPath_Events = value;
					else if(variable.equals("Latitude")) 
						latitude = Float.valueOf(value);
					else if(variable.equals("Longitude")) 
						longitude = Float.valueOf(value);
					else if(variable.equals("Plantname")) 
						plantname = value;
					else if(variable.equals("CalculateMissingSpotValues"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        calcMissingSpot = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("DateTimeFormat")) 
						DateTimeFormat = value;
					else if(variable.equals("DateFormat")) 
						DateFormat = value;
					else if(variable.equals("TimeFormat")) 
						TimeFormat = value;
					else if(variable.equals("DecimalPoint"))
	                {
						if (value.equals("comma")) 
							decimalpoint = ',';
						else if ((value.equals("dot")) || (value.equals("point"))) 
							decimalpoint = '.'; // Fix Issue 84 - 'Point' is accepted for backward compatibility
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "(comma|dot)");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Delimiter"))
	                {
						if (value.equals("comma")) 
							delimiter = ',';
						else if (value.equals("semicolon")) 
							delimiter = ';';
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "(comma|semicolon)");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("SynchTime"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        synchTime = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Export"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        CSV_Export = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_ExtendedHeader"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        CSV_ExtendedHeader = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Header"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        CSV_Header = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_SaveZeroPower"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        CSV_SaveZeroPower = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("SunRSOffset"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if ((lValue >= 0) && (lValue <= 3600) && (pEnd == 0))
	                        SunRSOffset = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "(0-3600)");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Spot_TimeSource"))
	                {
						if (value.equals("Inverter")) 
							SpotTimeSource = 0;
						else if (value.equals("Computer")) 
							SpotTimeSource = 1;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "Inverter|Computer");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("CSV_Spot_WebboxHeader"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        SpotWebboxHeader = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
	                else if(variable.equals("MIS_Enabled"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if (((lValue == 0) || (lValue == 1)) && (pEnd == 0))
	                        MIS_Enabled = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, CFG_Boolean);
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("Locale"))
					{
						if ((value.equals("de-DE")) ||
							(value.equals("en-US")) ||
							(value.equals("fr-FR")) ||
							(value.equals("nl-NL")) ||
							(value.equals("it-IT")) ||
							(value.equals("es-ES"))
							)
							locale = value;
						else
						{
							System.err.printf(CFG_InvalidValue, variable, "de-DE|en-US|fr-FR|nl-NL|it-IT|es-ES");
	                        rc = -2;
						}
					}
					else if(variable.equals("BTConnectRetries"))
	                {
	                    lValue = Integer.parseInt(value);
	                    if ((lValue >= 0) && (lValue <= 15) && (pEnd == 0))
							BT_ConnectRetries = lValue;
	                    else
	                    {
	                    	System.err.printf(CFG_InvalidValue, variable, "(1-15)");
	                        rc = -2;
	                    }
	                }
					else if(variable.equals("Timezone"))
					{
						timezone = value;
						try
						{
							//get the timezone
							tz = TimeZone.getTimeZone(value);
						}
						catch(Exception e)
						{
							System.err.printf("Invalid timezone specified: " + value);
							rc = -2;
						}
					}

					else if(variable.equals("SQL_Database"))
						sqlDatabase = value;
	                /*
	#if defined(USE_MYSQL)
					else if(stricmp(variable, "SQL_Hostname") == 0)
						sqlHostname = value;
					else if(stricmp(variable, "SQL_Username") == 0)
						sqlUsername = value;
					else if(stricmp(variable, "SQL_Password") == 0)
						sqlUserPassword = value;
	#endif*/
	                else
	                {
	                	System.err.printf("Warning: Ignoring keyword '%s'\n", variable);
                    	rc = -2;
	                }
	            }
	        }
	    }
	    //fr.close();
	    br.close();

        IP_Port = 9522;

	    if (SMA_Password.length == 0)
	    {
	    	System.err.printf("Missing USER Password.\n");
        	rc = -2;
	    }

	    if (decimalpoint == delimiter)
	    {
	    	System.err.printf("'CSV_Delimiter' and 'DecimalPoint' must be different character.\n");
        	rc = -2;
	    }

	    //Overrule CSV_Export from config with Commandline setting -nocsv
	    if (nocsv == 1)
	        CSV_Export = 0;

	    //Silently enable CSV_Header when CSV_ExtendedHeader is enabled
	    if (CSV_ExtendedHeader == 1)
	        CSV_Header = 1;

	    if (outputPath.length() == 0)
	    {
	    	System.err.printf("Missing OutputPath.\n");
        	rc = -2;
	    }

		//If OutputPathEvents is omitted, use OutputPath
		if (outputPath_Events.length() == 0)
			outputPath_Events = outputPath;

	    if (plantname.length() == 0)
	        plantname = "MyPlant";

		if (timezone == null)
		{
			System.err.printf("Missing timezone.\n");
        	rc = -2;
		}
		
		// If 1st day of the month and -am1 specified, force to -am2 to get last day of prev month
		if (archMonths == 1)
		{
			Calendar now;;
			now = Calendar.getInstance();
			if(now.get(Calendar.DAY_OF_MONTH) == 1)
				archMonths++;
		}

		if (verbose > 2) ShowConfig();
	    return rc;
	}
	
	private void ShowConfig()
	{
		System.out.println("Configuration settings:");
		if (IP_Address.length() == 0)	// No IP address -> Show BT address
			System.out.println("\nBTAddress=" + BT_Address.toString());
		if (BT_Address.length() == 0)	// No BT address -> Show IP address
			System.out.println("\nIP_Address=" + IP_Address.toString());
		System.out.print("\nPassword=<undisclosed>" + 
			"\nMIS_Enabled=" + MIS_Enabled +
			"\nPlantname=" + new String(plantname) + 
			"\nOutputPath=" + new String(outputPath) + 
			"\nOutputPathEvents=" + new String(outputPath_Events) + 
			"\nLatitude=" + latitude + 
			"\nLongitude=" + longitude + 
			"\nTimezone=" + timezone + 
			"\nCalculateMissingSpotValues=" + calcMissingSpot + 
			"\nDateTimeFormat=" + new String(DateTimeFormat) + 
			"\nDateFormat=" + new String(DateFormat) + 
			"\nTimeFormat=" + new String(TimeFormat) + 
			"\nSynchTime=" + synchTime + 
			"\nSunRSOffset=" + SunRSOffset + 
			"\nDecimalPoint=" + decimalpoint + 
			"\nCSV_Delimiter=" + delimiter + 
			"\nPrecision=" + precision + 
			"\nCSV_Export=" + CSV_Export + 
			"\nCSV_ExtendedHeader=" + CSV_ExtendedHeader + 
			"\nCSV_Header=" + CSV_Header + 
			"\nCSV_SaveZeroPower=" + CSV_SaveZeroPower + 
			"\nCSV_Spot_TimeSource=" + SpotTimeSource + 
			"\nCSV_Spot_WebboxHeader=" + SpotWebboxHeader + 
			"\nLocale=" + new String(locale) + 
			"\nBTConnectRetries=" + BT_ConnectRetries);

		/*
	#if defined(USE_MYSQL) || defined(USE_SQLITE)
		std::cout << "SQL_Database=" << cfg->sqlDatabase << std::endl;
	#endif

	#if defined(USE_MYSQL)
		std::cout << "SQL_Hostname=" << cfg->sqlHostname << \
			"\nSQL_Username=" << cfg->sqlUsername << \
			"\nSQL_Password=<undisclosed>" << std::endl;
	#endif*/
		System.out.println("\n### End of Config ###");
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
	    	System.out.println(" -ad#           Set #days for archived daydata: 0-" + MAX_CFG_AD + "\n");
	    	System.out.println("                0=disabled, 1=today (default), ...\n");
	    	System.out.println(" -am#           Set #months for archived monthdata: 0-" + MAX_CFG_AM + "\n");
	    	System.out.println("                0=disabled, 1=current month (default), ...\n");
	    	System.out.println(" -ae#           Set #months for archived events: 0-" + MAX_CFG_AE + "\n");
	    	System.out.println("                0=disabled, 1=current month (default), ...\n");
	    	System.out.println(" -cfgX.Y        Set alternative config file to X.Y (multiple inverters)\n");
	    	System.out.println(" -u             Upload to online monitoring system (see config file)\n");
	    	System.out.println(" -finq          Force Inquiry (Inquire inverter also during the night)\n");
	    	System.out.println(" -q             Quiet (No output)\n");
	    	System.out.println(" -nocsv         Disables CSV export (Overrules CSV_Export in config)\n");
	    	System.out.println(" -nosql         Disables SQL export\n");
	    	System.out.println(" -sp0           Disables Spot.csv export\n");
	    	System.out.println(" -installer     Login as installer\n");
	    	System.out.println(" -password:xxxx Installer password\n");
	    	System.out.println(" -loadlive      Use predefined settings for manual upload to pvoutput.org\n");
	    }
	}
}
