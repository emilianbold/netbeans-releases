/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.suite;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * Lists modules in a suite.
 * @author Jesse Glick
 */
final class SuiteSubprojectProviderImpl implements SubprojectProvider {
    
    private final SuiteProject project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    
    public SuiteSubprojectProviderImpl(SuiteProject project, AntProjectHelper helper, PropertyEvaluator eval) {
        this.project = project;
        this.helper = helper;
        this.eval = eval;
    }

    public Set/*<Project>*/ getSubprojects() {
        Set/*<Project>*/ projects = new HashSet();
        String modules = eval.getProperty("modules");
        if (modules != null) {
            String[] pieces = PropertyUtils.tokenizePath(modules);
            for (int i = 0; i < pieces.length; i++) {
                FileObject dir = helper.resolveFileObject(pieces[i]);
                if (dir != null) {
                    try {
                        Project subp = ProjectManager.getDefault().findProject(dir);
                        if (subp != null) {
                            projects.add(subp);
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        return projects;
    }
    
    public void addChangeListener(ChangeListener listener) {}
    
    public void removeChangeListener(ChangeListener listener) {}

}
