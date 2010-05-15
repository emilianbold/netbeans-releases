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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPath;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FRange;
import org.netbeans.modules.bpel.design.model.elements.NullBorder;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.DefaultConnection;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;


public class SequencePattern extends CompositePattern {
    
    
    private PlaceHolderElement placeHolder;
    private List<Connection> connections = new ArrayList<Connection>();
    
    
    public SequencePattern(DiagramModel model) {
        super(model);
    }
    
    
    public VisualElement getFirstElement() {
        BpelEntity[] activities = (BpelEntity[]) ((Sequence)getOMReference())
                .getActivities();
        
        if (activities.length > 0){
            return getNestedPattern(activities[0]).getFirstElement();
        }
        return placeHolder;
    }
    
    
    public VisualElement getLastElement() {
        BpelEntity[] activities = (BpelEntity[]) ((Sequence)getOMReference())
                .getActivities();
        
        if (activities.length > 0){
            return getNestedPattern(activities[activities.length - 1]).getLastElement();
        }
        return placeHolder;
        
    }

    public boolean isSelectable() {
        //sequence cannot be selected if sequences are filtered out.
        
        return (getBorder() instanceof GroupBorder);
        
    }
    

    public FBounds layoutPattern(LayoutManager manager) {
        Collection<Pattern> patterns = super.getNestedPatterns();
        
        FRange rangeW = new FRange(0);
        double yPos = 0;
        
        if (patterns.isEmpty()){
            double t = placeHolder.getWidth() / 2;
            placeHolder.setLocation(-t, 0);

            rangeW.extend(-t);
            rangeW.extend(t);
            yPos = placeHolder.getHeight();
            
        } else {
            BpelEntity[] activities = (BpelEntity[]) ((Sequence) 
                    getOMReference()).getActivities();
            
            
            for (BpelEntity a : activities) {
                Pattern p = getNestedPattern(a);
                assert p != null;
                FBounds clientSize = p.getBounds();
                FPoint origin = manager.getOriginOffset(p);
                
                manager.setPatternPosition(p, -origin.x, yPos);
                
                rangeW.extend(-origin.x);
                rangeW.extend(clientSize.width - origin.x);
                
                yPos += clientSize.height + LayoutManager.VSPACING;
                        
            }
            yPos -= LayoutManager.VSPACING;
        }
        
        getBorder().setClientRectangle(rangeW.min, 0, rangeW.getSize(), yPos);
        return getBorder().getBounds();
    }

    
    protected void createElementsImpl() {
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        if (getModel().getFilters().showImplicitSequences()){
            setBorder(new GroupBorder());  
            registerTextElement(getBorder());
        } else {
            setBorder(new NullBorder());    
        }
        
        
        BpelEntity activities[] = (BpelEntity[]) ((Sequence)getOMReference())
                .getActivities();
        
        for (BpelEntity a : activities) {
            Pattern p = getModel().createPattern(a);
            p.setParent(this);
        }
    }
    
    
    public String getDefaultName() {
        return "Sequence"; // NOI18N
    }
    
    
    protected void onAppendPattern(Pattern p) {
        if (placeHolder.getPattern() != null) {
            removeElement(placeHolder);
        }
    }
    
    
    protected void onRemovePattern(Pattern p) {
        if (getNestedPatterns().isEmpty()) {
            appendElement(placeHolder);
        }
    }
    
    
    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        
        BpelEntity[] activities = (BpelEntity[]) ((Sequence) getOMReference())
                .getActivities();
        
        if (activities.length > 0) {
            
            //iterate over nested element in correct order
            
            Connection inbound = getInboundConnection();
            Connection outbound = getOutboundConnection();
            
            Pattern firstPattern = getNestedPattern(activities[0]); 
            
            Pattern lastPattern = getNestedPattern(activities[activities.length 
                    - 1]); 
            
            
            Pattern p;
            
            Connection lastAdded = null;
            
            HashMap<Connection, Pattern> pairs
                    = new HashMap<Connection, Pattern>();
            
            if (inbound != null) {
                pairs.put(lastAdded = inbound, null);
            }
            
            for (int i = 0; i < connections.size(); i++) {
                p = getNestedPattern(activities[i]); 
                
                if (p != draggedPattern) {
                    pairs.put(lastAdded = connections.get(i), p);
                } else if (lastAdded != null) {
                    pairs.remove(lastAdded);
                }
            }
            
            if (lastPattern != draggedPattern) {
                //no outbound connection if sequence is inside
                if (outbound != null){
                    pairs.put(outbound, lastPattern);
                }
            } else if (lastAdded != null) {
                pairs.remove(lastAdded);
            }
            
            
            for (Connection c : pairs.keySet()) {
                // MAYBE BUG
                FPath connectedPathes = c.getSegmentsForPattern(this);
                
                if (connectedPathes != null) {
                        placeHolders.add(new ConnectionPlaceHolder(
                                draggedPattern, connectedPathes, pairs.get(c)));
//                    for (FPath path : connectedPathes) {
//                        placeHolders.add(new ConnectionPlaceHolder(
//                                draggedPattern, path, pairs.get(c)));
//                    }
                }
            }
            
            if ((inbound == null) && (firstPattern != draggedPattern)) {
                BorderElement border = getBorder();
                double cx = border.getCenterX();
                double cy = border.getY() + border.getInsets().top / 2;
                placeHolders.add(new FirstPlaceHolder(draggedPattern, cx, cy));
            }
            
            if ((outbound == null) && (lastPattern != draggedPattern)) {
                BorderElement border = getBorder();
                double cx = border.getCenterX();
                double cy = border.getY() + border.getHeight()
                        - border.getInsets().bottom / 2;
                placeHolders.add(new LastPlaceHolder(draggedPattern, cx, cy));
            }
            
        } else {
            placeHolders.add(new InnerPlaceHolder(draggedPattern));
        }
    }
    
    
    class FirstPlaceHolder extends PlaceHolder {
        public FirstPlaceHolder(Pattern draggedPattern, double cx, double cy) {
            super(SequencePattern.this, draggedPattern, cx, cy);
        }
        
        public void drop() {
            Pattern pattern = getDraggedPattern();
            Sequence sequence = (Sequence) getOMReference();
            sequence.insertActivity((Activity) pattern.getOMReference(), 0);
        }
    }
    
    
    class LastPlaceHolder extends PlaceHolder {
        public LastPlaceHolder(Pattern draggedPattern, double cx, double cy) {
            super(SequencePattern.this, draggedPattern, cx, cy);
        }
        
        public void drop() {
            Pattern pattern = getDraggedPattern();
            Sequence sequence = (Sequence) getOMReference();
            int i = sequence.getActivities().length;
            sequence.addActivity((Activity) pattern.getOMReference());
        }
    }
    
    
//    abstract class NoneConnectionPlaceHolder extends CirclePlaceHolder {
//        public NoneConnectionPlaceHolder(Pattern dndPattern,
//                float centerX, float centerY)
//        {
//            super(SequencePattern.this.getView(), SequencePattern.this,
//                    dndPattern, centerX, centerY);
//        }
//    }
    
    
    class ConnectionPlaceHolder extends PlaceHolder {
        
        private Pattern insertAfter;
        
        public ConnectionPlaceHolder(Pattern draggedPattern, FPath cPath,
                Pattern insertAfter) {
            super(SequencePattern.this, draggedPattern, cPath);
            this.insertAfter = insertAfter;
        }
        
        
        public void drop() {
            Pattern pattern = getDraggedPattern();
            
            // first find the place to paste new pattern
            // according to "connection" position
            BpelEntity[] activities = (BpelEntity[]) ((Sequence) 
                    getOMReference()).getActivities();
            
            int index = -1;
            
            if (insertAfter == null){
                index = 0;
            } else {
                Object refInsertAfter = insertAfter.getOMReference();
                for ( int n = 0; n < activities.length; n++ ){
                    
                    if ( activities[n] == refInsertAfter ){
                        index = n + 1;
                        break;
                    }
                }
            }
            
            ((Sequence) getOMReference()).insertActivity((Activity) pattern
                    .getOMReference(), index);
        }
    }
    
    
    class InnerPlaceHolder extends PlaceHolder {
        public InnerPlaceHolder(Pattern draggedPattern) {
            super(SequencePattern.this, draggedPattern,
                    placeHolder.getCenterX(), placeHolder.getCenterY());
        }
        
        public void drop() {
            ((Sequence) getOMReference()).addActivity((Activity)
            getDraggedPattern().getOMReference());
        }
    }
    
    
    public NodeType getNodeType() {
        return NodeType.SEQUENCE;
    }
    
    
    public void updateAccordingToViewFiltersStatus() {
        if (getModel().getFilters().showImplicitSequences()) {
            setBorder(new GroupBorder());
            registerTextElement(getBorder());
            setText(((Sequence) getOMReference()).getName());
        } else {
            setBorder(new NullBorder());
            registerTextElement(null);
        }
    }
    
    
    public void reconnectElements() {
        //Trying to reuse existing connections for optimisation
        BpelEntity[] activities = (BpelEntity[]) ((Sequence) getOMReference())
                .getActivities();
        
        ensureConnectionsCount(connections, activities.length - 1);
        
        for (int i = 0; i < activities.length - 1; i++){
            Pattern p1 = getNestedPattern(activities[i]); 
            Pattern p2 = getNestedPattern(activities[i + 1]); 
            
            VisualElement v1 = p1.getLastElement();
            VisualElement v2 = p2.getFirstElement();
            
            connections.get(i).connect(v1, Direction.BOTTOM, v2, Direction.TOP);
        }
        
    }
    
    
    private Connection getInboundConnection() {
        List<Connection> list = getFirstElement().getIncomingConnections();
        
        for (Connection connection : list) {
            Class c = connection.getClass();
            if ((c == Connection.class) || (c == DefaultConnection.class)) {
                return connection;
            }
        }
        
        return null;
    }
    
    
    private Connection getOutboundConnection() {
        List<Connection> list = getLastElement().getOutcomingConnections();
        
        for (Connection connection : list) {
            Class c = connection.getClass();
            if ((c == Connection.class) || (c == DefaultConnection.class)) {
                return connection;
            }
        }
        
        return null;
    }
}
