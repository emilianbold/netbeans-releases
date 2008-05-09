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

package org.netbeans.modules.bpel.debugger.pem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity.State;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel;
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.ProcessInstanceImpl;
import org.netbeans.modules.bpel.debugger.eventlog.ActivityCompletedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.ActivityStartedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.ActivityTerminatedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.BranchCompletedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.BranchStartedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.EventLog;
import org.netbeans.modules.bpel.debugger.eventlog.EventRecord;
import org.netbeans.modules.bpel.debugger.eventlog.ProcessInstanceCompletedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.ProcessInstanceStartedRecord;

/**
 *
 * @author Alexander Zgursky
 */
public class ProcessExecutionModelImpl implements ProcessExecutionModel {
    
    private final ProcessInstanceImpl myProcessInstance;
    private final ProcessStaticModel myPsm;
    
    private final Map<String, BranchImpl> myBranches = 
            new HashMap<String, BranchImpl>();
    private BranchImpl myCurrentBranch;
    
    private final List<Listener> myListeners = 
            new LinkedList<Listener>();
    
    private PemEntityImpl myRoot;
    private PemEntityImpl myLastStartedEntity;
    
    private final EventLog myEventLog;
    private final EventLogListener myEventLogListener;
    private int myLastEventIndex = -1;
    
    public static ProcessExecutionModelImpl build(
            final ProcessInstanceImpl processInstance) {
        if (processInstance.getEventLog() == null) {
            return null;
        }
        
        if (processInstance.getProcess().getProcessStaticModel() == null) {
            return null;
        }
        
        final ProcessExecutionModelImpl pem = 
                new ProcessExecutionModelImpl(processInstance);
        pem.update();
        
        return pem;
    }
    
    /** Creates a new instance of ProcessExecutionModel */
    private ProcessExecutionModelImpl(
            final ProcessInstanceImpl processInstance) {
        myProcessInstance = processInstance;
        myPsm = myProcessInstance.getProcess().getProcessStaticModel();
        
        myEventLog = myProcessInstance.getEventLog();
        myEventLogListener = new EventLogListener();
        myEventLog.addListener(myEventLogListener);
    }
    
    public ProcessStaticModel getProcessStaticModel() {
        return myPsm;
    }
    
    public PemEntityImpl getRoot() {
        synchronized (this) {
            return myRoot;
        }
    }
    
    public PemEntityImpl getLastStartedEntity() {
        return myLastStartedEntity;
    }
    
    public Branch[] getBranches() {
        return myBranches.values().toArray(new Branch[myBranches.size()]);
    }
    
    public Branch[] getBranches(
            final Branch parent) {
        
        final List<Branch> filtered = new LinkedList<Branch>();
        
        for (BranchImpl branch: myBranches.values()) {
            if ((parent == null) && (branch.getParent() == null)) {
                filtered.add(branch);
                continue;
            }
            
            if ((parent != null) && 
                    (branch.getParent() != null) && 
                    branch.getParent().equals(parent)) {
                filtered.add(branch);
            }
        }
        
        return filtered.toArray(new Branch[filtered.size()]);
    }
    
    public Branch getCurrentBranch() {
        return myCurrentBranch;
    }
    
    public void setCurrentBranch(
            final String id) {
        
        myCurrentBranch = myBranches.get(id);
        myProcessInstance.stepInto();
    }
    
    public void setCurrentBranchWithoutResume(
            final String id) {
        myCurrentBranch = myBranches.get(id);
    }
    
    public void addListener(
            final Listener listener) {
        synchronized (myListeners) {
            myListeners.add(listener);
        }
    }
    
    public void removeListener(
            final Listener listener) {
        synchronized (myListeners) {
            myListeners.remove(listener);
        }
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void update() {
        boolean modelChanged = false;
        
        synchronized (this) {
            final int logSize = myEventLog.getSize();
            if (logSize <= myLastEventIndex + 1) {
                return;
            }
            
            final EventRecord[] newRecords = 
                    myEventLog.getRecords(myLastEventIndex + 1, logSize);
            for (EventRecord record : newRecords) {
                myLastEventIndex++;
                if (record instanceof ActivityStartedRecord) {
                    update((ActivityStartedRecord) record);
                } else if (record instanceof ActivityCompletedRecord) {
                    update((ActivityCompletedRecord) record);
                } else if (record instanceof ActivityTerminatedRecord) {
                    update((ActivityTerminatedRecord) record);
                } else if (record instanceof BranchStartedRecord) {
                    update((BranchStartedRecord) record);
                } else if (record instanceof BranchCompletedRecord) {
                    update((BranchCompletedRecord) record);
                } else if (record instanceof ProcessInstanceStartedRecord) {
                    update((ProcessInstanceStartedRecord) record);
                } else if (record instanceof ProcessInstanceCompletedRecord) {
                    update((ProcessInstanceCompletedRecord) record);
                }
                modelChanged = true;
            }
        }
        
        if (modelChanged) {
            //TODO: if there are new events it doens't mean the model is changed
            fireModelUpdated();
        }
    }
    
    private void update(
            final ActivityStartedRecord record) {
        final PsmEntity psmEntity = myPsm.find(record.getActivityXpath());
        if (psmEntity == null) {
            return;
        }
        
        final BranchImpl branch = myBranches.get(record.getBranchId());
        if (branch == null) {
            return;
        }
        
        branch.activityStarted(psmEntity);
    }
    
    private void update(
            final ActivityCompletedRecord record) {
        final PsmEntity psmEntity = myPsm.find(record.getActivityXpath());
        if (psmEntity == null) {
            return;
        }
        
        final BranchImpl branch = myBranches.get(record.getBranchId());
        if (branch == null) {
            return;
        }
        
        branch.activityCompleted(psmEntity);
    }
    
    private void update(
            final ActivityTerminatedRecord record) {
        final PsmEntity psmEntity = myPsm.find(record.getActivityXpath());
        if (psmEntity == null) {
            return;
        }
        
        final BranchImpl branch = myBranches.get(record.getBranchId());
        if (branch == null) {
            return;
        }
        
        branch.activityTerminated(psmEntity);
    }
    
    private void update(
            final BranchStartedRecord record) {
        final String parentId = record.getParentBranchId();
        
        BranchImpl parentBranch = null;
        if (parentId != null) {
            parentBranch = myBranches.get(parentId);
        }
        
        final BranchImpl newBranch = 
                new BranchImpl(record.getBranchId(), parentBranch);
        
        final Branch parent = newBranch.getParent();
        if (((parent == null) && (myCurrentBranch == null)) || 
                ((parent != null) && (parent.equals(myCurrentBranch)))) {
            myCurrentBranch = newBranch;
        }
        
        newBranch.setState(Branch.State.ACTIVE);
        
        myBranches.put(record.getBranchId(), newBranch);
    }
    
    private void update(
            final BranchCompletedRecord record) {
        final BranchImpl branch = myBranches.get(record.getBranchId());
        
        if (branch != null) {
            branch.setState(Branch.State.COMPLETED);
            
            if ((myCurrentBranch != null ) && branch.equals(myCurrentBranch)) {
                myCurrentBranch = branch.getParent();
                
                if (myCurrentBranch == null) {
                    for (Branch temp: myBranches.values()) {
                        if ((temp.getParent() == null) && 
                                (temp.getState() == Branch.State.ACTIVE)) {
                            myCurrentBranch = (BranchImpl) temp;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void update(
            final ProcessInstanceStartedRecord record) {
        final PsmEntity psmRoot = myPsm.getRoot();
        
        myRoot = createEntity(psmRoot, null, true);
        myRoot.setState(State.STARTED);
        myRoot.setLastStartedEventIndex(myLastEventIndex);
    }
    
    private void update(
            final ProcessInstanceCompletedRecord record) {
        myRoot.setState(State.COMPLETED);
    }
    
    private void fireModelUpdated() {
        final Listener[] listeners;
        synchronized (myListeners) {
            listeners = myListeners.toArray(new Listener[myListeners.size()]);
        }
        
        for (Listener listener : listeners) {
            listener.modelUpdated();
        }
    }
    
    private PemEntityImpl createEntity(
            final PsmEntity psmEntity, 
            final String branchId, 
            final boolean isReceivingEvents) {
        if (psmEntity.isLoop()) {
            return new PemLoopEntityImpl(
                    this, psmEntity, branchId, isReceivingEvents);
        } else {
            return new PemNonLoopEntityImpl(
                    this, psmEntity, branchId, isReceivingEvents);
        }
    }
    
    /**
     * Returns an array of {@link PemEntityImpl} objects, which correspond to 
     * the given {@link PsmEntity}. In most cases there would be only one, but
     * when loops are involved, who can be sure?
     * 
     * @param psmEntity {@link PsmEntity} for which the corresponding 
     *      {@link PemEntityImpl} objects should be found.
     * @return an array of {@link PemEntityImpl} objects that correspond to the
     *      given {@link PsmEntity}.
     */
    private PemEntityImpl[] find(
            final PsmEntity psmEntity) {
        final Queue<PemEntityImpl> queue = new LinkedList<PemEntityImpl>();
        final List<PemEntityImpl> filtered = new LinkedList<PemEntityImpl>();
        
        queue.offer(myRoot);
        
        while (queue.peek() != null) {
            final PemEntityImpl pemEntity = queue.poll();
            
            if (pemEntity.getPsmEntity().equals(psmEntity)) {
                filtered.add(pemEntity);
            }
            
            for (PemEntityImpl child: pemEntity.getChildren()) {
                queue.offer(child);
            }
        }
        
        return filtered.toArray(new PemEntityImpl[filtered.size()]);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class BranchImpl implements Branch {
        private String myId;
        private BranchImpl myParent;
        private Stack<PemEntityImpl> myCallStack =
                new Stack<PemEntityImpl>();
        
        private Branch.State myState;
        
        public BranchImpl(
                final String id, 
                final BranchImpl parent) {
            myId = id;
            myParent = parent;
        }
        
        public String getId() {
            return myId;
        }
        
        public BranchImpl getParent() {
            return myParent;
        }
        
        public Branch.State getState() {
            return myState;
        }
        
        public PemEntityImpl getCurrentActivity() {
            if (!myCallStack.empty()) {
                return myCallStack.peek();
            }
            
            return null;
        }
        
        public Stack<PemEntity> getCallStack() {
            final Stack<PemEntity> stack = new Stack<PemEntity>();
            
            stack.addAll(myCallStack);
            
            return stack;
        }
        
        public void activityStarted(final PsmEntity psmEntity) {
            final PsmEntity psmParent = psmEntity.getParent();
            
            PemEntityImpl pemEntity = createEntity(psmEntity, myId, true);
            
            final String psmParentTag = psmParent.getTag();
            if (psmParentTag.equals("compensationHandler") ||
                    psmParentTag.equals("terminationHandler")) {
                // If the current activity is the direct child of a 
                // handler (there can be only one), we need to construct 
                // the PEM entity for the handler, register the currently 
                // executed one as its child and put the handler itself to 
                // the model.
                final PsmEntity handlerPsm = psmParent;
                final PsmEntity scopePsm = psmParent.getParent();
                
                final PemEntityImpl handlerPem = 
                        createEntity(handlerPsm, myId, true);
                        
                handlerPem.addChild(pemEntity);
                
                // We need to find the PEM entity of the scope which 
                // contains this handler. Then we'll attach the newly 
                // created handler PEM entity to the scope's one.
                final PemEntityImpl[] pems = find(scopePsm);
                
                if (pems.length == 0) {
                    return;
                }
                
                // Choose the candidate with the largest index, which does 
                // not already have a PEM handler attached. Note that it
                // holds true for both compensation and termination 
                // handlers as the former get executed in reverse order, 
                // and for the latter -- only the latest one (whose scope 
                // was terminated) will be executed.
                PemEntityImpl scopePem = pems[0];
                for (int i = 1; i < pems.length; i++) {
                    if ((pems[i].getIndex() > scopePem.getIndex()) && 
                            (pems[i].getChildren(handlerPsm).length == 0)) {
                        scopePem = pems[i];
                    }
                }
                
                scopePem.addChild(handlerPem);
                myCallStack.push(handlerPem);
            } else {
                if (!myCallStack.empty()) {
                    final PemEntityImpl pemParent = myCallStack.peek();
                    
                    // Sometimes runtime can set duplicate STARTED events. The most 
                    // common case for this is in while and repeatUntil activities,
                    // where these duplicates are used to differentiate between 
                    // iterations. In this case we simply ignore them, but restore 
                    // the myLastStartedEntity variable's value.
                    if (pemParent.getPsmEntity() == psmEntity) {
                        // Note that we update the myLastStartedEntity with 
                        // pemParent, and not pemEntity, as the latter is 
                        // synthetically constructed and does not belong to the 
                        // model tree.
                        myLastStartedEntity = pemParent;
                        return;
                    }
                    
                    // In the general case try to rebuild the 'pem' tree part for 
                    // the given entity and then attach it to the current parent
                    final PemEntityImpl pemChild = 
                            buildSyntheticEntities(pemParent, pemEntity);
                    if (pemChild != null) {
                        if (pemChild.getPsmEntity().getTag().equals(
                                "faultHandlers")) {
                            addEventHandlersToParent(pemChild);
                        } else {
                            pemParent.addChild(pemChild);
                        }
                    }
                } else {
                    if (myParent == null) {
                        // It's a root branch or an event handler branch.
                        if (psmEntity.getTag().equals("onAlarm") || 
                                psmEntity.getTag().equals("onEvent")) {
                            // We need to find (or create) a PEM entity of the 
                            // <eventHandlers> tag. In the latter case we also need
                            // to attach it to its PEM parent (process or scope).
                            PemEntityImpl[] pems = find(psmParent);
                            
                            final PemEntityImpl handlersPem;
                            if (pems.length == 0) {
                                handlersPem = createEntity(psmParent, myId, true);
                                
                                pems = find(psmParent.getParent());
                                
                                if (pems.length == 0) {
                                    return;
                                }
                                
                                PemEntityImpl scopePem = pems[0];
                                for (int i = 1; i < pems.length; i++) {
                                    if ((pems[i].getIndex() > scopePem.getIndex()) && 
                                            (pems[i].getChildren(psmEntity).length == 0)) {
                                        scopePem = pems[i];
                                    }
                                }
                                
                                scopePem.addChild(handlersPem);
                            } else {
                                handlersPem = pems[0];
                            }
                            
                            // If the handlers PEM entity does not contain a child 
                            // of this type (<onAlarm> or <onEvent>) -- add it. 
                            // Otherwise, just reuse the existing one.
                            pems = handlersPem.getChildren(psmEntity);
                            if (pems.length == 0) {
                                handlersPem.addChild(pemEntity);
                            } else {
                                pemEntity = pems[0];
                            }
                            
                            myCallStack.push(handlersPem);
                        } else {
                            myRoot.addChild(pemEntity);
                        }
                    } else if (psmParent.getTag().equals("flow")) {
                        // It's a flow branch
                        myParent.getCurrentActivity().addChild(pemEntity);
                    }
                }
            }            
            
            pemEntity.setState(PemEntity.State.STARTED);
            pemEntity.setLastStartedEventIndex(myLastEventIndex);
            
            myLastStartedEntity = pemEntity;
            myCallStack.push(pemEntity);
        }
        
        public void activityCompleted(final PsmEntity psmEntity) {
            if (!myCallStack.isEmpty()) {
                PemEntityImpl pemEntity = myCallStack.peek();
                
                if (pemEntity.getPsmEntity() == psmEntity) {
                    myCallStack.pop();
                    
                    if (pemEntity == myLastStartedEntity) {
                        myLastStartedEntity = null;
                    }
                    pemEntity.setState(PemEntity.State.COMPLETED);
                    
                    // If this emptied the stack, no other actions need to be 
                    // taken. Otherwise -- BUSINESS LOGIC !! ;)
                    if (myCallStack.size() == 0) {
                        return;
                    }
                    
                    // If the last activity on call stack is a termination or a 
                    // compensation handler or event handlers container -- 
                    // remove it. The runtime does not send any events which 
                    // would allow us to recognize its completion.
                    pemEntity = myCallStack.peek();
                    
                    final String pemEntityTag = 
                            pemEntity.getPsmEntity().getTag();
                    if (pemEntityTag.equals("terminationHandler") || 
                            pemEntityTag.equals("compensationHandler") ||
                            pemEntityTag.equals("eventHandlers")) {
                        myCallStack.pop();
                        
                        pemEntity.setState(PemEntity.State.COMPLETED);
                    }
                }
            }
        }
        
        public void activityTerminated(final PsmEntity psmEntity) {
            if (!myCallStack.isEmpty()) {
                // If the call stack is not empty -- try to remove all entities,
                // that are "below" it
                PsmEntity tip = myCallStack.peek().getPsmEntity();
                while ((tip != null) && 
                        tip.getXpath().startsWith(psmEntity.getXpath())) {
                    
                    final PemEntityImpl pemEntity = myCallStack.pop();
                    
                    pemEntity.setState(PemEntity.State.COMPLETED);
                    
                    if (myCallStack.isEmpty()) {
                        tip = null;
                    } else {
                        tip = myCallStack.peek().getPsmEntity();
                    }
                }
            }
        }
        
        // Private /////////////////////////////////////////////////////////////
        private void setState(
                final Branch.State newState) {
            myState = newState;
        }
        
        private PemEntityImpl findInCallStack(
                final PsmEntity psmEntity) {
            
            if (myCallStack.isEmpty()) {
                return null;
            }
            
            for (PemEntityImpl candidate : myCallStack) {
                if (candidate.getPsmEntity() == psmEntity) {
                    return candidate;
                }
            }
            
            return null;
        }
        
        private PemEntityImpl findInUpperCallStacks(
                final PsmEntity psmEntity) {
            
            final PemEntityImpl pemEntity = findInCallStack(psmEntity);
            
            if (pemEntity != null) {
                return pemEntity;
            }
            
            if (myParent != null) {
                return myParent.findInUpperCallStacks(psmEntity);
            }
            
            return null;
        }
        
        private PemEntityImpl buildSyntheticEntities(
                final PemEntityImpl parent, 
                final PemEntityImpl child) {
            PemEntityImpl pemChild = child;
            
            while (pemChild.getPsmEntity().getParent() != 
                    parent.getPsmEntity()) {
                final PsmEntity newPsmEntity = 
                        pemChild.getPsmEntity().getParent();
                
                if (newPsmEntity == null) {
                    // Reached a root and still didn't find an appropriate 
                    // parent? Wierd...
                    return null;
                }
                
                final PemEntityImpl newPemEntity = 
                        createEntity(newPsmEntity, myId, false);
                newPemEntity.addChild(pemChild);
                pemChild = newPemEntity;
                
                if (newPsmEntity.getTag().equals("faultHandlers")) {
                    break;
                }
            }
            
            return pemChild;
        }
        
        private void addEventHandlersToParent(
                final PemEntityImpl pemEventHandlers) {
            PemEntityImpl parent;
            if (pemEventHandlers.getPsmEntity().getParent().getTag().equals("process")) {
                parent = myRoot;
            } else {
                parent = findInUpperCallStacks(pemEventHandlers.getPsmEntity().getParent());
            }
            
            if (parent != null) {
                parent.addChild(pemEventHandlers);
            } else {
//                System.out.println("Couldn't find parent entity for " +
//                        pemEventHandlers.getPsmEntity().getTag() + "(" +
//                        pemEventHandlers.getPsmEntity().getName() + ")");
            }
        }
    }
    
    private class EventLogListener implements EventLog.Listener {
        public void recordAdded(EventRecord record) {
            update();
        }
    }
}
