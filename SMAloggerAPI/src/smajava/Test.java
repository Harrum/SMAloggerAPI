package smajava;

import java.util.List;

public class Test 
{
	public static void main(String[] args) 
	{
		SmaLogger smaLogger = new SmaLogger();
		List<Inverter> inverters;
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
		}
		
		System.out.println("Shutting down SMA Logger.");
		smaLogger.ShutDown();
	}
}
