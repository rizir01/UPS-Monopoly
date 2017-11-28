package com.mygdx.seme;

public class Message
{
	private String [] mess = new String [2];
	
	public Message (String type, String text)
	{
		mess[0] = type;
		mess[1] = text;
	}
	
	public String getMessageType()
	{
		return mess[0];
	}
	
	public String getMessageData()
	{
		return mess[1];
	}
	
	public String [] getAll()
	{
		return new String[]{mess[0], mess[1]};
	}
	
	public void setMessage(String type, String text)
	{
		mess[0] = type;
		mess[1] = text;
	}
	
	public void setAll(String [] all)
	{
		mess[0] = all[0];
		mess[1] = all[1];
	}
	
	@Override
	public String toString()
	{
		return "[" + mess[0] + "," + mess[1] + "]";  
	}
}
