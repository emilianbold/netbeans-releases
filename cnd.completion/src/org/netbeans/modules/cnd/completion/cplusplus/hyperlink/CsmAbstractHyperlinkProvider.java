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

import java.awt.Toolkit;
import java.text.MessageFormat;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmSyntaxSupport;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.completion.cplusplus.utils.TokenUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.NbBundle;

/**
 * base hyperlink provider for Csm elements
 * @author Vladimir Voskresensky
 */
public abstract class CsmAbstractHyperlinkProvider implements HyperlinkProvider {

    protected CsmAbstractHyperlinkProvider() {
        DefaultCaret caret = new DefaultCaret();
        caret.setMagicCaretPosition(null);        
    }
    
    protected abstract void performAction(final BaseDocument originalDoc, final JTextComponent target, final int offset);

    public void performClickAction(Document originalDoc, final int offset) {
        if (!(originalDoc instanceof BaseDocument))
            return ;
        
        final BaseDocument doc = (BaseDocument) originalDoc;
        final JTextComponent target = Utilities.getFocusedComponent();
        
        if (target == null || target.getDocument() != doc)
            return ;
        
        Runnable run = new Runnable() {
            public void run() {
                performAction(doc, target, offset);
            }
        };
        CsmModelAccessor.getModel().enqueue(run, "Following hyperlink");
    }
    
    public boolean isHyperlinkPoint(Document doc, int offset) {
        Token token = getToken(doc, offset);
        return isValidToken(token);
    }
    
    protected abstract boolean isValidToken(Token token);
    
    public int[] getHyperlinkSpan(Document doc, int offset) {
        Token token = getToken(doc, offset);
        if (isValidToken(token)) {
            return new int[] {token.getStartOffset(), token.getEndOffset()};
        } else {
            return null;
        }
    }  
    
    protected Token getToken(Document doc, int offset) {
        return TokenUtilities.getToken(doc, offset);
    }
    
    private Token jumpToken = null;
    protected boolean preJump(BaseDocument doc, JTextComponent target, int offset, String msgKey) {
        if (doc == null || target == null || offset < 0 || offset > doc.getLength()) {
            return false;
        }
        jumpToken = TokenUtilities.getToken(doc, offset);
        if (!isValidToken(jumpToken)) {
            return false;
        }
        String name = jumpToken.getText();
        String msg = NbBundle.getBundle(NbCsmSyntaxSupport.class).getString(msgKey); //NOI18N
        msg = MessageFormat.format(msg, new Object [] { name });
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(msg);//NOI18N
        return true;
    }    
    
    protected boolean postJump(CsmOffsetable item, String existItemKey, String noItemKey) {
        if (jumpToken == null) {
            return false;
        }
        if (item == null || !CsmUtilities.openSource(item)) {
            //nothing found (item == null) or no source found (itemDesc != null)
            //inform user, that we were not able to open the resource.
            Toolkit.getDefaultToolkit().beep();
            String key;
            String name;
            String itemDesc = CsmUtilities.getElementJumpName(item);
            if (itemDesc != null) {
                key = "goto_source_source_not_found"; // NOI18N
                name = itemDesc;
            } else {
                key = "cannot-open-csm-element";// NOI18N
                name = jumpToken.getText();
            }
            
            String msg = NbBundle.getBundle(NbCsmSyntaxSupport.class).getString(key);
            
            org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { name } ));
            return false;
        }
        return true;        
    }
}
