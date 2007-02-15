/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package test.dwarfclassview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ConfigFile {
    private ArrayList<String> filesToAnalyze = new ArrayList<String>();
    private ArrayList<String> projectDirs = new ArrayList<String>();
    private BufferedReader file = null;
    private String fileDir = ""; // NOI18N
    
    public ConfigFile(String fname) throws IOException {
        this.file = new BufferedReader(new FileReader(fname));
        
        if (!file.ready()) {
            throw new IOException("Cannot access " + fname); // NOI18N
        }
        
        fileDir = new File(fname).getParentFile().getAbsolutePath();
        
        parse();
    }
    
    private void parse() throws IOException {
        String line;
        
        projectDirs.add("."); // NOI18N
        
        while ((line = file.readLine()) != null) {
            
            line = line.trim();
            
            if (line.length() == 0 || line.charAt(0) == '#') {
                continue;
            }
            
            if (line.startsWith("Dir:")) { // NOI18N
                String projectDir = line.substring(5);
                
                if (!projectDirs.contains(projectDir)) {
                    projectDirs.add(projectDir);
                }
                
            } else {
                if (!new File(line).exists()) {
                    line = fileDir + File.separator + line;
                    if (!new File(line).exists()) {
                        continue;
                    }
                }
                if (!filesToAnalyze.contains(line)) {
                    filesToAnalyze.add(line);
                }
            }
        }
    }
    
    public ArrayList<String> getFilesToAnalyze() {
        return filesToAnalyze;
    }
    
    public ArrayList<String> getProjectDirs() {
        return projectDirs;
    }
    
}
