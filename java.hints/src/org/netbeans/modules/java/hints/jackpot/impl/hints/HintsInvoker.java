/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.impl.hints;

import com.sun.source.tree.Tree;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.introduce.CopyFinder;
import org.netbeans.modules.java.hints.introduce.CopyFinder.VariableAssignments;
import org.netbeans.modules.java.hints.jackpot.impl.MessageImpl;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.impl.Utilities;
import org.netbeans.modules.java.hints.jackpot.impl.pm.BulkSearch;
import org.netbeans.modules.java.hints.jackpot.impl.pm.BulkSearch.BulkPattern;
import org.netbeans.modules.java.hints.jackpot.impl.pm.Pattern;
import org.netbeans.modules.java.hints.jackpot.spi.Hacks;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata;
import org.netbeans.modules.java.hints.jackpot.spi.Trigger;
import org.netbeans.modules.java.hints.jackpot.spi.Trigger.Kinds;
import org.netbeans.modules.java.hints.jackpot.spi.Trigger.PatternDescription;
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
                if (m.kind == HintMetadata.Kind.SUGGESTION) {
                    descs.addAll(e.getValue());
                } else {
                    Preferences pref = RulesManager.getPreferences(m.id, HintsSettings.getCurrentProfileId());
                    if (HintsSettings.getSeverity(m, pref) == HintSeverity.CURRENT_LINE_WARNING) {
                        descs.addAll(e.getValue());
                    }
                }
            } else {
                if (m.kind == HintMetadata.Kind.HINT) {
                    Preferences pref = RulesManager.getPreferences(m.id, HintsSettings.getCurrentProfileId());
                    if (HintsSettings.getSeverity(m, pref) != HintSeverity.CURRENT_LINE_WARNING) {
                        descs.addAll(e.getValue());
                    }
                }
            }
        }

        long elementBasedStart = System.currentTimeMillis();

        RulesManager.computeElementBasedHintsXXX(info, cancel, descs);

        long elementBasedEnd = System.currentTimeMillis();

        timeLog.put("Computing Element Based Hints", elementBasedEnd - elementBasedStart);

        List<ErrorDescription> errors = join(computeHints(info, startAt, descs, new LinkedList<MessageImpl>()));

        dumpTimeSpentInHints();
        
        return errors;
    }

    public List<ErrorDescription> computeHints(CompilationInfo info,
                                               Iterable<? extends HintDescription> hints) {
        return computeHints(info, hints, new LinkedList<MessageImpl>());
    }

    public List<ErrorDescription> computeHints(CompilationInfo info,
                                               Iterable<? extends HintDescription> hints,
                                               Collection<? super MessageImpl> problems) {
        return join(computeHints(info, new TreePath(info.getCompilationUnit()), hints, problems));
    }

    public Map<HintDescription, List<ErrorDescription>> computeHints(CompilationInfo info,
                                        TreePath startAt,
                                        Iterable<? extends HintDescription> hints,
                                        Collection<? super MessageImpl> problems) {
        Map<Class, List<HintDescription>> triggerKind2Hints = new HashMap<Class, List<HintDescription>>();

        for (Class<? extends Trigger> c : Trigger.TRIGGER_KINDS) {
            triggerKind2Hints.put(c, new ArrayList<HintDescription>());
        }

        for (HintDescription hd : hints) {
            List<HintDescription> sorted = triggerKind2Hints.get(hd.getTrigger().getClass());

            sorted.add(hd);
        }
        
        if (caret != -1) {
            TreePath tp = info.getTreeUtilities().pathFor(caret);
            return computeSuggestions(info, tp, triggerKind2Hints, problems);
        } else {
            if (from != (-1) && to != (-1)) {
                return computeHintsInSpan(info, triggerKind2Hints, problems);
            } else {
                return computeHintsImpl(info, startAt, triggerKind2Hints, problems);
            }
        }
    }

    private Map<HintDescription, List<ErrorDescription>> computeHintsImpl(CompilationInfo info,
                                        TreePath startAt,
                                        Map<Class, List<HintDescription>> triggerKind2Hints,
                                        Collection<? super MessageImpl> problems) {
        Map<HintDescription, List<ErrorDescription>> errors = new HashMap<HintDescription, List<ErrorDescription>>();
        List<HintDescription> kindBasedHints = triggerKind2Hints.get(Kinds.class);

        timeLog.put("[C] Kind Based Hints", (long) kindBasedHints.size());

        if (!kindBasedHints.isEmpty()) {
            long kindStart = System.currentTimeMillis();

            new ScannerImpl(info, cancel, sortByKinds(kindBasedHints)).scan(startAt, errors);

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Hints", kindEnd - kindStart);
        }

        List<HintDescription> patternBasedHints = triggerKind2Hints.get(PatternDescription.class);

        timeLog.put("[C] Pattern Based Hints", (long) patternBasedHints.size());

        long patternStart = System.currentTimeMillis();

        Map<PatternDescription, List<HintDescription>> patternHints = sortByPatterns(patternBasedHints);
        Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

        long bulkPatternStart = System.currentTimeMillis();

        BulkPattern bulkPattern = BulkSearch.getDefault().create(info, patternTests.keySet());

        long bulkPatternEnd = System.currentTimeMillis();

        timeLog.put("Bulk Pattern preparation", bulkPatternEnd - bulkPatternStart);

        long bulkStart = System.currentTimeMillis();

        Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, startAt, bulkPattern, timeLog);

        long bulkEnd = System.currentTimeMillis();

        timeLog.put("Bulk Search", bulkEnd - bulkStart);

        mergeAll(errors, doComputeHints(info, occurringPatterns, patternTests, patternHints, problems));

        long patternEnd = System.currentTimeMillis();

        timeLog.put("Pattern Based Hints", patternEnd - patternStart);

        return errors;
    }

    private Map<HintDescription, List<ErrorDescription>> computeHintsInSpan(CompilationInfo info,
                                        Map<Class, List<HintDescription>> triggerKind2Hints,
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

        Map<HintDescription, List<ErrorDescription>> errors = new HashMap<HintDescription, List<ErrorDescription>>();
        List<HintDescription> kindBasedHints = triggerKind2Hints.get(Kinds.class);

        if (!kindBasedHints.isEmpty()) {
            long kindStart = System.currentTimeMillis();

            new ScannerImpl(info, cancel, sortByKinds(kindBasedHints)).scan(path, errors);

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Hints", kindEnd - kindStart);
        }

        List<HintDescription> patternBasedHints = triggerKind2Hints.get(PatternDescription.class);

        if (!patternBasedHints.isEmpty()) {
            long patternStart = System.currentTimeMillis();

            Map<PatternDescription, List<HintDescription>> patternHints = sortByPatterns(patternBasedHints);
            Map<String, List<PatternDescription>> patternTests = computePatternTests(patternHints);

            long bulkStart = System.currentTimeMillis();

            BulkPattern bulkPattern = BulkSearch.getDefault().create(info, patternTests.keySet());
            Map<String, Collection<TreePath>> occurringPatterns = BulkSearch.getDefault().match(info, path, bulkPattern, timeLog);

            long bulkEnd = System.currentTimeMillis();

            timeLog.put("Bulk Search", bulkEnd - bulkStart);

            mergeAll(errors, doComputeHints(info, occurringPatterns, patternTests, patternHints, problems));

            long patternEnd = System.currentTimeMillis();

            timeLog.put("Pattern Based Hints", patternEnd - patternStart);
        }

        if (path != null) {
            mergeAll(errors, computeSuggestions(info, path, triggerKind2Hints, problems));
        }

        return errors;
    }

    private Map<HintDescription, List<ErrorDescription>> computeSuggestions(CompilationInfo info,
                                        TreePath workOn,
                                        Map<Class, List<HintDescription>> triggerKind2Hints,
                                        Collection<? super MessageImpl> problems) {
        Map<HintDescription, List<ErrorDescription>> errors = new HashMap<HintDescription, List<ErrorDescription>>();
        List<HintDescription> kindBasedHints = triggerKind2Hints.get(Kinds.class);

        if (!kindBasedHints.isEmpty()) {
            long kindStart = System.currentTimeMillis();
            
            Map<Kind, List<HintDescription>> hints = sortByKinds(kindBasedHints);
            TreePath proc = workOn;

            while (proc != null) {
                new ScannerImpl(info, cancel, hints).scanDoNotGoDeeper(proc, errors);
                proc = proc.getParentPath();
            }

            long kindEnd = System.currentTimeMillis();

            timeLog.put("Kind Based Suggestions", kindEnd - kindStart);
        }

        List<HintDescription> patternBasedHints = triggerKind2Hints.get(PatternDescription.class);

        if (!patternBasedHints.isEmpty()) {
            long patternStart = System.currentTimeMillis();

            Map<PatternDescription, List<HintDescription>> patternHints = sortByPatterns(patternBasedHints);
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

            mergeAll(errors, doComputeHints(info, occurringPatterns, patternTests, patternHints, problems));

            long patternEnd = System.currentTimeMillis();

            timeLog.put("Pattern Based Hints", patternEnd - patternStart);
        }

        return errors;
    }

    public Map<HintDescription, List<ErrorDescription>> doComputeHints(CompilationInfo info, Map<String, Collection<TreePath>> occurringPatterns, Map<String, List<PatternDescription>> patterns, Map<PatternDescription, List<HintDescription>> patternHints) throws IllegalStateException {
        return doComputeHints(info, occurringPatterns, patterns, patternHints, new LinkedList<MessageImpl>());
    }

    private static Map<Kind, List<HintDescription>> sortByKinds(List<HintDescription> kindBasedHints) {
        Map<Kind, List<HintDescription>> result = new EnumMap<Kind, List<HintDescription>>(Kind.class);

        for (HintDescription hd : kindBasedHints) {
            for (Kind k : ((Kinds) hd.getTrigger()).getKinds()) {
                List<HintDescription> hints = result.get(k);

                if (hints == null) {
                    result.put(k, hints = new ArrayList<HintDescription>());
                }

                hints.add(hd);
            }
        }

        return result;
    }

    private static Map<PatternDescription, List<HintDescription>> sortByPatterns(List<HintDescription> kindBasedHints) {
        Map<PatternDescription, List<HintDescription>> result = new HashMap<PatternDescription, List<HintDescription>>();

        for (HintDescription hd : kindBasedHints) {
            List<HintDescription> hints = result.get((PatternDescription) hd.getTrigger());

            if (hints == null) {
                result.put((PatternDescription) hd.getTrigger(), hints = new ArrayList<HintDescription>());
            }

            hints.add(hd);
        }

        return result;
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

    private Map<HintDescription, List<ErrorDescription>> doComputeHints(CompilationInfo info, Map<String, Collection<TreePath>> occurringPatterns, Map<String, List<PatternDescription>> patterns, Map<PatternDescription, List<HintDescription>> patternHints, Collection<? super MessageImpl> problems) throws IllegalStateException {
        Map<HintDescription, List<ErrorDescription>> errors = new HashMap<HintDescription, List<ErrorDescription>>();

        for (Entry<String, Collection<TreePath>> occ : occurringPatterns.entrySet()) {
            PATTERN_LOOP: for (PatternDescription d : patterns.get(occ.getKey())) {
                Map<String, TypeMirror> constraints = new HashMap<String, TypeMirror>();

                for (Entry<String, String> e : d.getConstraints().entrySet()) {
                    TypeMirror designedType = Hacks.parseFQNType(info, e.getValue());

                    if (designedType == null || designedType.getKind() == TypeKind.ERROR) {
                        //will not bind to anything anyway (#190449), skip pattern:
                        continue PATTERN_LOOP;
                    }

                    constraints.put(e.getKey(), designedType);
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
                        HintContext c = new HintContext(info, hm, candidate, verified.variables, verified.multiVariables, verified.variables2Names, constraints, problems);

                        if (!Collections.disjoint(suppressedWarnings, hm.suppressWarnings))
                            continue;

                        Collection<? extends ErrorDescription> workerErrors = runHint(hd, c);

                        if (workerErrors != null) {
                            merge(errors, hd, workerErrors);
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

    private final class ScannerImpl extends CancellableTreePathScanner<Void, Map<HintDescription, List<ErrorDescription>>> {

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

        private void runAndAdd(TreePath path, List<HintDescription> rules, Map<HintDescription, List<ErrorDescription>> d) {
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
                        merge(d, hd, errors);
                    }
                }
            }
        }

        @Override
        public Void scan(Tree tree, Map<HintDescription, List<ErrorDescription>> p) {
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
        public Void scan(TreePath path, Map<HintDescription, List<ErrorDescription>> p) {
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

        public void scanDoNotGoDeeper(TreePath path, Map<HintDescription, List<ErrorDescription>> p) {
            Kind k = path.getLeaf().getKind();
            runAndAdd(path, hints.get(k), p);
        }

        private boolean pushSuppressWarrnings(TreePath path) {
            switch(path.getLeaf().getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
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

    public static <K, V> Map<K, List<V>> merge(Map<K, List<V>> to, K key, Collection<? extends V> value) {
        List<V> toColl = to.get(key);

        if (toColl == null) {
            to.put(key, toColl = new LinkedList<V>());
        }

        toColl.addAll(value);

        return to;
    }

    public static <K, V> Map<K, List<V>> mergeAll(Map<K, List<V>> to, Map<? extends K, ? extends Collection<? extends V>> what) {
        for (Entry<? extends K, ? extends Collection<? extends V>> e : what.entrySet()) {
            List<V> toColl = to.get(e.getKey());

            if (toColl == null) {
                to.put(e.getKey(), toColl = new LinkedList<V>());
            }

            toColl.addAll(e.getValue());
        }

        return to;
    }

    public static List<ErrorDescription> join(Map<?, ? extends List<? extends ErrorDescription>> errors) {
        List<ErrorDescription> result = new LinkedList<ErrorDescription>();

        for (Entry<?, ? extends Collection<? extends ErrorDescription>> e : errors.entrySet()) {
            result.addAll(e.getValue());
        }

        return result;
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
