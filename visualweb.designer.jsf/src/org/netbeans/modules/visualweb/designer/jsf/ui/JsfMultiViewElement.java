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

import org.netbeans.modules.visualweb.api.designer.Designer;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;

/**
 * Implemenation of JSF multiview element.
 *
 * @author Peter Zavadsky
 */
public class JsfMultiViewElement implements MultiViewElement {

    private final Designer designer;

    /** Creates a new instance of DesignerMultiViewElement */
    public JsfMultiViewElement(Designer designer) {
        if (designer == null) {
            throw new NullPointerException("The designer parameter is null!"); // NOI18N
        }
        this.designer = designer;
    }

    public JComponent getVisualRepresentation() {
        return designer.getVisualRepresentation();
    }

    public JComponent getToolbarRepresentation() {
        return designer.getToolbarRepresentation();
    }

    public Action[] getActions() {
        return designer.getActions();
    }

    public Lookup getLookup() {
        return designer.getLookup();
    }

    public void componentOpened() {
        designer.componentOpened();
    }

    public void componentClosed() {
        designer.componentClosed();
    }

    public void componentShowing() {
        designer.componentShowing();
    }

    public void componentHidden() {
        designer.componentHidden();
    }

    public void componentActivated() {
        designer.componentActivated();
    }

    public void componentDeactivated() {
        designer.componentDeactivated();
    }

    public UndoRedo getUndoRedo() {
        return designer.getUndoRedo();
    }

    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback) {
        designer.setMultiViewCallback(multiViewElementCallback);
    }

    public CloseOperationState canCloseElement() {
        return designer.canCloseElement();
    }

}
