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

package org.netbeans.api.project;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Utilities;

/**
 * Utility methods to get information about {@link Project}s.
 * @author Jesse Glick
 */
public class ProjectUtils {
    
    private ProjectUtils() {}
    
    /**
     * Get basic information about a project.
     * If the project has a {@link ProjectInformation} instance in its lookup,
     * that is used. Otherwise, a basic dummy implementation is returned.
     * @param p a project
     * @return some information about it
     * @see Project#getLookup
     */
    public static ProjectInformation getInformation(Project p) {
        ProjectInformation pi = (ProjectInformation)p.getLookup().lookup(ProjectInformation.class);
        if (pi != null) {
            return pi;
        } else {
            return new BasicInformation(p);
        }
    }
    
    /**
     * Get a list of sources for a project.
     * If the project has a {@link Sources} instance in its lookup,
     * that is used. Otherwise, a basic implementation is returned
     * using {@link GenericSources#genericOnly}.
     * @param p a project
     * @return a list of sources for it
     * @see Project#getLookup
     */
    public static Sources getSources(Project p) {
        Sources s = (Sources)p.getLookup().lookup(Sources.class);
        if (s != null) {
            return s;
        } else {
            return GenericSources.genericOnly(p);
        }
    }
    
    private static final class BasicInformation implements ProjectInformation {
        
        private final Project p;
        
        public BasicInformation(Project p) {
            this.p = p;
        }
        
        public String getName() {
            try {
                return p.getProjectDirectory().getURL().toExternalForm();
            } catch (FileStateInvalidException e) {
                return e.toString();
            }
        }
        
        public String getDisplayName() {
            return p.getProjectDirectory().getNameExt();
        }
        
        public Icon getIcon() {
            return new ImageIcon(Utilities.loadImage("org/netbeans/modules/projectapi/resources/empty.gif")); // NOI18N
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // never changes
        }
        
        public Project getProject() {
            return p;
        }
        
    }
    
}
