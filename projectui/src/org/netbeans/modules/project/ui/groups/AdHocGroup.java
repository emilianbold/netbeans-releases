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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Arbitrary collection of projects, with an optional main project.
 * @author Jesse Glick
 */
public class AdHocGroup extends Group {

    private static final Logger LOG = Logger.getLogger(AdHocGroup.class.getName());

    /** Preferences key for whether to automatically synchronize projects. */
    private static final String KEY_AUTO_SYNCH = "autoSynch"; // NOI18N

    static final String KIND = "adHoc"; // NOI18N

    /**
     * Create a new ad-hoc group of projects.
     */
    public static AdHocGroup create(String name, boolean autoSynch) {
        String id = sanitizeNameAndUniquifyForId(name);
        LOG.log(Level.FINE, "Creating: {0}", id);
        Preferences p = NODE.node(id);
        p.put(KEY_NAME, name);
        p.put(KEY_KIND, KIND);
        p.putBoolean(KEY_AUTO_SYNCH, autoSynch);
        return new AdHocGroup(id);
    }

    AdHocGroup(String id) {
        super(id);
    }

    protected void findProjects(Set<Project> projects, ProgressHandle h, int start, int end) {
        String paths = prefs().get(KEY_PATH, "");
        if (paths.length() > 0) { // "".split(...) -> [""]
            String[] items = paths.split(" ");
            for (String path : items) {
                Project p = projectForPath(path);
                if (p != null) {
                    if (h != null) {
                        h.progress(progressMessage(p), start += ((end - start) / items.length));
                    }
                    projects.add(p);
                }
            }
        }
    }

    /**
     * Change the projects in the group.
     */
    public void setProjects(Set<Project> projects) {
        Set<String> projectPaths = new TreeSet<String>();
        for (Project prj : projects) {
            try {
                projectPaths.add(prj.getProjectDirectory().getURL().toExternalForm());
            } catch (FileStateInvalidException x) {
                LOG.log(Level.WARNING, null, x);
            }
        }
        prefs().put(KEY_PATH, joinPaths(projectPaths));
        LOG.log(Level.FINE, "updating projects for {0} to {1}", new Object[] {id, projects});
    }

    private static String joinPaths(Collection<String> paths) {
        StringBuilder b = new StringBuilder();
        for (String p : paths) {
            if (b.length() > 0) {
                b.append(' ');
            }
            b.append(p);
        }
        return b.toString();
    }

    /**
     * If true, group will automatically update its contents when open.
     */
    public boolean isAutoSynch() {
        return prefs().getBoolean(KEY_AUTO_SYNCH, false);
    }

    /**
     * @see #isAutoSynch
     */
    public void setAutoSynch(boolean b) {
        prefs().putBoolean(KEY_AUTO_SYNCH, b);
    }

    /**
     * Update a group's definition with the current list of open projects (and main project).
     */
    public void synch() {
        OpenProjects op = OpenProjects.getDefault();
        setProjects(new HashSet<Project>(Arrays.asList(op.getOpenProjects())));
        setMainProject(op.getMainProject());
    }

    public GroupEditPanel createPropertiesPanel() {
        return Group.isAdvancedMode() ? new AdHocGroupEditPanel(this) : new AdHocGroupEditPanelBasic(this);
    }

    @Override
    protected void closed() {
        if (isAutoSynch()) {
            setProjects(new HashSet<Project>(Arrays.asList(OpenProjects.getDefault().getOpenProjects())));
        }
        // *After* setting projects - so that main project status correctly updated for new group.
        super.closed();
    }

    @Override
    public boolean isPristine() {
        if (isAutoSynch()) {
            return true;
        } else {
            return super.isPristine();
        }
    }

    @Override
    protected String toString(boolean scrubPersonalInfo) {
        return super.toString(scrubPersonalInfo) + (isAutoSynch() ? "" : "[!autoSynch]");
    }

}
