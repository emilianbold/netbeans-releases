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


package org.netbeans.modules.bpel.design.model.patterns;

import java.awt.geom.Area;
import java.util.Collection;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.Scope;


public class OnEventPattern extends CompositePattern {
    
    
    private VisualElement messageEvent;
    private Connection connection;
    private PlaceHolderElement placeHolder;
    
    
    public OnEventPattern(DiagramModel model) {
        super(model);
    }
    
    
    protected void onAppendPattern(Pattern p) {
        removeElement(placeHolder);
    }
    
    
    protected void onRemovePattern(Pattern p) {
        appendElement(placeHolder);
    }
    
    
    public VisualElement getFirstElement() {
        return messageEvent;
    }
    
    
    public VisualElement getLastElement() {
        if (getNestedPatterns().isEmpty()) {
            return placeHolder;
        }
        return getNestedPattern().getLastElement();
    }
    
    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        getBorder().setLabelText(getDefaultName());
        
        messageEvent = ContentElement.createMessageEvent();
        messageEvent.setLabelText("Event"); // NOI18N
        appendElement(messageEvent);
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        BpelEntity activity = ((OnEvent) getOMReference()).getScope();
        
        if (activity != null) {
            Pattern p =getModel().createPattern(activity);
            p.setParent(this);
        }
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        
        double height = messageEvent.getHeight() + LayoutManager.HSPACING;
        double width = messageEvent.getWidth();
        
        double eventX;
        
        if (getNestedPatterns().isEmpty()) {
            height += placeHolder.getHeight();
            width = Math.max(width, placeHolder.getWidth());
            
            placeHolder.setCenter(0, height / 2 - placeHolder.getHeight() / 2);
            
            eventX = 0;
        } else {
            Pattern p = getNestedPattern();

            FBounds pSize = p.getBounds();
            eventX = -pSize.width / 2 + manager.getOriginOffset(p).x;
            
            height += pSize.height;
            width = Math.max(width, pSize.width);
            manager.setPatternPosition(p, -pSize.width / 2, height / 2 - pSize.height);
        }
        
        messageEvent.setCenter(eventX, -height / 2 + messageEvent.getHeight() / 2);
        getBorder().setClientRectangle(-width / 2, -height / 2,
                width, height);
        
        return getBorder().getBounds();
    }
    
    
    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {
        if (draggedPattern == this) return;
        if (isNestedIn(draggedPattern)) return;
        if (!(draggedPattern.getOMReference() instanceof ExtendableActivity)) return;
        
        if (placeHolder.getPattern() != null) {
            placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
        
    }
    
    
    public String getDefaultName() {
        return "OnEvent"; // NOI18N
    }
    
    
    public NodeType getNodeType() {
        return NodeType.ON_EVENT;
    }
    
    
    public void reconnectElements() {
        if (connection == null) {
            connection = new Connection(this);
        }
        
        if (getNestedPatterns().isEmpty()){
            connection.connect(messageEvent, Direction.BOTTOM,
                    placeHolder, Direction.TOP);
        } else {
            connection.connect(messageEvent, Direction.BOTTOM,
                    getNestedPattern().getFirstElement(), Direction.TOP);
        }
        clearConnectionsExcept(connection);
        new PartnerLinkHelper(getModel()).updateMessageFlowLinks(this);
    }
    
    
    public Area createSelection() {
        Area res = new Area(getBorder().getShape());
        res.subtract(new Area(messageEvent.getShape()));
        return res;
    }
    
    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(OnEventPattern.this, draggedPattern,
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            if (getDraggedPattern().getOMReference() instanceof Scope) {
                ((OnEvent) getOMReference()).setScope(
                        (Scope) getDraggedPattern().getOMReference());
            } else {
                Scope scope = getOwnerPattern().getBpelModel().getBuilder()
                        .createScope();
                scope.setActivity((ExtendableActivity) getDraggedPattern()
                        .getOMReference());
                ((OnEvent) getOMReference()).setScope(scope);
            }         
        }
    }
}


