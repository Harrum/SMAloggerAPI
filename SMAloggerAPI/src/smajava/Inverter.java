package smajava;

import eth.ethPacket;

public class Inverter extends SmaConnection
{
	private short SUSyID;
	private long Serial;
	
	public Inverter(String ip, Ethernet ethernet)
	{		
		//Each inverters has his own connection but uses the same ethernet socket.
		//Sma connection constructor
		super(ethernet, ip);
	}
	
	public String GetIP()
	{
		return super.ip;
	}
	
	public int Logon(long userGroup, char[] password)
	{
		int rc = 0;
		//First initialize the connection.
		InitConnection();
		if ((rc = GetPacket()) == E_SBFSPOT.E_OK)
	    {
	    	ethPacket pckt = new ethPacket(ethernet.pcktBuf);
	    	SUSyID = pckt.Source.SUSyID;
	    	Serial = pckt.Source.Serial;
	    	//SMALogoff();
	    }
		else
		{
			System.err.println("ERROR: Connection to inverter failed!\n");
			System.err.println("Is " + ip + " the correct IP?\n");
			System.err.println("Please check IP_Address in SBFspot.cfg!\n");
			return E_SBFSPOT.E_INIT;
		}
		//Then login.
		if (SMALogin(userGroup, password) != E_SBFSPOT.E_OK)
	    {
	        System.err.printf("Logon failed. Check '%s' Password\n", userGroup == SmaLogger.UG_USER? "USER":"INSTALLER");
	        SMALogoff();
	        return E_SBFSPOT.E_INVPASSW;
	    }
		return 1;
	}
	
	public void Logoff()
	{
		SMALogoff();
	}
	
	/*
	public void GetInverterData(InverterDataType dataType)
	{
		
	}*/
	
	public void GetDayData()
	{
		
	}
	
	public void GetMonthData()
	{
		
	}
	
	public void GetEventData()
	{
		
	}
}
