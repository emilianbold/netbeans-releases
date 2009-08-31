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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import org.netbeans.modules.xml.xam.Model.State;
import org.openide.util.RequestProcessor;

/**
 * @author Chris Webster
 * @author Rico
 * @author Nam Nguyen
 */
public abstract class AbstractModel<T extends Component<T>> implements Model<T>, UndoableEditListener {
    
    private PropertyChangeSupport pcs;
    protected ModelUndoableEditSupport ues;
    private State status;
    private boolean inSync;
    private boolean inUndoRedo;
    private EventListenerList componentListeners;
    private Semaphore transactionSemaphore;
    private Transaction transaction;
    private ModelSource source;
    private UndoableEditListener[] savedUndoableEditListeners;
    
    public AbstractModel(ModelSource source) {
        this.source = source;
        pcs = new PropertyChangeSupport(this);
        ues = new ModelUndoableEditSupport();
        componentListeners = new EventListenerList();
        transactionSemaphore = new Semaphore(1,true); // binary semaphore
        status = State.VALID;
    }

    public abstract ModelAccess getAccess();

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

    public synchronized void addUndoableRefactorListener(javax.swing.event.UndoableEditListener uel) {
        savedUndoableEditListeners = ues.getUndoableEditListeners();
        if (savedUndoableEditListeners != null) {
            for (UndoableEditListener saved : savedUndoableEditListeners) {
                if (saved instanceof UndoManager) {
                    ((UndoManager)saved).discardAllEdits();
                }
            }
        }
        ues = new ModelUndoableEditSupport();
        ues.addUndoableEditListener(uel);
    }
    
    public synchronized void removeUndoableRefactorListener(javax.swing.event.UndoableEditListener uel) {
        ues.removeUndoableEditListener(uel);
        if (savedUndoableEditListeners != null) {
            ues = new ModelUndoableEditSupport();
            for (UndoableEditListener saved : savedUndoableEditListeners) {
                ues.addUndoableEditListener(saved);
            }
            savedUndoableEditListeners = null;
        }
    }

    protected CompoundEdit createModelUndoableEdit() {
        return new ModelUndoableEdit();
    }

    protected class ModelUndoableEditSupport extends UndoableEditSupport {
        
        @Override
        protected CompoundEdit createCompoundEdit() {
            return createModelUndoableEdit();
        }
        
        protected void abortUpdate() {
            ModelUndoableEdit mue = (ModelUndoableEdit) compoundEdit;
            mue.justUndo();
            super.compoundEdit = createCompoundEdit();
            super.updateLevel = 0;
    }
    }
    
    public boolean inSync() {
        return inSync;
    }
    
    protected void setInSync(boolean v) {
        inSync = v;
    }
    
    public boolean inUndoRedo() {
        return inUndoRedo;
    }
    
    protected void setInUndoRedo(boolean v) {
        inUndoRedo = v;
    }

    public State getState() {
        return status;
    }
    
    protected void setState(State s) {
        if (s == status) {
            return;
        }
        State old = status;
        status = s;
        PropertyChangeEvent event = new PropertyChangeEvent(this, STATE_PROPERTY, old, status);
        if (isIntransaction()) {
            firePropertyChangeEvent(event);
        } else {
            pcs.firePropertyChange(event);
        }
    }
    
    /**
     * This method is overridden by subclasses to determine if sync needs to be
     * performed. The default implementation simply returns true.
     */
    protected boolean needsSync() {
    return true;
    }
    
    /**
     * This template method is invoked when a transaction is started. The 
     * default implementation does nothing.  
     */
    protected void transactionStarted() {
    
    }
    
    /**
     * This method is invoked when a transaction has completed. The default 
     *  implementation  does nothing. 
     */
    protected void transactionCompleted() {
    
    }
    
    /**
     * This method is invoked when sync has started. The default implementation 
     * does nothing. 
     */
    protected void syncStarted() {
    
    }
    
    /**
     * This method is invoked when sync has completed. The default implementation 
     * does nothing. 
     */
    protected void syncCompleted() {
    
    }
    
    /**
     * Prepare for sync.  This allow splitting calculation intensive work from
     * event firing tasks that are mostly running on UI threads.  This should be
     * optional step, meaning the actual call sync() should task care of the
     * preparation if it is not done.
     */
    private void prepareSync() {
        if (needsSync()) {
            getAccess().prepareSync();
        }
    }
    
    public synchronized void sync() throws java.io.IOException {
        if (needsSync()) {
            syncStarted();
            boolean syncStartedTransaction = false;
            boolean success = false;
            try {
                startTransaction(true, false);  //start pseudo transaction for event firing
                syncStartedTransaction = true;
                setState(getAccess().sync());
                endTransaction();
                success = true;
            } catch (IOException e) {
                setState(State.NOT_WELL_FORMED);
                endTransaction(false); // do want to fire just the state transition event
                throw e;
            } finally {
                if (syncStartedTransaction && isIntransaction()) { //CR: consider separate try/catch
                    try {
                        endTransaction(true); // do not fire events
                    } catch(Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Sync cleanup error.", ex); //NOI18N
                    }
                }

                if (!success && getState() != State.NOT_WELL_FORMED) {
                    setState(State.NOT_SYNCED);
                    refresh(); 
                }
                
                setInSync(false);
                syncCompleted();
            }
        }
    }
    
    /**
     * Refresh the domain model component trees.  The model state should be VALID as the 
     * result of this call.
     * Note: subclasses need to override to provide the actual refresh service.
     */
    protected void refresh() {
        setState(State.VALID);
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
    
    public synchronized void endTransaction() {
        endTransaction(false);
    }
    
    protected synchronized void endTransaction(boolean quiet) {
        if (transaction == null) return;  // just no-op when not in transaction
        validateWrite(); // ensures that the releasing thread really owns trnx
        try {
            if (! quiet) {
                transaction.fireEvents();
            }
            // no-need to flush or undo/redo support while in sync
            if (! inSync() && transaction.hasEvents() ||
                transaction.hasEventsAfterFiring()) {
                getAccess().flush();
            }
            if (! inUndoRedo()) {
                ues.endUpdate();
            }
        } finally {
            transaction = null;
            setInSync(false);
            setInUndoRedo(false);
            transactionSemaphore.release();
            transactionCompleted();
        }
    }

    public boolean startTransaction() {
        return startTransaction(false, false);
    }
    
    private synchronized boolean startTransaction(boolean inSync, boolean inUndoRedo) {
        if (transaction != null && transaction.currentThreadIsTransactionThread()) {
            throw new IllegalStateException(
            "Current thread has already started a transaction");
        }
        
        if (! inSync && ! getModelSource().isEditable()) {
            throw new IllegalArgumentException("Model source is read-only.");
        }
        
        transactionSemaphore.acquireUninterruptibly();
        // other correctly behaving threads will be blocked acquiring the 
        // semaphore here. Also store the current Thread to ensure that 
        // no other writes are occurring
        assert transaction == null;
        
        if (! inSync && getState() == State.NOT_WELL_FORMED) {
        transactionSemaphore.release();
            return false;
        }

        transaction = new Transaction();
        transactionStarted();
        setInSync(inSync);
        setInUndoRedo(inUndoRedo);
        
        if (! inUndoRedo) {
            ues.beginUpdate();
        }
        
        return true;
    }
    
    public synchronized void rollbackTransaction() {
        if (transaction == null) return;  // just no-op when not in transaction
        validateWrite(); // ensures that the releasing thread really owns trnx
        try {
            if (inSync() || inUndoRedo()) {
                throw new IllegalArgumentException("Should never call rollback during sync or undo/redo.");
            }
            ues.abortUpdate();
        } finally {
            transaction = null;
            setInSync(false);
            setInUndoRedo(false);
            transactionSemaphore.release();
            transactionCompleted();
        }
    }

    // # 121042
    protected synchronized void finishTransaction() {
        if (transaction == null) return;  // just no-op when not in transaction
        validateWrite(); // ensures that the releasing thread really owns trnx
        try {
            if (inSync() || inUndoRedo()) {
                throw new IllegalArgumentException("Should never call rollback during sync or undo/redo.");
            }
        } finally {
            transaction = null;
            setInSync(false);
            setInUndoRedo(false);
            transactionSemaphore.release();
            transactionCompleted();
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
        private Boolean eventsAddedAfterFiring;
        private boolean hasEvents;
        
        public Transaction() {
            propertyChangeEvents = new ArrayList<PropertyChangeEvent>();
            componentListenerEvents = new ArrayList<ComponentEvent>();
            transactionThread = Thread.currentThread();
            eventAdded = false;
            eventsAddedAfterFiring = null;
            hasEvents = false;
        }
        
        public void addPropertyChangeEvent(PropertyChangeEvent pce) {
            propertyChangeEvents.add(pce);
            // do not chain events during undo/redo
            if (eventsAddedAfterFiring == null || ! inUndoRedo) {
                eventAdded = true;
            }
            if (eventsAddedAfterFiring != null) {
                eventsAddedAfterFiring = Boolean.TRUE;
            }
            hasEvents = true;
        }
        
        public void addComponentEvent(ComponentEvent cle) {
            componentListenerEvents.add(cle);
            // do not chain events during undo/redo
            if (eventsAddedAfterFiring == null || ! inUndoRedo) {
                eventAdded = true;
            }
            if (eventsAddedAfterFiring != null) {
                eventsAddedAfterFiring = Boolean.TRUE;
            }
            hasEvents = true;
        }
        
        public boolean currentThreadIsTransactionThread() {
            return Thread.currentThread().equals(transactionThread);
        }
        
        public void fireEvents() {
            if (eventsAddedAfterFiring == null) {
                eventsAddedAfterFiring = Boolean.FALSE;
            }
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
            //should clear event list
            propertyChangeEvents.clear();
            for (PropertyChangeEvent pce:clonedEvents) {
                pcs.firePropertyChange(pce);
            }
            
            final List<ComponentEvent> cEvents = 
                new ArrayList<ComponentEvent>(componentListenerEvents); 
            //should clear event list
            componentListenerEvents.clear();
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
        
        public boolean hasEvents() {
            return hasEvents;
        }

        public boolean hasEventsAfterFiring() {
            return eventsAddedAfterFiring != null && eventsAddedAfterFiring.booleanValue();
        }
    }
    
    /**
     * Whether the model has started firing events.  This is the indication of 
     * beginning of endTransaction call and any subsequent mutations are from
     * handlers of main transaction events or some of their own events.
     */
    public boolean startedFiringEvents() {
        return transaction != null && transaction.eventsAddedAfterFiring != null;
    }
    
    protected class ModelUndoableEdit extends CompoundEdit {
        static final long serialVersionUID = 1L;
        
        public boolean addEdit(UndoableEdit anEdit) {
            if (! isInProgress()) return false;
            UndoableEdit last = lastEdit();
            if (last == null) {
                return super.addEdit(anEdit);
            } else {
                if (! last.addEdit(anEdit)) {
                    return super.addEdit(anEdit);
                } else {
                    return true;
                }
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            boolean redoStartedTransaction = false;
            boolean needsRefresh = true;
            try {
                startTransaction(true, true); //start pseudo transaction for event firing
                redoStartedTransaction = true;
                AbstractModel.this.getAccess().prepareForUndoRedo();
                super.redo(); 
                AbstractModel.this.getAccess().finishUndoRedo();
                endTransaction();
                needsRefresh = false;
            } catch(CannotRedoException ex) {
                needsRefresh = false;
                throw ex;
            } finally {
                if (isIntransaction() && redoStartedTransaction) {
                    try {
                        endTransaction(true); // do not fire events
                    } catch(Exception e) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Redo error", e); //NOI18N
                    }
                }
                if (needsRefresh) {
                    setState(State.NOT_SYNCED);
                    refresh();
                }
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            boolean undoStartedTransaction = false;
            boolean needsRefresh = true;
            try {
                startTransaction(true, true); //start pseudo transaction for event firing
                undoStartedTransaction = true;
                AbstractModel.this.getAccess().prepareForUndoRedo();
                super.undo(); 
                AbstractModel.this.getAccess().finishUndoRedo();
                endTransaction();
                needsRefresh = false;
            } catch(CannotUndoException ex) {
                needsRefresh = false;
                throw ex;
            } finally {
                if (undoStartedTransaction && isIntransaction()) {
                    try {
                        endTransaction(true); // do not fire events
                    } catch(Exception e) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "Undo error", e); //NOI18N
                    }
                }
                if (needsRefresh) {
                    setState(State.NOT_SYNCED); 
                    refresh(); 
                }
            }
        }
        
        public void justUndo() {
            super.end();
            boolean oldValue = AbstractModel.this.inUndoRedo;
            AbstractModel.this.inUndoRedo = true;
            AbstractModel.this.getAccess().prepareForUndoRedo();
            super.undo();
            AbstractModel.this.getAccess().finishUndoRedo();
            AbstractModel.this.inUndoRedo = oldValue;
    }
    }
    
    public void undoableEditHappened(UndoableEditEvent e) {
        ues.postEdit(e.getEdit());
    }

    public ModelSource getModelSource() {
        return source;
    }
    
    EventListenerList getComponentListenerList() {
        return componentListeners;
    }
    
    public boolean isAutoSyncActive() {
        return getAccess().isAutoSync();
    }
    
    public void setAutoSyncActive(boolean v) {
        getAccess().setAutoSync(v);
    }
    
    void runAutoSync() {
        prepareSync();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    sync();
                } catch(Exception ioe) {
                    // just have to be quiet during background autosync
                    // sync() should have handled all faults
                }
            }
        });
    }
}


