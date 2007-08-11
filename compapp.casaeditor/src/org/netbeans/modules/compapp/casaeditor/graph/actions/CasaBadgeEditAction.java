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

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaBindingBadges;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetBinding;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;



public class CasaBadgeEditAction extends WidgetAction.Adapter {
    
    private CasaModelGraphScene mScene;
    private Node mEditNode;
    
    
    public CasaBadgeEditAction(CasaModelGraphScene scene) {
        mScene = scene;
    }
    
    public State mousePressed(Widget widget, WidgetMouseEvent event) {
        mEditNode = null;
        
        if (event.getButton () != MouseEvent.BUTTON1) {
            return State.REJECTED;
        }
        
        CasaNodeWidgetBinding nodeWidget = (CasaNodeWidgetBinding) widget;
        Rectangle badgeBounds = nodeWidget.getBadges().getBadgeBoundsForParent(
                CasaBindingBadges.Badge.IS_EDITABLE, 
                nodeWidget);
        if (!badgeBounds.contains(event.getPoint())) {
            return State.REJECTED;
        }
        
        CasaPort endpoint = (CasaPort) mScene.findObject(widget);
        if (endpoint == null || !mScene.getModel().isEditable(endpoint)) {
            return State.REJECTED;
        }
        
        mEditNode = mScene.getNodeFactory().createNodeFor(endpoint);
        if (mEditNode == null) {
            return State.REJECTED;
        }
        
        nodeWidget.getBadges().setBadgePressed(CasaBindingBadges.Badge.IS_EDITABLE, true);
        
        return State.CONSUMED;
    }

    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    public WidgetAction.State dragExit(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        return State.createLocked(widget, this);
    }
    
    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    public WidgetAction.State mouseExited(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        return State.createLocked(widget, this);
    }
    
    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    public WidgetAction.State mouseDragged(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        return State.createLocked (widget, this);
    }
    
    protected boolean isLocked() {
        return mEditNode != null;
    }
    
    public State mouseReleased(Widget widget, WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        
        final PropertySheet propertySheetPanel = new PropertySheet();
        final Node editNodeRef = mEditNode;
        mEditNode = null;
        
        CasaNodeWidgetBinding nodeWidget = (CasaNodeWidgetBinding) widget;
        nodeWidget.getBadges().setBadgePressed(CasaBindingBadges.Badge.IS_EDITABLE, false);
        
        propertySheetPanel.setNodes(new Node[] { editNodeRef });

        final Object[] options = new Object[] {Constants.CLOSE};
        final DialogDescriptor descriptor = new DialogDescriptor(
                propertySheetPanel,
                NbBundle.getMessage(getClass(), "STR_PROPERTIES", editNodeRef.getDisplayName()),
                true,
                options,
                null, 
                DialogDescriptor.DEFAULT_ALIGN, 
                null,
                null); 
        descriptor.setClosingOptions(options);
                
        
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        
        // The dialog is modal, allow the action chain to continue while
        // we open the dialog later.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dlg.setVisible(true);
            }
        });
        
        return State.CONSUMED;
    }
}
