/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author javier
 */
public class AOUtils
{
    public static HashMap<String, File> getCharacterDirs()
    {
        return AOUtils.getCharacterDirs(null);
    }
    
    /**
     * A map from characterName -> Directory File
     * @param account
     * @return 
     */
    public static HashMap<String, File> getCharacterDirs(File account)
    {
        HashMap<String, File> result = new HashMap();
        
        ArrayList<File> accounts = new ArrayList();
        if (account != null)
        {
            accounts.add(account);
        }
        else
        {
            HashMap<String, File> allAccounts = AOUtils.getAccountDirs();
            allAccounts.entrySet().stream().forEach((entry) ->
            {
                accounts.add(entry.getValue());
            });
        }
        
        for (File acc : accounts)
        {
            File[] accSubDirs = acc.listFiles((File file) ->
            {
                return file.isDirectory() && file.getName().startsWith("Char");
            });

            for (File charDir : accSubDirs)
            {
                String charName = AOUtils.getCharacterNameForDir(charDir);
                result.put(charName, charDir);
            }
        }
        
        return result;
    }
    
    public static String getCharacterNameForDir(File characterDir)
    {
        return characterDir.getName();
    }
    
    public static HashMap<String, File> getAccountDirs()
    {
        HashMap<String, File> result = new HashMap();
        
        File prefsDir = AOUtils.getAOPrefsDir();
        if (prefsDir == null)
        {
            return null;
        }
        
        File[] accDirs = prefsDir.listFiles((File file) ->
        {
            if (file.isDirectory())
            {
                File[] subs = file.listFiles();
                for (File sub : subs)
                {
                    if (sub.getName().equals("Login.cfg"))
                    {
                        return true;
                    }
                }
            }
            return false;
        });
        
        for (File accDir : accDirs)
        {
            result.put(accDir.getName(), accDir);
        }
        
        return result;
    }
    
    public static File geAOLocalDir()
    {
        String aoLocalDir = System.getProperty("user.home") + 
                            File.separator + "AppData" + 
                            File.separator + "Local" + 
                            File.separator +  "Funcom" + 
                            File.separator + "Anarchy Online";
        File aoDir = new File(aoLocalDir);
        if (!aoDir.exists() || !aoDir.isDirectory())
        {
            return null;
        }

        File[] subDirs = aoDir.listFiles();
        for (File subDir : subDirs)
        {
            if (subDir.isDirectory())
            {
                File tmpFile = new File(subDir.getAbsolutePath() +
                                        File.separator + "Anarchy Online");
                if (tmpFile.exists() && tmpFile.isDirectory())
                {
                    return tmpFile;
                }
            }
        }

        return null;
    }
    
    public static File getAOPrefsDir()
    {
        File localDir = AOUtils.geAOLocalDir();
        if (localDir != null)
        {
            String prefsPath = localDir.getAbsolutePath() + 
                               File.separator + "Prefs";
            File prefsDir = new File(prefsPath);
            if (prefsDir.exists() && prefsDir.isDirectory())
            {
                return prefsDir;
            }
        }
        
        return null;
    }
    
    public static HashMap<String, File> getWindowLogFiles(File charDir)
    {
        String windowsDirPath = charDir.getAbsolutePath() +
                File.separator + "Chat" +
                File.separator + "Windows";
        File windowsDir = new File(windowsDirPath);
        if (!windowsDir.exists() || !windowsDir.isDirectory())
        {
            return null;
        }
        
        HashMap<String, File> results = new HashMap();
        
        File[] windows = windowsDir.listFiles();
        for (File window : windows)
        {
            String cfgPath = window.getAbsolutePath() +
                            File.separator + "Config.xml";
            File configXml = new File(cfgPath);
            
            if (configXml.exists())
            {
                try
                {
                    SAXBuilder builder = new SAXBuilder();
                    Document doc = (Document)builder.build(configXml);
                    Element rootNode = doc.getRootElement();
                    
                    List<Element> stringElems = rootNode.getChildren("String");
                    stringElems.stream().forEach((stringElem) ->
                    {
                        String elemName = stringElem.getAttributeValue("name");
                        if (elemName.equals("name"))
                        {
                            String elemVal = stringElem.getAttributeValue("value");
                            elemVal = elemVal.substring(1, elemVal.length()-1);
                            String logPath = window.getAbsolutePath() +
                                             File.separator + "Log.txt";
                            File logFile = new File(logPath);
                            
                            results.put(elemVal, logFile);
                        }
                    });
                }
                catch (JDOMException|IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        return results;
    }
    
}
