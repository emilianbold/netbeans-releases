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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.nodes.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.soa.validation.core.Controller;
import org.netbeans.modules.soa.validation.core.Listener;
import org.netbeans.modules.bpel.core.annotations.AnnotationListener;
import org.netbeans.modules.bpel.core.annotations.AnnotationManagerCookie;
import org.netbeans.modules.bpel.core.annotations.DiagramAnnotation;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

/**
 * @author Vitaly Bychkov
 * @version 1.1
 */
public class ValidationProxyListener implements Listener, AnnotationListener {
    private Map<Component, Validator.ResultType> cachedResultMap = new HashMap<Component, Validator.ResultType>();
    private ChangeValidationSupport myValidationSupport;
    private Lookup myLookup;
    private Controller myValidationController;
    private AnnotationManagerCookie cookie;
    
    private ValidationProxyListener(Lookup lookup, Controller validationController) {
        assert lookup != null && validationController != null;
        myLookup = lookup;
        myValidationController = validationController;
        myValidationSupport = new ChangeValidationSupport();
        attachValidationController(this);
        subscribeOnAnnotationChanges();
        myValidationController.triggerValidation();
    }
    
    public synchronized static ValidationProxyListener getInstance(Lookup lookup) {
        if (lookup == null) {
            return null;
        }
        Controller validationController = (Controller) lookup.lookup(Controller.class);

        if (validationController == null) {
            return null;
        }
        return new ValidationProxyListener(lookup, validationController);
    }
    
    protected void finalize() throws Throwable {
        detachValidationController(this);
        unSubscribeOnAnnotationChanges();
    }
    
    private Lookup getLookup() {
        return myLookup;
    }
    
    public void addChangeValidationListener(ChangeValidationListener listener) {
        myValidationSupport.addChangeValidationListener(listener);
    }
    
    public void removeChangeValidationListener(ChangeValidationListener listener) {
        myValidationSupport.removeChangeValidationListener(listener);
    }
    
    public synchronized void validationUpdated(List<Validator.ResultItem> validationResults ) {
        validationResults = EditorUtil.filterBpelResultItems(validationResults);
        
        Map<Component, Validator.ResultType> newResultMap
                = getComponentResultMap(validationResults);
        
        Map<Component, Validator.ResultType> removedResults
                = getRemovedComponentResultMap(cachedResultMap, newResultMap);
        
        Map<Component, Validator.ResultType> changedResults
                = getChangedComponentResultMap(cachedResultMap, newResultMap);
        
        cachedResultMap = newResultMap;

        if (removedResults != null && removedResults.size() > 0) {
            myValidationSupport
                    .fireRemoveValidation(new ArrayList(removedResults.keySet()));
        }
        if (changedResults != null && changedResults.size() > 0) {
            myValidationSupport.fireChangeValidation(changedResults);
        }
    }
    
    private Map<Component, Validator.ResultType> getRemovedComponentResultMap(
            Map<Component, Validator.ResultType> oldMap
            , Map<Component, Validator.ResultType> newMap) {
        if (oldMap == null || oldMap.size() < 1) {
            return null;
        }
        
        if (newMap == null || newMap.size() < 1) {
            return oldMap;
        }
        
        Map<Component, Validator.ResultType> removedResults =
                oldMap == null ? null : new HashMap(oldMap);
        for (Component elem : newMap.keySet()) {
            removedResults.remove(elem);
        }
        
        return removedResults;
    }
    
    private Map<Component, Validator.ResultType> getChangedComponentResultMap(
            Map<Component, Validator.ResultType> oldMap
            , Map<Component, Validator.ResultType> newMap) {
        if (newMap == null || newMap.size() < 1) {
            return null;
        }
        
        if (oldMap == null || oldMap.size() < 1) {
            return newMap;
        }
        
        Map<Component, Validator.ResultType> changedResults
                = new HashMap<Component, Validator.ResultType>();
        
        Validator.ResultType tmpOldResultType = null;
        Validator.ResultType tmpNewResultType = null;
        for (Component elem : newMap.keySet()) {
            tmpOldResultType  = oldMap.get(elem);
            tmpNewResultType = newMap.get(elem);
            if (tmpNewResultType != null
                    && !(tmpNewResultType.equals(tmpOldResultType))) {
                changedResults.put(elem,tmpNewResultType);
            }
        }
        
        return changedResults;
    }
    
    public Validator.ResultType getValidationStatusForElement(Object element) {
        if (element == null || cachedResultMap == null
                || cachedResultMap.size() < 1) {
            return null;
        }
        
        for (Component item : cachedResultMap.keySet()) {
            if (item.equals(element)) {
                return cachedResultMap.get(item);
            }
        }
        
//        if (element == null || previousResultItems == null
//                || previousResultItems.size() < 1) {
//            return null;
//        }
//        for (ResultItem item : previousResultItems) {
//            for (Component component: item.getComponents()) {
//                if (component.equals(element)) {
//                    return item.getType();
//                }
//            }
//        }
        return null;
    }
    
    private void attachValidationController(Listener listener) {
        myValidationController.addListener(listener);
    }
    
    private void detachValidationController(Listener listener) {
        myValidationController.removeListener(listener);
    }
    
    /**
     *  Attach listener to validation changes.
     */
    private void subscribeOnAnnotationChanges() {
        DataObject dobj = (DataObject) myLookup.lookup(DataObject.class);

        if (dobj != null){
            cookie = (AnnotationManagerCookie) dobj.getCookie(AnnotationManagerCookie.class);
        }
        cookie.addAnnotationListener(this);
    }
    
    private void unSubscribeOnAnnotationChanges() {
        cookie.removeAnnotationListener(this);
    }

    private Map<Component, Validator.ResultType>
            getComponentResultMap(List<Validator.ResultItem> resultItems) {
        Map<Component, Validator.ResultType> componentResultMap
                = new HashMap<Component, Validator.ResultType>();
        if (resultItems == null || resultItems.size() < 1) {
            return componentResultMap;
        }
        
        Validator.ResultType tmpOldResultType = null;
        Validator.ResultType tmpNewResultType = null;
        boolean isRequreUpdate = true;
        for (ResultItem resultItemElem : resultItems) {
            Component componentElem = resultItemElem.getComponents();
            // check if already exist
            tmpOldResultType = componentResultMap.get(componentElem);
            tmpNewResultType = resultItemElem.getType();
            if (tmpNewResultType != null) {
                tmpNewResultType = getPriorytestType(tmpNewResultType
                        , tmpOldResultType);
                isRequreUpdate = !(tmpNewResultType.equals(tmpOldResultType));
            }
            if (isRequreUpdate) {
                componentResultMap.put(componentElem, tmpNewResultType);
            }
        }
        
        return componentResultMap;
    }
    
    public static Validator.ResultType getPriorytestType(
            List<Validator.ResultType> resultTypes) 
    {
        assert resultTypes != null;
        if (resultTypes.size() == 0) {
            return null;
        }
        
        Validator.ResultType priorytestResult = null;
        priorytestResult = resultTypes.get(0);
        for (int i = 1; i < resultTypes.size(); i++) {
            priorytestResult = getPriorytestType(
                    priorytestResult, resultTypes.get(i));
        }

        return priorytestResult;
    }
    
    /**
     * Utility method
     * TODO r | m
     */
    public static Validator.ResultType getPriorytestType(Validator.ResultType type1
            , Validator.ResultType type2) {
        if (type1 == null && type2 == null) {
            return null;
        }
        
        if (type1 == Validator.ResultType.ERROR
                || type2 == Validator.ResultType.ERROR ) {
            return Validator.ResultType.ERROR;
        }
        
        if (type1 == Validator.ResultType.WARNING
                || type2 == Validator.ResultType.WARNING ) {
            return Validator.ResultType.WARNING;
        }
        
        return Validator.ResultType.ADVICE;
    }
    
    /**
     * Utility method
     * TODO r | m
     */
    public static List<ResultItem> filterNavigatorRelatedResultItems(List<ResultItem> validationResults) {
        List<ResultItem> bpelResultItems = new ArrayList<ResultItem>();
        
        // Go through all result items.
        for(ResultItem resultItem: validationResults) {
            
            // For each result item go through all components for
            // which it applies.
            Component component = resultItem.getComponents();
            // Only gather bpel components.
            if(component instanceof BpelEntity || component instanceof BPELExtensibilityComponent ) {
                
                ResultItem bpelResultItem = new ResultItem(resultItem.getValidator(),
                        resultItem.getType(), component, resultItem.getDescription());
                bpelResultItems.add(bpelResultItem);
                
            }
        }
        
        return bpelResultItems;
    }

    public void annotationAdded(DiagramAnnotation diagramAnnotation) {
//        System.out.println("annotation added !!!");
        myValidationSupport.fireAddAnnotation(diagramAnnotation.getBpelEntityId(), diagramAnnotation.getAnnotationType());
    }

    public void annotationRemoved(DiagramAnnotation diagramAnnotation) {
//        System.out.println("annotation removed!!!");
        myValidationSupport.fireRemoveAnnotation(diagramAnnotation.getBpelEntityId(), diagramAnnotation.getAnnotationType());
    }
    
    public boolean getAnnotationStatusForElement(Object entity) {
        if (!(entity instanceof BpelEntity)) {
            return false;
        }
        
        DiagramAnnotation[] annotations = cookie.getAnnotations(((BpelEntity)entity).getUID());
//        System.out.println("getAnnotationStatusForElement: entity: "+entity+"; annotations: "+annotations);
//        for (DiagramAnnotation elem : annotations) {
//            System.out.println("elem annotation: "+elem.getAnnotationType());
//        }
        if ( annotations == null || annotations.length < 1) {
            return false;
        }
        
        return true;
    }
    
    public String[] getAnnotationTypes(Object entity) {
        if (!(entity instanceof BpelEntity)) {
            return null;
        }
        DiagramAnnotation[] annotations = cookie.getAnnotations(((BpelEntity)entity).getUID());
        if (annotations == null || annotations.length < 1) {
            return null;
        }
        String[] annotaionTypes = new String[annotations.length];
        for (int i = 0; i < annotaionTypes.length; i++) {
            annotaionTypes[i] = annotations[i].getAnnotationType();
        }
        return annotaionTypes;
    }
}
