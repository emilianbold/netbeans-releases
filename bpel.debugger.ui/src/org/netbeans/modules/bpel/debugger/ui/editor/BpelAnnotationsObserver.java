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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.debugger.ui.editor;

import com.sun.corba.se.spi.ior.iiop.RequestPartitioningComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.LinkedList;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListenerAdapter;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
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
        BpelModel model = annotation.getBpelEntityId().getModel();
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
        BpelModel model = annotation.getBpelEntityId().getModel();
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
