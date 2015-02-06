/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins.logparser;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Properties;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import mx.cornejo.anarchyonline.multitool.MultiTool;

import mx.cornejo.anarchyonline.multitool.plugins.AbstractPlugin;
import mx.cornejo.anarchyonline.utils.AOMessage;
import mx.cornejo.anarchyonline.utils.AOUtils;
import mx.cornejo.anarchyonline.utils.ChatWindow;

/**
 *
 * @author javier
 */
public class LogParser extends AbstractPlugin
{
    private static final String PREFS_LAST_USED_WINDOW = "last_used_window";
    private ParserWorker worker = null;
    private JPanel panel = null;

    public LogParser()
    {
        super();
    }
    
    @Override
    public JPanel getPanel()
    {
        if (panel == null)
        {
            panel = buildParserPanel();
        }
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

    private class MyCellRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList<?> list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            ChatWindow window = (ChatWindow)value;
            lbl.setText(window.getName() + " [" + window.getCharacter()/*+"@"+window.getAccount()*/+"]");
            return lbl;
        }
    }

    private JPanel buildParserPanel()
    {
        JTextArea txtArea = new JTextArea();
        txtArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtArea);
        
        // Character and window selection combo box
        JComboBox windowSelectBox = new JComboBox();
        windowSelectBox.setEditable(false);
        windowSelectBox.setRenderer(new MyCellRenderer());

        String prefsPath = getPreference(MultiTool.PREFS_AO_PREFS_DIR, null);
        if (prefsPath != null)
        {
            String lastWindowUsed = getPreference(PREFS_LAST_USED_WINDOW, null);

            File prefsDir = new File(prefsPath);
            AOUtils.applyToWindows(prefsDir, (window) ->
            {
                windowSelectBox.addItem(window);
                
                if (lastWindowUsed != null && lastWindowUsed.equals(window.getName()+window.getCharacter()))
                {
                    windowSelectBox.setSelectedItem(window);
                }
            });
        }

        windowSelectBox.addActionListener((e) -> 
        {
            JComboBox cb = (JComboBox)e.getSource();
            ChatWindow selectedWindow = (ChatWindow)cb.getSelectedItem();
            setPreference(PREFS_LAST_USED_WINDOW, selectedWindow.getName()+selectedWindow.getCharacter());
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
                    ChatWindow window = (ChatWindow)windowSelectBox.getSelectedItem();
                    File logFile = window.getLogFile();
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
        textPanel.add(scrollPane, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER,   GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.add("text", textPanel);
        
        JPanel p = new JPanel();
        p.setName(getString("panel.name"));
        p.setLayout(new GridBagLayout());
        
        p.add(windowSelectBox, new GridBagConstraints(0,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE, new Insets(2,2,2,2), 0,0));
        p.add(controlBttn,     new GridBagConstraints(1,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE, new Insets(2,2,2,2), 0,0));
        p.add(tabPane,         new GridBagConstraints(0,1, 2,1, 1.0,1.0, GridBagConstraints.WEST,   GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));

        return p;
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
