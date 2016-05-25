package inverterdata;

import inverter.InvDeviceClass;
import inverter.LriDef;

import java.util.ArrayList;
import java.util.Date;

import smajava.TagDefs;
import smajava.misc;
import smajava.misc.DEBUG;

public class InverterData 
{
	public char[] DeviceName = new char[33];    //32 bytes + terminating zero
	public String BTAddress;
	public String IPAddress;
	public short SUSyID;
	public long Serial;
	public char NetID;
	public float BT_Signal;
	public long InverterDatetime;
	public long WakeupTime;
	public long SleepTime;
	public long Pdc1;
	public long Pdc2;
	public long Udc1;
	public long Udc2;
	public long Idc1;
	public long Idc2;
	public long Pmax1;
	public long Pmax2;
	public long Pmax3;
	public long TotalPac;
	public long Pac1;
	public long Pac2;
	public long Pac3;
	public long Uac1;
	public long Uac2;
	public long Uac3;
	public long Iac1;
    public long Iac2;
    public long Iac3;
    public long GridFreq;
    public long OperationTime;
    public long FeedInTime;
    public long EToday;
    public long ETotal;
    public short modelID;
    public char[] DeviceType = new char[64];
    public char[] DeviceClass = new char[64];
    public InvDeviceClass DevClass;
    public String SWVersion;	//"03.01.05.R"
    public int DeviceStatus;
    public int GridRelayStatus;
    /**
     * @deprecated
     * Removing this value as it doesn't seem to have any use.
     */
    @Deprecated
    public int flags;
    public DayData[] dayData = new DayData[288];
    public MonthData[] monthData = new MonthData[31];
    public ArrayList<EventData> eventData;
    public long calPdcTot;
    public long calPacTot;
    public float calEfficiency;
    public long BatChaStt;			// Current battery charge status
    public long BatDiagCapacThrpCnt;	// Number of battery charge throughputs
    public long BatDiagTotAhIn;		// Amp hours counter for battery charge
    public long BatDiagTotAhOut;		// Amp hours counter for battery discharge
    public long BatTmpVal;			// Battery temperature
    public long BatVol;				// Battery voltage
    public long BatAmp;						// Battery current
    public long Temperature;					// Inverter Temperature
    
    
    public void SetInverterData(LriDef lri, int value, Date datetime)
    {
    	String strWatt = "%-12s: %d (W) %s\n";
	    String strVolt = "%-12s: %.2f (V) %s\n";
	    String strAmp = "%-12s: %.3f (A) %s\n";
	    
    	switch (lri)
        {
        case GridMsTotW: //SPOT_PACTOT
            //This function gives us the time when the inverter was switched off
            this.SleepTime = datetime.getTime();
            this.TotalPac = value;

            if (DEBUG.Normal()) 
            	System.out.printf(strWatt, "SPOT_PACTOT", value, datetime.toString());
            break;

        case OperationHealthSttOk: //INV_PACMAX1
            this.Pmax1 = value;

            if (DEBUG.Normal()) 
            	System.out.printf(strWatt, "INV_PACMAX1", value, datetime.toString());
            break;

        case OperationHealthSttWrn: //INV_PACMAX2
            this.Pmax2 = value;

            if (DEBUG.Normal()) 
            	System.out.printf(strWatt, "INV_PACMAX2", value, datetime.toString());
            break;

        case OperationHealthSttAlm: //INV_PACMAX3
            this.Pmax3 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strWatt, "INV_PACMAX3", value, datetime.toString());
            break;

        case GridMsWphsA: //SPOT_PAC1
            this.Pac1 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strWatt, "SPOT_PAC1", value, datetime.toString());
            break;

        case GridMsWphsB: //SPOT_PAC2
            this.Pac2 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strWatt, "SPOT_PAC2", value, datetime.toString());
            break;

        case GridMsWphsC: //SPOT_PAC3
            this.Pac3 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strWatt, "SPOT_PAC3", value, datetime.toString());
            break;

        case GridMsPhVphsA: //SPOT_UAC1
            this.Uac1 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strVolt, "SPOT_UAC1", misc.toVolt(value), datetime.toString());
            break;

        case GridMsPhVphsB: //SPOT_UAC2
            this.Uac2 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strVolt, "SPOT_UAC2", misc.toVolt(value), datetime.toString());
            break;

        case GridMsPhVphsC: //SPOT_UAC3
            this.Uac3 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strVolt, "SPOT_UAC3", misc.toVolt(value), datetime.toString());
            break;

        case GridMsAphsA_1: //SPOT_IAC1
		case GridMsAphsA:
            this.Iac1 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strAmp, "SPOT_IAC1", misc.toAmp(value), datetime.toString());
            break;

        case GridMsAphsB_1: //SPOT_IAC2
		case GridMsAphsB:
            this.Iac2 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strAmp, "SPOT_IAC2", misc.toAmp(value), datetime.toString());
            break;

        case GridMsAphsC_1: //SPOT_IAC3
		case GridMsAphsC:
            this.Iac3 = value;

            if (DEBUG.Normal())  
            	System.out.printf(strAmp, "SPOT_IAC3", misc.toAmp(value), datetime.toString());
            break;

        case GridMsHz: //SPOT_FREQ
            this.GridFreq = value;

            if (DEBUG.Normal())  
            	System.out.printf("%-12s: %.2f (Hz) %s\n", "SPOT_FREQ", misc.toHz(value), datetime.toString());
            break;          

        case BatChaStt:
            this.BatChaStt = value;
            break;

        case BatDiagCapacThrpCnt:
            this.BatDiagCapacThrpCnt = value;
            break;

        case BatDiagTotAhIn:
            this.BatDiagTotAhIn = value;
            break;

        case BatDiagTotAhOut:
            this.BatDiagTotAhOut = value;
            break;

        case BatTmpVal:
            this.BatTmpVal = value;
            break;

        case BatVol:
            this.BatVol = value;
            break;

        case BatAmp:
            this.BatAmp = value;
            break;

		case CoolsysTmpNom:
			this.Temperature = value;
			break;

		default:
			if (DEBUG.Normal())  
            	System.out.printf("Wrong enum given to this method (SetInverterData), %s\n", lri.toString());
			break;
        }
    }
    
    public void SetInverterDataINVNAME(char[] deviceName, Date datetime)
    {
    	this.WakeupTime = datetime.getTime();
    	this.DeviceName = deviceName;

        if (DEBUG.Normal())  
        	System.out.printf("%-12s: '%s' %s\n", "INV_NAME", new String(this.DeviceName), datetime.toString());
    }
    
    public void SetInverterDataSWVER(String swVersion, Date datetime)
    {
    	//INV_SWVER
        this.SWVersion = swVersion;
        if (DEBUG.Normal())  
        	System.out.printf("%-12s: '%s' %s\n", "INV_SWVER", this.SWVersion, datetime.toString());
    }
    
    public void SetInverterDataAttribute(LriDef lri, int attribute, Date datetime)
    {
    	switch(lri)
    	{
		    case OperationHealth: //INV_STATUS:
                this.DeviceStatus = attribute;
		
				if (DEBUG.Normal()) 
					System.out.printf("%-12s: '%s' %s\n", "INV_STATUS", TagDefs.GetInstance().getDesc(this.DeviceStatus, "?"), datetime.toString());
		        break;
		
		    case OperationGriSwStt: //INV_GRIDRELAY
                this.GridRelayStatus = attribute;
		
		        if (DEBUG.Normal()) 
		        	System.out.printf("%-12s: '%s' %s\n", "INV_GRIDRELAY", TagDefs.GetInstance().getDesc(this.GridRelayStatus, "?"), datetime.toString());
		        break;
		        
	        case NameplateMainModel: //INV_CLASS
	            
                this.DevClass = InvDeviceClass.intToEnum(attribute);
				String devclass = TagDefs.GetInstance().getDesc(attribute);
				if (!devclass.isEmpty())
					this.DeviceClass = devclass.toCharArray();
				else
				{
					this.DeviceClass = "UNKNOWN CLASS".toCharArray();
                    System.out.printf("Unknown Device Class. Report this issue at https://sbfspot.codeplex.com/workitem/list/basic with following info:\n");
                    System.out.printf("0x%08lX and Device Class=...\n", attribute);
                }
	
	            if (DEBUG.Normal()) 
	            	System.out.printf("%-12s: '%s' %s\n", "INV_CLASS", new String(this.DeviceClass), datetime.toString());
	            break;
	            
	        case NameplateModel: //INV_TYPE
	            
				String devtype = TagDefs.GetInstance().getDesc(attribute);
				if (!devtype.isEmpty())
					this.DeviceType = devtype.toCharArray();
				else
				{
					this.DeviceType = "UNKNOWN TYPE".toCharArray();
                    System.out.printf("Unknown Inverter Type. Report this issue at https://sbfspot.codeplex.com/workitem/list/basic with following info:\n");
                    System.out.printf("0x%08lX and Inverter Type=<Fill in the exact type> (e.g. SB1300TL-10)\n", attribute);
				}
	
	            if (DEBUG.Normal()) 
	            	System.out.printf("%-12s: '%s' %s\n", "INV_TYPE", new String(this.DeviceType), datetime.toString());
	            break;
	        default:
				if (DEBUG.Normal())  
	            	System.out.printf("Wrong enum given to this method (SetInverterDataAttribute), %s\n", lri.toString());
				break;
    	}
    }
    
    public void SetInverterDataCls(LriDef lri, int value, long cls, Date datetime)
    {
    	String strWatt = "%-12s: %d (W) %s\n";
	    String strVolt = "%-12s: %.2f (V) %s\n";
	    String strAmp = "%-12s: %.3f (A) %s\n";
	    
    	switch(lri)
    	{
		    case DcMsWatt: //SPOT_PDC1 / SPOT_PDC2
		        if (cls == 1)   // MPP1
		        {
		            this.Pdc1 = value;
		            if (DEBUG.Normal())  
		            	System.out.printf(strWatt, "SPOT_PDC1", value, datetime.toString());
		        }
		        if (cls == 2)   // MPP2
		        {
		            this.Pdc2 = value;
		            if (DEBUG.Normal())  
		            	System.out.printf(strWatt, "SPOT_PDC2", value, datetime.toString());
		        }
		
		        break;
		
		    case DcMsVol: //SPOT_UDC1 / SPOT_UDC2
		        if (cls == 1)
		        {
		            this.Udc1 = value;
		            if (DEBUG.Normal())  
		            	System.out.printf(strVolt, "SPOT_UDC1", misc.toVolt(value), datetime.toString());
		        }
		        if (cls == 2)
		        {
		            this.Udc2 = value;
		            if (DEBUG.Normal())  
		            	System.out.printf(strVolt, "SPOT_UDC2", misc.toVolt(value), datetime.toString());
		        }
		
		        break;
		
		    case DcMsAmp: //SPOT_IDC1 / SPOT_IDC2
		        if (cls == 1)
		        {
		            this.Idc1 = value;
		            if (DEBUG.Normal())  
		            	System.out.printf(strAmp, "SPOT_IDC1", misc.toAmp(value), datetime.toString());
		        }
		        if (cls == 2)
		        {
		            this.Idc2 = value;
		            if (DEBUG.Normal())  
		            	System.out.printf(strAmp, "SPOT_IDC2", misc.toAmp(value), datetime.toString());
		        }
		        break;
		default:
			if (DEBUG.Normal())  
            	System.out.printf("Wrong enum given to this method (SetInverterDataCls), %s\n", lri.toString());
			break;
    	}
    }
    
    public void SetInverterData64(LriDef lri, long value64, Date datetime)
    {
	    String strkWh = "%-12s: %.3f (kWh) %s\n";
	    String strHour = "%-12s: %.3f (h) %s\n";
	    
    	switch(lri)
    	{
		    case MeteringTotWhOut: //SPOT_ETOTAL
		        this.ETotal = value64;
		
		        if (DEBUG.Normal())  
		        	System.out.printf(strkWh, "SPOT_ETOTAL", misc.tokWh(value64), datetime.toString());
		        break;
		
		    case MeteringDyWhOut: //SPOT_ETODAY
		        //This function gives us the current inverter time
		        this.InverterDatetime = datetime.getTime();
		        this.EToday = value64;
		
		        if (DEBUG.Normal())  
		        	System.out.printf(strkWh, "SPOT_ETODAY", misc.tokWh(value64), datetime.toString());
		        break;
		
		    case MeteringTotOpTms: //SPOT_OPERTM
		        this.OperationTime = value64;
		
		        if (DEBUG.Normal())  
		        	System.out.printf(strHour, "SPOT_OPERTM", misc.toHour(value64), datetime.toString());
		        break;
		
		    case MeteringTotFeedTms: //SPOT_FEEDTM
		        this.FeedInTime = value64;
		
		        if (DEBUG.Normal())  
		        	System.out.printf(strHour, "SPOT_FEEDTM", misc.toHour(value64), datetime.toString());
		        break;
		default:
			if (DEBUG.Normal())  
            	System.out.printf("Wrong enum given to this method (SetInverterData64), %s\n", lri.toString());
			break;
    	}
    }
}
