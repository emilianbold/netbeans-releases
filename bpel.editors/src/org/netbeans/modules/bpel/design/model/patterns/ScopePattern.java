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

import java.awt.geom.Area;
import java.util.Collection;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.geom.BoundsBuilder;
import org.netbeans.modules.bpel.design.geom.FPoint;
import org.netbeans.modules.bpel.design.model.elements.TerminationBadgeElement;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.CompensationHandlerHolder;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.design.geom.FDimension;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.CompensateBadgeElement;

import org.netbeans.modules.bpel.design.model.elements.EndEvent;
import org.netbeans.modules.bpel.design.model.elements.EventsBadgeElement;
import org.netbeans.modules.bpel.design.model.elements.FaultBadgeElement;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.StartEvent;
import org.netbeans.modules.bpel.design.model.elements.SubprocessBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.TerminationHandler;

 
public class ScopePattern extends CompositePattern {
    
    private PlaceHolderElement placeHolder;
    private StartEvent startEvent;
    private EndEvent endEvent;
    
    private CompensateBadgeElement compensateBadge;
    private EventsBadgeElement eventsBadge;
    private FaultBadgeElement faultBadge;
    private TerminationBadgeElement terminationBadge;
            
    private Connection startConnection;
    private Connection endConnection;
    
    private Connection eventsConnection;
    private Connection faultConnection;
    private Connection compConnection;
    private Connection terminationConnection;

    
    public ScopePattern(DiagramModel model) {
        super(model);
        startConnection = new Connection(this);
        endConnection = new Connection(this);
    }
    
    
    public VisualElement getFirstElement() {
        return getBorder();
    }
    
    
    public VisualElement getLastElement() {
        return getBorder();
    }
    
    public Pattern getActivityPattern(){
        BpelEntity activity = ((Scope) getOMReference()).getActivity();
        if (activity == null){
            return null;
        }
        return getNestedPattern(activity); 
    }
    
    
    public Pattern getCompensationHandlerPattern() {
        CompensationHandler compHandler = ((CompensationHandlerHolder) 
                getOMReference()).getCompensationHandler();

        return (compHandler != null) ? getNestedPattern(compHandler) : null; 
    }

    
    public Pattern getEventHandlersPattern() {
        EventHandlers eventHandlers = ((Scope) getOMReference())
                .getEventHandlers();

        return (eventHandlers != null) ? getNestedPattern(eventHandlers) : null;
    }
    
    
    public Pattern getFaultHandlersPattern() {
        FaultHandlers faultHandlers = ((Scope) getOMReference())
                .getFaultHandlers();
        
        return (faultHandlers != null) ? getNestedPattern(faultHandlers) : null;
    }
    
    
    public Pattern getTerminationHandlerPattern() {
        TerminationHandler terminationHandler = ((Scope) getOMReference())
                .getTerminationHandler();
        
        return (terminationHandler != null) 
                ? getNestedPattern(terminationHandler) : null;
    }
    
    
    protected void createElementsImpl() {
        setBorder(new SubprocessBorder());
        getBorder().setLabelText(getDefaultName());
        registerTextElement(getBorder());        
        
        startEvent = new StartEvent();
        endEvent = new EndEvent();
        
        placeHolder = new PlaceHolderElement();

        compensateBadge = new CompensateBadgeElement();
        eventsBadge = new EventsBadgeElement();
        faultBadge = new FaultBadgeElement();
        terminationBadge = new TerminationBadgeElement();
        
        appendElement(placeHolder);
        
        startEvent = new StartEvent();
        appendElement(startEvent);
        startEvent.setLabelText(""); // NOI18N
        
        endEvent = new EndEvent();
        appendElement(endEvent);
        endEvent.setLabelText(""); // NOI18N
        
        Scope scopeOM = (Scope) getOMReference();
        
        Activity activity = (Activity) scopeOM.getActivity();
        
        if (activity != null) {
            Pattern p = getModel().createPattern(activity);
            p.setParent(this);
        }
        
        CompensationHandler compHandler = scopeOM.getCompensationHandler();
        if (compHandler != null) {
            Pattern p = getModel().createPattern(compHandler);
            p.setParent(this);
        }
        
        EventHandlers eventHandlers = scopeOM.getEventHandlers();
        if (eventHandlers != null) {
            Pattern p = getModel().createPattern(eventHandlers);
            p.setParent(this);
        }
        
        FaultHandlers faultHandlers = scopeOM.getFaultHandlers();
        if (faultHandlers != null) {
            Pattern p = getModel().createPattern(faultHandlers);
            p.setParent(this);
        }
        
        TerminationHandler terminationHandler = scopeOM.getTerminationHandler();
        if (terminationHandler != null) {
            Pattern p = getModel().createPattern(terminationHandler);
            p.setParent(this);
        }
    }
    
    
    public void onAppendPattern(Pattern p) {
        if (p instanceof CompensationHandlerPattern) {
            appendElement(compensateBadge);
        } else if (p instanceof EventHandlersPattern) {
            appendElement(eventsBadge);
        } else if (p instanceof FaultHandlersPattern) {
            appendElement(faultBadge);
        } else if (p instanceof TerminationHandlerPattern) { 
            appendElement(terminationBadge);
        } else {
            removeElement(placeHolder);
        }
    }
    
    
    public void onRemovePattern(Pattern p) {
        if (p instanceof CompensationHandlerPattern) {
            removeElement(compensateBadge);
        } else if (p instanceof EventHandlersPattern) {
            removeElement(eventsBadge);
        } else if (p instanceof FaultHandlersPattern) {
            removeElement(faultBadge);
        } else if (p instanceof TerminationHandlerPattern) {
            removeElement(terminationBadge);
        } else {
            appendElement(placeHolder);
        }
    }
    
    
    public FRectangle layoutPattern(LayoutManager manager) {
        BoundsBuilder bounds = new BoundsBuilder();
        Pattern activityPattern = getActivityPattern();
        
        if (activityPattern == null) {
            placeHolder.setCenter( 0, 0);
            bounds.add(placeHolder.getBounds());
        } else {
            FPoint origin = manager.getOriginOffset(activityPattern);
            
            bounds.add(manager.setPatternPosition(activityPattern, -origin.x, 0));
        }
        
        float sHeight = startEvent.getHeight();
        float eHeight = endEvent.getHeight();
        
        FRectangle nested = bounds.bounds();
        startEvent.setCenter(0, nested.y - LayoutManager.VSPACING - sHeight / 2);
        bounds.add(startEvent.getBounds());
        
        endEvent.setCenter(0, nested.height + LayoutManager.VSPACING + eHeight / 2);
        bounds.add(endEvent.getBounds());
        
        getBorder().setClientRectangle(bounds.bounds());

        FRectangle borderRect = getBorder().getBounds();
        setOrigin( borderRect.getCenter().x , 0);

        Pattern eventHandlers = getEventHandlersPattern();
        Pattern compHandler = getCompensationHandlerPattern();
        Pattern terminationHandler = getTerminationHandlerPattern();
        Pattern faultHandlers = getFaultHandlersPattern();
        
        int rightHandlerCount = 0;
        
        if (eventHandlers != null) { rightHandlerCount++; }
        if (compHandler != null) { rightHandlerCount++; }
        if (terminationHandler != null) { rightHandlerCount++; }
        if (faultHandlers != null) { rightHandlerCount++; }

        float ry = borderRect.y + getBorder().getInsets().top + 8;
        float rx = borderRect.x + borderRect.width + LayoutManager.HSPACING;
        float lx = borderRect.x - LayoutManager.HSPACING;
        
        float hy = ry + LayoutManager.VSPACING * rightHandlerCount;
        
        float hHeight = 0;
        
        int handlerNumber = 1;


        if (eventHandlers != null) {
            eventsBadge.setCenter(borderRect.x + borderRect.width, 
                    ry + (rightHandlerCount - handlerNumber) * LayoutManager.VSPACING);

            manager.setPatternPosition(eventHandlers, rx, hy);
            
            FDimension hSize = eventHandlers.getBounds().getSize();
            
            rx += hSize.width + LayoutManager.HSPACING;
            hHeight = Math.max(hHeight, hSize.height);
            
            handlerNumber++;
        }
        
        
        if (compHandler != null ) {
            compensateBadge.setCenter(borderRect.x + borderRect.width, 
                    ry + (rightHandlerCount - handlerNumber) * LayoutManager.VSPACING);
            
            manager.setPatternPosition(compHandler, rx, hy);
        
            FDimension hSize = compHandler.getBounds().getSize();
            
            rx += hSize.width + LayoutManager.HSPACING;
            hHeight = Math.max(hHeight, hSize.height);
            
            handlerNumber++;
        }

        if (terminationHandler != null) {
            terminationBadge.setCenter(borderRect.x + borderRect.width,
                    ry + (rightHandlerCount - handlerNumber) * LayoutManager.VSPACING);

            manager.setPatternPosition(terminationHandler, rx, hy);

            FDimension hSize = terminationHandler.getBounds().getSize();
            
            rx += hSize.width + LayoutManager.HSPACING;
            hHeight = Math.max(hHeight, hSize.height);
            
            handlerNumber++;
        }
            
        if (faultHandlers != null) {
            faultBadge.setCenter(borderRect.x + borderRect.width,
                    ry + (rightHandlerCount - handlerNumber) * LayoutManager.VSPACING);

            manager.setPatternPosition(faultHandlers, rx, hy);
            
            FDimension hSize = faultHandlers.getBounds().getSize();
            
            rx += hSize.width + LayoutManager.HSPACING;
            hHeight = Math.max(hHeight, hSize.height);
        }
        
//        height = Math.max(height, hHeight + hy - y - getBorder().getInsets().top);
//        
//        manager.setBorderBounds(getBorder(), x, y, width, height);
        
        return null; //LM will caculate pattern bounds by itself.
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
    
    
    public String getDefaultName() {
        return "Scope"; // NOI18N
    }
    
    
    public void reconnectElements() {
        Pattern p = getActivityPattern();
        if (p != null){
            startConnection.connect(startEvent, Direction.BOTTOM, 
                    p.getFirstElement(), Direction.TOP);
            
            endConnection.connect(p.getLastElement(), Direction.BOTTOM,
                    endEvent, Direction.TOP);
        } else {
            startConnection.connect(startEvent, Direction.BOTTOM,
                    placeHolder, Direction.TOP);
            endConnection.connect(placeHolder, Direction.BOTTOM,
                    endEvent, Direction.TOP);
        }
        
        Pattern compHandler = getCompensationHandlerPattern();
        
        if (compHandler != null){
            if (compConnection == null) {
                compConnection = new Connection(this);
            }
            compConnection.connect(compensateBadge, Direction.RIGHT,
                    compHandler.getFirstElement(), Direction.TOP);
        } else {
            if (compConnection != null) {
                compConnection.remove();
                compConnection = null;
            }
        }

        
        Pattern terminationHandler = getTerminationHandlerPattern();
        
        if (terminationHandler != null) {
            if (terminationConnection == null) {
                terminationConnection = new Connection(this);
            }
            terminationConnection.connect(terminationBadge, Direction.RIGHT,
                    terminationHandler.getFirstElement(), Direction.TOP);
        } else {
            if (terminationConnection != null) {
                terminationConnection.remove();
                terminationConnection = null;
            }
        }
        
        
        Pattern eventHandlres = getEventHandlersPattern();
        
        if (eventHandlres != null) {
            if (eventsConnection == null) {
                eventsConnection = new Connection(this);
            }
            eventsConnection.connect(eventsBadge, Direction.RIGHT,
                    eventHandlres.getFirstElement(), Direction.TOP);
        } else {
            if (eventsConnection != null) {
                eventsConnection.remove();
                eventsConnection = null;
            }
        }
        
        Pattern faultHandlers = getFaultHandlersPattern();
        
        if (faultHandlers != null) {
            if (faultConnection == null) {
                faultConnection = new Connection(this);
            }
            faultConnection.connect(faultBadge, Direction.RIGHT,
                    faultHandlers.getFirstElement(), Direction.TOP);
        } else {
            if (faultConnection != null) {
                faultConnection.remove();
                faultConnection = null;
            }
        }
    }
    
    
    public NodeType getNodeType() {
        return NodeType.SCOPE;
    }

    
    public Area createSelection() {
        Area a = GUtils.createArea(getBorder().getShape());
        if (compensateBadge.getPattern() != null) {
            a.add(GUtils.createArea(compensateBadge.getShape()));
        }
        if (eventsBadge.getPattern() != null) {
            a.add(GUtils.createArea(eventsBadge.getShape()));
        }
        if (faultBadge.getPattern() != null) {
            a.add(GUtils.createArea(faultBadge.getShape()));
        }
        if (terminationBadge.getPattern() != null) {
            a.add(GUtils.createArea(terminationBadge.getShape()));
        }
        
        a.subtract(GUtils.createArea(startEvent.getShape()));
        a.subtract(GUtils.createArea(endEvent.getShape()));
        
        return a;
    }
    
    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(ScopePattern.this, draggedPattern, placeHolder.getCenterX(),
                    placeHolder.getCenterY());
        }
        
        public void drop() {
            ((Scope) getOMReference()).
                    setActivity((Activity) getDraggedPattern().getOMReference());
        }
    }
}
