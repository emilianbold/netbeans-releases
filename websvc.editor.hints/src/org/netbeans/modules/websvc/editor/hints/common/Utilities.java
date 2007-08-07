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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.editor.hints.common;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.util.Parameters;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 * @author Ajit.Bhate@Sun.COM
 */
public class Utilities {

    public static AnnotationMirror findAnnotation(Element element, String annotationClass) {
        for (AnnotationMirror ann : element.getAnnotationMirrors()) {
            if (annotationClass.equals(ann.getAnnotationType().toString())) {
                return ann;
            }
        }

        return null;
    }

    /**
     * A convenience method, returns true if findAnnotation(...) != null
     */
    public static boolean hasAnnotation(Element element, String annClass) {
        AnnotationMirror annEntity = findAnnotation(element, annClass);
        return annEntity != null;
    }

    /**
     * @return the value of annotation attribute, null if the attribute
     * was not found or when ann was null
     */
    public static AnnotationValue getAnnotationAttrValue(AnnotationMirror ann, String attrName) {
        if (ann != null) {
            for (ExecutableElement attr : ann.getElementValues().keySet()) {
                if (attrName.equals(attr.getSimpleName().toString())) {
                    return ann.getElementValues().get(attr);
                }
            }
        }

        return null;
    }

    public static ExpressionTree getAnnotationArgumentTree(AnnotationTree annotationTree, String attrName) {
        for (ExpressionTree exTree : annotationTree.getArguments()) {
            if (exTree instanceof AssignmentTree) {
                ExpressionTree annVar = ((AssignmentTree) exTree).getVariable();
                if (annVar instanceof IdentifierTree) {
                    if (attrName.equals(((IdentifierTree) annVar).getName().toString())) {
                        return exTree;
                    }
                }
            }
        }
        return null;
    }

    public static void addAnnotation(WorkingCopy workingCopy, Element element, String annotationName) throws IOException {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        ModifiersTree oldTree = null;
        if (element instanceof TypeElement) {
            oldTree = workingCopy.getTrees().getTree((TypeElement) element).getModifiers();
        } else if (element instanceof ExecutableElement) {
            oldTree = workingCopy.getTrees().getTree((ExecutableElement) element).getModifiers();
        } else if (element instanceof VariableElement) {
            oldTree = ((VariableTree) workingCopy.getTrees().getTree(element)).getModifiers();
        }
        if (oldTree == null) {
            return;
        }
        TypeElement annotationElement = workingCopy.getElements().getTypeElement(annotationName);
        TreeMaker make = workingCopy.getTreeMaker();
        AnnotationTree annotationTree = make.Annotation(make.QualIdent(annotationElement), Collections.<ExpressionTree>emptyList());
        ModifiersTree newTree = make.addModifiersAnnotation(oldTree, annotationTree);
        workingCopy.rewrite(oldTree, newTree);
    }

    public static void removeAnnotation(WorkingCopy workingCopy, Element element, AnnotationMirror annMirror) throws IOException {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        ModifiersTree oldTree = null;
        if (element instanceof TypeElement) {
            oldTree = workingCopy.getTrees().getTree((TypeElement) element).getModifiers();
        } else if (element instanceof ExecutableElement) {
            oldTree = workingCopy.getTrees().getTree((ExecutableElement) element).getModifiers();
        } else if (element instanceof VariableElement) {
            oldTree = ((VariableTree) workingCopy.getTrees().getTree(element)).getModifiers();
        }
        if (oldTree == null) {
            return;
        }
        AnnotationTree annotation = (AnnotationTree) workingCopy.getTrees().getTree(element, annMirror);
        TreeMaker make = workingCopy.getTreeMaker();
        ModifiersTree newTree = make.removeModifiersAnnotation(oldTree, annotation);
        workingCopy.rewrite(oldTree, newTree);
    }

    public static void removeAnnotationArgument(WorkingCopy workingCopy, Element element, AnnotationMirror annMirror, String argumentName) throws IOException {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        Parameters.javaIdentifierOrNull("argumentName", argumentName); // NOI18N
        ModifiersTree oldTree = null;
        if (element instanceof TypeElement) {
            oldTree = workingCopy.getTrees().getTree((TypeElement) element).getModifiers();
        } else if (element instanceof ExecutableElement) {
            oldTree = workingCopy.getTrees().getTree((ExecutableElement) element).getModifiers();
        } else if (element instanceof VariableElement) {
            oldTree = ((VariableTree) workingCopy.getTrees().getTree(element)).getModifiers();
        }
        if (oldTree == null) {
            return;
        }
        AnnotationTree annotation = (AnnotationTree) workingCopy.getTrees().getTree(element, annMirror);
        TreeMaker make = workingCopy.getTreeMaker();
        ExpressionTree e = getAnnotationArgumentTree(annotation, argumentName);
        AnnotationTree modifiedAnnotation = make.removeAnnotationAttrValue(annotation, e);
        workingCopy.rewrite(annotation, modifiedAnnotation);
    }

    public static void addAnnotationArgument(WorkingCopy workingCopy, Element element, 
            AnnotationMirror annMirror, String argumentName, Object argumentValue) throws IOException {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        Parameters.javaIdentifierOrNull("argumentName", argumentName); // NOI18N
        Parameters.notNull("argumentValue", argumentValue); // NOI18N
        ModifiersTree oldTree = null;
        if (element instanceof TypeElement) {
            oldTree = workingCopy.getTrees().getTree((TypeElement) element).getModifiers();
        } else if (element instanceof ExecutableElement) {
            oldTree = workingCopy.getTrees().getTree((ExecutableElement) element).getModifiers();
        } else if (element instanceof VariableElement) {
            oldTree = ((VariableTree) workingCopy.getTrees().getTree(element)).getModifiers();
        }
        if (oldTree == null) {
            return;
        }
        AnnotationTree annotation = (AnnotationTree) workingCopy.getTrees().getTree(element, annMirror);
        TreeMaker make = workingCopy.getTreeMaker();
        ExpressionTree oldArgTree = getAnnotationArgumentTree(annotation, argumentName);
        if(oldArgTree!=null)
            annotation = make.removeAnnotationAttrValue(annotation, oldArgTree);
        ExpressionTree argumentValueTree = null;
        if(argumentValue instanceof Enum) {
            TypeElement enumClassElement = workingCopy.getElements().getTypeElement(argumentValue.getClass().getCanonicalName());
            argumentValueTree =  make.MemberSelect(make.QualIdent(enumClassElement),
                    ((Enum)argumentValue).name());
        } else {
            try {
            argumentValueTree = make.Literal(argumentValue);
            } catch (IllegalArgumentException iae) {
                // dont do anything for now
                return ;
            }
        }
        if (argumentName != null) {
            argumentValueTree =  make.Assignment(make.Identifier(argumentName), argumentValueTree);
        }
        AnnotationTree modifiedAnnotation = make.addAnnotationAttrValue(annotation, argumentValueTree);
        workingCopy.rewrite(annotation, modifiedAnnotation);
    }

    /**
     * This method returns the part of the syntax tree to be highlighted.
     * It will be usually the class/method/variable identifier.
     */
    public static TextSpan getUnderlineSpan(CompilationInfo info, Tree tree) {
        SourcePositions srcPos = info.getTrees().getSourcePositions();

        int startOffset = (int) srcPos.getStartPosition(info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(), tree);

        Tree startSearchingForNameIndentifierBehindThisTree = null;

        if (tree.getKind() == Tree.Kind.CLASS) {
            startSearchingForNameIndentifierBehindThisTree = ((ClassTree) tree).getModifiers();
        } else if (tree.getKind() == Tree.Kind.METHOD) {
            startSearchingForNameIndentifierBehindThisTree = ((MethodTree) tree).getReturnType();
        } else if (tree.getKind() == Tree.Kind.VARIABLE) {
            startSearchingForNameIndentifierBehindThisTree = ((VariableTree) tree).getType();
        }

        if (startSearchingForNameIndentifierBehindThisTree != null) {
            int searchStart = (int) srcPos.getEndPosition(info.getCompilationUnit(),
                    startSearchingForNameIndentifierBehindThisTree);

            TokenSequence<JavaTokenId> tokenSequence = info.getTreeUtilities().tokensFor(tree);

            if (tokenSequence != null) {
                boolean eob = false;
                tokenSequence.move(searchStart);

                do {
                    eob = !tokenSequence.moveNext();
                } while (!eob && tokenSequence.token().id() != JavaTokenId.IDENTIFIER);

                if (!eob) {
                    Token<JavaTokenId> identifier = tokenSequence.token();
                    startOffset = identifier.offset(info.getTokenHierarchy());
                    endOffset = startOffset + identifier.length();
                }
            }
        }

        return new TextSpan(startOffset, endOffset);
    }

/**
     * Represents a span of text
     */
    public static class TextSpan {

        private int startOffset;
        private int endOffset;

        public TextSpan(int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }
    }
}
