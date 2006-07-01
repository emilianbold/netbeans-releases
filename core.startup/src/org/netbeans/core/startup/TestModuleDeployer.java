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

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import org.openide.filesystems.FileUtil;

/**
 * Interface for deploying test modules.
 * @author Jesse Glick
 * @since org.netbeans.core/1 1.1
 */
public final class TestModuleDeployer {

    /**
     * Deploy a module in test mode.
     * You pass the JAR file.
     * Module system figures out the rest (i.e. whether it needs
     * to be installed, reinstalled, etc.).
     * The deployment is run synchronously so do not call this
     * method from a sensitive thread (e.g. event queue), nor
     * call it with any locks held. Best to call it e.g. from the
     * execution engine.
     * @param jar the path to the module JAR
     * @throws IOException if there is some error in the process
     */
    public static void deployTestModule(File jar) throws IOException {
        Main.getModuleSystem().deployTestModule(FileUtil.normalizeFile(jar));
    }
    
}
