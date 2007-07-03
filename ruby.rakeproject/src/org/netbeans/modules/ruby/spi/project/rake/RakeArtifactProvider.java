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

import org.netbeans.modules.ruby.api.project.rake.RakeArtifact;

/**
 * Interface to be implemented by projects which can supply a list
 * of Ant build artifacts.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public interface RakeArtifactProvider {

    /**
     * Get a list of supported build artifacts.
     * Typically the entries would be created using
     * {@link org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper#createSimpleRakeArtifact}.
     * @return a list of build artifacts produced by this project;
     *         the target names must be distinct, and if this provider is in a
     *         project's lookup, {@link RakeArtifact#getProject} must return the
     *         same project; list of artifacts for one project cannot contain
     *         two artifacts with the same {@link RakeArtifact#getID ID}
     */
    RakeArtifact[] getBuildArtifacts();
    
}
