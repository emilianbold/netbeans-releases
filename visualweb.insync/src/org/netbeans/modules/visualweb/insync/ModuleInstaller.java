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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

// XXX Do not depend on core internals!
//import org.netbeans.core.NbTopManager;
//import org.netbeans.core.modules.Module;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;

/**
 * Create a monitor singleton for the ModelSet types for insync.
 */
public class ModuleInstaller extends ModuleInstall {

    /**
     *
     */
    private static final long serialVersionUID = 3977301014502125881L;

    /*
     * Install the monitor when the insync NB module is loaded. Also let JavaBeans know we are a
     * design-time GUI environment.
     * @see org.openide.modules.ModuleInstall#restored()
     */
    public void restored() {
        java.beans.Beans.setDesignTime(true);
        java.beans.Beans.setGuiAvailable(true);
        System.setProperty("rave.version", "2.0.0"); // NOI18N
        // TODO -- automatically derive from the build somehow:
        System.setProperty("rave.build", "thresher-fcs"); // NOI18N
    }

    /*
     * @see org.openide.modules.ModuleInstall#uninstalled()
     */
    public void uninstalled() {
    }

}
