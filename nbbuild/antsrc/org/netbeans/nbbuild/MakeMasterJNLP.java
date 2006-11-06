/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/** Generates JNLP files for signed versions of the module JAR files.
 *
 * @author Jaroslav Tulach
 */
public class MakeMasterJNLP extends Task {
    /** the files to work on */
    private FileSet files;
    
    public FileSet createModules() 
    throws BuildException {
        if (files != null) throw new BuildException("modules can be created just once");
        files = new FileSet();
        return files;
    }
    
    private File target;
    public void setDir(File t) {
        target = t;
    }
    
    private String masterPrefix = "";
    public void setCodeBase(String p) {
        masterPrefix = p;
    }

    public void execute() throws BuildException {
        if (target == null) throw new BuildException("Output dir must be provided");
        if (files == null) throw new BuildException("modules must be provided");
        
        try {
            generateFiles();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void generateFiles() throws IOException, BuildException {
        for (String nm : files.getDirectoryScanner(getProject()).getIncludedFiles()) {
            File jar = new File (files.getDir(getProject()), nm);
            
            if (!jar.canRead()) {
                throw new BuildException("Cannot read file: " + jar);
            }
            
            JarFile theJar = new JarFile(jar);
            String codenamebase = theJar.getManifest().getMainAttributes().getValue("OpenIDE-Module");
            if (codenamebase == null) {
                throw new BuildException("Not a NetBeans Module: " + jar);
            }
            {
                int slash = codenamebase.indexOf('/');
                if (slash >= 0) {
                    codenamebase = codenamebase.substring(0, slash);
                }
            }
            String dashcnb = codenamebase.replace('.', '-');

            File n = new File(target, dashcnb + ".ref");
            FileWriter w = new FileWriter(n);
            w.write("    <extension name='" + codenamebase + "' href='" + this.masterPrefix + dashcnb + ".jnlp' />\n");
            w.close();
                
                
            theJar.close();
        }
        
    }
}
