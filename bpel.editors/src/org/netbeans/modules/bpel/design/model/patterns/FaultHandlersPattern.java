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

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;

import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;

 
public class FaultHandlersPattern extends CompositePattern {
    
    
    public FaultHandlersPattern(DiagramModel model) {
        super(model);
    }

    
    protected void onAppendPattern(Pattern nestedPattern) {}

    
    protected void onRemovePattern(Pattern nestedPattern) {}

    
    public VisualElement getFirstElement() {
        return getBorder();
    }

    
    public VisualElement getLastElement() {
        return getBorder();
    }

    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        getBorder().setLabelText(getDefaultName());
        
        FaultHandlers faultHandlers = (FaultHandlers) getOMReference();

        Catch[] catches = faultHandlers.getCatches();
        CompensatableActivityHolder catchAll = faultHandlers.getCatchAll();
        
        for (Catch c : catches) {
            Pattern p = getModel().createPattern(c);
            p.setParent(this);
        }
        
        if (catchAll != null) {
            Pattern p = getModel().createPattern(catchAll);
            p.setParent(this);
        }
    }

    
    public FBounds layoutPattern(LayoutManager manager) {
        FaultHandlers faultHandlers = (FaultHandlers) getOMReference();

        Catch[] catches = faultHandlers.getCatches();
        CompensatableActivityHolder catchAll = faultHandlers.getCatchAll();

        float xPosition = 0;
        
        float width;
        float height = 0;
        
        if (catchAll != null) {
            Pattern p = getNestedPattern(catchAll);
            manager.setPatternPosition(p, xPosition, 0);
            
            FDimension size =  p.getBounds().getSize();
            
            height = size.height;
            xPosition += size.width + LayoutManager.HSPACING;
        }
        
        for (Catch c : catches) {
            Pattern p = getNestedPattern(c);
            manager.setPatternPosition(p, xPosition, 0);
            
            FDimension size =  p.getBounds().getSize();
            
            height = Math.max(height, size.height);
            xPosition += size.width + LayoutManager.HSPACING;
        }
        
        height = Math.max(height, 20);
        width = Math.max(xPosition - LayoutManager.HSPACING, 20);
        
        getBorder().setClientRectangle(0, 0, width, height);
        return getBorder().getBounds();
                
    }
    

    public String getDefaultName() {
        return "Fault Handlers"; // NOI18N
    }     
    

    public NodeType getNodeType() {
        return NodeType.FAULT_HANDLERS;
    }
}
