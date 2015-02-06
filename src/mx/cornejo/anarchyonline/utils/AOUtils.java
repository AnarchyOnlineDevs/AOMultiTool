/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author javier
 */
public class AOUtils
{
    public static List<Account> getAccounts(File prefsDir)
    {
        List<Account> accounts = new ArrayList<>();
        if (prefsDir != null && prefsDir.exists() && prefsDir.isDirectory())
        {
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
    
    public static interface Applier3<T, U, V>
    {
        void apply(T obj, U obj2, V obj3);
    }
    
    public static void applyToBackpacks(File prefsDir, Applier<Backpack> func)
    {
        applyToBackpacks(prefsDir, (backpack, toon, account) ->
        {
            func.apply(backpack);
        });
    }
    
    public static void applyToBackpacks(File prefsDir, Applier3<Backpack,AOCharacter,Account> func)
    {

        AOUtils.getAccounts(prefsDir).stream().forEach((account) -> 
        {
            account.getCharacters().stream().forEach((toon) -> 
            {
                List<Backpack> backpacks = toon.getBackpacks();
                backpacks.sort((Backpack b1, Backpack b2) ->
                {
                    String name1 = b1.getName();
                    if (name1 == null)
                    {
                        name1 = "";
                    }

                    String name2 = b2.getName();
                    if (name2 == null)
                    {
                        name2 = "";
                    }

                    return name1.compareTo(name2);
                });

                backpacks.stream().forEach((backpack) -> 
                {
                    func.apply(backpack, toon, account);
                });
            });
        });
    }
    
    public static void applyToWindows(File prefsDir, Applier<ChatWindow> func)
    {
        AOUtils.getAccounts(prefsDir).stream().forEach((account) -> 
        {
            account.getCharacters().stream().forEach((toon) -> 
            {
                toon.getChatWindows().stream().forEach((window) -> 
                {
                    func.apply(window);
                });
            });
        });
    }
}
