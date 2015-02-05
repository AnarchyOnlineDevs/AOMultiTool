/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javier
 */
public class AOUtils
{
    public static List<Account> getAccounts(File prefsDir)
    {
        if (prefsDir == null || !prefsDir.exists() || !prefsDir.isDirectory())
        {
            return null;
        }

        List<Account> accounts = new ArrayList<>();
        
        File[] accDirs = prefsDir.listFiles();
        for (File accDir : accDirs)
        {
            try
            {
                accounts.add(new Account(accDir));
            }
            catch (FileNotFoundException ex)
            {
                // do nothing
            }
        }
        
        if (accounts.isEmpty())
        {
            accounts = null;
        }
        
        return accounts;
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
    
    public static interface Applier<T>
    {
        void apply(T obj);
    }
    
    public static void applyToBackpacks(File prefsDir, Applier<Backpack> func)
    {
        List<Account> accounts = AOUtils.getAccounts(prefsDir);
        accounts.stream().forEach((account) -> 
        {
            List<AOCharacter> toons = account.getCharacters();
            toons.stream().forEach((toon) -> 
            {
                List<Backpack> backpacks = toon.getBackpacks();
                backpacks.stream().forEach((backpack) -> 
                {
                    func.apply(backpack);
                });
            });
        });
    }
    
    public static void applyToWindows(File prefsDir, Applier<ChatWindow> func)
    {
        List<Account> accounts = AOUtils.getAccounts(prefsDir);
        accounts.stream().forEach((account) -> 
        {
            List<AOCharacter> toons = account.getCharacters();
            toons.stream().forEach((toon) -> 
            {
                List<ChatWindow> windows = toon.getChatWindows();
                windows.stream().forEach((window) -> 
                {
                    func.apply(window);
                });
            });
        });
    }
}
