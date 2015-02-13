/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins.logparser;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 *
 * @author javier
 */
public class RulesPanel extends JPanel
{
    private JTable table = new JTable(new MyTableModel());
    private ArrayList<Rule> rules = new ArrayList<>();
    
    private Frame getParentFrame()
    {
        Container c = this;
        while (c.getParent() != null)
        {
            c = c.getParent();
        }
        return (Frame)c;
    }
    
    public RulesPanel()
    {
        super();
        setName("Rules");
        setLayout(new GridBagLayout());
        
        table.getColumnModel().getColumn(0).setMaxWidth(20);
        
        JButton addButton = new JButton("Add");
        addButton.addActionListener((ActionEvent e) ->
        {
            openRuleEditDialog(null);
        });
        
        JButton editButton = new JButton("Edit");
        editButton.addActionListener((ActionEvent e) ->
        {
            openRuleEditDialog(rules.get(table.getSelectedRow()));
        });
        
        JButton deleteButton = new JButton("Remove");
        deleteButton.addActionListener((ActionEvent e) ->
        {
            rules.remove(table.getSelectedRow());
        });
        
        add(new JScrollPane(table), new GridBagConstraints(0,0, 3,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
        add(addButton,              new GridBagConstraints(0,1, 1,1, 0.0,1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
        add(editButton,             new GridBagConstraints(1,1, 1,1, 0.0,1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
        add(deleteButton,           new GridBagConstraints(2,1, 1,1, 0.0,1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,0,0,0), 0,0));
    }
    
    private void openRuleEditDialog(Rule rule)
    {
        JDialog dialog = new JDialog(getParentFrame());
        dialog.add(new RuleEditPanel(rule));
        dialog.pack();
        dialog.setVisible(true);
    }
    
    private class MyTableModel extends AbstractTableModel
    {
        String[] columnNames = new String[] {"", "Name", "Condition", "Action"};

        @Override
        public Object getValueAt(int row, int col)
        {
            Rule rule = rules.get(row);
            
            if (rule != null)
            {
                switch (col)
                {
                    case 0:
                        return rule.isEnabled();
                    case 1:
                        return rule.getName();
                    case 2:
                        return rule.getCondition();
                    case 3:
                        return rule.getAction();
                }
            }
            return null;
        }
        
        @Override
        public int getColumnCount()
        {
            return 4;
        }
        
        @Override
        public int getRowCount()
        {
            return rules.size();
        }
        
        @Override
        public String getColumnName(int col)
        {
            return columnNames[col];
        }
        
        @Override
        public Class getColumnClass(int col)
        {
            return getValueAt(0, col).getClass();
        }
    }
}
