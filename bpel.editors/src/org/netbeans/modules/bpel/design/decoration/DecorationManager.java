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


package org.netbeans.modules.bpel.design.decoration;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.decoration.components.DecorationComponent;
import org.netbeans.modules.bpel.design.decoration.components.ZoomableDecorationComponent;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.xam.Model;

public class DecorationManager {
    
    
    private final Object cacheKey = new Object();

    public static final Decoration emptyDecoration = new Decoration();
    
    
    private ArrayList<DecorationProvider> providersList = new ArrayList<DecorationProvider>();
    
    
    private DesignView designView;
    public DecorationManager(DesignView designView) {
        this.designView = designView;
    }
    
    /**
     * Calculates the superset of states from all providers
     **/
    public Decoration getDecoration(Pattern p) {
        BpelEntity entity = p.getOMReference();
        
        Decoration result = null;
        
        if (entity != null) {
            result = (Decoration) entity.getCookie(cacheKey);
        }
        
        return (result != null) ? result : emptyDecoration;
    }
    
    public List<DecorationProvider> getProviders() {
      return providersList;
    }
    
    public void attachProvider( DecorationProvider provider){
        providersList.add(provider);
        /*
         * trigger re-calculation of all decorations to add
         * of decorations reported by  new provider
         */
    }
    
    /**
     * Notify all providers, that view is about to close.
     * Providers can override this method to release resouces, unsubscribe from listeners, etc
     **/
    public void release(){
        for (DecorationProvider p : providersList){
            p.release();
        }
    }
    
    /**
     * listenenr method to be called by providers when they need to inform us about
     * changes in element decorations.
     * @entity - entity to update decorations or null, to update decorations for all entitites
     **/
    public void decorationChanged(BpelEntity entity) {
        if (designView.getModel() == null) return;
        if (designView.getBPELModel().getState() == Model.State.VALID){
            
            if (entity != null) {
                updateResult(entity);
            } else {
                // remove cookies for all elements  in tree
                // starting from root
                updateResultRecursive(designView.getBPELModel().getProcess());
            }
        }
        updateView();
    }
    public void repositionComponentsRecursive(){
        repositionComponentsRecursive(designView.getBPELModel().getProcess());
    }
    
    public void updateAllComponents(){
        
        Collection<Component> newList = new ArrayList<Component>();
        Collection<Component> oldList = new ArrayList<Component>();
        
        //iterate over all bpel entities and get all components attached
        buildComponentsList(designView.getBPELModel().getProcess(), newList);
        
        
        //sync-up the set of components with list of components already attached to view
        for (Component c: designView.getComponents()){
            if (c instanceof DecorationComponent){
                oldList.add(c);
            }
        }
        
        syncComponentList(oldList, newList);
        
        //try to re-position all components
        repositionComponentsRecursive();
        
    }
    
    
    
    
    private void updateResult(BpelEntity entity){
        Decoration newDecoration = new Decoration();
        
        for (DecorationProvider provider: providersList){
            Decoration d = provider.getDecoration(entity);
            if (d != null && d!= emptyDecoration){
                newDecoration.combineWith(d);
            }
        }
        
        Decoration oldDecoration = (Decoration) entity.getCookie(cacheKey);
        
        entity.setCookie(cacheKey, newDecoration);
        
        syncComponentList(
                (oldDecoration != null)?oldDecoration.getComponents():null,
                (newDecoration != null)?newDecoration.getComponents():null
                );
        
        
        
        repositionComponents(entity);
        
        
    }
    /**
     * adds to designView container all elements which are in newList and not in oldList
     * and removes elements which are in oldList and not in newList
     *
     *
     **/
    
    private void syncComponentList(Iterable<Component> oldList, Iterable<Component> newList){
        Set<Component> oldSet = new HashSet<Component>();
        
        if (oldList != null ){
            for (Component c: oldList){
                oldSet.add(c);
            }
        }
        
        if (newList == null){
            newList = new ArrayList<Component>();
        }
        
        for (Component c : newList) {
            if (!oldSet.remove(c)){
                designView.add(c);
            }
            
        }
        for (Component c: oldSet){
            designView.remove(c);
        }
    }
    
    
    private void updateView(){
        designView.revalidate();
        designView.repaint();
        designView.getRightStripe().repaint();
    }
    
    

    
    
    private void buildComponentsList(BpelEntity entity, Collection<Component> list){
        
        //Add all components, decorating given bpelentity to set,
        Decoration decoration = (Decoration) entity.getCookie(cacheKey);
        if (decoration != null && decoration.hasComponents()){
            for (Component c: decoration.getComponents()){
                list.add(c);
            }
            
        }
        
        //call recurision for all childs
        for (BpelEntity e: entity.getChildren()){
            buildComponentsList(e, list);
        }
    }
    
    private void updateResultRecursive(BpelEntity entity){
        updateResult(entity);
        for (BpelEntity e : entity.getChildren()){
            updateResultRecursive(e);
        }
    }
    
    
    private void repositionComponentsRecursive(BpelEntity entity) {
        repositionComponents(entity);
        for (BpelEntity e : entity.getChildren()){
            repositionComponentsRecursive(e);
        }
    }
    
    
    
    private void repositionComponents(BpelEntity entity){
        Decoration decoration = (Decoration) entity.getCookie(cacheKey);
        
        if ((decoration == null) || !decoration.hasComponents()) {
            return;
        }
        
        Pattern pattern = designView.getModel().getPattern(entity);
        
        if (pattern == null){
            return;
        }
        
        double zoom = designView.getCorrectedZoom();
        
        //group components by positioneer
        HashMap<Positioner, List<Component>> positionerComponents =
                new  HashMap<Positioner, List<Component>>();
        
        ComponentsDescriptor components = decoration.getComponents();
        int componentsCount = components.getComponentCount();
        
        for (int i = 0; i < componentsCount; i++){
            Component c = components.getComponent(i);
            Positioner p = components.getPositioner(i);
            
            List<Component> list = positionerComponents.get(p);
            
            if (list == null){
                list = new ArrayList<Component>(componentsCount);
                positionerComponents.put(p, list);
            }
            
            list.add(c);
            
            if (c instanceof ZoomableDecorationComponent){
                //apply zoom
                ((ZoomableDecorationComponent) c).setZoom(zoom);
            }
        }
        
        //call positioners for each group
        for (Positioner p : positionerComponents.keySet()) {
            p.position(pattern, positionerComponents.get(p), zoom);
        }
        
    }
    
}
