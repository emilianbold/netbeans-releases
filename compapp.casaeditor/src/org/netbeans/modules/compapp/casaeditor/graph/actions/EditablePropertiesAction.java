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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author rdara
 */
public final class EditablePropertiesAction extends WidgetAction.Adapter {
    
    private Node mEditNode;
    
    
    public EditablePropertiesAction() {
    }
    
   public State mousePressed(Widget widget, WidgetMouseEvent event) {
        mEditNode = null;
        
        if (event.getButton () != MouseEvent.BUTTON1) {
            return State.REJECTED;
        }
        if(!(widget instanceof ImageWidget)) {
            return State.REJECTED;
        }
        ((ImageWidget) widget).setPaintAsDisabled(true);
        while(widget instanceof CasaNodeWidget) {
            widget = widget.getParentWidget();
        }
        CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
        CasaComponent casaComponent = (CasaComponent) scene.findObject(widget);
        if (casaComponent == null) {
            return State.REJECTED;
        }
        mEditNode =  scene.getNodeFactory().createNodeFor(casaComponent);
        if (mEditNode == null) {
            return State.REJECTED;
        }
        return State.CONSUMED;
    }
   
    public State mouseReleased(Widget widget, WidgetMouseEvent event) {
        if (mEditNode == null) {
            return State.REJECTED;
        }
        if(widget instanceof ImageWidget) {
            ((ImageWidget) widget).setPaintAsDisabled(false);
        }
        final PropertySheet propertySheetPanel = new PropertySheet();
        final Node editNodeRef = mEditNode;
        mEditNode = null;
        
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


}
