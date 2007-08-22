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

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.core.multiview.MultiViewModel.ActionRequestObserverFactory;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;


/** Special subclass of TopComponent which shows and handles set of
 * MultiViewElements, shows them in switchable toggle buttons style, along
 * with toolbsrs af actions asociated with individual view elements.
 *
 *
 * @author Dafe Simonek, Milos Kleint
 */


public final class MultiViewCloneableTopComponent extends CloneableTopComponent 
                                                  implements ActionRequestObserverFactory, CloneableEditorSupport.Pane {

    MultiViewPeer peer;
                                           
    public MultiViewCloneableTopComponent() {
        super();
        peer = new MultiViewPeer(this, this);
        // initializes the multiview component.
        peer.initComponents();
        setFocusCycleRoot(false);
        setName("");
        // assocuate lookup needs to come after the init.. initComponents() initializes actionMap        
        associateLookup(peer.getLookup());
        
//        setFocusable(false);
    }
    
    
    public void setMultiViewDescriptions(MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        peer.setMultiViewDescriptions(descriptions, defaultDesc);
    }
    
    public void setCloseOperationHandler(CloseOperationHandler handler) {
        peer.setCloseOperationHandler(handler);
    }
    
    private void setDeserializedMultiViewDescriptions(MultiViewDescription[] descriptions, 
                                                      MultiViewDescription defaultDesc, Map existingElements) {
        peer.setDeserializedMultiViewDescriptions(descriptions, defaultDesc, existingElements);
    }
    
    MultiViewModel getModel() {
        return peer.getModel();
    }
    
    
    
    protected void componentClosed() {
        super.componentClosed();
        peer.peerComponentClosed();
    }
    
    protected void componentShowing() {
        super.componentShowing();
        peer.peerComponentShowing();
    }
    
    protected void componentHidden() {
        super.componentHidden();
        peer.peerComponentHidden();
    }
    
    protected void componentDeactivated() {
        super.componentDeactivated();
        peer.peerComponentDeactivated();
    }
    
    protected void componentActivated() {
        super.componentActivated();
        peer.peerComponentActivated();
    }
    
    protected void componentOpened() {
        super.componentOpened();
        peer.peerComponentOpened();
    }

    /**
     * delegate to the apppropriate active element's component
     */
    public boolean requestFocusInWindow() {
        return peer.requestFocusInWindow();
    }

    /**
     * delegate to the apppropriate active element's component
     */
    public void requestFocus() {
        peer.requestFocus();
    }    
    
    /**
     * merge action for the topcomponent and the enclosed MultiViewElement..
     * 
     */
    public Action[] getActions() {
        //TEMP don't delegate to element's actions..
        Action[] superActions = super.getActions();
        Action[] acts = peer.peerGetActions(superActions);
        return acts;
//        return acts;
    }
    
    public MultiViewHandlerDelegate getMultiViewHandlerDelegate() {
        // TODO have one handler only or create a new one each time?
        return peer.getMultiViewHandlerDelegate();
    }
    
    /**
     * Delegates the value to the element descriptions.
     */
    public int getPersistenceType() {
        return peer.getPersistenceType();
    }  
    
    protected String preferredID() {
        return peer.preferredID();
    }
    
    
    
    /** Serialize this top component.
    * Subclasses wishing to store state must call the super method, then write to the stream.
    * @param out the stream to serialize to
    */
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        peer.peerWriteExternal(out);
    }

    /** Deserialize this top component.
    * Subclasses wishing to store state must call the super method, then read from the stream.
    * @param in the stream to deserialize from
    */
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        peer.peerReadExternal(in);
    }    
    
    
    Action[] getDefaultTCActions() {
        return super.getActions();
    }
    
    public MultiViewElementCallback createElementCallback(MultiViewDescription desc) {
        return SpiAccessor.DEFAULT.createCallback(new ActReqObserver(desc));
    }
    
    public CloneableTopComponent getComponent() {
        return this;
    }    
    
    public javax.swing.JEditorPane getEditorPane() {
        if (peer == null || peer.model == null) {
            return null;
        }
        MultiViewElement paneEl = findPaneElement();
        if (paneEl != null) {
            CloneableEditorSupport.Pane pane = (CloneableEditorSupport.Pane)paneEl.getVisualRepresentation();
            return pane.getEditorPane();
        }
        // hopeless case, don't try to create new elements. it's users responsibility to
        // switch to the editor element before getEditorPane() 
        return null;
    }
    
    private MultiViewElement findPaneElement() {
        MultiViewElement el = peer.model.getActiveElement(false);
        if (el != null && el.getVisualRepresentation() instanceof CloneableEditorSupport.Pane) {
            return el;
        }
        // now try a best guess.. iterate the already created elements and check if any of
        // them is a Pane
        Collection col = peer.model.getCreatedElements();
        Iterator it = col.iterator();
        while (it.hasNext()) {
            el = (MultiViewElement)it.next();
            if (el.getVisualRepresentation() instanceof CloneableEditorSupport.Pane) {
                // fingers crossed and hope for the best... could result in bad results once
                // we have multiple editors in the multiview component.
                return el;
            }
        }
        // hopeless case, don't try to create new elements. it's users responsibility to
        // switch to the editor element before getEditorPane() 
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return peer.getHelpCtx();
    }
   
    public String toString() {
        return "MVCTC[name=" + getDisplayName() + ", peer=" + peer + "]"; // NOI18N
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
        retValue = peer.peerGetUndoRedo();
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
        return peer.canClose();
    }
    
    // from CloneableEditor.Pane
    public void updateName() {
        // ensure to trigger update name from AWT -> #44012 - will ultimately trigger winsys.
        if (peer != null) {
            if (SwingUtilities.isEventDispatchThread() ) {
                peer.updateName();
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        peer.updateName();
                    }
                });
            }
        }
    }
    
    /**
     * callback for the Pane implementation to adjust itself to the openAt() request.
     */
    public void ensureVisible() {
        MultiViewElement paneEl = findPaneElement();
        if (paneEl != null) {
            open();
            MultiViewElementCallback call = peer.getModel().getCallbackForElement(paneEl);
            call.requestVisible();
        }
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
            boolean activated = peer.isActivated();
            if (!activated) {
                MultiViewCloneableTopComponent.this.requestActive();
            }
            if (peer.model.getActiveDescription() != description) {
                if (activated) {
                    peer.model.getActiveElement().componentDeactivated();
                } 
                peer.tabs.changeActiveManually(description);
                if (activated) {
                    peer.model.getActiveElement().componentActivated();
                }
            }

        }
        
        public void requestVisible() {
            peer.tabs.changeVisibleManually(description);
        }
        
        public Action[] createDefaultActions() {
            return MultiViewCloneableTopComponent.this.getDefaultTCActions();
        }
        
        public void updateTitle(String title) {
            MultiViewCloneableTopComponent.this.setDisplayName(title);
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
            return (description.equals(peer.model.getActiveDescription()));
        }
        
        public TopComponent getTopComponent() {
            return MultiViewCloneableTopComponent.this;
        }
        
    }
    
}
