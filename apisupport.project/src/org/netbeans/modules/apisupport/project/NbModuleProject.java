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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
final class NbModuleProject implements Project {
    
    private static final URL BUILD_XSL = NbModuleProject.class.getResource("resources/build.xsl");
    private static final URL BUILD_IMPL_XSL = NbModuleProject.class.getResource("resources/build-impl.xsl");
    
    private final AntProjectHelper helper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private String displayName;
    
    NbModuleProject(AntProjectHelper helper) {
        this.helper = helper;
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = Lookups.fixed(new Object[] {
            helper.createExtensibleMetadataProvider(),
            new SavedHook(),
            new OpenedHook(),
            createActionProvider(),
            new ClassPathProviderImpl(),
            new SourceForBinary(),
            // XXX need, in rough descending order of importance:
            // LogicalViewProvider
            // SubprojectProvider - special impl
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
        String manifestMf = helper.evaluate("manifest.mf"); // NOI18N
        if (manifestMf == null) {
            manifestMf = "manifest.mf"; // NOI18N
        }
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
    
    private FileObject getSourceDirectory() {
        String srcDir = helper.evaluate("src.dir"); // NOI18N
        if (srcDir == null) {
            srcDir = "src"; // NOI18N
        }
        return helper.resolveFileObject(srcDir);
    }
    
    private boolean supportsJavadoc() {
        return supportsFeature("javadoc"); // NOI18N
    }
    
    private boolean supportsUnitTests() {
        return supportsFeature("unit-tests"); // NOI18N
    }
    
    private boolean supportsFeature(String name) {
        Element config = helper.getPrimaryConfigurationData(true);
        NodeList nl = config.getElementsByTagNameNS(NbModuleProjectType.NAMESPACE_SHARED, name);
        int length = nl.getLength();
        assert length < 2;
        return length == 1;
    }
    
    private ActionProvider createActionProvider() {
        Map/*<String,String[]>*/ commands = new HashMap();
        commands.put(ActionProvider.COMMAND_BUILD, new String[] {"netbeans"}); // NOI18N
        commands.put(ActionProvider.COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(ActionProvider.COMMAND_REBUILD, new String[] {"clean", "netbeans"}); // NOI18N
        if (supportsJavadoc()) {
            commands.put("javadoc", new String[] {"javadoc-nb"}); // NOI18N
        }
        if (supportsUnitTests()) {
            commands.put("test", new String[] {"test"}); // NOI18N
        }
        // XXX other commands, e.g. reload, nbm, *single*, ...
        return helper.createActionProvider(commands, /*XXX*/null);
    }
    
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                BUILD_IMPL_XSL, false);
            genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_XML_PATH,
                BUILD_XSL, false);
        }
        
    }
    
    private final class OpenedHook extends ProjectOpenedHook {
        
        OpenedHook() {}
        
        protected void projectOpened() {
            try {
                genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    BUILD_IMPL_XSL, true);
                genFilesHelper.refreshBuildScript(GeneratedFilesHelper.BUILD_XML_PATH,
                    BUILD_XSL, true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
        protected void projectClosed() {
            // ignore for now
            // XXX could discard caches, etc.
        }
        
    }
    
    private final class ClassPathProviderImpl implements ClassPathProvider {
        
        private ClassPath compile, source, boot;
        
        ClassPathProviderImpl() {}
        
        public ClassPath findClassPath(FileObject file, String type) {
            FileObject srcDir = getSourceDirectory();
            if (srcDir == null) {
                return null;
            }
            if (!FileUtil.isParentOf(srcDir, file)) {
                // XXX deal with tests too
                return null;
            }
            // XXX listen to changes, etc.
            if (type.equals(ClassPath.COMPILE) || type.equals(ClassPath.EXECUTE)) {
                // Should both be the same, hopefully. <run-dependency> in project.xml
                // means that the module should be enabled, but this module need not
                // be able to access its classes. <compile-dependency> is what we care about.
                if (compile == null) {
                    compile = createCompileClasspath();
                    System.err.println("compile-time classpath for " + getName() + ": " + compile);//XXX
                }
                return compile;
            } else if (type.equals(ClassPath.SOURCE)) {
                if (source == null) {
                    source = ClassPathSupport.createClassPath(new FileObject[] {srcDir});
                }
                return source;
            } else if (type.equals(ClassPath.BOOT)) {
                if (boot == null) {
                    JavaPlatformManager pm = JavaPlatformManager.getDefault();
                    JavaPlatform jdk = pm.getDefaultPlatform();
                    boot = jdk.getBootstrapLibraries();
                }
                return boot;
            } else {
                // XXX JAVADOC?
                return null;
            }
        }
        
        private ClassPath createCompileClasspath() {
            ModuleList ml = ModuleList.getDefault();
            Element data = helper.getPrimaryConfigurationData(true);
            Element moduleDependencies = Util.findElement(data,
                "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
            List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
            Iterator it = deps.iterator();
            List/*<PathResourceImplementation>*/ entries = new ArrayList();
            String nbrootRel = helper.evaluate("nbroot"); // NOI18N
            File nbroot = helper.resolveFile(nbrootRel);
            while (it.hasNext()) {
                Element dep = (Element)it.next();
                if (Util.findElement(dep, "compile-dependency", // NOI18N
                        NbModuleProjectType.NAMESPACE_SHARED) == null) {
                    continue;
                }
                Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED);
                String cnb = Util.findText(cnbEl);
                ModuleList.Entry module = ml.getEntry(cnb);
                if (module == null) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + getName());
                    continue;
                }
                File moduleJar = module.getJarLocation(nbroot);
                try {
                    // XXX creating a jar:file:/tmp/foo.jar!/ URL does *not* yet
                    // work; bug in URLMapper (fixed in trunk but not in base tag yet):
                    // java.lang.NullPointerException
                    //         at org.openide.filesystems.AbstractFileSystem.findResource(AbstractFileSystem.java:140)
                    //         at org.openide.filesystems.URLMapper$DefaultURLMapper.geFileObjectBasicImpl(URLMapper.java:240)
                    //         at org.openide.filesystems.URLMapper$DefaultURLMapper.getFileObjects(URLMapper.java:148)
                    //         at org.openide.filesystems.URLMapper.findFileObjects(URLMapper.java:108)
                    //         at org.netbeans.api.java.classpath.ClassPath$Entry.getRoot(ClassPath.java:449)
                    // Cf. also hack in ClassPath.getJarRoot which automagically translates
                    // file:/tmp/foo.jar to the root of some JarFileSystem; i.e. duplicating what
                    // URLMapper is already supposed to be doing.
                    entries.add(ClassPathSupport.createResource(moduleJar.toURI().toURL()));
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            // XXX add ${cp.extra}
            return ClassPathSupport.createClassPath(entries);
        }
        
    }
    
    private final class SourceForBinary implements SourceForBinaryQueryImplementation {
        
        private URL moduleJarUrl;
        
        SourceForBinary() {}
        
        public FileObject[] findSourceRoot(URL binaryRoot) {
            //System.err.println("findSourceRoot: " + binaryRoot);
            // XXX handle also jar: URLs here
            if (binaryRoot.equals(getModuleJarUrl())) {
                FileObject srcDir = getSourceDirectory();
                //System.err.println("\t-> " + srcDir);
                return new FileObject[] {srcDir};
            }
            // XXX handle also tests, and build/classes dir
            return new FileObject[0];
        }
        
        private URL getModuleJarUrl() {
            if (moduleJarUrl == null) {
                String moduleJarDir = helper.evaluate("module.jar.dir");
                if (moduleJarDir == null) {
                    moduleJarDir = "modules";
                }
                // XXX handle also other possible substitutions - most easily with better support in APH
                File actualJar = new File(new File(new File(
                            FileUtil.toFile(getProjectDirectory()),
                            "netbeans"), // NOI18N
                        moduleJarDir.replace('/', File.separatorChar)),
                    getName().replace('.', '-') + ".jar"); // NOI18N
                try {
                    moduleJarUrl = actualJar.toURI().toURL();
                } catch (MalformedURLException e) {
                    ErrorManager.getDefault().notify(e);
                }
                //System.err.println("Module JAR: " + moduleJarUrl);
            }
            return moduleJarUrl;
        }
        
    }
    
}
