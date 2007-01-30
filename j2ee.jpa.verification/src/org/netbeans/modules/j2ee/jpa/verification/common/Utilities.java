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

package org.netbeans.modules.j2ee.jpa.verification.common;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class Utilities {
    public static AnnotationMirror findAnnotation(Element element, String annotationClass){
        for (AnnotationMirror ann : element.getAnnotationMirrors()){
            if (annotationClass.equals(ann.getAnnotationType().toString())){
                return ann;
            }
        }
        
        return null;
    }
    
    /**
     * This method returns the part of the syntax tree to be highlighted.
     * It will be usually the class/method/variable identifier.
     */
    public static TextSpan getUnderlineSpan(CompilationInfo info, Tree tree){
        SourcePositions srcPos = info.getTrees().getSourcePositions();
        
        int startOffset = (int) srcPos.getStartPosition(info.getCompilationUnit(), tree);
        int endOffset = (int) srcPos.getEndPosition(info.getCompilationUnit(), tree);
        Tree modifiersTree = null;
        
        if (tree.getKind() == Tree.Kind.CLASS){
            modifiersTree = ((ClassTree)tree).getModifiers();
        } else
            if (tree.getKind() == Tree.Kind.VARIABLE){
                modifiersTree = ((VariableTree)tree).getModifiers();
            } else
                if (tree.getKind() == Tree.Kind.METHOD){
                    modifiersTree = ((MethodTree)tree).getModifiers();
                }
        
        
        if (modifiersTree != null){
            int searchStart = (int) srcPos.getEndPosition(info.getCompilationUnit(), modifiersTree);
            
            TokenSequence tokenSequence = info.getTreeUtilities().tokensFor(tree);
            
            if (tokenSequence != null){
                boolean eob = false;
                tokenSequence.move(searchStart);
                
                do{
                    eob = !tokenSequence.moveNext();
                }
                while (!eob && tokenSequence.token().id() != JavaTokenId.IDENTIFIER);
                
                if (!eob){
                    Token identifier = tokenSequence.token();
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
    public static class TextSpan{
        private int startOffset;
        private int endOffset;
        
        public TextSpan(int startOffset, int endOffset){
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public int getStartOffset(){
            return startOffset;
        }
        
        public int getEndOffset(){
            return endOffset;
        }
    }
}
