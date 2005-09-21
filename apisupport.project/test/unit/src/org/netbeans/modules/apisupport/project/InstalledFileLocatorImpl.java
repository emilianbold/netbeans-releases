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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class InstalledFileLocatorImpl extends org.openide.modules.InstalledFileLocator {
    private static File installDir;
        
        
    /**
     * Creates a new instance of InstalledFileLocatorImpl
     */
    public InstalledFileLocatorImpl() {
    }
    

    public File locate(String relativePath, String codeNameBase, boolean localized) {
        String user = System.getProperty("netbeans.user");
        File f = new File(user + File.separator + relativePath);
        if (f.exists()) {
            return f;
        }
        
        File root = installDir;
        if (root == null) {
            return null;
        }
        
        File[] arr = installDir.listFiles();
        for (int i = 0; i < arr.length; i++) {
            f = new File(arr[i], relativePath);
            if (f.exists()) {
                return f;
            }
        }
        
        return null;
    }

    public static void registerDestDir(File file) {
        installDir = file;
    }
    
}
