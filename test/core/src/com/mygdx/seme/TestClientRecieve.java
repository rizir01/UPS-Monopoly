package com.mygdx.seme;

import java.io.IOException;
import java.net.SocketException;
import java.util.Vector;

public class TestClientRecieve extends Thread
{	
	static final int MAXQUEUE = 7;
	private Vector<String> messages = new Vector<String>();
	boolean done;
	boolean fell;//Kdyz server spadnul
	
	public TestClientRecieve()
	{
		done = true;
		fell = true;
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
		
		if(fell)
		{
			//Jenom pokud to spadne
			Monopoly.EndScreen.hide = false;
			Monopoly.LobbyScreen.hide = false;
			Monopoly.GameScreen.hide = false;
			
			LobbyScreen.drawAllInfo = false;
			LobbyScreen.countDown = false;
			LobbyScreen.timeC = 0;
			//LobbyScreen.selectedLobby = -1;
			LobbyScreen.ready = false;
			
			Monopoly.LobbyScreen.game.setScreen(Monopoly.LoginScreen);			
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
