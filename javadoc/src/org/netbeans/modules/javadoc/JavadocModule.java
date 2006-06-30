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
