/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.javadoc.hints;

import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import static com.sun.source.tree.Tree.Kind.ANNOTATION_TYPE;
import static com.sun.source.tree.Tree.Kind.CLASS;
import static com.sun.source.tree.Tree.Kind.ENUM;
import static com.sun.source.tree.Tree.Kind.INTERFACE;
import static com.sun.source.tree.Tree.Kind.METHOD;
import static com.sun.source.tree.Tree.Kind.VARIABLE;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
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
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.hints.Severity;
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
    private static final String ERROR_IDENT = "<error>"; //NOI18N
    
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
        TokenSequence<?> s = javac.getTokenHierarchy().tokenSequence();
        s.move(elementStartOffset);
        Token token = null;
        while (s.movePrevious()) {
            token = s.token();
            if (token.id() == JavaTokenId.BLOCK_COMMENT) {
                if ("/**/".contentEquals(token.text())) { // NOI18N
                    // see #147533
                    break;
                }
            }
            if (!IGNORE_TOKES.contains(token.id())) {
                break;
            }
        }
        if (token == null || token.id() != JavaTokenId.JAVADOC_COMMENT) {
            return null;
        }
        
        return s.embedded(JavadocTokenId.language());
    }
    /**
     * Finds javadoc token sequence.
     * @param javac compilation info
     * @param e element for which the tokens are queried
     * @return javadoc token sequence or null.
     */
    static TokenSequence<JavadocTokenId> findTokenSequence(CompilationInfo javac, Element e) {
        if (e == null || javac.getElementUtilities().isSynthetic(e) || javac.getElements().getDocComment(e) == null) {
            return null;
        }
        Doc doc = javac.getElementUtilities().javaDocFor(e);
        return doc == null ? null: findTokenSequence(javac, doc);
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
        TokenSequence<?> s = javac.getTokenHierarchy().tokenSequence();
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
        TokenSequence<?> tseq = javac.getTokenHierarchy().tokenSequence();
        tseq.move(elementStartOffset);
        Token token = null;
        while (tseq.movePrevious()) {
            token = tseq.token();
            if (token.id() == JavaTokenId.BLOCK_COMMENT) {
                if ("/**/".contentEquals(token.text())) { // NOI18N
                    // see #147533
                    break;
                }
            }
            if (!IGNORE_TOKES.contains(token.id())) {
                break;
            }
        }
        if (token == null || token.id() != JavaTokenId.JAVADOC_COMMENT) {
            return null;
        }
        
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
        
        if (isLastToken != null && isLastToken.length > 0) {
                isLastToken[0] = false;
        }
        
        int lastTokenCleanUp = 0;
        if (token == null) {
            tseq.moveEnd();
            tseq.movePrevious();
            lastTokenCleanUp = tseq.token().text().toString().indexOf('\n');
            lastTokenCleanUp = lastTokenCleanUp >= 0? lastTokenCleanUp: tseq.token().length();
            if (isLastToken != null && isLastToken.length > 0) {
                isLastToken[0] = true;
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
        
        Position[] positions;
        if (tseq.isEmpty()) {
            // empty javadoc /***/
            positions = findDocBounds(javac, doc, jdoc);
            positions[0] = doc.createPosition(positions[0].getOffset() + "/**".length()); // NOI18N
            positions[1] = positions[0];
        } else {
            tseq.moveEnd();
            tseq.movePrevious();
            positions = new Position[2];
            positions[0] = doc.createPosition(tseq.offset());
            positions[1] = doc.createPosition(tseq.offset() + tseq.token().length());
        }
        
        return positions;
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
        if (deprAnn == null) {
            String msg = String.format("Even though the source level of %s" + //NOI18N
                    " is set to JDK5 or later, java.lang.Deprecated cannot" + //NOI18N
                    " be found on the bootclasspath: %s", //NOI18N
                    javac.getClasspathInfo().getClassPath(PathKind.SOURCE),
                    javac.getClasspathInfo().getClassPath(PathKind.BOOT));
            Logger.getLogger(JavadocUtilities.class.getName()).warning(msg);
            return null;
        }
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
    
    public static ParamTag findParamTag(CompilationInfo javac, MethodDoc doc, String paramName, boolean isTypeParam, boolean inherited) {
        ExecutableElement overrider = (ExecutableElement) javac.getElementUtilities().elementFor(doc);
        TypeElement overriderClass = (TypeElement) overrider.getEnclosingElement();
        TypeElement class2query = null;
        Set<TypeElement> exclude = null;
        while (doc != null) {
            ParamTag[] paramTags = isTypeParam ? doc.typeParamTags() : doc.paramTags();
            for (ParamTag paramTag : paramTags) {
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
                loadInheritedContext(javac, doc);
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

    /**
     * Resolves java types for inherited javadoc. It is necessary to call before
     * invoking e.g. {@code ThrowsTag.exceptionType()}. The present implementation
     * of JavadocEnv creates inherited javadocs in the separate javac instance
     * (see method {@code JavadocEnv.getRawCommentFor()}).
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=153352">Issue 153352</a>
     */
    private static void loadInheritedContext(CompilationInfo javac, MethodDoc doc) {
        Element elm = javac.getElementUtilities().elementFor(doc);
        Tree tree = javac.getTrees().getTree(elm);
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
                            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, column);
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

    static boolean isValid(CompilationInfo javac, TreePath path, Severity severity, Access access, int caret) {
        Tree leaf = path.getLeaf();
        boolean onLine = severity == Severity.HINT && caret > -1;
        switch (leaf.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                return access.isAccessible(javac, path, false) && (!onLine || isInHeader(javac, (ClassTree) leaf, caret));
            case METHOD:
                return access.isAccessible(javac, path, false) && (!onLine || isInHeader(javac, (MethodTree) leaf, caret));
            case VARIABLE:
                return access.isAccessible(javac, path, false);
        }
        return false;
    }
    
    public static boolean isGuarded(Tree node, CompilationInfo javac, Document doc) {
        GuardedSectionManager guards = GuardedSectionManager.getInstance((StyledDocument) doc);
        if (guards != null) {
            try {
                final int startOff = (int) javac.getTrees().getSourcePositions().
                        getStartPosition(javac.getCompilationUnit(), node);
                final Position startPos = doc.createPosition(startOff);

                for (GuardedSection guard : guards.getGuardedSections()) {
                    if (guard.contains(startPos, false)) {
                        return true;
                    }
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(Analyzer.class.getName()).log(Level.INFO, ex.getMessage(), ex);
                // consider it as guarded
                return true;
            }
        }
        return false;
    }
    
    /**
     * has syntax errors preventing to generate javadoc?
     */
    public static boolean hasErrors(Tree leaf) {
        switch (leaf.getKind()) {
            case METHOD:
                MethodTree mt = (MethodTree) leaf;
                Tree rt = mt.getReturnType();
                if (rt != null && rt.getKind() == Tree.Kind.ERRONEOUS) {
                    return true;
                }
                if (ERROR_IDENT.contentEquals(mt.getName())) {
                    return true;
                }
                for (VariableTree vt : mt.getParameters()) {
                    if (ERROR_IDENT.contentEquals(vt.getName())) {
                        return true;
                    }
                }
                for (Tree t : mt.getThrows()) {
                    if (t.getKind() == Tree.Kind.ERRONEOUS ||
                            (t.getKind() == Tree.Kind.IDENTIFIER && ERROR_IDENT.contentEquals(((IdentifierTree) t).getName()))) {
                        return true;
                    }
                }
                break;

            case VARIABLE:
                VariableTree vt = (VariableTree) leaf;
                return vt.getType().getKind() == Tree.Kind.ERRONEOUS
                        || ERROR_IDENT.contentEquals(vt.getName());

            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                ClassTree ct = (ClassTree) leaf;
                if (ERROR_IDENT.contentEquals(ct.getSimpleName())) {
                    return true;
                }
                for (TypeParameterTree tpt : ct.getTypeParameters()) {
                    if (ERROR_IDENT.contentEquals(tpt.getName())) {
                        return true;
                    }
                }
                break;

        }
        return false;
    }
        
    private static boolean isInHeader(CompilationInfo info, ClassTree tree, int offset) {
        CompilationUnitTree cut = info.getCompilationUnit();
        SourcePositions sp = info.getTrees().getSourcePositions();
        long lastKnownOffsetInHeader = sp.getStartPosition(cut, tree);
        
        List<? extends Tree> impls = tree.getImplementsClause();
        List<? extends TypeParameterTree> typeparams;
        if (impls != null && !impls.isEmpty()) {
            lastKnownOffsetInHeader= sp.getEndPosition(cut, impls.get(impls.size() - 1));
        } else if ((typeparams = tree.getTypeParameters()) != null && !typeparams.isEmpty()) {
            lastKnownOffsetInHeader= sp.getEndPosition(cut, typeparams.get(typeparams.size() - 1));
        } else if (tree.getExtendsClause() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getExtendsClause());
        } else if (tree.getModifiers() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getModifiers());
        }
        
        TokenSequence<JavaTokenId> ts = info.getTreeUtilities().tokensFor(tree);
        
        ts.move((int) lastKnownOffsetInHeader);
        
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.LBRACE) {
                return offset < ts.offset();
            }
        }
        
        return false;
    }
    
    private static boolean isInHeader(CompilationInfo info, MethodTree tree, int offset) {
        CompilationUnitTree cut = info.getCompilationUnit();
        SourcePositions sp = info.getTrees().getSourcePositions();
        long lastKnownOffsetInHeader = sp.getStartPosition(cut, tree);
        
        List<? extends ExpressionTree> throwz;
        List<? extends VariableTree> params;
        List<? extends TypeParameterTree> typeparams;
        
        if ((throwz = tree.getThrows()) != null && !throwz.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, throwz.get(throwz.size() - 1));
        } else if ((params = tree.getParameters()) != null && !params.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, params.get(params.size() - 1));
        } else if ((typeparams = tree.getTypeParameters()) != null && !typeparams.isEmpty()) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, typeparams.get(typeparams.size() - 1));
        } else if (tree.getReturnType() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getReturnType());
        } else if (tree.getModifiers() != null) {
            lastKnownOffsetInHeader = sp.getEndPosition(cut, tree.getModifiers());
        }
        
        TokenSequence<JavaTokenId> ts = info.getTreeUtilities().tokensFor(tree);
        
        ts.move((int) lastKnownOffsetInHeader);
        
        while (ts.moveNext()) {
            if (ts.token().id() == JavaTokenId.LBRACE || ts.token().id() == JavaTokenId.SEMICOLON) {
                return offset < ts.offset();
            }
        }
        
        return false;
    }
    
    public static Position[] createPositions(final Tree t, final CompilationInfo javac, final Document doc) throws BadLocationException {
        final Position[] poss = new Position[2];
        final int start = (int) javac.getTrees().getSourcePositions().
                getStartPosition(javac.getCompilationUnit(), t);
        final int end = (int) javac.getTrees().getSourcePositions().
                getEndPosition(javac.getCompilationUnit(), t);

        // XXX needs document lock?
        poss[0] = doc.createPosition(start);
        poss[1] = doc.createPosition(end);
        return poss;
    }

    /**
     * creates start and end positions of the tree
     */
    public static Position[] createSignaturePositions(final Tree t, final CompilationInfo javac, final Document doc) throws BadLocationException {
        final Position[] pos = new Position[2];
        final BadLocationException[] blex = new BadLocationException[1];
        doc.render(new Runnable() {
            public void run() {
                try {
                    int[] span = null;
                    if (t.getKind() == Tree.Kind.METHOD) { // method + constructor
                        span = javac.getTreeUtilities().findNameSpan((MethodTree) t);
                    } else if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                        span = javac.getTreeUtilities().findNameSpan((ClassTree) t);
                    } else if (Tree.Kind.VARIABLE == t.getKind()) {
                        span = javac.getTreeUtilities().findNameSpan((VariableTree) t);
                    }

                    if (span != null) {
                        pos[0] = doc.createPosition(span[0]);
                        pos[1] = doc.createPosition(span[1]);
                    }
                } catch (BadLocationException ex) {
                    blex[0] = ex;
                }
            }
        });
        if (blex[0] != null)
            throw (BadLocationException) new BadLocationException(blex[0].getMessage(), blex[0].offsetRequested()).initCause(blex[0]);
        return pos[0] != null ? pos : null;
    }

    public static SourceVersion resolveSourceVersion(FileObject file) {
        String sourceLevel = SourceLevelQuery.getSourceLevel(file);
        if (sourceLevel == null) {
            return SourceVersion.latest();
        } else if (sourceLevel.startsWith("1.6")) {
            return SourceVersion.RELEASE_6;
        } else if (sourceLevel.startsWith("1.5")) {
            return SourceVersion.RELEASE_5;
        } else if (sourceLevel.startsWith("1.4")) {
            return SourceVersion.RELEASE_4;
        } else if (sourceLevel.startsWith("1.3")) {
            return SourceVersion.RELEASE_3;
        } else if (sourceLevel.startsWith("1.2")) {
            return SourceVersion.RELEASE_2;
        } else if (sourceLevel.startsWith("1.1")) {
            return SourceVersion.RELEASE_1;
        } else if (sourceLevel.startsWith("1.0")) {
            return SourceVersion.RELEASE_0;
        }
        
        return SourceVersion.latest();
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
                if (this.name.equals(tag.name()) &&
                        this.text.equals(tag.text())) {
                    return tag;
                }
            }

            return null;
        }
        
        public int resolveIndex(Doc doc) {
            Tag[] tags = doc.tags();
            if (this.index < tags.length &&
                    this.name.equals(tags[this.index].name()) &&
                    this.text.equals(tags[this.index].text())) {
                return this.index;
            }
            
            // javadoc was changed
            for (int i = 0; i < tags.length; i++) {
                Tag tag = tags[i];
                if (this.name.equals(tag.name()) &&
                        this.text.equals(tag.text())) {
                    return i;
                }
            }

            return -1;
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
