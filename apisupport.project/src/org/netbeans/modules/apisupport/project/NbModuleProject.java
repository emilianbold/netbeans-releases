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
    private static final String BUILD_IMPL_XSL = "nbbuild/templates/build-impl.xsl";

    private final AntProjectHelper helper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private String displayName;
    private final ModuleList moduleList;
    
    NbModuleProject(AntProjectHelper helper) {
        this.helper = helper;
        moduleList = new ModuleList(this);
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
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
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
     * Create stock predefs: ${basedir} and system properties.
     */
    private Map/*<String,String>*/ makeEvalPredefs() {
        Map/*<String,String>*/ m = new HashMap();
        m.putAll(System.getProperties());
        m.put("basedir", FileUtil.toFile(getProjectDirectory()).getAbsolutePath()); // NOI18N
        return m;
    }
    
    /**
     * Create stock defs: private project props, shared project props, defaults.
     * Synch with build-impl.xsl.
     */
    private List/*<Map<String,String>>*/ makeEvalDefs() {
        Map defaults = new HashMap();
        defaults.put("code.name.base.dashes", getName().replace('.', '-')); // NOI18N
        defaults.put("module.jar.dir", "modules"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("manifest.mf", "manifest.mf");
        defaults.put("src.dir", "src");
        defaults.put("build.classes.dir", "build/classes");
        if (supportsUnitTests()) {
            defaults.put("test.src.dir", "test/unit/src");
            defaults.put("build.test.classes.dir", "build/test/classes");
        }
        // skip a bunch of properties irrelevant here - NBM stuff, etc.
        return Arrays.asList(new Map/*<String,String>*/[] {
            helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
            helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH),
            defaults,
        });
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
        String moduleJar = "netbeans/" + evaluate("module.jar"); // NOI18N
        return helper.resolveFile(moduleJar);
    }
    
    FileObject getModuleJavadocDirectory() {
        String moduleJavadoc = "javadoc/" + evaluate("javadoc.name"); // NOI18N
        return helper.resolveFileObject(moduleJavadoc);
    }
    
    FileObject getNbroot() {
        String nbrootRel = evaluate("nbroot"); // NOI18N
        FileObject nbroot = getHelper().resolveFileObject(nbrootRel);
        if (nbroot == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - cannot find nbroot for " + getName());
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
        FileObject buildImplXsl = getNbrootFile(BUILD_IMPL_XSL);
        if (buildImplXsl != null) {
            genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                buildImplXsl.getURL(), p);
        }
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
                ErrorManager.getDefault().notify(e);
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
                ErrorManager.getDefault().notify(e);
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
