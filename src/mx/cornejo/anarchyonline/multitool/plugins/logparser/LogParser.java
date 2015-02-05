/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins.logparser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import mx.cornejo.anarchyonline.multitool.plugins.AbstractPlugin;
import mx.cornejo.anarchyonline.utils.AOMessage;
import mx.cornejo.anarchyonline.utils.AOUtils;

/**
 *
 * @author javier
 */
public class LogParser extends AbstractPlugin
{
    private static final String PREFS_LAST_USED_WINDOW = "last_used_window";
    private ParserWorker worker = null;
    private JPanel panel = buildParserPanel();

    public LogParser()
    {
        super();
    }
    
    @Override
    public JPanel getPanel()
    {
        return panel;
    }
    
    @Override
    public void cleanUp()
    {
        if (worker != null)
        {
            worker.stop();
        } 
    }
    
    private JPanel buildParserPanel()
    {
        JTextArea txtArea = new JTextArea();
        txtArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtArea);
        
        // Character and window selection combo box
        HashMap<String, File> chars = AOUtils.getCharacterDirs();
        HashMap<String, File> windows = new HashMap();
        
        ArrayList<String> optionList = new ArrayList();
        chars.entrySet().stream().forEach((charEntry) ->
        {
            String charName = charEntry.getKey();
            File charDir = charEntry.getValue();
            
            HashMap<String, File> charWindows = AOUtils.getWindowLogFiles(charDir);
            charWindows.entrySet().stream().forEach((windowEntry) ->
            {
                String windowName = windowEntry.getKey();
                File windowLogFile = windowEntry.getValue();
                
                windows.put(charName + " - " + windowName, windowLogFile);
                optionList.add(charName + " - " + windowName);
            });
        });

        String[] optionArray = optionList.toArray(new String[optionList.size()]);
        JComboBox windowSelectBox = new JComboBox(optionArray);

        String lastWindowUsed = getPreference(PREFS_LAST_USED_WINDOW, null);
        if (lastWindowUsed != null)
        {
            windowSelectBox.setSelectedItem(lastWindowUsed);
        }

        windowSelectBox.addActionListener((e) -> 
        {
            JComboBox cb = (JComboBox)e.getSource();
            String selectedWindow = (String)cb.getSelectedItem();
            setPreference(PREFS_LAST_USED_WINDOW, selectedWindow);
        });
        ///////////////////////////////////
        
        JButton controlBttn = new JButton(getString("button.start"));
        controlBttn.setActionCommand("start");
        controlBttn.addActionListener((e) -> 
        {
            if (null != e.getActionCommand())
            switch (e.getActionCommand())
            {
                case "start":
                    String selectedWindow = (String)windowSelectBox.getSelectedItem();
                    File logFile = windows.get(selectedWindow);
                    if (logFile != null && logFile.exists())
                    {
                        windowSelectBox.setEnabled(false);

                        controlBttn.setActionCommand("stop");
                        controlBttn.setText(getString("button.stop"));
                        
                        Properties props = new Properties();
                        props.setProperty(ParserWorker.PROP_START_AT_BEGINNING, "false");
                        props.setProperty(ParserWorker.PROP_SAMPLE_INTERVAL, "1000");
                        
                        worker = new ParserWorker(props, logFile, txtArea);
                        worker.execute();

                    }
                break;

                case "stop":
                    worker.stop();

                    controlBttn.setActionCommand("start");
                    controlBttn.setText(getString("button.start"));

                    windowSelectBox.setEnabled(true);
                break;
            }
        });
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridBagLayout());
        textPanel.add(scrollPane, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER,   GridBagConstraints.BOTH, new Insets(2,2,2,2), 0,0));
        
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.add("text", textPanel);
        
        JPanel panel = new JPanel();
        panel.setName(getString("panel.name"));
        panel.setLayout(new GridBagLayout());
        
        panel.add(windowSelectBox, new GridBagConstraints(0,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE, new Insets(2,2,2,2), 0,0));
        panel.add(controlBttn,     new GridBagConstraints(1,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE, new Insets(2,2,2,2), 0,0));
        panel.add(tabPane,         new GridBagConstraints(0,1, 2,1, 1.0,1.0, GridBagConstraints.WEST,   GridBagConstraints.BOTH, new Insets(2,2,2,2), 0,0));

        return panel;
    }

    private class ParserWorker extends SwingWorker<Object, AOMessage>
    {
        public static final String PROP_START_AT_BEGINNING = "parser.startAtBeginning";
        public static final String PROP_SAMPLE_INTERVAL = "parser.sampleInterval";
        
        private Properties props = null;
        private File logFile = null;
        private JTextArea txtArea = null;
        private boolean working = false;

        public ParserWorker(Properties props, File logFile, JTextArea txtArea)
        {
            this.props = props;
            this.logFile = logFile;
            this.txtArea = txtArea;
        }
        
        @Override
        public Void doInBackground()
        {
            boolean startAtBeginning = Boolean.parseBoolean(props.getProperty(PROP_START_AT_BEGINNING));
            long sampleInterval = Long.parseLong(props.getProperty(PROP_SAMPLE_INTERVAL));
            
            long filePointer = logFile.length();
            if (startAtBeginning)
            {
                filePointer = 0;
            }
            
            try
            {
                working = true;
                RandomAccessFile raf = new RandomAccessFile(logFile, "r");
                
                while(working)
                {
                    try
                    {
                        long fileLength = logFile.length();
                        if (fileLength < filePointer)
                        {
                            // Log file must have been rotated or deleted; 
                            // reopen the file and reset the file pointer
                            raf = new RandomAccessFile(logFile, "r");
                            filePointer = 0;
                        }
                        
                        if (fileLength > filePointer)
                        {
                            raf.seek(filePointer);
                            String line = raf.readLine();
                            while (line != null)
                            {
                                publish(AOMessage.parse(line));
                                line = raf.readLine();
                            }
                            filePointer = raf.getFilePointer();
                        }
                        
                        Thread.sleep(sampleInterval);
                    }
                    catch (InterruptedException ex)
                    {
                        handleException(ex, ParserWorker.class.getCanonicalName(), "runInBackground");
                    }
                }
                
                raf.close();
            }
            catch (IOException ex)
            {
                handleException(ex, ParserWorker.class.getCanonicalName(), "runInBackground");
            }
            return null;
        }
        
        @Override
        public void process(List<AOMessage> messages)
        {
            messages.stream().forEach((msg) -> {
                txtArea.append(msg.toString() + "\n");
                txtArea.setCaretPosition(txtArea.getDocument().getLength());
            });
        }
        
        public void stop()
        {
            working = false;
        }
        
        public boolean isWorking()
        {
            return this.working;
        }
    }
}
