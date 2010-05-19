/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.compapp.casaeditor.graph.actions;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
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
            Set selectedObjects = mScene.getSelectedObjects();
            List<CasaComponent> objectsToDelete = new ArrayList<CasaComponent>();
            for (Object object : selectedObjects) {
                CasaNode node = 
                        (CasaNode) mScene.getNodeFactory().createNodeFor((CasaComponent) object);
                if (node != null && node.isDeletable()) {
                    objectsToDelete.add((CasaComponent)object);
                }
            }
            
            if (objectsToDelete.size() == 0) {
                return State.REJECTED;
            }
            
            // Perform the deletion.
            NodeDeleteAction.delete(mModel, objectsToDelete);
            return State.CONSUMED;
        }
        
        return State.REJECTED;
    }
}
