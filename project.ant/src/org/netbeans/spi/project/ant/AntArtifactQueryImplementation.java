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

package org.netbeans.spi.project.ant;

import java.io.File;
import org.netbeans.api.project.ant.AntArtifact;

/**
 * Represents knowledge about the origin of an Ant build product.
 * <p>
 * Normal code does not need to implement this query. A standard implementation
 * first finds an associated {@link org.netbeans.api.project.Project} and
 * then checks to see if it supports {@link org.netbeans.spi.project.ant.AntArtifactProvider}.
 * You would only need to implement this directly in case your project type
 * generated build artifacts outside of the project directory or otherwise not marked
 * as "owned" by you according to {@link org.netbeans.api.project.FileOwnerQuery}.
 * @see org.netbeans.api.project.ant.AntArtifactQuery
 * @author Jesse Glick
 */
public interface AntArtifactQueryImplementation {

    /**
     * Find an Ant build artifact corresponding to a given file.
     * @param file a file on disk (need not currently exist) which might be a build artifact from an Ant script
     * @return an artifact information object, or null if it is not recognized
     */
    AntArtifact findArtifact(File file);
    
}
