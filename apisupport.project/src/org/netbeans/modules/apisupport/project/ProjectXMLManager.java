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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TODO
 *
 * @author mkrauskopf
 */
public final class ProjectXMLManager {
    
    private AntProjectHelper helper;
    private Project project;
    
    private Set directDeps;
    
    /** Creates a new instance of ProjectXMLManager */
    public ProjectXMLManager(AntProjectHelper helper, Project project) {
        this.helper = helper;
        this.project = project;
    }
    
    public Set/*<ModuleList.Entry>*/ getDirectDependencies() throws IOException {
        if (directDeps != null) {
            return directDeps;
        }
        directDeps = new TreeSet();
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        ModuleList ml = getModuleList();
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            Element dep = (Element)it.next();
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            ModuleList.Entry me = ml.getEntry(cnb);
            directDeps.add(me);
        }
        return directDeps;
    }
    
    /** Remove given dependency from the configuration data. */
    public void removeDependency(String cnbToRemove) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            Element dep = (Element)it.next();
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            if (cnbToRemove.equals(cnb)) {
                moduleDependencies.removeChild(dep);
            }
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Use this for removing more than one dependencies. It's faster then
     * iterating and using <code>removeDependency</code> for every entry.
     */
    public void removeDependencies(Collection/*<String>*/ toDelete) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            Element dep = (Element)it.next();
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            if (toDelete.remove(cnb)) {
                moduleDependencies.removeChild(dep);
            }
            if (toDelete.size() == 0) {
                break; // everything was deleted
            }
        }
        if (toDelete.size() != 0) {
            Util.err.log(ErrorManager.WARNING,
                    "Some modules weren't deleted: " + toDelete); // NOI18N
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Adds given modules as module-dependencies for the project.
     */
    public void addDependencies(Set/*<ModuleList.Entry>*/ toAdd) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        Document doc = confData.getOwnerDocument();
        for (Iterator it = toAdd.iterator(); it.hasNext(); ) {
            ModuleList.Entry me = (ModuleList.Entry) it.next();
            Element modDepEl = createModuleElement(doc, "dependency"); // NOI18N
            moduleDependencies.appendChild(modDepEl);
            
            modDepEl.appendChild(createModuleElement(doc, "code-name-base", me.getCodeNameBase()));
            modDepEl.appendChild(createModuleElement(doc, "build-prerequisite")); // NOI18N;
            modDepEl.appendChild(createModuleElement(doc, "compile-dependency")); // NOI18N
            
            Element runDepEl = createModuleElement(doc, "run-dependency");
            modDepEl.appendChild(runDepEl); // NOI18N
            
            if (me.getReleaseVersion() != null) {
                runDepEl.appendChild(createModuleElement(
                        doc, "release-version", me.getReleaseVersion())); // NOI18N
            }
            if (me.getSpecificationVersion() != null) {
                runDepEl.appendChild(createModuleElement(
                        doc, "specification-version", me.getSpecificationVersion())); // NOI18N
            }
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    private Element findModuleDependencies(Element confData) {
        return Util.findElement(confData, "module-dependencies", // NOI18N
                NbModuleProjectType.NAMESPACE_SHARED);
    }
    
    private Element createModuleElement(Document doc, String name) {
        return doc.createElementNS(NbModuleProjectType.NAMESPACE_SHARED, name); // NOI18N
    }
    
    private Element createModuleElement(Document doc, String name, String innerText) {
        Element el = createModuleElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }
    
    /**
     * Helper method to get the <code>ModuleList</code> for the project this
     * instance manage.
     */
    private ModuleList getModuleList() throws IOException {
        return ModuleList.getModuleList(FileUtil.toFile(project.getProjectDirectory()));
    }
}
