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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
 * Implementation of the hyperlink provider for C/C++ language.
 * <br>
 * The hyperlinks are constructed for identifiers.
 * <br>
 * The click action corresponds to performing the goto-declaration action.
 *
 * @author Jan Lahoda, Vladimir Voskresensky
 */
public final class CsmHyperlinkProvider extends CsmAbstractHyperlinkProvider {
    public CsmHyperlinkProvider() {
    }
    
    protected void performAction(final BaseDocument doc, final JTextComponent target, final int offset) {
        goToDeclaration(doc, target, offset);
    }
    
    protected boolean isValidToken(Token token) {
        if ((token != null) && (token.getTokenID() == CCTokenContext.IDENTIFIER)) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean goToDeclaration(BaseDocument doc, JTextComponent target, int offset) {
        if (!preJump(doc, target, offset, "opening-csm-element")) {
            return false;
        }
        CsmOffsetable item = null;
        CsmDeclaration declItem = null;
        // try with code completion engine
        CsmObject csmObject = CompletionUtilities.findItemAtCaretPos(target, offset);
        
        if (CsmKindUtilities.isOffsetable(csmObject)) {
            item = (CsmOffsetable)csmObject;
            if (CsmKindUtilities.isFunctionDeclaration(csmObject)) {
                // check if we are in function definition name => go to declaration
                // else it is more useful to jump to definition of function
                CsmFunctionDefinition definition = ((CsmFunction)csmObject).getDefinition();
                if (definition != null) {
                    CsmFile csmFile = CsmUtilities.getCsmFile(doc, true);
                    if (csmFile == definition.getContainingFile() &&
                            (definition.getStartOffset() <= offset &&
                            offset <= definition.getBody().getStartOffset())
                            ) {
                        // it is ok to jump to declaration
                        if (definition.getDeclaration() != null) {
                            item = definition.getDeclaration();
                        } else if (definition != csmObject) {
                            item = (CsmOffsetable)csmObject;
                        }
                    } else {
                        // it's better to jump to definition
                        item = definition;
                    }
                }
            } else if (CsmKindUtilities.isVariableDeclaration(csmObject)) {
                // check if we are in function definition name => go to declaration
                // else it is more useful to jump to definition of function
                CsmVariableDefinition definition = ((CsmVariable)csmObject).getDefinition();
                if (definition != null) {
                    item = definition;
                }
            }
        }
        return postJump(item, "goto_source_source_not_found", "cannot-open-csm-element");
    }    
}
