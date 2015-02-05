/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author javier
 */
public class ChatWindow
{
    private static final SAXBuilder builder = new SAXBuilder();
    
    private File dir = null;
    private Document configDoc = null;
    
    public ChatWindow(File dir) throws JDOMException, IOException
    {
        String cfgPath = dir.getAbsolutePath() +
                         File.separator + "Config.xml";
        File configXml = new File(cfgPath);
        this.configDoc = (Document)builder.build(configXml);
        this.dir = dir;
    }
    
    private Element getNameElement()
    {
        Element rootNode = configDoc.getRootElement();
        List<Element> stringElems = rootNode.getChildren("String");
        for (Element elem : stringElems)
        {
            String elemName = elem.getAttributeValue("name");
            if (elemName.equals("name"))
            {
                return elem;
            }
        }
        return null;
    }
    
    public String getName()
    {
        Element elem = getNameElement();
        if (elem != null)
        {
            String name = elem.getAttributeValue("value");
            return name.substring(1, name.length()-1);
        }
        return null;
    }
    
    public File getLogFile()
    {
        String logPath = dir.getAbsolutePath() +
                         File.separator + "Log.txt";
        File logFile = new File(logPath);
        if (logFile.exists())
        {
            return logFile;
        }
        return null;
    }
    
    public String getCharacter()
    {
        String path = dir.getAbsolutePath();
        String[] segments = path.split(Pattern.quote(File.separator));
        return segments[segments.length-4];
    }
    
    public String getAccount()
    {
        String path = dir.getAbsolutePath();
        String[] segments = path.split(Pattern.quote(File.separator));
        return segments[segments.length-5];
    }
}
