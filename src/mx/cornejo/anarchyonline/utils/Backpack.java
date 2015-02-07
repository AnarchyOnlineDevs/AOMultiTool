/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.awt.geom.Rectangle2D;
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
    
    private static final XPathExpression<Element> dockNameExpr =
            xFactory.compile("/Archive/String[@name='DockableViewDockName']", Filters.element("String"));
    
    private static final XPathExpression<Element> dockSizeExpr = 
            xFactory.compile("/Archive/Archive/Rect[@name='WindowFrame']", Filters.element("Rect"));
    
    private File xmlFile = null;
    private Document doc = null;
    private Document dockAreaDoc = null;
    
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
        
        try
        {
            this.dockAreaDoc = (Document)builder.build(getDockAreaFile());
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();
        }
    }
    
    private static Element evalXPath(XPathExpression<Element> xpression, Document doc)
    {
        Element elem = null;
        
        if (doc != null)
        {
            List<Element> list = xpression.evaluate(doc);
            if (list.size() > 0)
            {
                elem = list.get(0);
            }
        }
        
        return elem;
    }
    
    private Element getNameElement()
    {
        return evalXPath(nameExpr, doc);
    }
    
    private Element getListModeElement()
    {
        return evalXPath(listModeExpr, doc);
    }
    
    private Element getDockAreaElement()
    {
        return evalXPath(dockNameExpr, doc);
    }
    
    private Element getDockAreaSizeElement()
    {
        return evalXPath(dockSizeExpr, dockAreaDoc);
    }
    
    public File getDockAreaFile()
    {
        File file = null;
        
        Element elem = getDockAreaElement();
        if (elem != null)
        {
            String dockName = elem.getAttributeValue("value");
            dockName = dockName.substring(1, dockName.length()-1);
            
            String dockAreaPath = xmlFile.getParentFile().getParentFile().getAbsolutePath() + 
                                    File.separator + "DockAreas" +
                                    File.separator + dockName + ".xml";
            file = new File(dockAreaPath);
        }
        return file;
    }
    
    private static void writeXMLFile(File file, Document doc) throws IOException
    {
        try (FileWriter fw = new FileWriter(file)) 
        {
            writer.output(doc, fw);
        }
        catch (IOException ex)
        {
            throw ex;
        }
    }
    
    public void reload() throws JDOMException, IOException
    {
        this.doc = (Document)builder.build(xmlFile);
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
        writeXMLFile(xmlFile, doc);
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
        writeXMLFile(xmlFile, doc);
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
    
    public String getWindowSize()
    {
        Element dockAreaElem = this.getDockAreaSizeElement();
        if (dockAreaElem != null)
        {
            return dockAreaElem.getAttributeValue("value");
        }
        return null;
    }
    
    public void setWindowSize(String size) throws IOException
    {
        File dockAreaFile = this.getDockAreaFile();
        if (dockAreaFile != null && dockAreaDoc != null)
        {
            Element elem = this.getDockAreaSizeElement();
            elem.setAttribute("value", size);
            writeXMLFile(dockAreaFile, dockAreaDoc);
        }
    }
}
