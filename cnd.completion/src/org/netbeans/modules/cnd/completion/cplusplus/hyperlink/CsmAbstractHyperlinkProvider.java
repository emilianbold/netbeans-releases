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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.awt.Toolkit;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.completion.cplusplus.NbCsmSyntaxSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;

/**
 * base hyperlink provider for Csm elements
 * @author Vladimir Voskresensky
 */
public abstract class CsmAbstractHyperlinkProvider implements HyperlinkProviderExt {

    private Token<CppTokenId> jumpToken = null;
    private Cancellable hyperLinkTask;
    protected CsmAbstractHyperlinkProvider() {
        DefaultCaret caret = new DefaultCaret();
        caret.setMagicCaretPosition(null);        
    }

    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    protected abstract void performAction(final Document originalDoc, final JTextComponent target, final int offset);

    public void performClickAction(Document originalDoc, final int offset, HyperlinkType type) {
        if (!(originalDoc instanceof Document))
            return ;
        
        final Document doc = (Document) originalDoc;
        final JTextComponent target = Utilities.getFocusedComponent();
        
        if (target == null || target.getDocument() != doc)
            return ;
        
        Runnable run = new Runnable() {
            public void run() {
                performAction(doc, target, offset);
            }
        };
        if (hyperLinkTask != null) {
            hyperLinkTask.cancel();
        }
        hyperLinkTask = CsmModelAccessor.getModel().enqueue(run, "Following hyperlink");// NOI18N
    }
    
    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        Token token = getToken(doc, offset);
        return isValidToken(token);
    }
    
    protected abstract boolean isValidToken(Token<CppTokenId> token);
    
    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        Token token = getToken(doc, offset);
        if (isValidToken(token)) {
            jumpToken = token;
            return new int[] {token.offset(null), token.offset(null) + token.length()};
        } else {
            return null;
        }
    }  
    
    protected boolean preJump(Document doc, JTextComponent target, int offset, String msgKey) {
        if (doc == null || target == null || offset < 0 || offset > doc.getLength()) {
            return false;
        }
        jumpToken = getToken(doc, offset);
        if (!isValidToken(jumpToken)) {
            return false;
        }
        String name = jumpToken.text().toString();
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
            if (itemDesc != null && itemDesc.length() > 0) {
                key = "goto_source_source_not_found"; // NOI18N
                name = itemDesc;
            } else {
                key = "cannot-open-csm-element";// NOI18N
                name = jumpToken.text().toString();
            }
            
            String msg = NbBundle.getBundle(NbCsmSyntaxSupport.class).getString(key);
            
            org.openide.awt.StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object [] { name } ));
            return false;
        }
        return true;        
    }
    
    protected Token getJumpToken() {
        return this.jumpToken;
    }

    static Token<CppTokenId> getToken(final Document doc, final int offset) {
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
            return CndTokenUtilities.getOffsetTokenCheckPrev(doc, offset);
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        }
    }

    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        if (doc == null || offset < 0 || offset > doc.getLength()) {
            return null;
        }
        
        Token token = jumpToken;
        if (token == null || token.offset(null) > offset || 
                (token.offset(null) + token.length()) < offset) {
            token = getToken(doc, offset);
        }        
        if (!isValidToken(token)) {
            return null;
        }
        return getTooltipText(doc, token, offset);
    }

    protected abstract String getTooltipText(Document doc, Token token, int offset);
}
