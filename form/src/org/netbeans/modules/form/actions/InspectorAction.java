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


package org.netbeans.modules.form.actions;


import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.netbeans.modules.form.ComponentInspector;

import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** Opens Inspector (Component Inspector) TopComponent.
 *
 * @author   Peter Zavadsky
 */
public class InspectorAction extends AbstractAction {

    public InspectorAction() {
        putValue(NAME, NbBundle.getMessage(InspectorAction.class, "CTL_InspectorAction"));
        putValue(SMALL_ICON, new ImageIcon(
            Utilities.loadImage("org/netbeans/modules/form/resources/inspector.gif"))); // NOI18N
    }


    /** Opens component inspector (Form structure) component. */
    public void actionPerformed(ActionEvent evt) {
        // show ComponentInspector
        ComponentInspector.getInstance().open();
    }
    
}

