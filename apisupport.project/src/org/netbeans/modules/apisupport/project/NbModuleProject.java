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
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Manifest;
import org.netbeans.api.project.Project;
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
    
}
