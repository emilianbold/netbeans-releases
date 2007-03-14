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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetBinding;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;



public class CasaBadgeEditAction extends WidgetAction.Adapter {
    
    private CasaModelGraphScene mScene;
    
    
    public CasaBadgeEditAction(CasaModelGraphScene scene) {
        mScene = scene;
    }
    
    public State mouseClicked(Widget widget, WidgetMouseEvent event) {
        
        CasaNodeWidgetBinding nodeWidget = (CasaNodeWidgetBinding) widget;
        Rectangle badgeBounds = nodeWidget.getEditBadgeBoundsForNode();
        if (!badgeBounds.contains(event.getPoint())) {
            return State.REJECTED;
        }
        
        CasaPort endpoint = (CasaPort) mScene.findObject(widget);
        if (endpoint == null || !mScene.getModel().isEditable(endpoint)) {
            return State.REJECTED;
        }
        
        Node node = mScene.getNodeFactory().createNodeFor(endpoint);
        if (node == null) {
            return State.REJECTED;
        }
        
        final PropertySheet propertySheetPanel = new PropertySheet();
        propertySheetPanel.setNodes(new Node[] { node });
        
        final DialogDescriptor descriptor = new DialogDescriptor(
                propertySheetPanel,
                "Test Foo",
                true,
                new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                }
            }
        });
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(new Dimension(500, 700));
        
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
