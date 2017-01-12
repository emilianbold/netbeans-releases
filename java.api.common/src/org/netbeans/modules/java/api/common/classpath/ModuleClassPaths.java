/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.ModuleElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.CommonModuleUtils;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.preprocessorbridge.api.ModuleUtilities;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import static org.netbeans.spi.java.classpath.ClassPathImplementation.PROP_RESOURCES;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleClassPaths {
    private static final Logger LOG = Logger.getLogger(ModuleClassPaths.class.getName());
    /**
     * Changes from ClassIndex are collapsed using runWhenScanFinished and need to be fired asynchronously
     * to make changes done by invalidate visible due to ParserManager.parse nesting.
     */
    private static final RequestProcessor CLASS_INDEX_FIRER = new RequestProcessor(ModuleClassPaths.class);

    private ModuleClassPaths() {
        throw new IllegalArgumentException("No instance allowed."); //NOI18N
    }

    @NonNull
    static ClassPathImplementation createModuleInfoBasedPath(
            @NonNull final ClassPath systemModules,
            @NonNull final SourceRoots sourceRoots,
            @NullAllowed final Function<URL,Boolean> filter) {
        return new ModuleInfoClassPathImplementation(
                systemModules,
                sourceRoots,
                null,
                null,
                filter);
    }

    @NonNull
    static ClassPathImplementation createModuleInfoBasedPath(
            @NonNull final ClassPath modulePath,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final ClassPath systemModules,
            @NonNull final ClassPath legacyClassPath,
            @NullAllowed final Function<URL,Boolean> filter) {
        Parameters.notNull("systemModules", systemModules); //NOI18N
        Parameters.notNull("legacyClassPath", legacyClassPath); //NOI18N
        return new ModuleInfoClassPathImplementation(
                modulePath,
                sourceRoots,
                systemModules,
                legacyClassPath,
                filter);
    }

    @NonNull
    static ClassPathImplementation createPlatformModulePath(
            @NonNull final PropertyEvaluator eval,
            @NonNull final String platformType) {
        return new PlatformModulePath(eval, platformType);
    }
    
    static ClassPathImplementation createPropertyBasedModulePath(
            @NonNull final File projectDir,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String... props) {
        return createPropertyBasedModulePath(projectDir, eval, null, props);
    }
    
    @NonNull
    static ClassPathImplementation createPropertyBasedModulePath(
            @NonNull final File projectDir,
            @NonNull final PropertyEvaluator eval,
            @NullAllowed final Function<URL,Boolean> filter,
            @NonNull final String... props) {
        return new PropertyModulePath(projectDir, eval, filter, props);
    }

    private static final class PlatformModulePath extends BaseClassPathImplementation implements PropertyChangeListener {
        private static final String PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N

        private final PropertyEvaluator eval;
        private final String platformType;

        PlatformModulePath(
                @NonNull final PropertyEvaluator eval,
                @NonNull final String platformType) {
            Parameters.notNull("evel", eval);   //NOI18N
            Parameters.notNull("platformType", platformType);   //NOI18N
            this.eval = eval;
            this.platformType = platformType;
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
            final JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            if (res != null) {
                return res;
            }
            res = createResources();
            synchronized (this) {
                assert res != null;
                if (getCache() == null) {
                    setCache(res);
                } else {
                    res = getCache();
                }
            }
            return res;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null ||
                ProjectProperties.PLATFORM_ACTIVE.equals(propName) ||
                (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(propName) && isActivePlatformChange())) {
                resetCache(true);
            }
        }

        private boolean isActivePlatformChange() {
            List<PathResourceImplementation> current = getCache();
            if (current == null) {
                return false;
            }
            final Stream<JavaPlatform> platforms = getPlatforms();
            return platforms.findFirst().isPresent() ?
                current.isEmpty() :
                !current.isEmpty();
        }

        private List<PathResourceImplementation> createResources() {
            final List<PathResourceImplementation> res = new ArrayList<>();
            getPlatforms()
                .flatMap((plat)->plat.getBootstrapLibraries().entries().stream())
                .map((entry) -> entry.getURL())
                .forEach((root)->{res.add(org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(root));});
            return res;
        }

        @NonNull
        private Stream<JavaPlatform> getPlatforms() {
            final String platformName = eval.getProperty(ProjectProperties.PLATFORM_ACTIVE);
            return platformName != null && !platformName.isEmpty() ?
                    Arrays.stream(JavaPlatformManager.getDefault().getInstalledPlatforms())
                        .filter((plat)->
                            platformName.equals(plat.getProperties().get(PLATFORM_ANT_NAME)) &&
                            platformType.equals(plat.getSpecification().getName())) :
                    Stream.empty();
        }
    }

    private static final class PropertyModulePath extends BaseClassPathImplementation implements PropertyChangeListener, FileChangeListener {

        private final File projectDir;
        private final PropertyEvaluator eval;
        private final Function<URL,Boolean> filter;
        private final Set<String> props;
        //@GuardedBy("this")
        private final Set<File> listensOn;

        PropertyModulePath(
            @NonNull final File projectDir,
            @NonNull final PropertyEvaluator eval,
            @NullAllowed final Function<URL,Boolean> filter,
            @NonNull final String... props) {
            Parameters.notNull("projectDir", projectDir);   //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("props", props); //NOI18N
            this.projectDir = projectDir;
            this.eval = eval;
            this.filter = filter == null ?
                    (url) -> true :
                    filter;
            this.props = new LinkedHashSet<>();
            this.listensOn = new HashSet<>();
            Collections.addAll(this.props, props);
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        }

        @Override
        @NonNull
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            if (res == null) {
                final List<PathResourceImplementation> collector = res = new ArrayList<>();
                final Collection<File> modulePathRoots = new HashSet<>();
                props.stream()
                    .map((prop)->eval.getProperty(prop))
                    .flatMap((path)-> {
                        return path == null ?
                            Collections.<String>emptyList().stream() :
                            Arrays.stream(PropertyUtils.tokenizePath(path));
                    })
                    .map((part)->PropertyUtils.resolveFile(projectDir, part))
                    .flatMap((modulePathEntry) -> {
                        if (isArchiveFile(modulePathEntry) || hasModuleInfo(modulePathEntry)) {
                            return Stream.of(modulePathEntry);
                        } else {
                            modulePathRoots.add(modulePathEntry);
                            return findModules(modulePathEntry);
                        }
                    })
                    .forEach((file)->{
                        URL url = FileUtil.urlForArchiveOrDir(file);
                        if (url != null && filter.apply(url) != Boolean.FALSE) {
                            collector.add(org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(url));
                        }
                    });
                synchronized (this) {
                    List<PathResourceImplementation> cv = getCache();
                    if (cv == null) {
                        final Set<File> toAdd = new HashSet<>(modulePathRoots);
                        final Set<File> toRemove = new HashSet<>(listensOn);
                        toAdd.removeAll(listensOn);
                        toRemove.removeAll(modulePathRoots);
                        for (File f : toRemove) {
                            FileUtil.removeFileChangeListener(this, f);
                            listensOn.remove(f);
                        }
                        for (File f : toAdd) {
                            FileUtil.addFileChangeListener(this, f);
                            listensOn.add(f);
                        }
                        LOG.log(
                            Level.FINE,
                            "{0} setting results: {1}, listening on: {2}",    //NOI18N
                            new Object[]{
                                getClass().getSimpleName(),
                                res,
                                listensOn
                            });
                        setCache(res);
                    } else {
                        res = cv;
                    }
                }
            }
            return res;
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || props.contains(propName)) {
                LOG.log(
                    Level.FINER,
                    "{0} propertyChange: {1}",    //NOI18N
                    new Object[]{
                        getClass().getSimpleName(),
                        propName
                    });
                resetCache(true);
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        private void handleFileEvent(@NonNull final FileEvent fe) {
            LOG.log(
                Level.FINER,
                "{0} file event: {1}",    //NOI18N
                new Object[]{
                    getClass().getSimpleName(),
                    fe.getFile()
                });
            resetCache(true);
        }

        private static boolean isArchiveFile(@NonNull final File file) {
            try {
                return FileUtil.isArchiveFile(BaseUtilities.toURI(file).toURL());
            } catch (MalformedURLException mue) {
                LOG.log(
                        Level.WARNING,
                        "Invalid URL for: {0}",
                        file);
                return false;
            }
        }

        private static boolean hasModuleInfo(@NonNull final File file) {
            //Cannot check just presence of module-info.class, the file can be build/classes of
            //an uncompiled project.
            return SourceUtils.getModuleName(FileUtil.urlForArchiveOrDir(file), true) != null;
        }

        @NonNull
        private static Stream<File> findModules(@NonNull final File modulesFolder) {
            //No project's dist folder do File.list
            File[] modules = modulesFolder.listFiles((File f) -> {
                try {
                    return f.isFile() &&
                            !f.getName().startsWith(".") &&
                            FileUtil.isArchiveFile(BaseUtilities.toURI(f).toURL());
                } catch (MalformedURLException e) {
                    Exceptions.printStackTrace(e);
                    return false;
                }
            });
            return modules == null ?
               Stream.empty():
               Arrays.stream(modules);
        }
    }

    private static final class ModuleInfoClassPathImplementation  extends BaseClassPathImplementation implements PropertyChangeListener, ChangeListener, FileChangeListener, ClassIndexListener {

        private static final String MODULE_INFO_JAVA = "module-info.java";   //NOI18N
        private static final String MOD_JAVA_BASE = "java.base";    //NOI18N
        private static final String MOD_JAVA_SE = "java.se";        //NOI18N
        private static final String MOD_ALL_UNNAMED = "ALL-UNNAMED";    //NOI18N
        private static final String JAVA_ = "java.";            //NOI18N
        private static final List<PathResourceImplementation> TOMBSTONE = Collections.unmodifiableList(new ArrayList<>());
        private static final Predicate<ModuleElement> NON_JAVA_PUBEXP = (e) -> 
                !e.getQualifiedName().toString().startsWith(JAVA_) &&
                e.getDirectives().stream()
                    .filter((d) -> d.getKind() == ModuleElement.DirectiveKind.EXPORTS)
                    .anyMatch((d) -> ((ModuleElement.ExportsDirective)d).getTargetModules() == null);
        private final ClassPath base;
        private final SourceRoots sources;
        private final ClassPath systemModules;
        private final ClassPath legacyClassPath;
        private final Function<URL,Boolean> filter;
        private final ThreadLocal<Object[]> selfRes;
        private final AtomicReference<CompilerOptionsQuery.Result> compilerOptions;

        //@GuardedBy("this")
        private ClasspathInfo activeProjectSourceRoots;
        //@GuardedBy("this")
        private volatile boolean rootsChanging;
        //@GuardedBy("this")
        private Collection<File> moduleInfos;

        ModuleInfoClassPathImplementation(
                @NonNull final ClassPath base,
                @NonNull final SourceRoots sources,
                @NullAllowed final ClassPath systemModules,
                @NullAllowed final ClassPath legacyClassPath,
                @NullAllowed final Function<URL,Boolean> filter) {
            super(null);
            Parameters.notNull("base", base);       //NOI18N
            Parameters.notNull("sources", sources); //NOI18N
            this.base = base;
            this.sources = sources;
            this.systemModules = systemModules;
            this.legacyClassPath = legacyClassPath;
            this.filter = filter == null ?
                    (url) -> null :
                    filter;
            this.selfRes = new ThreadLocal<>();
            this.compilerOptions = new AtomicReference<>();
            this.moduleInfos = Collections.emptyList();
            this.base.addPropertyChangeListener(WeakListeners.propertyChange(this, this.base));
            this.sources.addPropertyChangeListener(WeakListeners.propertyChange(this, this.sources));
            if (this.systemModules != null) {
                this.systemModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.systemModules));
            }
            if (this.legacyClassPath != null) {
                this.legacyClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this, this.legacyClassPath));
            }
        }

        @Override
        @NonNull
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            boolean needToFire = false;
            if (res == TOMBSTONE) {
                needToFire = true;
                res = null;
            }
            if (res != null) {
                return res;
            }
            final Object[] bestSoFar = selfRes.get();
            if (bestSoFar != null) {
                bestSoFar[1] = Boolean.TRUE;
                return (List<? extends PathResourceImplementation>) bestSoFar[0];
            }
            final Collection<File> newModuleInfos = new ArrayDeque<>();
            final List<URL> newActiveProjectSourceRoots = new ArrayList<>();
            final Map<String, List<URL>> modulesPatches = getPatches();
            final Map<String,List<URL>> modulesByName = getModulesByName(
                    base,
                    modulesPatches,
                    newActiveProjectSourceRoots);
            Collections.addAll(newActiveProjectSourceRoots, sources.getRootURLs());
            ProjectManager.mutex().readAccess(() -> {
                synchronized (this) {
                    if (activeProjectSourceRoots != null) {
                        activeProjectSourceRoots.getClassIndex().removeClassIndexListener(this);
                        activeProjectSourceRoots = null;
                    }
                    if (!newActiveProjectSourceRoots.isEmpty()) {
                        activeProjectSourceRoots = ClasspathInfo.create(
                                ClassPath.EMPTY,
                                ClassPath.EMPTY,
                                org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(newActiveProjectSourceRoots.toArray(new URL[newActiveProjectSourceRoots.size()])));
                        activeProjectSourceRoots.getClassIndex().addClassIndexListener(this);
                        LOG.log(
                            Level.FINER,
                            "{0} for {1} listening on: {2}",    //NOI18N
                            new Object[]{
                                getClass().getSimpleName(),
                                base,
                                newActiveProjectSourceRoots
                            });
                    }
                }
            });
            if(supportsModules(
                    systemModules != null ? systemModules : base,
                    systemModules != null ? base : ClassPath.EMPTY,
                    sources)) {
                res = modulesByName.values().stream()
                        .flatMap((urls) -> urls.stream())
                        .map((url)->org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(url))
                        .collect(Collectors.toList());
                final List<PathResourceImplementation> selfResResources;
                final ClassPath bootModules;
                final ClassPath userModules;
                if (systemModules != null) {
                    selfResResources = Collections.emptyList();
                    bootModules = systemModules;
                    userModules = base;
                } else {
                    selfResResources = findJavaBase(modulesByName);
                    bootModules = base;
                    userModules = ClassPath.EMPTY;
                }
                LOG.log(
                    Level.FINER,
                    "{0} for {1} self resources: {2}",    //NOI18N
                    new Object[]{
                        ModuleInfoClassPathImplementation.class.getSimpleName(),
                        base,
                        selfResResources
                    });
                LOG.log(
                    Level.FINEST,
                    "{0} for {1} bootModules: {2}, modules: {4}",    //NOI18N
                    new Object[]{
                        ModuleInfoClassPathImplementation.class.getSimpleName(),
                        base,
                        bootModules,
                        modulesByName
                    });
                selfRes.set(new Object[]{
                    selfResResources,
                    needToFire});
                try {
                    FileObject found = null;
                    for (URL root : sources.getRootURLs()) {
                        try {
                            final File moduleInfo = FileUtil.normalizeFile(new File(BaseUtilities.toFile(root.toURI()),MODULE_INFO_JAVA));
                            newModuleInfos.add(moduleInfo);
                            if (found == null) {
                                found = FileUtil.toFileObject(moduleInfo);
                            }
                        } catch (URISyntaxException e) {
                            LOG.log(
                                Level.WARNING,
                                "Invalid URL: {0}, reason: {1}",    //NOI18N
                                new Object[]{
                                    root,
                                    e.getMessage()
                                });
                        }
                    }
                    final List<PathResourceImplementation> bcprs = systemModules != null ?
                                findJavaBase(getModulesByName(systemModules, modulesPatches, null)) :
                                selfResResources;   //java.base
                    final ClassPath bootCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(bcprs);
                    final JavaSource src;
                    final Predicate<ModuleElement> rootModulesPredicate;
                    final String xmodule;
                    if (found != null) {                        
                        src = JavaSource.create(
                                new ClasspathInfo.Builder(bootCp)
                                        .setModuleBootPath(bootModules)
                                        .setModuleCompilePath(userModules)
                                        .setSourcePath(org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(sources.getRootURLs()))
                                        .build(),
                                found);
                        final Set<String> additionalModules = getAddMods();
                        additionalModules.remove(MOD_ALL_UNNAMED);
                        rootModulesPredicate = ModuleNames.create(additionalModules);
                        xmodule = null;
                    } else {
                        src = JavaSource.create(
                                new ClasspathInfo.Builder(bootCp)
                                        .setModuleBootPath(bootModules)
                                        .setModuleCompilePath(userModules)
                                        .setSourcePath(org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(sources.getRootURLs()))
                                        .build());
                        final Set<String> additionalModules = getAddMods();
                        additionalModules.remove(MOD_ALL_UNNAMED);
                        if (systemModules == null) {
                            additionalModules.add(MOD_JAVA_SE);
                            rootModulesPredicate = ModuleNames.create(additionalModules)
                                    .or(NON_JAVA_PUBEXP);
                        } else {
                            rootModulesPredicate = ModuleNames.create(additionalModules);
                        }
                        xmodule = Optional.ofNullable(getXModule())
                                .filter((n) -> modulesByName.keySet().contains(n))
                                .orElse(null);
                    }
                    boolean dependsOnUnnamed = false;
                    if (src != null) {
                        try {
                            final ModuleUtilities mu = ModuleUtilities.get(src);
                            if (mu != null) {
                                final Set<URL> requires = new HashSet<>();
                                if (found != null || xmodule != null) {
                                    final ModuleElement myModule;
                                    if (found != null) {
                                        myModule = mu.parseModule();
                                    } else {
                                        final List<URL> xmoduleLocs = modulesByName.get(xmodule);
                                        myModule = mu.resolveModule(xmodule);
                                        if (myModule != null) {
                                            requires.addAll(xmoduleLocs);
                                        }
                                    }
                                    if (myModule != null) {
                                        dependsOnUnnamed = dependsOnUnnamed(myModule, true);
                                        requires.addAll(collectRequiredModules(myModule, true, false, modulesByName));
                                    }
                                } else {
                                    //Unnamed module
                                    dependsOnUnnamed = true;
                                    for (String moduleName : modulesByName.keySet()) {
                                        Optional.ofNullable(mu.resolveModule(moduleName))
                                                .filter(rootModulesPredicate)
                                                .map((m) -> collectRequiredModules(m, true, true, modulesByName))
                                                .ifPresent(requires::addAll);
                                    }
                                }
                                res = filterModules(res, requires, filter);
                            }
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                    if (dependsOnUnnamed) {
                        //Unnamed module - add legacy classpath to classpath.
                        if (legacyClassPath != null) {
                            final List<ClassPath.Entry> legacyEntires = legacyClassPath.entries();
                            final List<PathResourceImplementation> tmp = new ArrayList<>(res.size() + legacyEntires.size());
                            legacyEntires.stream()
                                    .map((e)->org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(e.getURL()))
                                    .forEach(tmp::add);
                            tmp.addAll(res);
                            res = tmp;
                        }
                    }
                } finally {
                    needToFire = selfRes.get()[1] == Boolean.TRUE;
                    selfRes.remove();
                }
            } else {
                res = Collections.emptyList();
            }
            synchronized (this) {
                assert res != null;
                List<PathResourceImplementation> ccv = getCache();
                if (ccv == null || ccv == TOMBSTONE) {
                    LOG.log(
                        Level.FINE,
                        "{0} for {1} setting results: {2}, listening on: {3}",    //NOI18N
                        new Object[]{
                            getClass().getSimpleName(),
                            base,
                            res,
                            newModuleInfos
                        });
                    setCache(res);
                    final Collection<File> added = new ArrayList<>(newModuleInfos);
                    added.removeAll(moduleInfos);
                    final Collection<File> removed = new ArrayList<>(moduleInfos);
                    removed.removeAll(newModuleInfos);
                    removed.stream().forEach((f) -> FileUtil.removeFileChangeListener(this, f));
                    added.stream().forEach((f) -> FileUtil.addFileChangeListener(this, f));
                    moduleInfos = newModuleInfos;
                } else {
                    res = ccv;
                }
            }
            if (needToFire) {
                fire();
            }
            return res;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || ClassPath.PROP_ENTRIES.equals(propName) || SourceRoots.PROP_ROOTS.equals(propName)) {
                resetOutsideWriteAccess(null);
            }
        }
        
        @Override
        public void stateChanged(@NonNull final ChangeEvent evt) {
            resetOutsideWriteAccess(null);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            resetOutsideWriteAccess(fe.getFile());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            resetOutsideWriteAccess(fe.getFile());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            final ClasspathInfo info = ClasspathInfo.create(
                ClassPath.EMPTY,
                ClassPath.EMPTY,
                org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(sources.getRootURLs()));
            final Set<ElementHandle<ModuleElement>> mods = info.getClassIndex().getDeclaredModules(
                    "", //NOI18N
                    ClassIndex.NameKind.PREFIX,
                    EnumSet.of(ClassIndex.SearchScope.SOURCE));
            if (mods.isEmpty()) {
                resetOutsideWriteAccess(null);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            resetOutsideWriteAccess(fe.getFile());
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void rootsAdded(final RootsEvent event) {
        }

        @Override
        public void rootsRemoved(RootsEvent event) {
        }

        @Override
        public void typesAdded(TypesEvent event) {
            handleModuleChange(event);
        }

        @Override
        public void typesRemoved(TypesEvent event) {
            handleModuleChange(event);
        }

        @Override
        public void typesChanged(TypesEvent event) {
            handleModuleChange(event);
        }
        
        private void resetOutsideWriteAccess(FileObject artifact) {
            final boolean hasDocExclusiveLock = Optional.ofNullable(artifact)
                    .map((fo) -> {
                        try {
                            return DataObject.find(fo).getLookup().lookup(EditorCookie.class);
                        } catch (DataObjectNotFoundException e) {
                            return null;
                        }
                    })
                    .map((ec) -> ec.getDocument())
                    .map(DocumentUtilities::isWriteLocked)
                    .orElse(Boolean.FALSE);
            final Runnable action = () -> resetCache(TOMBSTONE, true);
            if (hasDocExclusiveLock) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(
                            Level.WARNING,
                            "Firing under editor write lock: {0}",   //NOI18N
                            Arrays.toString(Thread.currentThread().getStackTrace()));
                }
                CLASS_INDEX_FIRER.execute(action);
            } else if (ProjectManager.mutex().isWriteAccess()) {
                ProjectManager.mutex().postReadRequest(action);
            } else {
                action.run();
            }
        }

        private void handleModuleChange(@NonNull final TypesEvent event) {
            if (event.getModule() != null) {
                ClasspathInfo info;
                synchronized (this) {
                    info = activeProjectSourceRoots;
                    if (info != null) {
                        if (rootsChanging) {
                            info = null;
                        } else {
                            rootsChanging = true;
                        }
                    }                    
                }
                if (info != null) {
                    try {
                        JavaSource.create(info).runWhenScanFinished((cc)->{
                            LOG.log(
                                Level.FINER,
                                "{0} for {1} got class index event due to change of module {2} in {3}",    //NOI18N
                                new Object[]{
                                    ModuleInfoClassPathImplementation.class.getSimpleName(),
                                    base,
                                    event.getModule().getQualifiedName(),
                                    event.getRoot()
                                });
                            rootsChanging = false;
                            CLASS_INDEX_FIRER.execute(()->resetCache(TOMBSTONE, true));
                        },
                        true);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }
        
        @CheckForNull
        private CompilerOptionsQuery.Result getCompilerOptions() {
            CompilerOptionsQuery.Result res = compilerOptions.get();
            if (res == null) {
                final FileObject[] roots = sources.getRoots();
                res = roots.length == 0 ?
                        null :
                        CompilerOptionsQuery.getOptions(roots[0]);
                if (res != null) {
                    if (compilerOptions.compareAndSet(null, res)) {
                        res.addChangeListener(WeakListeners.change(this, res));
                    } else {
                        res = compilerOptions.get();
                        assert res != null;
                    }
                }
            }
            return res;
        }
        
        private Set<String> getAddMods() {
            final CompilerOptionsQuery.Result res = getCompilerOptions();
            return res == null ?
                    new HashSet<>() :
                    CommonModuleUtils.getAddModules(res);
        }

        @CheckForNull
        private String getXModule() {
            final CompilerOptionsQuery.Result res = getCompilerOptions();
            return res == null ?
                    null :
                    CommonModuleUtils.getXModule(res);
        }

        @NonNull
        private Map<String,List<URL>> getPatches() {
            final CompilerOptionsQuery.Result res = getCompilerOptions();
            return res == null ?
                    Collections.emptyMap() :
                    CommonModuleUtils.getPatches(res);
        }

        @NonNull
        private static Map<String,List<URL>> getModulesByName(
                @NonNull final ClassPath cp,
                @NonNull final Map<String,List<URL>> patches,
                @NullAllowed final Collection<URL> projectSourceRoots) {
            final Map<String,List<URL>> res = new LinkedHashMap<>();
            cp.entries().stream()
                    .map((entry)->entry.getURL())
                    .forEach((url)-> {
                        if (projectSourceRoots != null) {
                            final SourceForBinaryQuery.Result2 sfbqRes = SourceForBinaryQuery.findSourceRoots2(url);
                            if (sfbqRes.preferSources()) {
                                Arrays.stream(sfbqRes.getRoots())
                                        .map((fo)->fo.toURL())
                                        .forEach(projectSourceRoots::add);
                            }
                        }
                        final String moduleName = SourceUtils.getModuleName(url, true);
                        if (moduleName != null) {
                            final List<URL> roots = new ArrayList<>();
                            Optional.ofNullable(patches.get(moduleName))
                                    .ifPresent(roots::addAll);
                            roots.add(url);
                            res.put(moduleName, roots);
                        }
                    });
            return res;
        }

        @NonNull
        private static List<PathResourceImplementation> findJavaBase(final Map<String,List<URL>> modulesByName) {
            return Optional.ofNullable(modulesByName.get(MOD_JAVA_BASE))
                .map((urls) -> {
                    return urls.stream()
                            .map(org.netbeans.spi.java.classpath.support.ClassPathSupport::createResource)
                            .collect(Collectors.toList());                    
                })
                .orElseGet(Collections::emptyList);
        }
        
        private static boolean dependsOnUnnamed(
                @NonNull final ModuleElement module,
                final boolean transitive) {
            return dependsOnUnnamed(module, transitive, true, new HashSet<>());
        }
        
        private static boolean dependsOnUnnamed(
                @NonNull final ModuleElement module,
                final boolean transitive,
                final boolean topLevel,
                final Set<ModuleElement> seen) {
            if (module.isUnnamed()) {
                return true;
            }
            if (seen.add(module)) {
                for (ModuleElement.Directive d : module.getDirectives()) {
                    if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                        final ModuleElement.RequiresDirective rd = (ModuleElement.RequiresDirective) d;
                        if (topLevel || (transitive && rd.isTransitive())) {
                            if (dependsOnUnnamed(rd.getDependency(), transitive, false, seen)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        @NonNull
        private static Set<URL> collectRequiredModules(
                @NonNull final ModuleElement module,
                final boolean transitive,
                final boolean includeTopLevel,
                @NonNull final Map<String,List<URL>> modulesByName) {
            final Set<URL> res = new HashSet<>();
            final Set<ModuleElement> seen = new HashSet<>();
            if (includeTopLevel) {
                final List<URL> moduleLocs = modulesByName.get(module.getQualifiedName().toString());
                if (moduleLocs != null) {
                    res.addAll(moduleLocs);
                }
            }
            collectRequiredModulesImpl(module, transitive, !includeTopLevel, modulesByName, seen, res);
            return res;
        }

        private static boolean collectRequiredModulesImpl(
                @NullAllowed final ModuleElement module,
                final boolean transitive,
                final boolean topLevel,
                @NonNull final Map<String,List<URL>> modulesByName,
                @NonNull final Collection<? super ModuleElement> seen,
                @NonNull final Collection<? super URL> c) {
            if (module != null && seen.add(module) && !module.isUnnamed()) {
                for (ModuleElement.Directive directive : module.getDirectives()) {
                    if (directive.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                        ModuleElement.RequiresDirective req = (ModuleElement.RequiresDirective) directive;
                        if (topLevel || req.isTransitive()|| isMandated(req)) {
                            final ModuleElement dependency = req.getDependency();
                            boolean add = true;
                            if (transitive) {
                                add = collectRequiredModulesImpl(dependency, transitive, false, modulesByName, seen, c);
                            }
                            if (add) {
                                final List<URL> dependencyURLs = modulesByName.get(dependency.getQualifiedName().toString());
                                if (dependencyURLs != null) {
                                    c.addAll(dependencyURLs);
                                }
                            }
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        @NonNull
        private static List<PathResourceImplementation> filterModules(
                @NonNull List<PathResourceImplementation> modules,
                @NonNull Set<URL> requires,
                @NonNull final Function<URL,Boolean> filter) {
            final List<PathResourceImplementation> res = new ArrayList<>(modules.size());
            for (PathResourceImplementation pr : modules) {
                for (URL url : pr.getRoots()) {
                    final Boolean vote = filter.apply(url);
                    if (vote == Boolean.TRUE || (vote == null && requires.contains(url))) {
                        res.add(pr);
                    }
                }
            }
            return res;
        }
        
        private static boolean supportsModules(
            @NonNull final ClassPath boot,
            @NonNull final ClassPath compile,
            @NonNull final SourceRoots src) {
            if (boot.findResource("java/util/zip/CRC32C.class") != null) {  //NOI18N
                return true;
            }
            if (compile.findResource("java/util/zip/CRC32C.class") != null) {   //NOI18N
                return true;
            }
            return org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(src.getRootURLs())
                    .findResource("java/util/zip/CRC32C.java") != null;   //NOI18N
        }
        
        private static boolean isMandated(@NonNull final ModuleElement.RequiresDirective rd) {
            return Optional.ofNullable(rd.getDependency())
                    .map((me) -> MOD_JAVA_BASE.equals(me.getQualifiedName().toString()))
                    .orElse(Boolean.FALSE);
        }
        
        private static final class ModuleNames implements Predicate<ModuleElement> {
            private final Set<? extends String> moduleNames;

            private ModuleNames(@NonNull final Set<? extends String> names) {
                this.moduleNames = names;
            }

            @Override
            public boolean test(ModuleElement t) {
                return moduleNames.contains(t.getQualifiedName().toString());
            }       

            @NonNull
            static Predicate<ModuleElement> create(@NonNull final Set<? extends String> name) {
                return new ModuleNames(name);
            }
        }
    }

    private static abstract class BaseClassPathImplementation implements ClassPathImplementation {

        private final PropertyChangeSupport listeners;
        //@GuardedBy("this")
        private List<PathResourceImplementation> cache;

        BaseClassPathImplementation() {
            this(null);
        }

        BaseClassPathImplementation(final List<PathResourceImplementation> initialValue) {
            this.listeners = new PropertyChangeSupport(this);
            synchronized (this) {
                this.cache = initialValue;
            }
        }

        @Override
        public final void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.addPropertyChangeListener(listener);
        }

        @Override
        public final void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.removePropertyChangeListener(listener);
        }

        @CheckForNull
        final synchronized List<PathResourceImplementation> getCache() {
            return this.cache;
        }

        final synchronized void setCache(@NullAllowed final List<PathResourceImplementation> cache) {
            this.cache = cache;
        }

        final void resetCache(final boolean fire) {
            resetCache(null, fire);
        }

        final void resetCache(
                @NullAllowed final List<PathResourceImplementation> update,
                final boolean fire) {
            synchronized (this) {
                this.cache = update;
            }
            if (fire) {
                fire();
            }
        }

        final void fire() {
            this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
        }
    }

}
