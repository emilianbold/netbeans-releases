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
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class AnnotationAsSuperInterface extends AbstractHint {

    private static final List<Fix> NO_FIXES = Collections.<Fix>emptyList();
    
    private Set<Kind> KINDS = Collections.<Tree.Kind>singleton(Tree.Kind.CLASS);
    
    public AnnotationAsSuperInterface() {
        super( true, true, HintSeverity.WARNING );
    }

    public Set<Kind> getTreeKinds() {
        return KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        
        Tree node = treePath.getLeaf();

        if ( node.getKind() != Tree.Kind.CLASS ) {
            return null;
        }
        
        ClassTree tree = (ClassTree)node;       
        
        Element e = info.getTrees().getElement(treePath);

        if ( e == null || !(e instanceof TypeElement) ) {
            return null;
        }
        
        List<ErrorDescription> eds = new ArrayList<ErrorDescription>(); 
        TypeElement te = (TypeElement) e;
        List<? extends TypeMirror> interfaces = te.getInterfaces();
        Types types = info.getTypes();
        
        for (TypeMirror typeMirror : interfaces) {
            Element si = types.asElement(typeMirror);
            if ( si != null && si.getKind() == ElementKind.ANNOTATION_TYPE ) {
                
                Tree annoTree = findTreeForAnnotation(tree, si);
                annoTree = annoTree == null ? tree : annoTree;
                eds.add( ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(), 
                        NbBundle.getMessage(AnnotationAsSuperInterface.class, 
                                            "HNT_AnnotationAsSuperInterface",  // NOI18N
                                            si.getSimpleName().toString()), 
                        NO_FIXES, 
                        info.getFileObject(),
                        (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), annoTree ),
                        (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), annoTree ) ) );
            }
        }

        return eds;
        
        // todo create hint
        
        /* TODO */
        // string with ==
        // empty blocks
        // typos
        // I18N
        // want to override/implement
        // add deprecated annotation
        // javac warnings                        
    }

    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return "AnnotationAsSuperInterface"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AssignmentToItself.class, "LBL_AnnotationAsSuperInterface"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(AssignmentToItself.class, "DSC_AnnotationAsSuperInterface"); // NOI18N
    }
    
    private Tree findTreeForAnnotation( ClassTree ct, Element annotation ) {
        
        String name = annotation.getSimpleName().toString();
        
        for (Tree tree : ct.getImplementsClause()) {
            if ( tree.toString().endsWith(name)) {
                return tree;
            }
        }

        return null;
        
    }
    
}
