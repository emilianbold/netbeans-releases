/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
