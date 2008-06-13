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


import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FInsets;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
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
        BpelEntity entity = p.getOMReference();
        
        if ((entity instanceof Catch) || (entity instanceof CatchAll)) {
            if (!errorBadge.hasPattern()) {
                appendElement(borderElement);
                appendElement(errorBadge);
            }
        } else if (entity instanceof CompensationHandler) {
            appendElement(compensationBadge);
        }
    }
    
    
    protected void onRemovePattern(Pattern p) {
        Invoke invoke = (Invoke) getOMReference();
        
        if (errorBadge.hasPattern() && (invoke.sizeOfCathes() == 0) 
                    && (invoke.getCatchAll() == null))
        {
            removeElement(borderElement);
            removeElement(errorBadge);
        } else if (compensationBadge.hasPattern() 
                && (invoke.getCompensationHandler() == null)) 
        {
            removeElement(compensationBadge);
        }
    }

    
    
    protected void createElementsImpl() {
        invokeElement = ContentElement.createInvoke();
        appendElement(invokeElement);
        registerTextElement(invokeElement);
        
        errorBadge = ContentElement.createFaultBadge();
        compensationBadge = ContentElement.createCompensateBadge();
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
    

    public FBounds layoutPattern(LayoutManager manager) {
        
        double xmin = -invokeElement.getWidth() - LayoutManager.HSPACING;
        double ymin = -invokeElement.getHeight();
        
        double xmax = xmin + invokeElement.getWidth();
        double ymax = ymin + invokeElement.getHeight();
        
        double xcenter = (xmin + xmax) / 2;
        
        double rightSideX = xmax;
        double rightSideY0 = ymin;
        double rightSideY1 = ymax;
        
        invokeElement.setLocation(xmin, ymin);
        setOrigin( (xmin + xmax) / 2, (ymin + ymax) / 2);
        
        Invoke invoke = (Invoke) getOMReference();

        int catchCount = invoke.sizeOfCathes();
        CatchAll catchAll = invoke.getCatchAll();
        CompensationHandler compensation = invoke.getCompensationHandler();
        
        double x = 0;
        double y = 0;
        
        if ((catchCount > 0) || (catchAll != null)) {
            FInsets borderInsets = borderElement.getInsets();
            
            double x1 = x + borderInsets.left;
            double y1 = y + borderInsets.top;
            
            double x2 = x1;
            double y2 = y1;
            
            for (int i = 0; i < catchCount; i++) {
                Pattern p = getNestedPattern(invoke.getCatch(i));
                FBounds size =  p.getBounds();
                manager.setPatternPosition(p, x2, y1);
                y2 = Math.max(y2, y1 + size.height);
                x2 += size.width + LayoutManager.HSPACING;
            }
            
            if (catchAll != null) {
                Pattern p = getNestedPattern(catchAll);
                FBounds size =  p.getBounds();
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
            FBounds size =  p.getBounds();
            
            manager.setPatternPosition(p, x, y);

            xmax = Math.max(xmax, x + size.width);
            ymax = Math.max(ymax, y + size.height);
        }
        
        
        //layout badges
        if ((catchCount > 0) || (catchAll != null)) {
            if (compensation != null) {
                double y1 = 0.75 * rightSideY0 + 0.25 * rightSideY1;
                double y2 = 0.25 * rightSideY0 + 0.75 * rightSideY1;

                compensationBadge.setCenter(rightSideX, y1);
                errorBadge.setCenter(rightSideX, y2);
            } else {
                double yPos = 0.5 * (rightSideY0 + rightSideY1);
                double xPos = rightSideX;
                errorBadge.setCenter(xPos, yPos);
            }
        } else if (compensation != null) {
            double yPos = 0.5f * (rightSideY0 + rightSideY1);
            double xPos = rightSideX;
            
            compensationBadge.setCenter(xPos, yPos);
        }
        
        return new FBounds(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    
    public Area createOutline() {
        Area res = invokeElement.getShape().createArea();
        
        if (compensationBadge.getPattern() != null) {
            res.add(compensationBadge.getShape().createArea());
        }
        
        if (errorBadge.getPattern() != null) {
            res.add(errorBadge.getShape().createArea());
        }
        
        return res;
    }


    public boolean isCollapsable() {
        return false;
    }
} 
