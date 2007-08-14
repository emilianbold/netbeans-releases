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

package org.netbeans.modules.bpel.design.decoration.providers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.core.validation.BPELValidationController;
import org.netbeans.modules.bpel.core.validation.BPELValidationListener;
import org.netbeans.modules.bpel.core.validation.ValidationUtil;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.DecorationProvider;
import org.netbeans.modules.bpel.design.decoration.Descriptor;
import org.netbeans.modules.bpel.design.decoration.StripeDescriptor;
import org.netbeans.modules.bpel.design.decoration.TextstyleDescriptor;
import org.netbeans.modules.bpel.design.decoration.components.ShowGlassPaneButton;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
/**
 *
 * @author aa160298
 */
public class ValidationDecorationProvider extends DecorationProvider
        implements BPELValidationListener {
    
        
    private Object list_key = new Object();
    private Object decoration_key = new Object();
    
    private List<ResultItem> results = new ArrayList<ResultItem>();
    
    /** Creates a new instance of ValidationDecorationProvider */
    public ValidationDecorationProvider(DesignView designView) {
        super(designView);
        
        
  
        
        
        final BPELValidationController vc = getDesignView().getValidationController();
        
        vc.addValidationListener(this);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                vc.triggerValidation();
            }
        });
        
        
    }
    
    
    public void release(){
        // Removed validation listener.
        getDesignView().getValidationController().removeValidationListener(this);
        
    }
    public Decoration getDecoration(BpelEntity entity){
        return (Decoration) entity.getCookie(decoration_key);
        
    }
    
    public void updateDecorations(){
        
        final List<ResultItem> resultsFiltered = ValidationUtil.filterBpelResultItems(results);
       
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (getDesignView().getBPELModel().getState() != BpelModel.State.VALID){
                    return; //ignore notifications while model is in broken state
                }
                
                //group resultitem by entities and put list to temporary cookies
                final Object new_listkey = new Object();
                for(ResultItem item: resultsFiltered) {
                    Component component =  item.getComponents();                    
                        BpelEntity entity = getDiagramEntity((BpelEntity) component);
                        if (entity != null){
                            ArrayList<ResultItem> items = (ArrayList) entity.getCookie(new_listkey);
                            if (items == null) {
                                items = new ArrayList<ResultItem>();
                                entity.setCookie(new_listkey, items);
                            }
                            items.add(item);
                        }                    
                }
                
                
                //iterate over all diagram elements and update decoration if result was changed
                
                new ModelIterator(){
                    public void visit(BpelEntity entity){
                        ArrayList<ResultItem> new_results = (ArrayList) entity.getCookie(new_listkey);
                        ArrayList<ResultItem> old_results = (ArrayList) entity.getCookie(list_key);
                        if (new_results == null) {
                            entity.removeCookie(decoration_key);
                            entity.removeCookie(list_key);
                        } else if (!compareLists(old_results, new_results)){
                            ShowGlassPaneButton showGlassPaneButton 
                                    = getShowGlassPaneButton(entity);
                            if (showGlassPaneButton == null) {
                                showGlassPaneButton 
                                        = new ShowGlassPaneButton(new_results);
                            } else {
                                showGlassPaneButton.setResultItems(new_results);
                            }

                            ComponentsDescriptor cd = new ComponentsDescriptor();
                            cd.add(showGlassPaneButton, ComponentsDescriptor.RIGHT_TB);

                            StripeDescriptor sd = StripeDescriptor
                                    .createValidation(new_results);
                            
                            Decoration decoration = new Decoration((sd != null) 
                                    ? new Descriptor[] { cd, TEXT_STYLE, sd }
                                    : new Descriptor[] { cd, TEXT_STYLE });
                            
                            entity.setCookie(decoration_key, decoration);
                            entity.setCookie(list_key, new_results);
                        }
                        
                    }
                }.run(getDesignView().getProcessModel());
                
                
                //notify DM to update all elements
                fireDecorationChanged(null);
            }
        });
        
    }
    

    private ShowGlassPaneButton getShowGlassPaneButton(BpelEntity entity) {
        Decoration decoration = (Decoration) entity.getCookie(decoration_key);
        if (decoration == null) return null;
        ComponentsDescriptor components = decoration.getComponents();
        if (components == null) return null;

        for (int i = components.getComponentCount() - 1; i >= 0; i--) {
            java.awt.Component c = components.getComponent(i);
            if (c instanceof ShowGlassPaneButton) {
                return (ShowGlassPaneButton) c;
            }
        }

        return null;
    }
    

    public void validationUpdated(List<ResultItem> results) {
        this.results = results;
        updateDecorations();
    }
    
    

    
    
    private abstract class ModelIterator{
        public abstract void visit(BpelEntity entity);
        
        public void run(BpelEntity entity){
            visit(entity);
            for(BpelEntity e: entity.getChildren()){
                run(e);
            }
        }
    }
    
    private boolean compareLists(ArrayList<ResultItem> list1, ArrayList<ResultItem> list2 ){
        
        if (list1 == null || list2 == null){
            return false;
        }
        
        if (list1.size() != list2.size()){
            return false;
        }
        
        for (ResultItem item1: list1){
            boolean found = false;
            for (ResultItem item2: list2){
                if (ValidationUtil.equals(item1, item2)){
                    found = true;
                    break;
                }
            }
            if (!found){
                return false;
            }
        }
        
        return true;
    }
    
    private BpelEntity getDiagramEntity(BpelEntity entity){
        if (entity == null || entity.getModel() == null){
            return null;
        }
        
        while(entity != null ){
            Pattern p = getDesignView().getModel().getPattern(entity);
            
            //do not show badges on "invisible elements"
            //put badges on their parents instead
            if ( p != null && p.isSelectable() && p.isInModel()){
                return entity;
            }
            
            entity = entity.getParent();
            
        }
        return null;
        
    }
    Descriptor TEXT_STYLE = new TextstyleDescriptor(new Color(0xff0000));
}
