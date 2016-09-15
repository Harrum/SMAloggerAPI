package example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import smajava.SmaLogger;

public class SmaConsole 
{
	public static boolean running = false;
	
	public static void main(String[] args) 
	{
		SmaLogger smaLogger = new SmaLogger();
		if(smaLogger.Initialize(args) != 0)
		{
			System.out.println("Failed to initiaize SmaLogger API");
			System.exit(-1);
		}
		running = true;
		MainMenu mainMenu = new MainMenu(smaLogger);
		mainMenu.PrintMenu();
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String line =  "";
		
		while(running)
		{
			try 
			{
				line = input.readLine();
				try 
				{
					int cmd = Integer.parseInt(line);
					mainMenu.HandleInput(cmd);
				} 
				catch (NumberFormatException e) 
				{
					System.out.println("Wrong input given: " + line);
				}			
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		smaLogger.ShutDown();
	}
}
