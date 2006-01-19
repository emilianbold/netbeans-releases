/*
 * Utils.java
 *
 * Created on January 19, 2006, 10:34 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.editor.ext.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author marek
 */
public class Utils {
    
    static String readFileContentToString(File file) throws IOException {
        StringBuffer buff = new StringBuffer();
        
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        
        String line;
        
        try{
            while ((line = rdr.readLine()) != null){
                buff.append(line + "\n");
            }
        } finally{
            rdr.close();
        }
        
        return buff.toString();
    }
    
}
