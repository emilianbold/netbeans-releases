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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.debugger.jpda.heapwalk.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.netbeans.modules.debugger.jpda.heapwalk.views.ClassesCountsView;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/** Opens classes counts TopComponent.
 *
 * @author   Martin Entlicher
 */
public class ClassesCountsViewAction extends AbstractAction {

    public ClassesCountsViewAction () {
        putValue (
            Action.NAME,
            "Classes Counts"
            /*NbBundle.getMessage (
                ClassesViewAction.class, 
                "CTL_ClassesAction"
            )*/
        );
        putValue (
            Action.SMALL_ICON, 
            new ImageIcon (Utilities.loadImage ("org/netbeans/modules/debugger/resources/classesView/Classes.png")) // NOI18N
        );
    }

    public void actionPerformed (ActionEvent evt) {
        if (activateComponent (ClassesCountsView.class)) return;
        ClassesCountsView v = new ClassesCountsView ();
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

