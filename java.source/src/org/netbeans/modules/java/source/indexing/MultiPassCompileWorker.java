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
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.CancelAbort;
import com.sun.tools.javac.util.CancelService;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.AptSourceFileManager;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.usages.ClassNamesForFileOraculumImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.java.source.util.LMListener;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.ErrorsCache;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
final class MultiPassCompileWorker extends CompileWorker {

    private static final int MEMORY_LOW = 1;
    private static final int ERR = 2;

    ParsingOutput compile(final ParsingOutput previous, final Context context, JavaParsingContext javaContext, Iterable<? extends CompileTuple> files) {
        final LinkedList<CompileTuple> toProcess = new LinkedList<CompileTuple>();
        final HashMap<JavaFileObject, CompileTuple> jfo2tuples = new HashMap<JavaFileObject, CompileTuple>();
        for (CompileTuple i : files) {
            if (!previous.finishedFiles.contains(i.indexable)) {
                toProcess.add(i);
                jfo2tuples.put(i.jfo, i);
            }
        }
        if (toProcess.isEmpty()) {
            return previous;
        }
        
        final JavaFileManager fileManager = ClasspathInfoAccessor.getINSTANCE().getFileManager(javaContext.cpInfo);
        final ClassNamesForFileOraculumImpl cnffOraculum = new ClassNamesForFileOraculumImpl(previous.file2FQNs);

        final LMListener mem = new LMListener();

        final DiagnosticListenerImpl diagnosticListener = new DiagnosticListenerImpl();
        final LinkedList<CompileTuple> bigFiles = new LinkedList<CompileTuple>();
        JavacTaskImpl jt = null;
        CompileTuple active = null;
        int state = 0;
        boolean isBigFile = false;

        while (!toProcess.isEmpty() || !bigFiles.isEmpty() || active != null) {
            if (context.isCancelled()) {
                return null;
            }
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
                        if (active == null || previous.finishedFiles.contains(active.indexable))
                            continue;
                        isBigFile = false;
                    } else {
                        active = bigFiles.removeFirst();
                        isBigFile = true;
                    }
                }
                if (jt == null) {
                    jt = JavacParser.createJavacTask(javaContext.cpInfo, diagnosticListener, javaContext.sourceLevel, cnffOraculum, new CancelService() {
                        public @Override boolean isCanceled() {
                            return context.isCancelled();
                        }
                    }, APTUtils.get(context.getRoot()));
                    if (JavaIndex.LOG.isLoggable(Level.FINER)) {
                        JavaIndex.LOG.finer("Created new JavacTask for: " + FileUtil.getFileDisplayName(context.getRoot()) + " " + javaContext.cpInfo.toString()); //NOI18N
                    }
                }
                Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[]{active.jfo});
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
                Iterable<? extends TypeElement> types;
                fileManager.handleOption(AptSourceFileManager.ORIGIN_FILE, Collections.singletonList(active.indexable.getURL().toString()).iterator()); //NOI18N
                try {
                    types = jt.enterTrees(trees);
                    if (jfo2tuples.remove(active.jfo) != null) {
                        final Types ts = Types.instance(jt.getContext());
                        final Indexable activeIndexable = active.indexable;
                        class ScanNested extends TreeScanner {
                            Set<CompileTuple> dependencies = new LinkedHashSet<CompileTuple>();
                            @Override
                            public void visitClassDef(JCClassDecl node) {
                                if (node.sym != null) {
                                    Type st = ts.supertype(node.sym.type);
                                    if (st.tag == TypeTags.CLASS) {
                                        ClassSymbol c = st.tsym.outermostClass();
                                        CompileTuple u = jfo2tuples.get(c.sourcefile);
                                        if (u != null && !previous.finishedFiles.contains(u.indexable) && !u.indexable.equals(activeIndexable)) {
                                            dependencies.add(u);
                                        }
                                    }
                                }
                                super.visitClassDef(node);
                            }
                        }
                        ScanNested scanner = new ScanNested();
                        for (CompilationUnitTree cut : trees) {
                            scanner.scan((JCCompilationUnit)cut);
                        }
                        if (!scanner.dependencies.isEmpty()) {
                            toProcess.addFirst(active);
                            for (CompileTuple tuple : scanner.dependencies) {
                                toProcess.addFirst(tuple);
                            }
                            active = null;
                            continue;
                        }
                    }
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
                } finally {
                    fileManager.handleOption(AptSourceFileManager.ORIGIN_FILE, Collections.singletonList("").iterator()); //NOI18N
                }
                JavaCustomIndexer.addAptGenerated(context, javaContext, active.indexable.getRelativePath(), previous.aptGenerated);
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
                if (javaContext.checkSums.checkAndSet(active.indexable.getURL(), types, jt.getElements()) || context.isSupplementaryFilesIndexing()) {
                    javaContext.sa.analyse(trees, jt, fileManager, active, previous.addedTypes, main);
                } else {
                    final Set<ElementHandle<TypeElement>> aTypes = new HashSet<ElementHandle<TypeElement>>();
                    javaContext.sa.analyse(trees, jt, fileManager, active, aTypes, main);
                    previous.addedTypes.addAll(aTypes);
                    previous.modifiedTypes.addAll(aTypes);
                }
                ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().getURL(), active.indexable.getURL(), main[0]);
                for (JavaFileObject generated : jt.generate(types)) {
                    if (generated instanceof FileObjects.FileBase) {
                        previous.createdFiles.add(((FileObjects.FileBase) generated).getFile());
                    } else {
                        // presumably should not happen
                    }
                }
                if (!active.virtual) {
                    ErrorsCache.setErrors(context.getRootURI(), active.indexable, diagnosticListener.getDiagnostics(active.jfo), JavaCustomIndexer.ERROR_CONVERTOR);
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
                return new ParsingOutput(false, previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes, previous.aptGenerated);
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
                return new ParsingOutput(false, previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes, previous.aptGenerated);
            } catch (CancelAbort ca) {
                if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                    JavaIndex.LOG.log(Level.FINEST, "OnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
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
        return new ParsingOutput(true, previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes, previous.aptGenerated);
    }

    private void dumpSymFiles(JavaFileManager jfm, JavacTaskImpl jti) throws IOException {
        if (jti != null) {
            for (Env<AttrContext> env : jti.getTodo()) {
                TreeLoader.dumpSymFile(jfm, jti, env.enclClass.sym);
            }
        }
    }
}
