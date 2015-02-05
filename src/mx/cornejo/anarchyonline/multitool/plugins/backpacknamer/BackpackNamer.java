/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins.backpacknamer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import mx.cornejo.anarchyonline.multitool.MultiTool;
import mx.cornejo.anarchyonline.multitool.plugins.AbstractPlugin;
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
public class BackpackNamer extends AbstractPlugin
{
    public BackpackNamer()
    {
        super();
    }
    
    @Override
    public JPanel getPanel()
    {
        JLabel namePrefixLbl = new JLabel(getString("label.prefix.name"));
        JTextField namePrefixTxt = new JTextField(getString("text.prefix.default"));

        JTextArea logTxtArea = new JTextArea();
        logTxtArea.setEditable(false);
        
        JScrollPane logScrollPane = new JScrollPane(logTxtArea);
        logScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton nameButton = new JButton(getString("button.name.name"));
        nameButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (e.getSource() == nameButton)
                {
                    String prefsPath = getPreference(MultiTool.PREFS_AO_PREFS_DIR, null);
                    if (prefsPath != null)
                    {
                        logTxtArea.setText("");
                        File prefsDirFile = new File(prefsPath);
                        
                        Properties props = new Properties();
                        props.setProperty("namePrefix", namePrefixTxt.getText());
                        props.setProperty("renameIfGeneric", "true");
                        
                        NamerWorker task = new NamerWorker(props, prefsDirFile, logTxtArea);
                        task.addPropertyChangeListener(new PropertyChangeListener()
                        {
                            public void propertyChange(PropertyChangeEvent evt)
                            {
                                if ("progess".equals(evt.getPropertyName()))
                                {
                                    //progressBar.setValue((Integer)evt.getNewValue());
                                }
                            }
                        });
                        task.execute();
                    }
                }
            }
        });
        
        JPanel panel = new JPanel();
        panel.setName(getString("panel.name"));
        panel.setLayout(new GridBagLayout());
        
        panel.add(namePrefixLbl,   new GridBagConstraints(0,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        panel.add(namePrefixTxt,   new GridBagConstraints(1,0, 1,1, 1.0,0.0, GridBagConstraints.EAST,   GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2), 0,0));
        
        panel.add(logScrollPane,   new GridBagConstraints(0,1, 2,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,       new Insets(2,2,2,2), 0,0));
        panel.add(nameButton,      new GridBagConstraints(0,2, 2,1, 1.0,0.0, GridBagConstraints.EAST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        
        return panel;
    }
    
    @Override
    public void cleanUp()
    {
        
    }
    
    private class NamerWorker extends SwingWorker<Object, String>
    {
        private Properties props = null;
        private File prefsFolder = null;
        private JTextArea textArea = null;
        
        public NamerWorker(Properties props, File prefsFolder, JTextArea textArea)
        {
            this.props = props;
            this.prefsFolder = prefsFolder;
            this.textArea = textArea;
        }
        
        @Override
        public Void doInBackground()
        {
            processPrefsFolder(prefsFolder);
            return null;
        }
        
        @Override
        protected void process(List<String> chunks)
        {
            for (String status : chunks)
            {
                textArea.append(status + "\n");
            }
        }
        
        private void processPrefsFolder(File prefs)
        {
            File[] possibleAccounts = prefs.listFiles();
            if (possibleAccounts != null)
            {
                // Check each subfolder, if it has a "CharXXXX" folder, then this is an account folder
                for (File possAcc : possibleAccounts)
                {
                    if (!possAcc.isDirectory())
                    {
                        continue;
                    }

                    File[] subFiles = possAcc.listFiles();
                    if (subFiles == null)
                    {
                        continue;
                    }

                    boolean isAccountFolder = false;
                    for (File sub : subFiles)
                    {
                        if (sub.isDirectory() && sub.getName().matches("Char\\d+"))
                        {
                            isAccountFolder = true;
                            break;
                        }
                    }

                    if (!isAccountFolder)
                    {
                        continue;
                    }

                    processAccountFolder(possAcc);
                }
            }
        }
        
        private int processAccountFolder(File acc)
        {
            publish("Account " + acc.getName());
            
            int num = 0;
            
            File[] charFolders = acc.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.matches("Char\\d+");
                }
            });
            
            for (File charFolder : charFolders)
            {
                processCharFolder(charFolder);
                num++;
            }
            
            return num;
        }
        
        private int processCharFolder(File charFolder)
        {
            int num = 0;
            
            publish("- Character " + charFolder.getName());
            
            String containersPath = charFolder.getAbsolutePath() + File.separator + "Containers";
            File containersFolder = new File(containersPath);
            if (containersFolder.isDirectory())
            {
                File[] backpacks = containersFolder.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.matches("Container_51017x\\d+?\\.xml");
                    }
                });
                
                Format fmt = Format.getPrettyFormat();
                fmt.setIndent("    ");
                fmt.setOmitDeclaration(true);
                
                for (File backpack : backpacks)
                {
                    String bpName = backpack.getName();
                    String suffix = bpName.substring(16, bpName.length()-4);
                    props.setProperty("nameSuffix", suffix);
                    
                    processBackpack(backpack, props, fmt);
                }
            }
            
            return num;
        }
        
        private boolean processBackpack(File backpack, Properties props, Format xmlFormat)
        {
            String prefix = props.getProperty("namePrefix");
            String suffix = props.getProperty("nameSuffix");
            String name = prefix+suffix;
            
            boolean rename = Boolean.parseBoolean(props.getProperty("renameIfGeneric"));
            
            try
            {
                SAXBuilder builder = new SAXBuilder();

                Document doc = (Document) builder.build(backpack);
                Element rootNode = doc.getRootElement();

                Element nameElem = null;
                String oldName = null;
                
                // check if it already has a name
                List<Element> stringElems = rootNode.getChildren("String");
                for (Element stringElem : stringElems)
                {
                    String stringName = stringElem.getAttributeValue("name");
                    if (stringName.equals("container_name"))
                    {
                        nameElem = stringElem;
                        oldName = nameElem.getAttributeValue("value");
                        oldName = oldName.substring(1, oldName.length()-1); // remove the ""
                        break;
                    }
                }
                
                if (oldName != null && (!rename || name.equals(oldName) || !oldName.matches(prefix+"\\d+")))
                {
                    publish("Skipping backpack \"" + oldName + "\"");
                    return false;
                }
                
                if (nameElem == null)
                {
                    // <String name="container_name" value='&quot;Treatment Suit&quot;' />
                    nameElem = new Element("String");
                    nameElem.setAttribute("name", "container_name");
                    rootNode.addContent(nameElem);
                }
                nameElem.setAttribute("value", "\""+name+"\"");
                        
                try (FileWriter fw = new FileWriter(backpack))
                {
                    XMLOutputter xmlOut = new XMLOutputter();
                    xmlOut.setFormat(xmlFormat);
                    xmlOut.output(doc, fw);
                }

                publish("Named backpack \"" + name + "\"");
                return true;
            }
            catch (IOException|JDOMException ex)
            {
                LOG.throwing(NamerWorker.class.getName(), "processBackpack", ex);
                ex.printStackTrace();
                publish("There was a problem with " + backpack.getAbsolutePath());
            }
            
            return false;
        }
    }

}
