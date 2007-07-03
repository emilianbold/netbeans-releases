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

package org.netbeans.modules.ruby.modules.project.rake;

import java.io.File;
import java.net.URI;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.api.project.rake.RakeArtifact;
import org.netbeans.modules.ruby.spi.project.rake.RakeArtifactProvider;
import org.netbeans.modules.ruby.spi.project.rake.RakeArtifactQueryImplementation;

/**
 * Standard implementation of {@link RakeArtifactQueryImplementation} which uses
 * {@link RakeArtifactProvider}.
 * @author Jesse Glick
 */
public class StandardRakeArtifactQueryImpl implements RakeArtifactQueryImplementation {
    
    /** Default constructor for lookup. */
    public StandardRakeArtifactQueryImpl() {}
    
    public RakeArtifact findArtifact(File file) {
        Project p = FileOwnerQuery.getOwner(file.toURI());
        if (p == null) {
            return null;
        }
        RakeArtifactProvider prov = p.getLookup().lookup(RakeArtifactProvider.class);
        if (prov == null) {
            return null;
        }
        RakeArtifact[] artifacts = prov.getBuildArtifacts();
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
