package eth;

import java.nio.ByteBuffer;

public class ethEndpoint 
{
	public short SUSyID;
	public int       Serial;
	public short Ctrl;
	
	public ethEndpoint(ByteBuffer bb)
	{
		SUSyID = bb.getShort();
		Serial = bb.getInt();
		Ctrl = bb.getShort();
	}
}
