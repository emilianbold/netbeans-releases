/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
public class GetRightEditorAction extends AbstractAction {

    public GetRightEditorAction() {
        putValue(Action.NAME, NbBundle.getMessage(GetRightEditorAction.class, "GetRightEditorAction.name"));
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        WindowManager wm = WindowManager.getDefault();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(wm.getRegistry().getActivated());
        if (handler != null) {
            MultiViewPerspective pers = handler.getSelectedPerspective();
            MultiViewPerspective[] all = handler.getPerspectives();
            for (int i = 0; i < all.length; i++) {
                if (pers.equals(all[i])) {
                    int newIndex = i != all.length  - 1 ? i + 1 : 0; 
                    handler.requestActive(all[newIndex]);
                }
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    
    /** Overriden to share accelerator between instances of this action.
     */ 
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            GetLeftEditorAction.putSharedAccelerator("getRightEditor", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }

    /** Overriden to share accelerator between instances of this action.
     */ 
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return GetLeftEditorAction.getSharedAccelerator("getRightEditor"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }
    
}
