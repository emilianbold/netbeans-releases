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

package org.netbeans.modules.ruby.api.project.rake;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.spi.project.rake.RakeArtifactProvider;
import org.netbeans.modules.ruby.spi.project.rake.RakeArtifactQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Find out how to create a certain build artifact by calling some Ant script.
 * @see RakeArtifactQueryImplementation
 * @author Jesse Glick
 */
public class RakeArtifactQuery {
    
    private RakeArtifactQuery() {}
    
    /**
     * Try to find an Ant artifact object corresponding to a given file on disk.
     * The file need not currently exist for the query to succeed.
     * All registered {@link RakeArtifactQueryImplementation} providers are asked.
     * @param file a file which might be built by some Ant target
     * @return an Ant artifact object describing it, or null if it is not recognized
     */
    public static RakeArtifact findArtifactFromFile(File file) {
        if (!file.equals(FileUtil.normalizeFile(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+FileUtil.normalizeFile(file));  // NOI18N
        }
        Iterator it = Lookup.getDefault().lookupAll(RakeArtifactQueryImplementation.class).iterator();
        while (it.hasNext()) {
            RakeArtifactQueryImplementation aaqi = (RakeArtifactQueryImplementation)it.next();
            RakeArtifact artifact = aaqi.findArtifact(file);
            if (artifact != null) {
                return artifact;
            }
        }
        return null;
    }
    
    /**
     * Try to find a particular build artifact according to the Ant target producing it.
     * @param p a project (should have {@link RakeArtifactProvider} in lookup
     *          in order for this query to work)
     * @param id a desired {@link RakeArtifact#getID ID}
     * @return an artifact produced by that project with the specified target,
     *         or null if none such can be found
     */
    public static RakeArtifact findArtifactByID(Project p, String id) {
        RakeArtifactProvider prov = p.getLookup().lookup(RakeArtifactProvider.class);
        if (prov == null) {
            return null;
        }
        RakeArtifact[] artifacts = prov.getBuildArtifacts();
        for (int i = 0; i < artifacts.length; i++) {
            if (artifacts[i].getID().equals(id)) {
                return artifacts[i];
            }
        }
        return null;
    }
    
    /**
     * Try to find build artifacts of a certain type in a project.
     * @param p a project (should have {@link RakeArtifactProvider} in lookup
     *          in order for this query to work)
     * @param type a desired {@link RakeArtifact#getType artifact type}
     * @return all artifacts of the specified type produced by that project
     *         (may be an empty list)
     */
    public static RakeArtifact[] findArtifactsByType(Project p, String type) {
        RakeArtifactProvider prov = p.getLookup().lookup(RakeArtifactProvider.class);
        if (prov == null) {
            return new RakeArtifact[0];
        }
        RakeArtifact[] artifacts = prov.getBuildArtifacts();
        List<RakeArtifact> l = new ArrayList<RakeArtifact>(artifacts.length);
        for (RakeArtifact aa : artifacts) {
            if (aa.getType().equals(type)) {
                l.add(aa);
            }
        }
        return l.toArray(new RakeArtifact[l.size()]);
    }
    
}
