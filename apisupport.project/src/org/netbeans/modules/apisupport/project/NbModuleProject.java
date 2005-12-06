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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
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
    
    public static final String NB_PROJECT_ICON_PATH =
            "org/netbeans/modules/apisupport/project/resources/module.gif"; // NOI18N
    
    private static final Icon NB_PROJECT_ICON = new ImageIcon(
            Utilities.loadImage(NB_PROJECT_ICON_PATH));
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private Map/*<FileObject,Element>*/ extraCompilationUnits;
    private final GeneratedFilesHelper genFilesHelper;
    private final NbModuleTypeProviderImpl typeProvider;
    
    private LocalizedBundleInfo bundleInfo;
    
    private boolean manifestChanged;

    /** See issue #69440 for more details. */
    private boolean runInAtomicAction;
    
    NbModuleProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        genFilesHelper = new GeneratedFilesHelper(helper);
        Util.err.log("Loading project in " + getProjectDirectory());
        if (getCodeNameBase() == null) {
            throw new IOException("Misconfigured project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " has no defined <code-name-base>"); // NOI18N
        }
        typeProvider = new NbModuleTypeProviderImpl();
        if (typeProvider.getModuleType() == NbModuleTypeProvider.NETBEANS_ORG && ModuleList.findNetBeansOrg(FileUtil.toFile(getProjectDirectory())) == null) {
            // #69097: preferable to throwing an assertion error later...
            throw new IOException("netbeans.org-type module not in a complete netbeans.org source root: " + this); // NOI18N
        }
        eval = new Eval();
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
        sourcesHelper.addPrincipalSourceRoot("${test.unit.src.dir}", NbBundle.getMessage(NbModuleProject.class, "LBL_unit_test_packages"), null, null); // #68727
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
        Info info = new Info();
        if (bundleInfo != null) {
            bundleInfo.addPropertyChangeListener(info);
            getManifestFile().addFileChangeListener(new FileChangeAdapter() {
                public void fileChanged(FileEvent fe) {
                    // cannot reload manifest-depended things immediatelly (see 67961 for more details)
                    manifestChanged = true;
                }
            });
        }
        lookup = Lookups.fixed(new Object[] {
            info,
            helper.createAuxiliaryConfiguration(),
            helper.createCacheDirectoryProvider(),
            new SavedHook(),
            new OpenedHook(),
            new ModuleActions(this, typeProvider.getModuleType()),
            new ClassPathProviderImpl(this),
            new SourceForBinaryImpl(this),
            new JavadocForBinaryImpl(this),
            new UnitTestForSourceQueryImpl(this),
            new ModuleLogicalView(this),
            new SubprojectProviderImpl(this),
            fileBuilt,
            new AccessibilityQueryImpl(this),
            new SourceLevelQueryImpl(this),
            helper.createSharabilityQuery(evaluator(), new String[0], new String[] {
                // currently these are hardcoded
                "build", // NOI18N
            }),
            sourcesHelper.createSources(),
            new AntArtifactProviderImpl(this, helper, evaluator()),
            new CustomizerProviderImpl(this, getHelper(), evaluator()),
            new SuiteProviderImpl(),
            typeProvider,
            new PrivilegedTemplatesImpl(),
            new ModuleProjectClassPathExtender(this),
            new LocalizedBundleInfoProvider(),
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
        return helper.resolveFileObject(evaluator().getProperty("manifest.mf")); // NOI18N
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
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    /**
     * Evaluator for the project. Has two special behaviors of note:
     * 1. Does not call ModuleList until it really needs to.
     * 2. Is reset upon project.xml changes.
     */
    private final class Eval implements PropertyEvaluator, PropertyChangeListener, AntProjectListener {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        private PropertyEvaluator delegate;
        private boolean loadedModuleList = false;
        
        public Eval() {
            delegate = createEvaluator(null);
            delegate.addPropertyChangeListener(this);
            getHelper().addAntProjectListener(this);
        }
        
        public String getProperty(String prop) {
            PropertyEvaluator eval = delegatingEvaluator(false);
            assert eval != this;
            String v = eval.getProperty(prop);
            if ((v == null && isModuleListDependentProperty(prop)) || isModuleListDependentValue(v)) {
                return delegatingEvaluator(true).getProperty(prop);
            } else {
                return v;
            }
        }
        
        public String evaluate(String text) {
            String v = delegatingEvaluator(false).evaluate(text);
            if (isModuleListDependentValue(v)) {
                return delegatingEvaluator(true).evaluate(text);
            } else {
                return v;
            }
        }
        
        public Map getProperties() {
            return delegatingEvaluator(true).getProperties();
        }
        
        private boolean isModuleListDependentProperty(String p) {
            return p.equals("module.classpath") || // NOI18N
                    p.equals("cp") || p.endsWith(".cp") || p.endsWith(".cp.extra") || // NOI18N
                    p.equals("cluster") || // NOI18N
                    // MODULENAME.dir, but not module.jar.dir or the like:
                    (p.endsWith(".dir") && p.lastIndexOf('.', p.length() - 5) == -1); // NOI18N
        }
        
        private final Pattern ANT_PROP_REGEX = Pattern.compile("\\$\\{([a-zA-Z0-9._-]+)\\}"); // NOI18N
        private boolean isModuleListDependentValue(String v) {
            if (v == null) {
                return false;
            }
            Matcher m = ANT_PROP_REGEX.matcher(v);
            while (m.find()) {
                if (isModuleListDependentProperty(m.group(1))) {
                    return true;
                }
            }
            return false;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        private PropertyEvaluator delegatingEvaluator(boolean reset) {
            if (reset && !loadedModuleList) {
                reset();
                if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                    Util.err.log("Needed to reset evaluator in " + NbModuleProject.this + "due to use of module-list-dependent property; now cp=" + delegate.getProperty("cp"));
                }
            }
            return delegate;
        }
        
        private void reset() {
            loadedModuleList = true;
            delegate.removePropertyChangeListener(this);
            try {
                delegate = createEvaluator(getModuleList());
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
                // but leave old evaluator in place for now
            }
            delegate.addPropertyChangeListener(this);
            pcs.firePropertyChange(null, null, null);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if ("netbeans.dest.dir".equals(evt.getPropertyName()) || evt.getPropertyName() == null) {
                // Module list may have changed.
                reset();
            } else {
                Util.err.log("Refiring property change from delegate in " + evt.getPropertyName() + " for " + NbModuleProject.this);
                pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            if (!runInAtomicAction && ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
                reset();
            }
        }

        public void propertiesChanged(AntProjectEvent ev) {}
        
    }
    
    /**
     * Create a property evaluator: private project props, shared project props, various defaults.
     * Synch with nbbuild/templates/projectized.xml.
     * @param ml this module list, or may be left null to skip all properties which require knowledge of other modules
     */
    private PropertyEvaluator createEvaluator(ModuleList ml) {
        // XXX a lot of this duplicates ModuleList.parseProperties... can they be shared?
        PropertyProvider predefs = helper.getStockPropertyPreprovider();
        Map/*<String,String>*/ stock = new HashMap();
        File dir = FileUtil.toFile(getProjectDirectory());
        NbModuleTypeProvider.NbModuleType type = getModuleType();
        File nbroot;
        if (type == NbModuleTypeProvider.NETBEANS_ORG) {
            nbroot = ModuleList.findNetBeansOrg(dir);
            assert nbroot != null : "netbeans.org-type module not in a complete netbeans.org source root " + dir;
            stock.put("nb_all", nbroot.getAbsolutePath()); // NOI18N
            // Only needed for netbeans.org modules, since for external modules suite.properties suffices.
            stock.put("netbeans.dest.dir", new File(nbroot, ModuleList.DEST_DIR_IN_NETBEANS_ORG).getAbsolutePath()); // NOI18N
        } else {
            nbroot = null;
        }
        if (ml != null) {
            // Register *.dir for nb.org modules. There is no equivalent for external modules.
            Iterator it = ml.getAllEntriesSoft().iterator();
            while (it.hasNext()) {
                ModuleEntry e = (ModuleEntry) it.next();
                String nborgPath = e.getNetBeansOrgPath();
                if (nborgPath != null) {
                    // #48449: intern these; number is (size of modules.xml) * (# of loaded module projects)
                    stock.put((nborgPath + ".dir").intern(), e.getClusterDirectory().getAbsolutePath().intern()); // NOI18N
                }
            }
            ModuleEntry thisEntry = ml.getEntry(getCodeNameBase());
            if (thisEntry != null) { // can be null e.g. for a broken suite component module
                assert nbroot == null ^ thisEntry.getNetBeansOrgPath() != null : thisEntry;
                File clusterDir = thisEntry.getClusterDirectory();
                stock.put("cluster", clusterDir.getAbsolutePath()); // NOI18N
            }
        }
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
            providers.add(new Util.UserPropertiesFileProvider(baseEval, dir));
            baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            class DestDirProvider extends Util.ComputedPropertyProvider {
                public DestDirProvider(PropertyEvaluator eval) {
                    super(eval);
                }
                protected Map/*<String,String>*/ getProperties(Map/*<String,String>*/ inputPropertyValues) {
                    String platformS = (String) inputPropertyValues.get("nbplatform.active"); // NOI18N
                    if (platformS != null) {
                        return Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"); // NOI18N
                    } else {
                        return Collections.EMPTY_MAP;
                    }
                }
                protected Set inputProperties() {
                    return Collections.singleton("nbplatform.active"); // NOI18N
                }
            }
            providers.add(new DestDirProvider(baseEval));
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
        if (ml != null) {
            providers.add(PropertyUtils.fixedPropertyProvider(Collections.singletonMap("module.classpath", computeModuleClasspath(ml)))); // NOI18N
            Map/*<String,String>*/ buildDefaults = new HashMap();
            buildDefaults.put("cp.extra", ""); // NOI18N
            buildDefaults.put("cp", "${module.classpath}:${cp.extra}"); // NOI18N
            buildDefaults.put("run.cp", "${cp}:${build.classes.dir}"); // NOI18N
            PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            buildDefaults.put("test.unit.cp.extra", ""); // NOI18N
            String testJars; // #68685 - follow Ant script
            if (type == NbModuleTypeProvider.NETBEANS_ORG) {
                // Cf. nbbuild/templates/projectized.xml#test-lib-init
                buildDefaults.put("xtest.home", "${nb_all}/xtest"); // NOI18N
                testJars =
                        "${xtest.home}/lib/junit.jar:" + // NOI18N
                        "${xtest.home}/lib/nbjunit.jar:" + // NOI18N
                        "${xtest.home}/lib/nbjunit-ide.jar:" + // NOI18N
                        "${xtest.home}/lib/insanelib.jar"; // NOI18N
            } else {
                // Cf. apisupport/harness/release/build.xml#test-lib-init
                testJars =
                        "${test.unit.lib.cp}:" +
                        "${netbeans.dest.dir}/ide6/modules/ext/junit-3.8.1.jar:" + // NOI18N
                        "${netbeans.dest.dir}/testtools/modules/ext/nbjunit.jar:" + // NOI18N
                        "${netbeans.dest.dir}/testtools/modules/ext/insanelib.jar:" + // NOI18N
                        "${netbeans.home}/../ide6/modules/ext/junit-3.8.1.jar:" + // NOI18N
                        "${netbeans.home}/../testtools/modules/ext/nbjunit.jar:" + // NOI18N
                        "${netbeans.home}/../testtools/modules/ext/insanelib.jar:" + // NOI18N
                        "${netbeans.user}/modules/ext/nbjunit.jar:" + // NOI18N
                        "${netbeans.user}/modules/ext/insanelib.jar:" + // NOI18N
                        "${netbeans.dest.dir}/../../xtest/lib/junit.jar:" + // NOI18N
                        "${netbeans.dest.dir}/../../xtest/lib/nbjunit.jar:" + // NOI18N
                        "${netbeans.dest.dir}/../../xtest/lib/insanelib.jar"; // NOI18N
            }
            buildDefaults.put("test.unit.cp", "${cp}:${cluster}/${module.jar}:" + testJars + ":${test.unit.cp.extra}"); // NOI18N
            buildDefaults.put("test.unit.run.cp.extra", ""); // NOI18N
            buildDefaults.put("test.unit.run.cp", "${test.unit.cp}:${build.test.unit.classes.dir}:${test.unit.run.cp.extra}"); // NOI18N
            // #61085: need to treat qa-functional tests the same way...
            buildDefaults.put("test.qa-functional.cp.extra", ""); // NOI18N
            // No idea how XTest finds these, some weird magic, so no Ant script to match up to:
            String jemmyJar = findJemmyJar(baseEval);
            if (jemmyJar != null) {
                buildDefaults.put("jemmy.jar", jemmyJar); // NOI18N
            }
            String jelly2NbJar = findJelly2NbJar(baseEval);
            if (jelly2NbJar != null) {
                buildDefaults.put("jelly2-nb.jar", jelly2NbJar); // NOI18N
            }
            buildDefaults.put("test.qa-functional.cp", testJars + // NOI18N
                    ":${netbeans.home}/../testtools/modules/ext/nbjunit-ide.jar" + // NOI18N
                    ":${netbeans.user}/testtools/modules/ext/nbjunit.jar" + // NOI18N
                    ":${jemmy.jar}" + // NOI18N
                    ":${jelly2-nb.jar}" + // NOI18N
                    ":${test.qa-functional.cp.extra}"); // NOI18N
            buildDefaults.put("build.test.qa-functional.classes.dir", "build/test/qa-functional/classes"); // NOI18N
            buildDefaults.put("test.qa-functional.run.cp", "${test.qa-functional.cp}:${build.test.qa-functional.classes.dir}"); // NOI18N
            providers.add(PropertyUtils.fixedPropertyProvider(buildDefaults));
        }
        // skip a bunch of properties irrelevant here - NBM stuff, etc.
        return PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
    }
    
    /**
     * Get an Ant location for the root of jemmy.jar.
     */
    private String findJemmyJar(PropertyEvaluator eval) {
        File f = getNbrootFile("jemmy/builds/jemmy.jar", eval); // NOI18N
        if (f != null) {
            return f.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    /**
     * Get an Ant location for the root of jemmy.jar.
     */
    private String findJelly2NbJar(PropertyEvaluator eval) {
        File f = getNbrootFile("jellytools/builds/jelly2-nb.jar", eval); // NOI18N
        if (f != null) {
            return f.getAbsolutePath();
        } else {
            return null;
        }
    }
    
    /**
     * Should be similar to impl in ParseProjectXml.
     */
    private String computeModuleClasspath(ModuleList ml) {
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
        String classesDir = evaluator().getProperty("build.classes.dir"); // NOI18N
        return helper.resolveFile(classesDir);
    }
    
    public File getTestClassesDirectory() {
        String testClassesDir = evaluator().getProperty("build.test.unit.classes.dir"); // NOI18N
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
        return helper.resolveFile(evaluator().evaluate("${cluster}/${module.jar}")); // NOI18N
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
            if (platform != null) {
                URL[] roots = platform.getSourceRoots();
                for (int i = 0; i < roots.length; i++) {
                    if (roots[i].getProtocol().equals("file")) { // NOI18N
                        File f = new File(URI.create(roots[i].toExternalForm()));
                        if (ModuleList.isNetBeansOrg(f)) {
                            return f;
                        }
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
        NbPlatform p = getPlatform(false);
        if (p == null) {
            // #67148: have to use something... (and getEntry(codeNameBase) will certainly fail!)
            return ModuleList.getModuleList(FileUtil.toFile(getProjectDirectory()), NbPlatform.getDefaultPlatform().getDestDir());
        }
        ModuleList ml = ModuleList.getModuleList(FileUtil.toFile(getProjectDirectory()), p.getDestDir());
        if (ml.getEntry(getCodeNameBase()) == null) {
            ModuleList.refresh();
            ml = ModuleList.getModuleList(FileUtil.toFile(getProjectDirectory()));
            if (ml.getEntry(getCodeNameBase()) == null) {
                // XXX try to give better diagnostics - as examples are discovered
                Util.err.log(ErrorManager.WARNING, "Project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " does not appear to be listed in its own module list; some sort of misconfiguration (e.g. not listed in its own suite)"); // NOI18N
            }
        }
        return ml;
        /*
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
         */
    }
    
    /**
     * Get the platform which this project is currently associated with.
     * @param fallback if true, fall back to the default platform if necessary
     * @return the current platform; or null if fallback is false and there is no
     *         platform specified, or an invalid platform is specified, or even if
     *         fallback is true but even the default platform is not available
     */
    public NbPlatform getPlatform(boolean fallback) {
        NbPlatform p = getPlatform(null);
        if (fallback && (p == null || !p.isValid())) {
            p = NbPlatform.getDefaultPlatform();
        }
        return p;
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
     * <strong>For use from unit tests only.</strong> Returns {@link
     * LocalizedBundleInfo} for this project.
     */
    public LocalizedBundleInfo getBundleInfo() {
        return bundleInfo;
    }
    
    
    /** See issue #69440 for more details. */
    public void setRunInAtomicAction(boolean runInAtomicAction) {
        assert ProjectManager.mutex().isWriteAccess();
        this.runInAtomicAction = runInAtomicAction;
    }
    
    private final class Info implements ProjectInformation, PropertyChangeListener {
        
        private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

        private String displayName;
        
        Info() {
            displayName = bundleInfo != null ? bundleInfo.getDisplayName() : getName();
        }
        
        public String getName() {
            return getCodeNameBase();
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        private void setDisplayName(String newDisplayName) {
            String oldDisplayName = displayName;
            displayName = newDisplayName == null ? getName() : newDisplayName;
            firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, oldDisplayName, displayName);
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

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == ProjectInformation.PROP_DISPLAY_NAME) {
                setDisplayName((String) evt.getNewValue());
            }
        }
        
    }
    
    final class OpenedHook extends ProjectOpenedHook {
        
        private ClassPath[] boot, source, compile;
        
        OpenedHook() {}
        
        protected void projectOpened() {
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
            // register project's classpaths to GlobalClassPathRegistry
            ClassPathProviderImpl cpProvider = (ClassPathProviderImpl)lookup.lookup(ClassPathProviderImpl.class);
            ClassPath[] _boot = cpProvider.getProjectClassPaths(ClassPath.BOOT);
            assert _boot != null : "No BOOT path";
            ClassPath[] _source = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
            assert _source != null : "No SOURCE path";
            ClassPath[] _compile = cpProvider.getProjectClassPaths(ClassPath.COMPILE);
            assert _compile != null : "No COMPILE path";
            // Possible cause of #68414: do not change instance vars until after the dangerous stuff has been computed.
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, _boot);
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, _source);
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, _compile);
            boot = _boot;
            source = _source;
            compile = _compile;
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
            String suiteDir = evaluator().getProperty("suite.dir"); // NOI18N
            return suiteDir == null ? null : helper.resolveFile(suiteDir);
        }
        
    }
    
    private class NbModuleTypeProviderImpl implements NbModuleTypeProvider, AntProjectListener {
        
        private NbModuleType type;
        
        public NbModuleTypeProviderImpl() {
            getHelper().addAntProjectListener(this);
        }
        
        public NbModuleType getModuleType() {
            if (type == null) {
                type = NbModuleProject.this.getModuleType();
            }
            return type;
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
                type = null;
            }
        }

        public void propertiesChanged(AntProjectEvent ev) {}
        
    }
    
    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates, RecommendedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            //"Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/JUnit/SimpleJUnitTest.java", // NOI18N
            "Templates/NetBeansModuleDevelopment/newAction", // NOI18N
            "Templates/NetBeansModuleDevelopment/emptyLibraryDescriptor", // NOI18N
            "Templates/NetBeansModuleDevelopment/newLoader", // NOI18N
            "Templates/NetBeansModuleDevelopment/newProject", // NOI18N
            "Templates/NetBeansModuleDevelopment/newWindow", // NOI18N
            "Templates/NetBeansModuleDevelopment/newWizard", // NOI18N
            //"Templates/Other/properties.properties", // NOI18N
        };
        static {
            assert PRIVILEGED_NAMES.length <= 10 : "Too many privileged templates to fit! extras will be ignored: " +
                    Arrays.asList(PRIVILEGED_NAMES).subList(10, PRIVILEGED_NAMES.length);
        }
        
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

    private final class LocalizedBundleInfoProvider implements LocalizedBundleInfo.Provider {
        public LocalizedBundleInfo getLocalizedBundleInfo() {
            if (manifestChanged) {
                bundleInfo = Util.findLocalizedBundleInfo(getSourceDirectory(), getManifest());
                manifestChanged = false;
            }
            return bundleInfo;
        }
    }

}
