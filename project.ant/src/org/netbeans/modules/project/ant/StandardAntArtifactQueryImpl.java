/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ant;

import java.io.File;
import java.net.URI;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntArtifactQueryImplementation;

/**
 * Standard implementation of {@link AntArtifactQueryImplementation} which uses
 * {@link AntArtifactProvider}.
 * @author Jesse Glick
 */
public class StandardAntArtifactQueryImpl implements AntArtifactQueryImplementation {
    
    /** Default constructor for lookup. */
    public StandardAntArtifactQueryImpl() {}
    
    public AntArtifact findArtifact(File file) {
        Project p = FileOwnerQuery.getOwner(file.toURI());
        if (p == null) {
            return null;
        }
        AntArtifactProvider prov = (AntArtifactProvider)p.getLookup().lookup(AntArtifactProvider.class);
        if (prov == null) {
            return null;
        }
        AntArtifact[] artifacts = prov.getBuildArtifacts();
        for (int i = 0; i < artifacts.length; i++) {
            URI uris[] = artifacts[i].getArtifactLocations();
            for (int y = 0; y < uris.length; y++) {
                File testFile = new File(artifacts[i].getScriptLocation().toURI().resolve(uris[y]));
                if (file.equals(testFile)) {
                    return artifacts[i];
                }
            }
        }
        return null;
    }
    
}
