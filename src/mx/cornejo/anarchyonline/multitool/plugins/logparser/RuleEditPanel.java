/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins.logparser;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author javier
 */
public class RuleEditPanel extends JPanel
{
    private Rule rule = null;
    
    public RuleEditPanel()
    {
        this(null);
    }
    
    public RuleEditPanel(Rule rule)
    {
        super();

        if (rule == null)
        {
            rule = new Rule();
            rule.setName("New rule");
        }
        this.rule = rule;

        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(300,300));
        
        add(new JLabel(rule.getName()));
    }    
    
}
