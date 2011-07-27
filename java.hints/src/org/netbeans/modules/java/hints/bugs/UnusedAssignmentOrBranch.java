/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.modules.java.hints.jackpot.code.spi.Hint;
import org.netbeans.modules.java.hints.jackpot.code.spi.TriggerTreeKind;
import org.netbeans.modules.java.hints.jackpot.impl.RulesManager;
import org.netbeans.modules.java.hints.jackpot.spi.HintContext;
import org.netbeans.modules.java.hints.jackpot.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class UnusedAssignmentOrBranch implements CancellableTask<CompilationInfo> {
    
    private static final String UNUSED_ASSIGNMENT_ID = "org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.unusedAssignment";
    private static final String DEAD_BRANCH_ID = "org.netbeans.modules.java.hints.bugs.UnusedAssignmentOrBranch.deadBranch";

    private final AtomicBoolean cancel = new AtomicBoolean();
    
    public void run(final CompilationInfo info) throws Exception {
        cancel.set(false);

        Document doc = info.getDocument();

        if (doc == null) return ;

        OffsetsBag unusedValue = compute(info, doc, cancel);

        if (unusedValue == null || cancel.get()) return;
        
        getBag(doc).setHighlights(unusedValue);
    }

    static OffsetsBag compute(final CompilationInfo info, Document doc, AtomicBoolean cancel) {
        final OffsetsBag unusedValue = new OffsetsBag(doc);
        boolean computeUnusedAssignments = isEnabled(UNUSED_ASSIGNMENT_ID);
        boolean computeDeadBranch = isEnabled(DEAD_BRANCH_ID);

        if (!computeUnusedAssignments && !computeDeadBranch) return unusedValue;
        
        FlowResult flow = Flow.assignmentsForUse(info, cancel);

        if (flow == null || cancel.get()) return null;

        FontColorSettings fcs = MimeLookup.getLookup("text/x-java").lookup(FontColorSettings.class);
        AttributeSet unusedCode = fcs == null ? AttributesUtilities.createImmutable() : fcs.getTokenFontColors("unusedCode"); // NOI18N
        String unusedAssignmentLabel = NbBundle.getMessage(UnusedAssignmentOrBranch.class, "LBL_UNUSED_ASSIGNMENT_LABEL");
        String deadBranchLabel = NbBundle.getMessage(UnusedAssignmentOrBranch.class, "LBL_DEAD_BRANCH");
        final AttributeSet unusedAssignment = AttributesUtilities.createComposite(unusedCode, AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, unusedAssignmentLabel));
        final AttributeSet deadBranch = AttributesUtilities.createComposite(unusedCode, AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, deadBranchLabel));

        if (computeUnusedAssignments) {
            final Set<Tree> usedAssignments = new HashSet<Tree>();

            for (Iterable<? extends TreePath> i : flow.getAssignmentsForUse().values()) {
                for (TreePath tp : i) {
                    if (tp == null) continue;

                    usedAssignments.add(tp.getLeaf());
                }
            }

            final Set<Element> usedVariables = new HashSet<Element>();

            new CancellableTreePathScanner<Void, Void>(cancel) {
                @Override public Void visitAssignment(AssignmentTree node, Void p) {
                    Element var = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

                    if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(node.getExpression())) {
                        scan(node.getExpression(), null);
                        return null;
                    }

                    return super.visitAssignment(node, p);
                }
                @Override public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
                    Element var = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

                    if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(node.getExpression())) {
                        scan(node.getExpression(), null);
                        return null;
                    }

                    return super.visitCompoundAssignment(node, p);
                }
                @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                    Element var = info.getTrees().getElement(getCurrentPath());

                    if (var != null && LOCAL_VARIABLES.contains(var.getKind())) {
                        usedVariables.add(var);
                    }
                    return super.visitIdentifier(node, p);
                }
            }.scan(info.getCompilationUnit(), null);

            new CancellableTreePathScanner<Void, Void>(cancel) {
                @Override public Void visitAssignment(AssignmentTree node, Void p) {
                    Element var = info.getTrees().getElement(new TreePath(getCurrentPath(), node.getVariable()));

                    if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && !usedAssignments.contains(node.getExpression()) && usedVariables.contains(var)) {
                        unusedValue(node.getExpression());
                    }
                    return super.visitAssignment(node, p);
                }
                @Override public Void visitVariable(VariableTree node, Void p) {
                    Element var = info.getTrees().getElement(getCurrentPath());

                    if (var != null && LOCAL_VARIABLES.contains(var.getKind()) && node.getInitializer() != null && !usedAssignments.contains(node.getInitializer()) && usedVariables.contains(var)) {
                        unusedValue(node.getInitializer());
                    }
                    return super.visitVariable(node, p);
                }
                private void unusedValue(Tree t) {
                    int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t);
                    int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t);

                    if (start < 0 || end < 0) return;

                    unusedValue.addHighlight(start, end, unusedAssignment);
                }
            }.scan(info.getCompilationUnit(), null);
        }

        if (computeDeadBranch) {
            for (Tree t : flow.getDeadBranches()) {
                int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t);
                int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), t);

                if (start < 0 || end < 0) continue;

                unusedValue.addHighlight(start, end, deadBranch);
            }
        }

        return unusedValue;
    }

    public void cancel() {
        cancel.set(true);
    }

    private static final Set<ElementKind> LOCAL_VARIABLES = EnumSet.of(ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);

    private static OffsetsBag getBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(UnusedAssignmentOrBranch.class);

        if (bag == null) {
            doc.putProperty(UnusedAssignmentOrBranch.class, bag = new OffsetsBag(doc));
        }

        return bag;
    }

    static final String ENABLED_KEY = "enabled";         // NOI18N
    private static boolean isEnabled(String id) {
        Preferences p = RulesManager.getPreferences(id, HintsSettings.getCurrentProfileId());
        return p.getBoolean(ENABLED_KEY, true);
    }

    @Hint(category="bugs", id=UNUSED_ASSIGNMENT_ID, options={Options.NO_BATCH, Options.QUERY})
    @TriggerTreeKind(Tree.Kind.COMPILATION_UNIT)
    public static ErrorDescription unusedAssignment(HintContext ctx) {
        return null;
    }

    @Hint(category="bugs", id=DEAD_BRANCH_ID, options={Options.NO_BATCH, Options.QUERY})
    @TriggerTreeKind(Tree.Kind.COMPILATION_UNIT)
    public static ErrorDescription deadBranch(HintContext ctx) {
        return null;
    }

    @MimeRegistration(mimeType="text/x-java", service=HighlightsLayerFactory.class)
    public static final class HighlightsFactoryImpl implements HighlightsLayerFactory {

        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[] {
                HighlightsLayer.create(UnusedAssignmentOrBranch.class.getName(), ZOrder.CARET_RACK, true, getBag(context.getDocument()))
            };
        }

    }

    @ServiceProvider(service=JavaSourceTaskFactory.class)
    public static final class JavaFactoryImpl extends EditorAwareJavaSourceTaskFactory implements PreferenceChangeListener, LookupListener {

        public JavaFactoryImpl() {
            super(Phase.RESOLVED, Priority.LOW);
        }

        private Result<FontColorSettings> fcsResult;
        private boolean initialized;

        private synchronized void initialize() {
            if (initialized) return;

            initialized = true;

            Preferences unusedAssignmentPrefs = RulesManager.getPreferences(UNUSED_ASSIGNMENT_ID, HintsSettings.getCurrentProfileId());
            unusedAssignmentPrefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, unusedAssignmentPrefs));

            Preferences deadBranchPrefs = RulesManager.getPreferences(DEAD_BRANCH_ID, HintsSettings.getCurrentProfileId());
            deadBranchPrefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, deadBranchPrefs));

            //cannot run in the constructor - would lead to a deadlock:
            fcsResult = MimeLookup.getLookup("text/x-java").lookupResult(FontColorSettings.class);
            fcsResult.addLookupListener(WeakListeners.create(LookupListener.class, this, fcsResult));
            fcsResult.allItems();
        }

        @Override
        protected CancellableTask<CompilationInfo> createTask(FileObject file) {
            initialize();
            return new UnusedAssignmentOrBranch();
        }
        
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            refresh();
        }

        private void refresh() throws IllegalArgumentException {
            for (FileObject f : getFileObjects()) {
                reschedule(f);
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            refresh();
        }
    }
}
