/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.api;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Nikolay Koldunov
 */
public class SourceCodeProvider {
    public static String getSourcefile(String name) {
        StringBuilder builder = new StringBuilder();
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
            String s;
            
            while ((s = br.readLine()) != null) {
                builder.append(s);
            }
            
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return builder.toString();
    }
    
    public static String getHTMLForSourcefile(String name) {
        StringBuilder builder = new StringBuilder();
        
        builder.append("<html>");
        builder.append("<head>");
        builder.append("<title>Source Code</title>");            
        builder.append("</head>");
        builder.append("<body>");
        builder.append("<pre style=\"font-size: medium; font-family: Courier, monospace;\">");
                
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
            String s;
            int count = 0;
            while ((s = br.readLine()) != null) {
                builder.append("<input type=\"button\" id=\"" + ++count + "\" value=\"" + count + "\" onClick=\"toggleLineBreakpoint(id)\" class=\"glyph\"/>");
                builder.append("<span id=\"line" + count + "\">");
                builder.append(s);
                builder.append("</span>");
                builder.append("<br>");
            }
            
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        builder.append("</pre>");
        builder.append("</body>");
        builder.append("</html>");
        
        return builder.toString();
    }
}
