/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 *
 * @author javier
 */
public abstract class AbstractPlugin implements Plugin
{
    private final Logger LOG = Logger.getLogger(this.getClass().getCanonicalName());
    private ResourceBundle bundle = null;
    
    public AbstractPlugin()
    {
        bundle = ResourceBundle.getBundle(getFullBundleName("Messages"));
    }
    
    protected String getString(String key)
    {
        return bundle.getString(key);
    }
    
    protected void handleException(Exception ex, String sourceClass, String sourceMethod)
    {
        LOG.throwing(sourceClass, sourceMethod, ex);
        ex.printStackTrace();
    }
    
    private String getFullBundleName(String bundleName)
    {
        String simpleName = this.getClass().getSimpleName();
        String canonName = this.getClass().getCanonicalName();
        
        int lastDotIdx = canonName.lastIndexOf(".");
        
        String fullName = canonName.substring(0, lastDotIdx) + "." + bundleName;
        return fullName;
    }
}
