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
