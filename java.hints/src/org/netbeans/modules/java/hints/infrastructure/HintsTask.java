/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.hints.infrastructure;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.modules.java.hints.options.HintsSettings;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.TreeRule;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class HintsTask implements CancellableTask<CompilationInfo> {
    
    private AtomicBoolean cancel = new AtomicBoolean();
    private List<ErrorDescription> currentHints = new LinkedList<ErrorDescription>();
    
    private Map<Kind, List<TreeRule>> presetHints;
    
    public HintsTask() {}

    public HintsTask(List<TreeRule> hints) {
        presetHints = new  EnumMap<Kind, List<TreeRule>>(Kind.class);
        
        for (TreeRule r : hints) {
            for (Kind k : r.getTreeKinds()) {
                List<TreeRule> rules = presetHints.get(k);
                
                if (rules == null) {
                    presetHints.put(k, rules = new  LinkedList<TreeRule>());
                }
                
                rules.add(r);
            }
        }
    }
    
    public List<ErrorDescription> computeHints(CompilationInfo info) {
        return computeHints(info, new TreePath(info.getCompilationUnit()));
    }
    
    public List<ErrorDescription> computeHints(CompilationInfo info, TreePath startAt) {
        Map<Kind, List<TreeRule>> hints = presetHints != null ? presetHints : RulesManager.getInstance().getHints(false);
        
        if (hints.isEmpty()) {
            return Collections.<ErrorDescription>emptyList();
        }
        
        List<ErrorDescription> errors = new  LinkedList<ErrorDescription>();
        
        new ScannerImpl(info, cancel, hints).scan(startAt, errors);
        
        return errors;
    }
    
    public void run(CompilationInfo info) throws Exception {
        cancel.set(false);
        
        long startTime = System.currentTimeMillis();
        
        TreePath changedMethod = info.getChangedTree();
        
        if (changedMethod != null) {
            MethodTree method = (MethodTree) changedMethod.getLeaf();
            BlockTree block = method.getBody();
            int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), block);
            int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), block);
            List<ErrorDescription> errors = new LinkedList<ErrorDescription>(currentHints);

            for (Iterator<ErrorDescription> it = errors.iterator(); it.hasNext();) {
                ErrorDescription ed = it.next();
                int edStart = ed.getRange().getBegin().getOffset();
                int edEnd = ed.getRange().getEnd().getOffset();

                if (edStart >= start && edEnd <= end) {
                    it.remove();
                }
            }

            errors = computeHints(info, changedMethod);

            if (cancel.get()) {
                return;
            }
            
            currentHints = errors;

            HintsController.setErrors(info.getFileObject(), HintsTask.class.getName(), errors);
        } else {
            List<ErrorDescription> result = computeHints(info);

            if (cancel.get()) {
                return;
            }

            currentHints = result;

            HintsController.setErrors(info.getFileObject(), HintsTask.class.getName(), result);
        }
        
        long endTime = System.currentTimeMillis();
        
        Logger.getLogger("TIMER").log(Level.FINE, "HintsTask", new Object[] {info.getFileObject(), endTime - startTime});
    }
    
    public void cancel() {
        cancel.set(true);
    }
    
    private static final class ScannerImpl extends CancellableTreePathScanner<Void, List<ErrorDescription>> {
        
        private Stack<Set<String>> suppresWarnings = new Stack<Set<String>>();
        private CompilationInfo info;
        private Map<Kind, List<TreeRule>> hints;
        
        public ScannerImpl(CompilationInfo info, AtomicBoolean cancel, Map<Kind, List<TreeRule>> hints) {
            super(cancel);
            this.info = info;
            this.hints = hints;
        }
        
        private void runAndAdd(TreePath path, List<TreeRule> rules, List<ErrorDescription> d) {
            if (rules != null && !isInGuarded(info, path)) {
                for (TreeRule tr : rules) {
                    if (isCanceled()) {
                        return ;
                    }
                    
                    boolean enabled = true;
                    String[] suppressedBy = null;
                    
                    if (tr instanceof AbstractHint) {
                        enabled = HintsSettings.isEnabled((AbstractHint)tr);
                        suppressedBy = HintsSettings.getSuppressedBy((AbstractHint)tr);
                    }
                    
                    if ( suppressedBy != null && suppressedBy.length != 0 ) {
                        for (String wname : suppressedBy) {
                            if( !suppresWarnings.empty() && suppresWarnings.peek().contains(wname)) {
                                return;
                            }
                        }
                    }
                    
                    if (enabled) {
                        List<ErrorDescription> errors = tr.run(info, path);
                        
                        if (errors != null) {
                            d.addAll(errors);
                        }
                    }
                }
            }
        }

        private static final Set<Tree.Kind> SUPPRESS_ELEMENTS = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);

        @Override
        public Void scan(Tree tree, List<ErrorDescription> p) {
            if (tree == null)
                return null;
            
            TreePath tp = new TreePath(getCurrentPath(), tree);
            Kind k = tree.getKind();

            if(SUPPRESS_ELEMENTS.contains(k)) {
                pushSuppressWarrnings(tp);
                runAndAdd(tp, hints.get(k), p);
                suppresWarnings.pop();
            } else {
                runAndAdd(tp, hints.get(k), p);
            }
            
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
            pushSuppressWarrnings(getCurrentPath());
            Void r = super.visitMethod(tree, arg1);
            suppresWarnings.pop();
            return r;
        }

        @Override
        public Void visitClass(ClassTree tree, List<ErrorDescription> arg1) {
            pushSuppressWarrnings(getCurrentPath());
            Void r = super.visitClass(tree, arg1);
            suppresWarnings.pop();
            return r;
        }

        @Override
        public Void visitVariable(VariableTree tree, List<ErrorDescription> arg1) {
            pushSuppressWarrnings(getCurrentPath());
            Void r = super.visitVariable(tree, arg1);
            suppresWarnings.pop();
            return r;
        }
        
        private void pushSuppressWarrnings(TreePath tp) {
            Set<String> current = suppresWarnings.size() == 0 ? null : suppresWarnings.peek();
            Set<String> nju = current == null ? new HashSet<String>() : new HashSet<String>(current);
            
            Element e = info.getTrees().getElement(tp);
            
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
    }

    static boolean isInGuarded(CompilationInfo info, TreePath tree) {
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
    
}
