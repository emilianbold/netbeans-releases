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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.semantic.Utilities;
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
public class UtilityClass extends AbstractHint implements ElementVisitor<Boolean,CompilationInfo> {
    private boolean clazz;
    private transient volatile boolean stop;
    
    /** Creates a new instance of AddOverrideAnnotation */
    private UtilityClass(boolean b) {
        super( false, true, b ? AbstractHint.HintSeverity.WARNING : AbstractHint.HintSeverity.CURRENT_LINE_WARNING);
        clazz = b;
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(clazz ? Kind.CLASS : Kind.METHOD); 
    }
    
    public static UtilityClass withoutConstructor() {
        return new UtilityClass(true);
    }
    public static UtilityClass withConstructor() {
        return new UtilityClass(false);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        stop = false;
        try {
            Document doc = compilationInfo.getDocument();
            
            if (doc == null) {
                return null;
            }
            
            Element e = compilationInfo.getTrees().getElement(treePath);
            if (e == null) {
                return null;
            }
            
            if (clazz) {
                if (e.getKind() == ElementKind.ENUM) {
                    return null;
                }
                if (e.getKind() == ElementKind.INTERFACE) {
                    return null;
                }
                if (e.getKind() == ElementKind.ANNOTATION_TYPE) {
                    return null;
                }

                int cnt = 0;
                for (Element m : e.getEnclosedElements()) {
                    if (stop) {
                        return null;
                    }
                    if (m.accept(this, compilationInfo)) {
                        return null;
                    }
                    if (m.getKind() == ElementKind.METHOD || m.getKind() == ElementKind.FIELD) {
                        cnt++;
                    }
                }
                
                if (cnt == 0) {
                    return null;
                }
                
            } else {
                if (e.getKind() != ElementKind.CONSTRUCTOR) {
                    return null;
                }
                ExecutableElement x = (ExecutableElement)e;
                for (Element m : x.getEnclosingElement().getEnclosedElements()) {
                    if (stop) {
                        return null;
                    }
                    if (m.accept(this, compilationInfo)) {
                        return null;
                    }
                }
                if (x.getModifiers().contains(Modifier.PROTECTED) || x.getModifiers().contains(Modifier.PUBLIC)) {
                    // ok
                } else {
                    return null;
                }
            }
            List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
                clazz,
                TreePathHandle.create(e, compilationInfo),
                compilationInfo.getFileObject()
                ));

            int[] span = Utilities.findIdentifierSpan(treePath, compilationInfo, doc);

            if (span[0] != (-1) && span[1] != (-1)) {
                ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(),
                        NbBundle.getMessage(UtilityClass.class, clazz ? "MSG_UtilityClass" : "MSG_PublicConstructor"), // NOI18N
                        fixes,
                        doc,
                        doc.createPosition(span[0]),
                        doc.createPosition(span[1])
                        );
                
                return Collections.singletonList(ed);
            }
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
        return NbBundle.getMessage(UtilityClass.class, clazz ? "MSG_UtilityClass" : "MSG_PublicConstructor"); // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(UtilityClass.class, clazz ? "HINT_UtilityClass" : "HINT_PublicConstructor"); // NOI18N
    }

    public void cancel() {
        // XXX implement me 
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    
          
    public Boolean visit(Element arg0, CompilationInfo arg1) {
        return false;
    }

    public Boolean visit(Element arg0) {
        return false;
    }

    public Boolean visitPackage(PackageElement arg0, CompilationInfo arg1) {
        return false;
    }

    public Boolean visitType(TypeElement arg0, CompilationInfo arg1) {
        return false;
    }

    public Boolean visitVariable(VariableElement v, CompilationInfo arg1) {
        return !v.getModifiers().contains(Modifier.STATIC);
    }

    public Boolean visitExecutable(ExecutableElement m, CompilationInfo arg1) {
        if (clazz) {
            return !m.getModifiers().contains(Modifier.STATIC) && !arg1.getElementUtilities().isSynthetic(m);
        } else {
            return !m.getModifiers().contains(Modifier.STATIC) && !m.getSimpleName().contentEquals("<init>"); // NOI18N
        }
    }

    public Boolean visitTypeParameter(TypeParameterElement arg0, CompilationInfo arg1) {
        return false;
    }

    public Boolean visitUnknown(Element arg0, CompilationInfo arg1) {
        return false;
    }

    private static final class FixImpl implements Fix, Task<WorkingCopy> {
        private TreePathHandle handle;
        private FileObject file;
        private boolean clazz;
        
        public FixImpl(boolean clazz, TreePathHandle handle, FileObject file) {
            this.handle = handle;
            this.file = file;
            this.clazz = clazz;
        }
        
        public String getText() {
            return NbBundle.getMessage(UtilityClass.class, clazz ? "MSG_PrivateConstructor" : "MSG_MakePrivate"); // NOI18N
        }
        
        public ChangeInfo implement() throws IOException {
            ModificationResult result = JavaSource.forFileObject(file).runModificationTask(this);
            result.commit();
            return null;
        }
        
        @Override public String toString() {
            return "FixUtilityClass"; // NOI18N
        }

        public void run(WorkingCopy wc) throws Exception {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            
            Element e = handle.resolveElement(wc);
            if (e == null) {
                return;
            }
            Tree outer = wc.getTrees().getTree(e);
            if (clazz) {
                if (outer == null || outer.getKind() != Kind.CLASS) {
                    return;
                }
                ClassTree cls = (ClassTree)outer;
                
                ModifiersTree modifiers = wc.getTreeMaker().Modifiers(Collections.singleton(Modifier.PRIVATE));
                MethodTree m = wc.getTreeMaker().Constructor(
                    modifiers, 
                    Collections.<TypeParameterTree>emptyList(), 
                    Collections.<VariableTree>emptyList(), 
                    Collections.<ExpressionTree>emptyList(), 
                    wc.getTreeMaker().Block(Collections.<StatementTree>emptyList(), false)
                );
                wc.rewrite(cls, wc.getTreeMaker().addClassMember(cls, m));
            } else {
                if (outer == null || outer.getKind() != Kind.METHOD) {
                    return;
                }
                MethodTree met = (MethodTree)outer;
                
                ModifiersTree modifiers = wc.getTreeMaker().Modifiers(Collections.singleton(Modifier.PRIVATE), met.getModifiers().getAnnotations());
                wc.rewrite(met.getModifiers(), modifiers);
            }
        }
    }
    
}
