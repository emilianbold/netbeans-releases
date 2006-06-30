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

package org.netbeans.api.project;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Mutex;
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
        ProjectInformation pi = p.getLookup().lookup(ProjectInformation.class);
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
        Sources s = p.getLookup().lookup(Sources.class);
        if (s != null) {
            return s;
        } else {
            return GenericSources.genericOnly(p);
        }
    }
    
    /**
     * Check whether a project has, or might have, cycles in its subproject graph.
     * <p>
     * If the candidate parameter is null, this simply checks whether the master
     * project's current directed graph of (transitive) subprojects contains any
     * cycles. If the candidate is also passed, this checks whether the master
     * project's subproject graph would contain cycles if the candidate were added
     * as a (direct) subproject of the master project.
     * </p>
     * <p>
     * All cycles are reported even if they do not contain the master project.
     * </p>
     * <p>
     * If the master project already contains the candidate as a (direct) subproject,
     * the effect is as if the candidate were null.
     * </p>
     * <p>
     * Projects with no {@link SubprojectProvider} are considered to have no
     * subprojects, just as if the provider returned an empty set.
     * </p>
     * <p>
     * Acquires read access.
     * </p>
     * <p class="nonnormative">
     * Project types which let the user somehow configure subprojects in the GUI
     * (perhaps indirectly, e.g. via a classpath) should use this call to check
     * for possible cycles before adding new subprojects.
     * </p>
     * @param master a project to root the subproject graph from
     * @param candidate a potential direct subproject of the master project, or null
     * @return true if the master project currently has a cycle somewhere in its
     *         subproject graph, regardless of the candidate parameter, or if the
     *         candidate is not null and the master project does not currently have
     *         a cycle but would have one if the candidate were added as a subproject
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=43845">Issue #43845</a>
     */
    public static boolean hasSubprojectCycles(final Project master, final Project candidate) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                return visit(new HashSet<Project>(), master, master, candidate);
            }
        });
    }
    
    /**
     * Do a DFS traversal checking for cycles.
     * @param encountered projects already encountered in the DFS (added and removed as you go)
     * @param curr current node to visit
     * @param master the original master project (for use with candidate param)
     * @param candidate a candidate added subproject for master, or null
     */
    private static boolean visit(Set<Project> encountered, Project curr, Project master, Project candidate) {
        if (!encountered.add(curr)) {
            return true;
        }
        SubprojectProvider spp = curr.getLookup().lookup(SubprojectProvider.class);
        if (spp != null) {
            for (Project child : spp.getSubprojects()) {
                if (candidate == child) {
                    candidate = null;
                }
                if (visit(encountered, child, master, candidate)) {
                    return true;
                }
            }
        }
        if (candidate != null && curr == master) {
            if (visit(encountered, candidate, master, candidate)) {
                return true;
            }
        }
        assert encountered.contains(curr);
        encountered.remove(curr);
        return false;
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
