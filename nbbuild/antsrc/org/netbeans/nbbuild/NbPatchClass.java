/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
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
// IMPORTANT! You may need to mount ant.jar before this class will
// compile. So mount the JAR modules/ext/ant-1.4.1.jar (NOT modules/ant.jar)
// from your IDE installation directory in your Filesystems before
// continuing to ensure that it is in your classpath.

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Jaroslav Tulach
 */
public class NbPatchClass extends Task {

    /* Path to library containing the patch method */
    private Path patchPath;
    public Path createClasspath() {
        if (patchPath == null) {
            patchPath = new Path(getProject());
        }
        return patchPath.createPath();
    }
    
    
    /* Name of class with patch method */
    private String patchClass = "org.netbeans.PatchByteCode";
    public void setPatchClass (String f) {
        patchClass = f;
    }
    
    /** Name of the method to call. Must have byte[] array argument and return the same
     */
    private String patchMethod = "patch";
    public void setPatchMethod (String f) {
        patchMethod = f;
    }
    
    /** Source JAR to extract.
     */
    private File sourceJar;
    public void setSource (File f) {
        sourceJar = f;
    }
    
    /* Base dir to find classes relative to */
    private File targetdir;
    public void setTargetdir (File f) {
        targetdir = f;
    }
    
    public void execute() throws BuildException {
        if (targetdir == null) {
            throw new BuildException ("Attribute targetdir must be specified");
        }

        if (sourceJar == null) {
            throw new BuildException ("Attribute source must be specified");
        }
        
        
        JarFile jar;
        
        try {
            jar = new JarFile (sourceJar);
        } catch (IOException ex) {
            throw new BuildException ("Problem initializing file " + sourceJar, ex);
        }
        
        //
        // Initialize the method
        //
        
        log ("Initializing patching " + patchClass + '.' + patchMethod);
        
        ClassLoader cl = new AntClassLoader(getProject(), patchPath);
        
        java.lang.reflect.Method m;
        try {
            Class c = cl.loadClass (patchClass);
            m = c.getMethod(patchMethod, new Class[] { byte[].class, String.class });
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
        //
        
        java.util.Enumeration it = jar.entries();
        while (it.hasMoreElements()) {
            java.util.jar.JarEntry e = (java.util.jar.JarEntry)it.nextElement ();
            String entryname = e.getName();
            if (!entryname.endsWith(".class")) {
                // resource, skip
                continue;
            }
            String name = entryname.substring(0, entryname.length() - 6).replace('/', '.');

            int size = (int)e.getSize();
            if (size <= 4) {
                // not interesting entry
                continue;
            }
            
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
            
            byte[] original = (byte[])arr.clone ();
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
                continue;
            }

            File f = new File (targetdir, e.getName ().replace ('/', File.separatorChar));
            if (f.exists () && f.lastModified() > sourceJar.lastModified ()) {
                // if the file is newer
                continue;
            }
            
            f.getParentFile().mkdirs();
            
            log ("Writing patched file " + f);
            
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
