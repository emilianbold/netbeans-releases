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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectInformation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
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
    
    private static final Icon NB_PROJECT_ICON = new ImageIcon(Utilities.loadImage( "org/netbeans/modules/apisupport/project/resources/module.gif")); // NOI18N
    
    private final AntProjectHelper helper;
    private final Lookup lookup;
    private final ModuleList moduleList;
    private Map/*<String,String>*/ evalPredefs;
    private List/*<Map<String,String>>*/ evalDefs;
    
    NbModuleProject(AntProjectHelper helper) {
        this.helper = helper;
        Util.err.log("Loading project in " + getProjectDirectory());
        File nbroot = helper.resolveFile(getNbrootRel());
        moduleList = ModuleList.getModuleList(nbroot);
        FileBuiltQueryImplementation fileBuilt;
        if (supportsUnitTests()) {
            fileBuilt = helper.createGlobFileBuiltQuery(new String[] {
                // Can't currently include the ${props} directly in the string,
                // since APH does not yet grok optional properties.
                evaluate("src.dir") + "/*.java", // NOI18N
                evaluate("test.unit.src.dir") + "/*.java", // NOI18N
            }, new String[] {
                evaluate("build.classes.dir") + "/*.class", // NOI18N
                evaluate("build.test.unit.classes.dir") + "/*.class", // NOI18N
            });
        } else {
            fileBuilt = helper.createGlobFileBuiltQuery(new String[] {
                evaluate("src.dir") + "/*.java", // NOI18N
            }, new String[] {
                evaluate("build.classes.dir") + "/*.class", // NOI18N
            });
        }
        lookup = Lookups.fixed(new Object[] {
            new Info(),
            helper.createAuxiliaryConfiguration(),
            helper.createCacheDirectoryProvider(),
            //new SavedHook(),
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
            helper.createSharabilityQuery(new String[0], new String[] {
                // XXX replace with properties when possible using APH
                "build", // NOI18N
                "javadoc", // NOI18N
            }),
            // XXX need, in rough descending order of importance:
            // AntArtifactProvider - should it run netbeans target, or all-foo/bar?
            // CustomizerProvider - ???
        });
    }
    
    public String toString() {
        return "NbModuleProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    FileObject getManifestFile() {
        return helper.resolveFileObject(evaluate("manifest.mf")); // NOI18N
    }
    
    private Manifest getManifest() {
        FileObject manifestFO = getManifestFile();
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
        defaults.put("code.name.base.dashes", helper.getName().replace('.', '-')); // NOI18N
        defaults.put("module.jar.dir", "${nb.modules.dir}"); // NOI18N
        defaults.put("module.jar.basename", "${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${module.jar.basename}"); // NOI18N
        defaults.put("manifest.mf", "manifest.mf"); // NOI18N
        defaults.put("src.dir", "src"); // NOI18N
        defaults.put("build.classes.dir", "build/classes"); // NOI18N
        defaults.put("test.unit.src.dir", "test/unit/src"); // NOI18N
        defaults.put("build.test.unit.classes.dir", "build/test/unit/classes"); // NOI18N
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
        String testSrcDir = evaluate("test.unit.src.dir"); // NOI18N
        return helper.resolveFileObject(testSrcDir);
    }
    
    FileObject getFunctionalTestSourceDirectory() {
        // Hardcode location for now. XXX could make it based on some properties.
        return helper.resolveFileObject("test/qa-functional/src"); // NOI18N
    }
    
    File getTestClassesDirectory() {
        String testClassesDir = evaluate("build.test.unit.classes.dir"); // NOI18N
        return helper.resolveFile(testClassesDir);
    }
    
    FileObject getJavaHelpDirectory() {
        if (helper.resolveFileObject("javahelp/manifest.mf") != null) { // NOI18N
            // Special hack for core.
            return null;
        }
        return helper.resolveFileObject("javahelp"); // NOI18N
    }
    
    File getModuleJarLocation() {
        return helper.resolveFile(evaluateString("${netbeans.dest.dir}/${cluster.dir}/${module.jar}")); // NOI18N
    }
    
    URL getModuleJavadocDirectoryURL() {
        String moduleJavadoc = "javadoc/" + evaluate("javadoc.name"); // NOI18N
        File f = helper.resolveFile(moduleJavadoc);
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
            return null;
        }
    }
    
    String getPath() {
        Element config = helper.getPrimaryConfigurationData(true);
        Element path = Util.findElement(config, "path", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (path != null) {
            return Util.findText(path);
        } else {
            return null;
        }
    }
    
    private String getNbrootRel() {
        String path = getPath();
        if (path != null) {
            return path.replaceAll("[^/]+", ".."); // NOI18N
        }
        Util.err.log(ErrorManager.WARNING, "Could not compute relative path to nb_all for " + this);
        return ".."; // NOI18N
    }
    
    FileObject getNbroot() {
        String nbrootRel = getNbrootRel();
        FileObject nbroot = getHelper().resolveFileObject(nbrootRel);
        if (nbroot == null) {
            Util.err.log(ErrorManager.WARNING, "Warning - cannot find nb_all for " + this);
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
        return getTestSourceDirectory() != null;
    }
    
    private boolean supportsFeature(String name) {
        Element config = helper.getPrimaryConfigurationData(true);
        NodeList nl = config.getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED, name);
        int length = nl.getLength();
        assert length < 2;
        return length == 1;
    }
    
    private final class Info implements ProjectInformation {
        
        private String displayName;
        
        Info() {}
        
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
                if (displayName.equals("org.netbeans")) { // NOI18N
                    // Special case.
                    displayName = "Core Bootstrap";
                }
            }
            return displayName;
        }
        
        public Icon getIcon() {
            return NB_PROJECT_ICON;
        }
        
        public Project getProject() {
            return NbModuleProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        
    }
    
    private final class OpenedHook extends ProjectOpenedHook {
        
        OpenedHook() {}
        
        protected void projectOpened() {
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
