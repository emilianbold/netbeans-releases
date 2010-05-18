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

package org.netbeans.modules.bpel.design.layout;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.PartnerLinksPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

public class LayoutManager {
    
    
    
    public LayoutManager() {

    }
    
    /**
     * Perform layout for whole diagram starting from root pattern
     * Positioning is performed relatice to specified coordinates
     **/
    
    public void layout(Pattern pattern, float x, float y) {
        if (pattern != null) {
            /**
             * Pass ONE: each pattern should position it's elements and nested patterns.
             * Starting from LEAF patterns
             **/
            positionElements(pattern);
            
            setPatternPosition(pattern, x, y);
        }
    }
    
    /**
     * Perform layout for diagram subtree starting from specoified pattern
     * Positioning is performed relative to 0,0
     **/
    public void layout(Pattern pattern) {
        
        layout(pattern, HMARGIN, VMARGIN);
    }
    
    
    
 
    
    private void positionElements(Pattern pattern) {
        
        boolean composite = pattern instanceof CompositePattern;
        
        if (composite) {
            CompositePattern cPattern = (CompositePattern) pattern;
            
            for (Pattern nestedPattern : cPattern.getNestedPatterns()) {
                positionElements(nestedPattern);
            }
        }
        
        //Pattern can optionaly report it's sizes for optimisation
        FBounds patternBox = pattern.layoutPattern(this);
        
        if (patternBox == null) {
            /**
             * If pattern does not provide it's bounding box,
             * calculate it automaticaly
             **/
            
            List<FBounds> boundsList = new ArrayList<FBounds>();
            
            if (composite) {
                CompositePattern cPattern = (CompositePattern) pattern;
                
                for (Pattern nestedPattern : cPattern.getNestedPatterns()) {
                    boundsList.add(nestedPattern.getBounds());
                }
                
                BorderElement border = cPattern.getBorder();
                
                if (border != null) {
                    boundsList.add(border.getBounds());
                }
            }
            
            for (VisualElement element : pattern.getElements()) {
                boundsList.add(element.getBounds());
            }
            
            patternBox = new FBounds(boundsList);
        }
        
        pattern.setBounds(patternBox);
    }
    
    
    
    
    
    public static void translatePattern(Pattern pattern, double dx, double dy) {
        
        if (pattern instanceof PartnerLinksPattern){
            return;
        }
        
        FBounds bounds = pattern.getBounds();
        
        for (VisualElement e : pattern.getElements()) {
            e.setLocation(e.getX() + dx, e.getY()+ dy);
        }
        
        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            
            if (border != null) {
                border.setLocation(border.getX() + dx, border.getY() + dy);
            }
            
            for (Pattern p : ((CompositePattern) pattern).getNestedPatterns()) {
                translatePattern(p, dx, dy);
            }
        }
        
        pattern.setBounds(bounds.translate(dx, dy));
    }
    
    
    
    
    public FBounds setPatternPosition(Pattern pattern, double x, double y) {
        FBounds bounds = pattern.getBounds();
        translatePattern(pattern, x - bounds.x, y - bounds.y);
        return pattern.getBounds();
    }
    
    
    public FBounds setPatternCenterPosition(Pattern pattern, 
            double cx, double cy) 
    {
        FBounds bounds = pattern.getBounds();
        translatePattern(pattern, 
                cx - bounds.getCenterX(), 
                cy - bounds.getCenterY());
        return pattern.getBounds();
    }
    
    
    /**
     * Reporting the offset of pattern origin from upper-left corner of pattern box
     **/
    
    public FPoint getOriginOffset(Pattern pattern){
        FPoint origin = pattern.getOrigin();
        FBounds bounds = pattern.getBounds();
        
        if (origin == null){
            // If no origin was provided, taking 0,0 in pattern ccordinates
            // as default origin
            origin = new FPoint(0, 0);
        }
        
        return new FPoint(origin.x - bounds.x, origin.y - bounds.y);
    }
    
   
    public static final float HMARGIN = 1;
    public static final float VMARGIN = 1;
    
    public static final float HSPACING = 12f;
    public static final float VSPACING = 18f;
}
