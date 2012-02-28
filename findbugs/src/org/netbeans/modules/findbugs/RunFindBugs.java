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
import com.sun.source.tree.VariableTree;
import edu.umd.cs.findbugs.BugCategory;
import edu.umd.cs.findbugs.BugCollectionBugReporter;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.DetectorFactory;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.PackageMemberAnnotation;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.SourceLineAnnotation;
import edu.umd.cs.findbugs.config.UserPreferences;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result2;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author lahvac
 */
public class RunFindBugs {

    public static final String PREFIX_FINDBUGS = "findbugs:";
    private static final Logger LOG = Logger.getLogger(RunFindBugs.class.getName());
    
    public static List<ErrorDescription> runFindBugs(CompilationInfo info, Preferences customSettings, String singleBug, FileObject sourceRoot, Iterable<? extends String> classNames, SigFilesValidator validator) {
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

            BugCollectionBugReporter r = new BugCollectionBugReporter(p);

            r.setPriorityThreshold(Integer.MAX_VALUE);
            r.setRankThreshold(Integer.MAX_VALUE);

            FindBugs2 engine = new FindBugs2();

            engine.setProject(p);
            engine.setNoClassOk(true);
            engine.setBugReporter(r);

            Preferences settings = customSettings != null ? customSettings : NbPreferences.forModule(RunFindBugs.class).node("global-settings");
            UserPreferences preferences;

            if (singleBug != null) {
                singleBug = singleBug.substring(PREFIX_FINDBUGS.length());
                preferences = forSingleBug(singleBug);
            } else {
                preferences = readPreferences(settings, customSettings != null);
            }

            if (preferences == null) {
                //nothing enabled, stop
                return result;
            }
            
            engine.setUserPreferences(preferences);
            engine.setDetectorFactoryCollection(DetectorFactoryCollection.instance());

            LOG.log(Level.INFO, "Running FindBugs");
            
            engine.execute();

            for (BugInstance b : r.getBugCollection().getCollection()) {
                if (singleBug != null && !singleBug.equals(b.getBugPattern().getType())) continue;
                if (singleBug == null && !settings.getBoolean(b.getBugPattern().getType(), customSettings == null && isEnabledByDefault(b.getBugPattern()))) {
                    continue;
                }

                SourceLineAnnotation sourceLine = b.getPrimarySourceLineAnnotation();
                FileObject sourceFile = null;

                if (sourceLine != null) {
                    sourceFile = sourceRoot.getFileObject(sourceLine.getSourcePath());

                    if (sourceFile != null && sourceLine.getStartLine() >= 0) {
                        DataObject d = DataObject.find(sourceFile);
                        EditorCookie ec = d.getLookup().lookup(EditorCookie.class);
                        Document doc = ec.openDocument();
                        result.add(ErrorDescriptionFactory.createErrorDescription(PREFIX_FINDBUGS + b.getType(), Severity.VERIFIER, b.getMessageWithoutPrefix(), b.getBugPattern().getDetailHTML(), ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()), doc, sourceLine.getStartLine()));
                    } else {
                        if (sourceFile != null) {
                            addByElementAnnotation(b, info, sourceFile, result);
                        } else {
                            LOG.log(Level.WARNING, "{0}, location: {1}:{2}", new Object[]{b, sourceLine.getSourcePath(), sourceLine.getStartLine()});
                        }
                    }
                }
            }
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    private static void addByElementAnnotation(BugInstance b, CompilationInfo info, FileObject sourceFile, List<ErrorDescription> result) {
        int[] span = null;
        FieldAnnotation fieldAnnotation = b.getPrimaryField();

        if (fieldAnnotation != null) {
            span = spanFor(info, sourceFile, fieldAnnotation);
        }

        MethodAnnotation methodAnnotation = b.getPrimaryMethod();

        if ((span == null || span[0] == (-1)) && methodAnnotation != null) {
            span = spanFor(info, sourceFile, methodAnnotation);
        }

        ClassAnnotation classAnnotation = b.getPrimaryClass();

        if ((span == null || span[0] == (-1)) && classAnnotation != null) {
            span = spanFor(info, sourceFile, classAnnotation);
        }

        if (span != null && span[0] != (-1)) {
            result.add(ErrorDescriptionFactory.createErrorDescription(PREFIX_FINDBUGS + b.getType(), Severity.VERIFIER, b.getMessageWithoutPrefix(), b.getBugPattern().getDetailHTML(), ErrorDescriptionFactory.lazyListForFixes(Collections.<Fix>emptyList()), sourceFile, span[0], span[1]));
        }
    }

    private static int[] spanFor(CompilationInfo info, FileObject sourceFile, final PackageMemberAnnotation annotation) {
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
        
        final TaskImpl convertor = new TaskImpl();

        if (info != null) {
            convertor.run(info);
        } else {
            try {
                JavaSource.forFileObject(sourceFile).runUserActionTask(new Task<CompilationController>() {
                    @Override public void run(CompilationController parameter) throws Exception {
                        parameter.toPhase(Phase.RESOLVED); //XXX: ENTER should be enough in most cases, but not for anonymous innerclasses.
                        convertor.run(parameter);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return result;
    }

    private static UserPreferences readPreferences(Preferences settings, boolean defaultsToDisabled) {
        boolean atLeastOneEnabled = false;
        UserPreferences prefs = UserPreferences.createDefaultUserPreferences();
        DetectorFactoryCollection dfc = DetectorFactoryCollection.instance();

        for (DetectorFactory df : dfc.getFactories()) {
            boolean enable = false;

            for (BugPattern bp : df.getReportedBugPatterns()) {
                BugCategory c = dfc.getBugCategory(bp.getCategory());

                if (c.isHidden()) continue;

                enable |= settings.getBoolean(bp.getType(), !defaultsToDisabled && prefs.isDetectorEnabled(df));
            }

            atLeastOneEnabled |= enable;
            prefs.enableDetector(df, enable);
        }

        return atLeastOneEnabled ? prefs : null;
    }

    private static UserPreferences forSingleBug(String id) {
        boolean atLeastOneEnabled = false;
        UserPreferences prefs = UserPreferences.createDefaultUserPreferences();

        for (DetectorFactory df : DetectorFactoryCollection.instance().getFactories()) {
            boolean enable = false;

            for (BugPattern bp : df.getReportedBugPatterns()) {
                if (id.equals(bp.getType())) {
                    enable = true;
                    break;
                }
            }

            atLeastOneEnabled |= enable;
            prefs.enableDetector(df, enable);
        }

        return atLeastOneEnabled ? prefs : null;
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

    interface SigFilesValidator {
        public boolean validate(Iterable<? extends FileObject> files);
    }
}
