package test;

import inverter.InvDeviceClass;
import inverter.Inverter;
import inverterdata.InverterDataType;

import smajava.Config;
import smajava.SmaLogger;
import smajava.TagDefs;
import smajava.misc;

/**
 * This example basically does the exact same as the original SBFspot program minus the logging stuff
 * Mainly used to test all the functionality of the API.
 *
 */
public class SBFspotTest 
{
	public static void main(String[] args) 
	{
		Config config = new Config();
		SmaLogger smaLogger = new SmaLogger(config);
		Inverter inverter;
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
		
		//Manual creation
		inverter = smaLogger.CreateInverter("192.168.1.110");

		System.out.println("logging on inverter...");
		
		System.out.printf("Inverter %s logged on... ", inverter.GetIP());
		if(inverter.Logon(config.userGroup, config.SMA_Password) > -1)
			System.out.printf("Succesful \n");
		else
			System.out.printf("Unsuccesful\n");
		
		System.out.println("Getting some data...");
		if ((rc = inverter.GetInverterData(InverterDataType.SoftwareVersion)) != 0)
	        System.out.printf("getSoftwareVersion returned an error: %d\n", rc);
	    
	    if ((rc = inverter.GetInverterData(InverterDataType.TypeLabel)) != 0)
	        System.out.printf("getTypeLabel returned an error: %d\n", rc);    
	    else
	    {
            System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
            System.out.printf("Device Name:      %s\n", new String(inverter.Data.DeviceName));
            System.out.printf("Device Class:     %s\n", new String(inverter.Data.DeviceClass));
            System.out.printf("Device Type:      %s\n", new String(inverter.Data.DeviceType));
            System.out.printf("Software Version: %s\n", inverter.Data.SWVersion);
            System.out.printf("Serial number:    %d\n", inverter.Data.Serial);
	    }
	    if ((rc = inverter.GetInverterData(InverterDataType.BatteryChargeStatus)) != 0)
			System.out.printf("getBatteryChargeStatus returned an error: %d\n", rc);
		else
		{
		    if (inverter.Data.DevClass == InvDeviceClass.BatteryInverter)
		    {
                System.out.printf("SUSyID: %d - SN: %lu\n", inverter.Data.SUSyID, inverter.Data.Serial);
                System.out.printf("Batt. Charging Status: %lu%%\n", inverter.Data.BatChaStt);
		    }
		}

		if ((rc = inverter.GetInverterData(InverterDataType.BatteryInfo)) != 0)
			System.out.printf("getBatteryInfo returned an error: %d\n", rc);
		else
		{
		    if (inverter.Data.DevClass == InvDeviceClass.BatteryInverter)
		    {
                System.out.printf("SUSyID: %d - SN: %lu\n", inverter.Data.SUSyID, inverter.Data.Serial);
                System.out.printf("Batt. Temperature: %3.1f%sC\n", (float)(inverter.Data.BatTmpVal / 10), misc.SYM_DEGREE); // degree symbol is different on windows/linux
                System.out.printf("Batt. Voltage    : %3.2fV\n", misc.toVolt(inverter.Data.BatVol));
                System.out.printf("Batt. Current    : %2.3fA\n", misc.toAmp(inverter.Data.BatAmp));
		    }
		}

	    if ((rc = inverter.GetInverterData(InverterDataType.DeviceStatus)) != 0)
	        System.out.printf("getDeviceStatus returned an error: %d\n", rc);
	    else
	    {
            System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
			System.out.printf("Device Status:      %s\n", TagDefs.GetInstance().getDesc(inverter.Data.DeviceStatus, "?"));
	    }

		if ((rc = inverter.GetInverterData(InverterDataType.InverterTemperature)) != 0)
	        System.out.printf("getInverterTemperature returned an error: %d\n", rc);
	    else
	    {
            System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
			//System.out.printf("Device Temperature: %3.1f%sC\n", (float)(inverter.Data.Temperature / 100f), misc.SYM_DEGREE); // degree symbol is different on windows/linux
			System.out.printf("Device Temperature: %3.1f%sC\n", misc.toCelc(inverter.Data.Temperature), misc.SYM_DEGREE); // degree symbol is different on windows/linux
	    }

		if (inverter.Data.DevClass == InvDeviceClass.SolarInverter)
	    {
	        if ((rc = inverter.GetInverterData(InverterDataType.GridRelayStatus)) != 0)
	            System.out.printf("getGridRelayStatus returned an error: %d\n", rc);
	        else
	        {
                if (inverter.Data.DevClass == InvDeviceClass.SolarInverter)
                {
                    System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
					System.out.printf("GridRelay Status:      %s\n", TagDefs.GetInstance().getDesc(inverter.Data.GridRelayStatus, "?"));
                }
	        }
	    }

	    if ((rc = inverter.GetInverterData(InverterDataType.MaxACPower)) != 0)
	        System.out.printf("getMaxACPower returned an error: %d\n", rc);
	    else
	    {
	        //TODO: REVIEW THIS PART (getMaxACPower & getMaxACPower2 should be 1 function)
	        if ((inverter.Data.Pmax1 == 0) && (rc = inverter.GetInverterData(InverterDataType.MaxACPower2)) != 0)
	            System.out.printf("getMaxACPower2 returned an error: %d\n", rc);
	        else
	        {
                System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
                System.out.printf("Pac max phase 1: %dW\n", inverter.Data.Pmax1);
                System.out.printf("Pac max phase 2: %dW\n", inverter.Data.Pmax2);
                System.out.printf("Pac max phase 3: %dW\n", inverter.Data.Pmax3);
	        }
	    }

	    if ((rc = inverter.GetInverterData(InverterDataType.EnergyProduction)) != 0)
	        System.out.printf("getEnergyProduction returned an error: %d\n", rc);

	    if ((rc = inverter.GetInverterData(InverterDataType.OperationTime)) != 0)
	        System.out.printf("getOperationTime returned an error: %d\n", rc);
	    else
	    {
            System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
            System.out.println("Energy Production:");
            System.out.printf("\tEToday: %.3fkWh\n", misc.tokWh(inverter.Data.EToday));
            System.out.printf("\tETotal: %.3fkWh\n", misc.tokWh(inverter.Data.ETotal));
            System.out.printf("\tOperation Time: %.2fh\n", misc.toHour(inverter.Data.OperationTime));
            System.out.printf("\tFeed-In Time  : %.2fh\n", misc.toHour(inverter.Data.FeedInTime));
	    }

	    if ((rc = inverter.GetInverterData(InverterDataType.SpotDCPower)) != 0)
	        System.out.printf("getSpotDCPower returned an error: %d\n", rc);

	    if ((rc = inverter.GetInverterData(InverterDataType.SpotDCVoltage)) != 0)
	        System.out.printf("getSpotDCVoltage returned an error: %d\n", rc);

	    //Calculate missing DC Spot Values
	    if (config.calcMissingSpot == 1)
	        inverter.CalcMissingSpot();

		inverter.Data.calPdcTot = inverter.Data.Pdc1 + inverter.Data.Pdc2;
        System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
        System.out.println("DC Spot Data:");
        System.out.printf("\tString 1 Pdc: %7.3fkW - Udc: %6.2fV - Idc: %6.3fA\n", misc.tokW(inverter.Data.Pdc1), misc.toVolt(inverter.Data.Udc1), misc.toAmp(inverter.Data.Idc1));
        System.out.printf("\tString 2 Pdc: %7.3fkW - Udc: %6.2fV - Idc: %6.3fA\n", misc.tokW(inverter.Data.Pdc2), misc.toVolt(inverter.Data.Udc2), misc.toAmp(inverter.Data.Idc2));

	    if ((rc = inverter.GetInverterData(InverterDataType.SpotACPower)) != 0)
	        System.out.printf("getSpotACPower returned an error: %d\n", rc);

	    if ((rc = inverter.GetInverterData(InverterDataType.SpotACVoltage)) != 0)
	        System.out.printf("getSpotACVoltage returned an error: %d\n", rc);

	    if ((rc = inverter.GetInverterData(InverterDataType.SpotACTotalPower)) != 0)
	        System.out.printf("getSpotACTotalPower returned an error: %d\n", rc);

	    //Calculate missing AC Spot Values
	    if (config.calcMissingSpot == 1)
	        inverter.CalcMissingSpot();

        System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
        System.out.println("AC Spot Data:");
        System.out.printf("\tPhase 1 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(inverter.Data.Pac1), misc.toVolt(inverter.Data.Uac1), misc.toAmp(inverter.Data.Iac1));
        System.out.printf("\tPhase 2 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(inverter.Data.Pac2), misc.toVolt(inverter.Data.Uac2), misc.toAmp(inverter.Data.Iac2));
        System.out.printf("\tPhase 3 Pac : %7.3fkW - Uac: %6.2fV - Iac: %6.3fA\n", misc.tokW(inverter.Data.Pac3), misc.toVolt(inverter.Data.Uac3), misc.toAmp(inverter.Data.Iac3));
        System.out.printf("\tTotal Pac   : %7.3fkW\n", misc.tokW(inverter.Data.TotalPac));
	    
	    if ((rc = inverter.GetInverterData(InverterDataType.SpotGridFrequency)) != 0)
	        System.out.printf("getSpotGridFrequency returned an error: %d\n", rc);
	    else
	    {
            System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
            System.out.printf("Grid Freq. : %.2fHz\n", misc.toHz(inverter.Data.GridFreq));
	    }

	    if (inverter.Data.DevClass == InvDeviceClass.SolarInverter)
		{
			System.out.printf("SUSyID: %d - SN: %d\n", inverter.Data.SUSyID, inverter.Data.Serial);
			if (inverter.Data.InverterDatetime > 0)
				System.out.printf("Current Inverter Time: %s\n", misc.printDate(inverter.Data.InverterDatetime));

			if (inverter.Data.WakeupTime > 0)
				System.out.printf("Inverter Wake-Up Time: %s\n", misc.printDate(inverter.Data.WakeupTime));

			if (inverter.Data.SleepTime > 0)
				System.out.printf("Inverter Sleep Time  : %s\n", misc.printDate(inverter.Data.SleepTime));
		}

		if (inverter.Data.DevClass == InvDeviceClass.BatteryInverter)
		{
			//Note removed the logging part, also removed a second if statement which was the same as the first, why ?
			inverter.Logoff();
			//TODO freemem?
			//freemem(Inverters);
			smaLogger.ShutDown();
			System.out.printf("Terminating here... Dealing with Battery Inverter.\n");
			System.exit(0);
		}
		
		System.out.println("logging off inverter...");

		inverter.Logoff();
		
		System.out.println("Shutting down SMA Logger.");
		smaLogger.ShutDown();
	}
}
