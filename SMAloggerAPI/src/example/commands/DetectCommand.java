package example.commands;

import inverter.Inverter;

import java.util.List;

import smajava.SmaLogger;
import example.SmaConsoleCMD;

public class DetectCommand extends SmaConsoleCMD
{
	SmaLogger smaLogger;
	
	public DetectCommand(SmaLogger smaLogger) 
	{	
		super("Detect inverters");
		this.smaLogger = smaLogger;
	}

	@Override
	public void Execute() 
	{
		System.out.println("Detecting inverters...");
		List<Inverter> inverters = smaLogger.DetectDevices();
		for(int i = 0; i < inverters.size(); i++)
		{
			System.out.printf("%d - %s", i, inverters.get(i));
		}
	}
}
