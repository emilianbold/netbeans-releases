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

package org.netbeans.modules.javadoc;

import java.awt.Image;
import java.beans.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;

/** Class for initializing Javadoc module on IDE startup.

 @author Petr Hrebejk
*/
public final class JavadocModule extends ModuleInstall {

    /** serialVersionUID */
    private static final long serialVersionUID = 984124010415492146L;
    
    private static Collection floatingTopComponents;

    protected Object writeReplace(){
        return null;
    }
    
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
