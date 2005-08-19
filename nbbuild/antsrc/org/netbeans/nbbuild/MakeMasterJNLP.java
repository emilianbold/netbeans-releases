/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;

/** Generates JNLP files for signed versions of the module JAR files.
 *
 * @author Jaroslav Tulach
 */
public class MakeMasterJNLP extends Task {
    /** the files to work on */
    private org.apache.tools.ant.types.FileSet files;
    
    public org.apache.tools.ant.types.FileSet createModules() 
    throws BuildException {
        if (files != null) throw new BuildException("modules can be created just once");
        files = new org.apache.tools.ant.types.FileSet();
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
        DirectoryScanner scan = files.getDirectoryScanner(getProject());
        String[] arr = scan.getIncludedFiles();
        for (int i = 0; i < arr.length; i++) {
            File jar = new File (files.getDir(getProject()), arr[i]);
            
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
