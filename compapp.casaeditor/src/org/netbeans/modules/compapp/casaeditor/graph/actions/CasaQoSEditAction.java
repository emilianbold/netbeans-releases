/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.nodes.ConnectionNode;
import org.netbeans.modules.compapp.casaeditor.nodes.ExtensionPropertyHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 * @author jqian
 */
public class CasaQoSEditAction extends WidgetAction.Adapter {

    private CasaModelGraphScene mScene;

    public CasaQoSEditAction(CasaModelGraphScene scene) {
        mScene = scene;
    }

    @Override
    public State mousePressed(Widget widget, WidgetMouseEvent event) {

        if (event.getButton() != MouseEvent.BUTTON1) {
            return State.REJECTED;
        }

        ((ImageWidget) widget).setPaintAsDisabled(true); 
        
        // make sure the underlying connection gets selected
        event.setPoint(new Point(8, 5));
        return State.REJECTED;
    }

    @Override
    public State mouseReleased(Widget widget, WidgetMouseEvent event) {

        ((ImageWidget) widget).setPaintAsDisabled(false);

        if (event.getButton() != MouseEvent.BUTTON1) {
            return State.REJECTED;
        }
        
        Node[] selectedNodes = TopComponent.getRegistry().getActivatedNodes();
        
        if (selectedNodes != null && selectedNodes.length > 0 &&
                selectedNodes[0] instanceof CasaNode) {
            final CasaNode connectionNode = (CasaNode) selectedNodes[0];

            CasaConnection connection = (CasaConnection) connectionNode.getData();
            String cEndpointName = connection.getConsumer().get().getEndpointName();
            String pEndpointName = connection.getProvider().get().getEndpointName();
            
            final PropertySheetView propertySheetView = new PropertySheetView();

            Object[] options = new Object[]{Constants.CLOSE};
            DialogDescriptor descriptor = new DialogDescriptor(
                    propertySheetView,
                    NbBundle.getMessage(getClass(), "STR_PROPERTIES", 
                    cEndpointName + "<->" + pEndpointName),
                    true,
                    options,
                    null,
                    DialogDescriptor.DEFAULT_ALIGN,
                    null,
                    null);
            descriptor.setClosingOptions(options);

            final Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);

            // set nodes after PropertySheetView.addNotify() getting called
            propertySheetView.setNodes(new Node[]{selectedNodes[0]}); 

            // The dialog is modal, allow the action chain to continue while
            // we open the dialog later.
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    dlg.setVisible(true);                

                    // refresh to update the main property sheet
                    connectionNode.refresh(); 
                }
            });
        }

        return State.CONSUMED;
    }

    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    public WidgetAction.State dragExit(Widget widget, WidgetAction.WidgetMouseEvent event) {
        return State.createLocked(widget, this);
    }

    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    @Override
    public WidgetAction.State mouseExited(Widget widget, WidgetAction.WidgetMouseEvent event) {
        return State.createLocked(widget, this);
    }

    // If the mouse is ever moved off of the widget, either from a drag/move, then
    // we must lock the state so that we get the mouseReleased event.
    @Override
    public WidgetAction.State mouseDragged(Widget widget, WidgetAction.WidgetMouseEvent event) {
        return State.createLocked(widget, this);
    }
    
    // Handles the hover-over visual feedback.
    @Override
    public State mouseMoved(Widget widget, WidgetMouseEvent event) {
        mScene.getView().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        return State.CONSUMED;
    }
}
