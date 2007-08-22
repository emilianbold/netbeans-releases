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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.multiview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;


/** 
 * Model handling maintainance of descriptions, creation of elements and selection 
 * of active items.
 * @author Milos Kleint
 */

class MultiViewModel {
    
    private MultiViewDescription currentEditor;
    private Map nestedElements; //key=description, value null or multiviewelement
    private Map nestedCallbacks; //key=element, value null or the MultiViewElementCallback that it's used by this element.
    private Map nestedPerspectives; //key=description, value perspective
//    private Map nestedPerspectiveComponents; //key=description, value mull or perspectiveComponent
    private MultiViewDescription[] descriptions;
    private ButtonGroup group;
    private Collection shownElements;
    private ArrayList listeners;
    private ActionRequestObserverFactory observerFactory;
    
    MultiViewModel(MultiViewDescription[] descs, MultiViewDescription defaultDescr, 
                   MultiViewModel.ActionRequestObserverFactory factory) {
        this(descs, defaultDescr, factory, Collections.EMPTY_MAP);
    }
    
    /**
     * constructor used at deserialization...
     */
    MultiViewModel(MultiViewDescription[] descs, MultiViewDescription defaultDescr, 
                   MultiViewModel.ActionRequestObserverFactory factory, Map existingElements) {
        observerFactory = factory;
        nestedElements = new HashMap();
//        nestedPerspectiveComponents = new HashMap();
        nestedPerspectives = new HashMap();
        nestedCallbacks = new HashMap();
        shownElements = new HashSet(descs.length + 3);
        descriptions = descs;
        this.group = group;
        for (int i = 0; i < descriptions.length; i++) {
            MultiViewElement element = (MultiViewElement)existingElements.get(descriptions[i]);
            nestedElements.put(descriptions[i], element);
            nestedPerspectives.put(descriptions[i], Accessor.DEFAULT.createPerspective(descriptions[i]));
            if (element != null) {
                // set the observer..
                MultiViewElementCallback call = factory.createElementCallback(descriptions[i]);
                nestedCallbacks.put(element, call);
                element.setMultiViewCallback(call);
//                nestedPerspectiveComponents.put(descriptions[i], Accessor.DEFAULT.createPersComponent(element));
            }
        }
        currentEditor = (defaultDescr == null || !nestedElements.containsKey(defaultDescr) ? descriptions[0] : defaultDescr);
        group = new BtnGroup();
    }
    
    
    void setActiveDescription(MultiViewDescription description) {
        if (currentEditor == description) return;
        MultiViewDescription old = currentEditor;
        currentEditor = description;
        fireSelectionChanged(old, description);
    }
    
    MultiViewDescription getActiveDescription() {
        return currentEditor;
    }
    
    MultiViewElement getActiveElement() {
        return getActiveElement(true);
    }
    
    MultiViewElement getActiveElement(boolean createIfNotCreatedYet) {
        return getElementForDescription(currentEditor, createIfNotCreatedYet);
    }
    
    /**
     * returns all elements that were so far created/instantiated.
     */
    synchronized Collection getCreatedElements() {
       Collection col = new ArrayList(nestedElements.size());
       Iterator it = nestedElements.entrySet().iterator();
       while (it.hasNext()) {
           Map.Entry entry = (Map.Entry)it.next();
           if (entry.getValue() != null) {
               col.add(entry.getValue());
           }
       }
       return col;
    }
    
    /**
     * keeps track of already shown elements, so that componentOpened() is not called multiple Times on a single element.
     *
     */ 
    boolean wasShownBefore(MultiViewElement element) {
        return shownElements.contains(element);
    }
    /**
     * mars the compoment as shown before, meaning componentOpened() was called on it.
     */
    void markAsShown(MultiViewElement element) {
        shownElements.add(element);
    }

    /**
     * mars the compoment as currently hidden, meaning componentClosed() was called on it.
     */
    
    void markAsHidden(MultiViewElement element) {
        shownElements.remove(element);
    }
    
    /**
     * returns a list of current MultiViewDescriptions.
     */
    MultiViewDescription[] getDescriptions() {
        return descriptions;
    }
    
    MultiViewPerspective[] getPerspectives() {
        MultiViewPerspective[] toReturn = new MultiViewPerspective[descriptions.length];
        for (int i = 0; i < descriptions.length; i++) {
            toReturn[i] = (MultiViewPerspective)nestedPerspectives.get(descriptions[i]);
        }
        return toReturn;
    }
    
    MultiViewPerspective getSelectedPerspective() {
        return (MultiViewPerspective)nestedPerspectives.get(getActiveDescription());
    }
    
//    MultiViewPerspectiveComponent getMVComponentForDescription(MultiViewDescription desc) {
//        return (MultiViewPerspectiveComponent)nestedPerspectiveComponents.get(desc);
//    }
    
    /**
     * The button group where the togglebuttons for the descriptions are put into.
     */
    ButtonGroup getButtonGroup() {
        return group;
    }
    
    MultiViewElement getElementForDescription(MultiViewDescription description) {
        return getElementForDescription(description, true);
    }

    /**
     * used primarily at deserialization time.
     */
     synchronized MultiViewElement getElementForDescription(MultiViewDescription description, boolean create) {
        MultiViewElement element = (MultiViewElement)nestedElements.get(description);
        if (element == null && create) {
            element = description.createElement();
            MultiViewElementCallback call = observerFactory.createElementCallback(description);
            nestedCallbacks.put(element, call);
            element.setMultiViewCallback(call);
            nestedElements.put(description, element);
 //           nestedPerspectiveComponents.put(description, Accessor.DEFAULT.createPersComponent(element));
        }
        return element;
    }
     
     synchronized MultiViewElementCallback getCallbackForElement(MultiViewElement elem) {
         return (MultiViewElementCallback)nestedCallbacks.get(elem);
     }
    
    
    void addElementSelectionListener(ElementSelectionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList();
        }
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    void removeElementSelectionListener(ElementSelectionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList();
        }
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void fireSelectionChanged(MultiViewDescription oldOne, MultiViewDescription newOne) {
        if (listeners != null) {
            synchronized (listeners) {
                Iterator it = listeners.iterator(); 
                while (it.hasNext()) {
                    ElementSelectionListener list = (ElementSelectionListener)it.next();
                    list.selectionChanged(oldOne, newOne);
                }
            }
        }
    }
    
    void fireActivateCurrent() {
        if (listeners != null) {
            synchronized (listeners) {
                Iterator it = listeners.iterator(); 
                while (it.hasNext()) {
                    ElementSelectionListener list = (ElementSelectionListener)it.next();
                    list.selectionActivatedByButton();
                }
            }
        }
    }

    public String toString() {
        return "current=" + currentEditor; // NOI18N
    }

    /**
     * listener for changes in model's selected element.
     */
    static interface ElementSelectionListener  {
        
        public void selectionChanged(MultiViewDescription oldOne, MultiViewDescription newOne);
        
        public void selectionActivatedByButton();
    }
    
    /**
     * Interface for creating the observers that are passed to newly created elements.
     */
    static interface ActionRequestObserverFactory {
        MultiViewElementCallback createElementCallback(MultiViewDescription desc);
    }
    
    /**
     * handles selection of active element among the tgglebuttons.. more straightforward then adding listeners.
     */
    
    private class BtnGroup extends ButtonGroup {
        
        public void setSelected(ButtonModel m, boolean b) {
            super.setSelected(m, b);
            if (getSelection() instanceof TabsComponent.TabsButtonModel) {
                TabsComponent.TabsButtonModel mod = (TabsComponent.TabsButtonModel)m;
                MultiViewDescription desc = mod.getButtonsDescription();
                setActiveDescription(desc);
            }
        }
        
    }
    
    
}