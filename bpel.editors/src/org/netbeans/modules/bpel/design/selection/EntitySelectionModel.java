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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.support.UniqueId;


public class EntitySelectionModel {
    
    private UniqueId selectedID;
    private DiagramModel model;
    private Pattern selectedPattern;           
    
    private List<DiagramSelectionListener> listeners =
            new ArrayList<DiagramSelectionListener>();
    
   // private ArrayList<UniqueId> savedSelection = new ArrayList<UniqueId>();
    
    /** Creates a new instance of PatternSelectionModel */
    public EntitySelectionModel(DiagramModel model){
        this.model = model;
    }
    
    public void addSelectionListener(DiagramSelectionListener l) {
        listeners.add(l);
    }
    
    
    public void removeSelectionListener(DiagramSelectionListener l) {
        listeners.remove(l);
    }

    /**
     * method enforces the selection rule: put the selection on element which is visible on diagram
     */
    public void setSelectedPattern(Pattern newSelected){
        if ( newSelected != null){
            setSelected(newSelected.getOMReference());
        } else {
            setSelected((BpelEntity) null);
        }
    }

    public void setSelected(BpelEntity newSelected) {
        
        while (newSelected != null){
            Pattern p = model.getPattern(newSelected);
            
            if (p != null && p.isSelectable() && p.isInModel()){
                break;
            }
            
            newSelected = newSelected.getParent();
        }
        
        
        UniqueId oldSelectedID = this.selectedID;
        
        UniqueId newSelectedID = (newSelected != null)?
            newSelected.getUID() : null;
        
        if (oldSelectedID != newSelectedID) {
            
            this.selectedID = newSelectedID;
            this.selectedPattern = getSelectedPattern();
            
            for (DiagramSelectionListener l: listeners){
                l.selectionChanged(model.getEntity(oldSelectedID), newSelected);
            }
        }
    }
    /**
     * Handles the situation when currently selected pattern is about to be removed.
     * Tries to move selection to first available:
     * 1. next sibling 
     * 2. previous sibling 
     * 3. parent
     * 4. root 
     * 
     * @param removed - pattern prepared for removal
     **/
    public void fixSelection() {
        
        //check if currently selected entity is NOT in model 
        if (!(selectedID != null && getSelected() == null)){
            return;
           
        } 
        
        
        CompositePattern parent = (selectedPattern != null) ? 
            selectedPattern.getParent() : null;
        
        Pattern next = null;
        Pattern prev = null;
        
        if (parent != null) {
            Iterator<Pattern> it = parent.getNestedPatterns().iterator();
            while(it.hasNext()){
                Pattern p = it.next();
                if (p == selectedPattern) {
                    if (it.hasNext()){
                        next = it.next();
                    }
                    break;
                }
                prev = p;
            }
        }
        if (next != null) {
            setSelectedPattern(next);
        } else if (prev != null ){
            setSelectedPattern(prev);
        } else if (parent != null){
            setSelectedPattern(parent);
        } else {
            setSelectedPattern(model.getRootPattern());
        }
    }

    public Pattern getSelectedPattern(){
        BpelEntity entity = getSelected();
        return (entity != null) ?
            model.getPattern(entity) : null;
    }
    
    public UniqueId getSelectedID() {
        return selectedID;
        
    }

    public BpelEntity getSelected() {
        return (selectedID != null) ? 
            model.getEntity(selectedID) : null;
    }
    
    public boolean isEmpty() {
        return selectedID == null;
    }
    
    public void clear() {
        setSelected((BpelEntity) null);
    }
}
