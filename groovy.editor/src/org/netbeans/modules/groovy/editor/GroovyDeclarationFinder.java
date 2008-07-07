/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.groovy.editor;

import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.elements.IndexedClass;
import org.netbeans.modules.groovy.editor.elements.IndexedElement;
import org.netbeans.modules.groovy.editor.elements.IndexedMethod;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;
import org.netbeans.modules.gsf.api.NameKind;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author schmidtm
 */
public class GroovyDeclarationFinder implements DeclarationFinder{

    private final Logger LOG = Logger.getLogger(GroovyDeclarationFinder.class.getName());
    Token<? extends GroovyTokenId> tok;
    
    Document lastDoc = null;
    int lastOffset = -1;
    OffsetRange lastRange = OffsetRange.NONE;
    
    public GroovyDeclarationFinder() {
        // LOG.setLevel(Level.FINEST);
    }

    public OffsetRange getReferenceSpan(Document document, int lexOffset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);

        //BaseDocument doc = (BaseDocument)document;

        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(th, lexOffset);

        if (ts == null) {
            return OffsetRange.NONE;
        }

        ts.move(lexOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return OffsetRange.NONE;
        }

        // Determine whether the caret position is right between two tokens
        boolean isBetween = (lexOffset == ts.offset());

        OffsetRange range = getReferenceSpan(ts, th, lexOffset);

        if ((range == OffsetRange.NONE) && isBetween) {
            // The caret is between two tokens, and the token on the right
            // wasn't linkable. Try on the left instead.
            if (ts.movePrevious()) {
                range = getReferenceSpan(ts, th, lexOffset);
            }
        }

        System.out.println("### RANGE " + range);

        return range;
    }

    public DeclarationLocation findDeclaration(CompilationInfo info, int lexOffset) {
        try {
            Document document = info.getDocument();
            if (document == null) {
                return DeclarationLocation.NONE;
            }
            TokenHierarchy<Document> th = TokenHierarchy.get(document);
            BaseDocument doc = (BaseDocument)document;

            int astOffset = AstUtilities.getAstOffset(info, lexOffset);
            if (astOffset == -1) {
                return DeclarationLocation.NONE;
            }

            OffsetRange range = getReferenceSpan(doc, lexOffset);

            if (range == OffsetRange.NONE) {
                return DeclarationLocation.NONE;
            }

            // Determine the bias (if the caret is between two tokens, did we
            // click on a link for the left or the right?
            boolean leftSide = range.getEnd() <= lexOffset;

            ASTNode root = AstUtilities.getRoot(info);

            if (root == null) {
                // No parse tree - try to just use the syntax info to do a simple index lookup
                // for methods and classes
                String text = doc.getText(range.getStart(), range.getLength());
                GroovyIndex index = new GroovyIndex(info.getIndex(GroovyTokenId.GROOVY_MIME_TYPE));

                if ((index == null) || (text.length() == 0)) {
                    return DeclarationLocation.NONE;
                }

                if (Character.isUpperCase(text.charAt(0))) {
                    // A Class or Constant?
                    Set<IndexedClass> classes =
                        index.getClasses(text, NameKind.EXACT_NAME, true, false, false);

                    if (classes.size() == 0) {
                        return DeclarationLocation.NONE;
                    }

                    DeclarationLocation l = getClassDeclaration(info, classes, null, null, index, doc);
                    if (l != null) {
                        return l;
                    }
                } else {
                    // A method?
                    Set<IndexedMethod> methods =
                        index.getMethods(text, null, NameKind.EXACT_NAME, GroovyIndex.ALL_SCOPE);

                    if (methods.size() == 0) {
                        methods = index.getMethods(text, null, NameKind.EXACT_NAME);
                    }

                    DeclarationLocation l = getMethodDeclaration(info, text, methods,
                         null, null, index, astOffset, lexOffset);

                    if (l != null) {
                        return l;
                    }
                } // TODO: @ - field?

                return DeclarationLocation.NONE;
            }

            GroovyIndex index = new GroovyIndex(info.getIndex(GroovyTokenId.GROOVY_MIME_TYPE));

            int tokenOffset = lexOffset;

            if (leftSide && (tokenOffset > 0)) {
                tokenOffset--;
            }

            AstPath path = new AstPath(root, astOffset, doc);
            ASTNode closest = path.leaf();

            // Look at the parse tree; find the closest node and jump based on the context
//            if (closest instanceof FieldNode) {
//                String name = ((FieldNode)closest).getName();
//                ASTNode method = AstUtilities.findLocalScope(closest, path);
//
//                return fix(findLocal(info, method, name), info);
//            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
        return DeclarationLocation.NONE;
    }

    private OffsetRange getReferenceSpan(TokenSequence<?> ts, TokenHierarchy<Document> th, int lexOffset) {
        Token<?> token = ts.token();
        TokenId id = token.id();

        if (id == GroovyTokenId.IDENTIFIER) {
            if (token.length() == 1 && id == GroovyTokenId.IDENTIFIER && token.text().toString().equals(",")) {
                assert false : "Never planned to be here";
                return OffsetRange.NONE;
            }
        }

        // TODO: Tokens.SUPER, Tokens.THIS, Tokens.SELF ...
        if (id == GroovyTokenId.IDENTIFIER) {
            return new OffsetRange(ts.offset(), ts.offset() + token.length());
        }

        return OffsetRange.NONE;
    }

    private DeclarationLocation getClassDeclaration(CompilationInfo info, Set<IndexedClass> classes,
            AstPath path, ASTNode closest, GroovyIndex index, BaseDocument doc) {
        final IndexedClass candidate =
            findBestClassMatch(classes, path, closest, index);

        if (candidate != null) {
            IndexedElement com = candidate;
            ASTNode node = AstUtilities.getForeignNode(com, null);

            DeclarationLocation loc = new DeclarationLocation(com.getFile().getFileObject(),
                AstUtilities.getOffset(doc, node.getLineNumber(), node.getColumnNumber()), com);

            return loc;
        }

        return DeclarationLocation.NONE;
    }

    private DeclarationLocation getMethodDeclaration(CompilationInfo info, String name, Set<IndexedMethod> methods,
            AstPath path, ASTNode closest, GroovyIndex index, int astOffset, int lexOffset) {
        BaseDocument doc = (BaseDocument)info.getDocument();
        if (doc == null) {
            return DeclarationLocation.NONE;
        }

        IndexedMethod candidate =
            findBestMethodMatch(name, methods, doc,
                astOffset, lexOffset, path, closest, index);

        if (candidate != null) {
            FileObject fileObject = candidate.getFile().getFileObject();
            if (fileObject == null) {
                return DeclarationLocation.NONE;
            }

            ASTNode node = AstUtilities.getForeignNode(candidate, null);
            int nodeOffset = node != null ? AstUtilities.getOffset(doc, node.getLineNumber(), node.getColumnNumber()) : 0;

            DeclarationLocation loc = new DeclarationLocation(
                fileObject, nodeOffset, candidate);

            return loc;
        }

        return DeclarationLocation.NONE;
    }

    IndexedClass findBestClassMatch(Set<IndexedClass> classSet,
        AstPath path, ASTNode reference, GroovyIndex index) {
        // Make sure that the best fit method actually has a corresponding valid source location
        // and parse tree
        Set<IndexedClass> classes = new HashSet<IndexedClass>(classSet);

        while (!classes.isEmpty()) {
            IndexedClass clz = findBestClassMatchHelper(classes, path, reference, index);
            if (clz == null) {
                return null;
            }
            ASTNode node = AstUtilities.getForeignNode(clz, null);

            if (node != null) {
                return clz;
            }

            // TODO: Sort results, then pick candidate number modulo methodSelector
            if (!classes.contains(clz)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best class and we keep trying it
                classes.remove(classes.iterator().next());
            } else {
                classes.remove(clz);
            }
        }

        return null;
    }

    // Now that I have a common RubyObject superclass, can I combine this and findBestMethodMatchHelper
    // since there's a lot of code duplication that could be shared by just operating on RubyObjects ?
    private IndexedClass findBestClassMatchHelper(Set<IndexedClass> classes,
        AstPath path, ASTNode reference, GroovyIndex index) {
        return null;
    }

    IndexedMethod findBestMethodMatch(String name, Set<IndexedMethod> methodSet,
        BaseDocument doc, int astOffset, int lexOffset, AstPath path, ASTNode call, GroovyIndex index) {
        // Make sure that the best fit method actually has a corresponding valid source location
        // and parse tree

        Set<IndexedMethod> methods = new HashSet<IndexedMethod>(methodSet);

        while (!methods.isEmpty()) {
            IndexedMethod method =
                findBestMethodMatchHelper(name, methods, doc, astOffset, lexOffset, path, call, index);
            if (methods == null) {
                return null;
            }
            ASTNode node = AstUtilities.getForeignNode(method, null);

            if (node != null) {
                return method;
            }

            if (!methods.contains(method)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best method and we keep trying it
                methods.remove(methods.iterator().next());
            } else {
                methods.remove(method);
            }
        }

        // Dynamic methods that don't have source (such as the TableDefinition methods "binary", "boolean", etc.
        if (methodSet.size() > 0) {
            return methodSet.iterator().next();
        }

        return null;
    }

    private IndexedMethod findBestMethodMatchHelper(String name, Set<IndexedMethod> methods,
        BaseDocument doc, int astOffset, int lexOffset, AstPath path, ASTNode callNode, GroovyIndex index) {
        return null;
    }

    private DeclarationLocation fix(DeclarationLocation location, CompilationInfo info) {
        if ((location != DeclarationLocation.NONE) && (location.getFileObject() == null) &&
                (location.getUrl() == null)) {
            return new DeclarationLocation(info.getFileObject(), location.getOffset(), location.getElement());
        }

        return location;
    }

}
