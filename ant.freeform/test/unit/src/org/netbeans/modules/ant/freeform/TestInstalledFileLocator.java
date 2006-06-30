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
