package com.mygdx.seme;

import java.io.IOException;
import java.net.SocketException;

public class TestClientRecieve extends Thread
{	
	Message [] buffer; 
	
	boolean done;
	
	public TestClientRecieve(Message [] buff)
	{
		done = true;
		buffer = buff;
		//index = 0;
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
				System.out.println("cisty zaznam: " + message);
				if(message == null)
				{
					System.out.println("Odpojily jsme se od serveru!");
					break;
				}
				else if(message.charAt(0) != '$')
				{
					System.out.println("SR: |" + message + "|");
					System.out.println("Prisel spatny format zpravy(chybi $)!");
				}
				else
				{
					//System.out.println("GET FROM SERVER synch index: " + LoginScreen.indBuff);
					synchronized(buffer[LoginScreen.indBuff])
					{
						//System.out.println("GET FROM SERVER before index: " + LoginScreen.indBuff);
						//System.out.println("RC1 " + Arrays.toString(buffer));
						buffer[LoginScreen.indBuff].setMessage("SERVER", message.substring(1, message.length() - 1));
						//System.out.println("RC2 " + Arrays.toString(buffer));
						//System.out.println("GET FROM SERVER before2 index: " + LoginScreen.indBuff);
						buffer[LoginScreen.indBuff].notify();
						//System.out.println("indBuff TCR before" + LoginScreen.indBuff);
						LoginScreen.indBuff++;
						//System.out.println("indBuff TCR after" + LoginScreen.indBuff);
						//System.out.println("GET FROM SERVER after index: " + LoginScreen.indBuff);
					}
				}
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
