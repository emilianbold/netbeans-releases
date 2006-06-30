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

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.InterruptedException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;

/**
 * Rather dummy implementation of the Java Platform, but sufficient for communication
 * inside the Wizard.
 * Made public to allow ide/projectimport to reuse it
 */
public final class NewJ2SEPlatform extends J2SEPlatformImpl implements Runnable {
    
    private static Set propertiesToFix = new HashSet ();
    
    //Properties used by IDE which should be fixed not to use resolved symlink
    static {
        propertiesToFix.add ("sun.boot.class.path");    //NOI18N
        propertiesToFix.add ("sun.boot.library.path");  //NOI18N
        propertiesToFix.add ("java.library.path");      //NOI18N
        propertiesToFix.add ("java.ext.dirs");          //NOI18N
        propertiesToFix.add ("java.home");              //NOI18N       
    }
    
    private boolean valid;

    public static NewJ2SEPlatform create (FileObject installFolder) throws IOException {
        assert installFolder != null;
        Map platformProperties = new HashMap ();
        return new NewJ2SEPlatform (null,Collections.singletonList(installFolder.getURL()),platformProperties,Collections.EMPTY_MAP);
    }

    private NewJ2SEPlatform (String name, List installFolders, Map platformProperties, Map systemProperties) {
        super(name, name, installFolders, platformProperties, systemProperties,null,null);
    }

    public boolean isValid () {
        return this.valid;
    }

    /**
     * Actually performs the detection and stores relevant information
     * in this Iterator
     */
    public void run() {
        try {
            FileObject java = Util.findTool("java", this.getInstallFolders());
            if (java == null)
                return;
            File javaFile = FileUtil.toFile (java);
            if (javaFile == null)
                return;
            String javapath = javaFile.getAbsolutePath();
            String filePath = File.createTempFile("nb-platformdetect", "properties").getAbsolutePath();
            getSDKProperties(javapath, filePath);
            File f = new File(filePath);
            Properties p = new Properties();
            InputStream is = new FileInputStream(f);
            p.load(is);
            Map m = new HashMap(p.size());
            for (Enumeration en = p.keys(); en.hasMoreElements(); ) {
                String k = (String)en.nextElement();
                String v = (String) p.getProperty(k);
                v = fixSymLinks (k,v);
                m.put(k, v);
            }   
            this.setSystemProperties(m);
            this.valid = true;
            is.close();
            f.delete();
        } catch (IOException ex) {
            this.valid = false;
        }
    }
    
    
    /**
     * Fixes sun.boot.class.path property if it contains resolved
     * symbolic link. On Suse the jdk is symlinked and during update
     * the link is changed
     *
     */
    private String fixSymLinks (String key, String value) {
        if (Utilities.isUnix() && propertiesToFix.contains (key)) {
            try {
                String[] pathElements = value.split(File.pathSeparator);
                boolean changed = false;
                for (Iterator it = this.getInstallFolders().iterator(); it.hasNext();) {
                    File f = FileUtil.toFile ((FileObject) it.next());
                    if (f != null) {
                        String path = f.getAbsolutePath();
                        String canonicalPath = f.getCanonicalPath();
                        if (!path.equals(canonicalPath)) {
                            for (int i=0; i<pathElements.length; i++) {
                                if (pathElements[i].startsWith(canonicalPath)) {
                                    pathElements[i] = path + pathElements[i].substring(canonicalPath.length());
                                    changed = true;
                                }
                            }
                        }
                    }
                }
                if (changed) {
                    StringBuffer sb = new StringBuffer ();
                    for (int i = 0; i<pathElements.length; i++) {
                        if (i > 0) {
                            sb.append(File.pathSeparatorChar);
                        }
                        sb.append(pathElements[i]);                
                    }
                    return sb.toString();
                }
            } catch (IOException ioe) {
                //Return the original value
            }
        }
        return value;
    }


    private void getSDKProperties(String javaPath, String path) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        try {
            String[] command = new String[5];
            command[0] = javaPath;
            command[1] = "-classpath";    //NOI18N
            command[2] = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-modules-java-j2seplatform-probe.jar", "org.netbeans.modules.java.j2seplatform", false).getAbsolutePath(); // NOI18N
            command[3] = "org.netbeans.modules.java.j2seplatform.wizard.SDKProbe";
            command[4] = path;
            final Process process = runtime.exec(command);
            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0)
                throw new IOException();
        } catch (InterruptedException ex) {
            IOException e = new IOException();
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
        }
    }
}








