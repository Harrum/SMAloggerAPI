package example;

public abstract class SmaConsoleCMD 
{
	private String cmdName;
	
	public SmaConsoleCMD(String name)
	{
		cmdName = name;
	}
	
	public String GetName()
	{
		return cmdName;
	}
	
	public abstract void Execute();	
}
