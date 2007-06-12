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

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class EmptyCancelForCancellableTask extends AbstractHint {

    public EmptyCancelForCancellableTask() {
        super(true, true, HintSeverity.WARNING);
    }

    public String getDescription() {
        return NbBundle.getMessage(EmptyCancelForCancellableTask.class, "DSC_EmptyCancel");
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD);
    }

    private static Set<String> typesToCheck = new HashSet<String>(
            Arrays.asList(
                "org.netbeans.api.java.source.CancellableTask<org.netbeans.api.java.source.CompilationInfo>", //NOI18N
                "org.netbeans.modules.java.hints.spi.Rule" //NOI18N
            )
    );
    
    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        Element e = compilationInfo.getTrees().getElement(treePath);
        
        if (   e == null
            || e.getKind() != ElementKind.METHOD
            || !"cancel".equals(e.getSimpleName().toString()) //NOI18N
            || e.getModifiers().contains(Modifier.ABSTRACT)) {
            return null;
        }
        
        Element clazz = e.getEnclosingElement();
        
        if (!clazz.getKind().isClass()) {
            return null;
        }
        
        boolean found = false;
        
        OUT: for (String toCheck : typesToCheck) {
            TypeElement clazzTE = (TypeElement) clazz;
            TypeMirror  clazzTM = clazzTE.asType();
            TypeMirror  typeToCheck = compilationInfo.getTreeUtilities().parseType(toCheck, clazzTE);
            
            if (typeToCheck.getKind() != TypeKind.DECLARED)
                continue;
            
            TypeElement typeToCheckTE = (TypeElement) ((DeclaredType) typeToCheck).asElement();
            
            if (   compilationInfo.getTypes().isSubtype(clazzTM, typeToCheck)
                && !clazzTM.equals(typeToCheck)) {
                for (ExecutableElement ee : ElementFilter.methodsIn(typeToCheckTE.getEnclosedElements())) {
                    if (compilationInfo.getElements().overrides((ExecutableElement) e, ee, clazzTE)) {
                        found = true;
                        break OUT;
                    }
                }
            }
        }
        
        if (!found) {
            return null;
        }
        
        MethodTree mt = (MethodTree) treePath.getLeaf();
        
        if (!mt.getBody().getStatements().isEmpty()) {
            return null;
        }
        
        try {
            Document doc = compilationInfo.getDocument();
            int[] span = Utilities.findIdentifierSpan(treePath, compilationInfo.getCompilationUnit(), compilationInfo.getTrees().getSourcePositions(), doc);
            
            if (span[0] != (-1) && span[1] != (-1)) {
                String message = NbBundle.getMessage(EmptyCancelForCancellableTask.class, "MSG_EmptyCancel");
                ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), message, doc, doc.createPosition(span[0]), doc.createPosition(span[1]));
                
                return Collections.singletonList(ed);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }

    public String getId() {
        return EmptyCancelForCancellableTask.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(EmptyCancelForCancellableTask.class, "LBL_EmptyCancel");
    }

    public void cancel() {
    }

}
