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

package org.netbeans.modules.ruby.spi.project.rake;

import java.io.File;
import org.netbeans.modules.ruby.api.project.rake.RakeArtifact;

/**
 * Represents knowledge about the origin of an Ant build product.
 * <p>
 * Normal code does not need to implement this query. A standard implementation
 * first finds an associated {@link org.netbeans.api.project.Project} and
 * then checks to see if it supports {@link org.netbeans.modules.ruby.spi.project.rake.RakeArtifactProvider}.
 * You would only need to implement this directly in case your project type
 * generated build artifacts outside of the project directory or otherwise not marked
 * as "owned" by you according to {@link org.netbeans.api.project.FileOwnerQuery}.
 * @see org.netbeans.modules.ruby.api.project.rake.RakeArtifactQuery
 * @author Jesse Glick
 */
public interface RakeArtifactQueryImplementation {

    /**
     * Find an Ant build artifact corresponding to a given file.
     * @param file a file on disk (need not currently exist) which might be a build artifact from an Ant script
     * @return an artifact information object, or null if it is not recognized
     */
    RakeArtifact findArtifact(File file);
    
}
