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
import java.util.IdentityHashMap;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel;
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;

import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.openide.util.RequestProcessor;

/**
 * @author Alexander Zgursky
 */
public class ExecutionAnnotationsListener
        extends DebuggerManagerAdapter
        implements ProcessExecutionModel.Listener {

    // annotation for current line
    private ProcessInstance myCurrentProcessInstance;
    private ProcessExecutionModel myCurrentPem;
    private BpelDebugger myCurrentDebugger;
    private SourcePath mySourcePath;
    private final AnnotationsHolder myAnnotationsHolder = 
            new AnnotationsHolder();
            
    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }
    
    /**
     * Listens BpelDebuggerImpl and DebuggerManager.
     */
    @Override
    public void propertyChange (PropertyChangeEvent e) {
        if (DebuggerManager.PROP_CURRENT_ENGINE.equals(
                e.getPropertyName())) {
            
            updateCurrentDebugger();
            updateCurrentProcessInstance();
            annotate();
        } else if (BpelDebugger.PROP_CURRENT_PROCESS_INSTANCE.equals(
                e.getPropertyName())) {
            
            updateCurrentProcessInstance();
            annotate();
        }
    }
    
    public void modelUpdated() {
        annotate();
    }
    
    // helper methods ..........................................................

    private void updateCurrentDebugger () {
        BpelDebugger newDebugger = getCurrentDebugger();
        if (myCurrentDebugger == newDebugger) {
            return;
        }
        
        if (myCurrentDebugger != null) {
            myCurrentDebugger.removePropertyChangeListener(
                    BpelDebugger.PROP_CURRENT_PROCESS_INSTANCE, this);
        }
        
        if (newDebugger != null) {
            newDebugger.addPropertyChangeListener(BpelDebugger.PROP_CURRENT_PROCESS_INSTANCE, this);
            mySourcePath = getCurrentSourcePath();
        }
        myCurrentDebugger = newDebugger;
    }
    
    private static BpelDebugger getCurrentDebugger() {
        DebuggerEngine currentEngine = DebuggerManager.
                getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        return currentEngine.lookupFirst(null, BpelDebugger.class);
    }
    
    private static SourcePath getCurrentSourcePath() {
        DebuggerEngine currentEngine = DebuggerManager.
                getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        return currentEngine.lookupFirst(null, SourcePath.class);
    }

    private void updateCurrentProcessInstance() {
        if (myCurrentDebugger != null) {
            myCurrentProcessInstance = 
                    myCurrentDebugger.getCurrentProcessInstance();
        } else {
            myCurrentProcessInstance = null;
        }
        
        updatePem();
    }
    
    private void updatePem() {
        if (myCurrentPem != null) {
            myCurrentPem.removeListener(this);
        }
        
        if (myCurrentProcessInstance != null) {
            myCurrentPem = myCurrentProcessInstance.getProcessExecutionModel();
            
            if (myCurrentPem != null) {
                myCurrentPem.addListener(this);
            }
        } else {
            myCurrentPem = null;
        }
    }
    
    private RequestProcessor.Task  myAnnotationTask;
    
    private void annotate() {
        RequestProcessor.Task dependantTask = null;
        
        if (myAnnotationTask != null) {
            if (!myAnnotationTask.cancel()) {
                dependantTask = myAnnotationTask;
            }
        }
        
        final AnnotationRunnable annotationRunnable =
                new AnnotationRunnable(dependantTask, myCurrentPem);
        myAnnotationTask = RequestProcessor.getDefault().post(
                annotationRunnable, 200);
    }
    
    private class AnnotationRunnable implements Runnable {
        private final RequestProcessor.Task myDependantTask;
        private final ProcessExecutionModel myPem;
        
        public AnnotationRunnable(
                final RequestProcessor.Task dependantTask,
                final ProcessExecutionModel pem) {
            
            myDependantTask = dependantTask;
            myPem = pem;
        }
        
        public void run() {
            if (myDependantTask != null) {
                myDependantTask.waitFinished();
            }
            
            myAnnotationsHolder.annotate(myPem);
        }
    }
    
    private class AnnotationsHolder {
        private ProcessStaticModel myAnnotatedPsm;
        private String myAnnotatedUrl;
        private BpelModel myModel;
        
        private Map<PsmEntity, Object> myAnnotations =
                new IdentityHashMap<PsmEntity, Object>();
        private Map<PsmEntity, AnnotationType> myAnnotationTypes =
                new IdentityHashMap<PsmEntity, AnnotationType>();
                
        public synchronized void annotate(
                final ProcessExecutionModel pem) {
            
            if (pem == null) {
                clearAll();
                return;
            }
            
            final ProcessStaticModel psm = 
                    pem.getProcessStaticModel();
            final String url = 
                    mySourcePath.getSourcePath(psm.getProcessQName());
            
            if (url == null) {
                clearAll();
                return;
            }
            
            final BpelModel model = 
                    EditorUtil.getBpelModel(url);
            
            if (!url.equals(myAnnotatedUrl) || 
                    !psm.equals(myAnnotatedPsm) || 
                    !model.equals(myModel)) {
                clearAll();
                myAnnotatedPsm = psm;
                myAnnotatedUrl = url;
                myModel = model;
            }
            
            if (pem.getRoot() != null) {
                annotatePemEntity(pem.getRoot());
            } else {
                annotatePsmEntity(psm.getRoot());
            }
        }
        
        private void clearAll() {
            for (Object annotation : myAnnotations.values()) {
                if (annotation != null) {
                    EditorContextBridge.removeAnnotation(annotation);
                }
            }
            
            myAnnotations.clear();
            myAnnotationTypes.clear();
            myAnnotatedPsm = null;
            myAnnotatedUrl = null;
        }
        
        private void annotatePemEntity(
                final PemEntity pemEntity) {
            final PsmEntity psmEntity = pemEntity.getPsmEntity();
            final AnnotationType annotationType = 
                    annotationTypeByState(pemEntity.getState());
            
            updateAnnotation(psmEntity, annotationType);
            annotatePemEntityChildren(pemEntity);
        }
        
        private void annotatePemEntityChildren(
                final PemEntity pemEntity) {
            final PsmEntity psmEntity = pemEntity.getPsmEntity();
            
            if (!pemEntity.hasChildren()) {
                annotatePsmEntityChildren(psmEntity);
                return;
            }
            
            if (psmEntity.isLoop()) {
                int lastEventIndex = -1;
                
                PemEntity childToAnnotate = null;
                for (PemEntity child : pemEntity.getChildren()) {
                    if (child.getLastStartedEventIndex() > lastEventIndex) {
                        childToAnnotate = child;
                        lastEventIndex = child.getLastStartedEventIndex();
                    }
                }
                
                annotatePemEntity(childToAnnotate);
                return;
            }
            
            for (PsmEntity psmChild : psmEntity.getChildren()) {
                final PemEntity[] pemChildren = pemEntity.getChildren(psmChild);
                
                if (pemChildren.length > 0) {
                    annotatePemEntity(pemChildren[0]);
                } else {
                    annotatePsmEntity(psmChild);
                }
            }
        }
        
        private void annotatePsmEntity(
                final PsmEntity psmEntity) {
            updateAnnotation(psmEntity, AnnotationType.NEVER_EXECUTED_ELEMENT);
            annotatePsmEntityChildren(psmEntity);
        }
        
        private void annotatePsmEntityChildren(
                final PsmEntity psmEntity) {
            for (PsmEntity child : psmEntity.getChildren()) {
                annotatePsmEntity(child);
            }
        }
        
        private void updateAnnotation(
                final PsmEntity psmEntity, 
                final AnnotationType annotationType) {
            if (myAnnotationTypes.get(psmEntity) != annotationType) {
                Object annotation = myAnnotations.get(psmEntity);
                if (annotation != null) {
                    EditorContextBridge.removeAnnotation(annotation);
                }
                
                final int lineNumber = ModelUtil.getLineNumber(
                        myModel, psmEntity.getXpath());
                
                annotation = EditorContextBridge.addAnnotation(
                        myAnnotatedUrl,
                        psmEntity.getXpath(),
                        lineNumber,
                        annotationType);
                
                myAnnotations.put(psmEntity, annotation);
                myAnnotationTypes.put(psmEntity, annotationType);
            }
        }
        
        private AnnotationType annotationTypeByState(PemEntity.State state) {
            switch (state) {
                case STARTED :
                    return AnnotationType.STARTED_ELEMENT;
                case COMPLETED :
                    return AnnotationType.COMPLETED_ELEMENT;
//                case FAULTED :
//                    return AnnotationType.FAULTED_ELEMENT;
                case UNKNOWN :
                default :
                    return AnnotationType.STARTED_ELEMENT;
            }
        }
    }
}
