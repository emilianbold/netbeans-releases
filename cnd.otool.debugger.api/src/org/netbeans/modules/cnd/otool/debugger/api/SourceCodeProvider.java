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
        
        builder.append("<html>"); // NOI18N
        builder.append("<head>"); // NOI18N
        builder.append("<title>Source Code</title>"); // NOI18N          
        builder.append("</head>"); // NOI18N
        builder.append("<body>"); // NOI18N
        builder.append("<pre style=\"font-size: medium; font-family: Courier, monospace;\">"); // NOI18N
                
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
            String s;
            int count = 0;
            while ((s = br.readLine()) != null) {
                builder.append("<input type=\"button\" id=\"" + ++count + "\" value=\"" + count + "\" onClick=\"toggleLineBreakpoint(id)\" class=\"glyph\"/>"); // NOI18N
                builder.append("<span id=\"line" + count + "\">"); // NOI18N
                builder.append(s);
                builder.append("</span>"); // NOI18N
                builder.append("<br>"); // NOI18N
            }
            
            br.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        builder.append("</pre>"); // NOI18N
        builder.append("</body>"); // NOI18N
        builder.append("</html>"); // NOI18N
        
        return builder.toString();
    }
}
