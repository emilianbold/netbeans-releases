/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.javadoc;

import com.sun.javadoc.Doc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Tag;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Jan Pokorsky
 */
public final class JavadocImports {

    private JavadocImports() {
    }
    
    /**
     * Computes all unresolved (not imported) {@link Element}s referenced by
     * all javadocs of all class members of the passed java context {@code javac}.
     * 
     * @param javac a java context to search for all top level classes and
     *          all their members
     * @return names that have to be resoved (imported).
     */
    public static Set<String> computeUnresolvedImports(CompilationInfo javac) {
        List<? extends TypeElement> topLevelElements = javac.getTopLevelElements();
        UnresolvedImportScanner scanner = new UnresolvedImportScanner(javac);
        scanner.scan(topLevelElements, null);
        return scanner.unresolved;
    }
    
    /**
     * Computes all {@link Element}s referenced by javadoc of the passed element
     * {@code el}.
     * 
     * @param javac a java context
     * @param el an element to search
     * @return referenced elements.
     */
    public static Set<TypeElement> computeReferencedElements(CompilationInfo javac, TreePath tp) {
        Set<TypeElement> result = null;
        Element el = javac.getTrees().getElement(tp);
        TokenSequence<JavadocTokenId> jdTokenSequence = JavadocCompletionUtils.findJavadocTokenSequence(javac, tp.getLeaf(), el);
        if (el != null && jdTokenSequence != null) {
            ElementKind kind = el.getKind();
            TypeElement scope;
            
            if (kind.isClass() || kind.isInterface()) {
                scope = (TypeElement) el;
            } else {
                scope = javac.getElementUtilities().enclosingTypeElement(el);
            }
            Doc javadoc = javac.getElementUtilities().javaDocFor(el);
            DocPositions positions = null;
            List<? extends Tag> tags;
            if (javadoc != null && scope != null) {
                positions = DocPositions.get(javac, javadoc, jdTokenSequence);
                tags = positions.getTags();
            } else {
                tags = Collections.emptyList();
            }
            for (Tag tag : tags) {
                List<JavaReference> refs = findReferences (tag, positions, jdTokenSequence);
                if (refs != null) {
                    for (JavaReference reference : refs) {
                        if (reference.fqn != null) {
                            String fqn = reference.fqn.toString ();
                            TypeMirror type = javac.getTreeUtilities ().parseType (fqn, scope);
                            if (type != null &&
                                ( type.getKind () == TypeKind.DECLARED ||
                                  type.getKind() == TypeKind.ERROR
                                )
                            ) {
                                DeclaredType declaredType = (DeclaredType) type;
                                TypeElement foundElement = (TypeElement) declaredType.asElement ();
                                if (
                                    SourceVersion.isIdentifier (foundElement.getSimpleName ())
                                ) {
                                    if (result == null)
                                        result = new HashSet<TypeElement> ();
                                    result.add (foundElement);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (result == null) {
            result = Collections.emptySet();
        }
        return result;
    }
    
    /**
     * Computes all {@link Element}s referenced by javadoc of the passed element
     * {@code el}.
     * 
     * @param javac a java context
     * @param el an element to search
     * @param toFind an element to find in favadoc
     * @return referenced elements.
     */
    public static List<Token> computeTokensOfReferencedElements(CompilationInfo javac, TreePath forElement, Element toFind) {
        List<Token> result = null;
        Element el = javac.getTrees().getElement(forElement);
        TokenSequence<JavadocTokenId> jdTokenSequence = JavadocCompletionUtils.findJavadocTokenSequence(javac, forElement.getLeaf(), el);
        if (el != null && jdTokenSequence != null) {
            ElementKind kind = el.getKind();
            TypeElement scope;
            
            if (kind.isClass() || kind.isInterface()) {
                scope = (TypeElement) el;
            } else {
                scope = javac.getElementUtilities().enclosingTypeElement(el);
            }
            Doc javadoc = javac.getElementUtilities().javaDocFor(el);
            DocPositions positions = null;
            List<? extends Tag> tags;
            if (javadoc != null && scope != null) {
                positions = DocPositions.get(javac, javadoc, jdTokenSequence);
                tags = positions.getTags();
            } else {
                tags = Collections.emptyList();
            }

            final boolean isParam = toFind.getKind() == ElementKind.PARAMETER;
            final boolean isTypeParam = toFind.getKind() == ElementKind.TYPE_PARAMETER;

            for (Tag tag : tags) {
                List<JavaReference> references = findReferences (tag, positions, jdTokenSequence);
                if (references != null) {
                    for (JavaReference ref : references) {

                        Element referenced = ref.getReferencedElement(javac, scope);
                        while (referenced != null) {
                            if (referenced == toFind) {
                                break;
                            }
                            referenced = referenced.getEnclosingElement();
                        }
                        if (referenced == toFind) {
                            int pos = -1;
                            ElementKind rkind = referenced.getKind();
                            if (ref.fqn != null && (rkind.isClass() || rkind.isInterface())) {
                                String fqn = ((TypeElement) referenced).getQualifiedName().toString();
                                String reffqn = ref.fqn.toString(); // NOI18N
                                if (reffqn.startsWith(fqn)) {
                                    pos = ref.begin + fqn.length() - 1;
                                } else {
                                    String simpleName = referenced.getSimpleName().toString();
                                    pos = ref.begin + simpleName.length() - 1;
                                }
                            } else if (rkind.isField() || rkind == ElementKind.METHOD || rkind == ElementKind.CONSTRUCTOR || rkind == ElementKind.TYPE_PARAMETER) {
                                pos = ref.end - 1;
                            }

                            if (pos < 0) {
                                continue;
                            }

                            jdTokenSequence.move(pos);
                            if (jdTokenSequence.moveNext() && jdTokenSequence.token().id() == JavadocTokenId.IDENT) {
                                if (result == null) {
                                    result = new ArrayList<Token>();
                                }
                                result.add(jdTokenSequence.token());
                            }
                        }
                    }
                } else if ((isParam || isTypeParam) && tag != null && "@param".equals(tag.name())) { // NOI18N
                    ParamTag ptag = (ParamTag) tag;
                    boolean isKindMatching = (isParam && !ptag.isTypeParameter())
                            || (isTypeParam && ptag.isTypeParameter());
                    if (isKindMatching && toFind.getSimpleName().contentEquals(ptag.parameterName())
                            && toFind == paramElementFor(el, ptag)) {
                        Token<JavadocTokenId> token = findNameTokenOfParamTag(tag, positions, jdTokenSequence);
                        if (token != null) {
                            if (result == null) {
                                result = new ArrayList<Token>();
                            }
                            result.add(token);
                        }
                    }
                }
                
            }
        }
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /** maps ParamTag to parameter or type parameter of method or class */
    private static Element paramElementFor(Element methodOrClass, ParamTag ptag) {
        ElementKind kind = methodOrClass.getKind();
        List<? extends Element> params = Collections.emptyList();
        if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
            ExecutableElement ee = (ExecutableElement) methodOrClass;
            params = ptag.isTypeParameter()
                    ? ee.getTypeParameters()
                    : ee.getParameters();
        } else if (kind.isClass() || kind.isInterface()) {
            TypeElement te = (TypeElement) methodOrClass;
            params = te.getTypeParameters();
        }

        for (Element param : params) {
            if (param.getSimpleName().contentEquals(ptag.parameterName())) {
                return param;
            }
        }
        return null;
    }
    
    /**
     * Resolves class or member of the reference {@code (class#member)},
     * parameter {@code (@param parameter)} or type parameter {@code (@param <type_param>)}
     * with respect to the passed {@code offset}.
     * 
     * @param javac a java context
     * @param offset offset pointing to javadoc part to resolve
     * @return the found element or {@code null}.
     */
    public static Element findReferencedElement(CompilationInfo javac, int offset) {
        Element result = null;
        Document doc = null;
        try {
            doc = javac.getDocument();
        } catch (IOException ex) {
            // ignore
        }
        if (doc == null) {
            return null;
        }
        TokenSequence<JavadocTokenId> jdTokenSequence = JavadocCompletionUtils.findJavadocTokenSequence(javac, offset);
        if (jdTokenSequence != null) {
            Doc javadoc = JavadocCompletionUtils.findJavadoc(javac, doc, offset);
            if (javadoc == null) {
                return null;
            }
            DocPositions positions = DocPositions.get(javac, javadoc, jdTokenSequence);
            Element el = javac.getElementUtilities().elementFor(javadoc);
            if (positions == null || el == null) {
                return null;
            }
            
            ElementKind kind = el.getKind();
            TypeElement scope;
            
            if (kind.isClass() || kind.isInterface()) {
                scope = (TypeElement) el;
            } else {
                scope = javac.getElementUtilities().enclosingTypeElement(el);
            }

            Tag tag = positions.getTag (offset);
            
            List<JavaReference> references = tag != null
                    ? findReferences (tag, positions, jdTokenSequence)
                    : null;
            
            if (references != null && scope != null) {
                for (JavaReference reference : references) {
                    result = reference.getReferencedElement (javac, scope);
                    if (result != null &&
                        reference.fqn != null &&
                        offset < reference.begin + reference.fqn.length ()
                    ) {
                        result = result.getKind ().isClass () ||
                                 result.getKind ().isInterface () ||
                                 result.getKind() == ElementKind.TYPE_PARAMETER
                                    ? result : result.getEnclosingElement ();
                        int elmNameLength = result.getSimpleName ().length ();
                        while (
                            result != null &&
                            offset < reference.begin + reference.fqn.length () - elmNameLength
                        ) {
                            result = result.getEnclosingElement ();
                            elmNameLength += result != null
                                    ? result.getSimpleName ().length () + 1
                                    : 0;
                        }
                    }
                    if (result != null) break;
                }
            } else if (tag instanceof ParamTag && "@param".equals(tag.name())) { // NOI18N
                ParamTag ptag = (ParamTag) tag;
                result = paramElementFor(el, ptag);
            }
        }
        return result;
    }
    
    public static Token findNameTokenOfReferencedElement(CompilationInfo javac, int offset) {
        Document doc = null;
        try {
            doc = javac.getDocument();
        } catch (IOException ex) {
            // ignore
        }
        if (doc == null) {
            return null;
        }
        TokenSequence<JavadocTokenId> jdTokenSequence = JavadocCompletionUtils.findJavadocTokenSequence(javac, offset);
        if (jdTokenSequence != null) {
            Doc javadoc = JavadocCompletionUtils.findJavadoc(javac, doc, offset);
            if (javadoc == null) {
                return null;
            }
            DocPositions positions = DocPositions.get(javac, javadoc, jdTokenSequence);
            Element el = javac.getElementUtilities().elementFor(javadoc);
            if (positions == null || el == null) {
                return null;
            }
            
            ElementKind kind = el.getKind();
            TypeElement scope;
            
            if (kind.isClass() || kind.isInterface()) {
                scope = (TypeElement) el;
            } else {
                scope = javac.getElementUtilities().enclosingTypeElement(el);
            }

            Tag tag = positions.getTag(offset);
            
            List<JavaReference> references = tag != null
                    ? findReferences (tag, positions, jdTokenSequence)
                    : null;
            
            if (references != null && scope != null) {
                for (JavaReference reference : references) {
                    Element elm = reference.getReferencedElement (javac, scope);
                    if (elm != null) {
                        int fqnLength = reference.fqn != null ?
                            reference.fqn.length () : 0;
                        if (reference.fqn != null &&
                            offset >= reference.begin &&
                            offset < reference.begin + fqnLength ||
                            reference.member != null &&
                            offset > reference.begin + fqnLength &&
                            offset < reference.end
                        ) {
                            jdTokenSequence.move (offset);
                            if (jdTokenSequence.moveNext ()) {
                                return jdTokenSequence.token ().id () == JavadocTokenId.IDENT
                                    ? jdTokenSequence.token ()
                                    : null;
                            }
                        }
                    }
                }
            } else {
                // try to resolve @param name
                Token<JavadocTokenId> token = findNameTokenOfParamTag (
                    tag, positions, jdTokenSequence
                );
                return token;
            }

        }
        return null;
    }

    private static Token<JavadocTokenId> findNameTokenOfParamTag(Tag tag, DocPositions positions, TokenSequence<JavadocTokenId> jdTokenSequence) {
        if (tag == null || !"@param".equals(tag.name())) { // NOI18N
            return null;
        }

        int[] tagSpan = positions.getTagSpan(tag);
        Token<JavadocTokenId> result = null;

        jdTokenSequence.move(tagSpan[0]);
        if (jdTokenSequence.moveNext() && jdTokenSequence.token().id() == JavadocTokenId.TAG
                && jdTokenSequence.moveNext() && jdTokenSequence.token().id() == JavadocTokenId.OTHER_TEXT
                && jdTokenSequence.moveNext() && jdTokenSequence.token().id() == JavadocTokenId.IDENT
                ) {
            result = jdTokenSequence.token();
        }
        
        return result;
    }
    
    /**
     * Checks if the passed position {@code pos} is inside java reference of
     * some javadoc tag. This lightweight implementation ignores method parameters
     * 
     * @param jdts javadoc token sequence to search
     * @param pos position to check
     * @return {@code true} if the position is inside the reference.
     */
    public static boolean isInsideReference(TokenSequence<JavadocTokenId> jdts, int pos) {
        jdts.move(pos);
        if (jdts.moveNext() && JavadocTokenId.IDENT == jdts.token().id()) {
            // go back and find tag
            boolean isBeforeWS = false; // is current tage before white space?
            while (jdts.movePrevious()) {
                Token<JavadocTokenId> jdt = jdts.token();
                switch (jdt.id()) {
                    case DOT:
                    case HASH:
                    case IDENT:
                        if (isBeforeWS) {
                            return false;
                        } else {
                            continue;
                        }
                    case OTHER_TEXT:
                        isBeforeWS |= JavadocCompletionUtils.isWhiteSpace(jdt);
                        isBeforeWS |= JavadocCompletionUtils.isLineBreak(jdt);
                        if (isBeforeWS) {
                            continue;
                        } else {
                            return false;
                        }
                    case TAG:
                        return isBeforeWS && isReferenceTag(jdt);
                    case HTML_TAG:
                        return false;
                    default:
                        return false;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the passed position {@code pos} is inside name part of
     * some javadoc param tag.
     *
     * @param jdts javadoc token sequence to search
     * @param pos position to check
     * @return {@code true} if the position is inside the param name.
     */
    public static boolean isInsideParamName(TokenSequence<JavadocTokenId> jdts, int pos) {
        jdts.move(pos);
        if (jdts.moveNext() && (JavadocTokenId.IDENT == jdts.token().id() || JavadocTokenId.HTML_TAG == jdts.token().id())
                && jdts.movePrevious() && JavadocTokenId.OTHER_TEXT == jdts.token().id()
                && jdts.movePrevious() && JavadocTokenId.TAG == jdts.token().id()) {
            return "@param".contentEquals(jdts.token().text());
        }
        return false;
    }

    private static List<JavaReference> findReferences (
        Tag                     tag,
        DocPositions            positions,
        TokenSequence<JavadocTokenId>
                                jdTokenSequence
    ) {
        if (tag == null || !isReferenceTag(tag)) {
            return null;
        }
        int[] tagSpan = positions.getTagSpan(tag);
        jdTokenSequence.move(tagSpan[0] + (JavadocCompletionUtils.isBlockTag(tag)? 0: 1));
        if (!jdTokenSequence.moveNext() || jdTokenSequence.token().id() != JavadocTokenId.TAG) {
            return null;
        }
        if (!jdTokenSequence.moveNext()
                || !(JavadocCompletionUtils.isWhiteSpace(jdTokenSequence.token()) || JavadocCompletionUtils.isLineBreak(jdTokenSequence.token()))
                || !jdTokenSequence.moveNext()) {
            return null;
        }
        JavaReference reference = JavaReference.resolve (jdTokenSequence, jdTokenSequence.offset(), tagSpan[1]);
        return reference.getAllReferences ();
    }
    
    private static boolean isReferenceTag(Tag tag) {
        String tagName = tag.name();
        return ALL_REF_TAG_NAMES.contains(tagName.intern());
    }
    
    private static final Set<String> ALL_REF_TAG_NAMES = new HashSet<String>(
            Arrays.asList("@link", "@linkplain", "@value", "@see", "@throws")); // NOI18N
    
    private static boolean isReferenceTag(Token<JavadocTokenId> tag) {
        String tagName = tag.text().toString().intern();
        return tag.id() == JavadocTokenId.TAG && ALL_REF_TAG_NAMES.contains(tagName);
    }

    private static final class UnresolvedImportScanner extends ElementScanner6<Void, Void> {
        
        private final CompilationInfo javac;
        private Set<String> unresolved = new HashSet<String>();

        public UnresolvedImportScanner(CompilationInfo javac) {
            this.javac = javac;
        }
        
        @Override
        public Void visitExecutable(ExecutableElement e, Void p) {
            TypeElement enclosingTypeElement = javac.getElementUtilities().enclosingTypeElement(e);
            if (enclosingTypeElement != null) {
                resolveElement(e, enclosingTypeElement);
            }
            return super.visitExecutable(e, p);
        }

        @Override
        public Void visitType(TypeElement e, Void p) {
            resolveElement(e, e);
            return super.visitType(e, p);
        }

        @Override
        public Void visitVariable(VariableElement e, Void p) {
            TypeElement enclosingTypeElement = javac.getElementUtilities().enclosingTypeElement(e);
            if (enclosingTypeElement != null) {
                resolveElement(e, enclosingTypeElement);
            }
            return super.visitVariable(e, p);
        }
        
        private void resolveElement(Element el, TypeElement scope) {
            String jdText = javac.getElements().getDocComment(el);
            if (jdText != null) {
                Doc javadoc = javac.getElementUtilities().javaDocFor(el);
                TokenSequence<JavadocTokenId> jdTokenSequence = JavadocCompletionUtils.findJavadocTokenSequence(javac, null, el);
                if (jdTokenSequence != null) {
                    DocPositions positions = DocPositions.get(javac, javadoc, jdTokenSequence);
                    if (positions != null) {
                        resolveTags(positions, jdTokenSequence, scope);
                    }
                }
            }
        }
        
        private void resolveTags (
            DocPositions positions,
            TokenSequence<JavadocTokenId> jdTokenSequence,
            TypeElement scope
        ) {
            for (Tag tag : positions.getTags()) {
                List<JavaReference> references = findReferences (tag, positions, jdTokenSequence);
                if (references != null) {
                    for (JavaReference reference : references) {
                        if (reference.fqn != null && reference.fqn.length() > 0) {
                            String fqn = reference.fqn.toString ();
                            TypeMirror type = javac.getTreeUtilities ().parseType (fqn, scope);
                            if (type != null &&
                                type.getKind () == TypeKind.ERROR
                            ) {
                                unresolved.add (fqn);
                            }
                        }
                    }
                }
            }
        }
        
    }
}
