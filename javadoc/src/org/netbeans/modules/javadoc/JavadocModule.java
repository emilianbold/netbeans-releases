/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;

/**
 * Class for initializing Javadoc module on IDE startup.
 * @author Petr Hrebejk
 */
public final class JavadocModule extends ModuleInstall {

    private static Collection/*<TopComponent>*/ floatingTopComponents;

    public synchronized static void registerTopComponent(TopComponent tc) {
        if (floatingTopComponents == null)
            floatingTopComponents = new LinkedList();
        floatingTopComponents.add(tc);
    }
    
    public synchronized static void unregisterTopComponent(TopComponent tc) {
        if (floatingTopComponents == null)
            return;
        floatingTopComponents.remove(tc);
    }
    
    public void uninstalled() {
        Collection c;
        synchronized (JavadocModule.class) {
            if (floatingTopComponents != null) {
                c = new ArrayList(floatingTopComponents);
            } else {
                c = Collections.EMPTY_SET;
            }
        }
        for (Iterator it = c.iterator(); it.hasNext();) {
            TopComponent tc = (TopComponent)it.next();
            tc.close();
        }
    }
}
