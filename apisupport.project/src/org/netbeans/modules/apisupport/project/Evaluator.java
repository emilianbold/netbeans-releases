/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;

/**
 * Property evaluator for {@link NbModuleProject}.
 * Has two special behaviors of note:
 * 1. Does not call ModuleList until it really needs to.
 * 2. Is reset upon project.xml changes.
 * @author Jesse Glick, Martin Krauskopf
 */
final class Evaluator implements PropertyEvaluator, PropertyChangeListener, AntProjectListener {
    
    private final NbModuleProject project;
    private final NbModuleTypeProvider typeProvider;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private PropertyEvaluator delegate;
    private boolean loadedModuleList = false;
    
    /** See issue #69440 for more details. */
    private boolean runInAtomicAction;
    
    public Evaluator(NbModuleProject project, NbModuleTypeProvider typeProvider) {
        this.project = project;
        this.typeProvider = typeProvider;
        delegate = createEvaluator(null);
        delegate.addPropertyChangeListener(this);
        project.getHelper().addAntProjectListener(this);
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
                Util.err.log("Needed to reset evaluator in " + project + "due to use of module-list-dependent property; now cp=" + delegate.getProperty("cp"));
            }
        }
        return delegate;
    }
    
    private void reset() {
        loadedModuleList = true;
        delegate.removePropertyChangeListener(this);
        try {
            delegate = createEvaluator(project.getModuleList());
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
            Util.err.log("Refiring property change from delegate in " + evt.getPropertyName() + " for " + project);
            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }
    
    public void configurationXmlChanged(AntProjectEvent ev) {
        if (!runInAtomicAction && ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            reset();
        }
    }
    
    public void propertiesChanged(AntProjectEvent ev) {}
    
    /** See issue #69440 for more details. */
    public void setRunInAtomicAction(boolean runInAtomicAction) {
        assert ProjectManager.mutex().isWriteAccess();
        this.runInAtomicAction = runInAtomicAction;
    }
    
    public void removeListeners() {
        project.getHelper().removeAntProjectListener(this);
        delegate.removePropertyChangeListener(this);
    }
    
    /**
     * Create a property evaluator: private project props, shared project props, various defaults.
     * Synch with nbbuild/templates/projectized.xml.
     * @param ml this module list, or may be left null to skip all properties which require knowledge of other modules
     */
    private PropertyEvaluator createEvaluator(ModuleList ml) {
        // XXX a lot of this duplicates ModuleList.parseProperties... can they be shared?
        PropertyProvider predefs = project.getHelper().getStockPropertyPreprovider();
        Map/*<String,String>*/ stock = new HashMap();
        File dir = FileUtil.toFile(project.getProjectDirectory());
        NbModuleTypeProvider.NbModuleType type = typeProvider.getModuleType();
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
            ModuleEntry thisEntry = ml.getEntry(project.getCodeNameBase());
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
            providers.add(project.getHelper().getPropertyProvider("nbproject/private/suite-private.properties")); // NOI18N
            providers.add(project.getHelper().getPropertyProvider("nbproject/suite.properties")); // NOI18N
            PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
            String suiteDirS = baseEval.getProperty("suite.dir"); // NOI18N
            if (suiteDirS != null) {
                File suiteDir = PropertyUtils.resolveFile(dir, suiteDirS);
                providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(suiteDir, "nbproject" + File.separatorChar + "private" + File.separatorChar + "platform-private.properties"))); // NOI18N
                providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(suiteDir, "nbproject" + File.separatorChar + "platform.properties"))); // NOI18N
            }
        } else if (type == NbModuleTypeProvider.STANDALONE) {
            providers.add(project.getHelper().getPropertyProvider("nbproject/private/platform-private.properties")); // NOI18N
            providers.add(project.getHelper().getPropertyProvider("nbproject/platform.properties")); // NOI18N
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
        if (type == NbModuleTypeProvider.NETBEANS_ORG) {
            // For local definitions of nbjdk.* properties:
            File nbbuild = new File(nbroot, "nbbuild"); // NOII18N
            providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(nbbuild, "user.build.properties"))); // NOI18N
            providers.add(PropertyUtils.propertiesFilePropertyProvider(new File(nbbuild, "site.build.properties"))); // NOI18N
        }
        PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
        providers.add(new NbJdkProvider(baseEval));
        providers.add(project.getHelper().getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        providers.add(project.getHelper().getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        Map/*<String,String>*/ defaults = new HashMap();
        defaults.put("code.name.base.dashes", project.getCodeNameBase().replace('.', '-')); // NOI18N
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
            buildDefaults.put("run.cp", computeRuntimeModuleClasspath(ml) + ":${cp.extra}:${build.classes.dir}"); // NOI18N
            baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
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
                        "${netbeans.dest.dir}/ide8/modules/ext/junit-3.8.2.jar:" + // NOI18N
                        "${netbeans.dest.dir}/testtools/modules/ext/nbjunit.jar:" + // NOI18N
                        "${netbeans.dest.dir}/testtools/modules/ext/insanelib.jar:" + // NOI18N
                        "${netbeans.dest.dir}/testtools/modules/org-netbeans-modules-nbjunit.jar:" + // NOI18N, new for 6.0
                        "${netbeans.dest.dir}/testtools/modules/org-netbeans-modules-nbjunit-ide.jar:" + // NOI18N, new for 6.0
                        "${netbeans.home}/../ide6/modules/ext/junit-3.8.1.jar:" + // NOI18N
                        "${netbeans.home}/../ide8/modules/ext/junit-3.8.2.jar:" + // NOI18N
                        "${netbeans.home}/../testtools/modules/ext/nbjunit.jar:" + // NOI18N
                        "${netbeans.home}/../testtools/modules/ext/insanelib.jar:" + // NOI18N
                        "${netbeans.home}/../testtools/modules/org-netbeans-modules-nbjunit.jar:" + // NOI18N, new for 6.0
                        "${netbeans.home}/../testtools/modules/org-netbeans-modules-nbjunit-ide.jar:" + // NOI18N, new for 6.0
                        "${netbeans.user}/modules/ext/nbjunit.jar:" + // NOI18N
                        "${netbeans.user}/modules/ext/insanelib.jar:" + // NOI18N
                        "${netbeans.dest.dir}/../../xtest/lib/junit.jar:" + // NOI18N
                        "${netbeans.dest.dir}/../../xtest/lib/nbjunit.jar:" + // NOI18N
                        "${netbeans.dest.dir}/../../xtest/lib/insanelib.jar:" + // NOI18N
                        "${netbeans.user}/modules/org-netbeans-modules-nbjunit.jar:" + // NOI18N, new for 6.0
                        "${netbeans.user}/modules/org-netbeans-modules-nbjunit-ide.jar"; // NOI18N, new for 6.0
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
    
    private final class NbJdkProvider implements PropertyProvider, PropertyChangeListener { // #63541: JDK selection
        
        private final PropertyEvaluator eval;
        private final List/*<ChangeListener>*/ listeners = new ArrayList();
        private final PropertyChangeListener weakListener = WeakListeners.propertyChange(this, null);
        
        public NbJdkProvider(PropertyEvaluator eval) {
            this.eval = eval;
            eval.addPropertyChangeListener(weakListener);
            JavaPlatformManager.getDefault().addPropertyChangeListener(weakListener);
        }
        
        public final Map/*<String,String>*/ getProperties() {
            Map/*<String,String>*/ props = new HashMap();
            String home = eval.getProperty("nbjdk.home"); // NOI18N
            if (home == null) {
                String active = eval.getProperty("nbjdk.active"); // NOI18N
                if (active != null && !active.equals("default")) { // NOI18N
                    home = eval.getProperty("platforms." + active + ".home"); // NOI18N
                    if (home != null) {
                        props.put("nbjdk.home", home); // NOI18N
                    }
                }
            }
            if (home == null) {
                JavaPlatform platform = JavaPlatformManager.getDefault().getDefaultPlatform();
                if (platform != null) {
                    Collection/*<FileObject>*/ installs = platform.getInstallFolders();
                    if (installs.size() == 1) {
                        home = FileUtil.toFile((FileObject) installs.iterator().next()).getAbsolutePath();
                    }
                }
            }
            String bootcp = null;
            if (home != null) {
                FileObject homeFO = FileUtil.toFileObject(new File(home));
                if (homeFO != null) {
                    JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
                    for (int i = 0; i < platforms.length; i++) {
                        if (new HashSet(platforms[i].getInstallFolders()).equals(Collections.singleton(homeFO))) {
                            // Matching JDK is registered, so look up its real bootcp.
                            StringBuffer bootcpSB = new StringBuffer();
                            ClassPath boot = platforms[i].getBootstrapLibraries();
                            boot.removePropertyChangeListener(weakListener);
                            boot.addPropertyChangeListener(weakListener);
                            Iterator/*<ClassPath.Entry>*/ entries = boot.entries().iterator();
                            while (entries.hasNext()) {
                                ClassPath.Entry entry = (ClassPath.Entry) entries.next();
                                URL u = entry.getURL();
                                if (u.toExternalForm().endsWith("!/")) { // NOI18N
                                    URL nested = FileUtil.getArchiveFile(u);
                                    if (nested != null) {
                                        u = nested;
                                    }
                                }
                                if ("file".equals(u.getProtocol())) {
                                    File f = new File(URI.create(u.toExternalForm()));
                                    if (bootcpSB.length() > 0) {
                                        bootcpSB.append(File.pathSeparatorChar);
                                    }
                                    bootcpSB.append(f.getAbsolutePath());
                                }
                            }
                            bootcp = bootcpSB.toString();
                            break;
                        }
                    }
                }
                if (bootcp == null) {
                    bootcp = "${nbjdk.home}/jre/lib/rt.jar".replace('/', File.separatorChar); // NOI18N
                }
            }
            if (bootcp == null) {
                // Real fallback...
                bootcp = "${sun.boot.class.path}"; // NOI18N
            }
            props.put("nbjdk.bootclasspath", bootcp); // NOI18N
            props.put("tools.jar", "${nbjdk.home}/lib/tools.jar".replace('/', File.separatorChar)); // NOI18N
            if (Util.err.isLoggable(ErrorManager.INFORMATIONAL)) {
                Map/*<String,String>*/ _props = new TreeMap(eval.getProperties());
                Iterator it = _props.entrySet().iterator();
                while (it.hasNext()) {
                    String k = (String) ((Map.Entry) it.next()).getKey();
                    if (!k.startsWith("nbjdk.") && !k.startsWith("platforms.")) { // NOI18N
                        it.remove();
                    }
                }
                _props.putAll(props);
                Util.err.log("JDK-related properties of " + project + ": " + _props);
            }
            return props;
        }
        
        public final void addChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
        
        public final void removeChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
        }
        
        public final void propertyChange(PropertyChangeEvent evt) {
            String p = evt.getPropertyName();
            if (p != null && !p.startsWith("nbjdk.") && !p.startsWith("platforms.") && // NOI18N
                    !p.equals(ClassPath.PROP_ENTRIES) && !p.equals(JavaPlatformManager.PROP_INSTALLED_PLATFORMS)) {
                return;
            }
            final ChangeEvent ev = new ChangeEvent(this);
            final Iterator it;
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    return;
                }
                it = new HashSet(listeners).iterator();
            }
            final Mutex.Action action = new Mutex.Action() {
                public Object run() {
                    while (it.hasNext()) {
                        ((ChangeListener) it.next()).stateChanged(ev);
                    }
                    return null;
                }
            };
            // See ProjectProperties.PP.fireChange for explanation of this threading stuff:
            if (ProjectManager.mutex().isWriteAccess()) {
                ProjectManager.mutex().readAccess(action);
            } else if (ProjectManager.mutex().isReadAccess()) {
                action.run();
            } else {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        ProjectManager.mutex().readAccess(action);
                    }
                });
            }
        }
        
    }
    
    /**
     * Get an Ant location for the root of jemmy.jar.
     */
    private String findJemmyJar(PropertyEvaluator eval) {
        File f = project.getNbrootFile("jemmy/builds/jemmy.jar", eval); // NOI18N
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
        File f = project.getNbrootFile("jellytools/builds/jelly2-nb.jar", eval); // NOI18N
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
        Element data = project.getHelper().getPrimaryConfigurationData(true);
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
                Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + FileUtil.getFileDisplayName(project.getProjectDirectory()));
                continue;
            }
            File moduleJar = module.getJarLocation();
            if (cp.length() > 0) {
                cp.append(File.pathSeparatorChar);
            }
            cp.append(moduleJar.getAbsolutePath());
            cp.append(module.getClassPathExtensions());
        }
        ModuleEntry myself = ml.getEntry(project.getCodeNameBase());
        if (myself == null) {
            // ???
            return "";
        }
        cp.append(myself.getClassPathExtensions());
        return cp.toString();
    }
    
    /**
     * Follows transitive runtime dependencies.
     * @see "issue #70206"
     */
    private String computeRuntimeModuleClasspath(ModuleList ml) {
        Set/*<String>*/ unprocessed = new HashSet();
        unprocessed.add(project.getCodeNameBase());
        Set/*<String>*/ processed = new HashSet();
        StringBuffer cp = new StringBuffer();
        while (!unprocessed.isEmpty()) { // crude breadth-first search
            Iterator it = unprocessed.iterator();
            String cnb = (String) it.next();
            it.remove();
            if (processed.add(cnb)) {
                ModuleEntry module = ml.getEntry(cnb);
                if (module == null) {
                    Util.err.log(ErrorManager.WARNING, "Warning - could not find dependent module " + cnb + " for " + FileUtil.getFileDisplayName(project.getProjectDirectory()));
                    continue;
                }
                if (!cnb.equals(project.getCodeNameBase())) { // build/classes for this is special
                    if (cp.length() > 0) {
                        cp.append(File.pathSeparatorChar);
                    }
                    cp.append(module.getJarLocation().getAbsolutePath());
                    cp.append(module.getClassPathExtensions());
                }
                String[] newDeps = module.getRunDependencies();
                unprocessed.addAll(Arrays.asList(newDeps));
            }
        }
        return cp.toString();
    }
    
}
