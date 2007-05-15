/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.sandbox.reformatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Kirill Sorokin
 */
public class Reformatter {
    private File file;
    private int  length;
    
    public Reformatter(File file, int length) {
        this.file   = file;
        this.length = length;
    }
    
    public void reformat() throws IOException {
        List<String> lines = new LinkedList<String>();
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        String string = null;
        while ((string = reader.readLine()) != null) {
            lines.add(string);
        }
        
        reader.close();
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            
            if (line.length() > length) {
                int index = findPrevious(line, ' ', length);
                
                if (index != -1) {
                    lines.add(i, line.substring(0, index));
                    lines.set(i + 1, line.substring(index + 1));
                }
            } else if (!line.trim().equals("")) {
                String next = lines.get(i + 1);
                
                if (!next.trim().equals("")) {
                    if (!line.endsWith(" ")) {
                        line = line + " " + next;
                    } else {
                        line = line + next;
                    }
                    
                    if (line.length() > length) {
                        int index = findPrevious(line, ' ', length);
                        
                        if (index != -1) {
                            lines.set(i, line.substring(0, index));
                            lines.set(i + 1, line.substring(index + 1));
                        }
                    } else {
                        lines.set(i, line);
                        lines.remove(i + 1);
                    }
                }
            }
        }
        
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        
        for (String line: lines) {
            writer.println(line);
        }
        
        writer.close();
    }
    
    private int findPrevious(String string, char ch, int start) {
        for (int i = length; i > 0; i--) {
            if (string.charAt(i) == ch) {
                return i;
            }
        }
        
        return -1;
    }
    
    public static void main(String[] args) {
        try {
            new Reformatter(new File("D:\\temp\\license.txt"), 55).reformat();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
