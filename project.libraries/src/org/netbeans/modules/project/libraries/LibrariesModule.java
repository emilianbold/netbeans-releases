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
package org.netbeans.modules.project.libraries;

import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;

import java.util.Iterator;


public class LibrariesModule extends ModuleInstall {

    public void restored() {
        super.restored();
        Lookup.Result result = Lookup.getDefault().lookup(new Lookup.Template(LibraryProvider.class));
        for (Iterator it = result.allInstances().iterator(); it.hasNext();) {
            it.next();
        }
    }
}
