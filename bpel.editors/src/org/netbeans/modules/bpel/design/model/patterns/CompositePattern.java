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
import java.util.List;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.geometry.FPoint;

 import org.netbeans.modules.bpel.design.model.DiagramModel;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.elements.BorderElement;

import org.netbeans.modules.bpel.design.model.elements.VisualElement;


/**
 *
 * @author Alexey Yarmolenko
 */
public abstract class CompositePattern extends Pattern {
    
    private BorderElement border;
    private List<Pattern> patterns = new ArrayList<Pattern>();

    
    public CompositePattern(DiagramModel model) {
        super(model);;
    }

    
    public void setBorder(BorderElement newBorder){
        BorderElement oldBorder = this.border;
        if (oldBorder != null) { oldBorder.setPattern(null); }
        if (newBorder != null) { newBorder.setPattern(this); }
        this.border = newBorder;
    }
    
    
    public BorderElement getBorder(){
        return border;
    }
    
    
    public List<Pattern> getNestedPatterns(){
        return patterns;
    }
    
    
    public Pattern getNestedPattern(){
        if (patterns.size() == 0){
            return null;
        }
        return patterns.get(0);
    }
    
    protected abstract void onAppendPattern(Pattern nestedPattern);
    protected abstract void onRemovePattern(Pattern nestedPattern);
    
    
    protected void appendPattern(Pattern p){
        assert p != null : "Trying to add null pattern to " + this;
        int index = getChildIndex(p);
        int maxIndex = patterns.size();
        index = index < 0 || index > maxIndex ? patterns.size() : index;
        patterns.add(index, p);
        onAppendPattern(p);
    }
    
    /**
     * TODO m
     */
    private int getChildIndex(Pattern p) {
        int prevSiblIndex = -1;
        BpelEntity curEntity = p.getOMReference();
        if (curEntity == null) {
            return prevSiblIndex;
        }
        
        BpelEntity parentEntity = getOMReference();
        
        List<BpelEntity> children = null;
        if (parentEntity != null) {
            children = parentEntity.getChildren();
        }
        
        if (children != null) {
            int patternIndexes = -1;
            for (BpelEntity elem : children) {
                for (Pattern patternElem : patterns) {
                    if (elem != null && elem.equals(patternElem.getOMReference())) {
                        patternIndexes++;
                        break;
                    }
                }

                if (curEntity.equals(elem)) {
                    patternIndexes++;
                    break;
                }
            }
            prevSiblIndex = patternIndexes;
        }
        
        return prevSiblIndex;
    }
    
    protected void removePattern(Pattern p){
        patterns.remove(p);
        onRemovePattern(p);
    }

    
    public VisualElement getNamedElement() {
        return getBorder();
    }
    
    
    public void ensureConnectionsCount(List<Connection> connections, int count) {
        count = Math.max(count, 0);
        
        while (connections.size() > count) {
            connections.remove(connections.size() - 1).remove();
        }
        
        while (connections.size() < count) {
            connections.add(new Connection(this));
        }
    }
    

    public Pattern getNestedPattern(BpelEntity entity) {
        Pattern nested = getModel().getPattern(entity);
        
        
        if (nested == null) return null;
        
        if (nested.getParent()!= this){
            assert false;
            return null;
        }
        return nested;
    }

    
    public Area createSelection() {
        return createOutline();
    }

    
    public boolean isCollapsable() {
        return true;
    }
    
    
    public Area createOutline() {
        Area result = new Area();
        
        BorderElement border = getBorder();
        if (border != null) {
            result.add(new Area(border.getShape()));
        }
        
        for (VisualElement ve : getElements()) {
            if (ve.getWidth() < 2 && ve.getHeight() < 2) continue;
            result.add(new Area(ve.getShape()));
        }
        
        return result;
    }

}
