/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.indexing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaIndexerPlugin;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.SourceAnalyser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

class JavaParsingContext {

    final ClasspathInfo cpInfo;
    final String sourceLevel;
    final JavaFileFilterImplementation filter;
    final Charset encoding;
    final ClassIndexImpl uq;
    final CheckSums checkSums;
    final FQN2Files fqn2Files;
    final SourceAnalyser sa;
    private final Iterable<? extends JavaIndexerPlugin> pluginsCache;

    public JavaParsingContext(final Context context) throws IOException, NoSuchAlgorithmException {
        this(context, false);
    }
    
    JavaParsingContext(final Context context, final boolean allowNonExistentRoot) throws IOException, NoSuchAlgorithmException {
        final FileObject root = context.getRoot();
        final boolean rootNotNeeded = allowNonExistentRoot && root == null;
        cpInfo = rootNotNeeded ? null : ClasspathInfo.create(root);
        sourceLevel = rootNotNeeded ? null : SourceLevelQuery.getSourceLevel(root);
        filter = rootNotNeeded ? null : JavaFileFilterQuery.getFilter(root);
        encoding = rootNotNeeded ? null : FileEncodingQuery.getEncoding(root);
        uq = ClassIndexManager.getDefault().createUsagesQuery(context.getRootURI(), true);
        sa = uq != null ? uq.getSourceAnalyser() : null;
        checkSums = CheckSums.forContext(context);
        fqn2Files = FQN2Files.forRoot(context.getRootURI());
        pluginsCache = createPlugins(root);
    }

    public JavaParsingContext(final Context context, final ClassPath bootPath, final ClassPath compilePath, final ClassPath sourcePath,
            final Collection<? extends CompileTuple> virtualSources) throws IOException, NoSuchAlgorithmException {
        final FileObject root = context.getRoot();
        filter = JavaFileFilterQuery.getFilter(root);
        cpInfo = ClasspathInfoAccessor.getINSTANCE().create(bootPath,compilePath, sourcePath,
                filter, true, context.isSourceForBinaryRootIndexing(),
                !virtualSources.isEmpty(), context.checkForEditorModifications());
        registerVirtualSources(cpInfo, virtualSources);
        sourceLevel = SourceLevelQuery.getSourceLevel(root);
        encoding = FileEncodingQuery.getEncoding(root);
        uq = ClassIndexManager.getDefault().createUsagesQuery(context.getRootURI(), true);
        sa = uq != null ? uq.getSourceAnalyser() : null;
        checkSums = CheckSums.forContext(context);
        fqn2Files = FQN2Files.forRoot(context.getRootURI());
        pluginsCache = createPlugins(root);
    }

    void analyze(
            @NonNull final Iterable<? extends CompilationUnitTree> trees,
            @NonNull final JavacTaskImpl jt,
            @NonNull final JavaFileManager fileManager,
            @NonNull final CompileTuple active,
            @NonNull final Set<? super ElementHandle<TypeElement>> newTypes,
            @NonNull /*@Out*/ final boolean[] mainMethod) throws IOException {
        sa.analyse(trees, jt, fileManager, active, newTypes, mainMethod);
        final Lookup pluginServices = getPluginServices(jt);
        for (CompilationUnitTree cu : trees) {
            for (JavaIndexerPlugin plugin : getPlugins()) {
                plugin.process(cu, pluginServices);
            }
        }
    }

    @NonNull
    private Iterable<? extends JavaIndexerPlugin> getPlugins() {
        return pluginsCache;
    }

    private Iterable<? extends JavaIndexerPlugin> createPlugins(final FileObject root) {
        final List<JavaIndexerPlugin> plugins = new ArrayList<JavaIndexerPlugin>();
        for (JavaIndexerPlugin.Factory factory : MimeLookup.getLookup(
            MimePath.parse(JavaDataLoader.JAVA_MIME_TYPE)).lookupAll(JavaIndexerPlugin.Factory.class)) {
            final JavaIndexerPlugin plugin = factory.create(root);
            if (plugin != null) {
                plugins.add(plugin);
            }
        }
        return plugins;
    }

    @NonNull
    private Lookup getPluginServices(final JavacTaskImpl jt) {
        return Lookups.fixed(
            jt.getElements(),
            jt.getTypes(),
            JavacTrees.instance(jt.getContext()),
            JavaSourceAccessor.getINSTANCE().createElementUtilities(jt));
    }

    private static void registerVirtualSources(final ClasspathInfo cpInfo, final Collection<? extends CompileTuple> virtualSources) {
        for (CompileTuple compileTuple : virtualSources) {
            ClasspathInfoAccessor.getINSTANCE().registerVirtualSource(cpInfo, compileTuple.jfo);
        }
    }
}
