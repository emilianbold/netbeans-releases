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

package org.netbeans.modules.apisupport.project;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Element;

/**
 * TODO
 *
 * @author mkrauskopf
 */
public final class ProjectXMLManager {
    
    private AntProjectHelper helper;
    private Project project;
    
    private Set moduleList;
    
    /** Creates a new instance of ProjectXMLManager */
    public ProjectXMLManager(AntProjectHelper helper, Project project) {
        this.helper = helper;
        this.project = project;
    }
    
    public Set/*<ModuleList.Entry>*/ getDirectDependencies() throws IOException {
        if (moduleList != null) {
            return moduleList;
        }
        moduleList = new LinkedHashSet();
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = Util.findElement(confData,
                "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED_NEW); // NOI18N
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        StringBuffer cp = new StringBuffer();
        ModuleList ml = getModuleList();
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            Element dep = (Element)it.next();
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED_NEW);
            String cnb = Util.findText(cnbEl);
            ModuleList.Entry me = ml.getEntry(cnb);
            moduleList.add(me);
        }
        return moduleList;
    }
    
    public void removeDependency(String cnbToRemove) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = Util.findElement(confData,
                "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED_NEW); // NOI18N
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        Iterator it = deps.iterator();
        while (it.hasNext()) {
            Element dep = (Element)it.next();
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED_NEW);
            String cnb = Util.findText(cnbEl);
            if (cnbToRemove.equals(cnb)) {
                moduleDependencies.removeChild(dep);
            }
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    private ModuleList getModuleList() throws IOException {
        return ModuleList.getModuleList(FileUtil.toFile(project.getProjectDirectory()));
    }
}
