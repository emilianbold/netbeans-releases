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

package org.openide.loaders;


import org.openide.filesystems.FileObject;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.event.ChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.util.Lookup;

/** Utility to add a loader to the loader pool.
 * Should only be used in external execution mode!
 * Requires core.jar in classpath.
 * @author Jesse Glick
 */
public final class AddLoaderManuallyHid {

    private AddLoaderManuallyHid() {}

    /** Add a loader to the pool (to the front of the free area). */
    public static void addRemoveLoader(DataLoader l, boolean add) throws Exception {
        // Initialize IDE:
//        TopManager.getDefault();
        
        // Now add the loader. Would be easy enough if we could directly access
        // core classes, but then this test would have to be compiled with core.jar
        // in the classpath...
        Class lpnClazz = Class.forName("org.netbeans.core.LoaderPoolNode");
        Field loadersF = lpnClazz.getDeclaredField("loaders");
        loadersF.setAccessible(true);
        List loaders = (List)loadersF.get(null);
        if (add) {
            if (loaders.contains(l)) throw new IllegalArgumentException();
            loaders.add(0, l);
        } else {
            if (! loaders.contains(l)) throw new IllegalArgumentException();
            loaders.remove(l);
        }
        
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        if (add) {
            l.addPropertyChangeListener((PropertyChangeListener)pool);
        } else {
            l.removePropertyChangeListener((PropertyChangeListener)pool);
        }
        // Simulate behavior of update(), but fire pool change immediately:
        Field loadersArrayF = lpnClazz.getDeclaredField("loadersArray");
        loadersArrayF.setAccessible(true);
        loadersArrayF.set(null, null);
        pool.fireChangeEvent(new ChangeEvent(pool));
    }
    
}
