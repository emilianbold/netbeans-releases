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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.jackpot.spi.JavaFix;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class StaticAccess extends AbstractHint {
    
    private static final String SUPPRESS_WARNINGS_KEY = "static-access";
    
    private transient volatile boolean stop;
    /** Creates a new instance of StaticAccess */
    public StaticAccess() {
        super( true, true, AbstractHint.HintSeverity.WARNING, SUPPRESS_WARNINGS_KEY);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.MEMBER_SELECT);
    }

    protected List<Fix> computeFixes(CompilationInfo info, TreePath treePath, int[] bounds, int[] kind, String[] simpleName) {
        if (treePath.getLeaf().getKind() != Kind.MEMBER_SELECT) {
            return null;
        }
        MemberSelectTree mst = (MemberSelectTree)treePath.getLeaf();
        Tree expression = mst.getExpression();
        TreePath expr = new TreePath(treePath, expression);
        
        TypeMirror tm = info.getTrees().getTypeMirror(expr);
        if (tm == null) {
            return null;
        }
        Element el = info.getTypes().asElement(tm);
        if (el == null || el.getKind() != ElementKind.CLASS) {
            return null;
        }
        
        TypeElement type = (TypeElement)el;
        
        if (isError(type)) {
            return null;
        }
        
        Name idName = null;
        
        if (expression.getKind() == Kind.MEMBER_SELECT) {
            MemberSelectTree exprSelect = (MemberSelectTree)expression;
            idName = exprSelect.getIdentifier();
        }
        
        if (expression.getKind() == Kind.IDENTIFIER) {
            IdentifierTree idt = (IdentifierTree)expression;
            idName = idt.getName();
        }
        
        if (idName != null) {
            if (idName.equals(type.getSimpleName())) {
                return null;
            }
            if (idName.equals(type.getQualifiedName())) {
                return null;
            }
        }
        
        Element used = info.getTrees().getElement(treePath);
        
        if (used == null || !used.getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        
        if (isError(used)) {
            return null;
        }
        
        int[] span = {
            (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), expression),
            (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), expression),
        };
        
        if (span[0] == (-1) || span[1] == (-1)) {
            return null;
        }
        
        if (used.getKind().isField()) {
            kind[0] = 0;
        } else {
            if (used.getKind() == ElementKind.METHOD) {
                kind[0] = 1;
            } else {
                kind[0] = 2;
            }
        }
        
        simpleName[0] = used.getSimpleName().toString();
        
        List<Fix> fixes = new ArrayList<Fix>(2);
        fixes.add(JavaFix.toEditorFix(new FixImpl(info, expr, type)));
        fixes.addAll(FixFactory.createSuppressWarnings(info, treePath, SUPPRESS_WARNINGS_KEY));


        bounds[0] = span[0];
        bounds[1] = span[1];
        return fixes;
    }
    
    private boolean isError(Element e) {
        if (e == null) {
            return true;
        }
        
        if (e.getKind() != ElementKind.CLASS) {
            return false;
        }
        
        TypeMirror type = ((TypeElement) e).asType();
        
        return type == null || type.getKind() == TypeKind.ERROR;
    }
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        stop = false;
        int[] span = new int[2];
        int[] kind = new int[1];
        String[] simpleName = new String[1];
        List<Fix> fixes = computeFixes(compilationInfo, treePath, span, kind, simpleName);
        if (fixes == null) {
            return null;
        }

        ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
            getSeverity().toEditorSeverity(),
            NbBundle.getMessage(StaticAccess.class, "MSG_StaticAccess", kind[0], simpleName[0]), // NOI18N
            fixes,
            compilationInfo.getFileObject(),
            span[0],
            span[1] // NOI18N
        );

        return Collections.singletonList(ed);
    }

    public String getId() {
        return getClass().getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(DoubleCheck.class, "MSG_StaticAccessName"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(DoubleCheck.class, "HINT_StaticAccess"); // NOI18N
    }

    public void cancel() {
        stop = true;
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    

    static final class FixImpl extends JavaFix {
        private final ElementHandle<TypeElement> desiredType;
        public FixImpl(CompilationInfo info, TreePath expr, TypeElement desiredType) {
            super(info, expr);
            this.desiredType = ElementHandle.create(desiredType);
        }
        
        public String getText() {
            return NbBundle.getMessage(DoubleCheck.class, "MSG_StaticAccessText"); // NOI18N
        }
        
        @Override
        protected void performRewrite(WorkingCopy wc, TreePath tp, UpgradeUICallback callback) {
            Element element = desiredType.resolve(wc);

            if (element == null) {
                Logger.getLogger("org.netbeans.modules.java.hints").log(Level.INFO, "Cannot resolve target element.");
                return;
            }

            ExpressionTree idt = wc.getTreeMaker().QualIdent(element);
            wc.rewrite(tp.getLeaf(), idt);
        }
    }
    
}
