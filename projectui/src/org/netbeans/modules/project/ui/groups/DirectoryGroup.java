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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * All projects which can be found beneath some directory, with an optional main project.
 * @author Jesse Glick
 */
public class DirectoryGroup extends Group {

    private static final Logger LOG = Logger.getLogger(DirectoryGroup.class.getName());

    static final String KIND = "directory"; // NOI18N

    /**
     * Create a new group derived from a directory.
     */
    public static DirectoryGroup create(String name, FileObject dir) throws FileStateInvalidException {
        String path = dir.getURL().toExternalForm();
        String id = sanitizeNameAndUniquifyForId(name);
        LOG.log(Level.FINE, "Creating: {0}", id);
        Preferences p = NODE.node(id);
        p.put(KEY_NAME, name);
        p.put(KEY_KIND, KIND);
        p.put(KEY_PATH, path);
        return new DirectoryGroup(id);
    }

    DirectoryGroup(String id) {
        super(id);
    }

    protected void findProjects(Set<Project> projects, ProgressHandle h, int start, int end) {
        String dir = prefs().get(KEY_PATH, null);
        FileObject fo = null;
        try {
            fo = URLMapper.findFileObject(new URL(dir));
        } catch (MalformedURLException x) {
            LOG.log(Level.WARNING, null, x);
        }
        if (fo != null && fo.isFolder()) {
            try {
                Project p = ProjectManager.getDefault().findProject(fo);
                if (p != null) {
                    projects.add(p);
                    if (h != null) {
                        h.progress(progressMessage(p), Math.min(++start, end));
                    }
                }
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
            Enumeration<? extends FileObject> e = fo.getFolders(true);
            while (e.hasMoreElements()) {
                try {
                    Project p = ProjectManager.getDefault().findProject(e.nextElement());
                    if (p != null) {
                        projects.add(p);
                        if (h != null) {
                            h.progress(progressMessage(p), Math.min(++start, end));
                        }
                    }
                } catch (IOException x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }
    }

    public FileObject getDirectory() {
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
        return new DirectoryGroupEditPanel(this);
    }

}
