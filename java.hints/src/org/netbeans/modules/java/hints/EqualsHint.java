/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsHint extends AbstractHint {

    static final String KEY_DELEGATE = "delegate"; //NOI18N
    static final String KEY_ARRAY_EQUALS = "EqualsArray"; //NOI18N
    static final String KEY_INCOMPATIBLE_EQUALS = "EqualsIncompabile"; //NOI18N
    private static final String ERASURE_PREFS_KEY = "eguals-hint-erasure"; // NOI18N
    
    private static EqualsHint DELEGATE;
    private static EqualsHint ARRAY_EQUALS;
    private static EqualsHint INCOMPABILE_EQUALS;
    
    private String kind;
    
    public EqualsHint(String kind) {
        super(true, false, HintSeverity.WARNING, "array-equals");
        this.kind = kind;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(EqualsHint.class, "DSC_" + kind);
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD_INVOCATION);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        if (kind != KEY_DELEGATE || (!getArrayEquals().isEnabled() && !getIncompatibleEquals().isEnabled())) return null;
        
        if (!getTreeKinds().contains(treePath.getLeaf().getKind()))
            return null;
        
        MethodInvocationTree mit = (MethodInvocationTree) treePath.getLeaf();
        
        if (mit.getMethodSelect().getKind() != Kind.MEMBER_SELECT) {
            return null;
        }
        
        Name n = ((MemberSelectTree) mit.getMethodSelect()).getIdentifier();
        
        if (!n.contentEquals("equals")) { //NOI18N
            return null;
        }
        
        TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object"); //NOI18N
        
        if (jlObject == null) {
            return null;
        }
        
        ExecutableElement equals = null;
        
        for (ExecutableElement m : ElementFilter.methodsIn(jlObject.getEnclosedElements())) {
            if (m.getSimpleName().contentEquals("equals") && m.getParameters().size() == 1 && m.getParameters().get(0).asType().getKind() == TypeKind.DECLARED) { //NOI18N
                equals = m;
                break;
            }
        }
        
        if (equals == null) {
            return null;
        }
        
        Element msEl = info.getTrees().getElement(treePath);
        
        if (msEl == null || msEl.getKind() != ElementKind.METHOD) {
            return null;
        }
        
        //the called method needs to override Object.equals:
        
        if (!equals.equals(msEl) && !info.getElements().overrides((ExecutableElement) msEl, equals, (TypeElement) msEl.getEnclosingElement())) {
            return null;
        }
        
        List<ErrorDescription> errors = new LinkedList<ErrorDescription>();
        TypeMirror invokedOn = info.getTrees().getTypeMirror(new TreePath(new TreePath(treePath, mit.getMethodSelect()), ((MemberSelectTree) mit.getMethodSelect()).getExpression()));
                
        if (getArrayEquals().isEnabled()) {

            if (invokedOn != null && invokedOn.getKind() == TypeKind.ARRAY) {
                int[] nameSpan = info.getTreeUtilities().findNameSpan((MemberSelectTree) mit.getMethodSelect());

                if (nameSpan != null) {
                    String displayName = NbBundle.getMessage(EqualsHint.class, "ERR_ARRAY_EQUALS");
                    List<Fix> fixes = Arrays.<Fix>asList(new ReplaceFixImpl(info.getJavaSource(), TreePathHandle.create(treePath, info), true),
                                                         new ReplaceFixImpl(info.getJavaSource(), TreePathHandle.create(treePath, info), false));

                    errors.add(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), displayName, fixes, info.getFileObject(), nameSpan[0], nameSpan[1]));
                }
            }
        }
        
        if (getIncompatibleEquals().isEnabled()) {
            TypeMirror parameter = info.getTrees().getTypeMirror(new TreePath(treePath, mit.getArguments().get(0)));
            if (getErasure(getPreferences(null))) {
                Types types = info.getTypes();
                parameter = types.erasure(parameter);
                invokedOn = types.erasure(invokedOn);
            }
            boolean castable = info.getTypeUtilities().isCastable(invokedOn, parameter) || info.getTypeUtilities().isCastable(parameter, invokedOn);

            if (!castable) {
                int[] nameSpan = info.getTreeUtilities().findNameSpan((MemberSelectTree) mit.getMethodSelect());

                if (nameSpan != null) {
                    String displayName = NbBundle.getMessage(EqualsHint.class, "ERR_INCOMPATIBLE_EQUALS"); // NOI18N

                    errors.add(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), displayName, info.getFileObject(), nameSpan[0], nameSpan[1]));
                }
            }
        }
        
        return errors;
    }

    @Override
    public JComponent getCustomizer(Preferences node) {
        return new EqualsHintCustomiser(node);
    }

    static void setErasure(Preferences p, boolean value) {
        p.putBoolean(ERASURE_PREFS_KEY, value);
    }

    static boolean getErasure(Preferences p) {
        return p.getBoolean(ERASURE_PREFS_KEY, true);
    }

    public String getId() {
        return EqualsHint.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(EqualsHint.class, "DN_" + kind); // NOI18N
    }

    public void cancel() {
    }
    
    public static synchronized EqualsHint getDelegate() {
        if (DELEGATE == null) {
            DELEGATE = new EqualsHint(KEY_DELEGATE);
        }
        
        return DELEGATE;
    }

    public static synchronized EqualsHint getArrayEquals() {
        if (ARRAY_EQUALS == null) {
            ARRAY_EQUALS = new EqualsHint(KEY_ARRAY_EQUALS);
        }
        
        return ARRAY_EQUALS;
    }
    
    public static synchronized EqualsHint getIncompatibleEquals() {
        if (INCOMPABILE_EQUALS == null) {
            INCOMPABILE_EQUALS = new EqualsHint(KEY_INCOMPATIBLE_EQUALS);
        }
        
        return INCOMPABILE_EQUALS;
    }
    
    static final class ReplaceFixImpl implements Fix {

        private JavaSource js;
        private TreePathHandle what;
        private boolean arraysEquals;

        public ReplaceFixImpl(JavaSource js, TreePathHandle what, boolean arraysEquals) {
            this.js = js;
            this.what = what;
            this.arraysEquals = arraysEquals;
        }
        
        public String getText() {
            return NbBundle.getMessage(EqualsHint.class, arraysEquals ? "FIX_ReplaceWithArraysEquals" : "FIX_ReplaceWithInstanceEquals");
        }

        public ChangeInfo implement() throws Exception {
            computeResult().commit();
            
            return null;
        }

        boolean isArraysEquals() {
            return arraysEquals;
        }

        private ModificationResult computeResult() throws Exception {
            return js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy parameter) throws Exception {
                    parameter.toPhase(Phase.PARSED);

                    TreePath tp = what.resolve(parameter);

                    if (tp == null) {
                        return; //XXX log
                    }

                    MethodInvocationTree mit = (MethodInvocationTree) tp.getLeaf();
                    MemberSelectTree method = (MemberSelectTree) mit.getMethodSelect();
                    TypeElement arrays = parameter.getElements().getTypeElement("java.util.Arrays"); //NOI18N

                    if (arrays == null) {
                        return; //probably can happen for broken platforms
                    }

                    Tree nue;

                    if (arraysEquals) {
                        ExpressionTree nueMethod = parameter.getTreeMaker().MemberSelect(parameter.getTreeMaker().QualIdent(arrays), "equals"); //NOI18N
                        List<ExpressionTree> params = new LinkedList<ExpressionTree>();

                        params.add(method.getExpression());
                        params.add(mit.getArguments().get(0));

                        nue = parameter.getTreeMaker().MethodInvocation(Collections.<ExpressionTree>emptyList(), nueMethod, params);
                    } else {
                        nue = parameter.getTreeMaker().Binary(Kind.EQUAL_TO, method.getExpression(), mit.getArguments().get(0));
                    }

                    parameter.rewrite(mit, nue);
                }
            });
        }
        
    }

}
