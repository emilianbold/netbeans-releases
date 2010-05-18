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

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;

public class CatchAllPattern extends CompositePattern {


    private PlaceHolderElement placeHolder;
    
    
    public CatchAllPattern(DiagramModel model) {
        super(model);
        placeHolder = new PlaceHolderElement();
    }

    
    protected void onAppendPattern(Pattern nestedPattern) {
        removeElement(placeHolder);
    }

    
    protected void onRemovePattern(Pattern nestedPattern) {
        appendElement(placeHolder);
    }

    
    public VisualElement getFirstElement() {
        return getBorder();
    }

    
    public VisualElement getLastElement() {
        return getBorder();
    }

    
    public FBounds layoutPattern(LayoutManager manager) {
        
        Activity a = (Activity) ((CompensatableActivityHolder) getOMReference())
                .getActivity();

        double width;
        double height;

        if (a == null) {
            placeHolder.setLocation( 0, 0);
            
            width = placeHolder.getWidth();
            height = placeHolder.getHeight();
        } else {
            Pattern p = getNestedPattern(a);
            manager.setPatternPosition(p, 0, 0);
            
            FBounds bounds = p.getBounds();
            width = bounds.width;
            height = bounds.height;
        }
        
        getBorder().setClientRectangle(0, 0, width, height);
        return getBorder().getBounds(); 
    }

    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        
        getBorder().setLabelText(getDefaultName());
        
        appendElement(placeHolder);
        
        Activity a = (Activity) ((CompensatableActivityHolder) getOMReference()).getActivity();
        
        if (a != null) {
            Pattern p = getModel().createPattern(a);
            p.setParent(this);
        }
    }
    
    
    public String getDefaultName() {
        return "Catch All"; // NOI18N
    }     

    
    public NodeType getNodeType() {
        return NodeType.CATCH_ALL;
    }

    
    public void createPlaceholders(Pattern draggedPattern, 
            Collection<PlaceHolder> placeHolders) 
    {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
            
        if (placeHolder.getPattern() != null) {
            placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }
    
    
    private class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(CatchAllPattern.this, draggedPattern, 
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            Pattern p = getDraggedPattern();
            ((CompensatableActivityHolder) getOMReference())
                .setActivity((Activity) p.getOMReference());
        }
    }    
}
