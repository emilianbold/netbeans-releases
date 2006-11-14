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

package org.netbeans.modules.project.ui.groups;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;

/**
 * A main project and all (recursive) subprojects, with an optional main project.
 * @author Jesse Glick
 */
public class SubprojectsGroup extends Group {

    private static final Logger LOG = Logger.getLogger(SubprojectsGroup.class.getName());

    static final String KIND = "subprojects"; // NOI18N

    /**
     * Create a new group based on a superproject.
     * The main project is always initialized to be the superproject itself,
     * but this could be changed later.
     * The display name is by default that of the superproject.
     */
    public static SubprojectsGroup create(Project project) throws FileStateInvalidException {
        String path = project.getProjectDirectory().getURL().toExternalForm();
        String id = sanitizeNameAndUniquifyForId(ProjectUtils.getInformation(project).getName());
        LOG.log(Level.FINE, "Creating: {0}", id);
        Preferences p = NODE.node(id);
        p.put(KEY_KIND, KIND);
        p.put(KEY_PATH, path);
        p.put(KEY_MAIN, path);
        return new SubprojectsGroup(id);
    }

    SubprojectsGroup(String id) {
        super(id);
    }

    @Override
    protected String getNameOrNull() {
        String n = super.getNameOrNull();
        if (n == null) {
            Project p = projectForPath(prefs().get(KEY_PATH, null));
            if (p != null) {
                return ProjectUtils.getInformation(p).getDisplayName();
            }
        }
        return n;
    }

    protected void findProjects(Set<Project> projects, ProgressHandle h, int start, int end) {
        Project p = projectForPath(prefs().get(KEY_PATH, null));
        if (p != null) {
            visitSubprojects(p, projects, h, new int[] {start, end});
        }
    }

    private static void visitSubprojects(Project p, Set<Project> projects, ProgressHandle h, int[] startEnd) {
        if (projects.add(p)) {
            if (h != null) {
                h.progress(progressMessage(p), Math.min(++startEnd[0], startEnd[1]));
            }
            SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
            if (spp != null) {
                for (Project p2 : spp.getSubprojects()) {
                    visitSubprojects(p2, projects, h, startEnd);
                }
            }
        }
    }

    public FileObject getMasterProjectDirectory() {
        String p = prefs().get(KEY_PATH, null);
        if (p != null && p.length() > 0) {
            try {
                return URLMapper.findFileObject(new URL(p));
            } catch (MalformedURLException x) {
                LOG.log(Level.WARNING, null, x);
            }
        }
        return null;
    }

    public GroupEditPanel createPropertiesPanel() {
        return new SubprojectsGroupEditPanel(this);
    }

}
