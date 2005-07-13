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
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider.NbModuleType;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectType;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleDependency;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;

/**
 * Convenience class for managing project's <em>project.xml</em> file.
 *
 * @author mkrauskopf
 */
public final class ProjectXMLManager {
    
    /** Equal to AntProjectHelper.PROJECT_NS which is package private. */
    // XXX is there a better way? (impact of imposibility to use ProjectGenerator)
    private static final String PROJECT_NS =
            "http://www.netbeans.org/ns/project/1"; // NOI18N
    
    // elements constants
    private static final String BINARY_ORIGIN = "binary-origin"; // NOI18N
    private static final String BUILD_PREREQUISITE = "build-prerequisite"; // NOI18N
    private static final String CLASS_PATH_EXTENSION = "class-path-extension"; // NOI18N
    private static final String CODE_NAME_BASE = "code-name-base"; // NOI18N
    private static final String COMPILE_DEPENDENCY = "compile-dependency"; // NOI18N
    private static final String DATA = "data"; // NOI18N
    private static final String DEPENDENCY = "dependency"; // NOI18N
    private static final String FRIEND = "friend"; // NOI18N
    private static final String FRIEND_PACKAGES= "friend-packages"; // NOI18N
    private static final String IMPLEMENTATION_VERSION = "implementation-version"; // NOI18N
    private static final String MODULE_DEPENDENCIES = "module-dependencies"; // NOI18N
    private static final String PACKAGE = "package"; // NOI18N
    private static final String PUBLIC_PACKAGES= "public-packages"; // NOI18N
    private static final String RELEASE_VERSION = "release-version"; // NOI18N
    private static final String RUN_DEPENDENCY = "run-dependency"; // NOI18N
    private static final String SPECIFICATION_VERSION = "specification-version"; // NOI18N
    private static final String STANDALONE = "standalone"; // NOI18N
    private static final String SUBPACKAGES = "subpackages"; // NOI18N
    private static final String SUITE_COMPONENT = "suite-component"; // NOI18N
    
    private AntProjectHelper helper;
    private NbPlatform plaf;
    
    private String cnb;
    private SortedSet/*<ModuleDependency>*/ directDeps;
    private ManifestManager.PackageExport[] publicPackages;
    private String[] cpExtensions;
    private String[] friends;
    
    // cached confData element for easy access with getConfData
    private Element confData;
    
    /** Creates a new instance of ProjectXMLManager */
    public ProjectXMLManager(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    public void setModuleType(NbModuleType moduleType) {
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();
        
        Element standaloneEl = Util.findElement(confData, ProjectXMLManager.STANDALONE,
                NbModuleProjectType.NAMESPACE_SHARED);
        if (standaloneEl != null && moduleType == NbModuleTypeProvider.STANDALONE) {
            // nothing needs to be done - standalone is already set
            return;
        }
        
        Element suiteCompEl = Util.findElement(confData, ProjectXMLManager.SUITE_COMPONENT,
                NbModuleProjectType.NAMESPACE_SHARED);
        if (suiteCompEl != null && moduleType == NbModuleTypeProvider.SUITE_COMPONENT) {
            // nothing needs to be done - suiteCompEl is already set
            return;
        }
        
        if (suiteCompEl == null && standaloneEl == null && moduleType == NbModuleTypeProvider.NETBEANS_ORG) {
            // nothing needs to be done - nb.org modules don't have any element
            return;
        }
        
        // Ok, we get here. So clean up....
        if (suiteCompEl != null) {
            confData.removeChild(suiteCompEl);
        }
        if (standaloneEl != null) {
            confData.removeChild(standaloneEl);
        }
        
        // ....and create element for new module type.
        Element newModuleType = createTypeElement(doc, moduleType);
        if (newModuleType != null) {
            confData.insertBefore(newModuleType, findModuleDependencies(confData));
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /** Returns sorted direct module dependencies. */
    public SortedSet/*<ModuleDependency>*/ getDirectDependencies(NbPlatform plaf) throws IOException {
        if (this.plaf == plaf && directDeps != null) {
            return directDeps;
        }
        directDeps = new TreeSet();
        Element moduleDependencies = findModuleDependencies(getConfData());
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        ModuleList ml = getModuleList(plaf);
        for (Iterator it = deps.iterator(); it.hasNext(); ) {
            Element depEl = (Element)it.next();
            
            Element cnbEl = Util.findElement(depEl,
                    ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            ModuleEntry me = ml.getEntry(cnb);
            if (me == null) {
                // XXX might be e.g. shown in nb.errorForreground and "disabled"
                Util.err.log(ErrorManager.WARNING,
                        "Detected dependency on module which cannot be found in " + // NOI18N
                        "the current module's universe (platform, suite): " +  // NOI18N
                        cnb + " (skipping)"); // NOI18N
                continue;
            }
            
            Element runDepEl = Util.findElement(depEl,
                    ProjectXMLManager.RUN_DEPENDENCY,
                    NbModuleProjectType.NAMESPACE_SHARED);
            if (runDepEl == null) {
                directDeps.add(new ModuleDependency(me));
                continue;
            }
            
            Element relVerEl = Util.findElement(runDepEl,
                    ProjectXMLManager.RELEASE_VERSION,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String relVer = null;
            if (relVerEl != null) {
                relVer = Util.findText(relVerEl);
            }
            
            Element specVerEl = Util.findElement(runDepEl,
                    ProjectXMLManager.SPECIFICATION_VERSION,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String specVer = null;
            if (specVerEl != null) {
                specVer = Util.findText(specVerEl);
            }
            
            Element compDepEl = Util.findElement(depEl,
                    ProjectXMLManager.COMPILE_DEPENDENCY,
                    NbModuleProjectType.NAMESPACE_SHARED);
            
            Element impleVerEl = Util.findElement(runDepEl,
                    ProjectXMLManager.IMPLEMENTATION_VERSION,
                    NbModuleProjectType.NAMESPACE_SHARED);
            
            directDeps.add(new ModuleDependency(
                    me, relVer, specVer, compDepEl != null, impleVerEl != null));
        }
        return directDeps;
    }
    
    /** Remove given dependency from the configuration data. */
    public void removeDependency(String cnbToRemove) {
        Element confData = getConfData();
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
    public void removeDependencies(Collection/*<ModuleDependency>*/ depsToDelete) {
        Set cnbsToDelete = new HashSet(depsToDelete.size());
        for (Iterator it = depsToDelete.iterator(); it.hasNext(); ) {
            cnbsToDelete.add(((ModuleDependency) it.next()).
                    getModuleEntry().getCodeNameBase());
        }
        removeDependenciesByCNB(cnbsToDelete);
    }
    
    /**
     * Use this for removing more than one dependencies. It's faster then
     * iterating and using <code>removeDependency</code> for every entry.
     */
    public void removeDependenciesByCNB(Collection/*<String>*/ cnbsToDelete) {
        Element confData = getConfData();
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            Element dep = (Element)it.next();
            Element cnbEl = Util.findElement(dep, ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            if (cnbsToDelete.remove(cnb)) {
                moduleDependencies.removeChild(dep);
            }
            if (cnbsToDelete.size() == 0) {
                break; // everything was deleted
            }
        }
        if (cnbsToDelete.size() != 0) {
            Util.err.log(ErrorManager.WARNING,
                    "Some modules weren't deleted: " + cnbsToDelete); // NOI18N
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    public void editDependency(ModuleDependency origDep, ModuleDependency newDep) {
        Element confData = getConfData();
        Element moduleDependencies = findModuleDependencies(confData);
        List/*<Element>*/ currentDeps = Util.findSubElements(moduleDependencies);
        for (Iterator it = currentDeps.iterator(); it.hasNext(); ) {
            Element dep = (Element) it.next();
            Element cnbEl = Util.findElement(dep, ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            if (cnb.equals(origDep.getModuleEntry().getCodeNameBase())) {
                moduleDependencies.removeChild(dep);
                Element nextDep = it.hasNext() ? (Element) it.next() : null;
                createModuleDependencyElement(moduleDependencies, newDep, nextDep);
                break;
            }
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Adds given modules as module-dependencies for the project.
     */
    public void addDependencies(Set/*<ModuleDependency>*/ toAdd) {
        Element confData = getConfData();
        Element moduleDependencies = findModuleDependencies(confData);
        for (Iterator it = toAdd.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            createModuleDependencyElement(moduleDependencies, md, null);
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Replaces all original dependencies with the given <code>newDeps</code>.
     */
    public void replaceDependencies(Set/*<ModuleDependency>*/ newDeps) {
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();
        Element moduleDependencies = findModuleDependencies(confData);
        confData.removeChild(moduleDependencies);
        moduleDependencies = createModuleElement(doc, ProjectXMLManager.MODULE_DEPENDENCIES);
        Element publicPackages = findPublicPackagesElement(confData);
        confData.insertBefore(moduleDependencies, publicPackages);
        for (Iterator it = newDeps.iterator(); it.hasNext(); ) {
            ModuleDependency md = (ModuleDependency) it.next();
            createModuleDependencyElement(moduleDependencies, md, null);
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Replaces all original public packages with the given
     * <code>newPackages</code>. Also removes friend packages if there are any
     * since those two mutually exclusive.
     */
    public void replacePublicPackages(String[] newPackages) {
        removePublicAndFriends();
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();
        Element publicPackages = createModuleElement(doc, ProjectXMLManager.PUBLIC_PACKAGES);
        confData.appendChild(publicPackages);
        for (int i = 0; i < newPackages.length; i++) {
            publicPackages.appendChild(
                    createModuleElement(doc, ProjectXMLManager.PACKAGE, newPackages[i]));
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Replaces all original friends with the given <code>friends</code> with
     * <code>packages</code> as exposed packages to those friends. Also removes
     * public packages if there are any since those two are mutually exclusive.
     */
    public void replaceFriendPackages(String[] friends, String[] packages) {
        removePublicAndFriends();
        Element confData = getConfData();
        Document doc = confData.getOwnerDocument();
        Element friendPackages = createModuleElement(doc, ProjectXMLManager.FRIEND_PACKAGES);
        confData.appendChild(friendPackages);
        for (int i = 0; i < friends.length; i++) {
            friendPackages.appendChild(
                    createModuleElement(doc, ProjectXMLManager.FRIEND, friends[i]));
        }
        for (int i = 0; i < packages.length; i++) {
            friendPackages.appendChild(
                    createModuleElement(doc, ProjectXMLManager.PACKAGE, packages[i]));
        }
        helper.putPrimaryConfigurationData(confData, true);
    }
    
    /**
     * Returns an array of {@link ManifestManager.PackageExport}s of all
     * exposed public packages. Method considers both <em>package</em> and
     * <em>subpackages</em> elements with the recursivity flag set
     * appropriately for returned entries.
     */
    public ManifestManager.PackageExport[] getPublicPackages() {
        if (publicPackages == null) {
            publicPackages = ProjectXMLManager.findPublicPackages(getConfData());
        }
        return publicPackages;
    }
    
    /** Returns all friends or <code>null</code> if there are none. */
    public String[] getFriends() {
        if (friends == null) {
            friends = ProjectXMLManager.findFriends(getConfData());
        }
        return friends;
    }
    
    
    /**
     * Returns paths of all libraries bundled within a project this
     * <em>manager</em> manage. So the result should be an array of
     * <code>String</code>s each representing a relative path to the project's
     * external library (jar/zip).
     * @return an array of strings (may be empty)
     */
    public String[] getBinaryOrigins() {
        if (cpExtensions != null) {
            return cpExtensions;
        }
        
        List/*<Element>*/ cpExtEls = Util.findSubElements(getConfData());
        Set/*<String>*/ binaryOrigs = new TreeSet();
        for (Iterator it = cpExtEls.iterator(); it.hasNext(); ) {
            Element cpExtEl = (Element) it.next();
            if (CLASS_PATH_EXTENSION.equals(cpExtEl.getTagName())) {
                Element binOrigEl = Util.findElement(cpExtEl, BINARY_ORIGIN, NbModuleProjectType.NAMESPACE_SHARED);
                if (binOrigEl != null) {
                    binaryOrigs.add(Util.findText(binOrigEl));
                }
            }
        }
        String[] result = new String[binaryOrigs.size()];
        return (String[]) binaryOrigs.toArray(result);
    }
    
    /** Returns code-name-base. */
    public String getCodeNameBase() {
        if (cnb == null) {
            Element cnbEl = Util.findElement(getConfData(),
                    ProjectXMLManager.CODE_NAME_BASE,
                    NbModuleProjectType.NAMESPACE_SHARED);
            cnb = Util.findText(cnbEl);
        }
        return cnb;
    }
    
    private void createModuleDependencyElement(
            Element moduleDependencies, ModuleDependency md, Element nextSibling) {
        
        Document doc = moduleDependencies.getOwnerDocument();
        Element modDepEl = createModuleElement(doc, ProjectXMLManager.DEPENDENCY);
        moduleDependencies.insertBefore(modDepEl, nextSibling);
        
        modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.CODE_NAME_BASE,
                md.getModuleEntry().getCodeNameBase()));
        if (md.hasCompileDependency()) {
            modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.BUILD_PREREQUISITE));
            modDepEl.appendChild(createModuleElement(doc, ProjectXMLManager.COMPILE_DEPENDENCY));
        }
        
        Element runDepEl = createModuleElement(doc, ProjectXMLManager.RUN_DEPENDENCY);
        modDepEl.appendChild(runDepEl);
        
        String rv = md.getReleaseVersion();
        if (rv != null && !rv.trim().equals("")) {
            runDepEl.appendChild(createModuleElement(
                    doc, ProjectXMLManager.RELEASE_VERSION, rv));
        }
        if (md.hasImplementationDepedendency()) {
            runDepEl.appendChild(createModuleElement(
                    doc, ProjectXMLManager.IMPLEMENTATION_VERSION));
        } else {
            String sv = md.getSpecificationVersion();
            if (sv != null && !"".equals(sv)) { // NOI18N
                runDepEl.appendChild(createModuleElement(
                        doc, ProjectXMLManager.SPECIFICATION_VERSION, sv));
            }
        }
    }
    
    /** Removes public-packages and friend-packages elements. */
    private void removePublicAndFriends() {
        Element friendPackages = findFriendsElement(getConfData());
        if (friendPackages != null) {
            getConfData().removeChild(friendPackages);
        }
        Element publicPackages = findPublicPackagesElement(getConfData());
        if (publicPackages != null) {
            getConfData().removeChild(publicPackages);
        }
    }
    
    private static Element findModuleDependencies(Element confData) {
        return Util.findElement(confData, ProjectXMLManager.MODULE_DEPENDENCIES,
                NbModuleProjectType.NAMESPACE_SHARED);
    }
    
    private static Element findPublicPackagesElement(Element confData) {
        return Util.findElement(confData, ProjectXMLManager.PUBLIC_PACKAGES,
                NbModuleProjectType.NAMESPACE_SHARED);
    }
    
    private static Element findFriendsElement(Element confData) {
        return Util.findElement(confData, ProjectXMLManager.FRIEND_PACKAGES,
                NbModuleProjectType.NAMESPACE_SHARED);
    }
    
    private static Element createModuleElement(Document doc, String name) {
        return doc.createElementNS(NbModuleProjectType.NAMESPACE_SHARED, name);
    }
    
    private static Element createModuleElement(Document doc, String name, String innerText) {
        Element el = createModuleElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }
    
    private static Element createSuiteElement(Document doc, String name) {
        return doc.createElementNS(SuiteProjectType.NAMESPACE_SHARED, name);
    }
    
    private static Element createSuiteElement(Document doc, String name, String innerText) {
        Element el = createSuiteElement(doc, name);
        el.appendChild(doc.createTextNode(innerText));
        return el;
    }
    
    /**
     * Helper method to get the <code>ModuleList</code> for the project this
     * instance manage.
     */
    private ModuleList getModuleList(NbPlatform plaf) throws IOException {
        if (plaf != null) {
            return ModuleList.getModuleList(FileUtil.toFile(helper.getProjectDirectory()), plaf.getDestDir());
        } else {
            return ModuleList.getModuleList(FileUtil.toFile(helper.getProjectDirectory()));
        }
    }
    
    /**
     * Find packages in public-packages or friend-packages section. Method
     * considers both <em>package</em> and <em>subpackages</em> elements with
     * the recursivity flag set appropriately for returned entries.
     */
    private static Set/*<ManifestManager.PackageExport>*/ findAllPackages(Element parent) {
        Set/*<ManifestManager.PackageExport>*/ packages = new HashSet();
        List/*<Element>*/ pkgEls = Util.findSubElements(parent);
        for (Iterator it = pkgEls.iterator(); it.hasNext(); ) {
            Element pkgEl = (Element) it.next();
            if (PACKAGE.equals(pkgEl.getTagName())) {
                packages.add(new ManifestManager.PackageExport(Util.findText(pkgEl), false));
            } else if (SUBPACKAGES.equals(pkgEl.getTagName())) {
                packages.add(new ManifestManager.PackageExport(Util.findText(pkgEl), true));
            }
        }
        return packages;
    }
    
    /**
     * Utility method for finding public packages. Method considers both
     * <em>package</em> and <em>subpackages</em> elements with the recursivity
     * flag set appropriately for returned entries.
     */
    public static ManifestManager.PackageExport[] findPublicPackages(final Element confData) {
        Element ppEl = findPublicPackagesElement(confData);
        Set/*<ManifestManager.PackageExport>*/ pps = new HashSet();
        if (ppEl != null) {
            pps.addAll(findAllPackages(ppEl));
        }
        ppEl = findFriendsElement(confData);
        if (ppEl != null) {
            pps.addAll(findAllPackages(ppEl));
        }
        return pps.isEmpty() ? ManifestManager.EMPTY_EXPORTED_PACKAGES :
            (ManifestManager.PackageExport[]) pps.toArray(new ManifestManager.PackageExport[pps.size()]);
    }
    
    /** Utility method for finding friend. */
    public static String[] findFriends(final Element confData) {
        Element friendsEl = findFriendsElement(confData);
        if (friendsEl != null) {
            List/*<Element>*/ friendEls = Util.findSubElements(friendsEl);
            Set/*<String>*/ friends = new TreeSet();
            for (Iterator it = friendEls.iterator(); it.hasNext(); ) {
                Element friendEl = (Element) it.next();
                if (FRIEND.equals(friendEl.getTagName())) {
                    friends.add(Util.findText(friendEl));
                }
            }
            String[] result = new String[friends.size()];
            return (String[]) friends.toArray(result);
        }
        return null;
    }
    
    /**
     * Generates a basic <em>project.xml</em> templates into the given
     * <code>projectXml</code> for <em>standalone</em> or <em>module in
     * suite</em> module.
     */
    static void generateEmptyModuleTemplate(FileObject projectXml, String cnb,
            NbModuleType moduleType) throws IOException {
        
        Document prjDoc = XMLUtil.createDocument("project", PROJECT_NS, null, null); // NOI18N
        
        // generate general project elements
        Element typeEl = prjDoc.createElementNS(PROJECT_NS, "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode(NbModuleProjectType.TYPE));
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS(PROJECT_NS, "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);
        
        // generate NB Module project type specific elements
        Element dataEl = createModuleElement(confEl.getOwnerDocument(), DATA);
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createModuleElement(dataDoc, CODE_NAME_BASE, cnb));
        Element moduleTypeEl = createTypeElement(dataDoc, moduleType);
        if (moduleTypeEl != null) {
            dataEl.appendChild(moduleTypeEl);
        }
        dataEl.appendChild(createModuleElement(dataDoc, MODULE_DEPENDENCIES));
        dataEl.appendChild(createModuleElement(dataDoc, PUBLIC_PACKAGES));
        
        // store document to disk
        ProjectXMLManager.safelyWrite(projectXml, prjDoc);
    }
    
    private static Element createTypeElement(Document dataDoc, NbModuleType type) {
        Element result = null;
        if (type == NbModuleTypeProvider.STANDALONE) {
            result = createModuleElement(dataDoc, STANDALONE);
        } else if (type == NbModuleTypeProvider.SUITE_COMPONENT) {
            result = createModuleElement(dataDoc, SUITE_COMPONENT);
        }
        return result;
    }
    
    /**
     * Generates a basic <em>project.xml</em> templates into the given
     * <code>projectXml</code> for <em>Suite</em>.
     */
    public static void generateEmptySuiteTemplate(FileObject projectXml, String name) throws IOException {
        // XXX this method could be moved in a future (depends on how complex
        // suite's project.xml will be) to the .suite package dedicated class
        Document prjDoc = XMLUtil.createDocument("project", PROJECT_NS, null, null); // NOI18N
        
        // generate general project elements
        Element typeEl = prjDoc.createElementNS(PROJECT_NS, "type"); // NOI18N
        typeEl.appendChild(prjDoc.createTextNode(SuiteProjectType.TYPE));
        prjDoc.getDocumentElement().appendChild(typeEl);
        Element confEl = prjDoc.createElementNS(PROJECT_NS, "configuration"); // NOI18N
        prjDoc.getDocumentElement().appendChild(confEl);
        
        // generate NB Suite project type specific elements
        Element dataEl = createSuiteElement(confEl.getOwnerDocument(), DATA);
        confEl.appendChild(dataEl);
        Document dataDoc = dataEl.getOwnerDocument();
        dataEl.appendChild(createSuiteElement(dataDoc, "name", name)); // NOI18N
        
        // store document to disk
        ProjectXMLManager.safelyWrite(projectXml, prjDoc);
    }
    
    private static void safelyWrite(FileObject projectXml, Document prjDoc) throws IOException {
        FileLock lock = projectXml.lock();
        try {
            OutputStream os = projectXml.getOutputStream(lock);
            try {
                XMLUtil.write(prjDoc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private Element getConfData() {
        if (confData == null) {
            confData = helper.getPrimaryConfigurationData(true);
        }
        return confData;
    }
    
}
