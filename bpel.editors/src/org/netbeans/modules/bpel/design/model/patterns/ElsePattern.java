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


import java.util.Collection;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;


public class ElsePattern extends CompositePattern {
    
    private PlaceHolderElement placeHolder;
    
    
    public ElsePattern(DiagramModel model) {
        super(model);
    }
    
    public boolean isSelectable() {
        return false;
    }
    
    protected void onAppendPattern(Pattern nestedPattern) {
        removeElement(placeHolder);
    }
    
    
    protected void onRemovePattern(Pattern nestedPattern) {
        appendElement(placeHolder);
    }
    
    
    public VisualElement getFirstElement() {
        Pattern p = getNestedPattern();
        return (p != null) ? p.getFirstElement() : placeHolder;
    }
    
    
    public VisualElement getLastElement() {
        Pattern p = getNestedPattern();
        return (p != null) ? p.getLastElement() : placeHolder;
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        Pattern p = getNestedPattern();
        
        double width;
        double height;
        
        if (p != null) {
            FBounds bounds = p.getBounds();
            width = bounds.width;
            height = bounds.height;
            manager.setPatternPosition(p, 0, 0);
        } else {
            width = placeHolder.getWidth();
            height = placeHolder.getHeight();
            placeHolder.setLocation(0, 0);
        }
        
        return null; //manager.setBorderBounds(getBorder(), 0, 0, width, height);
    }
    
    
    protected void createElementsImpl() {
        //setBorder(new GroupBorder());
        //setBorder(new ElseIfBorder());
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        BpelEntity activity = ((Else) getOMReference()).getActivity();
        if (activity != null) {
            getModel().createPattern(activity).setParent(this);
        }
    }
    
    
    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        if (((Else) getOMReference()).getActivity() != null) return;
        
        placeHolders.add(new InnerPlaceHolder(draggedPattern));
    }
    
    
    public NodeType getNodeType() {
        return NodeType.ELSE;
    }
    
    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(ElsePattern.this, draggedPattern,
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            ((Else) getOMReference()).setActivity((Activity)
            getDraggedPattern().getOMReference());
        }
    }
}
