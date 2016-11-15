/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class MultiModuleSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation2 {
    private static final Logger LOG = Logger.getLogger(MultiModuleSourceForBinaryQueryImpl.class.getName());
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final MultiModule modules;
    private final MultiModule testModules;
    private final String[] binaryProperties;
    private final String[] testBinaryProperties;
    private final Map<URI,R> cache;

    MultiModuleSourceForBinaryQueryImpl(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull MultiModule modules,
            @NonNull MultiModule testModules,
            @NonNull final String[] binaryProperties,
            @NonNull final String[] testBinaryProperties) {
        Parameters.notNull("helper", helper);       //NOI18N
        Parameters.notNull("eval", eval);           //NOI18N
        Parameters.notNull("modules", modules);     //NOI18N
        Parameters.notNull("testModules", testModules); //NOI18N
        Parameters.notNull("binaryProperties", binaryProperties);       //NOI18N
        Parameters.notNull("testBinaryProperties", testBinaryProperties);   //NOI18N
        this.helper = helper;
        this.eval = eval;
        this.modules = modules;
        this.testModules = testModules;
        this.binaryProperties = binaryProperties;
        this.testBinaryProperties = testBinaryProperties;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Result findSourceRoots2(@NonNull URL binaryRoot) {
        boolean archive = false;
        if (FileUtil.isArchiveArtifact(binaryRoot)) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            archive = true;
        }
        R res = null;
        try {
            URI artefact = binaryRoot.toURI();
            res = cache.get(artefact);
            if (res == null) {
                res = createResult(artefact, archive, modules, binaryProperties);
                if (res == null) {
                    res = createResult(artefact, archive, testModules, testBinaryProperties);
                }
                R prev = cache.get(artefact);
                if (prev != null) {
                    res = prev;
                } else if (res != null) {
                    prev = cache.putIfAbsent(artefact, res);
                    if (prev != null) {
                        res = prev;
                    }
                }
            }
        } catch (URISyntaxException e) {
            LOG.log(
                    Level.WARNING,
                    "Invalid URI: {0}", //NOI18N
                    binaryRoot.toExternalForm());
        }
        return res;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    @CheckForNull
    private R createResult(
            @NonNull final URI artefact,
            final boolean archive,
            @NonNull final MultiModule modules,
            @NonNull final String... properties) {
        if (isOwned(artefact, properties)) {
            final String moduleName = getModuleName(artefact, archive);
            if (moduleName != null) {
                final ClassPath cp = modules.getModuleSources(moduleName);
                if (cp != null) {
                    return new R(cp);
                }
            }
        }
        return null;
    }

    private boolean isOwned(
            @NonNull final URI artefact,
            @NonNull final String[] properties) {
        return Arrays.stream(properties)
                .map((prop) -> eval.getProperty(prop))
                .filter((prop) -> prop != null)
                .map((path) -> {
                    try {
                        final File f = helper.resolveFile(path);
                        URI uri = BaseUtilities.toURI(f);
                        final String suri = uri.toString();
                        if (!suri.endsWith("/")) {      //NOI18N
                            uri = new URI(suri+'/');    //NOI18N
                        }
                        return uri;
                    } catch (URISyntaxException e) {
                        return null;
                    }
                })
                .filter((folderURI) -> folderURI != null && artefact.toString().startsWith(folderURI.toString()))
                .findAny()
                .isPresent();
    }

    @CheckForNull
    private static String getModuleName(
            @NonNull final URI uri,
            final boolean archive) {
        final Path p = Paths.get(uri);
        if (p == null) {
            return null;
        }
        final String nameExt = p.getFileName().toString();
        final int dot = nameExt.lastIndexOf('.');   //NOI18N
        if (dot < 0 || !archive) {
            return nameExt;
        } else if (dot == 0) {
            return null;
        } else {
            return nameExt.substring(0, dot);
        }
    }

    private static final class R implements Result, PropertyChangeListener {
        private final ClassPath srcPath;
        private final ChangeSupport listeners;

        R(@NonNull final ClassPath srcPath) {
            Parameters.notNull("srcPath", srcPath); //NOI18N
            this.srcPath = srcPath;
            this.listeners = new ChangeSupport(this);
            this.srcPath.addPropertyChangeListener(WeakListeners.propertyChange(this, this.srcPath));
        }

        @Override
        public boolean preferSources() {
            return true;
        }

        @Override
        public FileObject[] getRoots() {
            return srcPath.getRoots();
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener l) {
            listeners.removeChangeListener(l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                listeners.fireChange();
            }
        }
    }

}
