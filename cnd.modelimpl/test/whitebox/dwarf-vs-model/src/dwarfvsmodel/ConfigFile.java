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

package dwarfvsmodel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author ak119685
 */
public class ConfigFile {
    private BufferedReader file = null;
    private PrintStream log = null;
    HashMap<String, FileInfo> filesMap = null;
    
    public ConfigFile(String fname, PrintStream log) throws IOException {
        file = new BufferedReader(new FileReader(fname));
        
        if (!file.ready()) {
            throw new IOException();
        }
        
        this.log = log;
        
        parse();
    }
    
    private HashMap<String, FileInfo> parse() throws IOException {
        String line;
        filesMap = new HashMap<String, FileInfo>();
        int lineNo = 0;
        while ((line = file.readLine()) != null) {
            lineNo++;
//            if (log != null) {
//                log.println("Examing line " + line); // NOI18N
//            }
	    line = line.trim();
	    if( line.length() == 0 || line.charAt(0) == '#' ) {
		continue;
	    }
            try {
                FileInfo info = new FileInfo(line);
                String srcFile = info.getSrcFileName();
                if( srcFile == null ) {
                    log.println("Error in line " + lineNo + ": can not determine source file"); // NOI18N
                }
                else if (filesMap.put(srcFile, info) != null && log != null) {
                    log.println("File " + srcFile + " has been compiled more than once. Disregarding previous entries!"); // NOI18N
                }
                
            } catch (IllegalArgumentException e) { 
                if (log != null) {
                    log.println("Cannot parse following line: " + line); // NOI18N
                    log.println(e.getMessage());
                }
            }
        }
        
        file.close();
        
        return filesMap;
    }

    public Collection<FileInfo> getFilesToProcess() {
        return filesMap.values();
    }

    public Set<String> getSrcFileNames() {
        return filesMap.keySet();
    }
    
    public String getObjFileName(String srcFileName) {
        return filesMap.get(srcFileName).getObjFileName();
    }
}
