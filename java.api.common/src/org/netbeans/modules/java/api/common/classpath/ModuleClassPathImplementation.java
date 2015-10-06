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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ModuleElement;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleClassPathImplementation  implements ClassPathImplementation, PropertyChangeListener, FileChangeListener {

    private static final Logger LOG = Logger.getLogger(ModuleClassPathImplementation.class.getName());
    private static final String PLATFORM_ACTIVE = "platform.active"; // NOI18N
    private static final String MODULE_INFO = "module-info.java";   //NOI18N
    private static final String PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N
    private static final String PROTOCOL_NBJRT = "nbjrt";   //NOI18N

    private final SourceRoots sources;
    private final PropertyEvaluator eval;
    private final PropertyChangeSupport listeners;
    private final ThreadLocal<Object[]> selfRes;

    //@GuardedBy("this")
    private List<PathResourceImplementation> cache;
    //@GuardedBy("this")
    private Collection<File> moduleInfos;

    ModuleClassPathImplementation(
            @NonNull final SourceRoots sources,
            @NonNull final PropertyEvaluator eval) {
        Parameters.notNull("sources", sources); //NOI18N
        Parameters.notNull("eval", eval);   //NOI18N
        this.sources = sources;
        this.eval = eval;
        this.listeners = new PropertyChangeSupport(this);
        this.selfRes = new ThreadLocal<>();
        this.moduleInfos = Collections.emptyList();
        this.sources.addPropertyChangeListener(WeakListeners.propertyChange(this, this.sources));
        this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
    }

    @Override
    @NonNull
    public List<? extends PathResourceImplementation> getResources() {
        List<PathResourceImplementation> res;
        synchronized (this) {
            res = cache;
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
        boolean needToFire;
        final Map<String,URL> modulesByName = getAllModules();
        res = new ArrayList<>(modulesByName.size());
        modulesByName.values().stream().map((url)->ClassPathSupport.createResource(url)).forEach(res::add);
        selfRes.set(new Object[]{res, Boolean.FALSE});
        try {
            boolean found = false;
            for (URL root : sources.getRootURLs()) {
                try {
                    final File moduleInfo = FileUtil.normalizeFile(new File(BaseUtilities.toFile(root.toURI()),MODULE_INFO));
                    newModuleInfos.add(moduleInfo);
                    if (!found) {
                        final FileObject modules = FileUtil.toFileObject(moduleInfo);
                        if (modules != null) {
                            found = true;
                            final JavaSource src = JavaSource.forFileObject(modules);
                            if (src != null) {
                                try {
                                    final List<List<PathResourceImplementation>> resInOut = new ArrayList<>(1);
                                    resInOut.add(res);
                                    src.runUserActionTask((CompilationController cc) -> {
                                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                        final Trees trees = cc.getTrees();
                                        final TreePathScanner<ModuleElement, Void> scanner =
                                                new TreePathScanner<ModuleElement, Void>() {
                                                    @Override
                                                    public ModuleElement visitModule(ModuleTree node, Void p) {
                                                        return (ModuleElement) trees.getElement(getCurrentPath());
                                                    }
                                            };
                                        final ModuleElement rootModule = scanner.scan(new TreePath(cc.getCompilationUnit()), null);
                                        Set<URL> requires = collectRequiredModules(rootModule, true, modulesByName);
                                        resInOut.set(0, filterModules(resInOut.get(0), requires));
                                    }, true);
                                    res = resInOut.get(0);
                                } catch (IOException ioe) {
                                    Exceptions.printStackTrace(ioe);
                                }
                            }
                        }
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
        } finally {
            needToFire = selfRes.get()[1] == Boolean.TRUE;
            selfRes.remove();
        }
        synchronized (this) {
            assert res != null;
            if (cache == null) {
                cache = res;
                final Collection<File> added = new ArrayList<>(newModuleInfos);
                added.removeAll(moduleInfos);
                final Collection<File> removed = new ArrayList<>(moduleInfos);
                removed.removeAll(newModuleInfos);
                for (File f : removed) {
                    FileUtil.removeFileChangeListener(this, f);
                }
                for (File f : added) {
                    FileUtil.addFileChangeListener(this, f);
                }
                moduleInfos = newModuleInfos;
            } else {
                res = cache;
            }
        }
        if (needToFire) {
            this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
        }
        return res;
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (propName == null || PLATFORM_ACTIVE.equals(propName) || SourceRoots.PROP_ROOTS.equals(propName)) {
            reset();
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        reset();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        reset();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        reset();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        reset();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private void reset() {
        synchronized (this) {
            this.cache = null;
        }
        this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
    }

    private Map<String,URL> getAllModules() {
        final Map<String,URL> res = new HashMap<>();
        final String platformName = eval.getProperty(PLATFORM_ACTIVE);
        if (platformName != null && !platformName.isEmpty()) {
            Arrays.stream(JavaPlatformManager.getDefault().getInstalledPlatforms())
                    .filter((plat)->platformName.equals(plat.getProperties().get(PLATFORM_ANT_NAME)))
                    .flatMap((plat)->plat.getBootstrapLibraries().entries().stream())
                    .map((entry) -> entry.getURL())
                    .filter((root) -> (PROTOCOL_NBJRT.equals(root.getProtocol())))
                    .forEach((root) -> {
                        res.put(getModuleName(root),root);
                    });
        }
        return res;
    }

    @NonNull
    private static Set<URL> collectRequiredModules(
            @NonNull final ModuleElement module,
            final boolean transitive,
            @NonNull final Map<String,URL> modulesByName) {
        final Set<URL> res = new HashSet<>();
        collectRequiredModulesImpl(module, transitive, modulesByName, res);
        return res;
    }

    private static void collectRequiredModulesImpl(
            @NullAllowed final ModuleElement module,
            final boolean transitive,
            @NonNull final Map<String,URL> modulesByName,
            @NonNull final Collection<? super URL> c) {
        if (module != null) {
            for (ModuleElement.Directive directive : module.getDirectives()) {
                if (directive.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                    ModuleElement.RequiresDirective req = (ModuleElement.RequiresDirective) directive;
                    final ModuleElement dependency = req.getDependency();
                    if (transitive) {
                        collectRequiredModulesImpl(dependency, transitive, modulesByName, c);
                    }
                    final URL dependencyURL = modulesByName.get(dependency.getQualifiedName().toString());
                    if (dependencyURL != null) {
                        c.add(dependencyURL);
                    }
                }
            }
        }
    }

    @NonNull
    private static String getModuleName(@NonNull final URL moduleURL) {
        final String path = moduleURL.getPath();
        int endIndex = path.length() - 1;
        int startIndex = path.lastIndexOf('/', endIndex - 1);   //NOI18N
        return path.substring(startIndex+1, endIndex);
    }

    @NonNull
    private static List<PathResourceImplementation> filterModules(
            @NonNull List<PathResourceImplementation> modules,
            @NonNull Set<URL> requires) {
        final List<PathResourceImplementation> res = new ArrayList<>(modules.size());
        for (PathResourceImplementation pr : modules) {
            for (URL url : pr.getRoots()) {
                if (requires.contains(url)) {
                    res.add(pr);
                }
            }
        }
        return res;
    }

}
