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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
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
public class ExportNonAccessibleElement extends AbstractHint 
implements ElementVisitor<Boolean,Void>, TypeVisitor<Boolean,Void> {
    private transient volatile boolean stop;
    
    /** Creates a new instance of AddOverrideAnnotation */
    public ExportNonAccessibleElement() {
        super( true, true, AbstractHint.HintSeverity.WARNING );
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD, Kind.CLASS, Kind.VARIABLE);
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
            Boolean b = e.accept(this, null);
            
            if (b) {
                Element parent = e;
                for (;;) {
                    if (stop) {
                        return null;
                    }
                    
                    if (parent == null || parent.getKind() == ElementKind.PACKAGE) {
                        break;
                    }
                    if (!parent.getModifiers().contains(Modifier.PUBLIC) && !parent.getModifiers().contains(Modifier.PROTECTED)) {
                        return null;
                    }
                    parent = parent.getEnclosingElement();
                }

                List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(
                    "MSG_ExportNonAccessibleElementMakeNonVisible", // NOI18N
                    TreePathHandle.create(e, compilationInfo), 
                    compilationInfo.getFileObject()
                ));

                int[] span = Utilities.findIdentifierSpan(treePath, compilationInfo, doc);

                if (span[0] != (-1) && span[1] != (-1)) {
                    ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                            getSeverity().toEditorSeverity(),
                            NbBundle.getMessage(ExportNonAccessibleElement.class, "MSG_ExportNonAccessibleElement"),
                            fixes,
                            doc,
                            doc.createPosition(span[0]),
                            doc.createPosition(span[1])
                            );
                    
                    return Collections.singletonList(ed);
                }
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
        return NbBundle.getMessage(MissingHashCode.class, "MSG_ExportNonAccessibleElement");
    }

    public String getDescription() {
        return NbBundle.getMessage(MissingHashCode.class, "HINT_ExportNonAccessibleElement");
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
    public Boolean visit(Element arg0, Void arg1) {
        // no need for hint
        return false;
    }

    public Boolean visit(Element arg0) {
        // no need for hint
        return false;
    }

    public Boolean visitPackage(PackageElement arg0, Void arg1) {
        return false;
    }

    public Boolean visitType(TypeElement arg0, Void arg1) {
        for (TypeParameterElement e : arg0.getTypeParameters()) {
            if (stop) {
                return false;
            }
            
            for (TypeMirror b : e.getBounds()) {
                if (stop) {
                    return false;
                }
                
                if (b.accept(this, arg1)) {
                    return true;
                }
            }
        }
        return arg0.getSuperclass().accept(this, arg1);
    }

    public Boolean visitVariable(VariableElement field, Void arg1) {
        if (!isVisible(field)) {
            // no need for hint
            return false;
        }
        return field.asType().accept(this, arg1);
    }

    public Boolean visitExecutable(ExecutableElement method, Void nothing) {
        if (!isVisible(method)) {
            // ok
            return false;
        }
        for (VariableElement v : method.getParameters()) {
            if (stop) {
                return false;
            }
            
            if (v.asType().accept(this, nothing)) {
                return true;
            }
        }
        return method.getReturnType().accept(this, nothing);
    }

    public Boolean visitTypeParameter(TypeParameterElement arg0, Void arg1) {
        return false;
    }

    public Boolean visitUnknown(Element arg0, Void arg1) {
        // ok, probably no need for hint
        return false;
    }


    public Boolean visit(TypeMirror arg0, Void arg1) {
        return false;
    }

    public Boolean visit(TypeMirror arg0) {
        return false;
    }

    public Boolean visitPrimitive(PrimitiveType arg0, Void arg1) {
        return false;
    }

    public Boolean visitNull(NullType arg0, Void arg1) {
        return false;
    }

    public Boolean visitArray(ArrayType arg0, Void arg1) {
        return arg0.getComponentType().accept(this, arg1);
    }

    public Boolean visitDeclared(DeclaredType arg0, Void arg1) {
        if (!isVisible(arg0.asElement())) {
            return true;
        }
        for (TypeMirror t : arg0.getTypeArguments()) {
            if (stop) {
                return false;
            }
            
            if (t.accept(this, arg1)) {
                return true;
            }
        }
        return arg0.getEnclosingType().accept(this, arg1);
    }

    public Boolean visitError(ErrorType arg0, Void arg1) {
        // no hint
        return false;
    }

    public Boolean visitTypeVariable(TypeVariable arg0, Void arg1) {
        return arg0.getLowerBound().accept(this, arg1) && arg0.getUpperBound().accept(this, arg1);
    }

    public Boolean visitWildcard(WildcardType wild, Void arg1) {
        TypeMirror eb = wild.getExtendsBound();
        TypeMirror sb = wild.getSuperBound();
        return (eb != null && eb.accept(this, arg1)) ||
               (sb != null && sb.accept(this, arg1));
    }

    public Boolean visitExecutable(ExecutableType arg0, Void arg1) {
        return false;
    }

    public Boolean visitNoType(NoType arg0, Void arg1) {
        return false;
    }

    public Boolean visitUnknown(TypeMirror arg0, Void arg1) {
        return false;
    }
    
    
    private boolean isVisible(Element... arr) {
        return isVisible(Arrays.asList(arr));
    }
    private boolean isVisible(Collection<? extends Element> arr) {
        for (Element el : arr) {
            if (stop) {
                return false;
            }
            
            if (el.getModifiers().contains(Modifier.PUBLIC) || 
                el.getModifiers().contains(Modifier.PROTECTED)
            ) {
                return true;
            }
        }
        return false;
    }
          
    private static final class FixImpl implements Fix {
        private TreePathHandle handle;
        private FileObject file;
        private String msg;
        
        public FixImpl(String type, TreePathHandle handle, FileObject file) {
            this.handle = handle;
            this.file = file;
            this.msg = type;
        }
        
        public String getText() {
            return NbBundle.getMessage(MissingHashCode.class, msg);
        }
        
        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
        
        public ChangeInfo implement() throws IOException {
            ModificationResult result = JavaSource.forFileObject(file).runModificationTask(new Task<WorkingCopy>() {

                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    Element e = handle.resolveElement(wc);
                    
                    Tree t = wc.getTrees().getTree(e);
                    if (t.getKind() == Kind.CLASS) {
                        ClassTree ct = (ClassTree)t;
                        Set<Modifier> flags = new HashSet<Modifier>(ct.getModifiers().getFlags());
                        flags.remove(Modifier.PUBLIC);
                        flags.remove(Modifier.PROTECTED);
                        ModifiersTree mt = wc.getTreeMaker().Modifiers(flags, ct.getModifiers().getAnnotations());
                        wc.rewrite(ct.getModifiers(), mt);
                        return;
                    }
                    if (t.getKind() == Kind.METHOD) {
                        MethodTree mt = (MethodTree)t;
                        Set<Modifier> flags = new HashSet<Modifier>(mt.getModifiers().getFlags());
                        flags.remove(Modifier.PUBLIC);
                        flags.remove(Modifier.PROTECTED);
                        ModifiersTree modt = wc.getTreeMaker().Modifiers(flags, mt.getModifiers().getAnnotations());
                        wc.rewrite(mt.getModifiers(), modt);
                        return;
                    }
                    if (t.getKind() == Kind.VARIABLE) {
                        VariableTree vt = (VariableTree)t;
                        Set<Modifier> flags = new HashSet<Modifier>(vt.getModifiers().getFlags());
                        flags.remove(Modifier.PUBLIC);
                        flags.remove(Modifier.PROTECTED);
                        ModifiersTree modt = wc.getTreeMaker().Modifiers(flags, vt.getModifiers().getAnnotations());
                        wc.rewrite(vt.getModifiers(), modt);
                        return;
                    }
                }
            });

            result.commit();
            
            return null;
        }
        
        @Override
        public String toString() {
            return "FixExportNonAccessibleElement"; // NOI18N
        }
    }
}
