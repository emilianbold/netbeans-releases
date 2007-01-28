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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.project.jsf.services;

import org.netbeans.api.project.Project;
import org.openide.util.Lookup;


/**
 * <p>
 * This interface provides a service to refresh web pages in the
 * project when a new theme is chosen.
 *
 * @author Tor Norbye
 */
public abstract class RefreshService {
    private static RefreshService service; // TODO Use weak reference?

    /** Creates a new instance of RefreshService */
    protected RefreshService() {
    }

    /**
     * Refresh the pages in the given project such that new themes take effect
     * @param project The project which should be refreshed
     */
    public abstract void refresh(Project project);

    /** Obtain a default instance of the RefreshService */
    public static RefreshService getDefault() {
        // The service has no state so doesn't need to be a singleton. Therefore,
        // I won't bother with synchronization since getDefault may be called a lot.
        if (service == null) {
            // Add the import items to the menu
            Lookup l = Lookup.getDefault();
            service = (RefreshService)l.lookup(RefreshService.class);
        }

        return service;
    }
}
