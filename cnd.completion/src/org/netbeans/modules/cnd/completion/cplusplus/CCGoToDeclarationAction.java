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
package org.netbeans.modules.cnd.completion.cplusplus;

import java.awt.event.ActionEvent;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmIncludeHyperlinkProvider;
import org.openide.util.NbBundle;

/**
 * Open CC source according to the given expression.
 *
 * @author Vladimir Voskresensky
 * @version 1.0
 */
@EditorActionRegistrations({
    @EditorActionRegistration(
        name = "goto-declaration",
        menuPath = "GoTo",
        menuPosition = 900,
        menuText = "#goto-identifier-declaration"
    )
})
public class CCGoToDeclarationAction extends BaseAction {

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
            int offset = target.getSelectionStart();
            // first try include provider
            if (new CsmIncludeHyperlinkProvider().isHyperlinkPoint(doc, offset, HyperlinkType.GO_TO_DECLARATION)) {
                return true;
            } else if (new CsmHyperlinkProvider().isHyperlinkPoint(doc, offset, HyperlinkType.GO_TO_DECLARATION)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean asynchonous() {
        return false;
    }

    public boolean gotoDeclaration(final JTextComponent target) {
        final String taskName = "Go to declaration"; //NOI18N
        Runnable run = new Runnable() {

            public void run() {
                if (target != null && (target.getDocument() instanceof BaseDocument)) {
                    BaseDocument doc = (BaseDocument) target.getDocument();
                    int offset = target.getSelectionStart();
                    // first try include provider
                    if (!new CsmIncludeHyperlinkProvider().goToInclude(doc, target, offset, HyperlinkType.GO_TO_DECLARATION)) {
                        // if failed => try identifier provider
                        new CsmHyperlinkProvider().goToDeclaration(doc, target, offset, HyperlinkType.GO_TO_DECLARATION);
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
        if (target != null && (target.getDocument() instanceof BaseDocument)) {
            BaseDocument doc = (BaseDocument) target.getDocument();
            int offset = target.getSelectionStart();
            // don't need to lock document because we are in EQ
            TokenItem<CppTokenId> token = CndTokenUtilities.getTokenCheckPrev(doc, offset);
            if (token != null) {
                if (CsmIncludeHyperlinkProvider.isSupportedToken(token, HyperlinkType.GO_TO_DECLARATION)) {
                    retValue = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-included-file");
//                } else if (CsmHyperlinkProvider.isSupportedToken(token)) {
//                    // check if next token is '(' => it's possible to be functon
//                    Token next = TokenUtilities.getToken(doc, token.getEndOffset());
//                    if (next != null && next.getTokenID() == CCTokenContext.WHITESPACE) {
//                        // try next one
//                        next = TokenUtilities.getToken(doc, next.getEndOffset());
//                    }
//                    if (next != null && next.getTokenID() == CCTokenContext.LPAREN) {
//                        // this is function call or function definition or function declaration
//                        retValue = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-definition-declaration");
//                    }
                }
            }
        }
        return retValue;
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        gotoDeclaration(target);
    }
}
