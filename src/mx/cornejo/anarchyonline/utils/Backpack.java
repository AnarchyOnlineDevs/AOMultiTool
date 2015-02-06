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
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 *
 * @author javier
 */
public class Backpack
{
    private static final SAXBuilder builder = new SAXBuilder();
    private static final XMLOutputter writer = new XMLOutputter();
    private static final Format format = Format.getPrettyFormat();
    
    private static final XPathFactory xFactory = XPathFactory.instance();

    private static final XPathExpression<Element> nameExpr = 
            xFactory.compile("/Archive/String[@name='container_name']", Filters.element("String"));
    
    private static final XPathExpression<Element> listModeExpr = 
            xFactory.compile("/Archive/Archive/Bool[@name='listview_mode']", Filters.element("Bool"));
    
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
    
    private Element evalXPath(XPathExpression<Element> xpression)
    {
        Element elem = null;
        List<Element> list = xpression.evaluate(doc);
        if (list.size() > 0)
        {
            elem = list.get(0);
        }
        return elem;
    }
    
    private Element getNameElement()
    {
        return evalXPath(nameExpr);
    }
    
    private Element getListModeElement()
    {
        return evalXPath(listModeExpr);
    }
    
    private void writeXMLFile() throws IOException
    {
        try (FileWriter fw = new FileWriter(xmlFile)) 
        {
            writer.output(doc, fw);
        }
        catch (IOException ex)
        {
            throw ex;
        }
    }
    
    public boolean isListMode()
    {
        Element elem = getListModeElement();
        if (elem != null)
        {
            return Boolean.parseBoolean(elem.getAttributeValue("value"));
        }
        return false;
    }
    
    public void setListMode(boolean flag) throws IOException
    {
        Element elem = getListModeElement();
        if (elem == null)
        {
            elem = new Element("Bool");
            elem.setAttribute("name", "listview_mode");
            //todo: create parent and attach to root
        }
        elem.setAttribute("value", Boolean.toString(flag));
        writeXMLFile();
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
        writeXMLFile();
    }
    
    public String getXMLPath()
    {
        return xmlFile.getAbsolutePath();
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
