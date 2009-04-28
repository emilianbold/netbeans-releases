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
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.parsing.OutputFileObject;
import org.netbeans.modules.java.source.tasklist.RebuildOraculum;
import org.netbeans.modules.java.source.tasklist.TaskCache;
import org.netbeans.modules.java.source.usages.ClassNamesForFileOraculumImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
final class MultiPassCompileWorker extends CompileWorker {

    ParsingOutput compile(ParsingOutput previous, Context context, JavaParsingContext javaContext, Iterable<? extends Indexable> files) {
        final LinkedList<Indexable> toProcess = new LinkedList<Indexable>();
        for (Indexable i : files) {
            if (!previous.finishedFiles.contains(i)) {
                toProcess.add(i);
            }
        }
        if (toProcess.isEmpty()) {
            return previous;
        }
        
        final JavaFileManager fileManager = ClasspathInfoAccessor.getINSTANCE().getFileManager(javaContext.cpInfo);
        final Set<Indexable> finished = previous.finishedFiles;
        final Map<URL, Set<URL>> root2Rebuild = previous.root2Rebuild;
        final ClassNamesForFileOraculumImpl cnffOraculum = new ClassNamesForFileOraculumImpl(previous.file2FQNs);

        final LowMemoryListenerImpl mem = new LowMemoryListenerImpl();
        LowMemoryNotifier.getDefault().addLowMemoryListener(mem);

        try {
            final DiagnosticListenerImpl diagnosticListener = new DiagnosticListenerImpl();
            final LinkedList<CompileTuple> bigFiles = new LinkedList<CompileTuple>();
            JavacTaskImpl jt = null;
            CompileTuple active = null;
            int state = 0;
            boolean isBigFile = false;

            while (!toProcess.isEmpty() || !bigFiles.isEmpty() || active != null) {
// TODO:
//                    if (canceled != null && canceled.getAndSet(false)) {
//                        return toRefresh;
//                    }
//                    if (ideClosed != null && ideClosed.get()) {
//                        return toRefresh;
//                    }
                try {
                    if (mem.isLowMemory()) {
                        dumpSymFiles(fileManager, jt);
                        mem.isLowMemory();
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        if (state == 1) {
                            break;
                        } else {
                            state = 1;
                        }
                        System.gc();
                        continue;
                    }
                    if (active == null) {
                        if (!toProcess.isEmpty()) {
                            active = createTuple(context, javaContext, toProcess.removeFirst());
                            if (active == null)
                                continue;
                            isBigFile = false;
                        } else {
                            active = bigFiles.removeFirst();
                            isBigFile = true;
                        }
//                            if (CALLBACK != null) {
//                                CALLBACK.willCompile(activeTuple.jfo);
//                            }
                    }
                    if (jt == null) {
                        jt = JavacParser.createJavacTask(javaContext.cpInfo, diagnosticListener, javaContext.sourceLevel, cnffOraculum);
//                            jt.setTaskListener(listener);
                        if (JavaIndex.LOG.isLoggable(Level.FINER)) {
                            JavaIndex.LOG.finer("Created new JavacTask for: " + FileUtil.getFileDisplayName(context.getRoot()) + " " + javaContext.cpInfo.toString()); //NOI18N
                        }
                    }
                    Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[] {active.jfo});
                    if (mem.isLowMemory()) {
                        dumpSymFiles(fileManager, jt);
                        mem.isLowMemory();
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        trees = null;
                        if (state == 1) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state = 0;
                            }
                        } else {
                            state = 1;
                        }
                        System.gc();
                        continue;
                    }
                    Iterable<? extends TypeElement> types = jt.enterTrees(trees);
                    if (mem.isLowMemory()) {
                        dumpSymFiles(fileManager, jt);
                        mem.isLowMemory();
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        trees = null;
                        types = null;
                        if (state == 1) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state = 0;
                            }
                        } else {
                            state = 1;
                        }
                        System.gc();
                        continue;
                    }
                    jt.analyze(types);
                    if (mem.isLowMemory()) {
                        dumpSymFiles(fileManager, jt);
                        mem.isLowMemory();
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        trees = null;
                        types = null;
                        if (state == 1) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state = 0;
                            }
                        } else {
                            state = 1;
                        }
                        System.gc();
                        continue;
                    }
                    boolean[] main = new boolean[1];
                    javaContext.sa.analyse(trees, jt, fileManager, false, true, active.jfo, previous.addedTypes, main);
//                        if (activeTuple.file != null) {
                    //When the active file is not set (generated virtual source) ignore executable flag
                    ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().getURL(), active.jfo.toUri().toURL(), main[0]);
//                        }
                    TaskCache.getDefault().dumpErrors(context.getRootURI(), active.indexable.getURL(), diagnosticListener.getDiagnostics(active.jfo));
//                        Log.instance(jt.getContext()).nerrors = 0;
//                        if (compiledFiles != null && !activeTuple.virtual) {
//                            //compiledFiles are not tracked for virtual sources
//                            compiledFiles.add(activeTuple.file);
//                        }
                    if (!context.isSupplementaryFilesIndexing() /*&& !activeTuple.virtual*/) {
                        for (Map.Entry<URL, Collection<URL>> toRebuild : RebuildOraculum.findFilesToRebuild(context.getRootURI(), active.jfo.toUri().toURL(), javaContext.cpInfo, jt.getElements(), types).entrySet()) {
                            Set<URL> urls = root2Rebuild.get(toRebuild.getKey());
                            if (urls == null) {
                                root2Rebuild.put(toRebuild.getKey(), urls = new HashSet<URL>());
                            }
                            urls.addAll(toRebuild.getValue());
                        }
                    }
                    for (JavaFileObject generated : jt.generate(types)) {
                        if (generated instanceof OutputFileObject) {
                            previous.createdFiles.add(((OutputFileObject) generated).getFile());
                        } else {
                            // presumably should not happen
                        }
                    }
                    Log.instance(jt.getContext()).nerrors = 0;
                    finished.add(active.indexable);
                    active = null;
                    state  = 0;
                } catch (CouplingAbort ca) {
                    //Coupling error
                    TreeLoader.dumpCouplingAbort(ca, active.jfo);
                    jt = null;
                    diagnosticListener.cleanDiagnostics();
                    state = 0;
                } catch (OutputFileManager.InvalidSourcePath isp) {
                    //Deleted project - log & ignore
                    if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                        final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                        final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                        final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                        final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
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
                        final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
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
                    }
                    else {
                        if (JavaIndex.LOG.isLoggable(Level.WARNING)) {
                            final ClassPath bootPath   = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                            final ClassPath classPath  = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                            final ClassPath sourcePath = javaContext.cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                            final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                        active == null ? null : active.jfo.toUri().toString(),
                                        FileUtil.getFileDisplayName(context.getRoot()),
                                        bootPath == null ? null : bootPath.toString(),
                                        classPath == null ? null : classPath.toString(),
                                        sourcePath == null ? null : sourcePath.toString()
                                        );
                            JavaIndex.LOG.log(Level.WARNING, message, t);  //NOI18N
                        }
                        //When a javac failed with the Exception mark a file
                        //causing this exceptin as compiled
                        //otherwise tasklist will reschedule the parse again
                        //and the RepositoryUpdater ends in infinite loop of reparse.
                        if (active != null)
                            finished.add(active.indexable);
                        diagnosticListener.cleanDiagnostics();
                        jt = null;
                        active = null;
                    }
                }
            }
            if (state == 1) {
                JavaIndex.LOG.warning("Not enough memory to compile folder: " + FileUtil.getFileDisplayName(context.getRoot())); // NOI18N
            }
        } finally {
            LowMemoryNotifier.getDefault().removeLowMemoryListener(mem);
        }
        return new ParsingOutput(true, previous.file2FQNs, previous.addedTypes, previous.createdFiles, finished, previous.root2Rebuild);
    }

    private void dumpSymFiles(JavaFileManager jfm, JavacTaskImpl jti) throws IOException {
        if (jti != null) {
            for (Env<AttrContext> env : jti.getTodo()) {
                TreeLoader.dumpSymFile(jfm, jti, env.enclClass.sym);
            }
        }
    }
}
