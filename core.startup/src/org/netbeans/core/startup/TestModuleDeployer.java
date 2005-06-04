/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;

/** Interface for deploying test modules.
 * Find an instance via lookup and deploy away.
 * @author Jesse Glick
 * @since org.netbeans.core/1 1.1
 */
public final class TestModuleDeployer {
    /** Deploy a module in test mode.
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
        org.netbeans.core.startup.Main.getModuleSystem ().deployTestModule(jar);
    }
    
}
