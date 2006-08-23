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

package org.netbeans.modules.cnd.completion.cplusplus;

import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.GotoDeclarationAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Open CC source according to the given expression.
 *
 * @author Vladimir Voskresensky
 * @version 1.0
 */

public class CCGoToDeclarationAction extends GotoDeclarationAction {
    
    private static CCGoToDeclarationAction instance;
    
    public CCGoToDeclarationAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public static synchronized CCGoToDeclarationAction getInstance(){
        if (instance == null){
            instance = new CCGoToDeclarationAction();
            String trimmedName = NbBundle.getBundle(CCGoToDeclarationAction.class).getString("goto-declaration-trimmed"); //NOI18N
            instance.putValue(org.netbeans.editor.ext.ExtKit.TRIMMED_TEXT, trimmedName);
            instance.putValue(BaseAction.POPUP_MENU_TEXT, trimmedName);
        }
        return instance;
    }
    
    public String getName() {
        return NbBundle.getBundle(CCGoToDeclarationAction.class).getString("NAME_GoToDeclarationAction"); // NOI18N
    }
    
    public boolean isEnabled() {
        /* If there are no paths in registry, the action shoud be disabled (#46632)*/
//        Set sources = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
//        Set compile = GlobalPathRegistry.getDefault().getPaths(ClassPath.COMPILE);
//        Set boot = GlobalPathRegistry.getDefault().getPaths(ClassPath.BOOT);
//        return !(sources.isEmpty() && compile.isEmpty() && boot.isEmpty());
        return true;
    }
    
    protected boolean asynchonous() {
        return false;
    }
    
    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
	final String taskName = "Go to declaration";
        Runnable run = new Runnable() {
            public void run() {
                if (SwingUtilities.isEventDispatchThread()) {
                    //RequestProcessor.getDefault().post(this);
		    CsmModelAccessor.getModel().enqueue(this, taskName);
                    return;
                }
                if (target != null) {
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    CsmHyperlinkProvider.goToDeclaration(doc, target, target.getCaret().getDot());
                }
            }
        };
        //RequestProcessor.getDefault().post(run);
	CsmModelAccessor.getModel().enqueue(run, taskName);
    }
}
