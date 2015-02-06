/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins.backpacknamer;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mx.cornejo.anarchyonline.multitool.MultiTool;
import mx.cornejo.anarchyonline.multitool.plugins.AbstractPlugin;
import mx.cornejo.anarchyonline.utils.AOUtils;
import mx.cornejo.anarchyonline.utils.Backpack;

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
    public void cleanUp()
    {
        
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
    
    private TreeNode buildBackpackTree()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("All");
        
        HashMap<String, DefaultMutableTreeNode> charNodes = new HashMap<>();
        HashMap<String, DefaultMutableTreeNode> accNodes = new HashMap<>();

        String prefsPath = getPreference(MultiTool.PREFS_AO_PREFS_DIR, null);
        AOUtils.applyToBackpacks(new File(prefsPath), (backpack) -> 
        {
            DefaultMutableTreeNode backpackNode = new DefaultMutableTreeNode(backpack);
            
            String account = backpack.getAccount();
            DefaultMutableTreeNode accNode = accNodes.get(account);
            if (accNode == null)
            {
                accNode = new DefaultMutableTreeNode(account);
                accNodes.put(account, accNode);
                root.add(accNode);
            }
            
            String toon = backpack.getCharacter();
            DefaultMutableTreeNode charNode = charNodes.get(toon);
            if (charNode == null)
            {
                charNode = new DefaultMutableTreeNode(toon);
                charNodes.put(toon, charNode);
                accNode.add(charNode);
            }
            
            charNode.add(backpackNode);
        });
        
        return root;
    }
    
    private JPanel buildDetailPanel(TreePath[] selected)
    {
        JLabel namePrefixLbl = new JLabel(getString("label.prefix.name"));
        JTextField namePrefixTxt = new JTextField(getString("text.prefix.default"));

        JTextArea logTxtArea = new JTextArea();
        logTxtArea.setEditable(false);
        
        JScrollPane logScrollPane = new JScrollPane(logTxtArea);
        logScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton nameButton = new JButton(getString("button.name.name"));
        nameButton.addActionListener((ActionEvent e) ->
        {
            if (e.getSource() == nameButton)
            {
                String prefsPath = getPreference(MultiTool.PREFS_AO_PREFS_DIR, null);
                if (prefsPath != null)
                {
                    logTxtArea.setText("");
                    
                    Properties props = new Properties();
                    
                    String namePrefix = namePrefixTxt.getText();
                    if (namePrefix != null && !namePrefix.isEmpty())
                    {
                        props.setProperty("namePrefix", namePrefix);
                    }
                    props.setProperty("renamePattern", Pattern.quote(namePrefix)+"\\d+");
                    props.setProperty("prefsDir", prefsPath);
                    
                    NamerWorker task = new NamerWorker(props, logTxtArea);
                    task.addPropertyChangeListener((PropertyChangeEvent evt) ->
                    {
                        if ("progess".equals(evt.getPropertyName()))
                        {
                            //progressBar.setValue((Integer)evt.getNewValue());
                        }
                    });
                    task.execute();
                }
            }
        });
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        p.add(namePrefixLbl,   new GridBagConstraints(1,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        p.add(namePrefixTxt,   new GridBagConstraints(2,0, 1,1, 1.0,0.0, GridBagConstraints.EAST,   GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2), 0,0));
        
        p.add(logScrollPane,   new GridBagConstraints(1,1, 2,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,       new Insets(2,2,2,2), 0,0));
        p.add(nameButton,      new GridBagConstraints(1,2, 2,1, 1.0,0.0, GridBagConstraints.EAST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        
        return p;
    }
    
    private JPanel buildPanel()
    {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.3);
        splitPane.setDividerSize(4);

        TreeNode root = buildBackpackTree();
        JTree tree = new JTree(root);
        tree.setCellRenderer(new MyTreeRenderer());
        tree.addTreeSelectionListener((TreeSelectionEvent e) ->
        {
            Component detailView = splitPane.getRightComponent();
            if (detailView != null)
            {
                splitPane.remove(detailView);
            }
            detailView = buildDetailPanel(tree.getSelectionPaths());
            splitPane.setRightComponent(detailView);
        });
        tree.setSelectionRow(0);

        JScrollPane treeView = new JScrollPane(tree);
        splitPane.setLeftComponent(treeView);
        
        JPanel p = new JPanel();
        p.setName(getString("panel.name"));
        p.setLayout(new GridBagLayout());
        p.add(splitPane, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        return p;
    }
    
    private class MyTreeRenderer extends DefaultTreeCellRenderer 
    {
        @Override
        public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object obj = node.getUserObject();
            if (obj instanceof Backpack)
            {
                Backpack bp = (Backpack)obj;
                setText(bp.getName());
                //                 setIcon(tutorialIcon);
                // setToolTipText("This book is in the Tutorial series.");
            }

            return this;
        }
    }
    
    private class NamerWorker extends SwingWorker<Object, String>
    {
        private Properties props = null;
        private JTextArea textArea = null;
        
        public NamerWorker(Properties props, JTextArea textArea)
        {
            this.props = props;
            this.textArea = textArea;
        }
        
        @Override
        public Void doInBackground()
        {
            String prefsPath = props.getProperty("prefsDir");
            File prefsDir = new File(prefsPath);
            
            String prefix = props.getProperty("namePrefix", "backpack");
            
            AOUtils.applyToBackpacks(prefsDir, (backpack) ->
            {
                try
                {
                    backpack.setListMode(true);
                    
                    String oldName = backpack.getName();
                    String newName = prefix + backpack.getXMLNumber();

                    if (checkRename(oldName, newName))
                    {
                        backpack.setName(newName);
                        LOG.log(Level.FINER, "Renamed backpack \"{0}\" to \"{1}\"", new Object[]{oldName, newName});
                    }
                    else
                    {
                        LOG.log(Level.FINER, "Skipped backpack \"{0}\"", oldName);
                    }
                }
                catch (IOException ex)
                {
                    LOG.log(Level.WARNING, "Exception trown trying to rename backpack in {0}", new Object[]{backpack.getXMLPath()});
                    handleException(ex, NamerWorker.class.getCanonicalName(), "doInBackground");
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
        
        private boolean checkRename(String oldName, String newName)
        {
            if (newName != null && !newName.isEmpty())
            {
                if (oldName == null || oldName.isEmpty())
                {
                    return true;
                }
                
                if (oldName.equals(newName))
                {
                    return false;
                }

                String renamePattern = props.getProperty("renamePattern");
                if (oldName.matches(renamePattern))
                {
                    return true;
                }
            }
            
            return false;
        }
    }

}
