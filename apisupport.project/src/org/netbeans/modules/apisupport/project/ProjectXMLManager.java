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
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Convenient class for managing project's <em>project.xml</em> file.
 *
 * @author mkrauskopf
 */
public final class ProjectXMLManager {
    
    // elements constants
    private static final String CODE_NAME_BASE = "code-name-base"; // NOI18N
    private static final String MODULE_DEPENDENCIES = "module-dependencies"; // NOI18N
    private static final String DEPENDENCY = "dependency"; // NOI18N
    private static final String RUN_DEPENDENCY = "run-dependency"; // NOI18N
    private static final String COMPILE_DEPENDENCY = "compile-dependency"; // NOI18N
    private static final String RELEASE_VERSION = "release-version"; // NOI18N
    private static final String SPECIFICATION_VERSION = "specification-version"; // NOI18N
    private static final String BUILD_PREREQUISITE = "build-prerequisite"; // NOI18N
    
    private AntProjectHelper helper;
    private Project project;
    
    private Set directDeps;
    
    /** Creates a new instance of ProjectXMLManager */
    public ProjectXMLManager(AntProjectHelper helper, Project project) {
        this.helper = helper;
        this.project = project;
    }
    
    /** Returns direct module dependencies. */
    public Set/*<ModuleDependency>*/ getDirectDependencies() throws IOException {
        if (directDeps != null) {
            return directDeps;
        }
        directDeps = new TreeSet();
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        ModuleList ml = getModuleList();
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            Element depEl = (Element)it.next();

            Element cnbEl = Util.findElement(depEl, ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            ModuleList.Entry me = ml.getEntry(cnb);

            Element runDepEl = Util.findElement(depEl, ProjectXMLManager.RUN_DEPENDENCY,
                    NbModuleProjectType.NAMESPACE_SHARED);
            if (runDepEl == null) {
                directDeps.add(new ModuleDependency(me));
                continue;
            }
            
            Element relVerEl = Util.findElement(runDepEl, ProjectXMLManager.RELEASE_VERSION,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String relVer = null;
            if (relVerEl != null) {
                relVer = Util.findText(relVerEl);
            }
            
            Element specVerEl = Util.findElement(runDepEl, ProjectXMLManager.SPECIFICATION_VERSION,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String specVer = null;
            if (specVerEl != null) {
                specVer = Util.findText(specVerEl);
            }
            
            directDeps.add(new ModuleDependency(me, relVer, specVer));
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
            Element cnbEl = Util.findElement(dep, ProjectXMLManager.CODE_NAME_BASE,
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
            Element cnbEl = Util.findElement(dep, ProjectXMLManager.CODE_NAME_BASE,
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
    
    public void editDependency(ModuleDependency origDep, ModuleDependency newDep) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            Element dep = (Element) it.next();
            Element cnbEl = Util.findElement(dep, ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            if (cnb.equals(origDep.getModuleEntry().getCodeNameBase())) {
                moduleDependencies.removeChild(dep);
//                ModuleDependency next = (ModuleDependency) it.next();
                createModuleDependencyElement(moduleDependencies, newDep);
//                if (next != null) {
//                    // TODO insert it here
//                }
            }
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Adds given modules as module-dependencies for the project.
     */
    public void addDependencies(Set/*<ModuleDependency>*/ toAdd) {
        Element confData = helper.getPrimaryConfigurationData(true);
        Element moduleDependencies = findModuleDependencies(confData);
        Document doc = moduleDependencies.getOwnerDocument();
        for (Iterator it = toAdd.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            createModuleDependencyElement(moduleDependencies, md);
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    private void createModuleDependencyElement(
            Element moduleDependencies, ModuleDependency md) {
        
        Document doc = moduleDependencies.getOwnerDocument();
        Element modDepEl = createModuleElement(doc, ProjectXMLManager.DEPENDENCY);
        moduleDependencies.appendChild(modDepEl);
        
        modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.CODE_NAME_BASE,
                md.getModuleEntry().getCodeNameBase()));
        modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.BUILD_PREREQUISITE));
        modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.COMPILE_DEPENDENCY));
        
        Element runDepEl = createModuleElement(doc, ProjectXMLManager.RUN_DEPENDENCY);
        modDepEl.appendChild(runDepEl);
        
        String rv = md.getReleaseVersion();
        if (rv != null) {
            runDepEl.appendChild(createModuleElement(
                    doc, ProjectXMLManager.RELEASE_VERSION, rv));
        }
        String sv = md.getSpecificationVersion();
        if (sv != null) {
            runDepEl.appendChild(createModuleElement(
                    doc, ProjectXMLManager.SPECIFICATION_VERSION, sv));
        }
    }
    
    private Element findModuleDependencies(Element confData) {
        return Util.findElement(confData, ProjectXMLManager.MODULE_DEPENDENCIES,
                NbModuleProjectType.NAMESPACE_SHARED);
    }
    
    private Element createModuleElement(Document doc, String name) {
        return doc.createElementNS(NbModuleProjectType.NAMESPACE_SHARED, name);
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
