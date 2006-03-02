/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Chris Webster
 * @author Rico
 * @author Nam Nguyen
 */
public abstract class AbstractModel<T extends DocumentComponent<T>> implements DocumentModel<T>, UndoableEditListener {
    
    private PropertyChangeSupport pcs;
    protected ModelUndoableEditSupport ues;
    private State status;
    private boolean inSync;
    private EventListenerList componentListeners;
    private Semaphore transactionSemaphore;
    private Transaction transaction;
    private ModelSource source;
    
    public AbstractModel() {
        pcs = new PropertyChangeSupport(this);
        ues = new ModelUndoableEditSupport();
        componentListeners = new EventListenerList();
        transactionSemaphore = new Semaphore(1,true); // binary semaphore
        status = State.NOT_WELL_FORMED;
    }

    public abstract T createRootComponent(Element root);
    public abstract ModelAccess getAccess();
    
    public boolean areSameNodes(Node n1, Node n2) {
        return getAccess().areSameNodes(n1, n2);
    }
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
    
    /**
     * Add property change listener which will receive events for any element
     * in the underlying schema model.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
    public void firePropertyChangeEvent(PropertyChangeEvent event) {
        assert transaction != null;
        transaction.addPropertyChangeEvent(event);
    }
    
    public void removeUndoableEditListener(javax.swing.event.UndoableEditListener uel) {
        ues.removeUndoableEditListener(uel);
    }
    
    public void addUndoableEditListener(javax.swing.event.UndoableEditListener uel) {
        ues.addUndoableEditListener(uel);
    }
    
    protected class ModelUndoableEditSupport extends UndoableEditSupport {
        
        @Override
        protected CompoundEdit createCompoundEdit() {
            return new ModelUndoableEdit();
        }
    }
    
    public boolean inSync() {
        return inSync;
    }
    
    protected void setInSync(boolean v) {
        inSync = v;
    }
    
    public State getState() {
        return status;
    }
    
    protected void setState(State s) {
        status = s;
    }
    
    public void sync() throws java.io.IOException {
        if (transaction != null) {
            throw new IllegalStateException("Sync should not happen in middle of transaction."); //NOI18N
        }
        try {
            setInSync(true);
            startTransaction(true);  //start pseudo transaction for event firing
            setState(getAccess().sync());
        } catch (IOException e) {
            setState(State.NOT_WELL_FORMED);
            throw e;
        } finally {
            if (transaction != null) {
                endTransaction();
            }
            setInSync(false);
        }
    }
    
    public void removeComponentListener(ComponentListener cl) {
        componentListeners.remove(ComponentListener.class, cl);
    }

    public void addComponentListener(ComponentListener cl) {
        componentListeners.add(ComponentListener.class, cl);
    }

    public void fireComponentChangedEvent(ComponentEvent evt) {
        assert transaction != null;
        transaction.addComponentEvent(evt);
    }
    
    public boolean isIntransaction() {
        return transaction != null;
   }
    
    public void endTransaction() throws IOException {
        validateWrite(); // ensures that the releasing thread really owns trnx
        try {
            transaction.fireEvents();
            // no-need to flush or undo/redo support while in sync
            if (! inSync()) {
                getAccess().flush();
                ues.endUpdate();
            }
        } finally {
            synchronized (this) {
                transaction = null;
                setInSync(false);
            }
            transactionSemaphore.release();
        }
    }

    public void startTransaction() {
        startTransaction(false);
    }
    
    private void startTransaction(boolean inSync) {
	if (transaction != null && 
	    transaction.currentThreadIsTransactionThread()) {
	    throw new IllegalStateException(
		"current thread has already started a transaction");
	}
        
        if (! inSync && isReadOnly()) {
            throw new IllegalArgumentException("Model source is read-only.");
        }
        
        transactionSemaphore.acquireUninterruptibly();
        // other correctly behaving threads will be blocked acquiring the 
        // semaphore here. Also store the current Thread to ensure that 
        // no other writes are occurring
        synchronized (this) {
	    assert transaction == null;
            transaction = new Transaction();
            setInSync(inSync);
        }
        // no undo/redo event for sync or undo/redo
        if (! inSync) {
            ues.beginUpdate();
        }
    }
    
    /**
     * This method ensures that a transaction is currently in progress and
     * that the current thread is able to write. 
     */
    public synchronized void validateWrite() {
        if (transaction == null || 
            !transaction.currentThreadIsTransactionThread()) {
            throw new IllegalStateException("attempted model write without " +
                    "invoking startTransaction");
        }
    }
    
    private class Transaction {
        private final List<PropertyChangeEvent> propertyChangeEvents;
        private final List<ComponentEvent> componentListenerEvents;
        private final Thread transactionThread;
        private boolean eventAdded;
        
        public Transaction() {
            propertyChangeEvents = new ArrayList<PropertyChangeEvent>();
            componentListenerEvents = new ArrayList<ComponentEvent>();
            transactionThread = Thread.currentThread();
            eventAdded = false;
        }
        
        public void addPropertyChangeEvent(PropertyChangeEvent pce) {
            propertyChangeEvents.add(pce);
            eventAdded = true;
        }
        
        public void addComponentEvent(ComponentEvent cle) {
            componentListenerEvents.add(cle);
            eventAdded = true;
        }
        
        public boolean currentThreadIsTransactionThread() {
            return Thread.currentThread().equals(transactionThread);
        }
        
        public void fireEvents() {
            while (eventAdded) {
                eventAdded = false;
                fireCompleteEventSet();
            }
        }
        
        /**
         * This method is added to allow mutations to occur inside events. The
	 * list is cloned so that additional events can be added. 
         */
        private void fireCompleteEventSet() {
            final List<PropertyChangeEvent> clonedEvents = 
                    new ArrayList<PropertyChangeEvent>(propertyChangeEvents);
            for (PropertyChangeEvent pce:clonedEvents) {
                pcs.firePropertyChange(pce);
            }
            
            final List<ComponentEvent> cEvents = 
                new ArrayList<ComponentEvent>(componentListenerEvents);
            Map<Object, Set<ComponentEvent.EventType>> fired = new HashMap<Object, Set<ComponentEvent.EventType>>();
            
            for (ComponentEvent cle:cEvents) {
                // make sure we only fire one event per component per event type.
                Object source = cle.getSource();
                if (fired.keySet().contains(source)) {
                    Set<ComponentEvent.EventType> types = fired.get(source);
                    if (types.contains(cle.getEventType())) {
                        continue;
                    } else {
                        types.add(cle.getEventType());
                    }
                } else {
                    Set<ComponentEvent.EventType> types = new HashSet<ComponentEvent.EventType>();
                    types.add(cle.getEventType());
                    fired.put(cle.getSource(), types);
                }
                
                final ComponentListener[] listeners = 
                    componentListeners.getListeners(ComponentListener.class);
                for (ComponentListener cl : listeners) {
                    cle.getEventType().fireEvent(cle,cl);
                }
            }
        }
    }
    
    
    protected class ModelUndoableEdit extends CompoundEdit {
        static final long serialVersionUID = 1L;
        
        public boolean addEdit(UndoableEdit anEdit) {
            return isInProgress() &&
                    (lastEdit() == null ? super.addEdit(anEdit) : lastEdit().addEdit(anEdit));
        }
        
        @Override
        public void redo() throws CannotRedoException {
            try {
                prepare();
                super.redo(); 
                finish();
            } catch(IOException ioe) {
                CannotRedoException e = new CannotRedoException();
                e.initCause(ioe);
                throw e;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            try {
                prepare();
                super.undo(); 
                finish();
            } catch(IOException ioe) {
                CannotUndoException e = new CannotUndoException();
                e.initCause(ioe);
                throw e;
            }
        }
        
        private void prepare() {
            startTransaction(true);  //start pseudo transaction for event firing
            AbstractModel.this.getAccess().prepareForUndoRedo();
        }
        
        private void finish() throws IOException {
            try {
                AbstractModel.this.getAccess().finishUndoRedo();
            } finally {
                endTransaction();
            }
        }
    }
    
    public void undoableEditHappened(UndoableEditEvent e) {
	ues.postEdit(e.getEdit());
    }

    /**
     * Returns QName of elements used in model.  Domain model implementation needs 
     * to override this to be able to embed elements outside of the domain such as
     * child elements of documentation in schema model.
     * @return full set of element QName's or null if there is no needs for distinction 
     * between domain and non-domain elements.
     */
    public Set<QName> getQNames() { 
        return null; 
    }

    protected void setModelSource(ModelSource modelSource) {
        source = modelSource;
    }
    
    public ModelSource getModelSource() {
        return source;
    }
    
    public boolean isReadOnly() {
        return getModelSource() == null ? false : getModelSource().isReadOnly();
    }
}


