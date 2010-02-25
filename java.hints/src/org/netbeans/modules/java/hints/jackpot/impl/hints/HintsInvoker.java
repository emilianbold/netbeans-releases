/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl.hints;

import com.sun.source.tree.Tree;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.Document;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.MarkBlockChain;
import org.openide.filesystems.FileObject;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.impl.MessageImpl;
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
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.spi.AbstractHint.HintSeverity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class HintsInvoker {

    private final Map<String, Long> timeLog = new HashMap<String, Long>();

    private final CompilationInfo info;
    private final int caret;
    private final int from;
    private final int to;
    private final AtomicBoolean cancel;

    public HintsInvoker(CompilationInfo info, AtomicBoolean cancel) {
        this(info, -1, cancel);
    }

    public HintsInvoker(CompilationInfo info, int caret, AtomicBoolean cancel) {
        this(info, caret, -1, -1, cancel);
    }

    public HintsInvoker(CompilationInfo info, int from, int to, AtomicBoolean cancel) {
        this(info, -1, from, to, cancel);
    }

    private HintsInvoker(CompilationInfo info, int caret, int from, int to, AtomicBoolean cancel) {
        this.info = info;
        this.caret = caret;
        this.from = from;
        this.to = to;
        this.cancel = cancel;
    }

    public List<ErrorDescription> computeHints(CompilationInfo info) {
        return computeHints(info, new TreePath(info.getCompilationUnit()));
    }

    private List<ErrorDescription> computeHints(CompilationInfo info, TreePath startAt) {
        List<HintDescription> descs = new LinkedList<HintDescription>();
        for (Entry<HintMetadata, Collection<? extends HintDescription>> e : RulesManager.getInstance().allHints.entrySet()) {
            HintMetadata m = e.getKey();

            if (!HintsSettings.isEnabled(m)) {
                continue;
            }

            if (caret != -1) {
                if (m.kind == HintMetadata.Kind.SUGGESTION || m.kind == HintMetadata.Kind.SUGGESTION_NON_GUI) {
                    descs.addAll(e.getValue());
                } else {
                    Preferences pref = RulesManager.getPreferences(m.id, HintsSettings.getCurrentProfileId());
                    if (HintsSettings.getSeverity(m, pref) == HintSeverity.CURRENT_LINE_WARNING) {
                        descs.addAll(e.getValue());
                    }
                }
            } else {
                if (m.kind == HintMetadata.Kind.HINT || m.kind == HintMetadata.Kind.HINT_NON_GUI) {
                    Preferences pref = RulesManager.getPreferences(m.id, HintsSettings.getCurrentProfileId());
                    if (HintsSettings.getSeverity(m, pref) != HintSeverity.CURRENT_LINE_WARNING) {
                        descs.addAll(e.getValue());
                    }
                }
            }
        }

        Map<Kind, List<HintDescription>> kindHints = new HashMap<Kind, List<HintDescription>>();
        Map<PatternDescription, List<HintDescription>> patternHints = new HashMap<PatternDescription, List<HintDescription>>();

        RulesManager.sortOut(descs, kindHints, patternHints);

        long elementBasedStart = System.currentTimeMillis();

        RulesManager.computeElementBasedHintsXXX(info, cancel, kindHints, patternHints);

        long elementBasedEnd = System.currentTimeMillis();

        timeLog.put("Computing Element Based Hints", elementBasedEnd - elementBasedStart);

        List<ErrorDescription> errors = compute(info, startAt, kindHints, patternHints, new LinkedList<MessageImpl>());

        dumpTimeSpentInHints();
        
        return errors;
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
        return compute(info, new TreePath(info.getCompilationUnit()), hints, patternHints, problems);
    }

    List<ErrorDescription> compute(CompilationInfo info,
                                        TreePath startAt,
                                        Map<Kind, List<HintDescription>> hints,
                                        Map<PatternDescription, List<HintDescription>> patternHints,
                                        Collection<? super MessageImpl> problems) {
        if (caret != -1) {
            TreePath tp = info.getTreeUtilities().pathFor(caret);
            return computeSuggestions(info, tp, hints, patternHints, problems);
        } else {
            if (from != (-1) && to != (-1)) {
                return computeHintsInSpan(info, hints, patternHints, problems);
            } else {
                return computeHints(info, startAt, hints, patternHints, problems);
            }
        }
    }

    List<ErrorDescription> computeHints(CompilationInfo info,
                                        TreePath startAt,
                                        Map<Kind, List<HintDescription>> hints,
                                        Map<PatternDescription, List<HintDescription>> patternHints,
                                        Collection<? super MessageImpl> problems) {
        List<ErrorDescription> errors = new  LinkedList<ErrorDescription>();

        long kindCount = 0;

        for (Entry<Kind, List<HintDescription>> e : hints.entrySet()) {
            kindCount += e.getValue().size();
        }

        timeLog.put("[C] Kind Based Hints", kindCount);

        if (!hints.isEmpty()) {
            long kindStart = System.currentTimeMillis();

            new ScannerImpl(info, cancel, hints).scan(startAt, errors);

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Hints", kindEnd - kindStart);
        }

        timeLog.put("[C] Pattern Based Hints", (long) patternHints.size());

        long patternStart = System.currentTimeMillis();

        Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

        long bulkPatternStart = System.currentTimeMillis();

        BulkPattern bulkPattern = BulkSearch.getDefault().create(info, patternTests.keySet());

        long bulkPatternEnd = System.currentTimeMillis();

        timeLog.put("Bulk Pattern preparation", bulkPatternEnd - bulkPatternStart);

        long bulkStart = System.currentTimeMillis();

        Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, startAt, bulkPattern, timeLog);

        long bulkEnd = System.currentTimeMillis();

        timeLog.put("Bulk Search", bulkEnd - bulkStart);

        errors.addAll(doComputeHints(info, occurringPatterns, patternTests, patternHints, problems));

        long patternEnd = System.currentTimeMillis();

        timeLog.put("Pattern Based Hints", patternEnd - patternStart);

        return errors;
    }

    List<ErrorDescription> computeHintsInSpan(CompilationInfo info,
                                        Map<Kind, List<HintDescription>> hints,
                                        Map<PatternDescription, List<HintDescription>> patternHints,
                                        Collection<? super MessageImpl> problems) {

        TreePath path = info.getTreeUtilities().pathFor((from + to) / 2);

        while (path.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), path.getLeaf());
            int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), path.getLeaf());

            if (start <= from && end >= to) {
                break;
            }

            path = path.getParentPath();
        }

        List<ErrorDescription> errors = new LinkedList<ErrorDescription>();

        if (!hints.isEmpty()) {
            long kindStart = System.currentTimeMillis();

            new ScannerImpl(info, cancel, hints).scan(path, errors);

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Hints", kindEnd - kindStart);
        }

        if (!patternHints.isEmpty()) {
            long patternStart = System.currentTimeMillis();

            Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

            long bulkStart = System.currentTimeMillis();

            BulkPattern bulkPattern = BulkSearch.getDefault().create(info, patternTests.keySet());
            Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, path, bulkPattern, timeLog);

            long bulkEnd = System.currentTimeMillis();

            timeLog.put("Bulk Search", bulkEnd - bulkStart);

            errors.addAll(doComputeHints(info, occurringPatterns, patternTests, patternHints, problems));

            long patternEnd = System.currentTimeMillis();

            timeLog.put("Pattern Based Hints", patternEnd - patternStart);
        }

        return errors;
    }

    List<ErrorDescription> computeSuggestions(CompilationInfo info,
                                        TreePath workOn,
                                        Map<Kind, List<HintDescription>> hints,
                                        Map<PatternDescription, List<HintDescription>> patternHints,
                                        Collection<? super MessageImpl> problems) {
        List<ErrorDescription> errors = new  LinkedList<ErrorDescription>();

        if (!hints.isEmpty()) {
            long kindStart = System.currentTimeMillis();

            TreePath proc = workOn;

            while (proc != null) {
                new ScannerImpl(info, cancel, hints).scanDoNotGoDeeper(proc, errors);
                proc = proc.getParentPath();
            }

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Suggestions", kindEnd - kindStart);
        }

        if (!patternHints.isEmpty()) {
            long patternStart = System.currentTimeMillis();

            Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

            //pretend that all the patterns occur on all treepaths from the current path
            //up (probably faster than using BulkSearch over whole file)
            //TODO: what about machint trees under the current path?
            Set<TreePath> paths = new HashSet<TreePath>();

            TreePath tp = workOn;

            while (tp != null) {
                paths.add(tp);
                tp = tp.getParentPath();
            }

            Map<String, Collection<TreePath>> occurringPatterns = new HashMap<String, Collection<TreePath>>();

            for (String p : patternTests.keySet()) {
                occurringPatterns.put(p, paths);
            }

//            long bulkStart = System.currentTimeMillis();
//
//            BulkPattern bulkPattern = BulkSearch.getDefault().create(info, patternTests.keySet());
//            Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, new TreePath(info.getCompilationUnit()), bulkPattern, timeLog);
//
//            long bulkEnd = System.currentTimeMillis();
//
//            Set<Tree> acceptedLeafs = new HashSet<Tree>();
//
//            TreePath tp = workOn;
//
//            while (tp != null) {
//                acceptedLeafs.add(tp.getLeaf());
//                tp = tp.getParentPath();
//            }
//
//            for (Entry<String, Collection<TreePath>> e : occurringPatterns.entrySet()) {
//                for (Iterator<TreePath> it = e.getValue().iterator(); it.hasNext(); ) {
//                    if (!acceptedLeafs.contains(it.next().getLeaf())) {
//                        it.remove();
//                    }
//                }
//            }
//
//            timeLog.put("Bulk Search", bulkEnd - bulkStart);

            errors.addAll(doComputeHints(info, occurringPatterns, patternTests, patternHints, problems));

            long patternEnd = System.currentTimeMillis();

            timeLog.put("Pattern Based Hints", patternEnd - patternStart);
        }

        return errors;
    }

    public List<ErrorDescription> doComputeHints(CompilationInfo info, Map<String, Collection<TreePath>> occurringPatterns, Map<String, List<PatternDescription>> patterns, Map<PatternDescription, List<HintDescription>> patternHints) throws IllegalStateException {
        return doComputeHints(info, occurringPatterns, patterns, patternHints, new LinkedList<MessageImpl>());
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

    private List<ErrorDescription> doComputeHints(CompilationInfo info, Map<String, Collection<TreePath>> occurringPatterns, Map<String, List<PatternDescription>> patterns, Map<PatternDescription, List<HintDescription>> patternHints, Collection<? super MessageImpl> problems) throws IllegalStateException {
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

                    for (HintDescription hd : patternHints.get(d)) {
                        HintMetadata hm = hd.getMetadata();
                        HintContext c = new HintContext(info, hm, candidate, verified.variables, verified.multiVariables, verified.variables2Names, problems);

                        if (!Collections.disjoint(suppressedWarnings, hm.suppressWarnings))
                            continue;

                        Collection<? extends ErrorDescription> workerErrors = runHint(hd, c);

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

    public Map<String, Long> getTimeLog() {
        return timeLog;
    }

    private final class ScannerImpl extends CancellableTreePathScanner<Void, List<ErrorDescription>> {

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
                OUTER: for (HintDescription hd : rules) {
                    if (isCanceled()) {
                        return ;
                    }

                    HintMetadata hm = hd.getMetadata();

                    for (String wname : hm.suppressWarnings) {
                        if( !suppresWarnings.empty() && suppresWarnings.peek().contains(wname)) {
                            continue OUTER;
                        }
                    }

                    HintContext c = new HintContext(info, hm, path, Collections.<String, TreePath>emptyMap(), Collections.<String, Collection<? extends TreePath>>emptyMap(), Collections.<String, String>emptyMap());
                    Collection<? extends ErrorDescription> errors = runHint(hd, c);

                    if (errors != null) {
                        d.addAll(errors);
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

            boolean b = pushSuppressWarrnings(tp);
            try {
                runAndAdd(tp, hints.get(k), p);

                if (isCanceled()) {
                    return null;
                }

                return super.scan(tree, p);
            } finally {
                if (b) {
                    suppresWarnings.pop();
                }
            }
        }

        @Override
        public Void scan(TreePath path, List<ErrorDescription> p) {
            Kind k = path.getLeaf().getKind();
            boolean b = pushSuppressWarrnings(path);
            try {
                runAndAdd(path, hints.get(k), p);

                if (isCanceled()) {
                    return null;
                }

                return super.scan(path, p);
            } finally {
                if (b) {
                    suppresWarnings.pop();
                }
            }
        }

        public void scanDoNotGoDeeper(TreePath path, List<ErrorDescription> p) {
            Kind k = path.getLeaf().getKind();
            runAndAdd(path, hints.get(k), p);
        }

        private boolean pushSuppressWarrnings(TreePath path) {
            switch(path.getLeaf().getKind()) {
                case CLASS:
                case METHOD:
                case VARIABLE:
                    Set<String> current = suppresWarnings.size() == 0 ? null : suppresWarnings.peek();
                    Set<String> nju = current == null ? new HashSet<String>() : new HashSet<String>(current);

                    Element e = getTrees().getElement(path);

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
                    return true;
            }
            return false;
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

    private Collection<? extends ErrorDescription> runHint(HintDescription hd, HintContext ctx) {
        long start = System.nanoTime();

        try {
            return hd.getWorker().createErrors(ctx);
        } finally {
            long end = System.nanoTime();
            reportSpentTime(hd.getMetadata().id, end - start);
        }
    }

    private static final boolean logTimeSpentInHints = Boolean.getBoolean("java.HintsInvoker.time.in.hints");
    private final Map<String, Long> hint2SpentTime = new HashMap<String, Long>();

    private void reportSpentTime(String id, long nanoTime) {
        if (!logTimeSpentInHints) return;
        
        Long prev = hint2SpentTime.get(id);

        if (prev == null) {
            prev = (long) 0;
        }

        hint2SpentTime.put(id, prev + nanoTime);
    }

    private void dumpTimeSpentInHints() {
        if (!logTimeSpentInHints) return;

        List<Entry<String, Long>> l = new ArrayList<Entry<String, Long>>(hint2SpentTime.entrySet());

        Collections.sort(l, new Comparator<Entry<String, Long>>() {
            @Override
            public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                return (int) Math.signum(o1.getValue() - o2.getValue());
            }
        });

        for (Entry<String, Long> e : l) {
            System.err.println(e.getKey() + "=" + String.format("%3.2f", e.getValue() / 1000000.0));
        }
    }
}
