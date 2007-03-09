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

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Set;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.NodeDeleteAction;

/**
 *
 * @author jsandusky
 */
public class CasaRemoveAction extends WidgetAction.Adapter {
    
    private CasaModelGraphScene mScene;
    private CasaWrapperModel mModel;
    
    
    public CasaRemoveAction(CasaModelGraphScene scene, CasaWrapperModel model) {
        mScene = scene;
        mModel = model;
    }
    
    
    public State keyPressed(Widget widget, WidgetKeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_DELETE) {
            // First check whether any of the selected objects are
            // not deletable. If at least one is not, then bail.
            // Otherwise, allow the delete.
            Set objectsToDelete = mScene.getSelectedObjects();
            for (Object object : objectsToDelete) {
                CasaNode node = 
                        (CasaNode) mScene.getNodeFactory().createNodeFor((CasaComponent) object);
                if (!node.isDeletable()) {
                    return State.REJECTED;
                }
            }
            // Perform the deletion.
            NodeDeleteAction.delete(mModel, new ArrayList(mScene.getSelectedObjects()));
            return State.CONSUMED;
        }
        
        return State.REJECTED;
    }
}
