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
package org.netbeans.modules.bpel.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.validation.ui.ValidationAnnotation;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListenerAdapter;
import org.netbeans.modules.bpel.model.api.support.Util;

public class BPELValidationController extends ChangeEventListenerAdapter {
    
    public BPELValidationController(BpelModel bpelModel) {
        myWeaklisteners = new WeakHashMap<BPELValidationListener, Object>();
        myBpelModel = bpelModel;
        myTrigger = new ExternalModelsValidationTrigger( this );
        myAnnotations = new ArrayList<ValidationAnnotation>();
    }

    public void attach() {
        if (myBpelModel != null) {
            myBpelModel.addEntityChangeListener(this);
            myBpelModel.addEntityChangeListener(getTrigger());
            getTrigger().loadImports();
        }
    }

    public void detach() {
        if(myBpelModel != null) {
            myBpelModel.removeEntityChangeListener(this);
            myBpelModel.removeEntityChangeListener(getTrigger());
            getTrigger().clearTrigger();
        }
    }

    public void addValidationListener(BPELValidationListener listener) {
        synchronized(myWeaklisteners) {
            myWeaklisteners.put(listener, null);
        }
    }
    
    public void removeValidationListener(BPELValidationListener listener) {
        synchronized(myWeaklisteners) {
            myWeaklisteners.remove(listener);
        }
    }
    
    BpelModel getModel() {
        return myBpelModel;
    }
    
    private void modelChanged(ChangeEvent event) {
        if (event.isLastInAtomic()) {
          startValidation();
        }
    }
    
    public void triggerValidation() {
        triggerValidation( false );
    }
    
    public void triggerValidation( boolean checkExternallyTriggered ) {
        if ( checkExternallyTriggered && getTrigger().isTriggerDirty()) {
            startValidation();
        }
        else if ( !checkExternallyTriggered) {
            startValidation();
        }
    }
    
    @Override
    public void notifyEvent(ChangeEvent changeEvent) {
        if ( !State.VALID.equals(myBpelModel.getState())){
            return;
        }
        modelChanged(changeEvent);
    }

    public void notifyCompleteValidationResults(List<ResultItem> result) {
        notifyListeners(result);
    }
    
    private void startValidation() {
        synchronized(lock) {
            final TimerTask timerTask= new TimerTask() {
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//System.out.println();
//System.out.println("---- VALIDATION");
//System.out.println();
                    Validation validation = new Validation();
                    validation.validate(myBpelModel, ValidationType.PARTIAL);
                    notifyListeners(validation.getValidationResult());
                }
            };
            myTimer.cancel();
            myTimer = new Timer();
            myTimer.schedule(timerTask, DELAY);
        }
    }

    private void notifyListeners(List<ResultItem> result) {
        synchronized (myWeaklisteners) {
            for (BPELValidationListener listener: myWeaklisteners.keySet()) {
                if (listener != null) {
                    listener.validationUpdated(result);
                }
            }
        }
        showAnnotationsInEditor(result);
    }
    
    private void showAnnotationsInEditor(List<ResultItem> result) {
      for (ValidationAnnotation annotation : myAnnotations) {
        annotation.detach();
      }
      myAnnotations.clear();
//System.out.println();
//System.out.println("SHOW ANNOTATION IN EDITOR");

      for (ResultItem item : result) {
        Line line = Util.getLine(item);
//System.out.println("  see line: " + line);

        if (line == null) {
          continue;
        }
        ValidationAnnotation annotation = new ValidationAnnotation();
        myAnnotations.add(annotation);
        annotation.show(line, item.getDescription());
      }
    }

    private ExternalModelsValidationTrigger getTrigger() {
      return myTrigger;
    }
    
    private Object lock = new Object();
    private Timer myTimer = new Timer();
    private BpelModel myBpelModel;
    private ExternalModelsValidationTrigger myTrigger;
    private Map<BPELValidationListener, Object> myWeaklisteners;
    private List<ResultItem> myValidationResult;
    private List<ValidationAnnotation> myAnnotations;

    private static final int DELAY = 3456;
}
