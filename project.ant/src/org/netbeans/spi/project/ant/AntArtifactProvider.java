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

import org.netbeans.api.project.ant.AntArtifact;

/**
 * Interface to be implemented by projects which can supply a list
 * of Ant build artifacts.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public interface AntArtifactProvider {

    /**
     * Get a list of supported build artifacts.
     * Typically the entries would be created using
     * {@link org.netbeans.spi.project.support.ant.AntProjectHelper#createSimpleAntArtifact}.
     * @return a list of build artifacts produced by this project;
     *         the target names must be distinct, and if this provider is in a
     *         project's lookup, {@link AntArtifact#getProject} must return the
     *         same project
     */
    AntArtifact[] getBuildArtifacts();
    
}
