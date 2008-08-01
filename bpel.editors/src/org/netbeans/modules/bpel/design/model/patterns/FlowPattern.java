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
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FPath;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.ConnectionManager;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.Sequence;

public class FlowPattern extends CompositePattern {
    
    private VisualElement startGateway;
    private VisualElement endGateway;
    
    private PlaceHolderElement placeHolder;
    
    private List<Connection> connectionsStart = new ArrayList<Connection>();
    private List<Connection> connectionsEnd = new ArrayList<Connection>();
    
    private Connection startPlaceHolderConnection;
    private Connection endPlaceHolderConnection;
    
    public FlowPattern(DiagramModel model) {
        super(model);
    }
    
    public VisualElement getFirstElement() {
        return startGateway;
    }
    
    public VisualElement getLastElement() {
        return endGateway;
    }
    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        getBorder().setLabelText(getDefaultName());
        registerTextElement(getBorder());
        
        startGateway = ContentElement.createFlowGateway();
        appendElement(startGateway);
        startGateway.setLabelText(""); // NOI18N
        
        endGateway = ContentElement.createFlowGateway();
        appendElement(endGateway);
        endGateway.setLabelText(""); // NOI18N
        
        placeHolder = new PlaceHolderElement();
        appendElement(placeHolder);
        
        Flow flow = (Flow) getOMReference();
        
        BpelEntity[] activities = flow.getActivities();
        
        for (BpelEntity a : activities) {
            Pattern p = getModel().createPattern(a);
            p.setParent(this);
        }
    }
    
    public FBounds layoutPattern(LayoutManager manager) {
        Collection<Pattern> patterns = getNestedPatterns();
        boolean empty = patterns.isEmpty();
        
        double height = 0;
        double width = 0;
        
        if (empty) {
            width = placeHolder.getWidth();
            height = placeHolder.getHeight();

           placeHolder.setLocation(-width / 2, -height / 2);
        } else {
            Flow flow = (Flow) getOMReference();
            ExtendableActivity[] activities = flow.getActivities();
            
            for (ExtendableActivity a : activities) {
                Pattern p = getNestedPattern(a);
                FDimension pSize =  p.getBounds().getSize();
                height = Math.max(height, pSize.height);
                width += pSize.width;
            }
            
            width += (patterns.size() - 1) * LayoutManager.HSPACING;
            
            double x = -width / 2;

            for (ExtendableActivity a : activities) {
                Pattern p = getNestedPattern(a);
                FBounds pBounds =  p.getBounds();
                float y = -pBounds.height / 2;
                manager.setPatternPosition(p, x, y);
                x += pBounds.width + LayoutManager.HSPACING;
            }
        }

        double y1 = -(height / 2 + startGateway.getHeight() / 2
                + LayoutManager.VSPACING);
        double y2 = height / 2 + endGateway.getHeight() / 2
                + LayoutManager.VSPACING;
        
        startGateway.setCenter(0, y1);
        endGateway.setCenter(0, y2);
        
        width = Math.max(width, startGateway.getWidth());
        
        double x0 = -width  / 2;
        double y0 = -height / 2 - startGateway.getHeight()
                - LayoutManager.VSPACING;
        
        height += startGateway.getHeight() + endGateway.getHeight()
                + 2 * LayoutManager.VSPACING;
        
        getBorder().setClientRectangle( x0, y0, width, height);
        return getBorder().getBounds();
    }
    
    protected void onAppendPattern(Pattern p) {
        if (placeHolder.getPattern() == this) {
            removeElement(placeHolder);
        }
    }
    
    protected void onRemovePattern(Pattern p) {
        Flow flow = (Flow) getOMReference();
        if (flow.sizeOfActivities() == 0) {
            appendElement(placeHolder);
        }
    }
    
    public String getDefaultName() {
        return "Flow"; // NOI18N
    }
    
    public void createPlaceholders(Pattern draggedPattern, 
            Collection<PlaceHolder> placeHolders) 
    {
        if (draggedPattern == this) return;
        if (isNestedIn(draggedPattern)) return;
        if (!(draggedPattern.getOMReference() instanceof Activity)) return;
        
        if (placeHolder.getPattern() != null) {
            placeHolders.add(new FirstPlaceHolder(draggedPattern));
        } else {
            placeHolders.add(new NextPlaceHolder(draggedPattern));
            
            Flow flow = (Flow) getOMReference();
            
            ExtendableActivity[] activities = flow.getActivities();
            
            for (int i = 0; i < activities.length; i++) {
                ExtendableActivity activity = activities[i];
                
                if (activity instanceof Sequence) continue;
                
                Pattern p = getNestedPattern(activity);
                
                if (p == draggedPattern) continue;
                
                FPath start = connectionsStart.get(i).getPath();
                FPath end = connectionsEnd.get(i).getPath();
                        
                if (p instanceof CompositePattern) {
                    BorderElement border = ((CompositePattern) p).getBorder();
                    
                    if (border != null) {
                        FShape borderShape = border.getShape();
                        
                        start = start.subtract(borderShape);
                        end = end.subtract(borderShape);
                    }
                }
                    
                placeHolders.add(new BeforeImpliciteSequencePlaceHolder(i, 
                        draggedPattern, start));
                placeHolders.add(new AfterImpliciteSequencePlaceHolder(i, 
                        draggedPattern, end));
            }
        }
    }
    
    public void reconnectElements() {
        BpelEntity[] activities = ((Flow) getOMReference()).getActivities();
        
        ensureConnectionsCount(connectionsStart, activities.length);
        ensureConnectionsCount(connectionsEnd, activities.length);
        
        for (int i = 0; i < activities.length; i++) {
            Pattern p = getNestedPattern(activities[i]);
            
            Connection cs = connectionsStart.get(i);
            Connection ce = connectionsEnd.get(i);
            
            ConnectionManager.connectVerticaly(startGateway, cs, p,
                    ce, endGateway);
        }
        

        if (placeHolder.getPattern() != null) {
            if (startPlaceHolderConnection == null) {
                startPlaceHolderConnection = new Connection(this);
            }

            if (endPlaceHolderConnection == null) {
                endPlaceHolderConnection = new Connection(this);
            }

            ConnectionManager.connectVerticaly(startGateway, 
                    startPlaceHolderConnection, placeHolder, 
                    endPlaceHolderConnection, endGateway);
        } else {
            if (startPlaceHolderConnection != null) {
                startPlaceHolderConnection.remove();
                startPlaceHolderConnection = null;
            }

            if (endPlaceHolderConnection != null) {
                endPlaceHolderConnection.remove();
                endPlaceHolderConnection = null;
            }
        }
    }
    
    public NodeType getNodeType() {
        return NodeType.FLOW;
    }

    public Area createSelection() {
        Area a = new Area(getBorder().getShape());
        a.subtract(new Area(startGateway.getShape()));
        a.subtract(new Area(endGateway.getShape()));
        return a;
    }
    
    private double getNextPlaceHolderX() {
        BorderElement border = getBorder();
        return border.getX() + border.getWidth() - border.getInsets().right / 2;
    }
    
    class FirstPlaceHolder extends PlaceHolder {
        public FirstPlaceHolder(Pattern draggedPattern) {
            super(FlowPattern.this, draggedPattern, placeHolder.getCenterX(),
                    placeHolder.getCenterY());
        }
        
        public void drop() {
            Pattern p = getDraggedPattern();
            ((Flow) getOMReference()).addActivity((Activity) p.getOMReference());
            
        }
    }

    class NextPlaceHolder extends PlaceHolder {
        
        private FPath tempPath;
        
        public NextPlaceHolder(Pattern draggedPattern) {
            super(FlowPattern.this, draggedPattern, getNextPlaceHolderX(),
                    getBorder().getCenterY());
            
            tempPath = createTempPath();
        }
        

        public void drop() {
            Pattern p = getDraggedPattern();
            ((Flow) getOMReference()).addActivity((Activity) p.getOMReference());
        }

        public void paint(Graphics2D g2) {
            Connection.paintConnection(g2, tempPath, false, true, false, false, 
                    null);
            super.paint(g2);
        }
        
        private FPath createTempPath() {
            double x1 = startGateway.getX() + startGateway.getWidth();
            double y1 = startGateway.getY() + startGateway.getHeight();
            for (Connection c : startGateway.getOutcomingConnections()) {
                if (c.getSourceDirection() == Direction.RIGHT) {
                    y1 = Math.min(c.getPath().point(0).y, y1);
                }
            }
            y1 = (y1 + startGateway.getY()) / 2;

            double x2 = endGateway.getX() + endGateway.getWidth();
            double y2 = endGateway.getY();
            for (Connection c : endGateway.getIncomingConnections()) {
                if (c.getTargetDirection() == Direction.RIGHT) {
                    y2 = Math.max(c.getPath().point(1).y, y2);
                }
            }
            y2 = (y2 + endGateway.getY() + endGateway.getHeight()) / 2;
            
            double xp = getNextPlaceHolderX();
            
            FPath p = new FPath(x1, y1, xp, y1, xp, y2, x2, y2);
            
            return p.round(2);
        }
    }
    
    private class BeforeImpliciteSequencePlaceHolder extends PlaceHolder {
        
        private int index;
        
        public BeforeImpliciteSequencePlaceHolder(int index, 
                Pattern draggedPattern, FPath path) 
        {
            super(FlowPattern.this, draggedPattern, 
                    path.point(Math.max(0, 1f - 10f / path.length())));
            
            Flow flow = (Flow) FlowPattern.this.getOMReference();
            ExtendableActivity dragged = (ExtendableActivity) draggedPattern.getOMReference();
            
            this.index = index;
            
            if (dragged.getParent() == flow) {
                int oldIndex = flow.indexOf(ExtendableActivity.class, dragged);
                if (oldIndex < index) {
                    this.index--;
                }
            }
        }
        
        public void drop() {
            Flow flow = (Flow) getOwnerPattern().getOMReference();
            
            ExtendableActivity dragged = (ExtendableActivity) getDraggedPattern().getOMReference();
            ExtendableActivity activity = (ExtendableActivity) flow.getActivity(index).cut();
            
            Sequence sequence = flow.getBpelModel().getBuilder().createSequence();
            
            sequence.addActivity(dragged);
            sequence.addActivity(activity);
            
            flow.insertActivity(sequence, index);
            setName(flow, sequence);
        }
    }

    private class AfterImpliciteSequencePlaceHolder extends PlaceHolder {
        
        private int index;
        
        public AfterImpliciteSequencePlaceHolder(int index, 
                Pattern draggedPattern, FPath path) 
        {
            super(FlowPattern.this, draggedPattern, 
                    path.point(Math.min(1, 10f / path.length())));
            
            Flow flow = (Flow) FlowPattern.this.getOMReference();
            ExtendableActivity dragged = (ExtendableActivity) draggedPattern.getOMReference();
            
            this.index = index;
            
            if (dragged.getParent() == flow) {
                int oldIndex = flow.indexOf(ExtendableActivity.class, dragged);
                if (oldIndex < index) {
                    this.index--;
                }
            }
        }
         
        public void drop() {
            Flow flow = (Flow) getOwnerPattern().getOMReference();
            
            ExtendableActivity dragged = (ExtendableActivity) getDraggedPattern().getOMReference();
            ExtendableActivity activity = (ExtendableActivity) flow.getActivity(index).cut();
            
            Sequence sequence = flow.getBpelModel().getBuilder().createSequence();
            
            sequence.addActivity(activity);
            sequence.addActivity(dragged);
            
            flow.insertActivity(sequence, index);
            setName(flow, sequence);
        }
    }

    private void setName(Flow flow, Sequence sequence) {
      String name = FLOW_SEQUENCE_NAME;
      int index = 1;

      while (true) {
        if ( !hasName(flow, name)) {
          break;
        }
//System.out.println("++1");
        name = FLOW_SEQUENCE_NAME + (index++);
      }
      try {
        sequence.setName(name);
      }
      catch (VetoException e) {}
    }

    private boolean hasName(BpelEntity entity, String name) {
      if (entity instanceof Named && name.equals(((Named) entity).getName())) {
        return true;
      }
      List<BpelEntity> children = entity.getChildren();

      for (BpelEntity child : children) {
        if (hasName(child, name)) {
          return true;
        }
      }
      return false;
    }

    private static final String FLOW_SEQUENCE_NAME = "FlowSequence"; // NOI18N
}
