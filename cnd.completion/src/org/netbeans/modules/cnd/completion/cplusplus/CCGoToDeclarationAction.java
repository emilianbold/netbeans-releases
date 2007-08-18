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

package org.netbeans.modules.cnd.completion.cplusplus;

import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.GotoDeclarationAction;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmIncludeHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.completion.cplusplus.utils.TokenUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.openide.util.NbBundle;

/**
 * Open CC source according to the given expression.
 *
 * @author Vladimir Voskresensky
 * @version 1.0
 */
public class CCGoToDeclarationAction extends GotoDeclarationAction {

    static final long serialVersionUID = 1L;
    
    private static CCGoToDeclarationAction instance;

    public CCGoToDeclarationAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public static synchronized CCGoToDeclarationAction getInstance() {
        if (instance == null) {
            instance = new CCGoToDeclarationAction();
            String trimmedName = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-definition-declaration"); //NOI18N
            instance.putValue(org.netbeans.editor.ext.ExtKit.TRIMMED_TEXT, trimmedName);
            instance.putValue(BaseAction.POPUP_MENU_TEXT, trimmedName);
        }
        return instance;
    }

    public String getName() {
        return NbBundle.getBundle(CCGoToDeclarationAction.class).getString("NAME_GoToDeclarationAction"); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        /* If there are no paths in registry, the action shoud be disabled (#46632)*/
        //        Set sources = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        //        Set compile = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
        //        Set boot = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);
        //        return !(sources.isEmpty() && compile.isEmpty() && boot.isEmpty());
        final JTextComponent target = getFocusedComponent();
        if (target != null && (target.getDocument() instanceof BaseDocument)) {
            BaseDocument doc = (BaseDocument) target.getDocument();
            int offset = target.getCaret().getDot();
            offset = TokenUtilities.correctOffsetToID(doc, offset);
            // first try include provider
            if (new CsmIncludeHyperlinkProvider().isHyperlinkPoint(doc, offset)) {
                return true;
            } else if (new CsmHyperlinkProvider().isHyperlinkPoint(doc, offset)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean asynchonous() {
        return false;
    }

    @Override
    public boolean gotoDeclaration(final JTextComponent target) {
        final String taskName = "Go to declaration"; //NOI18N
        Runnable run = new Runnable() {

            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    //RequestProcessor.getDefault().post(this);
                    CsmModelAccessor.getModel().enqueue(this, taskName);
                    return;
                }
                if (target != null) {
                    BaseDocument doc = (BaseDocument) target.getDocument();
                    int offset = target.getCaret().getDot();
                    offset = TokenUtilities.correctOffsetToID(doc, offset);
                    // first try include provider
                    if (!new CsmIncludeHyperlinkProvider().goToInclude(doc, target, offset)) {
                        // if failed => try identifier provider
                        new CsmHyperlinkProvider().goToDeclaration(doc, target, offset);
                    }
                }
            }
        };
        //RequestProcessor.getDefault().post(run);
        CsmModelAccessor.getModel().enqueue(run, taskName);
        return false;
    }

    @Override
    public String getPopupMenuText(JTextComponent target) {
        String retValue;

        retValue = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-identifier-declaration");
        if (target != null) {
            BaseDocument doc = (BaseDocument) target.getDocument();
            int offset = target.getCaret().getDot();
            offset = TokenUtilities.correctOffsetToID(doc, offset);
            Token token = TokenUtilities.getToken(doc, offset);
            if (token != null) {
                if (CsmIncludeHyperlinkProvider.isSupportedToken(token)) {
                    retValue = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-included-file");
                } else if (CsmHyperlinkProvider.isSupportedToken(token)) {
                    // check if next token is '(' => it's possible to be functon
                    Token next = TokenUtilities.getToken(doc, token.getEndOffset());
                    if (next != null && next.getTokenID() == CCTokenContext.WHITESPACE) {
                        // try next one
                        next = TokenUtilities.getToken(doc, next.getEndOffset());
                    }
                    if (next != null && next.getTokenID() == CCTokenContext.LPAREN) {
                        // this is function call or function definition or function declaration
                        retValue = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-definition-declaration");
                    }
                }
            }
        }
        return retValue;
    }
}
