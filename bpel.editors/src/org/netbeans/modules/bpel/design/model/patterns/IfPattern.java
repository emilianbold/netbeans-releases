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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.design.geometry.FDimension;
import org.netbeans.modules.bpel.design.geometry.FPath;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FRectangle;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.ConnectionManager;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.ContentElement;
import org.netbeans.modules.bpel.design.model.elements.GroupBorder;
import org.netbeans.modules.bpel.design.model.elements.PlaceHolderElement;
import org.netbeans.modules.bpel.design.model.elements.StubElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.model.api.BooleanExpr;


public class IfPattern extends CompositePattern {
    
    private VisualElement forkGateway;
    private VisualElement mergeGateway;
    private PlaceHolderElement thenPlaceHolder;
    private StubElement elseStub;
            
    private List<Connection> connectionsFork = new ArrayList<Connection>();
    private List<Connection> connectionsMerge = new ArrayList<Connection>();
    

    public IfPattern(DiagramModel model) {
        super(model);
    }

    
    protected void onAppendPattern(Pattern nestedPattern) {
        BpelEntity entity = nestedPattern.getOMReference();
        
        if (entity instanceof Else) {
            removeElement(elseStub);
        } else if (!(entity instanceof ElseIf)) {
            removeElement(thenPlaceHolder);
        }
    }

    
    protected void onRemovePattern(Pattern nestedPattern) {
        If ifOM = (If) getOMReference();
        
        if (!elseStub.hasPattern() && (ifOM.getElse() == null)) {
            appendElement(elseStub);
        } else if (!thenPlaceHolder.hasPattern() 
                && (ifOM.getActivity() == null))
        {
            appendElement(thenPlaceHolder);
        }
    }

    
    public VisualElement getFirstElement() {
        return forkGateway;
    }
    

    public VisualElement getLastElement() {
        return mergeGateway;
    }
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        If ifActivity = (If) getOMReference();
        BpelEntity thenBranch = ifActivity.getActivity();
        Else elseBranch = ifActivity.getElse();
        ElseIf[] elseIfBranches = ifActivity.getElseIfs();
        int elseIfCount = (elseIfBranches == null) ? 0 : elseIfBranches.length;
        
        forkGateway.setCenter(0, 0);
        
        double y0 = -forkGateway.getHeight() / 2; // - (elseIfCount > 0) ? 8 : 0; //TODO: Replace with constant
        double y1 = forkGateway.getHeight() / 2 + LayoutManager.VSPACING; 
                //+ ((elseIfCount > 0) ? 8 : 0);
        double x0, x1, x2, y2;
        
        if (thenBranch != null) {
            Pattern p = getNestedPattern(thenBranch);
            FBounds size = p.getBounds();
            FPoint offset = manager.getOriginOffset(p);
            x1 = -offset.x;
            x2 = x1 + size.width;
            y2 = y1 + size.height;
            manager.setPatternPosition(p, x1, y1);
        } else {
            double width = thenPlaceHolder.getWidth();
            double height = thenPlaceHolder.getHeight();
            x1 = -width / 2;
            x2 = x1 + width;
            y2 = y1 + height;
            thenPlaceHolder.setLocation(x1, y1);
        }
        x0 = Math.min(-forkGateway.getWidth() / 2, x1);
        x2 = Math.max(x2, forkGateway.getWidth() / 2) + LayoutManager.HSPACING;
        
        for (int i = 0; i < elseIfCount; i++) {
            Pattern p = getNestedPattern(elseIfBranches[i]);
            FBounds size = p.getBounds();
            manager.setPatternPosition(p, x2, y0);
            
            y2 = Math.max(y2, y0 + size.height);
            x2 += size.width;
            x2 += LayoutManager.HSPACING;
        }
        
        if (elseBranch != null) {
            Pattern p = getNestedPattern(elseBranch);
            FBounds size = p.getBounds();
            manager.setPatternPosition(p, x2, y1);
            y2 = Math.max(y2, y1 + size.height);
            x2 += size.width;
        } else {
            double width = elseStub.getWidth();
            double height = elseStub.getHeight();
            elseStub.setLocation(x2, y1 + thenPlaceHolder.getHeight() / 2);
            y2 = Math.max(y2, y1 + height + thenPlaceHolder.getHeight() / 2);
            x2 += width;
        }
        
        y2 += LayoutManager.VSPACING;
        
        mergeGateway.setCenter(0, y2 + mergeGateway.getHeight() / 2);
        
        y2 += mergeGateway.getHeight();
        getBorder().setClientRectangle( x0, y0, x2 - x0, y2 - y0);
        
        return getBorder().getBounds();
    }

    
    protected void createElementsImpl() {
        setBorder(new GroupBorder());
        registerTextElement(getBorder());
        
        mergeGateway = ContentElement.createIfGateway();
        forkGateway = ContentElement.createIfGateway();
        
        appendElement(mergeGateway);
        appendElement(forkGateway);
        
        thenPlaceHolder = new PlaceHolderElement();
        elseStub = new StubElement();

        appendElement(thenPlaceHolder);
        appendElement(elseStub);
        
        If ifActivity = (If) getOMReference();
        BpelEntity thenBranch = ifActivity.getActivity();
        Else elseBranch = ifActivity.getElse();
        ElseIf[] elseIfBranches = ifActivity.getElseIfs();
        int elseIfCount = (elseIfBranches == null) ? 0 : elseIfBranches.length;
        
        if (thenBranch != null) {
            getModel().createPattern(thenBranch)
                    .setParent(this);
        }
            
        for (int i = 0; i < elseIfCount; i++) {
            getModel().createPattern(elseIfBranches[i])
                    .setParent(this);
        }
        
        if (elseBranch != null) {
            getModel().createPattern(elseBranch)
                    .setParent(this);
        }
    }

    
    public void reconnectElements() {
        If ifActivity = (If) getOMReference();
        BpelEntity thenBranch = ifActivity.getActivity();
        Else elseBranch = ifActivity.getElse();
        ElseIf[] elseIfBranches = ifActivity.getElseIfs();
        
        ensureConnectionsCount(connectionsFork, 0);
        ensureConnectionsCount(connectionsMerge, 0);

        VisualElement thenFirst;
        VisualElement thenLast;
        if (thenBranch != null) {
            Pattern p = getNestedPattern(thenBranch);
            thenFirst = p.getFirstElement();
            thenLast = p.getLastElement();
        } else {
            thenFirst = thenPlaceHolder;
            thenLast = thenPlaceHolder;
        }
        
        VisualElement elseFirst;
        VisualElement elseLast;
        if (elseBranch != null) {
            Pattern p = getNestedPattern(elseBranch);
            elseFirst = p.getFirstElement();
            elseLast = p.getLastElement();
        } else {
            elseFirst = elseStub;
            elseLast = elseStub;
        }
        
        int elseIfCount = (elseIfBranches == null) ? 0 : elseIfBranches.length;
        VisualElement[] elseIfFirst = new VisualElement[elseIfCount];
        VisualElement[] elseIfLast = new VisualElement[elseIfCount];
        for (int i = 0; i < elseIfCount; i++) {
            Pattern p = getNestedPattern(elseIfBranches[i]);
            elseIfFirst[i] = p.getFirstElement();
            elseIfLast[i] = p.getLastElement();
        }

        Connection c1;
        Connection c2; 

        c1 = new Connection(this);
        c2 = new Connection(this);
        ConnectionManager.connectVerticaly(forkGateway, c1, thenFirst, 
                thenLast, c2, mergeGateway);
        connectionsFork.add(c1);
        connectionsMerge.add(c2);
                
        VisualElement prevFork = forkGateway;
        
        for (int i = 0; i < elseIfCount; i++) {
            c1 = new Connection(this);
            c2 = new Connection(this);
            ConnectionManager.connectVerticaly(prevFork, c1, elseIfFirst[i], 
                    elseIfLast[i], c2, mergeGateway);
            c1.setTarget(c1.getTarget(), Direction.LEFT);
            connectionsFork.add(c1);
            connectionsMerge.add(c2);
            c1.setPaintSlash(true);
            prevFork = elseIfFirst[i];
        }
        
        c1 = new Connection(this);
        c2 = new Connection(this);
        c1.setPaintSlash(true);
        c1.setPaintArrow(elseFirst != elseStub);
        ConnectionManager.connectVerticaly(prevFork, c1, elseFirst, 
                elseLast, c2, mergeGateway);
        connectionsFork.add(c1);
        connectionsMerge.add(c2);
    }
    
    

    public String getDefaultName() {
        return "If"; // NOI18N
    }

    public NodeType getNodeType() {
        return NodeType.IF;
    }


    public Area createSelection() {
        Area res = new Area(getBorder().getShape());
        res.subtract(new Area(forkGateway.getShape()));
        res.subtract(new Area(mergeGateway.getShape()));
        return res;
    }


    public void createPlaceholders(Pattern draggedPattern,
            Collection<PlaceHolder> placeHolders) {
        if (draggedPattern.getOMReference() instanceof Activity) {
            If ifOM = (If) getOMReference();

            if (ifOM.getActivity() == null) {
                placeHolders.add(new ThenActivityPlaceHolder(draggedPattern));
            }

            if (ifOM.getElse() == null) {
                placeHolders.add(new ElsePlaceHolder(draggedPattern));
            }
        } else if (draggedPattern.getOMReference() instanceof ElseIf) {
            ElseIf elseIf = (ElseIf) draggedPattern.getOMReference();

            if (elseIf.getParent() != getOMReference()) return;

            int elseIfIndex = ((If) getOMReference()).indexOf(ElseIf.class, 
                    elseIf);
            
            int connectionsCount = connectionsFork.size();
            
            
            FPoint center;
            
            if (!forkGateway.getIncomingConnections().isEmpty()) {
                FPath inp = forkGateway.getIncomingConnections().get(0).getPath();
                double inpLength = inp.length();
                center = inp.point(Math.max(0.5f, (inpLength - 4) / inpLength));
            } else {
                center = new FPoint(forkGateway.getCenterX(), 
                        forkGateway.getY() - 4);
            }
            
            
            placeHolders.add(new ElseIfReorderingPlaceHolder(draggedPattern, 
                        center.x, center.y, 0));
            
            if ((connectionsCount > 2) && (elseIfIndex != 0)) {
                Connection c = connectionsFork.get(1);
                FPath path = c.getPath();

                path = path.subtract(((ElseIfPattern) c.getTarget().getPattern())
                        .getBorder().getShape());

                placeHolders.add(new ElseIfReorderingPlaceHolder(draggedPattern, 
                        path, 1));
            }
            
            
            for (int i = 2; i < connectionsCount - 1; i++) {
                if ((elseIfIndex + 1 == i) || (elseIfIndex + 2 == i)) {
                    continue;
                }
                
                Connection c = connectionsFork.get(i);
                
                FPath path = c.getPath();
                
                path = path.subtract(((ElseIfPattern) c.getSource().getPattern())
                        .getBorder().getShape());
                path = path.subtract(((ElseIfPattern) c.getTarget().getPattern())
                        .getBorder().getShape());
                
                placeHolders.add(new ElseIfReorderingPlaceHolder(draggedPattern, 
                        path, i));
            }
            
            
            if ((connectionsCount > 1) 
                    && (elseIfIndex + 1 != connectionsCount - 1) 
                    && (elseIfIndex + 2 != connectionsCount - 1)) 
            {
                Connection c = connectionsFork.get(connectionsCount - 1);
                
                FPath path = c.getPath();
                
                for (Pattern p = c.getTarget().getPattern(); p != this; 
                        p = p.getParent())
                {
                    if (!(p instanceof CompositePattern)) {
                        continue;
                    }
                    
                    BorderElement border = ((CompositePattern) p).getBorder();
                    
                    if (border == null) {
                        continue;
                    }

                    path = path.subtract(border.getShape());
                }

                Pattern sourcePattern = c.getSource().getPattern();
                
                if (sourcePattern instanceof ElseIfPattern) {
                    path = path.subtract(((ElseIfPattern) sourcePattern).getBorder()
                            .getShape());
                }
                
                
                FPoint p = path.point(0.5f * Math.min(1, 
                        LayoutManager.HSPACING / path.length()));
                
                
                placeHolders.add(new ElseIfReorderingPlaceHolder(draggedPattern, 
                        p.x, p.y, connectionsCount - 1));
            }
        }
    }
    
    
    class ThenActivityPlaceHolder extends PlaceHolder {
        public ThenActivityPlaceHolder(Pattern draggedPattern) {
            super(IfPattern.this, draggedPattern,
                    thenPlaceHolder.getCenterX(), thenPlaceHolder.getCenterY());
        }
        
        public void drop() {

            ((If) getOMReference()).setActivity(
                    (Activity) getDraggedPattern().getOMReference());
            
        }
    }    
    

    class ElsePlaceHolder extends PlaceHolder {
        public ElsePlaceHolder(Pattern draggedPattern) {
            super(IfPattern.this, draggedPattern,
                    elseStub.getCenterX(), elseStub.getCenterY());
        }
        
        public void drop() {
            Else elseOM = getOMReference().getBpelModel().getBuilder()
                    .createElse();
            
            elseOM.setActivity((Activity) getDraggedPattern().getOMReference());
            ((If) getOMReference()).setElse(elseOM);
        }
    }    
    

    class ElseIfReorderingPlaceHolder extends PlaceHolder {
        
        private int placeHolderPosition;
        private int elseIfIndex;
        
        public ElseIfReorderingPlaceHolder(Pattern draggedPattern, FPath cPath,
                int placeHolderPosition) 
        {
            super(IfPattern.this, draggedPattern, cPath);
            init(placeHolderPosition);
        }

        
        public ElseIfReorderingPlaceHolder(Pattern draggedPattern, float cx,
                float cy, int placeHolderPosition) 
        {
            super(IfPattern.this, draggedPattern, cx, cy);
            init(placeHolderPosition);
        }
        
        
        private void init(int placeHolderPosition) {
            this.placeHolderPosition = placeHolderPosition;
            
            If ifOM = (If) getOwnerPattern().getOMReference();
            ElseIf elseIfOM = (ElseIf) getDraggedPattern().getOMReference();
            
            this.elseIfIndex = ifOM.indexOf(ElseIf.class, elseIfOM);
        }
        
        
        public void drop() {
            If ifOM = (If) getOwnerPattern().getOMReference();
            ElseIf elseIfOM = (ElseIf) getDraggedPattern().getOMReference();
            
            if (placeHolderPosition == 0) {
                ifOM.insertElseIf(elseIfOM, 0);
                
                ElseIf newElseIf = ifOM.getElseIf(0);
                
                
                BooleanExpr expr1 = ifOM.getCondition();
                ExtendableActivity activity1 = ifOM.getActivity();
                
                if (expr1 != null) {
                    expr1 = (BooleanExpr) expr1.cut();
                }
                
                if (activity1 != null) {
                    activity1 = (ExtendableActivity) activity1.cut();
                }
                
                BooleanExpr expr2 = newElseIf.getCondition();
                ExtendableActivity activity2 = newElseIf.getActivity();
                
                if (expr2 != null) {
                    expr2 = (BooleanExpr) expr2.cut();
                }
                
                if (activity2 != null) {
                    activity2 = (ExtendableActivity) activity2.cut();
                }
                    
                
                if (expr2 != null) {
                    ifOM.setCondition(expr2);
                }
                
                if (activity2 != null) {
                    ifOM.setActivity(activity2);
                }
                
                if (expr1 != null) {
                    newElseIf.setCondition(expr1);
                }
                
                if (activity1 != null) {
                    newElseIf.setActivity(activity1);
                }
            } else if (placeHolderPosition < elseIfIndex + 2) {
                //elseIfOM = (ElseIf) elseIfOM.cut();
                ifOM.insertElseIf(elseIfOM, placeHolderPosition - 1);
            } else {
                //elseIfOM = (ElseIf) elseIfOM.cut();
                ifOM.insertElseIf(elseIfOM, placeHolderPosition - 2);
            }
        }
    }
}
