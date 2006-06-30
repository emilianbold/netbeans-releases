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
package org.netbeans.modules.utilities;

import org.openide.modules.ModuleInstall;
import org.openide.util.SharedClassObject;

/** Module install class for Utilities module.
 *
 * @author Jesse Glick, Petr Kuzel, Martin Ryzl
 */
public class Installer extends ModuleInstall {

    /** Installation instance for &quot;sub-module&quot; Search.  */
    private final org.netbeans.modules.search.Installer searchInstaller;

    /** Constructs modules installer. */
    public Installer() {
        searchInstaller = (org.netbeans.modules.search.Installer)
                          SharedClassObject.findObject(
                                  org.netbeans.modules.search.Installer.class,
                                  true);
    }
    
    /**
     * Restores module. Restores &quot;sub-module&quot; Search.
     */
    public void restored() {
        searchInstaller.restored();
    }
    
    /**
     * Uninstalls module. Uninstalls
     * the Search &quot;sub-module&quot;.
     */
    public void uninstalled() {
        searchInstaller.uninstalled();
    }
    
    /**
     */
    public void close() {
        searchInstaller.close();
    }
    
    /**
     */
    public boolean closing() {
        return searchInstaller.closing();
    }

}
