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

import org.openide.text.Line;
import org.netbeans.modules.xml.xam.Model.State;
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
        if(myBpelModel != null) {
            myBpelModel.addEntityChangeListener(this);
            myBpelModel.addEntityChangeListener( getTrigger() );
            getTrigger().loadImports( );
        }
    }

    public void detach() {
        if(myBpelModel != null) {
            myBpelModel.removeEntityChangeListener(this);
            myBpelModel.removeEntityChangeListener( getTrigger() );
            getTrigger().clearTrigger();
        }
    }

    
    /**
     *  Add a validation listener.
     *  Listeners are maintained as weaklisteners to clients should maintain
     *  a strong reference to the listener.
     *
     */
    public void addValidationListener(BPELValidationListener listener) {
        synchronized(myWeaklisteners) {
            myWeaklisteners.put(listener, null);
        }
    }
    
    /**
     * Remove a validation listener. Although listners are maintained as
     * weak listeners, clients can explicity unregister a listener.
     */
    public void removeValidationListener(BPELValidationListener listener) {
        synchronized(myWeaklisteners) {
            myWeaklisteners.remove(listener);
        }
    }
    
    BpelModel getModel() {
        return myBpelModel;
    }
    
    /**
     *  Return current validation results.
     */
    private List<ResultItem> getFastValidationResults() {
        return myLatestFastValidationResult;
    }
    
    /**
     *  Return current validation results.
     */
    private List<ResultItem> getSlowValidationResults() {
        return myLatestSlowValidationResult;
    }
    
    /**
     *  Called when the model has changed.
     */
    private void modelChanged(ChangeEvent event) {
        // Validate on the last event in a chain.
        if(!event.isLastInAtomic()) {
            return;
        }
        startValidation();
    }
    
    /**
     *  Use this for clients who want to initiate validation.
     *  For example when initially opening the editor. Make sure you
     *  attach a listener first and then call this method, so that you are
     *  notified with the validation results.
     */
    public void triggerValidation() {
        triggerValidation( false );
    }
    
    /**
     * Initiate validation.
     * 
     * @param checkExternallyTriggered if true then validation will be 
     * started only in the case external artifact was modifed 
     */
    public void triggerValidation( boolean checkExternallyTriggered ) {
        if ( checkExternallyTriggered && getTrigger().isTriggerDirty()) {
            startValidation();
        }
        else if ( !checkExternallyTriggered) {
            startValidation();
        }
    }
    
    public void notifyCompleteValidationResults(List<ResultItem> results) {
        //System.out.println(" Complete validation results obtained.");
        // Filter to keep only slow validator results here.
        myLatestSlowValidationResult = new ArrayList<ResultItem>();
        myLatestFastValidationResult = new ArrayList<ResultItem>();
        
        for (ResultItem result: results){
            ( ValidationUtil.isSlowValidationResult(result)?
                myLatestSlowValidationResult :
                myLatestFastValidationResult).add(result);
            
        }
        notifyListeners();
    }
    
    /**
     *  Listener to listen to Object model changes.
     */
    @Override
    public void notifyEvent(ChangeEvent changeEvent) {
        if ( !State.VALID.equals(myBpelModel.getState())){
            return;
        }
        modelChanged(changeEvent);
    }
    
    public void startValidation() {
        synchronized(lock) {
            final TimerTask timerTask= new TimerTask() {
                public void run() {

                    // Run at a low priority.
                    Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

                    Validation validation = new Validation();
                    validation.validate(myBpelModel, ValidationType.PARTIAL);
                    myLatestFastValidationResult = validation
                            .getValidationResult();

                    notifyListeners();
                }
            };
            myTimer.cancel(); // This removes any tasks on the queue while
                            // allowing any already running task to complete.
            
            myTimer = new Timer();
            myTimer.schedule(timerTask, DELAY);
        }
    }
    
    /**
     *  Listeners are notified about change in ValidationResult.
     *  Happens on a non-AWT thread.
     */
    private void notifyListeners() {
        List<ResultItem> mergedResults = getResultItems();

        synchronized(myWeaklisteners) {
            for(BPELValidationListener listener: myWeaklisteners.keySet()) {
                if(listener != null)
                    listener.validationUpdated(mergedResults);
            }
        }
        showAnnotationsInEditor(mergedResults);
    }
    
    public List<ResultItem> getResultItems() {
//System.out.println("RESULT ITEMS");
      List<ResultItem> mergedResults = new ArrayList<ResultItem>();

      if (getFastValidationResults() != null) {
        mergedResults.addAll(getFastValidationResults());
      }
      if (getSlowValidationResults() != null) {
        mergedResults.addAll(getSlowValidationResults());
      }
      return mergedResults;
    }

    private void showAnnotationsInEditor(List<ResultItem> items) {
      for (ValidationAnnotation annotation : myAnnotations) {
        annotation.detach();
      }
      myAnnotations.clear();
//System.out.println();
//System.out.println("SHOW ANNOTATION IN EDITOR");

      for (ResultItem item : items) {
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
    
    private BpelModel myBpelModel;
    private ExternalModelsValidationTrigger myTrigger;
    private Map<BPELValidationListener, Object> myWeaklisteners;
    private Object lock = new Object();
    private Timer myTimer = new Timer();
    
    private List<ResultItem> myLatestSlowValidationResult;
    private List<ResultItem> myLatestFastValidationResult;
    private List<ValidationAnnotation> myAnnotations;
    private static final int DELAY = 4000;
}
