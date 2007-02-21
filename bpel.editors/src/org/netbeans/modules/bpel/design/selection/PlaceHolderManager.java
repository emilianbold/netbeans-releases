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


package org.netbeans.modules.bpel.design.selection;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.geometry.FPoint;

import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.design.model.patterns.CompensatePattern;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.Else;
import org.openide.ErrorManager;

public class PlaceHolderManager implements DnDTool {
    
    private List<PlaceHolder> placeHolders = new ArrayList<PlaceHolder>();
    private PlaceHolder currentPlaceHolder;
    private DesignView designView;
    private Pattern draggedPattern;
    
    private PlaceHolder defaultPlaceholder;
    
    public PlaceHolderManager(DesignView designView) {
        this.designView = designView;
    }
    
    
    public boolean isEmpty() {
        return placeHolders.isEmpty();
    }
    
    
    public void clear() {
        if (!isEmpty()) {
            placeHolders.clear();
            currentPlaceHolder = null;
            draggedPattern = null;
            getDesignView().repaint();
            defaultPlaceholder = null;
        }
        //getDesignView().getButtonsManager().update();
    }
    
    
    
    public DesignView getDesignView() {
        return designView;
    }
    
    public Pattern getDraggedPattern() {
        return draggedPattern;
    }
    
    
    
    private PlaceHolder findPlaceholder(float px, float py) {
        for (int i = placeHolders.size() - 1; i >= 0; i--) {
            PlaceHolder p = placeHolders.get(i);
            if (p.contains(px, py)) { return p; }
        }
        return defaultPlaceholder;
    }
    
    
    public void init(Pattern draggedPattern) {
        this.draggedPattern = draggedPattern;
        this.defaultPlaceholder = null;
        createPlaceHolders(getDesignView().getRootPattern());
        //getDesignView().getButtonsManager().clear();
    }
    
    
    private void createPlaceHolders(Pattern pattern) {
        
        if (getDraggedPattern() == pattern) return;
        if (pattern.isNestedIn(getDraggedPattern())) return;
        
        //special case for Compensate activity which breaks common rule. see bug 6364908
        if (!(getDraggedPattern() instanceof CompensatePattern &&
                !(pattern.getOMReference() instanceof CompensatableActivityHolder))) {
            pattern.createPlaceholders(getDraggedPattern(), placeHolders);
        }
        
        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            for (Pattern p : composite.getNestedPatterns()) {
                createPlaceHolders(p);
            }
        }

        //Try to create "implicit sequence" placeholders
        if (getDraggedPattern().getOMReference() instanceof Activity){
            ImpliciteSequencePlaceHolder.create(getDraggedPattern(), pattern,
                    placeHolders);
        }
        
        
        /*
         * Find first placeholder marked as default
         * This placheholder will recieve drop if element was dropped on whitespace
         */
       
        for (PlaceHolder placeHolder: placeHolders ){
            if (placeHolder instanceof DefaultPlaceholder){
                defaultPlaceholder = placeHolder;
                break;
            }
        }
    }
    
    
    public void move(FPoint mp) {
        if (isEmpty()) { return; }
        
        PlaceHolder newCurrentPlaceHolder = findPlaceholder(mp.x, mp.y);
        PlaceHolder oldCurrentPlaceHolder = currentPlaceHolder;
        
        if (oldCurrentPlaceHolder != newCurrentPlaceHolder) {
            if (oldCurrentPlaceHolder != null) {
                oldCurrentPlaceHolder.dragExit();
            }
            
            if (newCurrentPlaceHolder != null) {
                newCurrentPlaceHolder.dragEnter();
            }
            
            currentPlaceHolder = newCurrentPlaceHolder;
            getDesignView().repaint();
        }
    }
    public boolean isValidLocation(){
        return (currentPlaceHolder != null);
    }
    
    
    public void drop(FPoint mp) {
        if (isEmpty()) {
            return;
        }
        PlaceHolder targetPlaceholder = findPlaceholder(mp.x, mp.y);
        
        if (targetPlaceholder == null) {
            targetPlaceholder = defaultPlaceholder;
        }
        if (targetPlaceholder != null){
            try {
                
                BpelEntity activity = (BpelEntity)getDraggedPattern().getOMReference();
                BpelEntity activityParent = (activity != null) 
                        ? activity.getParent() : null;
                
                if (activityParent != null){
                    activity.cut();
                    
                    if (activityParent instanceof Else) {
                        BpelContainer elseParent = activityParent.getParent();
                        if (elseParent != null) {
                            elseParent.remove(activityParent);
                        }
                    }
                }
                targetPlaceholder.drop();
                
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        
        
        clear();
    }
    
    
    public void paint(Graphics2D g2) {
        if (isEmpty()) {
            return;
        }
        for (PlaceHolder p : placeHolders) {
            p.paint(g2);
        }
    }
    
    
}
