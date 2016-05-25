package inverter;

public enum InvDeviceClass
{
	/**
	 * DevClss0
	 */
    AllDevices ( 8000) ,
    /**
     * DevClss1
     */
    SolarInverter ( 8001 ),
    /**
     * DevClss2
     */
    WindTurbineInverter ( 8002 ),
    /**
     * DevClss7
     */
    BatteryInverter ( 8007 ),
    /**
     * DevClss33
     */
    Consumer ( 8033 ),
    /**
     * DevClss64
     */
    SensorSystem ( 8064 ),
    /**
     * DevClss65
     */
    ElectricityMeter ( 8065 ),
    /**
     * DevClss128
     */
    CommunicationProduct ( 8128 );
    
    private int Value;
    
    private static InvDeviceClass[] enumValues = values();
    
    public static InvDeviceClass intToEnum(int value)
    {
        for(int i = 0; i < enumValues.length; i++)
        {
        	if(enumValues[i].Value == value)
        		return enumValues[i];
        }
        return null;
    }
    
    private InvDeviceClass(int value)
    {
    	this.Value = value;
    }
}
