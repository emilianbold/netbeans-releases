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

package org.netbeans.modules.ruby.spi.project.support.rake;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

/**
 * Translates a list of subproject names into actual subprojects
 * for an Ant-based project.
 * @author Jesse Glick
 */
final class SubprojectProviderImpl implements SubprojectProvider {
    
    private final ReferenceHelper helper;
    
    SubprojectProviderImpl(ReferenceHelper helper) {
        this.helper = helper;
    }
    
    public Set<? extends Project> getSubprojects() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Set<? extends Project>>() {
            public Set<? extends Project> run() {
                // XXX could use a special set w/ lazy isEmpty() - cf. #58639 for freeform
                Set<String> foreignProjectNames = new HashSet<String>();
                for (ReferenceHelper.RawReference ref : helper.getRawReferences()) {
                    foreignProjectNames.add(ref.getForeignProjectName());
                }
                Set<Project> foreignProjects = new HashSet<Project>();
                for (String foreignProjectName : foreignProjectNames) {
                    String prop = "project." + foreignProjectName; // NOI18N
                    RakeProjectHelper h = helper.getRakeProjectHelper();
                    String foreignProjectDirS = helper.eval.getProperty(prop);
                    if (foreignProjectDirS == null) {
                        // Missing for some reason. Skip it.
                        continue;
                    }
                    FileObject foreignProjectDir = h.resolveFileObject(foreignProjectDirS);
                    if (foreignProjectDir == null) {
                        // Not present on disk, erroneous property, etc. Skip it.
                        continue;
                    }
                    try {
                        Project p = ProjectManager.getDefault().findProject(foreignProjectDir);
                        if (p != null) {
                            // OK, got a real project.
                            foreignProjects.add(p);
                        }
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                        // skip it
                    }
                }
                return foreignProjects;
            }
        });
    }
    
    public void addChangeListener(ChangeListener listener) {
        // XXX implement - listen to references added and removed
    }    
    
    public void removeChangeListener(ChangeListener listener) {
        // XXX
    }
    
}
