package smajava;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import smajava.misc.DEBUG;
import smajava.misc.VERBOSE;

public class Ethernet extends SBFNet
{
	private int MAX_CommBuf = 0;
	
	private DatagramSocket sock;
	private short port;
	
	/***
	 * Connects the socket to the port
	 * @param port The port to connect to.
	 * @return 0 if success, -1 if connection failed.
	 */
	public int Connect(short port)
	{
		if (VERBOSE.Normal()) 
			System.out.println("Initialising Socket...\n");
		try
		{
			sock = new DatagramSocket();
		}
		catch(Exception e)
		{
			System.err.println("Socket error : " + e.getMessage());
		}
		
	    // set up parameters for UDP
		this.port = port;
		try 
		{
			sock.setBroadcast(true);
		} 
		catch (SocketException e) 
		{
			System.err.println("Setting broadcast failed\n" + e.getMessage());
			return -1;
		}
	    // end of setting broadcast options

	    return 0; //OK
	}
	
	/**
	 * Disconnects and closes the socket connection.
	 */
	public void Close()
	{
		sock.disconnect();
		sock.close();
	}
	
	/***
	 * Clears the buffer and sets packetposition to 0.
	 */
	public void ClearBuffer()
	{
		this.packetposition = 0;
		pcktBuf = new byte[maxpcktBufsize];
	}
	
	/***
	 * Reads incoming data from the socket
	 * @param buf The buffer the hold the incoming data.
	 * @return Number of bytes read.
	 */
	public int Read(byte[] buf)
	{
		boolean keepReading = true;
	    int bytes_read = 0;
	    short timeout = 5; //5 seconds
	    
	    while(keepReading)
	    {
	    	DatagramPacket recv = new DatagramPacket(buf, buf.length);
	    	try 
	    	{
				sock.setSoTimeout(timeout * 1000);
			} 
	    	catch (SocketException e) 
	    	{
	    		if (DEBUG.Highest()) System.out.println("Error setting timeout socket \n" + e.getMessage());
				return -1;
			}
	    	try 
	    	{
				sock.receive(recv);
				bytes_read = recv.getLength();
			} 
	    	catch (SocketTimeoutException e1)
	    	{
	    		if (DEBUG.Highest()) System.out.println("Timeout reading socket");
				return -1;
	    	}
	    	catch (IOException e) 
	    	{
	    		if (DEBUG.Highest()) System.out.println("Error reading socket \n" + e.getMessage());
				return -1;
			}	    	
			
			if ( bytes_read > 0)
			{
				if (bytes_read > MAX_CommBuf)
				{
					MAX_CommBuf = bytes_read;
					if (DEBUG.Normal())
						System.out.printf("MAX_CommBuf is now %d bytes\n", MAX_CommBuf);
				}
			   	if (DEBUG.Normal())
			   	{
					System.out.printf("Received %d bytes from IP [%s]\n", bytes_read, recv.getAddress().getHostAddress());
			   		if (bytes_read == 600 || bytes_read == 0)
			   			System.out.printf(" ==> packet ignored\n");
				}
			}
			else
				System.out.printf("recvfrom() returned an error: %d\n", bytes_read);

			if (bytes_read == 600) timeout--;	// decrease timeout if the packet received within the timeout is an energymeter packet
			else keepReading = false;
	    }
	    return bytes_read;
	}
	
	/***
	 * Sends what's currently stored in the buffer.
	 * @param toIP The ip addres to send it to.
	 * @return The number of bytes sent.
	 */
	public int Send(String toIP)
	{
		if (DEBUG.Normal()) 
			misc.HexDump(pcktBuf, packetposition, 10);

		DatagramPacket p = new DatagramPacket(pcktBuf, packetposition, new InetSocketAddress(toIP, port));
		int bytes_sent = p.getLength();
	    try 
	    {
			sock.send(p);
			if (DEBUG.Normal()) 
		    	System.out.println(bytes_sent + " Bytes sent to IP [" + toIP + "]");
		} 
	    catch (IOException e) 
	    {
	    	if (DEBUG.Normal()) 
	    		System.out.println("Failed to send data");
	    	return 0;
		}
	    return bytes_sent;
	}
}
