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

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.InterruptedException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * Rather dummy implementation of the Java Platform, but sufficient for communication
 * inside the Wizard.
 * Made public to allow ide/projectimport to reuse it
 */
public final class NewJ2SEPlatform extends J2SEPlatformImpl implements Runnable {

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
                m.put(k, p.getProperty(k));
            }   
            this.setSystemProperties(m);
            this.valid = true;
            is.close();
            f.delete();
        } catch (IOException ex) {
            this.valid = false;
        }
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








