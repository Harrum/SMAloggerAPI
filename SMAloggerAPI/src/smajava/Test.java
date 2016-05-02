package smajava;

import java.util.ArrayList;
import java.util.List;

public class Test 
{
	public static void main(String[] args) 
	{
		Config config = new Config();
		SmaLogger smaLogger = new SmaLogger(config);
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
		
		//Manual creation
		inverters.add(smaLogger.CreateInverter("192.168.1.110"));
		
		//Network detection
		//inverters = smaLogger.DetectDevices();		
		
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
		}

		System.out.println("logging on inverters...");
		for(Inverter inv : inverters)
		{
			System.out.printf("Inverter %s logged on... ", inv.GetIP());
			if(inv.Logon(config.userGroup, config.SMA_Password) > 0)
				System.out.printf("Succesful \n");
			else
				System.out.printf("Unsuccesful\n");
		}
		
		System.out.println("logging off inverters...");
		for(Inverter inv : inverters)
		{
			inv.Logoff();
		}
		
		System.out.println("Shutting down SMA Logger.");
		smaLogger.ShutDown();
	}
}
