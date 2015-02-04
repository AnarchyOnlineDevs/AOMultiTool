/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author javier
 */
public class AOMessage
{
    private static final Pattern pattern = Pattern.compile("\\[\"([^\"]*)\\\",\"([^\"]*)\\\",\"([^\"]*)\\\",([0-9]*)\\](.*)");

    private long timestamp = 0l;

    private String something = null;
    private String channel = null;
    private String sender = null;
    private String text = null;
    
    public AOMessage(long timestamp, String something, String channel, String sender, String text)
    {
        this.timestamp = timestamp;
        this.something = something;
        this.channel = channel;
        this.sender = sender;
        this.text = text;
    }

    public long getTimeStamp()
    {
        return timestamp;
    }

    public String getSomething()
    {
        return something;
    }

    public String getChannel()
    {
        return channel;
    }

    public String getSender()
    {
        return sender;
    }

    public String getText()
    {
        return text;
    }
    
    public String toString()
    {
        return "[\""+getSomething()+"\","+
                "\""+getChannel()  +"\","+
                "\""+getSender()   +"\","+
                     getTimeStamp()+"]"+
                     getText();
    }
    
    public static AOMessage parse(String msg)
    {
        try
        {
            Matcher m = pattern.matcher(msg);
            if (m.matches())
            {
                String something = m.group(1);
                String channel = m.group(2);
                String sender = m.group(3);
                long time = Long.parseLong(m.group(4));
                String text = m.group(5);

                return new AOMessage(time, something, channel, sender, text);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return null;
    }
}
