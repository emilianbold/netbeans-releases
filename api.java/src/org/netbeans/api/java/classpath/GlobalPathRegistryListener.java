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
