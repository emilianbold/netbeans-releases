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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author ak119685
 */
public class FileInfo {
    private String srcFileName = null;
    private String objFileName = null;
    private String compiler = null;
    private String workdir = null;
    private ArrayList<String> includes = null;
    private ArrayList<String> defines = null;
    
    /** Creates a new instance of CompileInfo */
    public FileInfo(String compileStr) throws IllegalArgumentException {
        
        // Check if it is a compilation string ...
        if (compileStr.indexOf(" -c ") == -1) {
            throw new IllegalArgumentException("This is not a compilation string.");
        }
        
        includes = new ArrayList<String>();
        defines = new ArrayList<String>();
        
        StringTokenizer st = new StringTokenizer(compileStr, " ");
        
        int total_count = st.countTokens();
        
        compiler = st.nextToken();
        workdir = st.nextToken();
        
        int token_count = 2;
        
        if (workdir.startsWith("\"")) {
            String tok = st.nextToken();
            do {
                workdir += " " + tok;
                token_count++;
            } while (!tok.endsWith("\""));
            
            workdir = workdir.substring(1, workdir.length() - 1);
        }
        
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            token_count++;
            
            if (token.equals("-o")) {
                objFileName = calculatePath(st.nextToken());
                token_count++;
            } else if (token.startsWith("-I")) {
                if (token.length() == 2) {
                    includes.add(calculatePath(st.nextToken()));
                    token_count++;
                } else {
                    includes.add(calculatePath(token.substring(2)));
                }
            } else if (token.startsWith("-D")) {
                if (token.length() == 2) {
                    defines.add(st.nextToken());
                    token_count++;
                } else {
                    defines.add(token.substring(2));
                }
            } else {
                // TODO: Change algorythm!
                if (!token.startsWith("-")) {
                    srcFileName = calculatePath(token);
                }
            }
        }
        
        if (objFileName == null) {
            objFileName = srcFileName.substring(0, srcFileName.lastIndexOf('.')) + ".o";
        }
    }
    
    public String getSrcFileName() {
        return srcFileName;
    }
    
    public String getObjFileName() {
        return objFileName;
    }
    
    public ArrayList<String> getSysIncludes() {
        return includes;
    }
    
    public ArrayList<String> getQuoteIncludes() {
        // TODO: make disting. b/w sys and quote
        return includes;
    }
    
    public ArrayList<String> getDefines() {
        return defines;
    }
    
    private String calculatePath(String file) {
        String path = "";
        
        try {
            if (file.startsWith("/") || file.startsWith("\\")) {
                path = new File(file).getCanonicalPath();
            } else {
                path = new File(workdir + File.separator + file).getCanonicalPath();
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot convert " + file + " to canonical path");
        }
        
        return path;
    }

    ArrayList<String> convertPaths(ArrayList<String> paths) {
        ArrayList<String> result = new ArrayList<String>();
        
        for (String path : paths) {
            result.add(calculatePath(path));
        }
        
        return result;
    }
}
