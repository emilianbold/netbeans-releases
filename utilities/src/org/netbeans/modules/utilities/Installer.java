/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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

}
