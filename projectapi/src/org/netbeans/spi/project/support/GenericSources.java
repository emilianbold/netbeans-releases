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

package org.netbeans.spi.project.support;

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.Sources;
import org.netbeans.spi.project.SourceGroup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

// XXX need test

/**
 * Factories for standard {@link Sources} implementations.
 * @author Jesse Glick
 */
public class GenericSources {
    
    private GenericSources() {}
    
    /**
     * Lists only one source folder group, of {@link Sources#TYPE_GENERIC},
     * containing the project directory, as by {@link #group}.
     */
    public static Sources genericOnly(Project p) {
        return new GenericOnlySources(p);
    }
    
    private static final class GenericOnlySources implements Sources {
        
        private final Project p;
        
        GenericOnlySources(Project p) {
            this.p = p;
        }
        
        public SourceGroup[] getSourceGroups(String type) {
            if (type.equals(Sources.TYPE_GENERIC)) {
                return new SourceGroup[] {
                    group(p, p.getProjectDirectory(), ProjectUtils.getInformation(p).getDisplayName())
                };
            } else {
                return new SourceGroup[0];
            }
        }
        
    }
    
    /**
     * Default kind of source folder group.
     * Contains everything inside the supplied root folder which belongs to the
     * supplied project and is considered sharable by VCS.
     */
    public static SourceGroup group(Project p, FileObject rootFolder, String displayName) {
        return new Group(p, rootFolder, displayName);
    }
    
    private static final class Group implements SourceGroup {
        
        private final Project p;
        private final FileObject rootFolder;
        private final String displayName;
        
        Group(Project p, FileObject rootFolder, String displayName) {
            this.p = p;
            this.rootFolder = rootFolder;
            this.displayName = displayName;
        }
        
        public FileObject getRootFolder() {
            return rootFolder;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public boolean contains(FileObject file) throws IllegalArgumentException {
            if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                throw new IllegalArgumentException();
            }
            if (FileOwnerQuery.getOwner(file) != p) {
                return false;
            }
            File f = FileUtil.toFile(file);
            if (f != null) {
                // MIXED, UNKNOWN, and SHARABLE -> include it
                return SharabilityQuery.getSharability(f) != SharabilityQuery.NOT_SHARABLE;
            } else {
                // Not on disk, include it.
                return true;
            }
        }
        
    }
    
}
