package example.commands;

import example.SmaConsole;
import example.SmaConsoleCMD;

public class ExitCommand extends SmaConsoleCMD
{
	public ExitCommand()
	{
		super("Exit");
	}

	@Override
	public void Execute() 
	{
		System.out.println("Exiting...");
		SmaConsole.running = false;
	}
}