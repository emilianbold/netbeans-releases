/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.actions.RenameAction;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter.Menu;
import org.openide.util.actions.Presenter.Popup;

/** Action that displays refactoring submenu action on JavaDataObject
 * and delegates to it.
 *
 * @author Martin Matula, Jan Becicka
 */
public class RSMDataObjectAction extends SystemAction implements Menu, Popup, ContextAwareAction {
    // create delegate (RefactoringSubMenuAction)
    private final RefactoringSubMenuAction action = new RefactoringSubMenuAction(false);
    
    private static final RenameAction renameAction = (RenameAction) SystemAction.get(RenameAction.class);
    
    public void actionPerformed(ActionEvent ev) {
        // do nothing -- should never be called
    }
    
    public String getName() {
        return (String) action.getValue(Action.NAME);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(RSMEditorActionAction.class);
    }
    
    public JMenuItem getMenuPresenter() {
        return action.getMenuPresenter();
    }
    
    public JMenuItem getPopupPresenter() {
        return action.getPopupPresenter();
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        DataObject dobj = (DataObject) actionContext.lookup(DataObject.class);
        while (dobj instanceof DataShadow) {
            dobj = ((DataShadow) dobj).getOriginal();
        }
        
        boolean isRecursive = !(actionContext.lookup(NonRecursiveFolder.class) != null);
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        if (a.isEnabled()) {
            return this;
        } else {
            return renameAction.createContextAwareInstance(actionContext);
        }
    }
}
    
