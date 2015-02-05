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
import mx.cornejo.anarchyonline.utils.AOUtils;

/**
 *
 * @author javier
 */
public class BackpackNamer extends AbstractPlugin
{
    private JPanel panel = null;
    
    public BackpackNamer()
    {
        super();
    }
    
    @Override
    public JPanel getPanel()
    {
        if (panel == null)
        {
            panel = buildPanel();
        }
        return panel;
    }
    
    private JPanel buildPanel()
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
        private File prefsDir = null;
        private JTextArea textArea = null;
        
        public NamerWorker(Properties props, File prefsDir, JTextArea textArea)
        {
            this.props = props;
            this.prefsDir = prefsDir;
            this.textArea = textArea;
        }
        
        @Override
        public Void doInBackground()
        {
            String prefix = (String)props.get("namePrefix");
            
            AOUtils.applyToBackpacks(prefsDir, (backpack) ->
            {
                try
                {
                    backpack.setName(prefix + backpack.getXMLNumber());
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }

            });
            
            return null;
        }
        
        @Override
        protected void process(List<String> chunks)
        {
            chunks.stream().forEach((status) ->
            {
                textArea.append(status + "\n");
            });
        }
    }

}
