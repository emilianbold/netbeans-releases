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
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.multiview.MultiViewModel.ActionRequestObserverFactory;
import org.netbeans.core.multiview.MultiViewModel.ElementSelectionListener;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;


/** Special subclass of TopComponent which shows and handles set of
 * MultiViewElements, shows them in switchable toggle buttons style, along
 * with toolbsrs af actions asociated with individual view elements.
 *
 *
 * @author Dafe Simonek, Milos Kleint
 */


public final class MultiViewTopComponent 
                            extends CloneableTopComponent 
                            implements ActionRequestObserverFactory, 
                                       CloneableEditorSupport.Pane {

    private static final String MULTIVIEW_ID = "MultiView-"; //NOI18N
    
    private MultiViewModel model;
    private TabsComponent tabs;
    private SelectionListener selListener;
    private CloseOperationHandler closeHandler;
    private transient MVProxyLookup lookup;
    
    public MultiViewTopComponent() {
        super();
        initComponents();
        selListener = new SelectionListener();
    }
    
    
    public void setMultiViewDescriptions(MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        if (model != null) {
            model.removeElementSelectionListener(selListener);
        }
        model = new MultiViewModel(descriptions, defaultDesc, this);
        model.addElementSelectionListener(selListener);
        tabs.setModel(model);
    }
    
    public void setCloseOperationHandler(CloseOperationHandler handler) {
        closeHandler = handler;
    }
    
    private void setDeserializedMultiViewDescriptions(MultiViewDescription[] descriptions, 
                                                      MultiViewDescription defaultDesc, Map existingElements) {
        if (model != null) {
            model.removeElementSelectionListener(selListener);
        }
        model = new MultiViewModel(descriptions, defaultDesc, this, existingElements);
        model.addElementSelectionListener(selListener);
        tabs.setModel(model);
    }
    
    /**
     * @deprecated - for use in tests only!!!!!
     */
    MultiViewModel getModel() {
        return model;
    }
    
    
    private void initComponents() {
        setLayout(new BorderLayout());
        tabs = new TabsComponent();
        add(tabs);
        ActionMap map = getActionMap();
        Action act = new AccessTogglesAction();
        map.put("accesstoggles", act);
        InputMap input = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke stroke = KeyStroke.getKeyStroke("control F10"); //NOI18N
        input.put(stroke, "accesstoggles");
    }
    
    protected void componentClosed() {
        super.componentClosed();
        
        Iterator it = model.getCreatedElements().iterator();
        while (it.hasNext()) {
            MultiViewElement el = (MultiViewElement)it.next();
            model.markAsHidden(el);
            el.componentClosed();
        }

    }
    
    protected void componentShowing() {
        super.componentShowing();
        model.getActiveElement().componentShowing();
    }
    
    protected void componentHidden() {
        super.componentHidden();
        model.getActiveElement().componentHidden();
    }
    
    protected void componentDeactivated() {
        super.componentDeactivated();
        model.getActiveElement().componentDeactivated();
    }
    
    protected void componentActivated() {
        super.componentActivated();
        model.getActiveElement().componentActivated();
    }
    
    protected void componentOpened() {
        super.componentOpened();
        showCurrentElement(true);
    }
    
    /**
     * hides the old element when switching elements.
     */
    private void hideElement(MultiViewDescription desc) {
        if (desc != null) {
            model.getElementForDescription(desc).componentHidden();
        }
    }

    
    private void showCurrentElement() {
        showCurrentElement(false);
    }
    
    /**
     * shows the new element after switching elements.
     */
    
    private void showCurrentElement(boolean calledFromComponentOpened) {
        MultiViewElement el = model.getActiveElement();
        MultiViewDescription desc = model.getActiveDescription();
        
        ((MVProxyLookup)getLookup()).setElementLookup(el.getLookup());
        // TODO display name is not a good unique id..
        // also consider a usecase where multiple elements point to a single visual component.
        //. eg. property sheet uses same component and only changes model.
        // in this case we probably should not remove and add the component from awt hierarchy
        tabs.switchToCard(el, desc.getDisplayName());
        setIcon(desc.getIcon());
        // the first time the component is shown, we need to call componentOpened() on it to be in synch with current
        // TopComponent behaviour?
        if (isOpened() || calledFromComponentOpened) {
            if (!model.wasShownBefore(el)) {
                el.componentOpened();
                model.markAsShown(el);
            }
        }
        if (isOpened()) {
            el.componentShowing();
        }
        tabs.setInnerToolBar(el.getToolbarRepresentation());
    }
    
    
    /**
     * merge action for the topcomponent and the enclosed MultiViewElement..
     * 
     */
    public Action[] getActions() {
        //TEMP don't delegate to element's actions..
        Action[] superActions = super.getActions();
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
//        return acts;
    }
    
    public MultiViewHandlerDelegate getMultiViewHandlerDelegate() {
        // TODO have one handler only or create a new one each time?
        return new MVTCHandler();
    }
    
    /**
     * Delegates the value to the element descriptions.
     */
    public int getPersistenceType() {
        
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
    
    protected String preferredID() {
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
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
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
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
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
        return super.getActions();
    }
    
    public MultiViewElementCallback createElementCallback(MultiViewDescription desc) {
        return SpiAccessor.DEFAULT.createCallback(new ActReqObserver(desc));
    }
    
    public CloneableTopComponent getComponent() {
        return this;
    }    
    
    public javax.swing.JEditorPane getEditorPane() {
        MultiViewElement el = model.getActiveElement();
        if (el.getVisualRepresentation() instanceof CloneableEditorSupport.Pane) {
            CloneableEditorSupport.Pane pane = (CloneableEditorSupport.Pane)el.getVisualRepresentation();
            return pane.getEditorPane();
        }
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return model.getActiveDescription().getHelpCtx();
    }
    
    /**
     * Get the undo/redo support for this component.
     * The default implementation returns a dummy support that cannot
     * undo anything.
     *
     * @return undoable edit for this component
     */
    public UndoRedo getUndoRedo() {
        UndoRedo retValue;
        retValue = model.getActiveElement().getUndoRedo();
        if (retValue == null) {
            retValue = super.getUndoRedo();
        }
        return retValue;
    }    
    
    /**
     * This method is called when this <code>TopComponent</code> is about to close.
     * Delegates to CloseOperationHandler.
     */
    public boolean canClose() {
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
            if (el.getVisualRepresentation() instanceof CloneableEditorSupport.Pane) {
                CloneableEditorSupport.Pane pane = (CloneableEditorSupport.Pane)el.getVisualRepresentation();
                pane.updateName();
                setDisplayName(pane.getComponent().getDisplayName());
            }
        }
        //TODO
    }
    
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new MVProxyLookup(super.getLookup());
        }
        return lookup;
    }
    
    /**
     * implementation of the MultiViewElement.ActionRequestObserver, manages activatation of the elements
     * and the TC itself based on requests from the elements.
     */
    class ActReqObserver implements Serializable, MultiViewElementCallbackDelegate {
        
        private static final long serialVersionUID =-3126744916624172415L;        
        private MultiViewDescription description;
        
        ActReqObserver(MultiViewDescription desc) {
            description = desc;
        }
        
        public void requestActive() {
            //TODO what to do if the TC itself is not focused? request it?
            // how will the internal callbacks work then?
            if (model.getActiveDescription() != description) {
                model.getActiveElement().componentDeactivated();
                tabs.changeActiveManually(description);
                model.getActiveElement().componentActivated();
            }
            
        }
        
        public void requestVisible() {
            tabs.changeVisibleManually(description);
        }
        
        public Action[] createDefaultActions() {
            return MultiViewTopComponent.this.getDefaultTCActions();
        }
        
        public void updateTitle(String title) {
            MultiViewTopComponent.this.setDisplayName(title);
        }
        
        /** replace as null - should not be stored and read..*/
        public Object writeReplace() throws ObjectStreamException {
            return null;
        }

        /** Resolve as null -should not be stored and read..*/
        public Object readResolve() throws ObjectStreamException {
            return null;
        }
        
        public boolean isSelectedElement() {
            return (description.equals(model.getActiveDescription()));
        }
        
        public TopComponent getTopComponent() {
            return MultiViewTopComponent.this;
        }
        
    }
    
    /**
     * notification from the model that the selection changed.
     */
    private class SelectionListener implements ElementSelectionListener {
        
        public void selectionChanged(MultiViewDescription oldOne, MultiViewDescription newOne) {
            hideElement(oldOne);
            showCurrentElement();
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
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            System.out.println("tabs requests focus..");
            tabs.requestFocus();
        }
        
        
    }
    
    private static class MVProxyLookup extends ProxyLookup {
        private Lookup initialLookup;
        public MVProxyLookup(Lookup initial) {
            super(new Lookup[] {initial});
            initialLookup = initial;
        }
        
        public void setElementLookup(Lookup look) {
            setLookups(new Lookup[] {initialLookup, look});
        }
    }
    
}