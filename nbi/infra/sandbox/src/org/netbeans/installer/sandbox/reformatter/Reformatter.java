/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
