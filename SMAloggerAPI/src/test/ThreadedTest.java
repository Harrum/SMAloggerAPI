package test;

import inverter.Inverter;
import inverterdata.InverterDataType;
import smajava.SmaLogger;
import smajava.misc;

/**
 * Example of usage of this api while using a thread to keep reading the inverter after login.
 *
 */
public class ThreadedTest implements Runnable
{
	final String PASSWORD = "0000";	//Default password
	final int RUN_COUNT = 5;	//Number of times to read the inverter.
	final int WAIT_TIME = 5000;	//Wait time between reading the inverter again.
	SmaLogger smaLogger;
	Inverter inverter;
	
	public void Initialize(String[] args)
	{
		smaLogger = new SmaLogger();
		
		int rc = 0;
		
		System.out.println("Initializing SMA Logger");
		rc = smaLogger.Initialize(args);
		
		if(rc == -1)
		{
			System.out.println("Failed to initialize SMA Logger");
			System.exit(-1);
		}
		else
		{
			System.out.println("SMA Logger succesfully initialized");
		}
	}
	
	public void Start()
	{
		//Manual creation
		inverter = smaLogger.CreateInverter("192.168.1.110");
		Thread t = new Thread(this);
		t.start();
	}
	
	public void Stop()
	{
		System.out.println("logging off inverter...");

		inverter.Logoff();
		
		System.out.println("Shutting down SMA Logger.");
		smaLogger.ShutDown();
	}

	@Override
	public void run() 
	{
		boolean loggedOn = false;
		int counter = RUN_COUNT;
		int rc = 0;
		System.out.println("logging on inverter...");
		
		System.out.printf("Inverter %s logged on... ", inverter.GetIP());
		if(inverter.Logon(PASSWORD) > -1)
		{
			System.out.printf("Succesful \n");
			loggedOn = true;
		}
		else
		{
			System.out.printf("Unsuccesful\n");	
			loggedOn = false;
		}
		if ((rc = inverter.GetInverterData(InverterDataType.SoftwareVersion)) != 0)
	        System.out.printf("getSoftwareVersion returned an error: %d\n", rc);
	    
	    if ((rc = inverter.GetInverterData(InverterDataType.TypeLabel)) != 0)
	        System.out.printf("getTypeLabel returned an error: %d\n", rc);    
	    else
	    {
            System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
            System.out.printf("Device Name:      %s\n", new String(inverter.Data.DeviceName));
            System.out.printf("Device Class:     %s\n", new String(inverter.Data.DeviceClass));
            System.out.printf("Device Type:      %s\n", new String(inverter.Data.DeviceType));
            System.out.printf("Software Version: %s\n", inverter.Data.SWVersion);
            System.out.printf("Serial number:    %d\n", inverter.Data.Serial);
	    }
		if(loggedOn)
		{
			while(counter > 0)
			{
				if ((rc = inverter.GetInverterData(InverterDataType.EnergyProduction)) != 0)
			        System.out.printf("getEnergyProduction returned an error: %d\n", rc);
			    else
			    {
			    	System.out.println("==================================");
		            System.out.println("Energy Production:");
		            System.out.printf("\tEToday: %.3fkWh\n", misc.tokWh(inverter.Data.EToday));
		            System.out.printf("\tETotal: %.3fkWh\n", misc.tokWh(inverter.Data.ETotal));
			    }
				
				if ((rc = inverter.GetInverterData(InverterDataType.SpotACPower)) != 0)
			        System.out.printf("getSpotACPower returned an error: %d\n", rc);

			    if ((rc = inverter.GetInverterData(InverterDataType.SpotACVoltage)) != 0)
			        System.out.printf("getSpotACVoltage returned an error: %d\n", rc);

			    if ((rc = inverter.GetInverterData(InverterDataType.SpotACTotalPower)) != 0)
			        System.out.printf("getSpotACTotalPower returned an error: %d\n", rc);

			    //Calculate missing AC Spot Values
			    inverter.CalcMissingSpot();

		        System.out.println("AC Spot Data:");
		        System.out.printf("\tPhase 1 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(inverter.Data.Pac1), misc.toVolt(inverter.Data.Uac1), misc.toAmp(inverter.Data.Iac1));
		        System.out.printf("\tPhase 2 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(inverter.Data.Pac2), misc.toVolt(inverter.Data.Uac2), misc.toAmp(inverter.Data.Iac2));
		        System.out.printf("\tPhase 3 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(inverter.Data.Pac3), misc.toVolt(inverter.Data.Uac3), misc.toAmp(inverter.Data.Iac3));
		        System.out.printf("\tTotal Pac   : %7.3fkW\n", misc.tokW(inverter.Data.TotalPac));
				
				counter--;
				try 
				{
					Thread.sleep(WAIT_TIME);
				} 
				catch (InterruptedException e) 
				{
					counter = 0;
					e.printStackTrace();
				}
			}
		}
		Stop();
	}
	
	public static void main(String[] args) 
	{	
		ThreadedTest tt = new ThreadedTest();
		tt.Initialize(args);
		tt.Start();
	}
}
