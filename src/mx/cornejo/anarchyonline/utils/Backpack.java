/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author javier
 */
public class Backpack
{
    private static final SAXBuilder builder = new SAXBuilder();
    private static final XMLOutputter writer = new XMLOutputter();
    private static final Format format = Format.getPrettyFormat();
    
    private File xmlFile = null;
    private Document doc = null;
    
    static
    {
        format.setOmitDeclaration(true);
        format.setIndent("    ");
        writer.setFormat(format);
    }
    
    public Backpack(File xmlFile) throws JDOMException, IOException
    {
        this.xmlFile = xmlFile;
        this.doc = (Document)builder.build(xmlFile);
    }
    
    private Element getNameElement()
    {
        Element rootNode = doc.getRootElement();
        List<Element> stringElems = rootNode.getChildren("String");
        for (Element elem : stringElems) 
        {
            String elemName = elem.getAttributeValue("name");
            if (elemName.equals("container_name"))
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
    
    public void setName(String name) throws IOException
    {
        Element elem = getNameElement();
        if (elem == null)
        {
            elem = new Element("String");
            elem.setAttribute("name", "container_name");
            doc.getRootElement().addContent(elem);
        }
        elem.setAttribute("value", "\""+name+"\"");
        
        try (FileWriter fw = new FileWriter(xmlFile)) 
        {
            writer.output(doc, fw);
        }
        catch (IOException ex)
        {
            throw ex;
        }
    }
    
    public String getXMLNumber()
    {
        //Container_51017x3379405.xml
        String xmlName = xmlFile.getName();
        return xmlName.substring(16, xmlName.length()-4);
    }
    
    public String getCharacter()
    {
        String path = xmlFile.getAbsolutePath();
        String[] segments = path.split(Pattern.quote(File.separator));
        return segments[segments.length-3];
    }
    
    public String getAccount()
    {
        String path = xmlFile.getAbsolutePath();
        String[] segments = path.split(Pattern.quote(File.separator));
        return segments[segments.length-4];
    }
}
