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

package org.netbeans.modules.bpel.debugger.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListenerAdapter;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Zgursky
 */
public class BpelAnnotationsObserver {
    private static HashMap<BpelModel, LinkedList<BpelAnnotation>> myModelAnnotations =
            new HashMap<BpelModel, LinkedList<BpelAnnotation>>();
    
    private static HashMap<BpelModel, Listener> myModelListeners =
            new HashMap<BpelModel, Listener>();
    
    private BpelAnnotationsObserver() {
    }
    
    public static void subscribe(BpelAnnotation annotation) {
        final BpelModel model = annotation.getBpelModel();
        
        if (model == null) {
            return;
        }
        
        synchronized(myModelAnnotations) {
            LinkedList<BpelAnnotation> annotations = 
                    myModelAnnotations.get(model);
            if (annotations == null) {
                annotations = new LinkedList<BpelAnnotation>();
                myModelAnnotations.put(model, annotations);
                subscribeToModel(model);
            }
            annotations.add(annotation);
        }
    }
    
    public static void unsubscribe(BpelAnnotation annotation) {
        final BpelModel model = annotation.getBpelModel();
        
        if (model == null) {
            return;
        }
        
        synchronized(myModelAnnotations) {
            LinkedList<BpelAnnotation> annotations = 
                    myModelAnnotations.get(model);
            if (annotations != null) {
                annotations.remove(annotation);
                if (annotations.size() == 0) {
                    unsubscribeFromModel(model);
                    myModelAnnotations.remove(model);
                }
            }
        }
    }
    
    public static BpelAnnotation[] getAnnotations(BpelModel model) {
        BpelAnnotation[] annotations = null;
        synchronized(myModelAnnotations) {
            LinkedList<BpelAnnotation> al = myModelAnnotations.get(model);
            if (al != null) {
                annotations = al.toArray(new BpelAnnotation[al.size()]);
            }
        }
        
        if (annotations != null) {
            return annotations;
        } else {
            return new BpelAnnotation[0];
        }
    }
    
    private static void subscribeToModel(BpelModel model) {
        Listener listener = new Listener(model);
        myModelListeners.put(model, listener);
        listener.subscribe();
    }
    
    private static void unsubscribeFromModel(BpelModel model) {
        Listener listener = myModelListeners.get(model);
        listener.unsubscribe();
        myModelListeners.remove(model);
    }
    
    private static class Listener
            extends ChangeEventListenerAdapter
            implements PropertyChangeListener
    {
        private BpelModel myModel;
        private RequestProcessor.Task myTask;
        
        public Listener(BpelModel model) {
            myModel = model;
        }
        
        public void subscribe() {
            myModel.addEntityChangeListener(this);
            myModel.addPropertyChangeListener(this);
        }
        
        public void unsubscribe() {
            myModel.removeEntityChangeListener(this);
            myModel.removePropertyChangeListener(this);
            if (myTask != null) {
                myTask.cancel();
                myTask = null;
            }
        }
        
        protected void notifyEvent(ChangeEvent event) {
            if(event.isLastInAtomic()) {
                notifyModelChanged();
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            notifyModelChanged();
        }
        
        private void notifyModelChanged() {
            if (myTask != null) {
                myTask.cancel();
                myTask = null;
            }
            
            myTask = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    BpelAnnotation[] annotations =
                            BpelAnnotationsObserver.getAnnotations(myModel);
                    for (BpelAnnotation annotation : annotations) {
                        annotation.update();
                    }
                }
            }, 1000);
        }
    }
}
