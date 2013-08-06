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
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeScanner;
import org.netbeans.lib.nbjavac.services.CancelAbort;
import org.netbeans.lib.nbjavac.services.CancelService;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.annotation.processing.Processor;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.usages.ClassNamesForFileOraculumImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
final class MultiPassCompileWorker extends CompileWorker {

    private static final int MEMORY_LOW = 1;
    private static final int ERR = 2;
    private boolean checkForMemLow = true;

    @Override
    ParsingOutput compile(
            final ParsingOutput previous,
            final Context context,
            final JavaParsingContext javaContext,
            final Collection<? extends CompileTuple> files) {
        final LinkedList<CompileTuple> toProcess = new LinkedList<CompileTuple>();
        final HashMap<JavaFileObject, CompileTuple> jfo2tuples = new HashMap<JavaFileObject, CompileTuple>();
        for (CompileTuple i : files) {
            if (!previous.finishedFiles.contains(i.indexable)) {
                toProcess.add(i);
                jfo2tuples.put(i.jfo, i);
            }
        }
        if (toProcess.isEmpty()) {
            return ParsingOutput.success(
                    previous.file2FQNs,
                    previous.addedTypes,
                    previous.createdFiles,
                    previous.finishedFiles,
                    previous.modifiedTypes,
                    previous.aptGenerated);
        }
        
        final ClassNamesForFileOraculumImpl cnffOraculum = new ClassNamesForFileOraculumImpl(previous.file2FQNs);

        final LowMemoryWatcher mem = LowMemoryWatcher.getInstance();

        final DiagnosticListenerImpl diagnosticListener = new DiagnosticListenerImpl();
        final LinkedList<CompileTuple> bigFiles = new LinkedList<CompileTuple>();
        JavacTaskImpl jt = null;
        CompileTuple active = null;
        boolean aptEnabled = false;
        int state = 0;
        boolean isBigFile = false;

        while (!toProcess.isEmpty() || !bigFiles.isEmpty() || active != null) {
            if (context.isCancelled()) {
                return null;
            }
            try {
                context.getSuspendStatus().parkWhileSuspended();
            } catch (InterruptedException ex) {
                //NOP - safe to ignore
            }
            try {
                try {
                    if (mem.isLowMemory()) {
                        dumpSymFiles(jt, previous.createdFiles, context);
                        mem.isLowMemory();
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        if ((state & MEMORY_LOW) != 0) {
                            break;
                        } else {
                            state |= MEMORY_LOW;
                        }
                        mem.free();
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
                        jt = JavacParser.createJavacTask(
                                javaContext.getClasspathInfo(),
                                diagnosticListener,
                                javaContext.getSourceLevel(),
                                javaContext.getProfile(),
                                cnffOraculum,
                                javaContext.getFQNs(),
                                new CancelService() {
                                    public @Override boolean isCanceled() {
                                        return context.isCancelled() || (checkForMemLow && mem.isLowMemory());
                                    }
                                },
                                active.aptGenerated ? null : APTUtils.get(context.getRoot()));
                        Iterable<? extends Processor> processors = jt.getProcessors();
                        aptEnabled = processors != null && processors.iterator().hasNext();
                        if (JavaIndex.LOG.isLoggable(Level.FINER)) {
                            JavaIndex.LOG.finer("Created new JavacTask for: " + FileUtil.getFileDisplayName(context.getRoot()) + " " + javaContext.getClasspathInfo().toString()); //NOI18N
                        }
                    }
                    Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[]{active.jfo});
                    if (mem.isLowMemory()) {
                        dumpSymFiles(jt, previous.createdFiles, context);
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
                        mem.free();
                        continue;
                    }
                    Iterable<? extends TypeElement> types;
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
                                    boolean envForSuperTypeFound = false;
                                    while (!envForSuperTypeFound && st != null && st.hasTag(TypeTag.CLASS)) {
                                        ClassSymbol c = st.tsym.outermostClass();
                                        CompileTuple u = jfo2tuples.get(c.sourcefile);
                                        if (u != null && !previous.finishedFiles.contains(u.indexable) && !u.indexable.equals(activeIndexable)) {
                                            dependencies.add(u);
                                            envForSuperTypeFound = true;
                                        }
                                        st = ts.supertype(st);
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
                        dumpSymFiles(jt, previous.createdFiles, context);
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
                        mem.free();
                        continue;
                    }
                    jt.analyze(types);
                    if (aptEnabled) {
                        JavaCustomIndexer.addAptGenerated(context, javaContext, active, previous.aptGenerated);
                    }
                    if (mem.isLowMemory()) {
                        dumpSymFiles(jt, previous.createdFiles, context);
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
                        mem.free();
                        continue;
                    }
                    javaContext.getFQNs().set(types, active.indexable.getURL());
                    boolean[] main = new boolean[1];
                    if (javaContext.getCheckSums().checkAndSet(active.indexable.getURL(), types, jt.getElements()) || context.isSupplementaryFilesIndexing()) {
                        javaContext.analyze(trees, jt, active, previous.addedTypes, main);
                    } else {
                        final Set<ElementHandle<TypeElement>> aTypes = new HashSet<ElementHandle<TypeElement>>();
                        javaContext.analyze(trees, jt, active, aTypes, main);
                        previous.addedTypes.addAll(aTypes);
                        previous.modifiedTypes.addAll(aTypes);
                    }
                    ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().getURL(), active.indexable.getURL(), main[0]);
                    Iterable<? extends JavaFileObject> generatedFiles = jt.generate(types);
                    if (!active.virtual) {
                        for (JavaFileObject generated : generatedFiles) {
                            if (generated instanceof FileObjects.FileBase) {
                                previous.createdFiles.add(((FileObjects.FileBase) generated).getFile());
                            } else {
                                // presumably should not happen
                            }
                        }
                    }
                    JavaCustomIndexer.setErrors(context, active, diagnosticListener);
                    Log.instance(jt.getContext()).nerrors = 0;
                    previous.finishedFiles.add(active.indexable);
                    active = null;
                    state  = 0;
                } catch (CancelAbort ca) {
                    if (mem.isLowMemory()) {
                        dumpSymFiles(jt, previous.createdFiles, context);
                        mem.isLowMemory();
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
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
                        mem.free();
                    } else if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                        JavaIndex.LOG.log(Level.FINEST, "OnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
                    }
                }
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
                    final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(Level.FINEST, message, isp);
                }
                return ParsingOutput.failure(previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes, previous.aptGenerated);
            } catch (MissingPlatformError mpe) {
                //No platform - log & mark files as errornous
                if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                    final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(Level.FINEST, message, mpe);
                }
                JavaCustomIndexer.brokenPlatform(context, files, mpe.getDiagnostic());
                return ParsingOutput.failure(previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes, previous.aptGenerated);
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                }
                else {
                    Level level = t instanceof FatalError ? Level.FINEST : Level.WARNING;
                    if (JavaIndex.LOG.isLoggable(level)) {
                        final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                        final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                        final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                        final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                    active == null ? null : active.jfo.toUri().toString(),
                                    FileUtil.getFileDisplayName(context.getRoot()),
                                    bootPath == null ? null : bootPath.toString(),
                                    classPath == null ? null : classPath.toString(),
                                    sourcePath == null ? null : sourcePath.toString()
                                    );
                        JavaIndex.LOG.log(level, message, t);  //NOI18N
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
        return (state & MEMORY_LOW) == 0?
            ParsingOutput.success(previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes, previous.aptGenerated):
            ParsingOutput.lowMemory(previous.file2FQNs, previous.addedTypes, previous.createdFiles, previous.finishedFiles, previous.modifiedTypes, previous.aptGenerated);
    }

    private void dumpSymFiles(
            final JavacTaskImpl jti,
            final Set<File> alreadyCreated,
            final Context ctx) throws IOException {
        if (jti != null) {
            final JavaFileManager jfm = jti.getContext().get(JavaFileManager.class);
            checkForMemLow = false;
            try {
                final Types types = Types.instance(jti.getContext());
                final Enter enter = Enter.instance(jti.getContext());
                final Symtab syms = Symtab.instance(jti.getContext());
                class ScanNested extends TreeScanner {
                    private Env<AttrContext> env;
                    private Set<Env<AttrContext>> checked = new HashSet<Env<AttrContext>>();
                    private List<Env<AttrContext>> dependencies = new LinkedList<Env<AttrContext>>();
                    public ScanNested(Env<AttrContext> env) {
                        this.env = env;
                    }
                    @Override
                    public void visitClassDef(JCClassDecl node) {
                        if (node.sym != null) {
                            Type st = types.supertype(node.sym.type);
                            boolean envForSuperTypeFound = false;
                            while (!envForSuperTypeFound && st != null && st.hasTag(TypeTag.CLASS)) {
                                ClassSymbol c = st.tsym.outermostClass();
                                Env<AttrContext> stEnv = enter.getEnv(c);
                                if (stEnv != null && env != stEnv) {
                                    if (checked.add(stEnv)) {
                                        scan(stEnv.tree);
                                        if (TreeLoader.pruneTree(stEnv.tree, syms))
                                            dependencies.add(stEnv);
                                    }
                                    envForSuperTypeFound = true;
                                }
                                st = types.supertype(st);
                            }
                        }
                        super.visitClassDef(node);
                    }
                }
                final Set<Env<AttrContext>> processedEnvs = new HashSet<Env<AttrContext>>();
                File classes = JavaIndex.getClassFolder(ctx);
                for (Env<AttrContext> env : jti.getTodo()) {
                    if (processedEnvs.add(env)) {
                        ScanNested scanner = new ScanNested(env);
                        scanner.scan(env.tree);
                        for (Env<AttrContext> dep: scanner.dependencies) {
                            if (processedEnvs.add(dep)) {
                                dumpSymFile(jfm, jti, dep.enclClass.sym, alreadyCreated, classes);
                            }
                        }
                        if (TreeLoader.pruneTree(env.tree, syms))
                            dumpSymFile(jfm, jti, env.enclClass.sym, alreadyCreated, classes);
                    }
                }
            } finally {
                checkForMemLow = true;
            }
        }
    }
    
    private void dumpSymFile(
            @NonNull final JavaFileManager jfm,
            @NonNull final JavacTaskImpl jti,
            @NullAllowed final ClassSymbol cs,
            @NonNull final Set<File> alreadyCreated,
            @NonNull final File classes) throws IOException {
        if (cs == null) {
            //ClassDecl has no symbol because compilation was cancelled
            //by low memory before ENTER done.
            return;
        }        
        JavaFileObject file = jfm.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT,
                cs.flatname.toString(), JavaFileObject.Kind.CLASS, cs.sourcefile);
        if (file instanceof FileObjects.FileBase && !alreadyCreated.contains(((FileObjects.FileBase)file).getFile())) {
            TreeLoader.dumpSymFile(jfm, jti, cs, classes);
        }
    }
}
