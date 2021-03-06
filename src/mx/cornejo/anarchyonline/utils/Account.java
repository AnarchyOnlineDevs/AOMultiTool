/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.cornejo.anarchyonline.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author javier
 */
public class Account
{
    private List<AOCharacter> characters = null;
    private File dir = null;
    
    public Account(File dir) throws FileNotFoundException
    {
        boolean loginCfgFound = false;
        
        if (dir.exists() && dir.isDirectory())
        {
            File[] subs = dir.listFiles();
            if (subs != null)
            {
                for (File sub : subs)
                {
                    if (sub.getName().equals("Login.cfg"))
                    {
                        loginCfgFound = true;
                        break;
                    }
                }
            }
        }
        
        if (!loginCfgFound)
        {
            throw new FileNotFoundException("Login.cfg not found inside " + dir.getAbsolutePath());
        }
        
        this.dir = dir;
    }
    
    public String getName()
    {
        return dir.getName();
    }
    
    public List<AOCharacter> getCharacters()
    {
        if (characters == null)
        {
            characters = new ArrayList<>();

            File[] subDirs = dir.listFiles((file) ->
            {
                return file.isDirectory() && file.getName().startsWith("Char");
            });
            
            for (File charDir : subDirs)
            {
                characters.add(new AOCharacter(charDir));
            }
        }
        
        return characters;
    }
    
}
