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

package org.netbeans.api.project.ant;

import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Allows extending the project's build script with 3rd party additions.
 * Check the Project's lookup to see if the feature is supported by a given Ant project type.
 * Typical usage:
 * <ul>
 *    <li>Lookup the instance of AntBuildExtender in the project at hand</li>
 *    <li>Create the external build script file with your targets and configuration</li>
 *    <li>Use the AntBuildExtender to wire your script and targets into the main build lifecycle</li>
 *    <li>Call {@link org.netbeans.api.project.ProjectManager#saveProject} to persist the changes and
 *        regenerate the main build script</li>
 * </ul>
 * 
 * Please note that it's easy to break the build script functionality and any script extensions
 * shall be done with care. A few rules to follow:
 * <ul>
 *   <li>Pick a reasonably unique extension id</li>
 *   <li>Prefix target names and properties you define in your extension with the extension id to prevent clashes.</li>
 * </ul>
 * @author mkleint
 * @since org.netbeans.modules.project.ant 1.16
 */
public final class AntBuildExtender {
    private HashMap<String, Extension> extensions;
    private AntBuildExtenderImplementation implementation;
    
    static {
        AntBuildExtenderAccessorImpl.createAccesor();
    }

    AntBuildExtender(AntBuildExtenderImplementation implementation) {
        this.implementation = implementation;
    }
    
    /**
     * Get a list of target names in the main build script that are allowed to be 
     * extended by adding the "depends" attribute definition to them.
     * @return list of target names
     */
    public List<String> getExtensibleTargets() {
        List<String> targets = new ArrayList<String>();
        targets.addAll(implementation.getExtensibleTargets());
        targets = Collections.unmodifiableList(targets);
        return targets;
    }
    
    /**
     * Adds a new build script extension.
     * @param id identification of the extension
     * @param extensionXml fileobject referencing the build script for the extension, 
     * needs to be located in nbproject directory or below.
     * @return the newly created extension.
     */
    public synchronized Extension addExtension(String id, FileObject extensionXml) {
        assert extensionXml != null;
        assert extensionXml.isValid() && extensionXml.isData();
        //TODO assert the owner is the same as the owner of this instance of entender.
        assert FileOwnerQuery.getOwner(extensionXml) == implementation.getOwningProject();
        FileObject nbproj = implementation.getOwningProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH).getParent();
        assert FileUtil.isParentOf(nbproj, extensionXml);
        if (extensions == null) {
            readProjectMetadata();
        }
        if (extensions.get(id) != null) {
            throw new IllegalStateException("Extension with id '" + id + "' already exists.");
        }
        Extension ex = new Extension(id, extensionXml, FileUtil.getRelativePath(nbproj, extensionXml));
        extensions.put(id, ex);
        updateProjectMetadata();
        return ex;
    }
    /**
     * Remove an existing build script extension. Make sure to remove the extension's script file
     * before/after removing the extension.
     * @param id identification of the extension
     */
    public synchronized void removeExtension(String id) {
        if (extensions == null) {
            readProjectMetadata();
        }
        if (extensions.get(id) == null) {
            // oh well, just ignore.
            return;
        }
        extensions.remove(id);
        updateProjectMetadata();
    }
    
    /**
     * Get an extension by the id.
     * @param id identification token
     * @return Extention with the given id or null if not found.
     */
    public synchronized Extension getExtension(String id) {
        if (extensions == null) {
            readProjectMetadata();
        }
        return extensions.get(id);
    }


    synchronized Set<Extension> getExtensions() {
        Set<Extension> ext =  new HashSet<Extension>();
        if (extensions == null) {
            readProjectMetadata();
        }
        ext.addAll(extensions.values());
        return ext;
    }
    
    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Document createNewDocument() {
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            return db.newDocument();
        }
    }
    
    
    private void updateProjectMetadata() {
        Document doc = createNewDocument();
        Element root = doc.createElementNS(AntBuildExtenderImplementation.AUX_NAMESPACE, 
                                           AntBuildExtenderImplementation.ELEMENT_ROOT);
        if (extensions  != null) {
            FileObject nbproj = implementation.getOwningProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH).getParent();
            for (Extension ext : extensions.values()) {
                Element child = doc.createElement(AntBuildExtenderImplementation.ELEMENT_EXTENSION);
                child.setAttribute(AntBuildExtenderImplementation.ATTR_ID, ext.id);
                String relPath = FileUtil.getRelativePath(nbproj, ext.file);
                assert relPath != null;
                child.setAttribute(AntBuildExtenderImplementation.ATTR_FILE, relPath);
                root.appendChild(child);
                for (String target : ext.dependencies.keySet()) {
                    for (String depTarget : ext.dependencies.get(target)) {
                        Element dep = doc.createElement(AntBuildExtenderImplementation.ELEMENT_DEPENDENCY);
                        dep.setAttribute(AntBuildExtenderImplementation.ATTR_TARGET, target);
                        dep.setAttribute(AntBuildExtenderImplementation.ATTR_DEPENDSON, depTarget);
                        child.appendChild(dep);
                    }
                }
            }
        }
        AuxiliaryConfiguration config = implementation.getOwningProject().getLookup().lookup(AuxiliaryConfiguration.class);
        config.putConfigurationFragment(root, true);
    }
    
    private void readProjectMetadata() {
        AuxiliaryConfiguration config = implementation.getOwningProject().getLookup().lookup(AuxiliaryConfiguration.class);
        Element cfgEl = config.getConfigurationFragment(AntBuildExtenderImplementation.ELEMENT_ROOT, AntBuildExtenderImplementation.AUX_NAMESPACE, true);
        FileObject nbproj = implementation.getOwningProject().getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH).getParent();
        extensions = new HashMap<String, Extension>();
        if (cfgEl != null) {
            String namespace = cfgEl.getNamespaceURI();
            NodeList roots = cfgEl.getElementsByTagNameNS(namespace, AntBuildExtenderImplementation.ELEMENT_EXTENSION);
            for (int i=0; i <roots.getLength(); i++) {
                Element root = (Element) roots.item(i);
                String id = root.getAttribute(AntBuildExtenderImplementation.ATTR_ID);
                assert id.length() > 0 : "Illegal project.xml";
                String value = root.getAttribute(AntBuildExtenderImplementation.ATTR_FILE);
                FileObject script = nbproj.getFileObject(value);
                assert script != null : "Missing file " + script;
                Extension ext = new Extension(id, script, value);
                extensions.put(id, ext);
                NodeList deps = root.getElementsByTagNameNS(namespace, AntBuildExtenderImplementation.ELEMENT_DEPENDENCY);
                for (int j = 0; j < deps.getLength(); j++) {
                    Element dep = (Element)deps.item(j);
                    String target = dep.getAttribute(AntBuildExtenderImplementation.ATTR_TARGET);
                    String dependsOn = dep.getAttribute(AntBuildExtenderImplementation.ATTR_DEPENDSON);
                    assert target != null;
                    assert dependsOn != null;
                    ext.loadDependency(target, dependsOn);
                }
            }
        }
    }
    
    /**
     * Describes and allows to manipulate the build script extension and it's links to the main build script
     * of the project.
     */
    public final class Extension {
        String id;
        FileObject file;
        String path;
        TreeMap<String, Collection<String>> dependencies;
        
        Extension(String id, FileObject script, String relPath) {
            this.id = id;
            file = script;
            path = relPath;
            dependencies = new TreeMap<String, Collection<String>>();
        }

        String getPath() {
            return path;
        }
        
        /**
         * Add a dependency of a main build script target on the target in the extension's script.
         * @param mainBuildTarget name of target in the main build script (see {@link org.netbeans.api.project.ant.AntBuildExtender#getExtendableTargets})
         * @param extensionTarget name of target in the extention script
         */
        public void addDependency(String mainBuildTarget, String extensionTarget) {
            assert implementation.getExtensibleTargets().contains(mainBuildTarget) : 
                "The target '" + mainBuildTarget + "' is not designated by the project type as extensible.";
            synchronized (AntBuildExtender.class) {
                loadDependency(mainBuildTarget, extensionTarget);
                updateProjectMetadata();
            }
        }
        
        private void loadDependency(String mainBuildTarget, String extensionTarget) {
            synchronized (AntBuildExtender.class) {
                Collection<String> tars = dependencies.get(mainBuildTarget);
                if (tars == null) {
                    tars = new ArrayList<String>();
                    dependencies.put(mainBuildTarget, tars);
                }
                if (!tars.contains(extensionTarget)) {
                    tars.add(extensionTarget);
                } else {
                    //log?
                }
            }
        }
        
        
        /**
         * Remove a dependency of a main build script target on the target in the extension's script.
         * 
         * @param mainBuildTarget name of target in the main build script (see {@link org.netbeans.api.project.ant.AntBuildExtender#getExtendableTargets})
         * @param extensionTarget name of target in the extention script
         */
        public void removeDependency(String mainBuildTarget, String extensionTarget) {
            Collection<String> str = dependencies.get(mainBuildTarget);
            if (str != null) {
                str.remove(extensionTarget);
                updateProjectMetadata();
            } else {
                //oh well, just ignore, nothing to update anyway..
            }
        }

        Map<String, Collection<String>> getDependencies() {
            TreeMap<String, Collection<String>> toRet = new TreeMap<String, Collection<String>>();
            synchronized (AntBuildExtender.class) {
                for (String str : dependencies.keySet()) {
                    ArrayList<String> col = new ArrayList<String>();
                    col.addAll(dependencies.get(str));
                    toRet.put(str, col);
                }
            }
            return toRet;
        }
    }
}
