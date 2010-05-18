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

package org.netbeans.modules.bpel.debugger.ui;

import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.modules.bpel.debugger.ui.breakpoint.BpelLineBreakpointView;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.spi.viewmodel.Model;
import org.openide.util.RequestProcessor;



/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager} on
 * {@link BpelDebugger#PROP_CURRENT_POSITION}
 * property and annotates current line in NetBeans editor.
 *
 * @author Alexander Zgursky
 */
public class CurrentPositionAnnotationListener extends DebuggerManagerAdapter {

    // annotation for current line
    private transient Object myCurrentPositionAnnotation;
    private transient Object myCurrentlyExecutingAnnotation;
    
    private Position myCurrentPosition;
    private BpelDebugger myCurrentDebugger;
    private SourcePath mySourcePath;
    
    private Queue<Position> myPositionsQueue = new LinkedList<Position>();
    private boolean doAnnotate = true;
    
    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    /**
     * Listens BpelDebuggerImpl and DebuggerManager.
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(e.getPropertyName())) {
            updateCurrentDebugger();
            updateCurrentPosition();
            annotate();
        } else if (BpelDebugger.PROP_CURRENT_POSITION.equals(
                e.getPropertyName())) {
            updateCurrentPosition();
            annotate();
        }
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void updateCurrentDebugger () {
        BpelDebugger newDebugger = getCurrentDebugger();
        if (myCurrentDebugger == newDebugger) {
            return;
        }
        
        if (myCurrentDebugger != null) {
            myCurrentDebugger.removePropertyChangeListener(this);
        }
        
        if (newDebugger != null) {
            newDebugger.addPropertyChangeListener(this);
            mySourcePath = getCurrentSourcePath();
        }
        
        myCurrentDebugger = newDebugger;
        
        if (myCurrentDebugger == null) {
            stopAnnotatingTask();
        } else {
            startAnnotatingTask();
        }
    }
    
    private static BpelDebugger getCurrentDebugger() {
        final DebuggerEngine currentEngine = DebuggerManager.
                getDebuggerManager().getCurrentEngine();
        
        if (currentEngine == null) {
            return null;
        }
        
        return currentEngine.lookupFirst(null, BpelDebugger.class);
    }
    
    private static SourcePath getCurrentSourcePath() {
        final DebuggerEngine currentEngine = DebuggerManager.
                getDebuggerManager().getCurrentEngine();
        
        if (currentEngine == null) {
            return null;
        }
        
        return currentEngine.lookupFirst(null, SourcePath.class);
    }
    
    private void updateCurrentPosition() {
        if (myCurrentDebugger != null) {
            myCurrentPosition = myCurrentDebugger.getCurrentPosition();
        } else {
            myCurrentPosition = null;
        }
        
        if (getBreakpointsNodeModel() != null) {
            getBreakpointsNodeModel().setCurrentPosition(myCurrentPosition);
        }
    }
    
    /**
     * Annotates current position or removes annotation.
     */
    private void annotate() {
        //System.out.println("-- offering - " + myCurrentPosition);
        myPositionsQueue.offer(myCurrentPosition);
        
        synchronized (myPositionsQueue) {
            myPositionsQueue.notifyAll();
        }
    }
    
    private void startAnnotatingTask() {
        doAnnotate = true;
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                while (doAnnotate) {
                    synchronized (myPositionsQueue) {
                        while (!myPositionsQueue.isEmpty()) {
                            final Position position = myPositionsQueue.poll();
                            
                            removeAnnotations();
                            
                            if (position != null) {
                                final String url = mySourcePath.getSourcePath(
                                        position.getProcessQName());
                                        
                                if (url != null) {
                                    myCurrentPositionAnnotation = 
                                            EditorContextBridge.addAnnotation(
                                                    url,
                                                    position.getXpath(),
                                                    position.getLineNumber(),
                                                    AnnotationType.CURRENT_POSITION);
                                                    
                                    EditorContextBridge.showSource(
                                            url,
                                            position.getXpath(),
                                            null);
                                }
                            } else {
                                if (myCurrentDebugger == null) {
                                    continue;
                                }
                                
                                final ProcessInstance currentInstance = 
                                        myCurrentDebugger.getCurrentProcessInstance();
                                        
                                if (currentInstance == null) {
                                    continue;
                                }
                                
                                final String url = mySourcePath.getSourcePath(
                                        currentInstance.getProcess().getQName());
                                        
                                if (url == null) {
                                    continue;
                                }
                                
                                final PemEntity pemEntity = currentInstance.
                                        getProcessExecutionModel().getLastStartedEntity();
                                        
                                if (pemEntity != null) {
                                    final PsmEntity psmEntity = pemEntity.getPsmEntity();
                                    
                                    final BpelModel model = ModelUtil.getBpelModel(
                                            currentInstance.getProcess().getQName());
                                            
                                    final int lineNumber = ModelUtil.getLineNumber(
                                            model, psmEntity.getXpath());
                                            
                                    myCurrentlyExecutingAnnotation = 
                                            EditorContextBridge.addAnnotation(
                                                    url,
                                                    psmEntity.getXpath(),
                                                    lineNumber,
                                                    AnnotationType.CURRENTLY_EXECUTING);
                                }
                            }
                        }
                    }
                    
                    try {
                        synchronized (myPositionsQueue) {
                            myPositionsQueue.wait(1000);
                        }
                    } catch (InterruptedException e) {
                        // We should ignore this exception, as it is basically 
                        // harmless. If we were interrupted, just repeat the 
                        // iteration and sleep again.
                    }
                }
                
                removeAnnotations();
            }
            
            private void removeAnnotations() {
                if (myCurrentPositionAnnotation != null) {
                    EditorContextBridge.removeAnnotation(
                            myCurrentPositionAnnotation);
                    myCurrentPositionAnnotation = null;
                }
                
                if (myCurrentlyExecutingAnnotation != null) {
                    EditorContextBridge.removeAnnotation(
                            myCurrentlyExecutingAnnotation);
                    myCurrentlyExecutingAnnotation = null;
                }
            }
        });
    }
    
    private void stopAnnotatingTask() {
        doAnnotate = false;
        
        synchronized (myPositionsQueue) {
            myPositionsQueue.notifyAll();
        }
    }
    
    private BpelLineBreakpointView myBreakpointsNodeModel;
    private BpelLineBreakpointView getBreakpointsNodeModel () {
        if (myBreakpointsNodeModel == null) {
            List l = DebuggerManager.getDebuggerManager ().lookup
                ("BreakpointsView", Model.class);
            Iterator it = l.iterator ();
            while (it.hasNext ()) {
                Model nm = (Model) it.next ();
                if (nm instanceof BpelLineBreakpointView) {
                    myBreakpointsNodeModel = (BpelLineBreakpointView) nm;
                    break;
                }
            }
        }
        return myBreakpointsNodeModel;
    }
    
}
