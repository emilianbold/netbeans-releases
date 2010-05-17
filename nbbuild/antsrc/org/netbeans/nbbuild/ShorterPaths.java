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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * Replace paths prefixes with variables. 
 * Designed for netbeans.dest dir and test.dist.dir variables 
 */
public class ShorterPaths extends Task {
    
   
    /** dir is prefix and name is name of varaible
     */
    public static class Replacement {
        String name; 
        File dir;
        
        public void setName(String name) {
            this.name = name;
        }
        public void setDir(File dir) {
            this.dir = dir;
        }

        @Override
        public String toString() {
            return dir + " => ${" + name + "}";
        }
    }
    private List<Replacement> replacements = new LinkedList<Replacement>(); // List<Nestme>
    public Replacement createReplacement() {
        Replacement r = new Replacement();
        replacements.add(r);
        return r;
    }
    // Or:
    public void addReplacement(Replacement r) {
        replacements.add(r);
    }
    // <shorterpaths in="inputpropname" out="outpropNames">
    //     <replacement name="property_name" dir="directory"/>
    // </shorterpaths>
    
     
    private Path in;
    public void setIn(Path p) {
        if (in == null) {
            in = p.createPath();
        }   
        in.append(p);
    }
    public Path createIn () {
        if (in == null) {
            in = new Path(getProject());
        }
        return in;
    }
    public void setinRef(Reference r) {
        createIn().setRefid(r);
    }
    // <customtask path="foo:bar"/>
    // <customtask>
    //     <path>
    //         <pathelement location="foo"/>
    //     </path>
    // </customtask>
    // Etc.

    String out;
    public void setOut(String out) {
        this.out = out;
    }
    
    String extraLibs;
    public void setExtraLibs(String extraLibs) {
        this.extraLibs = extraLibs;    
    }
    File extraLibsDir;
    public void setExtraLibsDir(File extraLibsDir) {
        this.extraLibsDir = extraLibsDir;
    }
    
    File testProperties;
    public void setTestProperties(File testProperties) {
        this.testProperties = testProperties;
    } 

    @Override
    public void execute() throws BuildException {
        // TODO code here what the task actually does:
        String paths[] = in.list();
        StringBuffer nbLibBuff = new StringBuffer() ;
//        Path nbLibPath = new Path(getProject());
        StringBuffer externalLibBuf = new StringBuffer();
        try {
            for (int i = 0 ; i < paths.length ; i++) {
                String path = paths[i];
                File file = new File(path);
                // check if file exists
                if (file.exists()) {
                    // add it on classpath
                    path = file.getCanonicalPath();
                    simplyPath(path, externalLibBuf, nbLibBuff);
                } else {
                    log("Path element "+ file + " doesn't exist.",Project.MSG_VERBOSE);
                }
            } 
            if (out != null) {
                define(out, nbLibBuff.toString()); 
            }
            if (this.extraLibs != null) {
                define(extraLibs,externalLibBuf.toString());
            }

            if (testProperties != null) {
                // create properties file
                PrintWriter pw = new PrintWriter(testProperties);
                
                // copy extra unit.test.properties
                Hashtable properties = getProject().getProperties();  
                StringBuffer outProp = new StringBuffer();
                for (Iterator it = properties.keySet().iterator(); it.hasNext();) {
                    String name = (String) it.next();
                    if (name.matches("test-(unit|qa-functional)-sys-prop\\..+")) {
                        if (name.equals("test-unit-sys-prop.xtest.data")) {
                            // ignore overring xtest.data.dir, data.zip placed to standard location
                            continue;
                        }
                       //  
                       outProp.setLength(0);
                       StringTokenizer tokenizer = new StringTokenizer(properties.get(name).toString(), ":;");
                       String nextToken = null;
                       while (nextToken != null || tokenizer.hasMoreTokens()) {
                           String token = nextToken ;
                           nextToken = null;
                           if (token == null) {
                               token = tokenizer.nextToken();
                           }
                           if (tokenizer.hasMoreTokens()) {
                               nextToken = tokenizer.nextToken();
                           }
                           // check if <disk drive>:\path is property"
                           String path = token + ":" + nextToken;
                           if (new File(path).exists()) {
                               nextToken = null;
                           } else {
                               path = token;
                           }

                           simplyPath(path,externalLibBuf,outProp);
                       }
                       pw.println(name.replaceFirst("^test-(unit|qa-functional)-sys-prop\\.", "test-sys-prop.") + "=" + outProp);
                    } else if (name.startsWith("test.config")) {
                        pw.println(name + "=" + properties.get(name));
                    }
                }
                pw.println("extra.test.libs=" + externalLibBuf.toString());
                pw.println("test.run.cp=" + nbLibBuff.toString());
                pw.close();
            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        } 
    }

    private void simplyPath(String path, final StringBuffer externalLibBuf, final StringBuffer nbLibBuff) throws IOException {
        boolean bAppend = false;
        File file = new File(path);
        if (file.exists()) {
            // file exists, try to to replace the path with ${a.prop}/relpath
            //
           path = file.getAbsolutePath();
           for (Replacement repl: replacements) {
                String dirCan = repl.dir.getCanonicalPath();
                if (path.startsWith(dirCan)) {
                    if (nbLibBuff.length() > 0 ) {
                        nbLibBuff.append(":\\\n");
                    }  

                    nbLibBuff.append("${" + repl.name + "}");
                    // postfix + unify file separators to '/'
                    nbLibBuff.append(path.substring(dirCan.length()).replace(File.separatorChar,'/'));
                    bAppend = true;
                    break;
                } 
            }
            if (!bAppend) {
                String fName = copyExtraLib(file);
                if (fName != null) {
                    if (externalLibBuf.length() > 0 ) {
                        externalLibBuf.append(":\\\n");
                    }
                   externalLibBuf.append("${extra.test.libs.dir}/" + fName);
                }
            }
           
        } else {
            if (nbLibBuff.length() > 0 ) {
                nbLibBuff.append(":\\\n");
            }  
            nbLibBuff.append(path);
        }
            
    }
    
    private void define(String prop, String val) {
        log("Setting " + prop + "=" + val, Project.MSG_VERBOSE);
        String old = getProject().getProperty(prop);
        if (old != null && !old.equals(val)) {
            getProject().log("Warning: " + prop + " was already set to " + old, Project.MSG_WARN);
        }
        getProject().setNewProperty(prop, val);
    }

    private String copyExtraLib(File file) throws IOException{
        String name = null;
        if (this.extraLibsDir != null && extraLibsDir.isDirectory() && file.isFile()) {
            log("Copying " + file + " to extralibs despite " + replacements);
            name = file.getName();
            byte buff[] = new byte[100000];
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(new File (extraLibsDir,name));
            int size = 0;
            while ((size = fis.read(buff)) > 0 ) {
                fos.write(buff,0,size);
            }
            fos.close();
            fis.close();
        }
        return name;
    }
 
    
}

    
