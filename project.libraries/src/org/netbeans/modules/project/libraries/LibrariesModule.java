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
package org.netbeans.modules.project.libraries;

import java.util.Iterator;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.netbeans.spi.project.libraries.LibraryProvider;

/**
 * Ensures that all {@link LibraryProvider}s are actually loaded.
 * Some of them may perform initialization actions, such as updating
 * $userdir/build.properties with concrete values of some library paths.
 * This needs to happen before any Ant build is run.
 * @author Tomas Zezula
 */
public class LibrariesModule extends ModuleInstall {

    public void restored() {
        super.restored();
        Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(LibraryProvider.class));
        for (Iterator it = result.allInstances().iterator(); it.hasNext();) {
            //XXX: Workaround of lookup non reentrant issue (#49405)            
            //Library can not do an initialization in its constructor
            //For promo-E the LibraryProvider should be extended by init method
            LibraryProvider lp = (LibraryProvider) it.next();
            lp.getLibraries();
        }
    }
    
}
