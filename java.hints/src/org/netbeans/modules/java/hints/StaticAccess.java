/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav tulach
 */
public class StaticAccess extends AbstractHint {
    private transient volatile boolean stop;
    /** Creates a new instance of AddOverrideAnnotation */
    public StaticAccess() {
        super( true, true, AbstractHint.HintSeverity.WARNING);
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.MEMBER_SELECT);
    }

    protected List<Fix> computeFixes(CompilationInfo info, TreePath treePath, Document doc, int[] bounds) {
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
        
        int[] span = {
            (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), expression),
            (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), expression),
        };
        
        if (span[0] == (-1) || span[1] == (-1)) {
            return null;
        }
        
        List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
            TreePathHandle.create(expr, info),
            TreePathHandle.create(type, info),
            info.getFileObject()
        ));


        bounds[0] = span[0];
        bounds[1] = span[1];
        return fixes;
    }
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        stop = false;
        try {
            Document doc = compilationInfo.getDocument();
            
            if (doc == null) {
                return null;
            }
        
            int[] span = new int[2];
            List<Fix> fixes = computeFixes(compilationInfo, treePath, doc, span);
            if (fixes == null) {
                return null;
            }

            ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                getSeverity().toEditorSeverity(),
                NbBundle.getMessage(StaticAccess.class, "MSG_StaticAccess"), // NOI18N
                fixes,
                doc,
                doc.createPosition(span[0]),
                doc.createPosition(span[1]) // NOI18N
            );

            return Collections.singletonList(ed);
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return null;
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

    static final class FixImpl implements Fix, Task<WorkingCopy> {
        private final TreePathHandle expr;
        private final TreePathHandle type;
        private final FileObject file;
        
        public FixImpl(TreePathHandle expr, TreePathHandle type, FileObject file) {
            this.file = file;
            this.type = type;
            this.expr = expr;
        }
        
        
        public String getText() {
            return NbBundle.getMessage(DoubleCheck.class, "MSG_StaticAccessText"); // NOI18N
        }
        
        public ChangeInfo implement() throws IOException {
            ModificationResult result = JavaSource.forFileObject(file).runModificationTask(this);
            result.commit();
            return null;
        }
        
        @Override public String toString() {
            return "FixStaticAccess"; // NOI18N
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath path = expr.resolve(copy);
            Element element = type.resolveElement(copy);
            ExpressionTree idt = copy.getTreeMaker().QualIdent(element);
            copy.rewrite(path.getLeaf(), idt);
        }
    }
    
}
