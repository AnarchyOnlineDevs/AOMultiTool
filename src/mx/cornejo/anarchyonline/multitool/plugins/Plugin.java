/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.multitool.plugins;

import java.util.prefs.Preferences;
import javax.swing.JPanel;

/**
 *
 * @author javier
 */
public interface Plugin
{
    JPanel getPanel();
    void cleanUp();
    void setGlobalPreferences(Preferences globalPrefs);
    
}
