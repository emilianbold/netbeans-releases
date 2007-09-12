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
package org.netbeans.modules.bpel.core.annotations.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.core.annotations.AnnotationListener;
import org.netbeans.modules.bpel.core.annotations.AnnotationManagerCookie;
import org.netbeans.modules.bpel.core.annotations.DiagramAnnotation;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 *
 * @author Alexander Zgursky
 */
public class AnnotationManagerProvider implements AnnotationManagerCookie {
    public final static String ANNOTATIONS_COOKIE_KEY = "annotations";
    
    private BPELDataObject myDataObject;
    private LinkedList<AnnotationListener> myListeners =
            new LinkedList<AnnotationListener>();
    private HashMap<UniqueId, ArrayList<DiagramAnnotation>> myAnnotationById =
            new HashMap<UniqueId, ArrayList<DiagramAnnotation>>();
    
    /** Creates a new instance of AnnotationManagerProvider */
    public AnnotationManagerProvider(BPELDataObject dataObject) {
        myDataObject = dataObject;
    }

    public boolean addAnnotation(final DiagramAnnotation annotation) {
        BpelModel model =
                (BpelModel)myDataObject.getLookup().lookup(BpelModel.class);
        UniqueId bpelEntityId = annotation.getBpelEntityId();
        assert bpelEntityId.getModel() == model;
        
        synchronized (myAnnotationById) {
            ArrayList<DiagramAnnotation> annotations =
                    myAnnotationById.get(bpelEntityId);
            if (annotations == null) {
                annotations = new ArrayList<DiagramAnnotation>();
                myAnnotationById.put(bpelEntityId, annotations);
            } else if (annotations.contains(annotation)) {
                //the given annotation is already added
                return false;
            }
            annotations.add(annotation);
        }
        fireAnnotationAdded(annotation);
        return true;
        
//        final BpelModel model =
//                (BpelModel)myDataObject.getLookup().lookup(BpelModel.class);
//        final UniqueId bpelEntityId = annotation.getBpelEntityId();
//        
//        Callable<Object> addAnnotationTask = new Callable<Object>() {
//            public Object call() throws Exception {
//                BpelEntity bpelEntity = model.getEntity(bpelEntityId);
//                
//                if (bpelEntity == null) {
//                    throw new Exception("bpelEntity is null"); //NOI18N
//                }
//                
//                assert bpelEntity.getBpelModel() == model;
//                
//                ArrayList<DiagramAnnotation> annotations = 
//                        (ArrayList<DiagramAnnotation>)bpelEntity.
//                        getCookie(ANNOTATIONS_COOKIE_KEY);
//
//                if (annotations == null) {
//                    annotations = new ArrayList<DiagramAnnotation>();
//                    bpelEntity.setCookie(ANNOTATIONS_COOKIE_KEY, annotations);
//                } else if (annotations.contains(annotation)) {
//                    throw new Exception("annotation already added"); //NOI18N
//                }
//
//                annotations.add(annotation);
//                return null;
//            }
//        };
//        
//        try {
//            model.invoke(addAnnotationTask, null);
//            fireAnnotationAdded(annotation);
//            return true;
//        } catch (Exception ex) {
//            return false;
//        }
    }

    public boolean removeAnnotation(final DiagramAnnotation annotation) {
        BpelModel model =
                (BpelModel)myDataObject.getLookup().lookup(BpelModel.class);
        UniqueId bpelEntityId = annotation.getBpelEntityId();
        assert bpelEntityId.getModel() == model;
        
        synchronized (myAnnotationById) {
            ArrayList<DiagramAnnotation> annotations =
                    myAnnotationById.get(bpelEntityId);

            if (annotations != null) {
                if (annotations.remove(annotation)) {
                    if (annotations.size() == 0) {
                        myAnnotationById.remove(bpelEntityId);
                    }
                } else {
                    //the given annotation was not in the list
                    return false;
                }
            } else {
                //the list was already empty
                return false;
            }
        }
        
        fireAnnotationRemoved(annotation);
        return true;
        
//        final BpelModel model =
//                (BpelModel)myDataObject.getLookup().lookup(BpelModel.class);
//        final UniqueId bpelEntityId = annotation.getBpelEntityId();
//        
//        Callable<Object> removeAnnotationTask = new Callable<Object>() {
//            public Object call() throws Exception {
//                BpelEntity bpelEntity = model.getEntity(bpelEntityId);
//                
//                if (bpelEntity == null) {
//                    throw new Exception("bpelEntity is null"); //NOI18N
//                }
//                
//                assert bpelEntity.getBpelModel() == model;
//                    
//                ArrayList<DiagramAnnotation> annotations = 
//                        (ArrayList<DiagramAnnotation>)bpelEntity.
//                        getCookie(ANNOTATIONS_COOKIE_KEY);
//
//                if (annotations != null) {
//                    if (annotations.remove(annotation)) {
//                        if (annotations.size() == 0) {
//                            bpelEntity.removeCookie(ANNOTATIONS_COOKIE_KEY);
//                        }
//                        return null;
//                    }
//                }
//                throw new Exception("annotation not found");
//            }
//        };
//        
//        try {
//            model.invoke(removeAnnotationTask, null);
//            fireAnnotationRemoved(annotation);
//            return true;
//        } catch (Exception ex) {
//            return false;
//        }
    }

    public DiagramAnnotation[] getAnnotations(final UniqueId bpelEntityId) {
        BpelModel model =
                (BpelModel)myDataObject.getLookup().lookup(BpelModel.class);
        assert bpelEntityId.getModel() == model;
        
        synchronized (myAnnotationById) {
            ArrayList<DiagramAnnotation> annotations =
                    myAnnotationById.get(bpelEntityId);

            if (annotations != null) {
                return annotations.toArray(
                        new DiagramAnnotation[annotations.size()]);
            }
        }
        return new DiagramAnnotation[0];

//        final BpelModel model =
//                (BpelModel)myDataObject.getLookup().lookup(BpelModel.class);
//        
//        Callable<DiagramAnnotation[]> getAnnotationsTask =
//                new Callable<DiagramAnnotation[]>()
//        {
//            public DiagramAnnotation[] call() throws Exception {
//                BpelEntity bpelEntity = model.getEntity(bpelEntityId);
//                
//                assert bpelEntity.getBpelModel() == model;
//                
//                if (bpelEntity != null) {
//                    ArrayList<DiagramAnnotation> annotations = 
//                            (ArrayList<DiagramAnnotation>)bpelEntity.
//                            getCookie(ANNOTATIONS_COOKIE_KEY);
//
//                    if (annotations != null) {
//                        return annotations.toArray(
//                                new DiagramAnnotation[annotations.size()]);
//                    }
//                }
//                return new DiagramAnnotation[0];
//            }
//        };
//        
//        try {
//            return model.invoke(getAnnotationsTask, null);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return new DiagramAnnotation[0];
//        }
    }

    public void addAnnotationListener(AnnotationListener listener) {
        synchronized (myListeners) {
            myListeners.add(listener);
        }
    }

    public void removeAnnotationListener(AnnotationListener listener) {
        synchronized (myListeners) {
            myListeners.remove(listener);
        }
    }
    
    protected void fireAnnotationAdded(DiagramAnnotation annotation) {
        LinkedList<AnnotationListener> cloneListeners = null;
        synchronized(myListeners) {
            cloneListeners = (LinkedList<AnnotationListener>)myListeners.clone();
        }
        
        for (AnnotationListener listener : cloneListeners) {
            listener.annotationAdded(annotation);
        }
    }
    
    protected void fireAnnotationRemoved(DiagramAnnotation annotation) {
        LinkedList<AnnotationListener> cloneListeners = null;
        synchronized(myListeners) {
            cloneListeners = (LinkedList<AnnotationListener>)myListeners.clone();
        }
        
        for (AnnotationListener listener : cloneListeners) {
            listener.annotationRemoved(annotation);
        }
    }
}
