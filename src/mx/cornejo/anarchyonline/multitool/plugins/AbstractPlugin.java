/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins;

import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 *
 * @author javier
 */
public abstract class AbstractPlugin implements Plugin
{
    protected final Logger LOG = Logger.getLogger(this.getClass().getCanonicalName());
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private Preferences globalPrefs = null;
    private ResourceBundle bundle = null;
    
    public AbstractPlugin()
    {
        bundle = ResourceBundle.getBundle(getFullBundleName("Messages"));
    }
    
    @Override
    public void setGlobalPreferences(Preferences globalPrefs)
    {
        this.globalPrefs = globalPrefs;
    }
    
    protected String getString(String key)
    {
        return bundle.getString(key);
    }
    
    protected String getPreference(String key, String def)
    {
        String value = prefs.get(key, null);
        if (value == null)
        {
            value = globalPrefs.get(key, null);
        }
        return value;
    }
    
    protected void setPreference(String key, String value)
    {
        prefs.put(key, value);
    }
    
    protected void handleException(Exception ex, String sourceClass, String sourceMethod)
    {
        LOG.throwing(sourceClass, sourceMethod, ex);
        ex.printStackTrace();
    }
    
    private String getFullBundleName(String bundleName)
    {
        String canonName = this.getClass().getCanonicalName();
        
        int lastDotIdx = canonName.lastIndexOf(".");
        
        String fullName = canonName.substring(0, lastDotIdx) + "." + bundleName;
        return fullName;
    }
}
