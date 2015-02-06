/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins.backpacknamer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mx.cornejo.anarchyonline.multitool.MultiTool;
import mx.cornejo.anarchyonline.multitool.plugins.AbstractPlugin;
import mx.cornejo.anarchyonline.utils.AOCharacter;
import mx.cornejo.anarchyonline.utils.AOUtils;
import mx.cornejo.anarchyonline.utils.Account;
import mx.cornejo.anarchyonline.utils.Backpack;
import org.jdom2.JDOMException;

/**
 *
 * @author javier
 */
public class BackpackNamer extends AbstractPlugin
{
    private JPanel panel = null;
    private JTree tree = null;
    
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
    
    private DefaultMutableTreeNode buildBackpackTreeRoot()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(getString("tree.root.text"));
        
        HashMap<AOCharacter, DefaultMutableTreeNode> charNodes = new HashMap<>();
        HashMap<Account, DefaultMutableTreeNode> accNodes = new HashMap<>();

        String prefsPath = getPreference(MultiTool.PREFS_AO_PREFS_DIR, null);
        AOUtils.applyToBackpacks(new File(prefsPath), (backpack, toon, account) -> 
        {
            DefaultMutableTreeNode backpackNode = new DefaultMutableTreeNode(backpack);
            
            DefaultMutableTreeNode accNode = accNodes.get(account);
            if (accNode == null)
            {
                accNode = new DefaultMutableTreeNode(account);
                accNodes.put(account, accNode);
                root.add(accNode);
            }
            
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
    
    private JPanel buildDetailPanel(JTree tree)
    {
        JCheckBox renameChkBox = new JCheckBox(getString("checkbox.rename.text"));
        JTextField namePrefixTxt = new JTextField(getString("text.prefix.default"));
        JLabel nameSuffixLbl = new JLabel("####");
        
        JCheckBox listModeChkBox = new JCheckBox(getString("checkbox.listmode.text"));
        JComboBox listModeCombo = new JComboBox(new String[] {getString("combobox.mode.list"), getString("combobox.mode.icon")});
        
        JTextArea logTxtArea = new JTextArea();
        logTxtArea.setEditable(false);
        
        JScrollPane logScrollPane = new JScrollPane(logTxtArea);
        logScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton nameButton = new JButton(getString("button.name.name"));
        nameButton.addActionListener((ActionEvent e) ->
        {
            logTxtArea.setText("");

            TreePath[] selectedPaths = tree.getSelectionPaths();

            Properties props = new Properties();
            
            boolean rename = renameChkBox.isSelected();
            if (rename)
            {
                String namePrefix = namePrefixTxt.getText();
                if (namePrefix != null && !namePrefix.isEmpty())
                {
                    props.setProperty("namePrefix", namePrefix);
                }
            }
            
            boolean changeMode = listModeChkBox.isSelected();
            if (changeMode)
            {
                String mode = (String)listModeCombo.getSelectedItem();
                if (mode.equals(getString("combobox.mode.list")))
                {
                    props.setProperty("backpackMode", "list");
                }
                else
                {
                    props.setProperty("backpackMode", "icon");
                }
            }

            NamerWorker task = new NamerWorker(selectedPaths, props, logTxtArea);
            task.addPropertyChangeListener((PropertyChangeEvent evt) ->
            {
                if ("progess".equals(evt.getPropertyName()))
                {
                    //progressBar.setValue((Integer)evt.getNewValue());
                }
            });
            task.execute();
        });
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        p.add(renameChkBox,    new GridBagConstraints(0,0, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        p.add(namePrefixTxt,   new GridBagConstraints(1,0, 2,1, 1.0,0.0, GridBagConstraints.EAST,   GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2), 0,0));
        p.add(nameSuffixLbl,   new GridBagConstraints(3,0, 1,1, 0.0,0.0, GridBagConstraints.EAST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        
        p.add(listModeChkBox,  new GridBagConstraints(0,1, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        p.add(listModeCombo,   new GridBagConstraints(1,1, 1,1, 0.0,0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        
        p.add(nameButton,      new GridBagConstraints(2,1, 2,1, 0.0,0.0, GridBagConstraints.EAST,   GridBagConstraints.NONE,       new Insets(2,2,2,2), 0,0));
        
        p.add(logScrollPane,   new GridBagConstraints(0,2, 4,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,       new Insets(2,2,2,2), 0,0));
        
        return p;
    }
    
    private void reloadTree()
    {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        Enumeration<DefaultMutableTreeNode> childEnum = root.depthFirstEnumeration();
        while (childEnum.hasMoreElements())
        {
            DefaultMutableTreeNode child = childEnum.nextElement();
            if (child.isLeaf())
            {
                Backpack backpack = (Backpack)child.getUserObject();
                try
                {    
                    backpack.reload();
                    model.nodeChanged(child);
                }
                catch (JDOMException|IOException ex)
                {
                    handleException(ex, BackpackNamer.class.getCanonicalName(), "reloadTree");
                }
            }
        }
    }
    
    private JPanel buildPanel()
    {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(4);

        DefaultMutableTreeNode root = buildBackpackTreeRoot();
        tree = new JTree(root);
        tree.setCellRenderer(new MyTreeRenderer());
        tree.addTreeSelectionListener((TreeSelectionEvent e) ->
        {
            /*
            int divLoc = splitPane.getDividerLocation();
            Component detailView = splitPane.getRightComponent();
            if (detailView != null)
            {
                splitPane.remove(detailView);
            }
            detailView = buildDetailPanel(tree);
            splitPane.setRightComponent(detailView);
            splitPane.setDividerLocation(divLoc);
            */
            
            /*
            TreePath[] modifiedPaths = e.getPaths();
            for (TreePath path : modifiedPaths)
            {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            if (!node.isLeaf())
            {
            disable = true;
            Enumeration<DefaultMutableTreeNode> enu = node.depthFirstEnumeration();
            while(enu.hasMoreElements())
            {
            DefaultMutableTreeNode elem = enu.nextElement();
            
            TreePath elemPath = new TreePath(elem.getPath());
            if (e.isAddedPath(path))
            {
            tree.addSelectionPath(elemPath);
            }
            else
            {
            tree.removeSelectionPath(elemPath);
            }
            }
            disable = false;
            }
            }
            */
        });
        
        
        // Expand account nodes
        Enumeration<DefaultMutableTreeNode> accEnu = root.children();
        while (accEnu.hasMoreElements())
        {
            DefaultMutableTreeNode acc = accEnu.nextElement();
            tree.expandPath(new TreePath(acc.getPath()));
        }
        
        JScrollPane treeView = new JScrollPane(tree);
        splitPane.setLeftComponent(treeView);
        splitPane.setRightComponent(buildDetailPanel(tree));
        
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
                setText(((Backpack)obj).getName());
                // setIcon(tutorialIcon);
                // setToolTipText("This book is in the Tutorial series.");
            }
            else if (obj instanceof AOCharacter)
            {
                setText(((AOCharacter)obj).getName());
            }
            else if (obj instanceof Account)
            {
                setText(((Account)obj).getName());
            }

            return this;
        }
    }
    
    private class NamerWorker extends SwingWorker<Object, String>
    {
        private TreePath[] selectedPaths = null;
        private Properties props = null;
        private JTextArea textArea = null;
        
        public NamerWorker(TreePath[] selectedPaths, Properties props, JTextArea textArea)
        {
            this.selectedPaths = selectedPaths;
            this.props = props;
            this.textArea = textArea;
        }
        
        private void processAccount(Account account)
        {
            account.getCharacters().stream().forEach((toon) -> 
            {
                processCharacter(toon);
            });
        }
        
        private void processCharacter(AOCharacter toon)
        {
            toon.getBackpacks().stream().forEach((backpack) ->
            {
                processBackpack(backpack);
            });
        }
        
        private void processBackpack(Backpack backpack)
        {
            String mode = props.getProperty("backpackMode");
            if (mode != null)
            {
                try
                {
                    if (backpack.isListMode() != mode.equals("list"))
                    {
                        backpack.setListMode(mode.equals("list"));
                        publish("Set backpack " + backpack.getName() + " mode to " + mode);
                    }
                }
                catch (IOException ex)
                {
                    LOG.log(Level.WARNING, "Exception trown trying to rename backpack in {0}", new Object[]{backpack.getXMLPath()});
                    handleException(ex, NamerWorker.class.getCanonicalName(), "doInBackground");
                }
            }

            String prefix = props.getProperty("namePrefix");
            if (prefix != null)
            {
                try
                {
                    String oldName = backpack.getName();
                    String newName = prefix + backpack.getXMLNumber();

                    if (checkRename(oldName, newName))
                    {
                        backpack.setName(newName);
                        publish("Set backpack " + oldName + " name to " + newName);
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
            }
        }
        
        @Override
        public Void doInBackground()
        {
            for (TreePath path : selectedPaths)
            {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                if (node.isRoot())
                {
                    Enumeration<DefaultMutableTreeNode> accountEnum = node.children();
                    while (accountEnum.hasMoreElements())
                    {
                        processAccount((Account)accountEnum.nextElement().getUserObject());
                    }
                    
                    // was root, nothing else to do
                    break;
                }
                
                Object obj = node.getUserObject();
                if (obj instanceof Backpack)
                {
                    processBackpack((Backpack)obj);
                }
                else if (obj instanceof AOCharacter)
                {
                    processCharacter((AOCharacter)obj);
                }
                else if (obj instanceof Account)
                {
                    processAccount((Account)obj);
                }
            }
            publish(getString("log.messages.finished"));
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
        
        @Override
        protected void done()
        {
            reloadTree();
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

                //String renamePattern = props.getProperty("renamePattern");
                if (oldName.matches("[^0-9]+\\d+"))
                {
                    return true;
                }
            }
            
            return false;
        }
    }

}
