/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.multiview;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.*;
import javax.swing.*;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.multiview.MultiViewModel.ActionRequestObserverFactory;
import org.netbeans.core.multiview.MultiViewModel.ElementSelectionListener;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;



/** Special subclass of TopComponent which shows and handles set of
 * MultiViewElements, shows them in switchable toggle buttons style, along
 * with toolbsrs af actions asociated with individual view elements.
 *
 *
 * @author Dafe Simonek, Milos Kleint
 */


public final class MultiViewPeer  {

    static final String MULTIVIEW_ID = "MultiView-"; //NOI18N
    
    MultiViewModel model;
    TabsComponent tabs;
    SelectionListener selListener;
    CloseOperationHandler closeHandler;
    transient MultiViewTopComponentLookup lookup;
    TopComponent peer;
    private ActionRequestObserverFactory factory;
    private MultiViewActionMap delegatingMap;
    private boolean activated = false;
    
    public MultiViewPeer(TopComponent pr, ActionRequestObserverFactory fact) {
        selListener = new SelectionListener();
        peer = pr;
        factory = fact;
    }
    
 
    
    
    public void setMultiViewDescriptions(MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        if (model != null) {
            model.removeElementSelectionListener(selListener);
        }
        model = new MultiViewModel(descriptions, defaultDesc, factory);
        model.addElementSelectionListener(selListener);
        tabs.setModel(model);
    }
    
    public void setCloseOperationHandler(CloseOperationHandler handler) {
        closeHandler = handler;
    }
    
    void setDeserializedMultiViewDescriptions(MultiViewDescription[] descriptions, 
                                                      MultiViewDescription defaultDesc, Map existingElements) {
        if (model != null) {
            model.removeElementSelectionListener(selListener);
        }
        model = new MultiViewModel(descriptions, defaultDesc, factory, existingElements);
        model.addElementSelectionListener(selListener);
        tabs.setModel(model);
    }
    
    /**
     * for use in tests only!!!!!
     */
    MultiViewModel getModel() {
        return model;
    }
    
    
    void initComponents() {
        initActionMap();
        peer.setLayout(new BorderLayout());
        tabs = new TabsComponent();
        peer.add(tabs);
        ActionMap map = peer.getActionMap();
        Action act = new AccessTogglesAction();
        map.put("NextViewAction", new GetRightEditorAction()); //NOI18N
        map.put("PreviousViewAction", new GetLeftEditorAction()); //NOI18N
        map.put("accesstoggles", act); //NOI18N
        InputMap input = peer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke stroke = KeyStroke.getKeyStroke("control F10"); //NOI18N
        input.put(stroke, "accesstoggles"); //NOI18N
//        stroke = (KeyStroke)new GetLeftEditorAction().getValue(Action.ACCELERATOR_KEY);
//        input.put(stroke, "getLeftEditor");
        input = peer.getInputMap(JComponent.WHEN_FOCUSED);
        input.put(stroke, "accesstoggles"); //NOI18N
        
        peer.putClientProperty("MultiViewBorderHack.topOffset", new Integer(tabs.getPreferredSize().height - 1));
    }
    
  // It is necessary so the old actions (clone and close from org.openide.actions package) remain working.
    // cannot use the
    private void initActionMap() {
        delegatingMap = new MultiViewActionMap(peer, new ActionMap ());
        if(peer instanceof TopComponent.Cloneable) {
            delegatingMap.put("cloneWindow", new javax.swing.AbstractAction() { // NOI18N
                public void actionPerformed(ActionEvent evt) {
                    TopComponent cloned = ((TopComponent.Cloneable)
                        peer).cloneComponent();
                    cloned.open();
                    cloned.requestActive();
                }
            });
        }
        delegatingMap.put("closeWindow", new javax.swing.AbstractAction() { // NOI18N
           public void actionPerformed(ActionEvent evt) {
               peer.close();
           }
        });
        peer.setActionMap(delegatingMap);
    }        
    
    void peerComponentClosed() {
        Iterator it = model.getCreatedElements().iterator();
        while (it.hasNext()) {
            MultiViewElement el = (MultiViewElement)it.next();
            model.markAsHidden(el);
            el.componentClosed();
        }

    }
    
    void peerComponentShowing() {
        MultiViewElement el = model.getActiveElement();
        el.componentShowing();
        delegatingMap.setDelegateMap(el.getVisualRepresentation().getActionMap());
        ((MultiViewTopComponentLookup)peer.getLookup()).setElementLookup(el.getLookup());
        JComponent jc = el.getToolbarRepresentation();
        jc.setOpaque(false);
        tabs.setInnerToolBar(jc);
        
    }
    
    void peerComponentHidden() {
        model.getActiveElement().componentHidden();
    }
    
    void peerComponentDeactivated() {
        activated = false;
        model.getActiveElement().componentDeactivated();
    }
    
    boolean isActivated() {
        return activated;
    }
    
    void peerComponentActivated() {
        activated = true;
        model.getActiveElement().componentActivated();
    }
    
    void peerComponentOpened() {
        showCurrentElement(true);
    }
    
    /**
     * hides the old element when switching elements.
     */
    void hideElement(MultiViewDescription desc) {
        if (desc != null) {
            MultiViewElement el = model.getElementForDescription(desc);
            el.componentHidden();
        }
    }

    
    void showCurrentElement() {
        showCurrentElement(false);
    }
    
    /**
     * shows the new element after switching elements.
     */
    
    private void showCurrentElement(boolean calledFromComponentOpened) {
        MultiViewElement el = model.getActiveElement();
        MultiViewDescription desc = model.getActiveDescription();

        // TODO display name is not a good unique id..
        // also consider a usecase where multiple elements point to a single visual component.
        //. eg. property sheet uses same component and only changes model.
        // in this case we probably should not remove and add the component from awt hierarchy
        tabs.switchToCard(el, desc.getDisplayName());
        peer.setIcon(desc.getIcon());
        // the first time the component is shown, we need to call componentOpened() on it to be in synch with current
        // TopComponent behaviour?
        if (peer.isOpened() || calledFromComponentOpened) {
            if (!model.wasShownBefore(el)) {
                el.componentOpened();
                model.markAsShown(el);
            }
        }
        
        if (!calledFromComponentOpened) {
            if (peer.isOpened()) {
                el.componentShowing();
                tabs.setInnerToolBar(el.getToolbarRepresentation());                
            }
        // should we really set the stuff only when not called from componentOpened()? maybe it's better to call it twice sometimes.
            // if we don't call it here for opened but not showing component, then the map, lookup and nodes will not be initialized properly.
            // is it a problem?
            delegatingMap.setDelegateMap(el.getVisualRepresentation().getActionMap());
            ((MultiViewTopComponentLookup)peer.getLookup()).setElementLookup(el.getLookup());
        }
    }
    
    
    
    /**
     * merge action for the topcomponent and the enclosed MultiViewElement..
     * 
     */
    Action[] peerGetActions(Action[] superActions) {
        //TEMP don't delegate to element's actions..
        Action[] acts = model.getActiveElement().getActions();
        for (int i = 0; i < acts.length; i++) {
            Action act = acts[i];
            for (int j = 0 ; j < superActions.length; j++) {
                Action superact = superActions[j];
                if (superact != null && act != null && superact.getClass().equals(act.getClass())) {
                    // these are the default topcomponent actions.. we need to replace them
                    // in order to have the correct context.
                    acts[i] = superActions[j];
                    break;
                }
            }
        }
        return acts;
    }
    
    MultiViewHandlerDelegate getMultiViewHandlerDelegate() {
        // TODO have one handler only or create a new one each time?
        return new MVTCHandler();
    }
    
    /**
     * Delegates the value to the element descriptions.
     */
    int getPersistenceType() {
        
        // should also take the opened/created elements into account.
        // no need to serialize the tc when the element that want to be serialized, was not 
        // even opened?!? but maybe handle this during the serialization proceess, avoid creating
        // the element when serializing.
        MultiViewDescription[] descs = model.getDescriptions();
        int type = TopComponent.PERSISTENCE_NEVER;
        for (int i = 0; i < descs.length; i++) {
            if (!(descs[i] instanceof Serializable)) {
                ErrorManager.getDefault().getInstance(MultiViewTopComponent.class.getName()).log(ErrorManager.WARNING, 
                        "The MultiviewDescription instance " + descs[i].getClass() + " is not serializable. Cannot persist TopComponent.");
                type = TopComponent.PERSISTENCE_NEVER;
                break;
            }
            if (descs[i].getPersistenceType() == TopComponent.PERSISTENCE_ALWAYS) {
                type = descs[i].getPersistenceType();
                // cannot ge any better than that.
            }
            if (descs[i].getPersistenceType() == TopComponent.PERSISTENCE_ONLY_OPENED &&
                 type != TopComponent.PERSISTENCE_ALWAYS) {
                type = descs[i].getPersistenceType();
                // go on searching..
            }
        
        }
        return type;
    }  
    
    String preferredID() {
        StringBuffer retValue = new StringBuffer(MULTIVIEW_ID);
        MultiViewDescription[] descs = model.getDescriptions();
        for (int i = 0; i < descs.length; i++) {
            retValue.append(descs[i].preferredID());
            retValue.append("|"); //NOI18N
        }
        return retValue.toString();
    }
    
    
    
    /** Serialize this top component.
    * Subclasses wishing to store state must call the super method, then write to the stream.
    * @param out the stream to serialize to
    */
    void peerWriteExternal (ObjectOutput out) throws IOException {
        if (closeHandler != null) {
            if (closeHandler instanceof Serializable) {
                out.writeObject(closeHandler);
            } else {
                //TODO some warning to the SPI programmer
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, 
                       "The CloseOperationHandler isn not serializable. MultiView component id=" + preferredID());
            }
        }
        MultiViewDescription[] descs = model.getDescriptions();
        MultiViewDescription curr = model.getActiveDescription();
        int currIndex = 0;
        for (int i = 0; i < descs.length; i++) {
            out.writeObject(descs[i]);
            if (descs[i].getPersistenceType() != TopComponent.PERSISTENCE_NEVER) {
                // only those requeTopsted and previously created elements are serialized.
                MultiViewElement elem = model.getElementForDescription(descs[i], false);
                if (elem != null && elem instanceof Serializable) {
                    out.writeObject(elem);
                }
            }
            if (descs[i] == curr) {
                currIndex = i;
            }
        }
        out.writeObject(new Integer(currIndex));
        
    }

    /** Deserialize this top component.
    * Subclasses wishing to store state must call the super method, then read from the stream.
    * @param in the stream to deserialize from
    */
    void peerReadExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        ArrayList descList = new ArrayList();
        HashMap map = new HashMap();
        int current = 0;
        CloseOperationHandler close = null;
        while (true) {
            Object obj = in.readObject();
            if (obj instanceof MultiViewDescription) {
                descList.add(obj);
            }
            else if (obj instanceof MultiViewElement) {
                map.put(descList.get(descList.size() - 1), obj);
            }
            else if (obj instanceof Integer)  {
                Integer integ = (Integer)obj;
                current = integ.intValue();
                break;
            } 
            if (obj instanceof CloseOperationHandler) {
                close = (CloseOperationHandler)obj;
            }
        }
        if (close == null) {
                //TODO some warning to the SPI programmer
            close = SpiAccessor.DEFAULT.createDefaultCloseHandler();
        }
        setCloseOperationHandler(close);
        // now that we've read everything, we should set it correctly.
        MultiViewDescription[] descs = new MultiViewDescription[descList.size()];
        descs = (MultiViewDescription[])descList.toArray(descs);
        MultiViewDescription currDesc = descs[current];
        setDeserializedMultiViewDescriptions(descs, currDesc, map);
    }    
    
    
    private Action[] getDefaultTCActions() {
        //TODO for each suppoerted peer have one entry..
        if (peer instanceof MultiViewTopComponent) {
            return ((MultiViewTopComponent)peer).getDefaultTCActions();
        }
        return new Action[0];
    }
    

    
    JEditorPane getEditorPane() {
        MultiViewElement el = model.getActiveElement();
        if (el.getVisualRepresentation() instanceof Pane) {
            Pane pane = (Pane)el.getVisualRepresentation();
            return pane.getEditorPane();
        }
        return null;
    }
    
    HelpCtx getHelpCtx() {
        return model.getActiveDescription().getHelpCtx();
    }
    
    /**
     * Get the undo/redo support for this component.
     * The default implementation returns a dummy support that cannot
     * undo anything.
     *
     * @return undoable edit for this component
     */
    UndoRedo peerGetUndoRedo() {
        UndoRedo retValue;
        retValue = model.getActiveElement().getUndoRedo();
        return retValue;
    }    
    
    /**
     * This method is called when this <code>TopComponent</code> is about to close.
     * Delegates to CloseOperationHandler.
     */
    boolean canClose() {
        Collection col = model.getCreatedElements();
        Iterator it = col.iterator();
        Collection badOnes = new ArrayList();
        while (it.hasNext()) {
           MultiViewElement el = (MultiViewElement)it.next();
           CloseOperationState state = el.canCloseElement();
           if (!state.canClose()) {
               badOnes.add(state);
           }
        }
        if (badOnes.size() > 0) {
            CloseOperationState[] states = new CloseOperationState[badOnes.size()];
            states = (CloseOperationState[])badOnes.toArray(states);
            return closeHandler.resolveCloseOperation(states);
        }
        return true;
    }
    
    // from CloneableEditor.Pane
    public void updateName() {
        // is called before setMultiViewDescriptions() need to check for null.
        if (model != null) {
            MultiViewElement el = model.getActiveElement();
            if (el.getVisualRepresentation() instanceof Pane) {
                Pane pane = (Pane)el.getVisualRepresentation();
                pane.updateName();
                peer.setDisplayName(pane.getComponent().getDisplayName());
            }
        }
        //TODO
    }
    
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new MultiViewTopComponentLookup(delegatingMap);
        }
        return lookup;
    }
    
    public String toString() {
        return "[model=" + model + "]"; // NOI18N
    }
    /**
     * notification from the model that the selection changed.
     */
    private class SelectionListener implements ElementSelectionListener {
        
        public void selectionChanged(MultiViewDescription oldOne, MultiViewDescription newOne) {
            hideElement(oldOne);
            showCurrentElement();
        }
        
        public void selectionActivatedByButton() {
            MultiViewElement elem = model.getActiveElement();
            elem.getVisualRepresentation().requestFocus();
            elem.componentActivated();
        }
        
    }
    
    private class MVTCHandler implements MultiViewHandlerDelegate {
        private MultiViewPerspective[] perspectives = null;
        
        public MultiViewPerspective[] getDescriptions() {
            return model.getPerspectives();
        }
        
        public MultiViewPerspective getSelectedDescription() {
            return model.getSelectedPerspective();
        }
        
        public void requestActive(MultiViewPerspective pers) {
            MultiViewDescription desc = Accessor.DEFAULT.extractDescription(pers);
            if (model.getActiveDescription() != desc) {
                model.getActiveElement().componentDeactivated();
                tabs.changeActiveManually(desc);
                model.getActiveElement().componentActivated();
            }
        }
        
        public void requestVisible(MultiViewPerspective pers) {
            MultiViewDescription desc = Accessor.DEFAULT.extractDescription(pers);
            tabs.changeVisibleManually(desc);
        }
        
//        public MultiViewPerspectiveComponent getElementForDescription(MultiViewPerspective pers) {
//            MultiViewDescription desc = Accessor.DEFAULT.extractDescription(pers);
//            return model.getMVComponentForDescription(desc);
//        }
        
        
    }
    

    private class AccessTogglesAction extends AbstractAction {
        
        AccessTogglesAction() {
//            putValue(Action.NAME, "AccessToggleMenu");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F10")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            tabs.requestFocusForSelectedButton();
            
        }
    }
    
}