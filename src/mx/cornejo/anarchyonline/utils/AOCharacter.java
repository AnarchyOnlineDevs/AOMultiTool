/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.JDOMException;

/**
 *
 * @author javier
 */
public class AOCharacter
{
    private List<Backpack> backpacks = null;
    private List<ChatWindow> chatWindows = null;
    private File dir = null;
    
    public AOCharacter(File dir)
    {
        this.dir = dir;
    }
    
    public List<Backpack> getBackpacks()
    {
        if (backpacks == null)
        {
            String containersPath = dir + File.separator + "Containers";
            File containersDir = new File(containersPath);
            if (containersDir.exists() && containersDir.isDirectory())
            {
                File[] files = containersDir.listFiles(new FilenameFilter()
                {
                    public boolean accept(File folder, String name)
                    {
                        return name.matches("Container_51017x\\d+?\\.xml");
                    }
                });
                
                backpacks = new ArrayList<>();
                
                for (File file : files)
                {
                    try
                    {
                        backpacks.add(new Backpack(file));
                    }
                    catch (JDOMException|IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        
        if (backpacks != null && backpacks.size() == 0)
        {
            backpacks = null;
        }
        
        return backpacks;
    }
    
    public List<ChatWindow> getChatWindows()
    {
        if (chatWindows == null)
        {
            String windowsDirPath = dir.getAbsolutePath() +
                                    File.separator + "Chat" +
                                    File.separator + "Windows";
            File windowsDir = new File(windowsDirPath);
            if (windowsDir.exists() && windowsDir.isDirectory())
            {
                chatWindows = new ArrayList<>();
                
                File[] windowsDirs = windowsDir.listFiles();
                for (File windowDir : windowsDirs)
                {
                    try
                    {
                        chatWindows.add(new ChatWindow(windowDir));
                    }
                    catch (JDOMException|IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
        
        if (chatWindows.isEmpty())
        {
            chatWindows = null;
        }
        
        return chatWindows;
    }
}
