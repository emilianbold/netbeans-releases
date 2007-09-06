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

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider.NbModuleType;
import org.netbeans.modules.apisupport.project.metainf.ServiceNodeHandler;
import org.netbeans.modules.apisupport.project.queries.ModuleProjectClassPathExtender;
import org.netbeans.modules.apisupport.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
import org.netbeans.modules.apisupport.project.queries.FileEncodingQueryImpl;
import org.netbeans.modules.apisupport.project.queries.JavadocForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.SourceForBinaryImpl;
import org.netbeans.modules.apisupport.project.queries.SubprojectProviderImpl;
import org.netbeans.modules.apisupport.project.queries.TemplateAttributesProvider;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.ui.ModuleActions;
import org.netbeans.modules.apisupport.project.ui.ModuleLogicalView;
import org.netbeans.modules.apisupport.project.ui.ModuleOperations;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;

/**
 * A NetBeans module project.
 * @author Jesse Glick
 */
public final class NbModuleProject implements Project {
    
    public static final String NB_PROJECT_ICON_PATH =
            "org/netbeans/modules/apisupport/project/resources/module.gif"; // NOI18N
    
    private static final Icon NB_PROJECT_ICON = new ImageIcon(
            Utilities.loadImage(NB_PROJECT_ICON_PATH));
    
    public static final String SOURCES_TYPE_JAVAHELP = "javahelp"; // NOI18N
    
    private final AntProjectHelper helper;
    private final Evaluator eval;
    private final Lookup lookup;
    private Map<FileObject,Element> extraCompilationUnits;
    private final GeneratedFilesHelper genFilesHelper;
    private final NbModuleProviderImpl typeProvider;
    
    NbModuleProject(AntProjectHelper helper) throws IOException {
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        for (int v = 4; v < 10; v++) {
            if (aux.getConfigurationFragment("data", "http://www.netbeans.org/ns/nb-module-project/" + v, true) != null) { // NOI18N
                throw Exceptions.attachLocalizedMessage(new IOException("too new"), // NOI18N
                        NbBundle.getMessage(NbModuleProject.class, "NbModuleProject.too_new", FileUtil.getFileDisplayName(helper.getProjectDirectory())));
            }
        }
        this.helper = helper;
        genFilesHelper = new GeneratedFilesHelper(helper);
        Util.err.log("Loading project in " + getProjectDirectory());
        if (getCodeNameBase() == null) {
            throw new IOException("Misconfigured project in " + FileUtil.getFileDisplayName(getProjectDirectory()) + " has no defined <code-name-base>"); // NOI18N
        }
        typeProvider = new NbModuleProviderImpl();
        if (typeProvider.getModuleType() == NbModuleProvider.NETBEANS_ORG && ModuleList.findNetBeansOrg(getProjectDirectoryFile()) == null) {
            // #69097: preferable to throwing an assertion error later...
            throw new IOException("netbeans.org-type module not in a complete netbeans.org source root: " + this); // NOI18N
        }
        eval = new Evaluator(this, typeProvider);
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
            Enumeration<? extends FileObject> kids = testDir.getChildren(false);
            while (kids.hasMoreElements()) {
                FileObject testSubdir = (FileObject) kids.nextElement();
                if (!testSubdir.isFolder()) {
                    continue;
                }
                String name = testSubdir.getNameExt();
                if (testDir.getFileObject("build-" + name + ".xml") == null) { // NOI18N
                    continue;
                }
                if (name.equals("unit") || name.equals("qa-functional") || name.equals("qa-performance")) { // NOI18N
                    // Already handled specially.
                    continue;
                }
                sourcesHelper.addTypedSourceRoot("test/" + name + "/src", JavaProjectConstants.SOURCES_TYPE_JAVA, NbBundle.getMessage(NbModuleProject.class, "LBL_unknown_test_packages", name), null, null);
            }
        }
        if (helper.resolveFileObject("javahelp/manifest.mf") == null) { // NOI18N
            // Special hack for core - ignore core/javahelp
            sourcesHelper.addTypedSourceRoot("javahelp", SOURCES_TYPE_JAVAHELP, NbBundle.getMessage(NbModuleProject.class, "LBL_javahelp_packages"), null, null);
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
        Lookup baseLookup = Lookups.fixed(
            this,
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            new SavedHook(),
            UILookupMergerSupport.createProjectOpenHookMerger(new OpenedHook()),
            new ModuleActions(this),
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
            new ModuleOperations(this),
            new ServiceNodeHandler(this, typeProvider),
            LookupProviderSupport.createSourcesMerger(),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            new TemplateAttributesProvider(getHelper(), getModuleType() == NbModuleType.NETBEANS_ORG),
            new FileEncodingQueryImpl());
        lookup = LookupProviderSupport.createCompositeLookup(baseLookup, "Projects/org-netbeans-modules-apisupport-project/Lookup"); //NOI18N
    }
    
    public @Override String toString() {
        return "NbModuleProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public File getProjectDirectoryFile() {
        return FileUtil.toFile(getProjectDirectory());
    }
    
    /**
     * Get the minimum harness version required to work with this module.
     */
    public int getMinimumHarnessVersion() {
        if (helper.createAuxiliaryConfiguration().getConfigurationFragment(NbModuleProjectType.NAME_SHARED, NbModuleProjectType.NAMESPACE_SHARED_2, true) != null) {
            return NbPlatform.HARNESS_VERSION_50;
        } else {
            return NbPlatform.HARNESS_VERSION_55u1;
        }
    }

    /**
     * Replacement for {@link AntProjectHelper#getPrimaryConfigurationData}
     * taking into account the /2 -> /3 upgrade.
     */
    public Element getPrimaryConfigurationData() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Element>() {
            public Element run() {
                AuxiliaryConfiguration ac = helper.createAuxiliaryConfiguration();
                Element data = ac.getConfigurationFragment(NbModuleProjectType.NAME_SHARED, NbModuleProjectType.NAMESPACE_SHARED_2, true);
                if (data != null) {
                    return Util.translateXML(data, NbModuleProjectType.NAMESPACE_SHARED);
                } else {
                    return helper.getPrimaryConfigurationData(true);
                }
            }
        });
    }

    /**
     * Replacement for {@link AntProjectHelper#putPrimaryConfigurationData}
     * taking into account the /2 -> /3 upgrade.
     */
    public void putPrimaryConfigurationData(final Element data) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                AuxiliaryConfiguration ac = helper.createAuxiliaryConfiguration();
                if (ac.getConfigurationFragment(NbModuleProjectType.NAME_SHARED, NbModuleProjectType.NAMESPACE_SHARED_2, true) != null) {
                    ac.putConfigurationFragment(Util.translateXML(data, NbModuleProjectType.NAMESPACE_SHARED_2), true);
                } else {
                    helper.putPrimaryConfigurationData(data, true);
                }
                return null;
            }
        });
    }

    /** Returns a relative path to a project's source directory. */
    public String getSourceDirectoryPath() {
        return evaluator().getProperty("src.dir"); // NOI18N
    }
    
    private NbModuleProvider.NbModuleType getModuleType() {
        Element data = getPrimaryConfigurationData();
        if (Util.findElement(data, "suite-component", NbModuleProjectType.NAMESPACE_SHARED) != null) { // NOI18N
            return NbModuleProvider.SUITE_COMPONENT;
        } else if (Util.findElement(data, "standalone", NbModuleProjectType.NAMESPACE_SHARED) != null) { // NOI18N
            return NbModuleProvider.STANDALONE;
        } else {
            return NbModuleProvider.NETBEANS_ORG;
        }
    }
    
    public FileObject getManifestFile() {
        return helper.resolveFileObject(evaluator().getProperty("manifest.mf")); // NOI18N
    }
    
    public Manifest getManifest() {
        return Util.getManifest(getManifestFile());
    }

    public AntProjectHelper getHelper() {
        return helper;
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    private final Map<String,FileObject> directoryCache = new WeakHashMap<String,FileObject>();
    
    private FileObject getDir(String prop) {
        // XXX also add a PropertyChangeListener to eval and clear the cache of changed props
        if (directoryCache.containsKey(prop)) {
            return directoryCache.get(prop);
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
        Element config = getPrimaryConfigurationData();
        Element cnb = Util.findElement(config, "code-name-base", NbModuleProjectType.NAMESPACE_SHARED); // NOI18N
        if (cnb != null) {
            return Util.findText(cnb);
        } else {
            return null;
        }
    }
    
    public String getSpecVersion() {
        //TODO shall we check for illegal cases like "none-defined" or "both-defined" here?
        Manifest m = getManifest();
        if (m != null) {
            String manVersion = m.getMainAttributes().getValue("OpenIDE-Module-Specification-Version"); //NOI18N
            if (manVersion != null) {
                return stripExcessZeros(manVersion);
            }
        }
        return stripExcessZeros(evaluator().getProperty(SingleModuleProperties.SPEC_VERSION_BASE));
    }
    private static String stripExcessZeros(String spec) { // #72826
        return spec != null ? spec.replaceAll("(\\.[0-9]+)\\.0$", "$1") : null; // NOI18N
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
        File dir = getProjectDirectoryFile();
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
    File getNbrootFile(String path, PropertyEvaluator eval) {
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
            return ModuleList.getModuleList(getProjectDirectoryFile(), NbPlatform.getDefaultPlatform().getDestDir());
        }
        ModuleList ml;
        try {
            ml = ModuleList.getModuleList(getProjectDirectoryFile(), p.getDestDir());
        } catch (IOException x) {
            // #69029: maybe invalidated platform? Try the default platform instead.
            Logger.getLogger(NbModuleProject.class.getName()).log(Level.FINE, null, x);
            return ModuleList.getModuleList(getProjectDirectoryFile(), NbPlatform.getDefaultPlatform().getDestDir());
        }
        if (ml.getEntry(getCodeNameBase()) == null) {
            ModuleList.refresh();
            ml = ModuleList.getModuleList(getProjectDirectoryFile());
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
            if (typeProvider.getModuleType() == NbModuleProvider.STANDALONE) {
                platformPropertiesFile = getProjectDirectory().getFileObject("nbproject/platform.properties"); // NOI18N
            } else if (typeProvider.getModuleType() == NbModuleProvider.SUITE_COMPONENT) {
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
                return ModuleList.getModuleList(getProjectDirectoryFile());
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
        File file = getPlatformFile(eval);
        if (file == null) {
            return null;
        }
        return NbPlatform.getPlatformByDestDir(file);
    }
    
    private File getPlatformFile(PropertyEvaluator eval) {
        if (eval == null) {
            eval = evaluator();
        }
        String prop = eval.getProperty("netbeans.dest.dir"); // NOI18N
        if (prop == null) {
            return null;
        }
        return getHelper().resolveFile(prop);
    }

    /**
     * Check whether Javadoc generation is possible.
     */
    public boolean supportsJavadoc() {
        if (evaluator().getProperty("module.javadoc.packages") != null) {
            return true;
        }
        Element config = getPrimaryConfigurationData();
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
    public Map<FileObject,Element> getExtraCompilationUnits() {
        if (extraCompilationUnits == null) {
            extraCompilationUnits = new HashMap<FileObject,Element>();
            for (Element ecu : Util.findSubElements(getPrimaryConfigurationData())) {
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
    
    private ClassPath[] boot, source, compile;
    private final class OpenedHook extends ProjectOpenedHook {
        OpenedHook() {}
        protected void projectOpened() {
            open();
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
    /**
     * Run the open hook.
     * For use from unit tests.
     */
    public void open() {
        // write user.properties.file=$userdir/build.properties to platform-private.properties
        if (getModuleType() == NbModuleProvider.STANDALONE) {
            // XXX skip this in case nbplatform.active is not defined
            ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                public Void run() {
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
        ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
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
        if (getModuleType() != NbModuleProvider.NETBEANS_ORG) {
            try {
                refreshBuildScripts(true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    /**
     * <strong>For use from unit tests only.</strong> Returns {@link
     * LocalizedBundleInfo} for this project.
     */
    public LocalizedBundleInfo getBundleInfo() {
        return getLookup().lookup(LocalizedBundleInfo.Provider.class).getLocalizedBundleInfo();
    }
    
    
    /** See issue #69440 for more details. */
    public void setRunInAtomicAction(boolean runInAtomicAction) {
        eval.setRunInAtomicAction(runInAtomicAction);
    }
        
    private final class Info implements ProjectInformation, PropertyChangeListener {
        
        private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

        private String displayName;
        
        Info() {}
        
        public String getName() {
            String cnb = getCodeNameBase();
            return cnb != null ? cnb : /* #70490 */getProjectDirectory().toString();
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                LocalizedBundleInfo bundleInfo = getBundleInfo();
                if (bundleInfo != null) {
                    displayName = bundleInfo.getDisplayName();
                }
            }
            if (/* #70490 */displayName == null) {
                displayName = getName();
            }
            assert displayName != null : NbModuleProject.this;
            return displayName;
        }
        
        private void setDisplayName(String newDisplayName) {
            String oldDisplayName = getDisplayName();
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
            if (ProjectInformation.PROP_DISPLAY_NAME.equals(evt.getPropertyName())) {
                setDisplayName((String) evt.getNewValue());
            }
        }
        
    }
    
    public void notifyDeleting() {
        eval.removeListeners();
    }
        
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        protected void projectXmlSaved() throws IOException {
            // refresh build.xml and build-impl.xml for external modules
            if (getModuleType() != NbModuleProvider.NETBEANS_ORG) {
                refreshBuildScripts(false);
            }
        }
        
    }
    
    private void refreshBuildScripts(boolean checkForProjectXmlModified) throws IOException {
        genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                NbModuleProject.class.getResource("resources/build-impl.xsl"), // NOI18N
                checkForProjectXmlModified);
        genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                NbModuleProject.class.getResource("resources/build.xsl"), // NOI18N
                checkForProjectXmlModified);
    }
    
    private final class SuiteProviderImpl implements SuiteProvider {

        public File getSuiteDirectory() {
            String suiteDir = evaluator().getProperty("suite.dir"); // NOI18N
            return suiteDir == null ? null : helper.resolveFile(suiteDir);
        }
        
    }
    
    private class NbModuleProviderImpl implements NbModuleProvider, AntProjectListener {
        
        private NbModuleType type;
        
        public NbModuleProviderImpl() {
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
        
        public void propertiesChanged(AntProjectEvent ev) {
            // do not need to react here, type is encoded in project.xml
        }
        public String getSpecVersion() {
            return NbModuleProject.this.getSpecVersion();
        }
        
        public String getCodeNameBase() {
            return NbModuleProject.this.getCodeNameBase();
        }
        
        public String getSourceDirectoryPath() {
            return NbModuleProject.this.getSourceDirectoryPath();
        }
        
        public FileObject getSourceDirectory() {
            return NbModuleProject.this.getSourceDirectory();
        }
        
        public FileObject getManifestFile() {
            return NbModuleProject.this.getManifestFile();
        }
        
        public String getResourceDirectoryPath(boolean inTests) {
            return evaluator().getProperty(inTests ? "test.unit.src.dir" : "src.dir");
        }
        
        public boolean addDependency(String codeNameBase, String releaseVersion,
                SpecificationVersion version,
                boolean useInCompiler) throws IOException {
            return Util.addDependency(NbModuleProject.this, codeNameBase, releaseVersion, version, useInCompiler);
        }
        
        public SpecificationVersion getDependencyVersion(String codenamebase) throws IOException {
            ModuleList moduleList = getModuleList();
            ModuleEntry entry = moduleList.getEntry(codenamebase); // NOI18N
            SpecificationVersion current = new SpecificationVersion(entry.getSpecificationVersion());
            return current;
            
        }
        
        public String getProjectFilePath() {
            return "nbproject/project.xml";
        }
        
        public File getActivePlatformLocation() {
            return NbModuleProject.this.getPlatformFile(null);
        }

        
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
            "nbm-specific2",         // NOI18N
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

        public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }
    }    

    private final class LocalizedBundleInfoProvider implements LocalizedBundleInfo.Provider {

        private LocalizedBundleInfo bundleInfo;

        public LocalizedBundleInfo getLocalizedBundleInfo() {
            if (bundleInfo == null) {
                Manifest mf = getManifest();
                FileObject srcFO = getSourceDirectory();
                if (mf != null && srcFO != null) {
                    bundleInfo = Util.findLocalizedBundleInfo(srcFO, getManifest());
                }
                if (bundleInfo != null) {
                    bundleInfo.addPropertyChangeListener(getLookup().lookup(Info.class));
                }
                if (mf != null) {
                    getManifestFile().addFileChangeListener(new FileChangeAdapter() {
                        public @Override void fileChanged(FileEvent fe) {
                            // cannot reload manifest-dependent things immediately (see 67961 for more details)
                            bundleInfo = null;
                        }
                    });
                }
            }
            return bundleInfo;
        }
    }

}
