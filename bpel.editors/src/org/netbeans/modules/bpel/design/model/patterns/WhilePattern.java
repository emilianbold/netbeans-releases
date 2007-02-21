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
import org.netbeans.modules.bpel.design.model.elements.ContentElement;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.SubprocessBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;


public class WhilePattern extends CompositePattern {
    
    private VisualElement gateway;
    private PlaceHolderElement placeHolder;
    private Connection connection1;
    private Connection connection2;
    
    
    public WhilePattern(DiagramModel model) {
        super(model);
    }
    
    
    public VisualElement getFirstElement() {
        return getBorder();
    }
     
    
    public VisualElement getLastElement() {
        return getBorder();
    }
    
    
    protected void createElementsImpl() {
        connection1 = new Connection(this);
        connection2 = new Connection(this);
        
        setBorder(new SubprocessBorder());
        registerTextElement(getBorder());
        
        gateway = ContentElement.createWhileGateway();
        appendElement(gateway);
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        While whileOM = (While) getOMReference();
        
        Activity activity = (Activity) whileOM.getActivity();
        
        if (activity != null) {
            Pattern p = getModel().createPattern(activity);
            p.setParent(this);
        }
    }
    
    
    public void createPlaceholders(Pattern draggedPattern, 
            Collection<PlaceHolder> placeHolders) 
    {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        
        if (placeHolder.getPattern() != null) {
             placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        double gx = -gateway.getWidth() - LayoutManager.HSPACING;
        double gy = -gateway.getHeight() - LayoutManager.VSPACING;
        gateway.setLocation(gx, gy);
        
        double width = -gx;
        double height = -gy;
        
        if (getNestedPatterns().isEmpty()) {
            placeHolder.setLocation(0, 0);
            
            width += placeHolder.getWidth();
            height += placeHolder.getHeight();
            
        } else {
            Pattern p = getNestedPattern();
            FDimension pSize = p.getBounds().getSize();
            manager.setPatternPosition(p, 0, 0);
            
            width += pSize.width;
            height += pSize.height;
        }
        setOrigin( gx + width / 2, gy + height / 2);
        
        getBorder().setClientRectangle( gx, gy, width, height);
        return getBorder().getBounds();
                
    }
    
    
    public String getDefaultName() {
        return "While"; // NOI18N
    }
    
    
    protected void onAppendPattern(Pattern nestedPattern) {
        removeElement(placeHolder);
    }
    

    protected void onRemovePattern(Pattern nestedPattern) {
        appendElement(placeHolder);
    }
    
    public void reconnectElements() {
        Pattern p = getNestedPattern();

        if (p != null) {
            connection1.connect(gateway, Direction.RIGHT, 
                    p.getFirstElement(), Direction.TOP);
            
            connection2.connect(p.getLastElement(), Direction.LEFT, 
                    gateway, Direction.BOTTOM);
        } else {
            connection1.connect(gateway, Direction.RIGHT, 
                    placeHolder, Direction.TOP);
            
            connection2.connect(placeHolder, Direction.LEFT, 
                    gateway, Direction.BOTTOM);
        }
    }

    public NodeType getNodeType() {
        return NodeType.WHILE;
    }

    
    public Area createSelection() {
        Area res = new Area(getBorder().getShape());
        res.subtract(new Area(gateway.getShape()));
        return res;
    }
    

    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(WhilePattern.this, draggedPattern, placeHolder.getCenterX(),
                    placeHolder.getCenterY());
        }

        public void drop() {
            Pattern p = getDraggedPattern();
            ((While) getOMReference()).setActivity((Activity) p.getOMReference());
        }
    }
    
}
