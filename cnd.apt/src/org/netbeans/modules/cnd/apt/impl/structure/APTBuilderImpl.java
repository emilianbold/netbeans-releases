/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;
import java.util.Stack;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * implementation of APTBuilder
 * @author Vladimir Voskresensky
 */
public final class APTBuilderImpl {
    
    private boolean fullAPT; // NOT YET SUPPORTED
    /** Creates a new instance of APTBuilder */
    public APTBuilderImpl(boolean full) {
        this.fullAPT = full;
    }

    public APTFile buildAPT(String path, TokenStream ts) {
        if (ts == null) {
            return null;
        }
        APTFileNode aptFile = new APTFileNode(path);        
        try {
            buildFileAPT(aptFile, ts);
        } catch (TokenStreamRecognitionException ex) {
            // recognition exception is OK for uncompleted code
            // it's better for lexer not to fail at all
            // lexer should have special token for "everything else"
            // but for now we use reporting about problems to see, where lexer should be improved
            APTUtils.LOG.log(Level.SEVERE, "error on building APT\n {0}", new Object[] { ex }); // NOI18N
        } catch (TokenStreamException ex) {
            // it's better for lexer not to fail at all
            // lexer should have special token for "everything else"
            // but for now we use reporting about problems to see, where lexer should be improved
            APTUtils.LOG.log(Level.SEVERE, "error on converting token stream to text while building APT", ex); // NOI18N
            APTUtils.LOG.log(Level.SEVERE, "problem file is {0}", new Object[] { path }); // NOI18N
        }
        return aptFile;
    }
    
    public static APT buildAPTLight(APT apt) {
        assert (apt != null);
        assert (isRootNode(apt));
        APT outApt = createLightCopy(apt);
        APT node = nextRoot(apt);
        APT nodeLight = outApt;
        do {
            // get first child skipping not interested ones
            APT child = nextRoot(node.getFirstChild());
            // build light version for child subtree
            if (child != null) {
                APT childLight = buildAPTLight(child);
                assert (childLight != null);
                assert (isRootNode(childLight));
                nodeLight.setFirstChild(childLight);
            }
            // move to next sibling skipping not interested ones
            APT sibling = nextRoot(node.getNextSibling());
            node = sibling;
            if (sibling != null) {
                APT siblingLight = createLightCopy(sibling);
                assert (siblingLight != null);
                assert (isRootNode(siblingLight));
                nodeLight.setNextSibling(siblingLight);
                nodeLight = siblingLight;
            }
        } while (node != null);
        assert (outApt != null);
        assert (isRootNode(outApt));
        return outApt;
    }
    
    private void buildFileAPT(APTFileNode aptFile, TokenStream ts) throws TokenStreamException {
        Token lastToken;
        if (APTTraceFlags.APT_RECURSIVE_BUILD) {
            lastToken = addChildren(aptFile, ts.nextToken(), ts, false);
        } else {
            lastToken = build(aptFile, ts);
        }
        assert (APTUtils.isEOF(lastToken));
    }
    
    //////Build APT without recursion (a little bit faster, can be tuned even more)
    private Stack<APTBaseNode> nodeStack = new Stack<APTBaseNode>();
    
    private Token build(APTBaseNode root, TokenStream stream) throws TokenStreamException {
        assert(stream != null);
        APTBaseNode activeNode = null;
        Token nextToken = stream.nextToken();
        while (!APTUtils.isEOF(nextToken)) {
            if (activeNode == null) { // If we have no active node - create it
                if (root.getType() != APT.Type.CONDITION_CONTAINER) {
                    activeNode = createNode(nextToken);
                } else {
                    activeNode = createConditionChildNode(nextToken);
                }
                
                if (APTUtils.isEndCondition(nextToken)) {
                    assert (!nodeStack.empty()) : nextToken.getText() + " found without corresponding if: " + nextToken;
                    root = nodeStack.pop();
                    root.addChild(activeNode);
                    nextToken = stream.nextToken();
                    continue;
                }

                // TODO: need optimization of last access
                root.addChild(activeNode);

                if (activeNode.getType() == APT.Type.CONDITION_CONTAINER) {
                    assert(root.getType() != APT.Type.CONDITION_CONTAINER);
                    nodeStack.push(root);
                    root = activeNode;
                    activeNode = createConditionChildNode(nextToken);
                    root.addChild(activeNode);
                } 
                //We have created new node and can go to the next token
                nextToken = stream.nextToken();
            } else { //If active node is available - fill it with tokens
                if (!activeNode.accept(nextToken)) {
                    if (APTUtils.isEndDirectiveToken(nextToken.getType())) {
                        nextToken = stream.nextToken();
                    }
                    if (activeNode.getType() == APT.Type.ENDIF) {
                        assert (!nodeStack.empty()) : "endif found without corresponding if: " + nextToken;
                        root = nodeStack.pop();
                        activeNode = null;
                    } else if (root.getType() == APT.Type.CONDITION_CONTAINER) {
                        nodeStack.push(root);
                        root = activeNode;
                    } 
                    activeNode = null;
                } else {
                    nextToken = stream.nextToken();
                }
            }
        }
        return nextToken;
    }
    
    private Token addChildren(APTBaseNode root, Token nextToken, TokenStream stream, boolean breakOnEndBlockToken) throws TokenStreamException {
        assert(stream != null);
        APTBaseNode lastChild = null;
        while (!APTUtils.isEOF(nextToken)) {
            APTBaseNode newNode = null;
            if (breakOnEndBlockToken && APTUtils.isEndCondition(nextToken)) {
                return nextToken;
            } else {
                newNode = createNode(nextToken);
            }
            // add new node to children on the root
            if (lastChild == null) {
                root.setFirstChild(newNode);
            } else {
                lastChild.setNextSibling(newNode);
            }
            // remember last node for fast appending children as next sibling
            lastChild = newNode;  
            if (newNode.getType() == APT.Type.CONDITION_CONTAINER) {
                assert(root.getType() != APT.Type.CONDITION_CONTAINER);
                nextToken = initPreprocBranch((APTConditionsBlockNode)newNode, nextToken, stream);
            } else {
                // allow new node to initialize from token stream
                nextToken = initNode(newNode, stream.nextToken(), stream);
            }
        }
        return nextToken;
    }

    private Token initPreprocBranch(APTConditionsBlockNode root, Token nextToken, TokenStream stream) throws TokenStreamException {
        assert(stream != null);
        APTBaseNode lastChild = null;
        while (!APTUtils.isEOF(nextToken)) {
            APTBaseNode newNode = null;
            newNode = createConditionChildNode(nextToken);
            // add new node to children on the root
            if (lastChild == null) {
                root.setFirstChild(newNode);
            } else {
                lastChild.setNextSibling(newNode);
            }
            // remember last node for fast appending children as next sibling
            lastChild = newNode; 
            nextToken = initNode(newNode, stream.nextToken(), stream);          
            if (newNode.getType() == APT.Type.ENDIF) {
                // #endif means end of condition container
                return nextToken;                
            } else {
                nextToken = addChildren(newNode, nextToken, stream, true);
            }
        }
        return nextToken;
    }
    
    private Token initNode(APT node, Token nextToken, TokenStream stream) throws TokenStreamException {
        while (!APTUtils.isEOF(nextToken) && node.accept(nextToken)) {
            nextToken = stream.nextToken();
        }   
        if (APTUtils.isEndDirectiveToken(nextToken.getType())) {
            // eat it
            nextToken = stream.nextToken();
        }
        return nextToken;
    }
    
    private APTBaseNode createNode(Token token) {
        assert (!APTUtils.isEOF(token));
        int ttype = token.getType();
        APTBaseNode newNode = null;
        switch (ttype) {
            case APTTokenTypes.IF:
            case APTTokenTypes.IFDEF:
            case APTTokenTypes.IFNDEF:
                newNode = new APTConditionsBlockNode();
                break;
            case APTTokenTypes.INCLUDE:
                newNode = new APTIncludeNode(token);
                break;
            case APTTokenTypes.INCLUDE_NEXT:
                newNode = new APTIncludeNextNode(token);
                break;                
            case APTTokenTypes.ELIF:
                newNode = new APTElifNode(token);
                break;
            case APTTokenTypes.ELSE:
                newNode = new APTElseNode(token);
                break;
            case APTTokenTypes.ENDIF:
                newNode = new APTEndifNode(token);
                break;
            case APTTokenTypes.DEFINE:
                newNode = new APTDefineNode(token);
                break;
            case APTTokenTypes.UNDEF:
                newNode = new APTUndefineNode(token);
                break;
            case APTTokenTypes.ERROR:
		newNode = new APTErrorNode(token);
		break;
            case APTTokenTypes.PRAGMA:
            case APTTokenTypes.LINE:
            case APTTokenTypes.PREPROC_DIRECTIVE:                
                newNode = new APTUnknownNode(token);
                break;
            default:
                assert (!APTUtils.isPreprocessorToken(ttype)) : 
                    "all preprocessor tokens should be handled above"; // NOI18N
                newNode = new APTStreamNode(token);            
        }        
        assert (newNode != null);
        return newNode;
    }    

    private APTBaseNode createConditionChildNode(Token token) {
        assert (!APTUtils.isEOF(token));
        assert (APTUtils.isConditionsBlockToken(token)) : "Not conditional token found:" + token;
        int ttype = token.getType();
        APTBaseNode newNode = null;
        switch (ttype) {
            case APTTokenTypes.IF:
                newNode = new APTIfNode(token);
                break;
            case APTTokenTypes.IFDEF:
                newNode = new APTIfdefNode(token);
                break;
            case APTTokenTypes.IFNDEF:
                newNode = new APTIfndefNode(token);
                break; 
            case APTTokenTypes.ELIF:
                newNode = new APTElifNode(token);
                break;
            case APTTokenTypes.ELSE:
                newNode = new APTElseNode(token);
                break;
            case APTTokenTypes.ENDIF:
                newNode = new APTEndifNode(token);
                break;
            default:
                assert(false) : "unexpected " + ttype; // NOI18N                
        }
        assert (newNode != null);        
        return newNode;
    }     

    static private APT createLightCopy(APT apt) {
        assert (apt != null);
        assert (isRootNode(apt));        
        APT light = null;
        switch (apt.getType()) {
            case APT.Type.TOKEN_STREAM:
                break;
            case APT.Type.DEFINE:
                light = new APTDefineNode((APTDefineNode)apt);
                break;
            case APT.Type.UNDEF:
                light = new APTUndefineNode((APTUndefineNode)apt);
                break;
            case APT.Type.CONDITION_CONTAINER:
                light = new APTConditionsBlockNode((APTConditionsBlockNode)apt);
                break;
            case APT.Type.IFDEF:
                light = new APTIfdefNode((APTIfdefNode)apt);
                break;
            case APT.Type.IFNDEF:
                light = new APTIfndefNode((APTIfndefNode)apt);
                break;
            case APT.Type.IF:
                light = new APTIfNode((APTIfNode)apt);
                break;
            case APT.Type.ELIF:
                light = new APTElifNode((APTElifNode)apt);
                break;
            case APT.Type.ELSE:             
                light = new APTElseNode((APTElseNode)apt);
                break;
            case APT.Type.ENDIF:
                light = new APTEndifNode((APTEndifNode)apt);
                break;
            case APT.Type.INCLUDE:
                light = new APTIncludeNode((APTIncludeNode)apt);
                break;
            case APT.Type.INCLUDE_NEXT:
                light = new APTIncludeNextNode((APTIncludeNextNode)apt);
                break;
            case APT.Type.FILE:
                light = new APTFileNode((APTFileNode)apt);
                break;
            default:
                break;
        }
        return light;
    }  
    
    static private boolean isRootNode(APT apt) {
        switch (apt.getType()) {
            case APT.Type.CONDITION_CONTAINER:
            case APT.Type.IFDEF:
            case APT.Type.IFNDEF:
            case APT.Type.IF:
            case APT.Type.ELIF:
            case APT.Type.ELSE:    
            case APT.Type.ENDIF:
            case APT.Type.INCLUDE:
            case APT.Type.INCLUDE_NEXT:
            case APT.Type.FILE:
            case APT.Type.DEFINE:
            case APT.Type.UNDEF:
                return true;
        }
        return false;        
    }

    static private APT nextRoot(APT apt) {
        APT node = apt;
        while (node != null) {
            if (isRootNode(node)) {
                return node;
            }
            node = node.getNextSibling();
        }
        return null;
    }
}
