/*
 * DistributionFileMap.java
 *
 * Created on August 30, 2004, 6:45 AM
 */

package org.netbeans.modules.j2ee.deployment.common.api;

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * Extra file mapping service for each j2ee module.  This service cover irregular source to
 * distribution file mapping.  Users of the mapping could update the mapping.  Provider of
 * the mapping has to ensure the mapping persistence.
 *
 * Note: the initial design is for non-static configuration file such as schema file used in
 * CMP mapping, but it could be used to expose any kind of source-to-distribution mapping.
 *
 * @author  nn136682
 */
public abstract class SourceFileMap {
    
    /**
     * Add new mapping or update existing mapping of the given distribution path.
     * Provider of the mapping needs to extract and persist the relative path to
     * ensure the mapping is in project sharable data.  The mapping would be
     * used in ensuring that during build time the source file is put at the 
     * right relative path in the distribution content.
     *
     * @param distributionPath file path in the distribution content
     * @param sourceFile souce concrete file object.
     * @return true if added successfully; false if source file is out of this mapping scope.
     */
    public abstract boolean add(String distributionPath, FileObject sourceFile);

    /**
     * Remove mapping for the given distribution path.
     * @param distributionPath file path in the distribution content
     */
    public abstract FileObject remove(String distributionPath);

    /**
     * Returns the concrete file for the ginven distribution path.
     * @param distributionPath distribution path for to find source file for.
     */
    public abstract FileObject findSourceFile(String distributionPath);

    
    /**
     * Gets a source file mapping using the given primary file.
     * @param primary a non-null abssolute path of a module file such a primary configuration 
     * descriptor.
     */
    public static final SourceFileMap getExtraFileMap(File primary) {
        FileObject primaryFO = FileUtil.toFileObject(primary);
        Project owner = FileOwnerQuery.getOwner(primaryFO);
        if (owner != null) {
            Lookup l = owner.getLookup();
            J2eeModuleProvider projectModule = (J2eeModuleProvider) l.lookup(J2eeModuleProvider.class);
            if (projectModule != null) {
                return projectModule.getSourceFileMap();
            }
        }
        return null;
    }
}
