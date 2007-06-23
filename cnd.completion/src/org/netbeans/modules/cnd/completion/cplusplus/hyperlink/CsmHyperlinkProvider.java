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


package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.completion.impl.xref.ReferencesSupport;
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
        if ((token != null) && (token.getTokenID() == CCTokenContext.IDENTIFIER ||
                token.getTokenID() == CCTokenContext.OPERATOR)) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean goToDeclaration(BaseDocument doc, JTextComponent target, int offset) {
        if (!preJump(doc, target, offset, "opening-csm-element")) { //NOI18N
            return false;
        }
        Token jumpToken = getJumpToken();
        CsmOffsetable item = findTargetObject(target, doc, jumpToken, offset);
        return postJump(item, "goto_source_source_not_found", "cannot-open-csm-element"); //NOI18N
    }    

    /*package*/ CsmOffsetable findTargetObject(final JTextComponent target, final BaseDocument doc, final Token jumpToken, final int offset) {
        CsmOffsetable item = null;
        assert jumpToken != null;
        CsmFile file = CsmUtilities.getCsmFile(doc, true);
        CsmObject csmObject = file == null ? null : ReferencesSupport.findDeclaration(file, doc, jumpToken, offset);
        if (csmObject != null) {
            // convert to jump object
            item = toJumpObject(csmObject, file, offset);
        }
        return item;
    }
    
    private CsmOffsetable toJumpObject(CsmObject csmObject, CsmFile csmFile, int offset) {
        CsmOffsetable item = null;
        if (CsmKindUtilities.isOffsetable(csmObject)) {
            item = (CsmOffsetable)csmObject;
            if (CsmKindUtilities.isFunctionDeclaration(csmObject)) {
                // check if we are in function definition name => go to declaration
                // else it is more useful to jump to definition of function
                CsmFunctionDefinition definition = ((CsmFunction)csmObject).getDefinition();
                if (definition != null) {
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
        } else if (CsmKindUtilities.isNamespace(csmObject)) {
            // get all definitions of namespace, but prefer the definition in this file
            CsmNamespace nmsp = (CsmNamespace)csmObject;
            Collection<CsmNamespaceDefinition> defs = nmsp.getDefinitions();
            CsmNamespaceDefinition bestDef = null;
            for (CsmNamespaceDefinition def : defs) {
                if (bestDef == null) {
                    // first time initialization
                    bestDef = def;
                }
                CsmFile container = def.getContainingFile();
                if (csmFile.equals(container)) {
                    // this is the best choice
                    bestDef = def;
                    break;
                }
            }
            item = bestDef;
        }
        return item;
    }
}
