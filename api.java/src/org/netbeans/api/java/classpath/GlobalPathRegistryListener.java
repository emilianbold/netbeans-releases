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
package org.netbeans.api.java.classpath;

import java.util.EventListener;

/**
 * Event listener interface for being notified of changes in the set of
 * available paths.
 */
public interface GlobalPathRegistryListener extends EventListener {

    /**
     * Called when some paths are added.
     * Only applies to the first copy of a path that is added.
     * @param event an event giving details
     */
    public void pathsAdded(GlobalPathRegistryEvent event);

    /**
     * Called when some paths are removed.
     * Only applies to the last copy of a path that is removed.
     * @param event an event giving details
     */
    public void pathsRemoved(GlobalPathRegistryEvent event);

}
