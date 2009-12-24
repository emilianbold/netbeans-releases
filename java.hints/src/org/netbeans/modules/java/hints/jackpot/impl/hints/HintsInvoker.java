/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.jackpot.impl.MessageImpl;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;
import org.netbeans.modules.java.hints.jackpot.impl.pm.BulkSearch;
import org.netbeans.modules.java.hints.jackpot.impl.pm.BulkSearch.BulkPattern;
import org.netbeans.modules.java.hints.introduce.CopyFinder;
import org.netbeans.modules.java.hints.introduce.CopyFinder.VariableAssignments;
import org.netbeans.modules.java.hints.jackpot.impl.pm.Pattern;
import org.netbeans.modules.java.hints.jackpot.spi.Hacks;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription.PatternDescription;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class HintsInvoker implements CancellableTask<CompilationInfo> {

    private static final Logger TIMER = Logger.getLogger("TIMER");
    
    private final AtomicBoolean cancel = new AtomicBoolean();
    private final Map<String, Long> timeLog = new HashMap<String, Long>();
    
    public void run(CompilationInfo info) {
        cancel.set(false);
        timeLog.clear();

        long startTime = System.currentTimeMillis();

        List<ErrorDescription> result = computeHints(info);

        if (cancel.get()) {
            return;
        }

        HintsController.setErrors(info.getFileObject(), HintsInvoker.class.getName(), result);

        long endTime = System.currentTimeMillis();
        
        TIMER.log(Level.FINE, "Jackpot 3.0 Hints Task", new Object[] {info.getFileObject(), endTime - startTime});

        for (Entry<String, Long> e : timeLog.entrySet()) {
            TIMER.log(Level.FINE, e.getKey(), new Object[] {info.getFileObject(), e.getValue()});
        }
    }

    public void cancel() {
        cancel.set(true);
    }

    public List<ErrorDescription> computeHints(CompilationInfo info) {
        return computeHints(info, new TreePath(info.getCompilationUnit()));
    }

    private List<ErrorDescription> computeHints(CompilationInfo info, TreePath startAt) {
        Map<Kind, List<HintDescription>> kindHints = new HashMap<Kind, List<HintDescription>>();
        Map<PatternDescription, List<HintDescription>> patternHints = new HashMap<PatternDescription, List<HintDescription>>();
        
        for (Entry<Kind, List<HintDescription>> e : RulesManager.getInstance().getKindBasedHints().entrySet()) {
            kindHints.put(e.getKey(), new LinkedList<HintDescription>(e.getValue()));
        }

        for (Entry<PatternDescription, List<HintDescription>> e : RulesManager.getInstance().getPatternBasedHints().entrySet()) {
            patternHints.put(e.getKey(), new LinkedList<HintDescription>(e.getValue()));
        }

        long elementBasedStart = System.currentTimeMillis();

        RulesManager.computeElementBasedHintsXXX(info, cancel, kindHints, patternHints);
        
        long elementBasedEnd = System.currentTimeMillis();

        timeLog.put("Jackpot 3.0 Element Based Hints", elementBasedEnd - elementBasedStart);

        return computeHints(info, startAt, kindHints, patternHints, new LinkedList<MessageImpl>());
    }

    public List<ErrorDescription> computeHints(CompilationInfo info,
                                        Map<Kind, List<HintDescription>> hints,
                                        Map<PatternDescription, List<HintDescription>> patternHints) {
        return computeHints(info, hints, patternHints, new LinkedList<MessageImpl>());
    }

    public List<ErrorDescription> computeHints(CompilationInfo info,
                                        Map<Kind, List<HintDescription>> hints,
                                        Map<PatternDescription, List<HintDescription>> patternHints,
                                        Collection<? super MessageImpl> problems) {
        return computeHints(info, new TreePath(info.getCompilationUnit()), hints, patternHints, problems);
    }

    List<ErrorDescription> computeHints(CompilationInfo info,
                                        TreePath startAt,
                                        Map<Kind, List<HintDescription>> hints,
                                        Map<PatternDescription, List<HintDescription>> patternHints,
                                        Collection<? super MessageImpl> problems) {
        List<ErrorDescription> errors = new  LinkedList<ErrorDescription>();

        if (!hints.isEmpty()) {
            long kindStart = System.currentTimeMillis();

            new ScannerImpl(info, cancel, hints).scan(startAt, errors);

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Jackpot 3.0 Kind Based Hints", kindEnd - kindStart);
        }

        long patternStart = System.currentTimeMillis();
        
        Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

        long bulkStart = System.currentTimeMillis();
        
        BulkPattern bulkPattern = BulkSearch.getDefault().create(info, patternTests.keySet());
        Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, info.getCompilationUnit(), bulkPattern, timeLog);
        
        long bulkEnd = System.currentTimeMillis();

        timeLog.put("Jackpot 3.0 Bulk Search", bulkEnd - bulkStart);
        
        errors.addAll(doComputeHints(info, occurringPatterns, patternTests, startAt, patternHints, problems));

        long patternEnd = System.currentTimeMillis();

        timeLog.put("Jackpot 3.0 Pattern Based Hints", patternEnd - patternStart);

        return errors;
    }

    public List<ErrorDescription> doComputeHints(CompilationInfo info, Map<String, Collection<TreePath>> occurringPatterns, Map<String, List<PatternDescription>> patterns, Map<PatternDescription, List<HintDescription>> patternHints) throws IllegalStateException {
        return doComputeHints(info, occurringPatterns, patterns, new TreePath(info.getCompilationUnit()), patternHints, new LinkedList<MessageImpl>());
    }

    public static Map<String, List<PatternDescription>> computePatternTests(Map<PatternDescription, List<HintDescription>> patternHints) {
        Map<String, List<PatternDescription>> patternTests = new HashMap<String, List<PatternDescription>>();
        for (Entry<PatternDescription, List<HintDescription>> e : patternHints.entrySet()) {
            String p = e.getKey().getPattern();
            List<PatternDescription> descs = patternTests.get(p);
            if (descs == null) {
                patternTests.put(p, descs = new LinkedList<PatternDescription>());
            }
            descs.add(e.getKey());
        }
        return patternTests;
    }
    
    private List<ErrorDescription> doComputeHints(CompilationInfo info, Map<String, Collection<TreePath>> occurringPatterns, Map<String, List<PatternDescription>> patterns, TreePath startAt, Map<PatternDescription, List<HintDescription>> patternHints, Collection<? super MessageImpl> problems) throws IllegalStateException {
        List<ErrorDescription> errors = new LinkedList<ErrorDescription>();
        
        for (Entry<String, Collection<TreePath>> occ : occurringPatterns.entrySet()) {
            for (PatternDescription d : patterns.get(occ.getKey())) {
                Map<String, TypeMirror> constraints = new HashMap<String, TypeMirror>();

                for (Entry<String, String> e : d.getConstraints().entrySet()) {
                    constraints.put(e.getKey(), Hacks.parseFQNType(info, e.getValue()));
                }

                Pattern p = Pattern.compile(info, occ.getKey(), constraints, d.getImports());
                TreePath toplevel = new TreePath(info.getCompilationUnit());
                TreePath patt = new TreePath(toplevel, p.getPattern());

                for (TreePath candidate : occ.getValue()) {
                    VariableAssignments verified = CopyFinder.computeVariables(info, patt, candidate, cancel, p.getConstraints());

                    if (verified == null) {
                        continue;
                    }
                    
                    Set<String> suppressedWarnings = new HashSet<String>(Utilities.findSuppressedWarnings(info, candidate));
                    HintContext c = new HintContext(info, AbstractHint.HintSeverity.WARNING, candidate, verified.variables, verified.multiVariables, verified.variables2Names, problems);

                    for (HintDescription hd : patternHints.get(d)) {
                        if (!Collections.disjoint(suppressedWarnings, hd.getSuppressWarnings()))
                            continue;
                        
                        Collection<? extends ErrorDescription> workerErrors = hd.getWorker().createErrors(c);

                        if (workerErrors != null) {
                            errors.addAll(workerErrors);
                        }
                    }
                }
            }
        }

        return errors;
    }
    
//    public static void computeHints(URI file, ProcessingEnvironment env, CompilationUnitTree cut, RulesManager m) {
//        Map<Kind, HintDescription> hints = m.getKindBasedHints();
//
//        if (hints.isEmpty()) {
//            return ;
//        }
//
//        List<ErrorDescription> errors = new  LinkedList<ErrorDescription>();
//
//        File af = new File(file.getPath());
//        FileObject f = FileUtil.toFileObject(af);
//
//        new ScannerImpl(f, env, hints).scan(cut, errors);
//
//        for (ErrorDescription ed : errors) {
//            Diagnostic.Kind k;
//
//            switch (ed.getSeverity()) {
//                case ERROR:
//                    k = Diagnostic.Kind.ERROR;
//                    break;
//                default:
//                    k = Diagnostic.Kind.WARNING;
//                    break;
//            }
//
//            env.getMessager().printMessage(k, ed.getDescription());
//        }
//    }
    
    private static final class ScannerImpl extends CancellableTreePathScanner<Void, List<ErrorDescription>> {

        private final Stack<Set<String>> suppresWarnings = new Stack<Set<String>>();
        private final CompilationInfo info;
        private final FileObject file;
        private final ProcessingEnvironment env;
        private final Map<Kind, List<HintDescription>> hints;

        public ScannerImpl(CompilationInfo info, AtomicBoolean cancel, Map<Kind, List<HintDescription>> hints) {
            super(cancel);
            this.info = info;
            this.file = null;
            this.env  = null;
            this.hints = hints;
        }

        public ScannerImpl(FileObject file, ProcessingEnvironment env, Map<Kind, List<HintDescription>> hints) {
            super(new AtomicBoolean());
            this.info = null;
            this.file = file;
            this.env = env;
            this.hints = hints;
        }

        private void runAndAdd(TreePath path, List<HintDescription> rules, List<ErrorDescription> d) {
            if (rules != null && !isInGuarded(info, path)) {
                for (HintDescription hd : rules) {
                    if (isCanceled()) {
                        return ;
                    }

                    boolean enabled = true;
                    String[] suppressedBy = null;

//                    if (tr instanceof AbstractHint) {
//                        enabled = HintsSettings.isEnabled((AbstractHint)tr);
//                        suppressedBy = HintsSettings.getSuppressedBy((AbstractHint)tr);
//                    }

                    if ( suppressedBy != null && suppressedBy.length != 0 ) {
                        for (String wname : suppressedBy) {
                            if( !suppresWarnings.empty() && suppresWarnings.peek().contains(wname)) {
                                return;
                            }
                        }
                    }

                    if (enabled) {
                        HintContext c = new HintContext(info, AbstractHint.HintSeverity.WARNING, path, Collections.<String, TreePath>emptyMap(), Collections.<String, Collection<? extends TreePath>>emptyMap(), Collections.<String, String>emptyMap());
                        Collection<? extends ErrorDescription> errors = hd.getWorker().createErrors(c);

                        if (errors != null) {
                            d.addAll(errors);
                        }
                    }
                }
            }
        }

        @Override
        public Void scan(Tree tree, List<ErrorDescription> p) {
            if (tree == null)
                return null;

            TreePath tp = new TreePath(getCurrentPath(), tree);
            Kind k = tree.getKind();

            runAndAdd(tp, hints.get(k), p);

            if (isCanceled()) {
                return null;
            }

            return super.scan(tree, p);
        }

        @Override
        public Void scan(TreePath path, List<ErrorDescription> p) {
            Kind k = path.getLeaf().getKind();
            runAndAdd(path, hints.get(k), p);

            if (isCanceled()) {
                return null;
            }

            return super.scan(path, p);
        }

        @Override
        public Void visitMethod(MethodTree tree, List<ErrorDescription> arg1) {
            pushSuppressWarrnings();
            Void r = super.visitMethod(tree, arg1);
            suppresWarnings.pop();
            return r;
        }

        @Override
        public Void visitClass(ClassTree tree, List<ErrorDescription> arg1) {
            pushSuppressWarrnings();
            Void r = super.visitClass(tree, arg1);
            suppresWarnings.pop();
            return r;
        }

        @Override
        public Void visitVariable(VariableTree tree, List<ErrorDescription> arg1) {
            pushSuppressWarrnings();
            Void r = super.visitVariable(tree, arg1);
            suppresWarnings.pop();
            return r;
        }

        private void pushSuppressWarrnings( ) {
            Set<String> current = suppresWarnings.size() == 0 ? null : suppresWarnings.peek();
            Set<String> nju = current == null ? new HashSet<String>() : new HashSet<String>(current);

            Element e = getTrees().getElement(getCurrentPath());

            if ( e != null) {
                for (AnnotationMirror am : e.getAnnotationMirrors()) {
                    String name = ((TypeElement)am.getAnnotationType().asElement()).getQualifiedName().toString();
                    if ( "java.lang.SuppressWarnings".equals(name) ) { // NOI18N
                        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = am.getElementValues();
                        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                            if( "value".equals(entry.getKey().getSimpleName().toString()) ) { // NOI18N
                                Object value = entry.getValue().getValue();
                                if ( value instanceof List) {
                                    for (Object av : (List)value) {
                                        if( av instanceof AnnotationValue ) {
                                            Object wname = ((AnnotationValue)av).getValue();
                                            if ( wname instanceof String ) {
                                                nju.add((String)wname);
                                            }
                                        }
                                    }

                                }
                            }
                        }

                    }
                }
            }

            suppresWarnings.push(nju);
        }

        private Trees getTrees() {
            return info != null ? info.getTrees() : Trees.instance(env);
        }
    }

    static boolean isInGuarded(CompilationInfo info, TreePath tree) {
        if (info == null) {
            return false;
        }
        
        try {
            Document doc = info.getDocument();

            if (doc instanceof GuardedDocument) {
                int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree.getLeaf());
                int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree.getLeaf());
                GuardedDocument gdoc = (GuardedDocument) doc;
                MarkBlockChain guardedBlockChain = gdoc.getGuardedBlockChain();
                if (guardedBlockChain.compareBlock(start, end) == MarkBlock.INNER) {
                    return true;
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class FactoryImpl extends EditorAwareJavaSourceTaskFactory {

        public FactoryImpl() {
            super(Phase.RESOLVED, Priority.LOW);
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new HintsInvoker();
        }
        
    }
}
