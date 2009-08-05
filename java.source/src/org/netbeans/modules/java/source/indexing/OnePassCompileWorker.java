/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.parsing.OutputFileObject;
import org.netbeans.modules.java.source.tasklist.TaskCache;
import org.netbeans.modules.java.source.tasklist.TasklistSettings;
import org.netbeans.modules.java.source.usages.ClassNamesForFileOraculumImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
final class OnePassCompileWorker extends CompileWorker {

    ParsingOutput compile(ParsingOutput previous, Context context, JavaParsingContext javaContext, Iterable<? extends CompileTuple> files) {
        final JavaFileManager fileManager = ClasspathInfoAccessor.getINSTANCE().getFileManager(javaContext.cpInfo);
        final Map<URI, List<String>> file2FQNs = new HashMap<URI, List<String>>();
        final Set<ElementHandle<TypeElement>> addedTypes = new HashSet<ElementHandle<TypeElement>>();
        final Set<File> createdFiles = new HashSet<File>();
        final Set<Indexable> finished = new HashSet<Indexable>();
        final Set<ElementHandle<TypeElement>> modifiedTypes = new HashSet<ElementHandle<TypeElement>>();
        final ClassNamesForFileOraculumImpl cnffOraculum = new ClassNamesForFileOraculumImpl(file2FQNs);

        final LowMemoryListenerImpl mem = new LowMemoryListenerImpl();
        LowMemoryNotifier.getDefault().addLowMemoryListener(mem);

        try {
            final DiagnosticListenerImpl dc = new DiagnosticListenerImpl();
            LinkedList<Pair<CompilationUnitTree, CompileTuple>> units = new LinkedList<Pair<CompilationUnitTree, CompileTuple>>();
            JavacTaskImpl jt = null;

            for (CompileTuple tuple : files) {
                try {
                    if (mem.isLowMemory()) {
                        jt = null;
                        units = null;
                        dc.cleanDiagnostics();
                        System.gc();
                    }
                    if (jt == null) {
                        jt = JavacParser.createJavacTask(javaContext.cpInfo, dc, javaContext.sourceLevel, cnffOraculum);
                    }
                    for (CompilationUnitTree cut : jt.parse(tuple.jfo)) { //TODO: should be exactly one
                        if (units != null)
                            units.add(Pair.<CompilationUnitTree, CompileTuple>of(cut, tuple));
                        computeFQNs(file2FQNs, cut, tuple.jfo);
                    }
                    Log.instance(jt.getContext()).nerrors = 0;
                } catch (Throwable t) {
                    if (JavaIndex.LOG.isLoggable(Level.WARNING)) {
                        final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                        final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                        final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                        final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                    tuple.indexable.getURL().toString(),
                                    FileUtil.getFileDisplayName(context.getRoot()),
                                    bootPath == null   ? null : bootPath.toString(),
                                    classPath == null  ? null : classPath.toString(),
                                    sourcePath == null ? null : sourcePath.toString()
                                    );
                        JavaIndex.LOG.log(Level.WARNING, message, t);  //NOI18N
                    }
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    }
                    else {
                        jt = null;
                        units = null;
                        dc.cleanDiagnostics();
                        System.gc();
                    }
                }
            }

            if (units == null) {
                return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes);
            }

            CompileTuple active = null;
            try {
                for (Pair<CompilationUnitTree, CompileTuple> unit : units) {
                    active = unit.second;
                    if (mem.isLowMemory()) {
                        units = null;
                        System.gc();
                        return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes);
                    }
                    Iterable<? extends TypeElement> types = jt.enterTrees(Collections.singletonList(unit.first));
                    if (mem.isLowMemory()) {
                        units = null;
                        System.gc();
                        return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes);
                    }
                    jt.analyze(types);
                    if (mem.isLowMemory()) {
                        units = null;
                        System.gc();
                        return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes);
                    }
                    boolean[] main = new boolean[1];
                    if (javaContext.checkSums.checkAndSet(active.indexable.getURL(), types, jt.getElements()) || context.isSupplementaryFilesIndexing() || context.isAllFilesIndexing() || TasklistSettings.getDependencyTracking() == TasklistSettings.DependencyTracking.DISABLED) {
                        javaContext.sa.analyse(Collections.singleton(unit.first), jt, fileManager, unit.second, addedTypes, main);
                    } else {
                        final Set<ElementHandle<TypeElement>> aTypes = new HashSet<ElementHandle<TypeElement>>();
                        javaContext.sa.analyse(Collections.singleton(unit.first), jt, fileManager, unit.second, aTypes, main);
                        addedTypes.addAll(aTypes);
                        modifiedTypes.addAll(aTypes);
                    }
                    ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().getURL(), active.indexable.getURL(), main[0]);
                    for (JavaFileObject generated : jt.generate(types)) {
                        if (generated instanceof OutputFileObject) {
                            createdFiles.add(((OutputFileObject) generated).getFile());
                        } else {
                            // presumably should not happen
                        }
                    }
                    if (!active.virtual) {
                        TaskCache.getDefault().dumpErrors(context.getRootURI(), active.indexable.getURL(), dc.getDiagnostics(active.jfo));
                    }
                    Log.instance(jt.getContext()).nerrors = 0;
                    finished.add(active.indexable);
                }
                return new ParsingOutput(true, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes);
            } catch (CouplingAbort ca) {
                //Coupling error
                TreeLoader.dumpCouplingAbort(ca, null);
            } catch (OutputFileManager.InvalidSourcePath isp) {
                //Deleted project - log & ignore
                if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                    final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(Level.FINEST, message, isp);
                }
            } catch (MissingPlatformError mpe) {
                //No platform - log & ignore
                if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                    final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(Level.FINEST, message, mpe);
                }
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                } else if (JavaIndex.LOG.isLoggable(Level.WARNING)) {
                    final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(Level.WARNING, message, t);  //NOI18N
                }
            }
            return new ParsingOutput(false, file2FQNs, addedTypes, createdFiles, finished, modifiedTypes);
        } finally {
            LowMemoryNotifier.getDefault().removeLowMemoryListener(mem);
        }
    }
}
