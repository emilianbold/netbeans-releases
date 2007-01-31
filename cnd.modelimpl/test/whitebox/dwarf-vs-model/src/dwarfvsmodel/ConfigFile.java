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
