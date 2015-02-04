/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import mx.cornejo.anarchyonline.utils.AOMessage;
import mx.cornejo.anarchyonline.utils.AOUtils;

/**
 *
 * @author javier
 */
public class MultiTool extends JFrame
{
    private static final String PREFS_AO_PREFS_DIR = "ao_prefs_dir";
    private static final String PREFS_LAST_USED_WINDOW = "last_used_window";

    private static final Logger LOG = Logger.getLogger(MultiTool.class.getCanonicalName());
    
    private final Preferences appPrefs = Preferences.userNodeForPackage(MultiTool.class);
    private ResourceBundle resourceBundle = null;
    private ParserWorker worker = null;
    
    public MultiTool()
    {
        super();

        resourceBundle = ResourceBundle.getBundle("mx.cornejo.anarchyonline.multitool.plugins.logparser.Messages");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(getString("app.title")); // "AO ChatLog Parser"
        buildGUI();
    }
    
    private String getString(String key)
    {
        return resourceBundle.getString(key);
    }

    private JMenuBar buildMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        
        {
            JMenu menu = new JMenu(getString("menu.file"));
            menu.setMnemonic(KeyEvent.VK_F);
            menu.getAccessibleContext().setAccessibleDescription(getString("menu.file.desc"));
            
            {
                JMenuItem menuItem = new JMenuItem(getString("menu.settings"));
                menuItem.setMnemonic(KeyEvent.VK_S);
                menuItem.getAccessibleContext().setAccessibleDescription(getString("menu.settings.desc"));
                menu.add(menuItem);
            }
            
            {
                JMenuItem menuItem = new JMenuItem(getString("menu.exit"));
                menuItem.setMnemonic(KeyEvent.VK_E);
                //menuItem.getAccessibleContext().setAccessibleDescription(getString("menu.exit.desc"));
                menuItem.addActionListener((e) ->
                {
                    System.exit(0);
                });
                menu.add(menuItem);
            }
            
            menuBar.add(menu);
        }
        
        {
            JMenu menu = new JMenu(getString("menu.help"));
            menu.setMnemonic(KeyEvent.VK_H);
            menu.getAccessibleContext().setAccessibleDescription(getString("menu.help.desc"));

            {
                JMenuItem menuItem = new JMenuItem(getString("menu.about"));
                menuItem.setMnemonic(KeyEvent.VK_A);
                menuItem.getAccessibleContext().setAccessibleDescription(getString("menu.about.desc"));
                menu.add(menuItem);
            }

            menuBar.add(menu);
        }

        return menuBar;
    }
    
    private JPanel getParserPanel()
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

        String lastWindowUsed = appPrefs.get(PREFS_LAST_USED_WINDOW, null);
        if (lastWindowUsed != null)
        {
            windowSelectBox.setSelectedItem(lastWindowUsed);
        }

        windowSelectBox.addActionListener((e) -> 
        {
            JComboBox cb = (JComboBox)e.getSource();
            String selectedWindow = (String)cb.getSelectedItem();
            appPrefs.put(PREFS_LAST_USED_WINDOW, selectedWindow);

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
        
        JPanel panel = new JPanel();
        panel.setName("Log Parser");
        panel.setLayout(new GridBagLayout());
        
        panel.add(windowSelectBox, new GridBagConstraints(0,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE, new Insets(2,2,2,2), 0,0));
        panel.add(controlBttn,     new GridBagConstraints(1,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE, new Insets(2,2,2,2), 0,0));
        panel.add(scrollPane,      new GridBagConstraints(0,1, 2,1, 1.0,1.0, GridBagConstraints.WEST,   GridBagConstraints.BOTH, new Insets(2,2,2,2), 0,0));

        return panel;
    }
    
    private List<JPanel> getPanels()
    {
        List<JPanel> panels = new ArrayList();
        panels.add(getParserPanel());
        
        return panels;
    }
    
    private void buildGUI()
    {
        setSize(800,600);
        setJMenuBar(buildMenuBar());
        
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.LEFT);
        
        List<JPanel> panels = getPanels();
        panels.stream().forEach((panel) ->
        {
            tabPane.add(panel, panel.getName());
        });
        
        Container c = getContentPane();
        c.setLayout(new GridBagLayout());
        c.add(tabPane, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2,2,2,2), 0,0));
    }
    
    private void checkSettings()
    {
        String aoPrefsPath = appPrefs.get(PREFS_AO_PREFS_DIR, null);
        
        // Try to guess the ao prefs dir
        if (aoPrefsPath == null)
        {
            File dir = AOUtils.getAOPrefsDir();
            if (dir != null && dir.exists())
            {
                aoPrefsPath = dir.getAbsolutePath();
            }
        }
        
        // Ask user to input the prefs dir
        if (aoPrefsPath == null)
        {
            int option = JOptionPane.showConfirmDialog(this, 
                        getString("dialog.no_prefs_dir.mssg"), 
                        getString("dialog.no_prefs_dir.title"), 
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.YES_OPTION)
            {
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int returnVal = fc.showOpenDialog(MultiTool.this);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    File selectedFile = fc.getSelectedFile();
                    if (selectedFile != null && selectedFile.isDirectory())
                    {
                        aoPrefsPath = selectedFile.getAbsolutePath();
                        // LOG.info("Selected: " + aoPrefsPath);
                    }
                }
            }    
        }
        
        appPrefs.put(PREFS_AO_PREFS_DIR, aoPrefsPath);
    }
    
    private void cleanUp()
    {
        if (worker != null)
        {
            worker.stop();
        }
    }
    
    private void handleException(Exception ex, String sourceClass, String sourceMethod)
    {
        LOG.throwing(sourceClass, sourceMethod, ex);
        ex.printStackTrace();
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
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        MultiTool parser = new MultiTool();
        
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                parser.cleanUp();
            }
        });
        
        parser.setVisible(true);
        parser.checkSettings();
    }
}
