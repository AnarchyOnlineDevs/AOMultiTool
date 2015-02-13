/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins.logparser;

import java.beans.PropertyChangeSupport;

/**
 *
 * @author javier
 */
public class Rule
{
    public static final String PROP_ENABLED = "PROP_ENABLED";
    public static final String PROP_NAME = "PROP_NAME";
    public static final String PROP_CONDITION = "PROP_CONDITION";
    public static final String PROP_ACTION = "PROP_ACTION";

    private boolean enabled = false;
    private String name = null;
    private Condition condition = null;
    private Action action = null;

    private final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    /**
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, enabled);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        java.lang.String oldName = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, name);
    }

    /**
     * @return the condition
     */
    public Condition getCondition()
    {
        return condition;
    }

    /**
     * @param condition the condition to set
     */
    public void setCondition(Condition condition)
    {
        Condition oldCondition = this.condition;
        this.condition = condition;
        propertyChangeSupport.firePropertyChange(PROP_CONDITION, oldCondition, condition);
    }

    /**
     * @return the action
     */
    public Action getAction()
    {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(Action action)
    {
        Action oldAction = this.action;
        this.action = action;
        propertyChangeSupport.firePropertyChange(PROP_ACTION, oldAction, action);
    }
}
