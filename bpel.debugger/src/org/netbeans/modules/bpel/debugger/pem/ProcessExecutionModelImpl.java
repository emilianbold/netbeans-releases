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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity.State;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel;
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.modules.bpel.debugger.bdiclient.impl.ProcessInstanceImpl;
import org.netbeans.modules.bpel.debugger.eventlog.ActivityCompletedRecord;
import org.netbeans.modules.bpel.debugger.eventlog.ActivityStartedRecord;
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
    private final EventLog myEventLog;
    private final EventLogListener myEventLogListener;
    private final Map<String, Branch> myBranches = new HashMap<String, Branch>();
    private final List<Listener> myListeners = new LinkedList<Listener>();
    
    private PemEntityImpl myRoot;
    private int myLastEventIndex = -1;
    private PemEntityImpl myLastStartedEntity;
    
    //TODO:ugly hack to workaround a bug in the runtime - "while" and "repeatUntil"
    //produce COMPLETED event for every iteration plus one more real COMPLETED event,
    //so we need to ignore the iteration events. This set stores the currently started
    //mentioned activities (see other parts of this hack below)
    private Set<PemEntityImpl> myLoops = new HashSet<PemEntityImpl>();
    //end of hack
    
    public static ProcessExecutionModelImpl build(ProcessInstanceImpl processInstance) {
        if (processInstance.getEventLog() == null) {
            return null;
        }
        if (processInstance.getProcess().getProcessStaticModel() == null) {
            return null;
        }
        ProcessExecutionModelImpl pem = new ProcessExecutionModelImpl(processInstance);
        pem.update();
        return pem;
    }
    
    /** Creates a new instance of ProcessExecutionModel */
    private ProcessExecutionModelImpl(ProcessInstanceImpl processInstance) {
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

    public void addListener(Listener listener) {
        synchronized (myListeners) {
            myListeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        synchronized (myListeners) {
            myListeners.remove(listener);
        }
    }
    
    private void update() {
        boolean modelChanged = false;
        synchronized (this) {
            int logSize = myEventLog.getSize();
            if (logSize <= myLastEventIndex + 1) {
                return;
            }
            EventRecord[] newRecords = myEventLog.getRecords(myLastEventIndex + 1, logSize);
            for (EventRecord record : newRecords) {
                myLastEventIndex++;
                if (record instanceof ActivityStartedRecord) {
                    update((ActivityStartedRecord)record);
                } else if (record instanceof ActivityCompletedRecord) {
                    update((ActivityCompletedRecord)record);
                } else if (record instanceof BranchStartedRecord) {
                    update((BranchStartedRecord)record);
                } else if (record instanceof BranchCompletedRecord) {
                    update((BranchCompletedRecord)record);
                } else if (record instanceof ProcessInstanceStartedRecord) {
                    update((ProcessInstanceStartedRecord)record);
                } else if (record instanceof ProcessInstanceCompletedRecord) {
                    update((ProcessInstanceCompletedRecord)record);
                }
                modelChanged = true;
            }
        }
        
        if (modelChanged) {
            //TODO: if there are new events it doens't mean the model is changed
            fireModelUpdated();
        }
    }
    
    private void update(ActivityStartedRecord record) {
        PsmEntity psmEntity = myPsm.find(record.getActivityXpath());
        if (psmEntity == null) {
            return;
        }
        
        Branch branch = myBranches.get(record.getBranchId());
        if (branch == null) {
            return;
        }
        
        branch.activityStarted(psmEntity);
    }
    
    private void update(ActivityCompletedRecord record) {
        PsmEntity psmEntity = myPsm.find(record.getActivityXpath());
        if (psmEntity == null) {
            return;
        }
        
        Branch branch = myBranches.get(record.getBranchId());
        if (branch == null) {
            return;
        }
        
        branch.activityCompleted(psmEntity);
    }
    
    private void update(BranchStartedRecord record) {
        String parentId = record.getParentBranchId();
        Branch parentBranch = null;
        if (parentId != null) {
            parentBranch = myBranches.get(parentId);
        }
        Branch branch = new Branch(record.getBranchId(), parentBranch);
        myBranches.put(record.getBranchId(), branch);
    }
    
    private void update(BranchCompletedRecord record) {
        //noop
    }
    
    private void update(ProcessInstanceStartedRecord record) {
        PsmEntity psmRoot = myPsm.getRoot();
        myRoot = createEntity(psmRoot, null, true);
        myRoot.setState(State.STARTED);
        myRoot.setLastStartedEventIndex(myLastEventIndex);
    }
    
    private void update(ProcessInstanceCompletedRecord record) {
        myRoot.setState(State.COMPLETED);
    }
    
    private void fireModelUpdated() {
        Listener[] listeners;
        synchronized (myListeners) {
            listeners = myListeners.toArray(new Listener[myListeners.size()]);
        }
        
        for (Listener listener : listeners) {
            listener.modelUpdated();
        }
    }
    
    private PemEntityImpl createEntity(
            PsmEntity psmEntity, String branchId, boolean isReceivingEvents)
    {
        if (psmEntity.isLoop()) {
            return new PemLoopEntityImpl(this, psmEntity, branchId, isReceivingEvents);
        } else {
            return new PemNonLoopEntityImpl(this, psmEntity, branchId, isReceivingEvents);
        }
    }

    private class Branch {
        private String myId;
        private Branch myParent;
        private Stack<PemEntityImpl> myCallStack =
                new Stack<PemEntityImpl>();
        
        public Branch(String id, Branch parent) {
            myId = id;
            myParent = parent;
        }
        
        public String getId() {
            return myId;
        }
        
        private PemEntityImpl buildSyntheticEntities(PemEntityImpl parent, PemEntityImpl child) {
            PemEntityImpl pemChild = child;
            while (pemChild.getPsmEntity().getParent() != parent.getPsmEntity()) {
                PsmEntity newPsmEntity = pemChild.getPsmEntity().getParent();
                if (newPsmEntity == null) {
                    //reached a root and still didn't found appropriate parent?
                    //weired...
                    System.out.println("Couldn't find parent entity for " +
                            pemChild.getPsmEntity().getTag() + "(" +
                            pemChild.getPsmEntity().getName() + ")");
                    return null;
                }
                PemEntityImpl newPemEntity = createEntity(newPsmEntity, myId, false);
                newPemEntity.addChild(pemChild);
                pemChild = newPemEntity;
                if (newPsmEntity.getTag().equals("faultHandlers")) {
                    break;
                }
            }
            return pemChild;
        }
        
        private void addEventHandlersToParent(PemEntityImpl pemEventHandlers) {
            PemEntityImpl parent;
            if (pemEventHandlers.getPsmEntity().getParent().getTag().equals("process")) {
                parent = myRoot;
            } else {
                parent = findInUpperCallStacks(pemEventHandlers.getPsmEntity().getParent());
            }
            
            if (parent != null) {
                parent.addChild(pemEventHandlers);
            } else {
                System.out.println("Couldn't find parent entity for " +
                        pemEventHandlers.getPsmEntity().getTag() + "(" +
                        pemEventHandlers.getPsmEntity().getName() + ")");
            }
        }
        
        public void activityStarted(PsmEntity psmEntity) {
            PsmEntity psmParent = psmEntity.getParent();
            PemEntityImpl pemEntity = createEntity(psmEntity, myId, true);
            if (!myCallStack.empty()) {
                PemEntityImpl pemParent = myCallStack.peek();
                //TODO:ugly hack to workaround a bug in the runtime -
                //ingore duplicated STARTED events
                if (pemParent.getPsmEntity() == psmEntity) {
                    System.out.println("Duplicated STARTED event received for " + psmEntity.getXpath());
                    return;
                }
                //end of hack
                
                //TODO:ugly hack to workaround a bug in the runtime - "while" and "repeatUntil"
                //produce COMPLETED event for every iteration plus one more real COMPLETED event,
                //so we need to ignore the iteration events.
                if ("while".equals(psmParent.getTag()) || "repeatUntil".equals(psmParent.getTag())) {
                    myLoops.add(pemParent);
                }
                //end of hack
                
                PemEntityImpl pemChild = buildSyntheticEntities(pemParent, pemEntity);
                if (pemChild != null) {
                    if (pemChild.getPsmEntity().getTag().equals("faultHandlers")) {
                        addEventHandlersToParent(pemChild);
                    } else {
                        pemParent.addChild(pemChild);
                    }
                }
            } else {
                if (myParent == null) {
                    //it's a root branch
                    myRoot.addChild(pemEntity);
                } else if (psmParent.getTag().equals("flow")) {
                    //it's a flow branch
                    myParent.getCurrentActivity().addChild(pemEntity);
                } else {
                    //it's an event handler branch
                    PsmEntity eventHandler = psmEntity.getParent();
                    PsmEntity handlers = eventHandler.getParent();
                    PsmEntity handlersParent = handlers.getParent();
                    PemEntityImpl pemHandlersParent;
                    if (handlersParent.getTag().equals("process")) {
                        pemHandlersParent = myRoot;
                    } else {
                        Branch handlersBranch = myParent;
                        Branch handlersParentBranch = handlersBranch.getParent();
                        pemHandlersParent = handlersParentBranch.findInCallStack(handlersParent);
                    }
                    
                    PemEntityImpl pemHandlers;
                    if (pemHandlersParent.hasChildren(handlers)) {
                        pemHandlers = pemHandlersParent.getChildren(handlers)[0];
                    } else {
                        pemHandlers = createEntity(handlers, myId, false);
                        pemHandlersParent.addChild(pemHandlers);
                    }
                    
                    PemEntityImpl pemEventHandler;
                    if (pemHandlers.hasChildren(eventHandler)) {
                        pemEventHandler = pemHandlers.getChildren(eventHandler)[0];
                    } else {
                        pemEventHandler = createEntity(eventHandler, myId, false);
                        pemHandlers.addChild(pemEventHandler);
                    }
                    
                    pemEventHandler.addChild(pemEntity);
                }
            }
            pemEntity.setState(State.STARTED);
            pemEntity.setLastStartedEventIndex(myLastEventIndex);
            myLastStartedEntity = pemEntity;
            myCallStack.push(pemEntity);
        }
        
        public void activityCompleted(PsmEntity psmEntity) {
            if (!myCallStack.isEmpty()) {
                PemEntityImpl pemEntity = myCallStack.peek();
                
                //TODO:ugly hack to workaround a bug in the runtime - "while" and "repeatUntil"
                //produce COMPLETED event for every iteration plus one more real COMPLETED event,
                //so we need to ignore the iteration events.
                if ("while".equals(psmEntity.getTag()) || "repeatUntil".equals(psmEntity.getTag())) {
                    if (myLoops.remove(pemEntity)) {
                        return;
                    }
                }
                //end of hack
                
                if (pemEntity.getPsmEntity() == psmEntity) {
                    myCallStack.pop();
                    if (pemEntity == myLastStartedEntity) {
                        myLastStartedEntity = null;
                    }
                    pemEntity.setState(State.COMPLETED);
                    return;
                }
            }
            System.out.println("Received 'Activity completed' event for " +
                    psmEntity.getTag() + "(" + psmEntity.getName() + ") in frame " +
                    myId + ", but it doesn't seem to be running");
        }
        
        public Branch getParent() {
            return myParent;
        }
        
        public PemEntityImpl getCurrentActivity() {
            if (!myCallStack.empty()) {
                return myCallStack.peek();
            } else {
                return null;
            }
        }
        
        public PemEntityImpl findInCallStack(PsmEntity psmEntity) {
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
        
        public PemEntityImpl findInUpperCallStacks(PsmEntity psmEntity) {
            PemEntityImpl pemEntity = findInCallStack(psmEntity);
            if (pemEntity != null) {
                return pemEntity;
            } else if (myParent != null) {
                return myParent.findInUpperCallStacks(psmEntity);
            } else {
                return null;
            }
        }
    }
    
    private class EventLogListener implements EventLog.Listener {
        public void recordAdded(EventRecord record) {
            update();
        }
    }
}
