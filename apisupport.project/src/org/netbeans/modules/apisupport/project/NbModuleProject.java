/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.GlobFileBuiltQuery;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
final class NbModuleProject implements Project {
    
    private static final Image NB_PROJECT_ICON = Utilities.loadImage( "org/netbeans/modules/apisupport/project/resources/module.gif" ); // NOI18N
    
    private static final String BUILD_XSL = "nbbuild/templates/build.xsl";

    private final AntProjectHelper helper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private String displayName;
    private final ModuleList moduleList;
    private Map/*<String,String>*/ evalPredefs;
    private List/*<Map<String,String>>*/ evalDefs;
    
    NbModuleProject(AntProjectHelper helper) {
        this.helper = helper;
        File nbroot = helper.resolveFile(getNbrootRel());
        moduleList = ModuleList.getModuleList(nbroot);
        genFilesHelper = new GeneratedFilesHelper(helper);
        FileBuiltQueryImplementation fileBuilt;
        if (supportsUnitTests()) {
            fileBuilt = new GlobFileBuiltQuery(helper, new String[] {
                // Can't currently include the ${props} directly in the string,
                // since APH does not yet grok optional properties.
                evaluate("src.dir") + "/*.java", // NOI18N
                evaluate("test.src.dir") + "/*.java", // NOI18N
            }, new String[] {
                evaluate("build.classes.dir") + "/*.class", // NOI18N
                evaluate("build.test.classes.dir") + "/*.class", // NOI18N
            });
        } else {
            fileBuilt = new GlobFileBuiltQuery(helper, new String[] {
                evaluate("src.dir") + "/*.java", // NOI18N
            }, new String[] {
                evaluate("build.classes.dir") + "/*.class", // NOI18N
            });
        }
        lookup = Lookups.fixed(new Object[] {
            helper.createExtensibleMetadataProvider(),
            new SavedHook(),
            new OpenedHook(),
            new Actions(this),
            new ClassPathProviderImpl(this),
            new SourceForBinaryImpl(this),
            new JavadocForBinaryQueryImpl(this),
            new UnitTestForSourceQueryImpl(this),
            new LogicalView(this),
            new SubprojectProviderImpl(this),
            fileBuilt,
            new AccessibilityQueryImpl(this),
            // XXX need, in rough descending order of importance:
            // AntArtifactProvider - should it run netbeans target, or all-foo/bar?
            // CustomizerProvider - ???
        });
    }
    
    public String getName() {
        return helper.getName();
    }
    
    public String getDisplayName() {
        if (displayName == null) {
            Manifest mf = getManifest();
            if (mf != null) {
                String locBundleResource = mf.getMainAttributes().
                    getValue("OpenIDE-Module-Localizing-Bundle"); // NOI18N
                if (locBundleResource != null) {
                    String locBundleResourceBase, locBundleResourceExt;
                    int idx = locBundleResource.lastIndexOf('.');
                    if (idx != -1 && idx > locBundleResource.lastIndexOf('/')) {
                        locBundleResourceBase = locBundleResource.substring(0, idx);
                        locBundleResourceExt = locBundleResource.substring(idx);
                    } else {
                        locBundleResourceBase = locBundleResource;
                        locBundleResourceExt = "";
                    }
                    FileObject srcFO = getSourceDirectory();
                    if (srcFO != null) {
                        Iterator it = NbBundle.getLocalizingSuffixes();
                        while (it.hasNext()) {
                            String suffix = (String)it.next();
                            String resource = locBundleResourceBase + suffix +
                                locBundleResourceExt;
                            FileObject bundleFO = srcFO.getFileObject(resource);
                            if (bundleFO != null) {
                                Properties p = new Properties();
                                try {
                                    InputStream is = bundleFO.getInputStream();
                                    try {
                                        p.load(is);
                                    } finally {
                                        is.close();
                                    }
                                    displayName = p.getProperty("OpenIDE-Module-Name"); // NOI18N
                                    if (displayName != null) {
                                        break;
                                    }
                                } catch (IOException e) {
                                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (displayName == null) {
            displayName = getName();
        }
        return displayName;
    }
    
    public Image getIcon() {
        return NB_PROJECT_ICON;
    }
    
    public String toString() {
        return "NbModuleProject[" + getName() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
    
    private Manifest getManifest() {
        String manifestMf = evaluate("manifest.mf"); // NOI18N
        FileObject manifestFO = helper.resolveFileObject(manifestMf);
        if (manifestFO != null) {
            try {
                InputStream is = manifestFO.getInputStream();
                try {
                    return new Manifest(is);
                } finally {
                    is.close();
                }
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return null;
    }
    
    AntProjectHelper getHelper() {
        return helper;
    }

    /**
     * Replacement for AntProjectHelper.evaluate for NbModuleProject.
     * Takes into account default values for various properties.
     */
    String evaluate(String prop) {
        return PropertyUtils.evaluate(prop, makeEvalPredefs(), makeEvalDefs());
    }
    
    /**
     * Replacement for AntProjectHelper.evaluateString for NbModuleProject.
     * Takes into account default values for various properties.
     */
    String evaluateString(String text) {
        return PropertyUtils.evaluateString(text, makeEvalPredefs(), makeEvalDefs());
    }
    
    /**
     * Create stock predefs: ${basedir} and system properties.
     */
    private Map/*<String,String>*/ makeEvalPredefs() {
        if (evalPredefs == null) {
            evalPredefs = new HashMap();
            evalPredefs.putAll(System.getProperties());
            evalPredefs.put("basedir", FileUtil.toFile(getProjectDirectory()).getAbsolutePath()); // NOI18N
        }
        return evalPredefs;
    }
    
    /**
     * Create stock defs: private project props, shared project props, defaults.
     * Synch with nbbuild/templates/projectized.xml.
     */
    private List/*<Map<String,String>>*/ makeEvalDefs() {
        if (evalDefs != null) {
            return evalDefs;
        }
        EditableProperties priv = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        EditableProperties proj = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Map/*<String,String>*/ stock = new HashMap();
        stock.put("nb_all", getNbrootRel()); // NOI18N
        Iterator it = getModuleList().getAllEntries().iterator();
        while (it.hasNext()) {
            stock.put(((ModuleList.Entry)it.next()).getPath() + ".dir", "${nb_all}/nbbuild/netbeans"); // NOI18N
        }
        String[] dirs = {
            "modules", // NOI18N
            "modules/autoload", // NOI18N
            "modules/eager", // NOI18N
            "lib", // NOI18N
            "lib/ext", // NOI18N
        };
        for (int i = 0; i < dirs.length; i++) {
            stock.put("nb." + dirs[i] + ".dir", dirs[i]); // NOI18N
        }
        stock.put("netbeans.dest.dir", "${nb_all}/nbbuild"); // NOI18N
        stock.put("cluster.dir", "netbeans"); // NOI18N
        Map/*<String,String>*/ defaults = new HashMap();
        defaults.put("code.name.base.dashes", getName().replace('.', '-')); // NOI18N
        if ("true".equals(proj.getProperty("is.autoload"))) { // NOI18N
            defaults.put("module.jar.dir", "${nb.modules/autoload.dir}"); // NOI18N
        } else if ("true".equals(proj.getProperty("is.eager"))) { // NOI18N
            defaults.put("module.jar.dir", "${nb.modules/eager.dir}"); // NOI18N
        } else {
            defaults.put("module.jar.dir", "${nb.modules.dir}"); // NOI18N
        }
        defaults.put("module.jar.basename", "${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${module.jar.basename}"); // NOI18N
        defaults.put("manifest.mf", "manifest.mf"); // NOI18N
        defaults.put("src.dir", "src"); // NOI18N
        defaults.put("build.classes.dir", "build/classes"); // NOI18N
        if (supportsUnitTests()) {
            defaults.put("test.src.dir", "test/unit/src"); // NOI18N
            defaults.put("build.test.classes.dir", "build/test/classes"); // NOI18N
        }
        // skip a bunch of properties irrelevant here - NBM stuff, etc.
        evalDefs = Arrays.asList(new Map/*<String,String>*/[] {
            stock,
            priv,
            proj,
            defaults,
        });
        return evalDefs;
    }
    
    FileObject getSourceDirectory() {
        String srcDir = evaluate("src.dir"); // NOI18N
        return helper.resolveFileObject(srcDir);
    }
    
    FileObject getTestSourceDirectory() {
        if (!supportsUnitTests()) {
            return null;
        }
        String testSrcDir = evaluate("test.src.dir"); // NOI18N
        return helper.resolveFileObject(testSrcDir);
    }
    
    File getTestClassesDirectory() {
        if (!supportsUnitTests()) {
            return null;
        }
        String testClassesDir = evaluate("build.test.classes.dir"); // NOI18N
        return helper.resolveFile(testClassesDir);
    }
    
    File getModuleJarLocation() {
        return helper.resolveFile(evaluateString("${netbeans.dest.dir}/${cluster.dir}/${module.jar}")); // NOI18N
    }
    
    FileObject getModuleJavadocDirectory() {
        String moduleJavadoc = "javadoc/" + evaluate("javadoc.name"); // NOI18N
        return helper.resolveFileObject(moduleJavadoc);
    }
    
    private String getNbrootRel() {
        Element config = helper.getPrimaryConfigurationData(true);
        Element path = Util.findElement(config, "path", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (path != null) {
            String pathS = Util.findText(path);
            if (pathS != null) {
                return pathS.replaceAll("[^/]+", ".."); // NOI18N
            }
        }
        Util.err.log(ErrorManager.WARNING, "Could not compute relative path to nb_all for " + getName());
        return ".."; // NOI18N
    }
    
    FileObject getNbroot() {
        String nbrootRel = getNbrootRel();
        FileObject nbroot = getHelper().resolveFileObject(nbrootRel);
        if (nbroot == null) {
            Util.err.log(ErrorManager.WARNING, "Warning - cannot find nb_all for " + getName());
        }
        return nbroot;
    }
    
    FileObject getNbrootFile(String path) {
        FileObject nbroot = getNbroot();
        if (nbroot != null) {
            return nbroot.getFileObject(path);
        } else {
            return null;
        }
    }
    
    ModuleList getModuleList() {
        return moduleList;
    }
    
    boolean supportsJavadoc() {
        return supportsFeature("javadoc"); // NOI18N
    }
    
    boolean supportsUnitTests() {
        return supportsFeature("unit-tests"); // NOI18N
    }
    
    private boolean supportsFeature(String name) {
        Element config = helper.getPrimaryConfigurationData(true);
        NodeList nl = config.getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED, name);
        int length = nl.getLength();
        assert length < 2;
        return length == 1;
    }
    
    private void refreshBuildScripts(boolean p) throws IOException {
        FileObject buildXsl = getNbrootFile(BUILD_XSL);
        if (buildXsl != null) {
            genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_XML_PATH,
                buildXsl.getURL(), p);
        }
    }
    
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        protected void projectXmlSaved() throws IOException {
            refreshBuildScripts(false);
        }
        
    }
    
    private final class OpenedHook extends ProjectOpenedHook {
        
        OpenedHook() {}
        
        protected void projectOpened() {
            try {
                refreshBuildScripts(true);
            } catch (IOException e) {
                Util.err.notify(e);
            }
            
            // register project's classpaths to GlobalClassPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
        protected void projectClosed() {
            try {
                ProjectManager.getDefault().saveProject(NbModuleProject.this);
            } catch (IOException e) {
                Util.err.notify(e);
            }
            
            // XXX could discard caches, etc.
            
            // unregister project's classpaths to GlobalClassPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        }
        
    }
    
}
