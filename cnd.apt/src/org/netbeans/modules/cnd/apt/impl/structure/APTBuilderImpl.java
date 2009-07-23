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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamRecognitionException;
import java.util.LinkedList;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * implementation of APTBuilder
 * @author Vladimir Voskresensky
 */
public final class APTBuilderImpl {
    
    /** Creates a new instance of APTBuilder */
    public APTBuilderImpl() {
    }

    public APTFile buildAPT(CharSequence path, TokenStream ts) {
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
            APTUtils.LOG.log(Level.SEVERE, "error on converting token stream to text while building APT\n{0}", new Object[] { ex }); // NOI18N
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
        APTToken lastToken = nonRecursiveBuild(aptFile, ts);
        assert (APTUtils.isEOF(lastToken));
    }

    private static final class Pair {
        final APTBaseNode active;
        APTBaseNode lastChild;
        Pair(APTBaseNode activeNode) {
            active = activeNode;
        }
        void addChild(APTBaseNode newChild) {
            if (lastChild == null) {
                active.setFirstChild(newChild);
            } else {
                lastChild.setNextSibling(newChild);
            }
            lastChild = newChild;
        }

        @Override
        public String toString() {
            return "active:" + active + " lastChild:" + lastChild; // NOI18N
        }
    }
    //////Build APT without recursion (a little bit faster, can be tuned even more)
    private LinkedList<Pair> nodeStack = new LinkedList<Pair>();
    
    private APTToken nonRecursiveBuild(APTFileNode aptFile, TokenStream stream) throws TokenStreamException {
        assert(stream != null);
        Pair root = new Pair(aptFile);
        APTToken nextToken = (APTToken) stream.nextToken();
        while (!APTUtils.isEOF(nextToken)) {
            APTBaseNode activeNode = createNode(nextToken);
            nextToken = initNode(aptFile, activeNode, (APTToken) stream.nextToken(), stream);
            if (APTUtils.isEndConditionNode(activeNode.getType())) {
                if (!nodeStack.isEmpty()) {
                    root = nodeStack.removeLast();
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: {2} without corresponding #if\n", new Object[] { APTTraceUtils.toFileString(aptFile), nextToken.getLine(), nextToken.getText() }); // NOI18N
                }
            }
            root.addChild(activeNode);
            if (APTUtils.isStartOrSwitchConditionNode(activeNode.getType())) {
                nodeStack.addLast(root);
                root = new Pair(activeNode);
            }
        }
        for (Pair pair : nodeStack) {
            APTToken token = pair.active.getToken();
            APTUtils.LOG.log(Level.SEVERE, "{0}, line {1}: {2} without closing #endif\n", new Object[]{APTTraceUtils.toFileString(aptFile), token.getLine(), token.getText()}); // NOI18N
        }
        return nextToken;
    }
    
    private APTToken initNode(APTFileNode aptFile, APT node, APTToken nextToken, TokenStream stream) throws TokenStreamException {
        while (!APTUtils.isEOF(nextToken) && node.accept(aptFile, nextToken)) {
            nextToken = (APTToken) stream.nextToken();
        }   
        if (APTUtils.isEndDirectiveToken(nextToken.getType())) {
            // eat it
            nextToken = (APTToken) stream.nextToken();
        }
        return nextToken;
    }
    
    private APTBaseNode createNode(APTToken token) {
        assert (!APTUtils.isEOF(token));
        int ttype = token.getType();
        APTBaseNode newNode;
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

    public static APT createLightCopy(APT apt) {
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
            case APT.Type.ERROR:
                light = new APTErrorNode((APTErrorNode) apt);
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
            case APT.Type.ERROR:
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
