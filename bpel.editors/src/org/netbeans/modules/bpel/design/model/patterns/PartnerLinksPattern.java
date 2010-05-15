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



import java.util.List;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerRole;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.references.BpelReference;

/**
 *
 * @author Alexey Yarmolenko
 */
public class PartnerLinksPattern extends CompositePattern {
    
    private List<Pattern> providers = new ArrayList<Pattern>();
    private List<Pattern> consumers = new ArrayList<Pattern>();
    
            
    public PartnerLinksPattern(DiagramModel model) {
        super(model);
    }

    public List<Pattern> getConsumers() {
        return consumers;
    }

    public List<Pattern> getProviders() {
        return providers;
    }
    
    public boolean isSelectable() {
        return false;
    }
    
    public VisualElement getFirstElement() {
        return null;
    }
    
    public VisualElement getLastElement() {
        return null;
    }
    
    public DiagramView getView() {
        return null;
    }
    
    protected void onAppendPattern(Pattern p) {
        if (((PartnerlinkPattern) p).getType() == PartnerRole.PROVIDER){
            providers.add(p);
        } else {
            consumers.add(p);
        } 
    }
    
    
    protected void onRemovePattern(Pattern p) {
        
        consumers.remove(p);
        providers.remove(p);
    }
    
    protected void createElementsImpl() {
        
        // setBorder(new NullBorder());
        
        
        PartnerLinkContainer plc = (PartnerLinkContainer) getOMReference();
        if(plc == null || plc.getPartnerLinks() == null){
            return;
        }

        for (PartnerLink pl: plc.getPartnerLinks()){
            
            Pattern p = getModel().createPattern(pl);
            
            p.setParent(this);
        }
        
        
    }
    
   
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        
        //just identify thhe maximum width
        //PartnerLinks will be positioned, when all process activities are positioned
        //see optimizePositions method
        
        double ypos = 0;
        
        for (Pattern p: consumers){
            FBounds bounds = p.getBounds();
            manager.setPatternPosition(p, 0, ypos);
            ypos += bounds.height + LayoutManager.HSPACING;
        }
        
        ypos = 0;
        for (Pattern p: providers){
            FBounds bounds = p.getBounds();
            manager.setPatternPosition(p, 0, ypos);
            ypos += bounds.height + LayoutManager.HSPACING;
        }
        
        return null;
    }
    
/*    
    public void optimizePositions(LayoutManager manager) {
        FBounds oldBounds = getBounds();
        double ypos = oldBounds.y;
        double width = 0;
        
        
        
        for (Position pos: new PositionCalculator().getResult()){
            FBounds bounds = pos.pattern.getBounds();
            
            double y = Math.max(pos.position - bounds.height / 2, ypos);
            
            manager.setPatternPosition(pos.pattern, bounds.x, y);
            
            width = Math.max( width, bounds.width);
            
            ypos = y + bounds.height + LayoutManager.HSPACING;
            // getBorder().setClientRectangle(0, 0, width, ypos);
        }
        
        
        setBounds(new FBounds(oldBounds.x, oldBounds.y, 
                oldBounds.width, ypos - oldBounds.y));
    }
    */
    public String getDefaultName() {
        return "Process";
    }
    
    
    public Area createSelection() {
        return new Area();
        
    }
    
    
    private class PositionCalculator{
        
        
        private ArrayList<Position> optimalPositions;
        
        public PositionCalculator(){
            
            optimalPositions = new ArrayList<Position>(getNestedPatterns().size());
            
            //initialise the list of optimal y position per pattern
            for (Pattern p: getNestedPatterns()){
                optimalPositions.add( new Position(p));
            }
            
            //iterate over patterns tree and fill this list
            calculatePositions(getModel().getRootPattern());
            
            //Complete calculations
            for (Position p : optimalPositions){
                if (p.count > 0){
                    p.position = p.position / p.count;
                }
            }
            
            //Sort elements by Y ccordinate
            Collections.sort(optimalPositions);
            
        }
        
        public ArrayList<Position> getResult(){
            return this.optimalPositions;
        }
        
        private void calculatePositions(Pattern pattern){
            BpelEntity entity = pattern.getOMReference();
            if(entity instanceof PartnerLinkReference){
                BpelReference<PartnerLink> pl_ref =
                        ((PartnerLinkReference) entity).getPartnerLink();
                if ( pl_ref != null && !pl_ref.isBroken()) {
                    adjustPosition(
                            getModel().getPattern(pl_ref.get()),
                            pattern.getBounds().getCenterY()
                            );
                    
                    
                }
            }
            if (pattern instanceof CompositePattern){
                for(Pattern p:((CompositePattern) pattern).getNestedPatterns()){
                    calculatePositions(p);
                }
            }
        }
        private void adjustPosition(Pattern pattern, double new_value){
            Position pos = null;
            for (Position p: optimalPositions){
                if(p.pattern == pattern){
                    p.position += new_value;
                    p.count++;
                }
            }
            
        }
    }
    
    class Position implements Comparable<Position>{
        public double position = 0;
        public double count = 0;
        public Pattern pattern;
        
        public Position(Pattern pattern){
            this.pattern = pattern;
            
        }
        
        public int compareTo(Position p) {
            return Double.compare(this.position, p.position);
        }
    }
}


