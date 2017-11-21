package com.mygdx.seme;

import java.io.IOException;
import java.net.SocketException;

public class TestClientRecieve extends Thread
{	
	String [] inputBuffer = new String [5];
	
	int indInput = 0;
	
	boolean done;
	
	public TestClientRecieve()
	{
		done = true;
	}
	
	public void putData(String text)
	{
		inputBuffer[indInput] = text;
		indInput++;
	}
	
	@Override
	public void run()
	{
		try
		{
			while(done)
			{
				if(TestClient.socket.isClosed())
				{
					System.out.println("Soket byl uzavren!");
					break;
				}
				String message = (String)TestClient.bf.readLine();
				if(message == null)
				{
					System.out.println("Spadl socket nebo server!");
					break;
				}
				else if(message.charAt(0) != '$')
				{
					System.out.println("Prisel spatny format zpravy(chybi $)!");
				}
				System.out.println("READER: " + message.substring(1));
			}
		}
		catch(SocketException r)
		{
			//System.out.println("Socket byl uzavren!\n" + r.toString());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("READING THREAD CLOSED");
	}
	
}
