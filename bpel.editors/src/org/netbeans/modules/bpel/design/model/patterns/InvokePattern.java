/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.design.model.patterns;


import java.awt.Color;
import java.awt.geom.Area;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.GlowDescriptor;
import org.netbeans.modules.bpel.design.decoration.StrokeDescriptor;
import org.netbeans.modules.bpel.design.geom.FDimension;
import org.netbeans.modules.bpel.design.geom.FInsets;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.CompensateBadgeElement;
import org.netbeans.modules.bpel.design.model.elements.FaultBadgeElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.InvokeElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CatchAll;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.Invoke;

/**
 *
 * @author Alexey Yarmolenko
 */
public class InvokePattern extends CompositePattern{
  
    private VisualElement invokeElement;

    private VisualElement errorBadge;
    private VisualElement compensationBadge;
    private BorderElement borderElement;
    
    
    public InvokePattern(DiagramModel model) {
        super(model);
    }


    protected void onAppendPattern(Pattern p) {
        if ((p instanceof CatchPattern) || (p instanceof CatchAllPattern)) {
            if (borderElement.getPattern() == null) {
                appendElement(borderElement);
                appendElement(errorBadge);
            }
        } else if (p instanceof CompensationHandlerPattern) {
            appendElement(compensationBadge);
        }
    }
    
    
    protected void onRemovePattern(Pattern p) {
        if ((p instanceof CatchPattern) || (p instanceof CatchAllPattern)) {
            Invoke invoke = (Invoke) getOMReference();
            if ((invoke.sizeOfCathes() == 0) 
                    && (invoke.getCatchAll() == null)) 
            {
                removeElement(borderElement);
                removeElement(errorBadge);
            }
            
        } else if (p instanceof CompensationHandlerPattern) {
            removeElement(compensationBadge);
        }
    }

    
    
    protected void createElementsImpl() {
        invokeElement = new InvokeElement();
        appendElement(invokeElement);
        registerTextElement(invokeElement);
        
        errorBadge = new FaultBadgeElement();
        compensationBadge = new CompensateBadgeElement();
        borderElement = new GroupBorder();        
        
        Invoke invoke = (Invoke) getOMReference();
        
        int catchCount = invoke.sizeOfCathes();
        for (int i = 0; i < catchCount; i++) {
            Catch c = invoke.getCatch(i);
            Pattern p = getModel().createPattern(c);
            p.setParent(this);
        }
        
        CatchAll catchAll = invoke.getCatchAll();
        if (catchAll != null) {
            Pattern p = getModel().createPattern(catchAll);
            p.setParent(this);
        }
        
        CompensationHandler compensation = invoke.getCompensationHandler();
        if (compensation != null) {
            Pattern p = getModel().createPattern(compensation);
            p.setParent(this);
        }
    }
 
    
    public String getDefaultName() {
        return "Invoke"; // NOI18N
    }    

    
    public NodeType getNodeType() {
        return NodeType.INVOKE;
    }
    
    
    public void reconnectElements() {
        clearConnections();
        new PartnerLinkHelper(getModel()).updateMessageFlowLinks(this);
        
        if (errorBadge.getPattern() != null) {
            new Connection(this).connect(errorBadge, Direction.RIGHT,
                    borderElement, Direction.TOP);
        }
        
        if (compensationBadge.getPattern() != null) {
            Invoke invoke = (Invoke) getOMReference();
            CompensationHandler handler = invoke.getCompensationHandler();
            new Connection(this).connect(compensationBadge, Direction.RIGHT,
                    getNestedPattern(handler).getFirstElement(), Direction.TOP);
        }
    }   

    
    
    public VisualElement getFirstElement() {
        return invokeElement;
    }

    
    public VisualElement getLastElement() {
        return invokeElement;
    }
    

    public FRectangle layoutPattern(LayoutManager manager) {
        
        float xmin = -invokeElement.getWidth() - LayoutManager.HSPACING;
        float ymin = -invokeElement.getHeight();
        
        float xmax = xmin + invokeElement.getWidth();
        float ymax = ymin + invokeElement.getHeight();
        
        float xcenter = (xmin + xmax) / 2;
        
        float rightSideX = xmax;
        float rightSideY0 = ymin;
        float rightSideY1 = ymax;
        
        invokeElement.setLocation(xmin, ymin);
        setOrigin( (xmin + xmax) / 2, (ymin + ymax) / 2);
        
        Invoke invoke = (Invoke) getOMReference();

        int catchCount = invoke.sizeOfCathes();
        CatchAll catchAll = invoke.getCatchAll();
        CompensationHandler compensation = invoke.getCompensationHandler();
        
        float x = 0;
        float y = 0;
        
        if ((catchCount > 0) || (catchAll != null)) {
            FInsets borderInsets = borderElement.getInsets();
            
            float x1 = x + borderInsets.left;
            float y1 = y + borderInsets.top;
            
            float x2 = x1;
            float y2 = y1;
            
            for (int i = 0; i < catchCount; i++) {
                Pattern p = getNestedPattern(invoke.getCatch(i));
                FDimension size =  p.getBounds().getSize();
                manager.setPatternPosition(p, x2, y1);
                y2 = Math.max(y2, y1 + size.height);
                x2 += size.width + LayoutManager.HSPACING;
            }
            
            if (catchAll != null) {
                Pattern p = getNestedPattern(catchAll);
                FDimension size =  p.getBounds().getSize();
                manager.setPatternPosition(p, x2, y1);
                y2 = Math.max(y2, y1 + size.height);
                x2 += size.width;
            } else {
                x2 -= LayoutManager.HSPACING;
            }
            
            x1 -= borderInsets.left;
            y1 -= borderInsets.top;
            
            x2 += borderInsets.right;
            y2 += borderInsets.left;
            
            borderElement.setSize(x2 - x1, y2 - y1);
            borderElement.setLocation(x1, y1);
            
            x = x2;

            xmax = Math.max(xmax, x2);
            ymax = Math.max(ymax, y2);
        }

        
        if (compensation != null) {
            if (x > 0) {
                x += LayoutManager.HSPACING;
            }
            
            Pattern p = getNestedPattern(compensation);
            FDimension size =  p.getBounds().getSize();
            
            manager.setPatternPosition(p, x, y);

            xmax = Math.max(xmax, x + size.width);
            ymax = Math.max(ymax, y + size.height);
        }
        
        
        //layout badges
        if ((catchCount > 0) || (catchAll != null)) {
            if (compensation != null) {
                float y1 = 0.75f * rightSideY0 + 0.25f * rightSideY1;
                float y2 = 0.25f * rightSideY0 + 0.75f * rightSideY1;

                compensationBadge.setCenter(rightSideX, y1);
                errorBadge.setCenter(rightSideX, y2);
            } else {
                float yPos = 0.5f * rightSideY0 + 0.5f * rightSideY1;
                float xPos = rightSideX;
                errorBadge.setCenter(xPos, yPos);
            }
        } else if (compensation != null) {
            float yPos = 0.5f * rightSideY0 + 0.5f * rightSideY1;
            float xPos = rightSideX;
            
            compensationBadge.setCenter(xPos, yPos);
        }
        
        return new FRectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    
    public Area createOutline() {
        Area res = GUtils.createArea(invokeElement.getShape());
        
        if (compensationBadge.getPattern() != null) {
            res.add(GUtils.createArea(compensationBadge.getShape()));
        }
        
        if (errorBadge.getPattern() != null) {
            res.add(GUtils.createArea(errorBadge.getShape()));
        }
        
        return res;
    }
} 
