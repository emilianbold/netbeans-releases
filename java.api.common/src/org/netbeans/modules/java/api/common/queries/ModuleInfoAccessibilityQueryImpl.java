/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.queries;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.ModuleElement;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * An implementation of the {@link AccessibilityQueryImplementation} based on the module-info.
 * Accessible through the {@link QuerySupport#createModuleInfoAccessibilityQuery}.
 * @author Tomas Zezula
 */
final class ModuleInfoAccessibilityQueryImpl implements AccessibilityQueryImplementation, PropertyChangeListener, FileChangeListener {
    private static final String MODULE_INFO_JAVA = "module-info.java";  //NOI18N

    private final SourceRoots sources;
    private final SourceRoots tests;
    private final Set</*@GuardedBy("this")*/File> moduleInfoListeners;
    //@GuardedBy("this")
    private ExportsCache exportsCache;
    //@GuardedBy("this")
    private boolean listensOnRoots;

    ModuleInfoAccessibilityQueryImpl(
            @NonNull final SourceRoots sources,
            @NonNull final SourceRoots tests) {
        Parameters.notNull("sources", sources);     //NOI18N
        Parameters.notNull("tests", tests);         //NOI18N
        this.sources = sources;
        this.tests = tests;
        this.moduleInfoListeners = new HashSet<>();
    }


    @CheckForNull
    @Override
    @SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
    public Boolean isPubliclyAccessible(FileObject pkg) {
        final ExportsCache cache = getCache();
        if (!cache.isInModule(pkg)) {
            return null;
        }
        return cache.isExported(pkg);
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOTS.equals(propName)) {
            reset();
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        reset();
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
    public void fileFolderCreated(FileEvent fe) {
        //Not important
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        //Not important
    }

    private void reset() {
        synchronized (this) {
            exportsCache = null;
        }
    }

    @NonNull
    private ExportsCache getCache() {
        ExportsCache ec;
        synchronized (this) {
            ec = exportsCache;
        }
        if (ec == null) {
            final List<Pair<Set<FileObject>,Set<FileObject>>> data = new ArrayList<>(2);
            readExports(sources).ifPresent(data::add);
            readExports(tests).ifPresent(data::add);
            ec = new ExportsCache(data);
            synchronized (this) {
                if (exportsCache == null) {
                    exportsCache = ec;
                } else {
                    ec = exportsCache;
                }
                if (!listensOnRoots) {
                    listensOnRoots = true;
                    sources.addPropertyChangeListener(WeakListeners.propertyChange(this, sources));
                    tests.addPropertyChangeListener(WeakListeners.propertyChange(this, tests));
                }
                final Set<File> allRoots = data.stream()
                        .flatMap((p) -> p.first().stream())
                        .map((fo) -> FileUtil.toFile(fo))
                        .filter((f) -> f != null)
                        .collect(Collectors.toSet());
                final Set<File> toRemove = new HashSet<>(moduleInfoListeners);
                toRemove.removeAll(allRoots);
                allRoots.removeAll(moduleInfoListeners);
                for (File f : toRemove) {
                    FileUtil.removeFileChangeListener(
                            this,
                            new File(f, MODULE_INFO_JAVA));
                    moduleInfoListeners.remove(f);
                }
                for (File f : allRoots) {
                    FileUtil.addFileChangeListener(
                            this,
                            new File(f, MODULE_INFO_JAVA));
                    moduleInfoListeners.add(f);
                }
            }
        }
        return ec;
    }

    @NonNull
    private static Optional<Pair<Set<FileObject>,Set<FileObject>>> readExports(@NonNull final SourceRoots srcRoots) {
        final FileObject[] roots = srcRoots.getRoots();
        final Optional<FileObject> moduleInfo = Arrays.stream(roots)
                .map((root) -> root.getFileObject(MODULE_INFO_JAVA))
                .filter((mi) -> mi != null)
                .findFirst();
        if (!moduleInfo.isPresent()) {
            return Optional.empty();
        }
        final Set<FileObject> rootsSet = new HashSet<>();
        Collections.addAll(rootsSet, roots);
        final Set<FileObject> exportsSet = readExports(moduleInfo.get(), rootsSet);
        return Optional.of(Pair.of(rootsSet, exportsSet));
    }

    @NonNull
    private static Set<FileObject> readExports(
        @NonNull final FileObject moduleInfo,
        @NonNull final Set<FileObject> roots) {
        final Set<FileObject> exports = new HashSet<>();
        final JavaSource src = JavaSource.forFileObject(moduleInfo);
        if (src != null) {
            try {
                src.runUserActionTask((cc) -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    final CompilationUnitTree cu = cc.getCompilationUnit();
                    if (cu.getTypeDecls().size() == 1 && cu.getTypeDecls().get(0) instanceof ModuleTree) {
                        final ModuleTree mt = (ModuleTree) cu.getTypeDecls().get(0);
                        final ModuleElement me = (ModuleElement) cc.getTrees().getElement(TreePath.getPath(cu, mt));
                        if (me != null) {
                            for (ModuleElement.Directive directive : me.getDirectives()) {
                                if (directive.getKind() == ModuleElement.DirectiveKind.EXPORTS) {
                                    final ModuleElement.ExportsDirective export = (ModuleElement.ExportsDirective) directive;
                                    final String pkgName = export.getPackage().getQualifiedName().toString();
                                    exports.addAll(findPackage(pkgName, roots));
                                }
                            }
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return exports;
    }

    @NonNull
    private static Set<FileObject> findPackage(
            @NonNull final String pkgName,
            @NonNull final Collection<? extends FileObject> roots) {
        final String path = pkgName.replace('.', '/');  //NOI18N
        final Set<FileObject> res = new HashSet<>();
        for (FileObject root : roots) {
            final FileObject pkg = root.getFileObject(path);
            if (pkg != null) {
                res.add(pkg);
            }
        }
        return res;
    }

    private static final class ExportsCache {
        private final List<Pair<Set<FileObject>,Set<FileObject>>> data;

        ExportsCache(@NonNull final List<Pair<Set<FileObject>,Set<FileObject>>> data) {
            this.data = data;
        }

        boolean isInModule(@NonNull final FileObject pkg) {
            return data.stream()
                    .flatMap((p) -> p.first().stream())
                    .anyMatch((root) -> {
                        return root.equals(pkg) || FileUtil.isParentOf(root, pkg);
                });
        }

        boolean isExported(@NonNull final FileObject pkg) {
            return data.stream()
                    .flatMap((p) -> p.second().stream())
                    .anyMatch((exported) -> exported.equals(pkg));
        }
    }

}
