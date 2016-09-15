package example;

import smajava.SmaLogger;
import example.commands.DetectCommand;
import example.commands.ExitCommand;

public class MainMenu extends OptionsMenu
{
	public MainMenu(SmaLogger smaLogger)
	{
		super("Main Menu");
		AddCommand(new ExitCommand());
		AddCommand(new DetectCommand(smaLogger));
	}
}
