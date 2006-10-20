/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.core.validation.SelectBpelElement;
import org.netbeans.modules.bpel.debugger.ui.breakpoint.BreakpointTranslator;
import org.netbeans.modules.bpel.debugger.ui.util.Log;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.debugger.ui.util.Util;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.spi.EditorContext;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.26
 */
public class BpelEditorContext implements EditorContext {
    
    private Map<BpelAnnotation, PropertyChangeSupport> myAnnotationToListeners =
            new IdentityHashMap<BpelAnnotation, PropertyChangeSupport>();
    
    private Map<EditorCookie, EditorObserver> myEditorToObserver =
            new HashMap<EditorCookie, EditorObserver>();
    
    private Map<EditorCookie, Set<BpelAnnotation>> myEditorToAnnotations =
            new HashMap<EditorCookie, Set<BpelAnnotation>>();
    
    private EditorObserverListener myEditorObserverListener =
            new EditorObserverListener();
    
    private BreakpointTranslator myTranslator;

//    private PropertyChangeSupport myPcs =
//            new PropertyChangeSupport(this);
    
    /**{@inheritDoc}*/
    public Object annotate(
            String url,
            String xpath,
            AnnotationType annotationType)
    {
        DataObject dataObject = Util.getDataObject(url);
        if (dataObject == null) {
            Log.out("DataObject is null: " + url);
            return null;
        }
        
        BpelModel model = Util.getBpelModel(dataObject);
        if (model == null) {
            Log.out("BpelModel is null: " + url);
            return null;
        }
        
        UniqueId bpelEntityId = ModelUtil.getBpelEntityId(model, xpath);
        if (bpelEntityId == null) {
            Log.out("BPEL Entity not found: " + dataObject + ", " + xpath);
            return null;
        }
        
        BpelAnnotation annotation = new BpelAnnotation(annotationType, bpelEntityId);
        if (annotation.attach()) {
            return annotation;
        } else {
            return null;
        }
    }
    
    /**{@inheritDoc}*/
    public void removeAnnotation(Object annotation) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation) annotation;
        bpelAnnotation.detach();
    }
    
    public String getXpath(Object annotation) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation)annotation;
        UniqueId bpelEntityId = bpelAnnotation.getBpelEntityId();
        return ModelUtil.getXpath(bpelEntityId);
    }
    
    public QName getProcessQName(String url) {
        BpelModel model = Util.getBpelModel(url);
        if (model == null) {
            Log.out("BpelModel is null: " + url);
            return null;
        }
        
        Process proc = model.getProcess();
        if (proc == null) {
            Log.out("Process is null: " + url);
            return null;
        }
        
        if (proc.getName() == null) {
            Log.out("Process name is null: " + url);
            return null;
        }
        
        return new QName(proc.getTargetNamespace(), proc.getName());
    }
    
    /**{@inheritDoc}*/
    public boolean showSource(final String url, final String xpath) {
        DataObject dataObject = Util.getDataObject(url);
        if (dataObject == null) {
            Log.out("DataObject is null: " + url);
            return false;
        }

        BpelModel model = Util.getBpelModel(dataObject);
        if (model == null) {
            Log.out("BpelModel is null: " + url);
            return false;
        }
        
        UniqueId bpelEntityId = ModelUtil.getBpelEntityId(model, xpath);
        if (bpelEntityId == null) {
            Log.out("BPEL Entity not found: " + dataObject + ", " + xpath);
            return false;
        }
        
        showBpelEntity(bpelEntityId);
        
//        int lineNumber = ModelUtil.getLineNumber(bpelEntityId, true);
//        if (lineNumber < 0) {
//            Log.out("Failed to get line number for UniqueId: " + bpelEntityId);
//            return false;
//        }
//        final Line line = Util.getLine(dataObject, lineNumber);
//        
//        if (line == null) {
//            return false;
//        }
//        javax.swing.SwingUtilities.invokeLater (new Runnable () {
//            public void run () {
////                TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
////
////                MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
////                if (mvh == null) {
////                    return;
////                }
////
////                String tabId = "orch-designer";
////                MultiViewPerspective[] mvps = mvh.getPerspectives();
////                if (mvps != null && mvps.length >0) {
////                    for (MultiViewPerspective mvp : mvps) {
////                        if (mvp.preferredID().equals(tabId)) {
////                            mvh.requestVisible(mvp);
////                            mvh.requestActive(mvp);
////                        }
////                    }
////                }
////                Line line = Util.getLine(url, position.getLineNumber());
////
////                if (line == null) {
////                    return;
////                }
////                //TODO: see the openActiveSourceEditor method's todo
////                //"bpelsource"
//                line.show (Line.SHOW_GOTO);
//                openActiveEditorTab("bpelsource");
//            }
//        });
        return true;
    }

    public int translateBreakpointLine(String url, int lineNumber) {
        return getBreakpointTranslator().translateBreakpointLine(url, lineNumber);
    }
    
    public synchronized void addAnnotationListener(Object annotation, PropertyChangeListener l) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation)annotation;
        bpelAnnotation.addPropertyChangeListener(l);
//        BpelAnnotation bpelAnnotation = (BpelAnnotation)annotation;
//        DataObject dataObject = (DataObject)bpelAnnotation.getBpelEntityId().
//                getModel().getModelSource().getLookup().lookup(DataObject.class);
//        
//        if (dataObject == null) {
//            return;
//        }
//        
//        EditorCookie editor = (EditorCookie)dataObject.
//                getCookie(EditorCookie.class);
//        
//        if (editor == null || !(editor instanceof EditorCookie.Observable)) {
//            return;
//        }
//        
//                    
//        PropertyChangeSupport pcs = myAnnotationToListeners.get(bpelAnnotation);
//        if (pcs == null) {
//            pcs = new PropertyChangeSupport(bpelAnnotation);
//            myAnnotationToListeners.put(bpelAnnotation, pcs);
//        }
//        pcs.addPropertyChangeListener(l);
//        
//        
//        Set<BpelAnnotation> editorAnnotations = myEditorToAnnotations.get(editor);
//        if (editorAnnotations == null) {
//            editorAnnotations = new HashSet<BpelAnnotation>();
//            myEditorToAnnotations.put(editor, editorAnnotations);
//        }
//        editorAnnotations.add(bpelAnnotation);
//        
//        
//        EditorObserver observer = myEditorToObserver.get(editor);
//        if (observer == null) {
//            observer = new EditorObserver((EditorCookie.Observable)editor);
//            myEditorToObserver.put(editor, observer);
//            observer.subscribe(myEditorObserverListener);
//        }
    }

    public synchronized void removeAnnotationListener(Object annotation, PropertyChangeListener l) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation)annotation;
        bpelAnnotation.removePropertyChangeListener(l);
//        BpelAnnotation bpelAnnotation = (BpelAnnotation)annotation;
//        DataObject dataObject = (DataObject)bpelAnnotation.getBpelEntityId().
//                getModel().getModelSource().getLookup().lookup(DataObject.class);
//        
//        if (dataObject == null) {
//            return;
//        }
//        
//        EditorCookie editor = (EditorCookie)dataObject.
//                getCookie(EditorCookie.class);
//        
//        if (editor == null || !(editor instanceof EditorCookie.Observable)) {
//            return;
//        }
//        
//        
//        PropertyChangeSupport pcs = myAnnotationToListeners.get(bpelAnnotation);
//        if (pcs != null) {
//            pcs.removePropertyChangeListener(l);
//            if (!pcs.hasListeners(null)) {
//                myAnnotationToListeners.remove(bpelAnnotation);
//            }
//        }
//        
//        Set<BpelAnnotation> editorAnnotations = myEditorToAnnotations.get(editor);
//        if (editorAnnotations != null) {
//            editorAnnotations.remove(bpelAnnotation);
//            if (editorAnnotations.isEmpty()) {
//                myEditorToAnnotations.remove(editor);
//                
//                EditorObserver observer = myEditorToObserver.get(editor);
//                if (observer != null) {
//                    observer.unsubscribe();
//                    myEditorToObserver.remove(editor);
//                }
//            }
//        }
    }
    
    private void notifyAnnotationChanged(EditorCookie editor) {
        PropertyChangeSupport[] changeSupports = null;
        
        synchronized (this) {
            Set<BpelAnnotation> annotations = myEditorToAnnotations.get(editor);
            if (annotations == null) {
                return;
            }

            changeSupports = new PropertyChangeSupport[annotations.size()];

            int i = 0;
            for (BpelAnnotation annotation : annotations) {
                PropertyChangeSupport pcs = myAnnotationToListeners.get(annotation);
                if (pcs != null) {
                    changeSupports[i++] = pcs;
                }
            }
        }
        
        for (PropertyChangeSupport pcs : changeSupports) {
            pcs.firePropertyChange(null, null, null);
        }

    }
    
    private BreakpointTranslator getBreakpointTranslator() {
        if (myTranslator == null) {
            myTranslator = new BreakpointTranslator();
        }
        return myTranslator;
    }
    
    private void showBpelEntity(final UniqueId bpelEntityId) {
        BpelModel model = bpelEntityId.getModel();
        final BpelEntity bpelEntity = model.getEntity(bpelEntityId);
        
        final DataObject d = (DataObject)model.getModelSource().getLookup().lookup(DataObject.class);
        final LineCookie lc = (LineCookie) d.getCookie(LineCookie.class);
        final EditCookie ec = (EditCookie) d.getCookie(EditCookie.class);
        if (lc == null || ec == null) {
            return;
        }
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                ec.edit();
                
                TopComponent tc = WindowManager.getDefault().getRegistry()
                .getActivated();
                MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
                
                if (mvh == null) {
                    return;
                }
                
                MultiViewPerspective mvp = mvh.getSelectedPerspective();
                if (mvp.preferredID().equals("orch-designer")) {
                    List<TopComponent> list = getAssociatedTopComponents(d);
                    for (TopComponent topComponent : list) {
                        // Make sure this is a multiview window, and not just
                        // some
                        // window that has our DataObject (e.g. Projects,Files).
                        MultiViewHandler handler = MultiViews
                                .findMultiViewHandler(topComponent);
                        if (handler != null && topComponent != null) {
                            SelectBpelElement selectElement =
                                    (SelectBpelElement) topComponent.getLookup()
                                    .lookup(SelectBpelElement.class);
                            if (selectElement == null)
                                return;
                            selectElement.select(bpelEntity);
                        }
                    }
                } else if (mvp.preferredID().equals("bpelsource")) {
                    int lineNum = ModelUtil.getLineNumber(bpelEntityId);
                    if (lineNum < 0) {
                        return;
                    }
                    Line l = lc.getLineSet().getCurrent(lineNum - 1);
                    l.show(Line.SHOW_GOTO);
                    
                }
            }
        });
    }
    
    private List<TopComponent> getAssociatedTopComponents(DataObject targetDO) {
        // Create a list of TopComponents associated with the
        // editor's schema data object, starting with the the
        // active TopComponent. Add all open TopComponents in
        // any mode that are associated with the DataObject.
        // [Note that EDITOR_MODE does not contain editors in
        // split mode.]
        List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (activeTC != null
                && targetDO == (DataObject) activeTC.getLookup().lookup(
                DataObject.class)) {
            associatedTCs.add(activeTC);
        }
        Set openTCs = TopComponent.getRegistry().getOpened();
        for (Object tc : openTCs) {
            TopComponent tcc = (TopComponent) tc;
            if (targetDO == (DataObject) tcc.getLookup().lookup(
                    DataObject.class)) {
                associatedTCs.add(tcc);
            }
        }
        return associatedTCs;
    }
    
    //TODO:check it out
    //Maybe it's not a good way to ensure that the source editor pane
    //is opened when Line.show() is inovked. It seems that it is the
    //responsibility of some of the BPELDataObject's cookie implementations
    //(i.e. EditorCookie or LineCookie)
    private static void openActiveEditorTab(String tabId) {
//        EditorCookie editorCookie;
//        editorCookie.getOpenedPanes()
        
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
        if (mvh == null) {
            return;
        }

        MultiViewPerspective[] mvps = mvh.getPerspectives();
        if (mvps != null && mvps.length >0) {
            for (MultiViewPerspective mvp : mvps) {
                if (mvp.preferredID().equals(tabId)) {
                    mvh.requestVisible(mvp);
                    mvh.requestActive(mvp);
                }
            }
        }
    }
    
    private class EditorObserverListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            notifyAnnotationChanged((EditorCookie)evt.getSource());
        }
    }
}
