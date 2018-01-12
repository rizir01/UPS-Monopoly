package com.mygdx.seme;

import java.io.IOException;
import java.net.SocketException;
import java.util.Vector;

public class TestClientRecieve extends Thread
{	
	static final int MAXQUEUE = 7;
	private Vector messages = new Vector();
	boolean done;
	
	public TestClientRecieve()
	{
		done = true;
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
				else if(message.indexOf('#') == -1)
				{
					System.out.println("SR: |" + message + "|");
					System.out.println("Prisel spatny format zpravy(chybi #)!");
				}
				else
				{
					try
					{
						String zp = "S:" + message.substring(1, message.length() - 1);
						putMessage(zp);
					}
					catch(InterruptedException e)
					{
						System.out.println("Chyba v ulozeni zpravy v producentovi!");
						e.printStackTrace();
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
    
    public synchronized void putMessage(String mess) throws InterruptedException
    {
        while(messages.size() == MAXQUEUE)
        {
            wait();
        }
        //messages.addElement(new java.util.Date().toString());
        messages.addElement(mess);
        System.out.println("put message");
        notify();
        //Later, when the necessary event happens, the thread that is running it calls notify() from a block synchronized on the same object.
    }
 
    // Called by Consumer
    public synchronized String getMessage() throws InterruptedException
    {
        notify();
        while(messages.size() == 0)
        {
            wait();//By executing wait() from a synchronized block, a thread gives up its hold on the lock and goes to sleep.
        }
        String message = (String) messages.firstElement();
        messages.removeElement(message);
        return message;
    }
	
}
