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


package org.netbeans.modules.bpel.design.model.patterns;


import java.awt.geom.Area;
import java.util.Collection;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;


public class ElseIfPattern extends CompositePattern {
    
    private VisualElement gateway;
    private PlaceHolderElement placeHolder;
    private Connection connection;
    
    
    public ElseIfPattern(DiagramModel model) {
        super(model);
    }

    
    protected void onAppendPattern(Pattern nestedPattern) {
        removeElement(placeHolder);
    }

    
    protected void onRemovePattern(Pattern nestedPattern) {
        appendElement(placeHolder);
    }

    
    public VisualElement getFirstElement() {
        return gateway;
    }

            
    
    public VisualElement getLastElement() {
        Pattern p = getNestedPattern();
        return (p != null) ? p.getLastElement() : placeHolder;
    }

    
    public void reconnectElements() {
        Pattern p = getNestedPattern();
        VisualElement t = (p == null) ? placeHolder : p.getFirstElement();
        connection.connect(gateway, Direction.BOTTOM, t, Direction.TOP);
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        Pattern p = getNestedPattern();
        
        double x0 = -gateway.getWidth() / 2.0;
        double y0 = -gateway.getHeight() / 2.0;
        
        gateway.setLocation(x0, y0);
        
        double y1 = x0 + gateway.getHeight() + LayoutManager.VSPACING; // + 8; //TODO: Replace with constant
        double x1 = gateway.getWidth() / 2;
        
        if (p != null) {
            FBounds bounds = p.getBounds();
            FPoint offset = manager.getOriginOffset(p);
            
            manager.setPatternPosition(p, -offset.x, y1);
            
            y1 += bounds.height;
            x0 = Math.min(x0, -offset.x);
            x1 = Math.max(x1, bounds.width - offset.x);
        } else {
            double width = placeHolder.getWidth();
            double height = placeHolder.getHeight();
            placeHolder.setLocation(-width / 2, y1);
            y1 += height;
            x0 = Math.min(x0, -width / 2);
            x1 = Math.max(x1, width / 2);
        }
        getBorder().setClientRectangle(x0, y0, x1 - x0, y1 - y0);
        
        return getBorder().getBounds(); 
    }

    
    public Area createSelection() {
        Area result = new Area(getBorder().getShape());
        result.subtract(new Area(gateway.getShape()));
        return result;
    }
    

    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        getBorder().setText(getDefaultName());
        
        gateway = ContentElement.createElseIfGateway();
        placeHolder = new PlaceHolderElement();
        
        appendElement(gateway);
        appendElement(placeHolder);
        
        connection = new Connection(this);
        
        BpelEntity activity = ((ElseIf) getOMReference()).getActivity();
        if (activity != null) {
            getModel().createPattern(activity).setParent(this);
        }
    }


    public String getDefaultName() {
        return "ElseIf"; // NOI18N
    }
    
    
    public NodeType getNodeType() {
        return NodeType.ELSE_IF;
    }
    

    public boolean isCollapsable() {
        return true;
    }    
    
    
    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        if (((ElseIf) getOMReference()).getActivity() != null) return;
        
        placeHolders.add(new InnerPlaceHolder(draggedPattern));
    }

    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(ElseIfPattern.this, draggedPattern,
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            ((ElseIf) getOMReference()).setActivity((Activity)
                    getDraggedPattern().getOMReference());
        }
    }

}
