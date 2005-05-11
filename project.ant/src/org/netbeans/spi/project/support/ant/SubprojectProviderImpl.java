/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.support.ant;

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
    
    public Set/*<Project>*/ getSubprojects() {
        return (Set/*<Project>*/)ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                ReferenceHelper.RawReference[] refs = helper.getRawReferences();
                // XXX could use a special set w/ lazy isEmpty() - cf. #58639 for freeform
                Set/*<String>*/ foreignProjectNames = new HashSet();
                for (int i = 0; i < refs.length; i++) {
                    foreignProjectNames.add(refs[i].getForeignProjectName());
                }
                Set/*<Project>*/ foreignProjects = new HashSet();
                Iterator it = foreignProjectNames.iterator();
                while (it.hasNext()) {
                    String foreignProjectName = (String)it.next();
                    String prop = "project." + foreignProjectName; // NOI18N
                    AntProjectHelper h = helper.getAntProjectHelper();
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
