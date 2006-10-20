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

package org.netbeans.modules.bpel.design.layout;


import java.awt.Dimension;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.ViewProperties;
import org.netbeans.modules.bpel.design.geom.BoundsBuilder;
import org.netbeans.modules.bpel.design.geom.FDimension;
import org.netbeans.modules.bpel.design.geom.FPoint;
import org.netbeans.modules.bpel.design.geom.FRectangle;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;


public class LayoutManager {
    
    private JLabel label;
    

    
    
    
    /**
     * Coordinates of pattern origins
     * Can be used to align patters along some axis
     */
    private Map<Pattern, FPoint> patternOrigins
            = new HashMap<Pattern, FPoint>();
    
    private DesignView designView;
    
    
    public LayoutManager(DesignView designView) {
        this.designView = designView;
        label = new JLabel();
        label.setFont(ViewProperties.FONT);
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
     * Perform layout for whole diagram starting from root pattern
     * Positioning is performed relative to 0,0
     **/
    public void layout() {
        
       
        Pattern root = designView.getModel().getRootPattern();
        layout(root, HMARGIN, VMARGIN);
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
        FRectangle patternBox = pattern.layoutPattern(this);
        
        if (patternBox == null) {
            /**
             * If pattern does not provide it's bounding box,
             * calculate it automaticaly
             **/
            
            BoundsBuilder box_builder = new BoundsBuilder();
            
            if (composite) {
                CompositePattern cPattern = (CompositePattern) pattern;
                
                for (Pattern nestedPattern : cPattern.getNestedPatterns()) {
                    box_builder.add(nestedPattern.getBounds());
                }
                
                BorderElement border = cPattern.getBorder();
                
                if (border != null) {
                    box_builder.add(border.getBounds());
                }
            }
            
            for (VisualElement element : pattern.getElements()) {
                
                box_builder.add(element.getBounds());
            }
            
            patternBox = box_builder.bounds();
        }
        pattern.setBounds(patternBox);
    }
    
    
    
    
    
    public static void translatePattern(Pattern pattern, float dx, float dy){
        
        FRectangle bounds = pattern.getBounds();
        
        for (VisualElement e : pattern.getElements()) {
            e.setLocation(e.getX() + dx, e.getY()+ dy);
        }
        
        if (pattern instanceof CompositePattern) {
            BorderElement border = ((CompositePattern) pattern).getBorder();
            if (border != null) {
                border.setLocation(border.getX() + dx, border.getY()+ dy);
            }
            for (Pattern p: ((CompositePattern) pattern).getNestedPatterns()){
                translatePattern(p, dx, dy);
            }
        }
        
       
        pattern.setBounds(new FRectangle(bounds.x + dx, bounds.y + dy,
                bounds.width, bounds.height));
    }
    
    
    
    
 public FRectangle setPatternPosition(Pattern pattern, float x, float y) {
        FRectangle bounds = pattern.getBounds();
        translatePattern(pattern, x - bounds.x, y - bounds.y);
        return pattern.getBounds();
    }
    
    
    public FRectangle setPatternCenterPosition(Pattern pattern, float cx, float cy) {
        FRectangle bounds = pattern.getBounds();
        translatePattern(pattern, cx - bounds.getCenterX(), cy - bounds.getCenterY());
        return pattern.getBounds();
        
    }
    
    
        /**
     * Reporting the offset of pattern origin from upper-left corner of pattern box
     **/
    
    public FPoint getOriginOffset(Pattern pattern){
        
        
        FPoint origin = pattern.getOrigin();
        FRectangle bounds = pattern.getBounds();
        if (origin == null){
            //If no origin was provided, taking 0,0 in pattern ccordinates
            //as default origin
            origin = new FPoint(0, 0);
        }
        return new FPoint(origin.x - bounds.x, origin.y - bounds.y);
        
    }
    
   
    public static final float HMARGIN = 1;
    public static final float VMARGIN = 1;
    
    public static final float HSPACING = 20f;
    public static final float VSPACING = 24f;
}