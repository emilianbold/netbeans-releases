/*
 * GenerateAbbreviationsList.java
 *
 * Created on August 29, 2002, 1:15 PM
 */

package org.netbeans.test.editor.suites.abbrevs;

import java.io.FileWriter;
import java.io.PrintWriter;
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
        
        try {
            PrintWriter pw =  new PrintWriter(new FileWriter("/tmp/abbrevs.xml"));
            
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            
            pw.println("<Test Name=\"Abbreviations\" Author=\"ehucka\" Version=\"1.1\">");
            pw.println("<TestSubTest Name=\"BasicTests\" Author=\"ehucka\" Version=\"1.0\" Own_logger=\"true\">");
            pw.println("<TestStep Name=\"InvokeAllActions\">");
            while (keysIterator.hasNext()) {
                Object key = keysIterator.next();
                Object value = map.get(key);
                
                System.err.println("\t{" + key.toString() + ", " + value.toString() + "},");
                pw.println("<TestStringAction Name=\"string\" String=\""+key.toString()+" \" />");
                pw.println("<TestLogAction Name=\"caret-end-line\" Command=\"\" />");
                pw.println("<TestLogAction Name=\"insert-break\" Command=\"\" />");
            }
            pw.println("</TestStep>");
            pw.println("</TestSubTest>");
            pw.println("<Comment>");
            pw.println("<![CDATA[\"Test of invoking all abbreviations.\"]]>");
            pw.println("</Comment>");
            pw.println("</Test>\n");
            
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
