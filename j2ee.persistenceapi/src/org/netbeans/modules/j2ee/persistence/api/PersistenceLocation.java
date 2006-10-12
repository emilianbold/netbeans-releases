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

package org.netbeans.modules.j2ee.persistence.api;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceLocationProvider;
import org.openide.filesystems.FileObject;

/**
 * This class allows retrieving the default persistence location in a project
 * or creating this location if it doesn't exist.
 *
 * @author Andrei Badea
 */
public final class PersistenceLocation {

    private PersistenceLocation() {
    }

    /**
     * Returns the default persistence location in the given project.
     *
     * @param  project the project.
     * @return the persistence location or null if the project does not have
     *         a persistence location or the location does not exist.
     */
    public static FileObject getLocation(Project project) {
        PersistenceLocationProvider provider = (PersistenceLocationProvider)project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.getLocation();
        }
        return null;
    }

    /**
     * Creates the default persistence location in the given project.
     *
     * @param  project the project.
     * @return the persistence location; never null.
     * @throws IOException if the persistence location could not be created
     *         or the project did not have an implementation of 
     *         PersistenceLocationProvider in its lookup.
     */
    public static FileObject createLocation(Project project) throws IOException {
        PersistenceLocationProvider provider = (PersistenceLocationProvider)project.getLookup().lookup(PersistenceLocationProvider.class);
        if (provider != null) {
            return provider.createLocation();
        }
        throw new IOException("The project " + project + " does not have an implementation of PersistenceLocationProvider in its lookup"); // NOI18N
    }
}
