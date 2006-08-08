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

package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectType;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

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
        Element data = project.getPrimaryConfigurationData();
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
        // #63824: consider also artifacts found in ${cp.extra} and/or <class-path-extension>s
        List/*<Element>*/ cpexts = Util.findSubElements(data);
        it = cpexts.iterator();
        while (it.hasNext()) {
            Element cpext = (Element) it.next();
            if (!cpext.getTagName().equals("class-path-extension")) { // NOI18N
                continue;
            }
            Element binorig = Util.findElement(cpext, "binary-origin", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
            if (binorig == null) {
                continue;
            }
            String text = Util.findText(binorig);
            String eval = project.evaluator().evaluate(text);
            if (eval == null) {
                continue;
            }
            File jar = project.getHelper().resolveFile(eval);
            AntArtifact aa = AntArtifactQuery.findArtifactFromFile(jar);
            if (aa != null) {
                Project owner = aa.getProject();
                if (owner != null) {
                    s.add(owner);
                }
            }
        }
        String eval = project.evaluator().getProperty("cp.extra"); // NOI18N
        if (eval != null) {
            String[] pieces = PropertyUtils.tokenizePath(eval);
            for (int i = 0; i < pieces.length; i++) {
                File jar = project.getHelper().resolveFile(pieces[i]);
                Project owner = FileOwnerQuery.getOwner(jar.toURI());
                if (owner != null) {
                    s.add(owner);
                }
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
