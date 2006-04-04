/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.project.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntArtifactQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Find out how to create a certain build artifact by calling some Ant script.
 * @see AntArtifactQueryImplementation
 * @author Jesse Glick
 */
public class AntArtifactQuery {
    
    private AntArtifactQuery() {}
    
    /**
     * Try to find an Ant artifact object corresponding to a given file on disk.
     * The file need not currently exist for the query to succeed.
     * All registered {@link AntArtifactQueryImplementation} providers are asked.
     * @param file a file which might be built by some Ant target
     * @return an Ant artifact object describing it, or null if it is not recognized
     */
    public static AntArtifact findArtifactFromFile(File file) {
        if (!file.equals(FileUtil.normalizeFile(file))) {
            throw new IllegalArgumentException("Parameter file was not "+  // NOI18N
                "normalized. Was "+file+" instead of "+FileUtil.normalizeFile(file));  // NOI18N
        }
        Iterator it = Lookup.getDefault().lookupAll(AntArtifactQueryImplementation.class).iterator();
        while (it.hasNext()) {
            AntArtifactQueryImplementation aaqi = (AntArtifactQueryImplementation)it.next();
            AntArtifact artifact = aaqi.findArtifact(file);
            if (artifact != null) {
                return artifact;
            }
        }
        return null;
    }
    
    /**
     * Try to find a particular build artifact according to the Ant target producing it.
     * @param p a project (should have {@link AntArtifactProvider} in lookup
     *          in order for this query to work)
     * @param id a desired {@link AntArtifact#getID ID}
     * @return an artifact produced by that project with the specified target,
     *         or null if none such can be found
     */
    public static AntArtifact findArtifactByID(Project p, String id) {
        AntArtifactProvider prov = (AntArtifactProvider)p.getLookup().lookup(AntArtifactProvider.class);
        if (prov == null) {
            return null;
        }
        AntArtifact[] artifacts = prov.getBuildArtifacts();
        for (int i = 0; i < artifacts.length; i++) {
            if (artifacts[i].getID().equals(id)) {
                return artifacts[i];
            }
        }
        return null;
    }
    
    /**
     * Try to find build artifacts of a certain type in a project.
     * @param p a project (should have {@link AntArtifactProvider} in lookup
     *          in order for this query to work)
     * @param type a desired {@link AntArtifact#getType artifact type}
     * @return all artifacts of the specified type produced by that project
     *         (may be an empty list)
     */
    public static AntArtifact[] findArtifactsByType(Project p, String type) {
        AntArtifactProvider prov = (AntArtifactProvider)p.getLookup().lookup(AntArtifactProvider.class);
        if (prov == null) {
            return new AntArtifact[0];
        }
        AntArtifact[] artifacts = prov.getBuildArtifacts();
        List/*<AntArtifact>*/ l = new ArrayList(artifacts.length);
        for (int i = 0; i < artifacts.length; i++) {
            if (artifacts[i].getType().equals(type)) {
                l.add(artifacts[i]);
            }
        }
        return (AntArtifact[])l.toArray(new AntArtifact[l.size()]);
    }
    
}
