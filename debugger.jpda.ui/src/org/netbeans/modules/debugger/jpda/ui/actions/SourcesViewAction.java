/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.debugger.jpda.ui.actions;


import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.netbeans.modules.debugger.jpda.ui.views.SourcesView;
import org.netbeans.modules.debugger.ui.views.CallStackView;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/** 
 * Opens Call Stack TopComponent.
 *
 * @author Jan Jancura
 */
public class SourcesViewAction extends AbstractAction {

    public SourcesViewAction () {
        putValue (
            Action.NAME, 
            NbBundle.getMessage (
                SourcesViewAction.class, 
                "CTL_SourcesViewAction"
            )
        );
        putValue (
            Action.SMALL_ICON, 
            new ImageIcon (Utilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/root.gif")) // NOI18N
        );
    }

    public void actionPerformed (ActionEvent evt) {
        if (activateComponent (SourcesView.class)) return;
        SourcesView v = new SourcesView ();
        v.open ();
        v.requestActive ();
    }
    
    static boolean activateComponent (Class componentClass) {
        Iterator it = WindowManager.getDefault ().getModes ().iterator ();
        while (it.hasNext ()) {
            Mode m = (Mode) it.next ();
            TopComponent[] tcs = m.getTopComponents ();
            int i, k = tcs.length;
            for (i = 0; i < k; i++)
                if (tcs [i].getClass ().equals (componentClass)) {
                    tcs [i].open ();
                    tcs [i].requestActive ();
                    return true;
                }
        }
        return false;
    }
}

