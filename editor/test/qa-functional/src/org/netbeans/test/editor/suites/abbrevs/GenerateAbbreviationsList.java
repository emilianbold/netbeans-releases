/*
 * GenerateAbbreviationsList.java
 *
 * Created on August 29, 2002, 1:15 PM
 */

package org.netbeans.test.editor.suites.abbrevs;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.jellytools.modules.editor.Abbreviations;

/**
 *
 * @author  jl105142
 */
public class GenerateAbbreviationsList {
    
    /** Creates a new instance of GenerateAbbreviationsList */
    public GenerateAbbreviationsList() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Map map  = Abbreviations.listAbbreviations("Java Editor");
        Set keys = map.keySet();
        Iterator keysIterator = keys.iterator();
        
        while (keysIterator.hasNext()) {
            Object key = keysIterator.next();
            Object value = map.get(key);
            
            System.err.println("\t{" + key.toString() + ", " + value.toString() + "},");
        }
    }
    
}
