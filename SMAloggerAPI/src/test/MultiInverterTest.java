package test;

import java.util.ArrayList;
import java.util.List;

import inverter.Inverter;
import inverterdata.InverterDataType;
import smajava.SmaLogger;
import smajava.misc;

/**
 * Example of how using this api for multiple inverters should work.
 * NOTE: this is untested by me as I only have access to one inverter.
 *
 */
public class MultiInverterTest {

	public static void main(String[] args) 
	{
		final String PASSWORD = "0000";	//Default password
		
		SmaLogger smaLogger = new SmaLogger();
		List<Inverter> inverters = new ArrayList<Inverter>();
		int rc = 0;
		
		System.out.println("Initializing SMA Logger");
		rc = smaLogger.Initialize(args);
		
		if(rc == -1)
		{
			System.out.println("Failed to initialize SMA Logger");
			return;
		}
		else
		{
			System.out.println("SMA Logger succesfully initialized");
		}
		
		//Network detection
		inverters = smaLogger.DetectDevices();		
		
		if(inverters.size() > 0)
		{
			System.out.printf("Found %d inverter(s)...\n", inverters.size());
			for(int i = 0; i < inverters.size(); i++)
			{
				System.out.printf("\t%d  -  %s\n", i, inverters.get(i).GetIP());
			}
		}
		else
		{
			System.out.println("No inverters detected...");
			System.exit(0);
		}
		
		System.out.println("logging on inverters...");
		
		for(Inverter inverter : inverters)
		{
			System.out.printf("Inverter %s logged on... ", inverter.GetIP());
			if(inverter.Logon(PASSWORD) > -1)
				System.out.printf("Succesful \n");
			else
				System.out.printf("Unsuccesful\n");
		}
		
		System.out.println("Getting some data...");
		
		for(Inverter inverter : inverters)
		{
			System.out.printf("#####\n"
					+ "\tInverter data for %s\n"
					+ "#####\n", inverter.GetIP());
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
		    
		    if ((rc = inverter.GetInverterData(InverterDataType.EnergyProduction)) != 0)
		        System.out.printf("getEnergyProduction returned an error: %d\n", rc);

		    if ((rc = inverter.GetInverterData(InverterDataType.OperationTime)) != 0)
		        System.out.printf("getOperationTime returned an error: %d\n", rc);
		    else
		    {
	            System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
	            System.out.println("Energy Production:");
	            System.out.printf("\tEToday: %.3fkWh\n", misc.tokWh(inverter.Data.EToday));
	            System.out.printf("\tETotal: %.3fkWh\n", misc.tokWh(inverter.Data.ETotal));
	            System.out.printf("\tOperation Time: %.2fh\n", misc.toHour(inverter.Data.OperationTime));
	            System.out.printf("\tFeed-In Time  : %.2fh\n", misc.toHour(inverter.Data.FeedInTime));
		    }
		}
		
		System.out.println("logging off inverters...");

		for(Inverter inverter : inverters)
		{
			inverter.Logoff();
		}
		
		System.out.println("Shutting down SMA Logger.");
		smaLogger.ShutDown();
	}
}
