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

package org.netbeans.modules.visualweb.designer.jsf.ui;

import java.awt.SystemColor;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * XXX #6488351 MultiViewElement used for the cases when the <code>Designer</code>
 * can't be created because <code>FacesModel</code> is not available.
 *
 * @author Peter Zavadsky
 */
public class NotAvailableMultiViewElement implements MultiViewElement {

    /** Creates a new instance of NotAvailableMultiViewElement */
    public NotAvailableMultiViewElement() {
    }

    public JComponent getVisualRepresentation() {
        JLabel label = new JLabel(NbBundle.getMessage(NotAvailableMultiViewElement.class, "LBL_NotAvailable"), JLabel.CENTER);
        label.setForeground(SystemColor.textInactiveText);
        return label;
    }

    public JComponent getToolbarRepresentation() {
        return new JLabel();
    }

    public Action[] getActions() {
        return new Action[0];
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    public void componentOpened() {
    }

    public void componentClosed() {
    }

    public void componentShowing() {
    }

    public void componentHidden() {
    }

    public void componentActivated() {
    }

    public void componentDeactivated() {
    }

    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback) {
    }

    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

}
