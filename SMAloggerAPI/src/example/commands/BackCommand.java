package example.commands;

import example.OptionsMenu;
import example.SmaConsoleCMD;

public class BackCommand extends SmaConsoleCMD
{
	private OptionsMenu prevMenu;
	
	public BackCommand(OptionsMenu prevMenu)
	{
		super("Back");
		this.prevMenu = prevMenu;
	}

	@Override
	public void Execute() 
	{
		prevMenu.PrintMenu();
	}
}