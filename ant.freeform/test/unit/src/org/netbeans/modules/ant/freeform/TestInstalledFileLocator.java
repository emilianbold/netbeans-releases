/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ant.freeform;

import java.io.File;
import org.openide.modules.InstalledFileLocator;

/**
 * Points to ant.jar for unit tests.
 * @author Jesse Glick
 */
public final class TestInstalledFileLocator extends InstalledFileLocator {
    
    private final File antHome;
    
    /** Default instance for lookup. */
    public TestInstalledFileLocator() {
        String anthome = System.getProperty("test.ant.home");
        assert anthome != null : "Must set system property test.ant.home";
        antHome = new File(anthome);
        assert antHome.isDirectory() : "No such dir " + antHome;
    }

    public File locate(String relativePath, String codeNameBase, boolean localized) {
        // Simulate effect of having an Ant-task-providing module in user dir:
        if (relativePath.equals("ant")) {
            return new File("/my/user/dir/ant");
        } else if (relativePath.equals("ant/nblib/bridge.jar") || relativePath.equals("ant/lib/ant.jar")) {
            File f = new File(antHome, relativePath.substring(4).replace('/', File.separatorChar));
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }
    
}
