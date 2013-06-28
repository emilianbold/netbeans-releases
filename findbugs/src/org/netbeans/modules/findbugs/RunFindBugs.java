/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.findbugs;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import edu.umd.cs.findbugs.BugCategory;
import edu.umd.cs.findbugs.BugCollectionBugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.FindBugsProgress;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.PackageMemberAnnotation;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.config.UserPreferences;
import edu.umd.cs.findbugs.log.Profiler;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result2;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.analysis.api.CodeAnalysis;
import org.netbeans.modules.analysis.spi.Analyzer.WarningDescription;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.EnhancedFix;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 *
 * @author lahvac
 */
public class RunFindBugs {

    public static final String PREFIX_FINDBUGS = "findbugs:";
           static final Logger LOG = Logger.getLogger(RunFindBugs.class.getName());
    
    public static List<ErrorDescription> runFindBugs(CompilationInfo info, Preferences customSettings, String singleBug, FileObject sourceRoot, Iterable<? extends String> classNames, final FindBugsProgress progress, final Cancel cancel, SigFilesValidator validator) {
        List<ErrorDescription> result = new ArrayList<ErrorDescription>();
        
        try {
            Class.forName("org.netbeans.modules.findbugs.NbClassFactory", true, RunFindBugs.class.getClassLoader()); //NOI18N
            Project p = new Project();
            URL[] binaryRoots = CacheBinaryForSourceQuery.findCacheBinaryRoots(sourceRoot.toURL()).getRoots();

            if (classNames == null) {
                for (URL binary : binaryRoots) {
                    try {
                        p.addFile(new File(binary.toURI()).getAbsolutePath());
                    } catch (URISyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            } else {
                ClassPath binary = ClassPathSupport.createClassPath(binaryRoots);
                List<FileObject> sigFiles = new ArrayList<FileObject>();

                for (String className : classNames) {
                    FileObject classFO = binary.findResource(className.replace('.', '/') + ".sig"); //NOI18N

                    if (classFO != null) {
                        sigFiles.add(classFO);
                    } else {
                        LOG.log(Level.WARNING, "Cannot find sig file for: " + className); //TODO: should probably become FINE eventually
                    }
                }

                assert validator != null;

                if (!validator.validate(sigFiles)) return null;

                for (FileObject classFO : sigFiles) {
                    p.addFile(new File(classFO.toURI()).getAbsolutePath());
                }

                addCompileRootAsSource(p, sourceRoot);
            }

            ClassPath compile = ClassPath.getClassPath(sourceRoot, ClassPath.COMPILE);

            for (FileObject compileRoot : compile.getRoots()) {
                addCompileRoot(p, compileRoot);
            }
            
            DetectorCollectionProvider.initializeDetectorFactoryCollection();

            final Profiler profiler = new Profiler() {
                private final AtomicLong depth = new AtomicLong();
                @Override public void start(Class<?> c) {
                    depth.incrementAndGet();
                    super.start(c);
                }
                @Override public void end(Class<?> c) {
                    if (depth.getAndDecrement() > 0) {
                        super.end(c);
                    }
                }
            };
            final ProjectStats projectStats = new ProjectStats() {
                @Override public Profiler getProfiler() {
                    return profiler;
                }
            };
                
            BugCollectionBugReporter r = new BugCollectionBugReporter(p) {
                @Override protected void emitLine(String line) {
                    LOG.log(Level.FINE, line);
                }
                @Override public ProjectStats getProjectStats() {
                    return projectStats;
                }
            };

            r.setPriorityThreshold(Priorities.LOW_PRIORITY);
            r.setRankThreshold(20);

            FindBugs2 engine = new FindBugs2();

            engine.setProject(p);
            engine.setNoClassOk(true);
            engine.setBugReporter(r);
            
            if (progress != null || cancel != null) {
                engine.setProgressCallback(new FindBugsProgress() {
                    @Override public void reportNumberOfArchives(int i) {
                        if (cancel != null && cancel.isCancelled()) throw new Stop();
                        if (progress != null) progress.reportNumberOfArchives(i);
                    }
                    @Override public void startArchive(String string) {
                        if (cancel != null && cancel.isCancelled()) throw new Stop();
                        if (progress != null) progress.startArchive(string);
                    }
                    @Override public void finishArchive() {
                        if (cancel != null && cancel.isCancelled()) throw new Stop();
                        if (progress != null) progress.finishArchive();
                    }
                    @Override public void predictPassCount(int[] ints) {
                        if (cancel != null && cancel.isCancelled()) throw new Stop();
                        if (progress != null) progress.predictPassCount(ints);
                    }
                    @Override public void startAnalysis(int i) {
                        if (cancel != null && cancel.isCancelled()) throw new Stop();
                        if (progress != null) progress.startAnalysis(i);
                    }
                    @Override public void finishClass() {
                        if (cancel != null && cancel.isCancelled()) throw new Stop();
                        if (progress != null) progress.finishClass();
                    }
                    @Override public void finishPerClassAnalysis() {
                        if (cancel != null && cancel.isCancelled()) throw new Stop();
                        if (progress != null) progress.finishPerClassAnalysis();
                    }
                });
            }

            boolean inEditor = validator != null;
            final Preferences settings = customSettings != null ? customSettings : NbPreferences.forModule(RunFindBugs.class).node("global-settings");
            UserPreferences preferences;

            if (singleBug != null) {
                final String singleBugPattern = singleBug.substring(PREFIX_FINDBUGS.length());
                preferences = preparePreferences(new EnableBugPattern() {
                    @Override public boolean enable(BugPattern bp, DetectorFactory df) {
                        return singleBugPattern.equals(bp.getType());
                    }
                });
            } else {
                final boolean defaultsToDisabled = customSettings != null;
                preferences = preparePreferences(new EnableBugPattern() {
                    @Override public boolean enable(BugPattern bp, DetectorFactory df) {
                        return settings.getBoolean(bp.getType(), !defaultsToDisabled && DEFAULT_PREFS.isDetectorEnabled(df));
                    }
                });
            }

            if (preferences == null) {
                //nothing enabled, stop
                return result;
            }
            
            engine.setUserPreferences(preferences);
            engine.setDetectorFactoryCollection(DetectorFactoryCollection.instance());

            LOG.log(Level.FINE, "Running FindBugs");
            
            engine.execute();

            Map<FileObject, List<BugInstance>> file2Bugs = new HashMap<FileObject, List<BugInstance>>();
            
            for (BugInstance b : r.getBugCollection().getCollection()) {
                if (cancel != null && cancel.isCancelled()) return null;
                if (singleBug != null && !singleBug.equals(b.getBugPattern().getType())) continue;
                if (singleBug == null && !settings.getBoolean(b.getBugPattern().getType(), customSettings == null && isEnabledByDefault(b.getBugPattern()))) {
                    continue;
                }

                SourceLineAnnotation sourceLine = b.getPrimarySourceLineAnnotation();
                FileObject sourceFile = null;

                if (sourceLine != null) {
                    sourceFile = sourceRoot.getFileObject(sourceLine.getSourcePath());

                    if (sourceFile != null) {
                        List<BugInstance> bugs = file2Bugs.get(sourceFile);
                        
                        if (bugs == null) {
                            file2Bugs.put(sourceFile, bugs = new ArrayList<BugInstance>());
                        }
                        
                        bugs.add(b);
                    } else {
                        LOG.log(Level.WARNING, "{0}, location: {1}:{2}", new Object[]{b, sourceLine.getSourcePath(), sourceLine.getStartLine()});
                    }
                }
            }
            
            for (Entry<FileObject, List<BugInstance>> e : file2Bugs.entrySet()) {
                if (cancel != null && cancel.isCancelled()) return null;
                
                int[] lineOffsets = null;
                FileObject sourceFile = e.getKey();
                DataObject d = DataObject.find(sourceFile);
                EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
                Document doc = ec.getDocument();
                JavaSource js = null;
                
                for (BugInstance b : e.getValue()) {
                    if (cancel != null && cancel.isCancelled()) return null;
                    SourceLineAnnotation sourceLine = b.getPrimarySourceLineAnnotation();

                    if ("UPM_UNCALLED_PRIVATE_METHOD".equals(b.getBugPattern().getType())) {
                        if (js == null) {
                            js = JavaSource.forFileObject(sourceFile);
                        }
                        int[] span = spanForEnclosingMethod(info, js, sourceLine.getStartLine());
                        if (span != null && span[0] != (-1)) {
                            LazyFixList fixes = prepareFixes(b, inEditor, sourceFile, -1, span);
                            result.add(ErrorDescriptionFactory.createErrorDescription(PREFIX_FINDBUGS + b.getType(), Severity.VERIFIER, b.getMessageWithoutPrefix(), b.getBugPattern().getDetailHTML(), fixes, sourceFile, span[0], span[1]));
                            continue;
                        }
                    }
                    if (sourceLine.getStartLine() >= 0) {
                        LazyFixList fixes = prepareFixes(b, inEditor, sourceFile, sourceLine.getStartLine(), null);
                        
                        if (doc != null) {
                            result.add(ErrorDescriptionFactory.createErrorDescription(PREFIX_FINDBUGS + b.getType(), Severity.VERIFIER, b.getMessageWithoutPrefix(), b.getBugPattern().getDetailHTML(), fixes, doc, sourceLine.getStartLine()));
                        } else {
                            if (lineOffsets == null) {
                                lineOffsets = computeLineMap(sourceFile, FileEncodingQuery.getEncoding(sourceFile));
                            }

                            int edLine = 2 * (Math.min(sourceLine.getStartLine(), lineOffsets.length / 2) - 1);

                            result.add(ErrorDescriptionFactory.createErrorDescription(PREFIX_FINDBUGS + b.getType(), Severity.VERIFIER, b.getMessageWithoutPrefix(), b.getBugPattern().getDetailHTML(), fixes, sourceFile, lineOffsets[edLine], lineOffsets[edLine + 1]));
                        }
                    } else {
                        if (js == null) {
                            js = JavaSource.forFileObject(sourceFile);
                        }
                        addByElementAnnotation(b, info, sourceFile, js, result, inEditor);
                    }
                }
            }
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            LOG.log(Level.FINE, null, ex);
        } catch (Stop stop) {
            //cancelled
            return null;
        }

        return result;
    }

    private static void addByElementAnnotation(BugInstance b, CompilationInfo info, FileObject sourceFile, JavaSource js, List<ErrorDescription> result, boolean globalPreferences) {
        int[] span = null;
        FieldAnnotation fieldAnnotation = b.getPrimaryField();

        if (fieldAnnotation != null) {
            span = spanFor(info, js, fieldAnnotation);
        }

        MethodAnnotation methodAnnotation = b.getPrimaryMethod();

        if ((span == null || span[0] == (-1)) && methodAnnotation != null) {
            span = spanFor(info, js, methodAnnotation);
        }

        ClassAnnotation classAnnotation = b.getPrimaryClass();

        if ((span == null || span[0] == (-1)) && classAnnotation != null) {
            span = spanFor(info, js, classAnnotation);
        }

        if (span != null && span[0] != (-1)) {
            LazyFixList fixes = prepareFixes(b, globalPreferences, sourceFile, -1, span);
            result.add(ErrorDescriptionFactory.createErrorDescription(PREFIX_FINDBUGS + b.getType(), Severity.VERIFIER, b.getMessageWithoutPrefix(), b.getBugPattern().getDetailHTML(), fixes, sourceFile, span[0], span[1]));
        }
    }

    private static int[] spanFor(CompilationInfo info, JavaSource js, final PackageMemberAnnotation annotation) {
        final int[] result = new int[] {-1, -1};
        class TaskImpl implements Task<CompilationInfo> {
            @Override public void run(final CompilationInfo parameter) {
                TypeElement clazz = parameter.getElements().getTypeElement(annotation.getClassName());

                if (clazz == null) {
                    //XXX: log
                    return;
                }

                Element resolved = null;

                if (annotation instanceof FieldAnnotation) {
                    FieldAnnotation fa = (FieldAnnotation) annotation;
                    
                    for (VariableElement var : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
                        if (var.getSimpleName().contentEquals(fa.getFieldName())) {
                            resolved = var;
                            break;
                        }
                    }
                } else if (annotation instanceof MethodAnnotation) {
                    MethodAnnotation ma = (MethodAnnotation) annotation;

                    for (ExecutableElement method : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                        if (method.getSimpleName().contentEquals(ma.getMethodName())) {
                            if (ma.getMethodSignature().equals(SourceUtils.getJVMSignature(ElementHandle.create(method))[2])) {
                                resolved = method;
                                break;
                            }
                        }
                    }
                } else if (annotation instanceof ClassAnnotation) {
                    resolved = clazz;
                }

                if (resolved == null) {
                    //XXX: log
                    return;
                }

                final Element resolvedFin = resolved;

                new CancellableTreePathScanner<Void, Void>() {
                    @Override public Void visitVariable(VariableTree node, Void p) {
                        if (resolvedFin.equals(parameter.getTrees().getElement(getCurrentPath()))) {
                            int[] span = parameter.getTreeUtilities().findNameSpan(node);

                            if (span != null) {
                                result[0] = span[0];
                                result[1] = span[1];
                            }
                        }

                        return super.visitVariable(node, p);
                    }
                    @Override public Void visitMethod(MethodTree node, Void p) {
                        if (resolvedFin.equals(parameter.getTrees().getElement(getCurrentPath()))) {
                            int[] span = parameter.getTreeUtilities().findNameSpan(node);

                            if (span != null) {
                                result[0] = span[0];
                                result[1] = span[1];
                            }
                        }

                        return super.visitMethod(node, p);
                    }
                    @Override public Void visitClass(ClassTree node, Void p) {
                        if (resolvedFin.equals(parameter.getTrees().getElement(getCurrentPath()))) {
                            int[] span = parameter.getTreeUtilities().findNameSpan(node);

                            if (span != null) {
                                result[0] = span[0];
                                result[1] = span[1];
                            }
                        }

                        return super.visitClass(node, p);
                    }
                }.scan(parameter.getCompilationUnit(), null);
            }
        };
        
        runInJavac(info, js, new TaskImpl());

        return result;
    }

    private static int[] spanForEnclosingMethod(CompilationInfo info, JavaSource js, final int startLine) {
        final int[] result = new int[] {-1, -1};
        class TaskImpl implements Task<CompilationInfo> {
            @Override public void run(final CompilationInfo parameter) {
                long pos = parameter.getCompilationUnit().getLineMap().getStartPosition(startLine);
                for (Tree t : parameter.getTreeUtilities().pathFor((int) pos - 1)) {
                    if (t.getKind() == Kind.METHOD) {
                        int[] span = parameter.getTreeUtilities().findNameSpan((MethodTree) t);

                        if (span != null && span[0] < pos) {
                            result[0] = span[0];
                            result[1] = span[1];
                        }
                    }
                }
            }
        };
        
        runInJavac(info, js, new TaskImpl());

        return result;
    }
    
    private static void runInJavac(CompilationInfo info, JavaSource js, final Task<CompilationInfo> task) {
        if (info != null) {
            try {
                task.run(info);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    @Override public void run(CompilationController parameter) throws Exception {
                        parameter.toPhase(Phase.RESOLVED); //XXX: ENTER should be enough in most cases, but not for anonymous innerclasses.
                        task.run(parameter);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private static LazyFixList prepareFixes(BugInstance b, boolean globalPreferences, FileObject sourceFile, int line, int[] span) {
        List<Fix> fixes;

        if (globalPreferences) {
            String bugId = b.getBugPattern().getType();
            String bugDN = b.getBugPattern().getShortDescription();
            Fix topLevelFix = new TopLevelConfigureFix(bugId, bugDN);
            ErrorDescriptionFactory.attachSubfixes(topLevelFix, Arrays.asList(new DisableConfigure(bugId, bugDN, true),
                                                                              new DisableConfigure(bugId, bugDN, false),
                                                                              new InspectFix(WarningDescription.create(RunFindBugs.PREFIX_FINDBUGS + bugId, bugDN, null, null)),
                                                                              new SuppressWarningsFix(sourceFile, bugId, line, span != null ? span[0] : -1)));
            fixes = Collections.singletonList(topLevelFix);
        } else {
            fixes = Collections.emptyList();
        }

        return ErrorDescriptionFactory.lazyListForFixes(fixes);
    }

    private static final UserPreferences DEFAULT_PREFS = UserPreferences.createDefaultUserPreferences();
    
    private static UserPreferences preparePreferences(EnableBugPattern enabler) {
        boolean atLeastOneEnabled = false;
        UserPreferences prefs = UserPreferences.createDefaultUserPreferences();
        DetectorFactoryCollection dfc = DetectorFactoryCollection.instance();
        DetectorFactory fieldItemSummary = null;

        for (DetectorFactory df : dfc.getFactories()) {
            boolean enable = false;

            if (!df.isHidden()) {
                for (BugPattern bp : df.getReportedBugPatterns()) {
                    BugCategory c = dfc.getBugCategory(bp.getCategory());

                    if (c.isHidden()) continue;

                    enable |= enabler.enable(bp, df);
                }
                
                enable |= df.getReportedBugPatterns().isEmpty();
            }
            
            if ("edu.umd.cs.findbugs.detect.FieldItemSummary".contentEquals(df.getFullName())) {
                fieldItemSummary = df;
            }

            atLeastOneEnabled |= enable;
            prefs.enableDetector(df, enable);
        }
        
        if (atLeastOneEnabled) {
            prefs.enableDetector(fieldItemSummary, true);
        }

        return atLeastOneEnabled ? prefs : null;
    }
    
    private interface EnableBugPattern {
        public boolean enable(BugPattern bp, DetectorFactory df);
    }

    public static boolean isEnabledByDefault(BugPattern bp) {
        DetectorFactoryCollection dfc = DetectorFactoryCollection.instance();

        for (DetectorFactory df : dfc.getFactories()) {
            if (df.getReportedBugPatterns().contains(bp)) {
                return UserPreferences.createDefaultUserPreferences().isDetectorEnabled(df);
            }
        }

        return false;
    }

    private static void addCompileRoot(Project p, FileObject compileRoot) {
        Result2 sources = SourceForBinaryQuery.findSourceRoots2(compileRoot.toURL());

        if (sources.preferSources()) {
            //XXX:
            if (sources.getRoots().length == 0) {
                addAuxCPEntry(p, compileRoot.toURL());
            } else {
                for (FileObject source : sources.getRoots()) {
                    addCompileRootAsSource(p, source);
                }
            }
        } else {
            addAuxCPEntry(p, compileRoot.toURL());
        }
    }

    private static void addCompileRootAsSource(Project p, FileObject source) {
        for (URL br : CacheBinaryForSourceQuery.findCacheBinaryRoots(source.toURL()).getRoots()) {
            addAuxCPEntry(p, br);
        }
    }

    private static void addAuxCPEntry(Project p, URL url) {
        //XXX: need more reliable way
        File f = FileUtil.archiveOrDirForURL(url);

        if (f == null) return ;

        p.addAuxClasspathEntry(f.getAbsolutePath());
    }
    
    static int[] computeLineMap(FileObject file, Charset decoder) {
        Reader in = null;
        List<Integer> lineLengthsTemp = new ArrayList<Integer>();
        int currentOffset = 0;

        lineLengthsTemp.add(0);
        lineLengthsTemp.add(0);

        try {
            in = new InputStreamReader(file.getInputStream(), decoder);
            
            int read;
            boolean wascr = false;
            boolean lineStart = true;

            while ((read = in.read()) != (-1)) {
                currentOffset++;

                switch (read) {
                    case '\r':
                        wascr = true;
                        lineLengthsTemp.add(currentOffset);
                        lineLengthsTemp.add(currentOffset);
                        lineStart = true;
                        break;
                    case '\n':
                        if (wascr) {
                            wascr = false;
                            currentOffset--;
                            break;
                        }
                        lineLengthsTemp.add(currentOffset);
                        lineLengthsTemp.add(currentOffset);
                        wascr = false;
                        lineStart = true;
                        break;
                }
                
                if (lineStart && Character.isWhitespace(read)) {
                    lineLengthsTemp.set(lineLengthsTemp.size() - 2, currentOffset);
                    lineLengthsTemp.set(lineLengthsTemp.size() - 1, currentOffset);
                } else if (!Character.isWhitespace(read)) {
                    lineLengthsTemp.set(lineLengthsTemp.size() - 1, currentOffset);
                    lineStart = false;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        int[] lineOffsets = new int[lineLengthsTemp.size()];
        int i = 0;

        for (Integer o : lineLengthsTemp) {
            lineOffsets[i++] = o;
        }
        
        return lineOffsets;
    }

    public static String computeFilterText(BugPattern bp) {
        StringBuilder result = new StringBuilder();

        result.append(bp.getShortDescription())
              .append(bp.getLongDescription())
              .append(bp.getCategory())
              .append(bp.getDetailPlainText());

        return result.toString();
    }
    
    interface SigFilesValidator {
        public boolean validate(Iterable<? extends FileObject> files);
    }

    private static class DisableConfigure implements Fix {
        private final @NonNull String bugId;
        private final String bugDisplayName;
        private final boolean disable;

        public DisableConfigure(String bugId, String bugDisplayName, boolean disable) {
            this.bugId = bugId;
            this.bugDisplayName = bugDisplayName;
            this.disable = disable;
        }

        @Override
        @Messages({"FIX_DisableBug=Disable {0}",
                   "FIX_ConfigureBug=Configure {0}"})
        public String getText() {
            return disable ? Bundle.FIX_DisableBug(bugDisplayName) : Bundle.FIX_ConfigureBug(bugDisplayName);
        }

        @Override
        public ChangeInfo implement() throws Exception {
            if (disable) {
                NbPreferences.forModule(RunFindBugs.class).node("global-settings").putBoolean(bugId, false);
            } else {
                OptionsDisplayer.getDefault().open("Editor/Hints/text/findbugs+x-java/" + bugId);
            }

            return null;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.bugId != null ? this.bugId.hashCode() : 0);
            hash = 67 * hash + (this.bugDisplayName != null ? this.bugDisplayName.hashCode() : 0);
            hash = 67 * hash + (this.disable ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DisableConfigure other = (DisableConfigure) obj;
            if ((this.bugId == null) ? (other.bugId != null) : !this.bugId.equals(other.bugId)) {
                return false;
            }
            if ((this.bugDisplayName == null) ? (other.bugDisplayName != null) : !this.bugDisplayName.equals(other.bugDisplayName)) {
                return false;
            }
            if (this.disable != other.disable) {
                return false;
            }
            return true;
        }

    }

    private static final class TopLevelConfigureFix extends DisableConfigure implements EnhancedFix {

        public TopLevelConfigureFix(String bugId, String bugDisplayName) {
            super(bugId, bugDisplayName, false);
        }

        @Override
        public CharSequence getSortText() {
            return "\uFFFFzz";
        }

    }

    private static final String[] SUPPRESS_WARNINGS_ANNOTATIONS = {
        org.netbeans.api.annotations.common.SuppressWarnings.class.getName(),
        edu.umd.cs.findbugs.annotations.SuppressWarnings.class.getName()
    };

    private static final Set<Kind> ANNOTATABLE = EnumSet.of(Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.METHOD, Kind.VARIABLE);
    
    private static class SuppressWarningsFix implements Fix {

        private final FileObject sourceCode;
        private final String bugId;
        //TODO: should probably keep Position or PositionRef
        private final int line;
        private final int pos;
        
        public SuppressWarningsFix(FileObject sourceCode, String bugId, int line, int pos) {
            this.sourceCode = sourceCode;
            this.bugId = bugId;
            this.line = line;
            this.pos = pos;
        }

        @Override
        @Messages("FIX_SuppressWarnings=Suppress Warning")
        public String getText() {
            return Bundle.FIX_SuppressWarnings();
        }

        @Override
        public ChangeInfo implement() throws Exception {
            JavaSource js = JavaSource.forFileObject(sourceCode);

            js.runModificationTask(new Task<WorkingCopy>() {
                @Override
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    TypeElement suppressWarningsAnnotation = null;

                    for (String ann : SUPPRESS_WARNINGS_ANNOTATIONS) {
                        if ((suppressWarningsAnnotation = parameter.getElements().getTypeElement(ann)) != null) break;
                    }

                    if (suppressWarningsAnnotation == null) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message("Cannot find SuppressWarnings annotation with Retention.CLASS, please add some on the classpath", NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notifyLater(nd);
                        return;
                    }

                    int realPos;
                    
                    if (pos != (-1)) {
                        realPos = pos;
                    } else {
                        realPos = (int) parameter.getCompilationUnit().getLineMap().getPosition(line, 0);
                    }

                    TreePath tp = parameter.getTreeUtilities().pathFor(realPos);

                    while (tp != null && !ANNOTATABLE.contains(tp.getLeaf().getKind())) {
                        tp = tp.getParentPath();
                    }

                    if (tp == null) return;

                    ModifiersTree mods;
                    Tree leaf = tp.getLeaf();

                    switch (leaf.getKind()) {
                        case ANNOTATION_TYPE:
                        case CLASS:
                        case ENUM:
                        case INTERFACE:
                            mods = ((ClassTree) leaf).getModifiers();
                            break;
                        case METHOD:
                            mods = ((MethodTree) leaf).getModifiers();
                            break;
                        case VARIABLE:
                            mods = ((VariableTree) leaf).getModifiers();
                            break;
                        default:
                            throw new IllegalStateException(leaf.getKind().name());
                    }

                    parameter.rewrite(mods, GeneratorUtilities.get(parameter).appendToAnnotationValue(mods, suppressWarningsAnnotation, "value", parameter.getTreeMaker().Literal(bugId)));
                }
            }).commit();

            return null;
        }

    }
    
    private static class InspectFix implements Fix {
        private final @NonNull WarningDescription wd;

        InspectFix(WarningDescription wd) {
            this.wd = wd;
        }

        @Override
        @Messages({
            "DN_Inspect=Run Inspect on..."
        })
        public String getText() {
            return Bundle.DN_Inspect();
        }

        @Override
        public ChangeInfo implement() throws Exception {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    CodeAnalysis.open(wd);
                }
            });
            
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final InspectFix other = (InspectFix) obj;
            if (this.wd != other.wd && (this.wd == null || !this.wd.equals(other.wd))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + (this.wd != null ? this.wd.hashCode() : 0);
            return hash;
        }

    }
    
    public static interface Cancel {
        public boolean isCancelled();
    }
    
    private static final class Stop extends ThreadDeath { }
}
