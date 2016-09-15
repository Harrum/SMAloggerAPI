package example;

import java.util.ArrayList;
import java.util.List;

public abstract class OptionsMenu 
{
	private final String BORDER = "================================";
	
	private List<SmaConsoleCMD> commands;
	private String menuName;
	
	public OptionsMenu(String name)
	{
		menuName = name;
		commands = new ArrayList<SmaConsoleCMD>();
	}
	
	public void AddCommand(SmaConsoleCMD command)
	{
		commands.add(command);
	}
	
	public void PrintMenu()
	{
		System.out.println(BORDER);
		System.out.printf("%s %s %s\n", BORDER.substring(0, menuName.length() + 2), menuName, BORDER.substring(0, menuName.length() + 2));
		System.out.println(BORDER);
		
		for(int i = 0; i < commands.size(); i++)
		{
			System.out.printf("%d - %s\n", i, commands.get(i).GetName());
		}
	}
	
	public void HandleInput(int cmd)
	{
		if(cmd < commands.size())
		{
			commands.get(cmd).Execute();
		}
		else
		{
			System.out.println("Wrong input given...");
		}
	}
}
