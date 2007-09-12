/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.bpel.design.model.patterns;



import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.references.BpelReference;

/**
 *
 * @author Alexey Yarmolenko
 */
public class PartnerLinksPattern extends CompositePattern {
    
    public PartnerLinksPattern(DiagramModel model) {
        super(model);
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
    
    
    protected void onAppendPattern(Pattern p) {
    }
    
    
    protected void onRemovePattern(Pattern p) {
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
        
        double width = 0;
        double ypos = 0;
        
        for (Pattern p: getNestedPatterns()){
            FBounds bounds = p.getBounds();
            width = Math.max(width, bounds.width);
            manager.setPatternPosition(p, 0, ypos);
            ypos += bounds.height + LayoutManager.HSPACING;
        }
        
        return new FBounds(0, 0, width, ypos); //getBorder().getBounds();
    }
    
    
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


