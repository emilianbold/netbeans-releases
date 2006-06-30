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


package org.netbeans.modules.debugger.jpda.ui.actions;


import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.netbeans.modules.debugger.jpda.ui.views.SourcesView;

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
    // TODO Rewrite this code - it creates all TopComponents !!!
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

