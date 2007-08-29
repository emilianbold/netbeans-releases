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

package org.netbeans.modules.javadoc.hints;

import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Pokorsky
 */
public class JavadocUtilities {
    
    private JavadocUtilities() {
    }
    
    /**
     * Finds javadoc token sequence.
     * @param javac compilation info
     * @param doc javadoc for which the tokens are queried
     * @return javadoc token sequence or null.
     */
    private static TokenSequence<JavadocTokenId> findTokenSequence(CompilationInfo javac, Doc doc) {
        Element e = javac.getElementUtilities().elementFor(doc);
        
        if (e == null)
            return null;
        
        Tree tree = javac.getTrees().getTree(e);
        if (tree == null)
            return null;
        
        int elementStartOffset = (int) javac.getTrees().getSourcePositions().getStartPosition(javac.getCompilationUnit(), tree);
        TokenSequence<? extends TokenId> s = javac.getTokenHierarchy().tokenSequence();
        s.move(elementStartOffset);
        while (s.movePrevious() && IGNORE_TOKES.contains(s.token().id()))
            ;
        if (s.token().id() != JavaTokenId.JAVADOC_COMMENT)
            return null;
        
        return s.embedded(JavadocTokenId.language());
    }
    
    /**
     * Moves the passed token sequence to the first token that correspond to
     * the javadoc tag.
     * @param es javadoc token sequence
     * @param tag javadoc tag to find in the token sequence
     */
    private static void moveToTag(TokenSequence<JavadocTokenId> es, Tag tag, CompilationInfo javac) {
        Doc doc = tag.holder();
        Tag[] tags = doc.tags();
        int index = findIndex(tags, tag);
        
        if (index == (-1)) {
            //probably an inline tag:
            tags = doc.inlineTags();
            index = findIndex(tags, tag);
            
            assert index >= 0;
            
            index = computeTagsWithSameNumberBefore(tags, tag);
        } else {
            index = computeTagsWithSameNumberBefore(tags, tag);
        }
        
        assert index >=0;
        
        while (index >= 0 && es.moveNext()) {
            if (es.token().id() == JavadocTokenId.TAG &&
                    tag.name().contentEquals(es.token().text()) &&
                    --index < 0) {
                return;
            }
        }
        
        throw new IllegalStateException("Cannot match the tag: '" + tag.toString() // NOI18N
                + "'\nDoc.dump:\n" + doc.getRawCommentText() // NOI18N
                + "\nTokenSequence.dump: '" + es.toString() // NOI18N
                + "'\nElement.dump: " + javac.getElementUtilities().elementFor(doc) // NOI18N
                + "\nFile.name: " + javac.getFileObject() // NOI18N
                + "\nFile.dump:\n" + javac.getText() // NOI18N
                ); // NOI18N
    }
    
    public static TokenSequence<JavadocTokenId> tokensFor(CompilationInfo javac, Tag tag) {
        Doc doc = tag.holder();
        TokenSequence<JavadocTokenId> es = findTokenSequence(javac, doc);
        assert es != null;
        
        moveToTag(es, tag, javac);
        
        int offset = es.offset();
        
        int length = tag.text().length();
        length = length > 0? length: tag.name().length();
        TokenSequence<? extends TokenId> s = javac.getTokenHierarchy().tokenSequence();
        return s.embedded(JavadocTokenId.language()).subSequence(offset /*+ 1 */, offset + length);
    }
    
    /**
     * finds positions of tag name inside a document.
     * @param info compilation info
     * @param doc document
     * @param tag javadoc tag
     * @return Position[] {starOffset, endOffset}
     * @throws javax.swing.text.BadLocationException 
     */
    public static Position[] findTagNameBounds(CompilationInfo info, Document doc, Tag tag) throws BadLocationException {
        TokenSequence<JavadocTokenId> tseq = findTokenSequence(info, tag.holder());
        if (tseq == null)
            return null;
        moveToTag(tseq, tag, info);
        Position[] positions = new Position[2];
        positions[0] = doc.createPosition(tseq.offset());
        positions[1] = doc.createPosition(tseq.offset() + tseq.token().length());
        return positions;
    }
    
    public static Position[] findDocBounds(CompilationInfo javac, Document doc, Doc jdoc) throws BadLocationException {
        Element e = javac.getElementUtilities().elementFor(jdoc);
        
        if (e == null)
            return null;
        
        Tree tree = javac.getTrees().getTree(e);
        if (tree == null)
            return null;
        
        int elementStartOffset = (int) javac.getTrees().getSourcePositions().getStartPosition(javac.getCompilationUnit(), tree);
        TokenSequence<? extends TokenId> tseq = javac.getTokenHierarchy().tokenSequence();
        tseq.move(elementStartOffset);
        while (tseq.movePrevious() && IGNORE_TOKES.contains(tseq.token().id()))
            ;
        if (tseq.token().id() != JavaTokenId.JAVADOC_COMMENT)
            return null;
        
        Position[] positions = new Position[2];
        positions[0] = doc.createPosition(tseq.offset());
        positions[1] = doc.createPosition(tseq.offset() + tseq.token().length());
        return positions;
    }
    
    /**
     * finds block tag bounds
     */
    public static Position[] findTagBounds(CompilationInfo javac, Document doc, Tag tag) throws BadLocationException {
        return findTagBounds(javac, doc, tag, null);
    }
    
    public static Position[] findTagBounds(CompilationInfo javac, Document doc, Tag tag, boolean[] isLastToken) throws BadLocationException {
        TokenSequence<JavadocTokenId> tseq = findTokenSequence(javac, tag.holder());
        if (tseq == null)
            return null;
        moveToTag(tseq, tag, javac);
        
        int start = tseq.offset();
        
        Token<JavadocTokenId> token = null;
        Token<JavadocTokenId> last = null;
        while (tseq.moveNext()) {
            if (tseq.token().id() == JavadocTokenId.TAG &&
                    last != null && !(last.id() == JavadocTokenId.OTHER_TEXT &&
                    last.text().charAt(last.text().length() - 1) == '{')) { // hack for inline tags
                token = tseq.token();
                break;
            }
            last = tseq.token();
        }
        
        int lastTokenCleanUp = 0;
        if (token == null) {
            tseq.moveEnd();
            tseq.movePrevious();
            lastTokenCleanUp = tseq.token().text().toString().indexOf('\n');
            lastTokenCleanUp = lastTokenCleanUp > 0? lastTokenCleanUp: 0;
            if (isLastToken != null && isLastToken.length > 0) {
                isLastToken[0] = true;
            } else if (isLastToken != null && isLastToken.length > 0) {
                isLastToken[0] = false;
            }
        }
        
        Position[] positions = new Position[2];
        positions[0] = doc.createPosition(start);
        positions[1] = doc.createPosition(tseq.offset() + lastTokenCleanUp);
        return positions;
    }
    
    /**
     * finds last javadoc token bounds.
     */
    public static Position[] findLastTokenBounds(CompilationInfo javac, Document doc, Doc jdoc) throws BadLocationException {
        TokenSequence<JavadocTokenId> tseq = findTokenSequence(javac, jdoc);
        if (tseq == null)
            return null;
        
        tseq.moveEnd();
        if (tseq.movePrevious()) {
            Position[] positions = new Position[2];
            positions[0] = doc.createPosition(tseq.offset());
            positions[1] = doc.createPosition(tseq.offset() + tseq.token().length());
            return positions;
        }
        
        return null;
    }

    public static TokenSequence<JavaTokenId> findMethodNameToken(CompilationInfo javac, ClassTree enclosing, MethodTree t) {
        TokenSequence<JavaTokenId> tseq =  javac.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        Tree retType = t.getReturnType();
        if (retType == null) {
            // constructor
            int offset = (int) javac.getTrees().getSourcePositions().getStartPosition(javac.getCompilationUnit(), t);
            tseq.move(offset + 1);
            Token<JavaTokenId> tok = null;
            String name = enclosing.getSimpleName().toString();
            int index = -1;
//                System.out.println("Dump: " + t.getKind() + ", " + t.toString());
            while (tseq.moveNext()) {
//                System.out.println("dump.t: " + tseq.token().id() + ", '" + tseq.token().text() + "'"); 
                if (tok != null && tseq.token().id() == JavaTokenId.LPAREN && name.contentEquals(tok.text())) {
                    tseq.moveIndex(index);
                    tseq.moveNext();
//                        System.out.println("@@@@@@BINGO");
                    break;
                }
                if (tseq.token().id() == JavaTokenId.IDENTIFIER) {
                    tok = tseq.token();
                    index = tseq.index();
                }
            }
//                System.out.println();
        } else {
            int offset = (int) javac.getTrees().getSourcePositions().getEndPosition(javac.getCompilationUnit(), retType);
            tseq.move(offset + 1);
            while (tseq.moveNext() && tseq.token().id() != JavaTokenId.IDENTIFIER);
        }
        return tseq;
    }

    public static TokenSequence<JavaTokenId> findClassNameToken(CompilationInfo javac, ClassTree t) {
        TokenSequence<JavaTokenId> tseq =  javac.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        Tree modifs = t.getModifiers();
        assert modifs != null;
        int offset = (int) javac.getTrees().getSourcePositions().getEndPosition(javac.getCompilationUnit(), modifs);
        tseq.move(offset + 1);
        while (tseq.moveNext() && tseq.token().id() != JavaTokenId.IDENTIFIER);

        return tseq;
    }

    public static TokenSequence<JavaTokenId> findVariableNameToken(CompilationInfo javac, VariableTree t, boolean isEnum) {
        TokenSequence<JavaTokenId> tseq =  javac.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        Tree start = isEnum? t: t.getType();
        if (start == null) { // enum constant
            start = t.getModifiers();
        }
        assert start != null;
        int offset = isEnum?
            (int) javac.getTrees().getSourcePositions().getStartPosition(javac.getCompilationUnit(), start):
            (int) javac.getTrees().getSourcePositions().getEndPosition(javac.getCompilationUnit(), start);
        tseq.move(offset);
        String name = t.getName().toString();
        while (tseq.moveNext() /*&& !(tseq.token().id() == JavaTokenId.IDENTIFIER && name.contentEquals(tseq.token().text()))*/) {
            if (tseq.token().id() == JavaTokenId.IDENTIFIER && name.contentEquals(tseq.token().text())) {
                break;
            }
        }

        return tseq;
    }
    
    private static int computeTagsWithSameNumberBefore(Tag[] tags, Tag tag) {
        int index = 0;
        
        for (Tag t : tags) {
            if (t == tag)
                return index;
            if (t.name().equals(tag.name()))
                index++;
        }
        
        return -1;
    }
    
    private static int findIndex(Tag[] tags, Tag tag) {
        for (int i = 0; i < tags.length; i++) {
            if (tag == tags[i]) {
                return i;
            }
        }
        return -1;
    }
    
    public static boolean isDeprecated(CompilationInfo javac, Element elm) {
        return findDeprecated(javac, elm) != null;
    }
        
    public static AnnotationMirror findDeprecated(CompilationInfo javac, Element elm) {
        TypeElement deprAnn = javac.getElements().getTypeElement("java.lang.Deprecated"); //NOI18N
        assert deprAnn != null;
        for (AnnotationMirror annotationMirror : javac.getElements().getAllAnnotationMirrors(elm)) {
            if (deprAnn.equals(annotationMirror.getAnnotationType().asElement())) {
                return annotationMirror;
            }
        }
        return null;
    }
    
    public static boolean hasInheritedDoc(CompilationInfo javac, Element elm) {
        return findInheritedDoc(javac, elm) != null;
    }
    
    public static MethodDoc findInheritedDoc(CompilationInfo javac, Element elm) {
        if (elm.getKind() == ElementKind.METHOD) {
            TypeElement clazz = (TypeElement) elm.getEnclosingElement();
            return searchInInterfaces(javac, clazz, clazz,
                    (ExecutableElement) elm, new HashSet<TypeElement>());
        }
        return null;
    }
    
    /**
     * <a href="http://java.sun.com/javase/6/docs/technotes/tools/solaris/javadoc.html#inheritingcomments">
     * Algorithm for Inheriting Method Comments
     * </a>
     * <p>Do not use MethodDoc.overriddenMethod() instead since it fails for
     * interfaces!
     */
    private static MethodDoc searchInInterfaces(
            CompilationInfo javac, TypeElement class2query, TypeElement overriderClass,
            ExecutableElement overrider, Set<TypeElement> exclude) {
        
        // Step 1
        for (TypeMirror ifceMirror : class2query.getInterfaces()) {
            if (ifceMirror.getKind() == TypeKind.DECLARED) {
                TypeElement ifceEl = (TypeElement) ((DeclaredType) ifceMirror).asElement();
                if (exclude.contains(ifceEl)) {
                    continue;
                }
                // check methods
                MethodDoc jdoc = searchInMethods(javac, ifceEl, overriderClass, overrider);
                if (jdoc != null) {
                    return jdoc;
                }
                exclude.add(ifceEl);
            }
        }
        // Step 2
        for (TypeMirror ifceMirror : class2query.getInterfaces()) {
            if (ifceMirror.getKind() == TypeKind.DECLARED) {
                TypeElement ifceEl = (TypeElement) ((DeclaredType) ifceMirror).asElement();
                MethodDoc jdoc = searchInInterfaces(javac, ifceEl, overriderClass, overrider, exclude);
                if (jdoc != null) {
                    return jdoc;
                }
            }
        }
        // Step 3
        return searchInSuperclass(javac, class2query, overriderClass, overrider, exclude);
    }
    
    private static MethodDoc searchInSuperclass(
            CompilationInfo javac, TypeElement class2query, TypeElement overriderClass,
            ExecutableElement overrider, Set<TypeElement> exclude) {
        
        // Step 3a
        TypeMirror superclassMirror = class2query.getSuperclass();
        if (superclassMirror.getKind() != TypeKind.DECLARED) {
            return null;
        }
        TypeElement superclass = (TypeElement) ((DeclaredType) superclassMirror).asElement();
        // check methods
        MethodDoc jdoc = searchInMethods(javac, superclass, overriderClass, overrider);
        if (jdoc != null) {
            return jdoc;
        }
        
        // Step 3b
        return searchInInterfaces(javac, superclass, overriderClass, overrider, exclude);
    }
    
    private static MethodDoc searchInMethods(
            CompilationInfo javac, TypeElement class2query,
            TypeElement overriderClass, ExecutableElement overrider) {
        
        for (Element elm : class2query.getEnclosedElements()) {
            if (elm.getKind() == ElementKind.METHOD &&
                    javac.getElements().overrides(overrider, (ExecutableElement) elm, overriderClass)) {
                Doc jdoc = javac.getElementUtilities().javaDocFor(elm);
                return (jdoc != null && jdoc.getRawCommentText().length() > 0)?
                    (MethodDoc) jdoc: null;
            }
        }
        return null;
    }
    
    public static ParamTag findParamTag(CompilationInfo javac, MethodDoc doc, String paramName, boolean inherited) {
        ExecutableElement overrider = (ExecutableElement) javac.getElementUtilities().elementFor(doc);
        TypeElement overriderClass = (TypeElement) overrider.getEnclosingElement();
        TypeElement class2query = null;
        Set<TypeElement> exclude = null;
        while (doc != null) {
            for (ParamTag paramTag : doc.paramTags()) {
                if (paramName.equals(paramTag.parameterName())) {
                    return paramTag;
                }
            }
            if (inherited) {
                if (exclude == null) {
                    exclude = new HashSet<TypeElement>();
                }
                
                if (class2query == null) {
                    class2query = overriderClass;
                } else {
                    Element melm = javac.getElementUtilities().elementFor(doc);
                    class2query = (TypeElement) melm.getEnclosingElement();
                }
                
                doc = searchInInterfaces(javac, class2query, overriderClass, overrider, exclude);
            } else {
                break;
            }
        }
        return null;
    }
    
    public static ThrowsTag findThrowsTag(CompilationInfo javac, MethodDoc doc, String fqn, boolean inherited) {
        ExecutableElement overrider = (ExecutableElement) javac.getElementUtilities().elementFor(doc);
        TypeElement overriderClass = (TypeElement) overrider.getEnclosingElement();
        TypeElement class2query = null;
        Set<TypeElement> exclude = null;
        while (doc != null) {
            for (ThrowsTag throwsTag : doc.throwsTags()) {
                com.sun.javadoc.Type tagType = throwsTag.exceptionType();
                String tagFQN = null;
                if (tagType != null) {
                    tagFQN = throwsTag.exceptionType().qualifiedTypeName();
                } else { // unresolvable type
                    tagFQN = throwsTag.exceptionName();
                }
                if (tagFQN.equals(fqn)) {
                    return throwsTag;
                }
            }
            if (inherited) {
                if (exclude == null) {
                    exclude = new HashSet<TypeElement>();
                }
                
                if (class2query == null) {
                    class2query = overriderClass;
                } else {
                    Element melm = javac.getElementUtilities().elementFor(doc);
                    class2query = (TypeElement) melm.getEnclosingElement();
                }
                
                doc = searchInInterfaces(javac, class2query, overriderClass, overrider, exclude);
            } else {
                break;
            }
        }
        return null;
    }
    
    public static Tag findReturnTag(CompilationInfo javac, MethodDoc doc, boolean inherited) {
        ExecutableElement overrider = (ExecutableElement) javac.getElementUtilities().elementFor(doc);
        TypeElement overriderClass = (TypeElement) overrider.getEnclosingElement();
        TypeElement class2query = null;
        Set<TypeElement> exclude = null;
        while (doc != null) {
            Tag[] tags = doc.tags("@return"); // NOI18N
            if (tags.length > 0) {
                return tags[0];
            }
            if (inherited) {
                if (exclude == null) {
                    exclude = new HashSet<TypeElement>();
                }
                
                if (class2query == null) {
                    class2query = overriderClass;
                } else {
                    Element melm = javac.getElementUtilities().elementFor(doc);
                    class2query = (TypeElement) melm.getEnclosingElement();
                }
                
                doc = searchInInterfaces(javac, class2query, overriderClass, overrider, exclude);
            } else {
                break;
            }
        }
        return null;
    }
    
    public static void open(final FileObject fo, final int offset) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                doOpenImpl(fo, offset);
            }
        });
    }
    
    private static boolean doOpenImpl(FileObject fo, int offset) {
        try {
            DataObject od = DataObject.find(fo);
            EditorCookie ec = od.getCookie(EditorCookie.class);
            LineCookie lc = od.getCookie(LineCookie.class);
            
            if (ec != null && lc != null && offset != -1) {
                StyledDocument doc = ec.openDocument();
                if (doc != null) {
                    int line = NbDocument.findLineNumber(doc, offset);
                    int lineOffset = NbDocument.findLineOffset(doc, line);
                    int column = offset - lineOffset;
                    
                    if (line != -1) {
                        Line l = lc.getLineSet().getCurrent(line);
                        
                        if (l != null) {
                            l.show(Line.SHOW_GOTO, column);
                            return true;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(JavadocUtilities.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return false;
    }
    
    public static final class TagHandle {
        private final String name;
        private final String text;
        private final int index;
        
        private TagHandle(Tag tag) {
            this.name = tag.name();
            this.text = tag.text();
            this.index = findIndex(tag.holder().tags(), tag);
        }
        
        public static TagHandle create(Tag tag) {
            return new TagHandle(tag);
        }
        
        public Tag resolve(Doc doc) {
            Tag[] tags = doc.tags();
            if (this.index < tags.length &&
                    this.name.equals(tags[this.index].name()) &&
                    this.text.equals(tags[this.index].text())) {
                return tags[this.index];
            }
            
            // javadoc was changed
            for (Tag tag : tags) {
                if (this.name.equals(tags[this.index].name()) &&
                        this.text.equals(tags[this.index].text())) {
                    return tag;
                }
            }

            return null;
        }
        
        @Override
        public String toString() {
            return super.toString() + "[index: " + this.index + // NOI18N
                    "name: " + this.name + "text: " + this.text + ']'; // NOI18N
        }

        
    }
    
    private static Set<TokenId> IGNORE_TOKES = null;
    
    static {
        IGNORE_TOKES = new HashSet<TokenId>();
        IGNORE_TOKES.add(JavaTokenId.WHITESPACE);
        IGNORE_TOKES.add(JavaTokenId.BLOCK_COMMENT);
        IGNORE_TOKES.add(JavaTokenId.LINE_COMMENT);
    }

}
