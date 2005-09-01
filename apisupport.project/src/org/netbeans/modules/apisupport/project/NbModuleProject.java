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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider.NbModuleType;
import org.netbeans.modules.apisupport.project.queries.ModuleProjectClassPathExtender;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.netbeans.modules.apisupport.project.queries.AccessibilityQueryImpl;
import org.netbeans.modules.apisupport.project.queries.UnitTestForSourceQueryImpl;
import org.netbeans.modules.apisupport.project.queries.SourceLevelQueryImpl;
import org.netbeans.modules.apisupport.project.queries.AntArtifactProviderImpl;
import org.netbeans.modules.apisupport.project.queries.ClassPathProviderImpl;
import org.netbeans.modules.apisupport.project.queries.JavadocForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.SourceForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.SubprojectProviderImpl;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.ui.ModuleActions;
import org.netbeans.modules.apisupport.project.ui.ModuleLogicalView;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
public final class NbModuleProject implements Project {
    
    private static final Icon NB_PROJECT_ICON = new ImageIcon(
        Utilities.loadImage( "org/netbeans/modules/apisupport/project/resources/module.gif")); // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private Map/*<String,String>*/ evalPredefs;
    private List/*<Map<String,String>>*/ evalDefs;
    private Map/*<FileObject,Element>*/ extraCompilationUnits;
    private final GeneratedFilesHelper genFilesHelper;
    private final NbModuleTypeProviderImpl typeProvider;
    
    private LocalizedBundleInfo bundleInfo;
    private String infoDisplayName;
    
    NbModuleProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        genFilesHelper = new GeneratedFilesHelper(helper);
        Util.err.log("Loading project in " + getProjectDirectory());
        if (getCodeNameBase() == null) {
            throw new IOException("Misconfigured project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " has no defined <code-name-base>"); // NOI18N
        }
        typeProvider = new NbModuleTypeProviderImpl();
        ModuleList ml = getModuleList();
        if (ml.getEntry(getCodeNameBase()) == null) {
            ModuleList.refresh();
            ml = getModuleList();
            if (ml.getEntry(getCodeNameBase()) == null) {
                // XXX try to give better diagnostics - as examples are discovered
                throw new IOException("Project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " does not appear to be listed in its own module list; some sort of misconfiguration (e.g. not listed in its own suite)"); // NOI18N
            }
        }
        eval = createEvaluator(ml);
        FileBuiltQueryImplementation fileBuilt;
        // XXX could add globs for other package roots too
        if (supportsUnitTests()) {
            fileBuilt = helper.createGlobFileBuiltQuery(eval, new String[] {
                "${src.dir}/*.java", // NOI18N
                "${test.unit.src.dir}/*.java", // NOI18N
            }, new String[] {
                "${build.classes.dir}/*.class", // NOI18N
                "${build.test.unit.classes.dir}/*.class", // NOI18N
            });
        } else {
            fileBuilt = helper.createGlobFileBuiltQuery(eval, new String[] {
                "${src.dir}/*.java", // NOI18N
            }, new String[] {
                "${build.classes.dir}/*.class", // NOI18N
            });
        }
        final SourcesHelper sourcesHelper = new SourcesHelper(helper, eval);
        // Temp build dir is always internal; NBM build products go elsewhere, but
        // difficult to predict statically exactly what they are!
        // XXX would be good to mark at least the module JAR as owned by this project
        // (currently FOQ/SH do not support that)
        sourcesHelper.addPrincipalSourceRoot("${src.dir}", NbBundle.getMessage(NbModuleProject.class, "LBL_source_packages"), null, null); // #56457
        sourcesHelper.addTypedSourceRoot("${src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(NbModuleProject.class, "LBL_source_packages"), null, null);
        // XXX other principal source roots, as needed...
        sourcesHelper.addTypedSourceRoot("${test.unit.src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(NbModuleProject.class, "LBL_unit_test_packages"), null, null);
        sourcesHelper.addTypedSourceRoot("${test.qa-functional.src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(NbModuleProject.class, "LBL_functional_test_packages"), null, null);
        sourcesHelper.addTypedSourceRoot("${test.qa-performance.src.dir}", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(NbModuleProject.class, "LBL_performance_test_packages"), null, null);
        // #42332: also any other misc. test dirs (just add source roots, no CP etc. for now)
        FileObject testDir = helper.getProjectDirectory().getFileObject("test"); // NOI18N
        if (testDir != null) {
            Enumeration/*<FileObject>*/ kids = testDir.getChildren(false);
            while (kids.hasMoreElements()) {
                FileObject testSubdir = (FileObject) kids.nextElement();
                if (!testSubdir.isFolder()) {
                    continue;
                }
                String name = testSubdir.getNameExt();
                if (testDir.getFileObject("build-" + name + ".xml") == null) { // NOI18N
                    continue;
                }
                if (name.equals("unit") || name.equals("qa-functional")) { // NOI18N
                    // Already handled specially.
                    continue;
                }
                sourcesHelper.addTypedSourceRoot("test/" + name + "/src", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(NbModuleProject.class, "LBL_unknown_test_packages", name), null, null);
            }
        }
        if (helper.resolveFileObject("javahelp/manifest.mf") == null) { // NOI18N
            // Special hack for core - ignore core/javahelp
            sourcesHelper.addTypedSourceRoot("javahelp", "javahelp", NbBundle.getMessage(NbModuleProject.class, "LBL_javahelp_packages"), null, null);
        }
        Iterator it = getExtraCompilationUnits().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Element ecu = (Element) entry.getValue();
            Element pkgrootEl = Util.findElement(ecu, "package-root", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
            String pkgrootS = Util.findText(pkgrootEl);
            FileObject pkgroot = (FileObject) entry.getKey();
            sourcesHelper.addTypedSourceRoot(pkgrootS, JavaProjectConstants.SOURCES_TYPE_JAVA, /* XXX should schema incl. display name? */pkgroot.getNameExt(), null, null);
        }
        // #56457: support external source roots too.
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            public void run() {
                sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            }
        });
        Manifest mf = getManifest();
        FileObject srcFO = getSourceDirectory();
        if (mf != null && srcFO != null) {
            bundleInfo = Util.findLocalizedBundleInfo(srcFO, getManifest());
        }
        lookup = Lookups.fixed(new Object[] {
            new Info(),
            helper.createAuxiliaryConfiguration(),
            helper.createCacheDirectoryProvider(),
            new SavedHook(),
            new OpenedHook(),
            new ModuleActions(this),
            new ClassPathProviderImpl(this),
            new SourceForBinaryImpl(this),
            new JavadocForBinaryImpl(this),
            new UnitTestForSourceQueryImpl(this),
            new ModuleLogicalView(this),
            new SubprojectProviderImpl(this),
            fileBuilt,
            new AccessibilityQueryImpl(this),
            new SourceLevelQueryImpl(this, evaluator()),
            helper.createSharabilityQuery(evaluator(), new String[0], new String[] {
                // currently these are hardcoded
                "build", // NOI18N
            }),
            sourcesHelper.createSources(),
            new AntArtifactProviderImpl(this, helper, evaluator()),
            new CustomizerProviderImpl(this, getHelper(), evaluator(), bundleInfo),
            new SuiteProviderImpl(),
            typeProvider,
            new PrivilegedTemplatesImpl(),
            new ModuleProjectClassPathExtender(this),
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
    
    /** Returns a relative path to a project's source directory. */
    public String getSourceDirectoryPath() {
        return evaluator().getProperty("src.dir"); // NOI18N
    }
    
    private NbModuleTypeProvider.NbModuleType getModuleType() {
        Element data = getHelper().getPrimaryConfigurationData(true);
        if (Util.findElement(data, "suite-component", NbModuleProjectType.NAMESPACE_SHARED) != null) { // NOI18N
            return NbModuleTypeProvider.SUITE_COMPONENT;
        } else if (Util.findElement(data, "standalone", NbModuleProjectType.NAMESPACE_SHARED) != null) { // NOI18N
            return NbModuleTypeProvider.STANDALONE;
        } else {
            return NbModuleTypeProvider.NETBEANS_ORG;
        }
    }
    
    public FileObject getManifestFile() {
        return helper.resolveFileObject(eval.getProperty("manifest.mf")); // NOI18N
    }
    
    public Manifest getManifest() {
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

    public AntProjectHelper getHelper() {
        return helper;
    }
    
    /**
     * Create a property evaluator: private project props, shared project props, various defaults.
     * Synch with nbbuild/templates/projectized.xml.
     */
    private PropertyEvaluator createEvaluator(ModuleList ml) {
        // XXX a lot of this duplicates ModuleList.parseProperties... can they be shared?
        PropertyProvider predefs = helper.getStockPropertyPreprovider();
        Map/*<String,String>*/ stock = new HashMap();
        File dir = FileUtil.toFile(getProjectDirectory());
        NbModuleTypeProvider.NbModuleType type = getModuleType();
        File nbroot = ModuleList.findNetBeansOrg(dir);
        assert type == NbModuleTypeProvider.NETBEANS_ORG ^ nbroot == null : dir;
        if (nbroot != null) {
            stock.put("nb_all", nbroot.getAbsolutePath()); // NOI18N
        }
        // Register *.dir for nb.org modules. There is no equivalent for external modules.
        Iterator it = ml.getAllEntries().iterator();
        while (it.hasNext()) {
            ModuleEntry e = (ModuleEntry) it.next();
            String nborgPath = e.getNetBeansOrgPath();
            if (nborgPath != null) {
                // #48449: intern these; number is (size of modules.xml) * (# of loaded module projects)
                stock.put((nborgPath + ".dir").intern(), e.getClusterDirectory().getAbsolutePath().intern()); // NOI18N
            }
        }
        ModuleEntry thisEntry = ml.getEntry(getCodeNameBase());
        assert thisEntry != null : "Cannot find myself";
        if (nbroot != null) {
            // Only needed for netbeans.org modules, since for external modules suite.properties suffices.
            stock.put("netbeans.dest.dir", thisEntry.getDestDir().getAbsolutePath()); // NOI18N
            assert thisEntry.getNetBeansOrgPath() != null : thisEntry;
        } else {
            assert thisEntry.getNetBeansOrgPath() == null : thisEntry;
        }
        File clusterDir = thisEntry.getClusterDirectory();
        stock.put("cluster", clusterDir.getAbsolutePath()); // NOI18N
        List/*<PropertyProvider>*/ providers = new ArrayList();
        providers.add(PropertyUtils.fixedPropertyProvider(stock));
        // XXX should listen to changes in values of properties which refer to property files:
        if (type == NbModuleTypeProvider.SUITE_COMPONENT) {
            providers.add(helper.getPropertyProvider("nbproject/private/suite-private.properties")); // NOI18N
            providers.add(helper.getPropertyProvider("nbproject/suite.properties")); // NOI18N
            PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            String suiteDirS = baseEval.getProperty("suite.dir"); // NOI18N
            if (suiteDirS != null) {
                File suiteDir = PropertyUtils.resolveFile(dir, suiteDirS);
                providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(suiteDir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
                providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(suiteDir, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
            }
        } else if (type == NbModuleTypeProvider.STANDALONE) {
            providers.add(helper.getPropertyProvider("nbproject/private/platform-private.properties")); // NOI18N
            providers.add(helper.getPropertyProvider("nbproject/platform.properties")); // NOI18N
        }
        if (type == NbModuleTypeProvider.SUITE_COMPONENT || type == NbModuleTypeProvider.STANDALONE) {
            PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            String buildS = baseEval.getProperty("user.properties.file"); // NOI18N
            if (buildS != null) {
                providers.add(PropertyUtils.propertiesFilePropertyProvider(PropertyUtils.resolveFile(dir, buildS)));
            } else {
                providers.add(PropertyUtils.globalPropertyProvider());
            }
            baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            String platformS = baseEval.getProperty("nbplatform.active"); // NOI18N
            if (platformS != null) {
                providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"))); // NOI18N
            }
        }
        providers.add(helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        providers.add(helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        Map/*<String,String>*/ defaults = new HashMap();
        defaults.put("code.name.base.dashes", getCodeNameBase().replace('.', '-')); // NOI18N
        defaults.put("module.jar.dir", "modules"); // NOI18N
        defaults.put("module.jar.basename", "${code.name.base.dashes}.jar"); // NOI18N
        defaults.put("module.jar", "${module.jar.dir}/${module.jar.basename}"); // NOI18N
        defaults.put("manifest.mf", "manifest.mf"); // NOI18N
        defaults.put("src.dir", "src"); // NOI18N
        defaults.put("build.classes.dir", "build/classes"); // NOI18N
        defaults.put("test.unit.src.dir", "test/unit/src"); // NOI18N
        defaults.put("test.qa-functional.src.dir", "test/qa-functional/src"); // NOI18N
        defaults.put("test.qa-performance.src.dir", "test/qa-performance/src"); // NOI18N
        defaults.put("build.test.unit.classes.dir", "build/test/unit/classes"); // NOI18N
        defaults.put("javac.source", "1.4"); // NOI18N
        providers.add(PropertyUtils.fixedPropertyProvider(defaults));
        providers.add(createModuleClasspathPropertyProvider());
        Map/*<String,String>*/ buildDefaults = new HashMap();
        buildDefaults.put("cp.extra", ""); // NOI18N
        buildDefaults.put("cp", "${module.classpath}:${cp.extra}"); // NOI18N
        buildDefaults.put("run.cp", "${cp}:${build.classes.dir}"); // NOI18N
        PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
        buildDefaults.put("junit.jar", findJunitJar(baseEval)); // NOI18N
        buildDefaults.put("nbjunit.jar", findNbJunitJar(baseEval)); // NOI18N
        String insaneLibJar = findInsaneLibJar(baseEval);
        if (insaneLibJar != null) {
            buildDefaults.put("insanelib.jar", insaneLibJar); // NOI18N
        }
        buildDefaults.put("test.unit.cp.extra", ""); // NOI18N
        buildDefaults.put("test.unit.cp", "${cp}:${cluster}/${module.jar}:${junit.jar}:${nbjunit.jar}:${insanelib.jar}:${test.unit.cp.extra}"); // NOI18N
        buildDefaults.put("test.unit.run.cp.extra", ""); // NOI18N
        buildDefaults.put("test.unit.run.cp", "${test.unit.cp}:${build.test.unit.classes.dir}:${test.unit.run.cp.extra}"); // NOI18N
        providers.add(PropertyUtils.fixedPropertyProvider(buildDefaults));
        // skip a bunch of properties irrelevant here - NBM stuff, etc.
        return PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
    }
    
    /**
     * Get an Ant location for the root of junit.jar.
     * Prefer the IDE's lib version; else use xtest/lib/junit.jar.
     */
    private String findJunitJar(PropertyEvaluator eval) {
        File f = InstalledFileLocator.getDefault().locate("modules/ext/junit-3.8.1.jar", "org.netbeans.modules.junit", false); // NOI18N
        if (f != null) {
            return f.getAbsolutePath();
        } else {
            f = getNbrootFile("xtest/lib/junit.jar", eval); // NOI18N
            if (f != null) {
                return f.getAbsolutePath();
            } else {
                // External module with no ref to nb.org sources.
                return "${netbeans.dest.dir}/ide6/modules/ext/junit-3.8.1.jar"; // NOI18N
            }
        }
    }
    
    /**
     * Get an Ant location for the root of nbjunit.jar.
     * Assume it is installed in the IDE.
     */
    private String findNbJunitJar(PropertyEvaluator eval) {
        String path = "testtools/modules/org-netbeans-modules-nbjunit.jar"; // NOI18N
        File f = getNbrootFile("nbbuild/netbeans/" + path, eval); // NOI18N
        if (f != null) {
            return f.getAbsolutePath();
        } else {
            // External module with no ref to nb.org sources.
            return "${netbeans.dest.dir}/" + path; // NOI18N
        }
    }
    
    /**
     * Get an Ant location for the root of insanelib.jar.
     * Currently will only work for netbeans.org modules.
     */
    private String findInsaneLibJar(PropertyEvaluator eval) {
        File f = getNbrootFile("performance/insanelib/dist/insanelib.jar", eval); // NOI18N
        if (f != null) {
            return f.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    private PropertyProvider createModuleClasspathPropertyProvider() {
        // Wraps computeModuleClasspath and refires changes in project.xml.
        class Provider implements PropertyProvider, AntProjectListener {
            private final Set/*<ChangeListener>*/ listeners = new HashSet();
            private String path;
            Provider() {
                path = computeModuleClasspath();
                getHelper().addAntProjectListener(this);
            }
            public Map getProperties() {
                return Collections.singletonMap("module.classpath", path); // NOI18N
            }
            public void addChangeListener(ChangeListener l) {
                synchronized (listeners) {
                    listeners.add(l);
                }
            }
            public void removeChangeListener(ChangeListener l) {
                synchronized (listeners) {
                    listeners.remove(l);
                }
            }
            private void maybeFireChange() {
                String newpath = computeModuleClasspath();
                if (!newpath.equals(path)) {
                    Util.err.log("module classpath for " + getProjectDirectory() + " changed to " + newpath);
                    path = newpath;
                    ChangeEvent e = new ChangeEvent(this);
                    Iterator it;
                    synchronized (listeners) {
                        it = new HashSet(listeners).iterator();
                    }
                    while (it.hasNext()) {
                        ((ChangeListener) it.next()).stateChanged(e);
                    }
                }
            }
            public void configurationXmlChanged(AntProjectEvent ev) {
                // type could be changed
                typeProvider.reset();
                // Module dependencies may have changed.
                maybeFireChange();
            }
            public void propertiesChanged(AntProjectEvent ev) {
                // XXX probably not relevant, right?
            }
        }
        return new Provider();
    }
    
    /**
     * Should be similar to impl in ParseProjectXml.
     */
    private String computeModuleClasspath() {
        ModuleList ml;
        try {
            ml = getModuleList();
        } catch (IOException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
            return "";
        }
        Element data = getHelper().getPrimaryConfigurationData(true);
        Element moduleDependencies = Util.findElement(data,
            "module-dependencies", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        List/*<Element>*/ deps = Util.findSubElements(moduleDependencies);
        Iterator it = deps.iterator();
        StringBuffer cp = new StringBuffer();
        while (it.hasNext()) {
            Element dep = (Element)it.next();
            if (Util.findElement(dep, "compile-dependency", // NOI18N
                    NbModuleProjectType.NAMESPACE_SHARED) == null) {
                continue;
            }
            Element cnbEl = Util.findElement(dep, "code-name-base", // NOI18N
                NbModuleProjectType.NAMESPACE_SHARED);
            String cnb = Util.findText(cnbEl);
            ModuleEntry module = ml.getEntry(cnb);
            if (module == null) {
                Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + FileUtil.getFileDisplayName(getProjectDirectory()));
                continue;
            }
            // XXX if that module is projectized, check its public packages;
            // if it has none, skip it and issue a warning, unless we are
            // declaring an impl dependency
            File moduleJar = module.getJarLocation();
            if (cp.length() > 0) {
                cp.append(File.pathSeparatorChar);
            }
            cp.append(moduleJar.getAbsolutePath());
            cp.append(module.getClassPathExtensions());
        }
        ModuleEntry myself = ml.getEntry(getCodeNameBase());
        if (myself == null) {
            // ???
            return "";
        }
        cp.append(myself.getClassPathExtensions());
        return cp.toString();
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    private final Map/*<String,FileObject>*/ directoryCache = new WeakHashMap();
    
    private FileObject getDir(String prop) {
        // XXX also add a PropertyChangeListener to eval and clear the cache of changed props
        if (directoryCache.containsKey(prop)) {
            return (FileObject)directoryCache.get(prop);
        } else {
            String v = evaluator().getProperty(prop);
            assert v != null : "No value for " + prop;
            FileObject f = helper.resolveFileObject(v);
            directoryCache.put(prop, f);
            return f;
        }
    }

    public FileObject getSourceDirectory() {
        return getDir("src.dir"); // NOI18N
    }
    
    public FileObject getTestSourceDirectory() {
        return getDir("test.unit.src.dir"); // NOI18N
    }
    
    public FileObject getFunctionalTestSourceDirectory() {
        return getDir("test.qa-functional.src.dir"); // NOI18N
    }
    
    public FileObject getPerformanceTestSourceDirectory() {
        return getDir("test.qa-performance.src.dir"); // NOI18N
    }
    
    public File getClassesDirectory() {
        String classesDir = eval.getProperty("build.classes.dir"); // NOI18N
        return helper.resolveFile(classesDir);
    }
    
    public File getTestClassesDirectory() {
        String testClassesDir = eval.getProperty("build.test.unit.classes.dir"); // NOI18N
        return helper.resolveFile(testClassesDir);
    }
    
    public FileObject getJavaHelpDirectory() {
        if (helper.resolveFileObject("javahelp/manifest.mf") != null) { // NOI18N
            // Special hack for core.
            return null;
        }
        return helper.resolveFileObject("javahelp"); // NOI18N
    }
    
    public File getModuleJarLocation() {
        // XXX could use ModuleList here instead
        return helper.resolveFile(eval.evaluate("${cluster}/${module.jar}")); // NOI18N
    }
    
    public String getCodeNameBase() {
        Element config = getHelper().getPrimaryConfigurationData(true);
        Element cnb = Util.findElement(config, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (cnb != null) {
            return Util.findText(cnb);
        } else {
            return null;
        }
    }
    
    public String getSpecVersion() {
        //TODO shall we check for illegal cases like "none-defined" or "both-defined" here?
        String manVersion = getManifest().getMainAttributes().getValue("OpenIDE-Module-Specification-Version"); //NOI18N
        if (manVersion != null) {
            return manVersion;
        }
        String base = evaluator().getProperty("spec.version.base"); //NOI18N
        return base;
    }
    
    /**
     * Slash-separated path inside netbeans.org CVS, or null for external modules.
     */
    public String getPathWithinNetBeansOrg() {
        FileObject nbroot = getNbrootFileObject(null);
        if (nbroot != null) {
            return FileUtil.getRelativePath(nbroot, getProjectDirectory());
        } else {
            return null;
        }
    }
    
    private File getNbroot() {
        return getNbroot(null);
    }
    private File getNbroot(PropertyEvaluator eval) {
        File dir = FileUtil.toFile(getProjectDirectory());
        File nbroot = ModuleList.findNetBeansOrg(dir);
        if (nbroot != null) {
            return nbroot;
        } else {
            // OK, not it.
            NbPlatform platform = getPlatform(eval);
            URL[] roots = platform.getSourceRoots();
            for (int i = 0; i < roots.length; i++) {
                if (roots[i].getProtocol().equals("file")) { // NOI18N
                    File f = new File(URI.create(roots[i].toExternalForm()));
                    if (ModuleList.isNetBeansOrg(f)) {
                        return f;
                    }
                }
            }
            // Did not find it.
            return null;
        }
    }
    
    public File getNbrootFile(String path) {
        return getNbrootFile(path, null);
    }
    private File getNbrootFile(String path, PropertyEvaluator eval) {
        File nbroot = getNbroot(eval);
        if (nbroot != null) {
            return new File(nbroot, path.replace('/', File.separatorChar));
        } else {
            return null;
        }
    }
    
    public FileObject getNbrootFileObject(String path) {
        File f = path != null ? getNbrootFile(path) : getNbroot();
        if (f != null) {
            return FileUtil.toFileObject(f);
        } else {
            return null;
        }
    }
    
    public ModuleList getModuleList() throws IOException {
        try {
            return ModuleList.getModuleList(FileUtil.toFile(getProjectDirectory()));
        } catch (IOException e) {
            // #60094: see if we can fix it quietly by resetting platform to default.
            FileObject platformPropertiesFile = null;
            if (typeProvider.getModuleType() == NbModuleTypeProvider.STANDALONE) {
                platformPropertiesFile = getProjectDirectory().getFileObject("nbproject/platform.properties"); // NOI18N
            } else if (typeProvider.getModuleType() == NbModuleTypeProvider.SUITE_COMPONENT) {
                PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(
                        getHelper().getStockPropertyPreprovider(),
                        new PropertyProvider[] {
                            getHelper().getPropertyProvider("nbproject/private/suite-private.properties"), // NOI18N
                            getHelper().getPropertyProvider("nbproject/suite.properties"), // NOI18N
                        });
                String suiteDirS = baseEval.getProperty("suite.dir"); // NOI18N
                if (suiteDirS != null) {
                    FileObject suiteDir = getHelper().resolveFileObject(suiteDirS);
                    if (suiteDir != null) {
                        platformPropertiesFile = suiteDir.getFileObject("nbproject/platform.properties"); // NOI18N
                    }
                }
            }
            if (platformPropertiesFile != null) {
                try {
                    EditableProperties ep = Util.loadProperties(platformPropertiesFile);
                    if (!NbPlatform.PLATFORM_ID_DEFAULT.equals(ep.getProperty("nbplatform.active"))) { // NOI18N
                        ep.setProperty("nbplatform.active", NbPlatform.PLATFORM_ID_DEFAULT); // NOI18N
                        Util.storeProperties(platformPropertiesFile, ep);
                    } else {
                        // That wasn't it, never mind.
                        throw e;
                    }
                } catch (IOException e2) {
                    Util.err.notify(ErrorManager.INFORMATIONAL, e2);
                    // Well, throw original exception.
                    throw e;
                }
                // Try again!
                return ModuleList.getModuleList(FileUtil.toFile(getProjectDirectory()));
            }
            throw e;
        }
    }
    
    public NbPlatform getPlatform() {
        return getPlatform(null);
    }
    private NbPlatform getPlatform(PropertyEvaluator eval) {
        if (eval == null) {
            eval = evaluator();
        }
        String prop = eval.getProperty("netbeans.dest.dir"); // NOI18N
        if (prop == null) {
            return null;
        }
        return NbPlatform.getPlatformByDestDir(getHelper().resolveFile(prop));
    }

    /**
     * Check whether Javadoc generation is possible.
     */
    public boolean supportsJavadoc() {
        if (evaluator().getProperty("module.javadoc.packages") != null) {
            return true;
        }
        Element config = getHelper().getPrimaryConfigurationData(true);
        Element pubPkgs = Util.findElement(config, "public-packages", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (pubPkgs == null) {
            // Try <friend-packages> too.
            pubPkgs = Util.findElement(config, "friend-packages", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        }
        return pubPkgs != null && !Util.findSubElements(pubPkgs).isEmpty();
    }
    
    public boolean supportsUnitTests() {
        return getTestSourceDirectory() != null;
    }
    
    /**
     * Find marked extra compilation units.
     * Gives a map from the package root to the defining XML element.
     */
    public Map/*<FileObject,Element>*/ getExtraCompilationUnits() {
        if (extraCompilationUnits == null) {
            extraCompilationUnits = new HashMap();
            Iterator/*<Element>*/ ecuEls = Util.findSubElements(getHelper().getPrimaryConfigurationData(true)).iterator();
            while (ecuEls.hasNext()) {
                Element ecu = (Element) ecuEls.next();
                if (ecu.getLocalName().equals("extra-compilation-unit")) { // NOI18N
                    Element pkgrootEl = Util.findElement(ecu, "package-root", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
                    String pkgrootS = Util.findText(pkgrootEl);
                    String pkgrootEval = evaluator().evaluate(pkgrootS);
                    FileObject pkgroot = getHelper().resolveFileObject(pkgrootEval);
                    if (pkgroot == null) {
                        Util.err.log(ErrorManager.WARNING, "Could not find package-root " + pkgrootEval + " for " + getCodeNameBase());
                        continue;
                    }
                    extraCompilationUnits.put(pkgroot, ecu);
                }
            }
        }
        return extraCompilationUnits;
    }

    /** Get the Java source level used for this module. Default is 1.4. */
    public String getJavacSource() {
        String javacSource = evaluator().getProperty("javac.source");
	assert javacSource != null;
	return javacSource;
    }
    
    /**
     * Run the open hook.
     * For use from unit tests.
     */
    public void open() {
        ((OpenedHook) getLookup().lookup(OpenedHook.class)).projectOpened();
    }
    
    /**
     * Returns {@link LocalizedBundleInfo} for this project. For use from unit
     * tests.
     */
    LocalizedBundleInfo getBundleInfo() {
        return bundleInfo;
    }
    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
        
        Info() {}
        
        public String getName() {
            return getCodeNameBase();
        }
        
        public String getDisplayName() {
            String newInfoDisplayName = bundleInfo != null ? bundleInfo.getDisplayName() : getName();
            if (infoDisplayName == null || !newInfoDisplayName.equals(infoDisplayName)) {
                String oldValue = infoDisplayName;
                infoDisplayName = newInfoDisplayName;
                firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, oldValue, infoDisplayName);
            }
            return infoDisplayName;
        }
        
        public Icon getIcon() {
            return NB_PROJECT_ICON;
        }
        
        public Project getProject() {
            return NbModuleProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener pchl) {
            changeSupport.addPropertyChangeListener(pchl);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener pchl) {
            changeSupport.removePropertyChangeListener(pchl);
        }
        
        private void firePropertyChange(String propName, Object oldValue, Object newValue) {
            changeSupport.firePropertyChange(propName, oldValue, newValue);
        }
        
    }
    
    final class OpenedHook extends ProjectOpenedHook {
        
        private ClassPath[] boot, source, compile;
        
        OpenedHook() {}
        
        protected void projectOpened() {
            // register project's classpaths to GlobalClassPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, boot = cpProvider.getProjectClassPaths(ClassPath.BOOT));
            assert boot != null : "No BOOT path";
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, source = cpProvider.getProjectClassPaths(ClassPath.SOURCE));
            assert source != null : "No SOURCE path";
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, compile = cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            assert compile != null : "No COMPILE path";
            // write user.properties.file=$userdir/build.properties to platform-private.properties
            if (getModuleType() == NbModuleTypeProvider.STANDALONE) {
                // XXX skip this in case nbplatform.active is not defined
                ProjectManager.mutex().writeAccess(new Mutex.Action() {
                    public Object run() {
                        String path = "nbproject/private/platform-private.properties"; // NOI18N
                        EditableProperties ep = getHelper().getProperties(path);
                        File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                        ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                        getHelper().putProperties(path, ep);
                        try {
                            ProjectManager.getDefault().saveProject(NbModuleProject.this);
                        } catch (IOException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                        return null;
                    }
                });
            }
            // refresh build.xml and build-impl.xml for external modules
            if (getModuleType() != NbModuleTypeProvider.NETBEANS_ORG) {
                try {
                    genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        NbModuleProject.class.getResource("resources/build-impl.xsl"), // NOI18N
                        true);
                    genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        NbModuleProject.class.getResource("resources/build.xsl"), // NOI18N
                        true);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        protected void projectClosed() {
            try {
                ProjectManager.getDefault().saveProject(NbModuleProject.this);
            } catch (IOException e) {
                Util.err.notify(e);
            }
            
            // XXX could discard caches, etc.
            
            // unregister project's classpaths to GlobalClassPathRegistry
            // XXX behave more gracefully in case an exception was thrown during projectOpened
            assert boot != null && source != null && compile != null : "#46802: project being closed which was never opened?? " + NbModuleProject.this;
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, boot);
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, source);
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, compile);
            boot = null;
            source = null;
            compile = null;
        }
        
    }
    
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        protected void projectXmlSaved() throws IOException {
            // refresh build.xml and build-impl.xml for external modules
            if (getModuleType() != NbModuleTypeProvider.NETBEANS_ORG) {
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    NbModuleProject.class.getResource("resources/build-impl.xsl"), // NOI18N
                    false);
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    NbModuleProject.class.getResource("resources/build.xsl"), // NOI18N
                    false);
            }
        }
        
    }
    
    private final class SuiteProviderImpl implements SuiteProvider {

        public File getSuiteDirectory() {
            String suiteDir = eval.getProperty("suite.dir"); // NOI18N
            return suiteDir == null ? null : helper.resolveFile(suiteDir);
        }
        
    }
    
    private class NbModuleTypeProviderImpl implements NbModuleTypeProvider {
        
        private NbModuleType type;
        
        public NbModuleType getModuleType() {
            if (type == null) {
                type = NbModuleProject.this.getModuleType();
            }
            return type;
        }

        void reset() {
            type = null;
        }
        
    }
    
    private static final class PrivilegedTemplatesImpl implements /*PrivilegedTemplates,*/ RecommendedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
        };
        
        private static final String[] RECOMMENDED_TYPES = new String[] {         
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
            "junit",                // NOI18N                    
            "simple-files",         // NOI18N
            "nbm-specific",         // NOI18N
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

        public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }
    }    
}
