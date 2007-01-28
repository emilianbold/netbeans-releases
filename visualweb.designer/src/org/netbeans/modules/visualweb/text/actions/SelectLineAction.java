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
/*
 * Created on Mar 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.netbeans.modules.visualweb.text.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.netbeans.modules.visualweb.text.DesignerPaneBase;


/*
 * Select the line around the caret
 * @see DefaultEditorKit#endAction
 * @see DefaultEditorKit#getActions
 */
public class SelectLineAction extends TextAction {
    private Action start;
    private Action end;

    /**
     * Create this action with the appropriate identifier.
     * @param nm  the name of the action, Action.NAME.
     * @param select whether to extend the selection when
     *  changing the caret position.
     */
    public SelectLineAction() {
        super(DesignerPaneBase.selectLineAction);
        start = new BeginLineAction("pigdog", false);
        end = new EndLineAction("pigdog", true);
    }

    /** The operation to perform when this action is triggered. */
    public void actionPerformed(ActionEvent e) {
        start.actionPerformed(e);
        end.actionPerformed(e);
    }
}
