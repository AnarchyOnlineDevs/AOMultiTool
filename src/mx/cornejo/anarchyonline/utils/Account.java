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
            for (File sub : subs)
            {
                if (sub.getName().equals("Login.cfg"))
                {
                    loginCfgFound = true;
                    break;
                }
            }
        }
        
        if (!loginCfgFound)
        {
            throw new FileNotFoundException("Login.cfg not found inside " + dir.getAbsolutePath());
        }
        
        this.dir = dir;
    }
    
    public List<AOCharacter> getCharacters()
    {
        if (characters == null)
        {
            File[] subDirs = dir.listFiles((file) ->
            {
                return file.isDirectory() && file.getName().startsWith("Char");
            });
            
            characters = new ArrayList<>();
            
            for (File charDir : subDirs)
            {
                characters.add(new AOCharacter(charDir));
            }
        }
        
        if (characters != null && characters.size() == 0)
        {
            characters = null;
        }
        
        return characters;
    }
    
}
