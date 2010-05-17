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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.model.CsmFile;

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
    private CsmFile csmFile = null;
    
    /** Creates a new instance of CompileInfo */
    public FileInfo(String compileStr) throws IllegalArgumentException {
        
        // Check if it is a compilation string ...
        if (compileStr.indexOf(" -c ") == -1) { // NOI18N
            throw new IllegalArgumentException("This is not a compilation string."); // NOI18N
        }
        
        includes = new ArrayList<String>();
        defines = new ArrayList<String>();
        
        StringTokenizer st = new StringTokenizer(compileStr, " "); // NOI18N
        
        int total_count = st.countTokens();
        
        compiler = st.nextToken();
        workdir = st.nextToken();
        
        int token_count = 2;
        
        if (workdir.startsWith("\"")) { // NOI18N
            String tok = st.nextToken();
            do {
                workdir += " " + tok; // NOI18N
                token_count++;
            } while (!tok.endsWith("\"")); // NOI18N
            
            workdir = workdir.substring(1, workdir.length() - 1);
        }
        
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            token_count++;
            
            if (token.equals("-o")) { // NOI18N
                objFileName = calculatePath(st.nextToken());
                token_count++;
            } else if (token.startsWith("-I")) { // NOI18N
                if (token.length() == 2) {
                    includes.add(calculatePath(st.nextToken()));
                    token_count++;
                } else {
                    includes.add(calculatePath(token.substring(2)));
                }
            } else if (token.startsWith("-D")) { // NOI18N
                if (token.length() == 2) {
                    defines.add(st.nextToken());
                    token_count++;
                } else {
                    defines.add(token.substring(2));
                }
            } else {
                // TODO: Change algorythm!
                if (!token.startsWith("-")) { // NOI18N
                    srcFileName = calculatePath(token);
                }
            }
        }
        
        if (objFileName == null && srcFileName != null ) {
            objFileName = srcFileName.substring(0, srcFileName.lastIndexOf('.')) + ".o"; // NOI18N
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
            if (file.startsWith("/") || file.startsWith("\\")) { // NOI18N
                path = new File(file).getCanonicalPath();
            } else {
                path = new File(workdir + File.separator + file).getCanonicalPath();
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot convert " + file + " to canonical path"); // NOI18N
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

    public CsmFile getCsmFile() {
	return csmFile;
    }

    public void setCsmFile(CsmFile csmFile) {
	this.csmFile = csmFile;
    }    
}
