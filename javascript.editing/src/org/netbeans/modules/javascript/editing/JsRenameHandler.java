/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.api.InstantRenamer;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript.editing.lexer.JsCommentLexer;
import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * Handle instant rename for JavaScript
 * 
 * @author Tor Norbye
 */
public class JsRenameHandler implements InstantRenamer {

    public JsRenameHandler() {
    }

    public boolean isRenameAllowed(ParserResult info, int caretOffset, String[] explanationRetValue) {
        JsParseResult jspr = AstUtilities.getParseResult(info);
        if (jspr == null) {
            if (explanationRetValue != null) {
                explanationRetValue[0] = NbBundle.getMessage(JsRenameHandler.class, "NoRenameWithErrors");
            }

            return false;
        }

        int astOffset = AstUtilities.getAstOffset(info, caretOffset);
        if (astOffset == -1) {
            return false;
        }

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(info.getSnapshot(), astOffset);
        if (ts != null && ts.token().id() == JsTokenId.BLOCK_COMMENT) {
            TokenSequence<JsCommentTokenId> cts = ts.embedded(JsCommentTokenId.language());
            boolean canRename = false;
            if (cts != null) {
                canRename = getParameterName(cts, caretOffset) != null;
            }
            if (!canRename) {
                if (explanationRetValue != null) {
                    explanationRetValue[0] = NbBundle.getMessage(JsRenameHandler.class, "RenameOnlyParams");
                }
            }
            
            return canRename;
        }

        Node root = jspr.getRootNode();
        if(root == null) {
            //unparsable source
            return false;
        }

        AstPath path = new AstPath(root, astOffset);
        Node closest = path.leaf();

        switch (closest.getType()) {
            case Token.NAME:
            case Token.PARAMETER:
            case Token.BINDNAME:
            case Token.FUNCNAME:
                return true;
            case Token.OBJLITNAME:
                return AstUtilities.isLabelledFunction(closest);
            // TODO - block renaming of GLOBALS! I should already know
            // what's local and global based on JsSemantic...
        }

        return false;
    }

    public Set<OffsetRange> getRenameRegions(ParserResult info, int caretOffset) {
        JsParseResult rpr = AstUtilities.getParseResult(info);
        if (rpr == null) {
            return Collections.emptySet();
        }

        Node root = rpr.getRootNode();
        if (root == null) {
            return Collections.emptySet();
        }
        
        int astOffset = AstUtilities.getAstOffset(info, caretOffset);
        if (astOffset == -1) {
            return Collections.emptySet();
        }

        String parameterName = null;
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(info.getSnapshot(), astOffset);
        if (ts != null && ts.token().id() == JsTokenId.BLOCK_COMMENT) {
            TokenSequence<JsCommentTokenId> cts = ts.embedded(JsCommentTokenId.language());
            if (cts != null) {
                parameterName = getParameterName(cts, astOffset);
            } else {
                return Collections.emptySet();
            }
        }

        String name = parameterName;
        if (name != null) {
            assert ts != null;
            // Adjust the caret offset to find the associated function, then
            // find the associated parameter list and grab the first parameter
            // there as the offset.
            boolean seenFunction = false;
            boolean foundArg = false;
            while (ts.moveNext()) { // Done with the block comment
                org.netbeans.api.lexer.Token<? extends JsTokenId> token = ts.token();
                JsTokenId id = token.id();
                if (id == JsTokenId.FUNCTION) {
                    // Found the function
                    seenFunction = true;
                } else if (id == JsTokenId.LPAREN) {
                    // Found the parameter list.
                    assert seenFunction;
                    foundArg = true;
                    astOffset = ts.offset()+1;
                    break;
                } else if (id == JsTokenId.BLOCK_COMMENT) {
                    // Cannot skip another block comment - this is probably another function
                    break;
                }
            }
            if (!foundArg) {
                return Collections.emptySet();
            }
        }

        VariableVisitor v = rpr.getVariableVisitor();

        AstPath path = new AstPath(root, astOffset);
        Node leaf = path.leaf();
        if (name == null) {
            if (!(leaf instanceof Node.StringNode)) {
                return Collections.emptySet();
            }
            name = leaf.getString();
        }
        if (!name.equals(leaf.getString()) && parameterName != null) {
            // Probably renaming something from a document comment @param item
            // Find the corresponding node.
            List<Node> parameters = new ArrayList<Node>();
            Node func = AstUtilities.findLocalScope(leaf, path);
            AstUtilities.addNodesByType(func, new int[] { Token.PARAMETER }, parameters);
            for (Node parameter : parameters) {
                if (parameter.getString().equals(parameterName)) {
                    leaf = parameter;
                    break;
                }
            }
        }

        List<Node> nodes = v.getVarOccurrences(leaf);
        
        Set<OffsetRange> regions = new HashSet<OffsetRange>();
        Node parameterNode = null;
        if (nodes != null) {
            for (Node node : nodes) {
                regions.add(AstUtilities.getNameRange(node));
                if (node.getType() == Token.PARAMETER) {
                    parameterNode = node;
                }
            }
        }
        
        if (regions.size() > 0) {
            Set<OffsetRange> translated = new HashSet<OffsetRange>(2*regions.size());
            for (OffsetRange astRange : regions) {
                OffsetRange lexRange = LexUtilities.getLexerOffsets(rpr, astRange);
                if (lexRange != OffsetRange.NONE) {
                    translated.add(lexRange);
                }
            }

            regions = translated;
        }
        
        if (parameterNode != null) {
            // Look for @param too
            OffsetRange docRange = findParameterDoc(rpr, parameterNode, name);
            if (docRange != OffsetRange.NONE) {
                regions.add(docRange);
            }
        }
        
        return regions;
    }

    /** 
     * Look in the document to see if there's a comment, and if so, if there's
     * a parameter document for this item.
     */
    @NonNull
    private OffsetRange findParameterDoc(JsParseResult info, Node node, String name) {
        // Find function
        Node funcNode = node.getParentNode();
        while (funcNode != null && funcNode.getType() != Token.FUNCTION) {
            funcNode = funcNode.getParentNode();
        }
        if (funcNode == null) {
            return OffsetRange.NONE;
        }
        TokenSequence<? extends JsCommentTokenId> cts = AstUtilities.getCommentFor(info, funcNode);
        if (cts != null) {
            cts.moveStart();
            while (cts.moveNext()) {
                org.netbeans.api.lexer.Token<? extends JsCommentTokenId> token = cts.token();
                TokenId cid = token.id();
                if (cid == JsCommentTokenId.COMMENT_TAG) {
                    CharSequence text = token.text();
                     if (TokenUtilities.textEquals("@param", text)) { // NOI18N
                        int index = cts.index();
                        String paramType = JsCommentLexer.nextType(cts);
                        if (paramType == null) {
                            cts.moveIndex(index);
                            cts.moveNext();
                        }
                        String paramName = JsCommentLexer.nextIdent(cts);
                        if (paramName != null) {
                            if (name.equals(paramName)) {
                                // Figure out the offsets
                                int start = cts.offset();
                                return new OffsetRange(start, start+name.length());
                            }
                        } else {
                            cts.moveIndex(index);
                            cts.moveNext();
                        }
                    }
                }
            }
        }
        
        return OffsetRange.NONE;
    }

    @CheckForNull
    private String getParameterName(TokenSequence<? extends JsCommentTokenId> cts, int caretOffset) {
        assert cts != null;
        
        cts.move(caretOffset);
        if (!cts.moveNext() || cts.token().id() != JsCommentTokenId.IDENT) {
            return null;
        }
        cts.moveStart();
        while (cts.moveNext()) {
            org.netbeans.api.lexer.Token<? extends JsCommentTokenId> token = cts.token();
            TokenId cid = token.id();
            if (cid == JsCommentTokenId.COMMENT_TAG) {
                CharSequence text = token.text();
                 if (TokenUtilities.textEquals("@param", text)) { // NOI18N
                    int index = cts.index()+1;
                    String paramType = JsCommentLexer.nextType(cts);
                    if (paramType == null) {
                        cts.moveIndex(index);
                        cts.moveNext();
                    }
                    String paramName = JsCommentLexer.nextIdent(cts);
                    if (paramName != null) {
                        int start = cts.offset();
                        if (caretOffset >= start && caretOffset <= start+paramName.length()) {
                            return paramName;
                        }
                    } else {
                        cts.moveIndex(index);
                        cts.moveNext();
                    }
                }
            }
        }

        return null;
    }
}
