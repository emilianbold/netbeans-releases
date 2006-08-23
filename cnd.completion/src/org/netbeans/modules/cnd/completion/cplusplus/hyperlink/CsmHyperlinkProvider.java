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

import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmSyntaxSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.awt.Toolkit;
import java.text.MessageFormat;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Implementation of the hyperlink provider for java language.
 * <br>
 * The hyperlinks are constructed for identifiers.
 * <br>
 * The click action corresponds to performing the goto-declaration action.
 *
 * @author Jan Lahoda, Vladimir Voskresensky
 */
public final class CsmHyperlinkProvider implements HyperlinkProvider {
    
    private static final int TOKEN_LIMIT = 100;
    
    private static MessageFormat mf = null;
    
    /**
     * Creates a new instance of CsmHyperlinkProvider
     */
    public CsmHyperlinkProvider() {
        DefaultCaret caret = new DefaultCaret();
        caret.setMagicCaretPosition(null);
    }
    
    private static String findName(BaseDocument doc, int offset) {
        String name = "";
        SyntaxSupport sup = doc.getSyntaxSupport();
        NbCsmSyntaxSupport nbSyntaxSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
        boolean wasIdentifier = false;
        int tokenLimitCounter = 0;
        
        try {
            while (tokenLimitCounter < TOKEN_LIMIT) {
                TokenID token = nbSyntaxSup.getTokenID(offset);
                
                tokenLimitCounter++;
                if (token.getCategory() == CCTokenContext.KEYWORDS) {
                    return token.getName();
                } else if (token == CCTokenContext.IDENTIFIER && !wasIdentifier) {
                    int[] span = Utilities.getIdentifierBlock(doc, offset);
                    
                    name = doc.getText(span) + name;
                    offset = span[0] - 1;
                    
                    wasIdentifier = true;
                } else {
                    if (token == CCTokenContext.DOT) {
                        offset--;
                        name = "." + name; // NOI18N
                        wasIdentifier = false;
                    } else {
                        if (    token == CCTokenContext.WHITESPACE
                                || token == CCTokenContext.BLOCK_COMMENT
                                || token == CCTokenContext.LINE_COMMENT) {
                            offset--;
                        } else {
                            break;
                        }
                    }
                }
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return "<unknown>"; // NOI18N
        }
        
        return name;
    }
    
    public void performClickAction(Document originalDoc, final int offset) {
        if (!(originalDoc instanceof BaseDocument))
            return ;
        
        final BaseDocument doc = (BaseDocument) originalDoc;
        final JTextComponent target = Utilities.getFocusedComponent();
        
        if (target == null || target.getDocument() != doc)
            return ;
        
        final boolean findDefinition = false;
        
        Runnable run = new Runnable() {
            public void run() {
                goToDeclaration(doc, target, offset);
            }
        };
        //RequestProcessor.getDefault().post(run);
	CsmModelAccessor.getModel().enqueue(run, "Following hyperlink");
    }
    
    public static boolean goToDeclaration(BaseDocument doc, JTextComponent target, int offset) {
        if (doc == null || target == null || offset < 0 || offset > doc.getLength()) {
            return false;        
        }
        
        String name = findName((BaseDocument) doc, offset);
        String msg = NbBundle.getBundle(NbCsmSyntaxSupport.class).getString("opening-element"); //NOI18N
        msg = MessageFormat.format(msg, new Object [] { name });
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);//NOI18N

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
                    if (definition.getStartOffset() <= offset && 
                            offset <= definition.getBody().getStartOffset() ) {
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
            }
        }                    

        if (item == null || !CsmUtilities.openSource(item)) {
            //nothing found (item == null) or no source found (itemDesc != null)
            //inform user, that we were not able to open the resource.
            Toolkit.getDefaultToolkit().beep();
            String key;
            String itemDesc = CsmUtilities.getElementJumpName(item);
            if (itemDesc != null) {
                key = "goto_source_source_not_found"; // NOI18N
                name = itemDesc;
            } else {
                key = "cannot-open-element";// NOI18N
            }

            msg = NbBundle.getBundle(NbCsmSyntaxSupport.class).getString(key);

            org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { name } ));
            return false;
        }
        return true;
    }
    
    public boolean isHyperlinkPoint(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return false;
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            JTextComponent target = Utilities.getFocusedComponent();
            
            if (target == null || target.getDocument() != bdoc)
                return false;
            
            SyntaxSupport sup = bdoc.getSyntaxSupport();
            NbCsmSyntaxSupport nbSyntaxSup = (NbCsmSyntaxSupport)sup.get(NbCsmSyntaxSupport.class);
            
            TokenID token = nbSyntaxSup.getTokenID(offset);
            
            if (token == CCTokenContext.IDENTIFIER)
                return true;
            
            return false;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
    
    public int[] getHyperlinkSpan(Document doc, int offset) {
        if (!(doc instanceof BaseDocument))
            return null;
        
        try {
            if (isHyperlinkPoint(doc, offset))
                return Utilities.getIdentifierBlock((BaseDocument) doc, offset);
            else
                return null;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
}
