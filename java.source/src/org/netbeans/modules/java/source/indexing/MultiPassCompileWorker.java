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
import java.util.HashSet;
import java.util.LinkedList;
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
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
final class MultiPassCompileWorker extends CompileWorker {

    private static final int MEMORY_LOW = 1;
    private static final int ERR = 2;

    ParsingOutput compile(ParsingOutput previous, Context context, JavaParsingContext javaContext, Iterable<? extends CompileTuple> files) {
        final LinkedList<CompileTuple> toProcess = new LinkedList<CompileTuple>();
        for (CompileTuple i : files) {
            if (!previous.finishedFiles.contains(i.indexable)) {
                toProcess.add(i);
            }
        }
        if (toProcess.isEmpty()) {
            return previous;
        }
        
        final JavaFileManager fileManager = ClasspathInfoAccessor.getINSTANCE().getFileManager(javaContext.cpInfo);
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
                try {
                    if (mem.isLowMemory()) {
                        dumpSymFiles(fileManager, jt);
                        mem.isLowMemory();
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        if ((state & MEMORY_LOW) != 0) {
                            break;
                        } else {
                            state |= MEMORY_LOW;
                        }
                        System.gc();
                        continue;
                    }
                    if (active == null) {
                        if (!toProcess.isEmpty()) {
                            active = toProcess.removeFirst();
                            if (active == null)
                                continue;
                            isBigFile = false;
                        } else {
                            active = bigFiles.removeFirst();
                            isBigFile = true;
                        }
                    }
                    if (jt == null) {
                        jt = JavacParser.createJavacTask(javaContext.cpInfo, diagnosticListener, javaContext.sourceLevel, cnffOraculum);
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
                        if ((state & MEMORY_LOW) != 0) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state &= ~MEMORY_LOW;
                            }
                        } else {
                            state |= MEMORY_LOW;
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
                        if ((state & MEMORY_LOW) != 0) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state &= ~MEMORY_LOW;
                            }
                        } else {
                            state |= MEMORY_LOW;
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
                        if ((state & MEMORY_LOW) != 0) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state &= ~MEMORY_LOW;
                            }
                        } else {
                            state |= MEMORY_LOW;
                        }
                        System.gc();
                        continue;
                    }
                    boolean[] main = new boolean[1];
                    if (javaContext.checkSums.checkAndSet(active.indexable.getURL(), types, jt.getElements()) || context.isSupplementaryFilesIndexing() || context.isAllFilesIndexing() || TasklistSettings.getDependencyTracking() == TasklistSettings.DependencyTracking.DISABLED) {
                        javaContext.sa.analyse(trees, jt, fileManager, active, previous.addedTypes, main);
                    } else {
                        final Set<ElementHandle<TypeElement>> aTypes = new HashSet<ElementHandle<TypeElement>>();
                        javaContext.sa.analyse(trees, jt, fileManager, active, aTypes, main);
                        previous.addedTypes.addAll(aTypes);
                        previous.modifiedTypes.addAll(aTypes);
                    }
                    ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().getURL(), active.indexable.getURL(), main[0]);
                    for (JavaFileObject generated : jt.generate(types)) {
                        if (generated instanceof OutputFileObject) {
                            previous.createdFiles.add(((OutputFileObject) generated).getFile());
                        } else {
                            // presumably should not happen
                        }
                    }
                    if (!active.virtual) {
                        TaskCache.getDefault().dumpErrors(context.getRootURI(), active.indexable.getURL(), diagnosticListener.getDiagnostics(active.jfo));
                    }
                    Log.instance(jt.getContext()).nerrors = 0;
                    previous.finishedFiles.add(active.indexable);
                    active = null;
                    state  = 0;
                } catch (CouplingAbort ca) {
                    //Coupling error
                    TreeLoader.dumpCouplingAbort(ca, null);
                    jt = null;
                    diagnosticListener.cleanDiagnostics();
                    if ((state & ERR) != 0) {
                        //When a javac failed with the Exception mark a file
                        //causing this exceptin as compiled
                        if (active != null)
                            previous.finishedFiles.add(active.indexable);
                        active = null;
                        state = 0;
                    } else {
                        state |= ERR;
                    }
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
                    return new ParsingOutput(false, previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes);
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
                    return new ParsingOutput(false, previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes);
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
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        if ((state & ERR) != 0) {
                            //When a javac failed with the Exception mark a file
                            //causing this exceptin as compiled
                            if (active != null)
                                previous.finishedFiles.add(active.indexable);
                            active = null;
                            state = 0;
                        } else {
                            state |= ERR;
                        }
                    }
                }
            }
            if ((state & MEMORY_LOW) != 0) {
                JavaIndex.LOG.warning("Not enough memory to compile folder: " + FileUtil.getFileDisplayName(context.getRoot())); // NOI18N
            }
        } finally {
            LowMemoryNotifier.getDefault().removeLowMemoryListener(mem);
        }
        return new ParsingOutput(true, previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes);
    }

    private void dumpSymFiles(JavaFileManager jfm, JavacTaskImpl jti) throws IOException {
        if (jti != null) {
            for (Env<AttrContext> env : jti.getTodo()) {
                TreeLoader.dumpSymFile(jfm, jti, env.enclClass.sym);
            }
        }
    }
}
