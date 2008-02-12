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
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;

import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListenerAdapter;
import org.netbeans.modules.bpel.model.api.support.Util;

public class BPELValidationController extends ChangeEventListenerAdapter {
    
    public BPELValidationController(BpelModel bpelModel) {
        myWeaklisteners = new WeakHashMap<BPELValidationListener, Object>();
        myBpelModel = bpelModel;
        myTrigger = new ExternalModelsValidationTrigger( this );
        myAnnotations = new ArrayList<BPELValidationAnnotation>();
    }

    public void attach() {
        if (myBpelModel != null) {
            myBpelModel.addEntityChangeListener(this);
            myBpelModel.addEntityChangeListener(getTrigger());
            getTrigger().loadImports();
        }
    }

    public void detach() {
        if (myBpelModel != null) {
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
        triggerValidation(false);
    }
    
    public void triggerValidation(boolean checkExternallyTriggered) {
        if (checkExternallyTriggered && getTrigger().isTriggerDirty()) {
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
                    List<ResultItem> items = validation.getValidationResult();
                    List<ResultItem> result = new ArrayList<ResultItem>();

                    synchronized(items) {
                      for (ResultItem item : items) {
                        result.add(item);
                      }
                    }
                    notifyListeners(result);
                }
            };
            myTimer.cancel();
            myTimer = new Timer();
            myTimer.schedule(timerTask, DELAY);
        }
    }

    private void notifyListeners(List<ResultItem> result) {
        synchronized (myWeaklisteners) {
            for (BPELValidationListener listener : myWeaklisteners.keySet()) {
                if (listener != null) {
                    listener.validationUpdated(result);
                }
            }
        }
        showAnnotationsInEditor(result);
    }
    
    private void showAnnotationsInEditor(List<ResultItem> result) {
      for (BPELValidationAnnotation annotation : myAnnotations) {
        annotation.detach();
      }
      myAnnotations.clear();
//System.out.println();
//System.out.println("SHOW ANNOTATION IN EDITOR");

      for (ResultItem item : result) {
        if (item.getType() != ResultType.ERROR) {
          continue;
        }
        Line line = Util.getLine(item);
//System.out.println("  see line: " + line);

        if (line == null) {
          continue;
        }
        BPELValidationAnnotation annotation = new BPELValidationAnnotation();
        myAnnotations.add(annotation);
        annotation.show(line, item.getDescription());
      }
    }

    private ExternalModelsValidationTrigger getTrigger() {
      return myTrigger;
    }
    
    private BpelModel myBpelModel;
    private Object lock = new Object();
    private Timer myTimer = new Timer();
    private List<ResultItem> myValidationResult;
    private ExternalModelsValidationTrigger myTrigger;
    private List<BPELValidationAnnotation> myAnnotations;
    private Map<BPELValidationListener, Object> myWeaklisteners;

    private static final int DELAY = 3456;
}
