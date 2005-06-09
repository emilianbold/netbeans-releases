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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;
import org.netbeans.modules.apisupport.project.*;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;

/**
 * Enumerates subprojects, defined as other modules on which this one
 * has build-time dependencies.
 * @author Jesse Glick
 */
public final class SubprojectProviderImpl implements SubprojectProvider {
    
    private final NbModuleProject project;
    
    public SubprojectProviderImpl(NbModuleProject project) {
        this.project = project;
    }
    
    public Set getSubprojects() {
        // XXX could use a special set w/ lazy isEmpty() - cf. #58639 for freeform
        Set/*<Project>*/ s = new HashSet();
        ModuleList ml;
        try {
            ml = project.getModuleList();
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
            return Collections.EMPTY_SET;
        }
        Element data = project.getHelper().getPrimaryConfigurationData(true);
        Element moduleDependencies = Util.findElement(data,
            "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        Iterator it = deps.iterator();
        while (it.hasNext()) {
            Element dep = (Element)it.next();
            /* Probably better to open runtime deps too. TBD.
            if (Util.findElement(dep, "build-prerequisite", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED) == null) {
                continue;
            }
             */
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            ModuleEntry module = ml.getEntry(cnb);
            if (module == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + project);
                continue;
            }
            File moduleProjectDirF = module.getSourceLocation();
            if (moduleProjectDirF == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not find sources for dependent module " + cnb + " for " + project);
                continue;
            }
            FileObject moduleProjectDir = FileUtil.toFileObject(moduleProjectDirF);
            if (moduleProjectDir == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not load sources for dependent module " + cnb + " for " + project);
                continue;
            }
            try {
                Project moduleProject = ProjectManager.getDefault().findProject(moduleProjectDir);
                if (moduleProject == null) {
                    Util.err.log(ErrorManager.WARNING, "Warning - dependent module " + cnb + " for " + project + " is not projectized");
                    continue;
                }
                s.add(moduleProject);
            } catch (IOException e) {
                Util.err.notify(e);
            }
        }
        return s;
    }
    
    public void addChangeListener(ChangeListener listener) {
        // XXX no impl yet
    }
    
    public void removeChangeListener(ChangeListener listener) {
        // XXX
    }
    
}
