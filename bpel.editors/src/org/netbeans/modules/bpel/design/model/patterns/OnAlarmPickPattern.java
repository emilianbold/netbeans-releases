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

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;


public class OnAlarmPickPattern extends CompositePattern {
    
    
    private VisualElement timerEvent;
    private Connection connection;
    private PlaceHolderElement placeHolder;
    
    
    public OnAlarmPickPattern(DiagramModel model) {
       super(model);
    }
    
    
    public VisualElement getFirstElement() {
        return timerEvent;
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
        
        timerEvent = ContentElement.createTimerEvent();
        timerEvent.setLabelText("Timer"); // NOI18N
        appendElement(timerEvent);
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        BpelEntity activity = ((OnAlarmPick) getOMReference()).getActivity();
        
        if (activity != null) {
            Pattern p = getModel().createPattern(activity);
            p.setParent(this);
        }
    }
    
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        
        double height = timerEvent.getHeight() + LayoutManager.HSPACING;
        double width = timerEvent.getWidth();
        
        double eventX;
        
        if (getNestedPatterns().isEmpty()) {
            height += placeHolder.getHeight();
            width = Math.max(width, placeHolder.getWidth());
            
            placeHolder.setCenter(0, height / 2 - placeHolder.getHeight() / 2);
            
            eventX = 0;
        } else {
            Pattern p = getNestedPattern();
            FBounds pSize =  p.getBounds();
            eventX = -pSize.width / 2 + manager.getOriginOffset(p).x;
            
            height += pSize.height;
            width = Math.max(width, pSize.width);
            manager.setPatternPosition(p, -pSize.width / 2, height / 2 - pSize.height);
        }
        
        timerEvent.setCenter(eventX, -height / 2 + timerEvent.getHeight() / 2);
        
       
        getBorder().setClientRectangle( -width / 2, -height / 2,
                width, height);
        return getBorder().getBounds();
    }
    
    
    public void createPlaceholders(Pattern draggedPattern, 
            Collection<PlaceHolder> placeHolders) 
    {
        if (draggedPattern == this) return;
        if (isNestedIn(draggedPattern)) return;
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        
        if (placeHolder.getPattern() != null) {
            placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }
    
    
    protected void onAppendPattern(Pattern p) {
        removeElement(placeHolder);
    }
    
    protected void onRemovePattern(Pattern p) {
        appendElement(placeHolder);
    }
    
    
    public String getDefaultName() {
        return "OnAlarm"; // NOI18N
    }
    
    
    public NodeType getNodeType() {
        return NodeType.ALARM_HANDLER;
    }
    
    
    public void reconnectElements() {
        if (connection == null) {
            connection = new Connection(this);
        }
        
        if (getNestedPatterns().isEmpty()){
            connection.connect(timerEvent, Direction.BOTTOM, 
                    placeHolder, Direction.TOP);
        } else {
            connection.connect(timerEvent, Direction.BOTTOM, 
                    getNestedPattern().getFirstElement(), Direction.TOP);
        }
    }
    

    public Area createSelection() {
        Area res = new Area(getBorder().getShape());
        res.subtract(new Area(timerEvent.getShape()));
        return res;
    }
    
    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(OnAlarmPickPattern.this, draggedPattern, 
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            ((OnAlarmPick) getOMReference()).setActivity((Activity) 
                    getDraggedPattern().getOMReference());
        }
    }
}
