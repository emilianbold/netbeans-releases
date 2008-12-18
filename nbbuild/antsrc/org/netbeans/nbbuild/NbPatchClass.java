/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.JarFile;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.DirectoryScanner;
import java.util.Vector;

/**
 * @author Jaroslav Tulach
 */
public class NbPatchClass extends MatchingTask {

    /* Path to library containing the patch method */
    private Path patchPath;
    public Path createClasspath() {
        if (patchPath == null) {
            patchPath = new Path(getProject());
        }
        return patchPath.createPath();
    }
    
    
    /* Name of class with patch method */
    private String patchClass = "org.netbeans.PatchByteCode"; //NOI18N
    public void setPatchClass (String f) {
        patchClass = f;
    }
    
    /** Name of the method to call. Must have byte[] array argument and return the same
     */
    private String patchMethod = "patch"; //NOI18N
    public void setPatchMethod (String f) {
        patchMethod = f;
    }
    
    /** Source JAR to extract.
     */
    public void setSource (File f) {
        if (f.exists()) {
            log ("Adding source file " + f.getAbsolutePath(), Project.MSG_VERBOSE);
            FileSet xfs = new FileSet();
            xfs.setDir(f.getParentFile());
            log("Setting FileSet's dir to \"" + f.getParentFile().getAbsolutePath() + "\"", Project.MSG_DEBUG );
            xfs.setIncludes(f.getName());
            log("Setting FileSet's include to \"" + f.getName() + "\"",Project.MSG_DEBUG );
            DirectoryScanner ds = xfs.getDirectoryScanner(getProject());
            String[] files = ds.getIncludedFiles();
            if (files.length < 1) {
                log ("FileSet is empty, source doesn't doesn't exist (" + f.getParentFile().getAbsolutePath() + ")", Project.MSG_VERBOSE);
            } else {
                log ("Adding FileSet with "  + files.length + " file(s)", Project.MSG_VERBOSE);
                addFileset(xfs);
            }
        }
    }
    
    /* Base dir to find classes relative to */
    private File targetdir;
    public void setTargetdir (File f) {
        targetdir = f;
    }
    
    /**
     * Adds a set of files (nested fileset attribute).
     */
    private Vector<FileSet> filesets = new Vector<FileSet>();
    public void addFileset(FileSet set) {
        log ("Adding new FileSet", Project.MSG_DEBUG);
        filesets.addElement(set);
    }
 
    public void execute() throws BuildException {
        if (targetdir == null) {
            throw new BuildException ("Attribute targetdir must be specified");
        }

        boolean fs_empty = true;
        for (int i=0; i<filesets.size() && fs_empty; i++) {
            FileSet n = filesets.elementAt(i);
            if ( n != null ) {
                DirectoryScanner ds = n.getDirectoryScanner(getProject());
                String[] files = ds.getIncludedFiles();
                File bdir = ds.getBasedir();
                if (files.length < 1) log ("FileSet is empty, doesn't have included files", Project.MSG_VERBOSE);
                for (int k=0; k < files.length && fs_empty; k++) {
                    File n_file = new File(bdir, files[k]);
                    if (n_file.exists()) fs_empty = false;
                }
            }
        }
                    
        if (fs_empty) {
            throw new BuildException ("Attribute \"source\" or fileset includes must be specified");
        }
        
        //
        // Initialize the method
        //
        
        log ("Initializing patching " + patchClass + '.' + patchMethod);
        
        ClassLoader cl = new AntClassLoader(getProject(), patchPath);
        
        java.lang.reflect.Method m;
        try {
            Class<?> c = cl.loadClass(patchClass);
            m = c.getMethod(patchMethod, byte[].class, String.class);
            if (m.getReturnType() != byte[].class) {
                throw new BuildException ("Method does not return byte[]: " + m);
            }
        } catch (Exception ex) {
            throw new BuildException ("Cannot initialize class " + patchClass + " and method " + patchMethod, ex);
        }
        
        /*
        try {
            log ("Testing method " + m);
            byte[] res = (byte[])m.invoke (null, new Object[] { new byte[0], "someString" });
        } catch (Exception ex) {
            throw new BuildException ("Exception during test invocation of the method", ex);
        }
        */
                        
        //
        // Ok we have the method and we can do the patching
        // go over fileset includes

        for (FileSet n : filesets) {
            if ( n != null ) {
                DirectoryScanner ds = n.getDirectoryScanner(getProject());
                String[] files = ds.getIncludedFiles();
                File bdir = ds.getBasedir();
                for (int k=0; k <files.length; k++) {
                    File n_file = new File(bdir, files[k]);
                    JarFile jar;
                    log("Checking classes in jarfile " + n_file, Project.MSG_VERBOSE);

                    try {
                        jar = new JarFile (n_file);
                    } catch (IOException ex) {
                        throw new BuildException ("Problem initializing file " + n_file, ex);
                    }
                    
                    // do the patching
                    java.util.Enumeration it = jar.entries();
                    while (it.hasMoreElements()) {
                        java.util.jar.JarEntry e = (java.util.jar.JarEntry)it.nextElement ();
                        String entryname = e.getName();
                        if (!entryname.endsWith(".class")) { //NOI18N
                            // resource, skip
                            log("Skipping record (probably directory or resource) " + entryname, Project.MSG_DEBUG);
                            continue;
                        }
                        String name = entryname.substring(0, entryname.length() - 6).replace('/', '.');
            
                        int size = (int)e.getSize();
                        if (size <= 4) {
                            // not interesting entry
                            log("Class " + name + " is not an interesting entry (<5 bytes)", Project.MSG_DEBUG);
                            continue;
                        }

                        log("Checking class " + name, Project.MSG_DEBUG);
                        
                        byte[] arr = new byte[size];
                        
                        try {
                            java.io.InputStream is = jar.getInputStream(e);
                            
                            int indx = 0;
                            while (indx < arr.length) {
                                int read = is.read (arr, indx, arr.length - indx);
                                if (read == -1) {
                                    throw new BuildException("Entry: " + name + " size should be: " + size + " but was read just: " + indx);
                                }
                                indx += read;
                            }
                        } catch (IOException ex) {
                            throw new BuildException (ex);
                        }
                        
                        byte[] original = arr.clone();
                        byte[] out;
                        try {
                            out = (byte[])m.invoke (null, new Object[] { arr, name });
                        } catch (java.lang.reflect.InvocationTargetException ex) {
                            throw new BuildException (ex.getTargetException());
                        } catch (Exception ex) {
                            throw new BuildException (ex);
                        }
                        
                        if (java.util.Arrays.equals (original, out)) {
                            // no patching, go on
                            log("Not patching class " + name, Project.MSG_DEBUG);
                            continue;
                        }
            
                        File f = new File (targetdir, e.getName ().replace ('/', File.separatorChar));
                        if (f.exists () && f.lastModified() > n_file.lastModified ()) {
                            // if the file is newer
                            log("Patched class " + name + " in " + targetdir.getAbsolutePath() + " is newer than jarfile of origin, not saving patched bytestream to file " + f.getAbsolutePath() , Project.MSG_VERBOSE);
                            continue;
                        }
                        
                        f.getParentFile().mkdirs();
                        
                        log ("Writing patched file " + f, Project.MSG_INFO);
                        //log ("Writing patched file " + f + " (jarfile of origin " + n_file.getName() + ")", Project.MSG_VERBOSE);
                        
                        try {
                            FileOutputStream os = new FileOutputStream (f);
                            os.write (out);
                            os.close ();
                        } catch (IOException ex) {
                            throw new BuildException ("Cannot write file " + f, ex);
                        }
                    }
  	        }
 	    }
        }
    }
    
}
