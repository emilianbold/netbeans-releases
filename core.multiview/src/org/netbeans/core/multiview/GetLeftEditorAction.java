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

package org.netbeans.core.multiview;

import java.awt.Toolkit;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

import java.beans.PropertyChangeListener;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.openide.windows.WindowManager;


/**
 *
 * @author  Milos Kleint
 */
public class GetLeftEditorAction extends AbstractAction {

    public GetLeftEditorAction() {
        putValue(Action.NAME, NbBundle.getMessage(GetLeftEditorAction.class, "GetLeftEditorAction.name"));
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        WindowManager wm = WindowManager.getDefault();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(wm.getRegistry().getActivated());
        if (handler != null) {
            MultiViewPerspective pers = handler.getSelectedPerspective();
            MultiViewPerspective[] all = handler.getPerspectives();
            for (int i = 0; i < all.length; i++) {
                if (pers.getDisplayName().equals(all[i].getDisplayName())) {
                    int newIndex = i != 0 ? i -1 : all.length - 1; 
                    handler.requestActive(all[newIndex]);
                }
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
}

