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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.xml.namespace.QName;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.core.SelectBpelElement;
import org.netbeans.modules.bpel.debugger.ui.breakpoint.BreakpointTranslator;
import org.netbeans.modules.bpel.debugger.ui.util.Log;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
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
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
 * @author Kirill Sorokin
 */
public class BpelEditorContext implements EditorContext {
    
    private Map<BpelAnnotation, PropertyChangeSupport> myAnnotationToListeners =
            new IdentityHashMap<BpelAnnotation, PropertyChangeSupport>();
    
    private Map<EditorCookie, Set<BpelAnnotation>> myEditorToAnnotations =
            new HashMap<EditorCookie, Set<BpelAnnotation>>();
    
    private BreakpointTranslator myTranslator;
    
    /**{@inheritDoc}*/
    public Object addAnnotation(
            final String url,
            final String xpath,
            final int lineNumber,
            final AnnotationType annotationType) {
        
        final DataObject dataObject = EditorUtil.getDataObject(url);
        if (dataObject == null) {
            Log.out("DataObject is null: " + url);
            return null;
        }
        
        final BpelModel model = EditorUtil.getBpelModel(dataObject);
        if (model == null) {
            Log.out("BpelModel is null: " + url);
            return null;
        }
        
        final BpelAnnotation annotation = new BpelAnnotation(
                annotationType, 
                dataObject,
                xpath, 
                lineNumber);
        
        if (annotation.attach()) {
            return annotation;
        } else {
            return null;
        }
    }
    
    /**{@inheritDoc}*/
    public void removeAnnotation(Object annotation) {
        final BpelAnnotation bpelAnnotation = (BpelAnnotation) annotation;
        bpelAnnotation.detach();
    }
    
    public boolean isAttached(Object annotation) {
        final BpelAnnotation bpelAnnotation = (BpelAnnotation) annotation;
        
        return bpelAnnotation.getState() == BpelAnnotation.State.ATTACHED;
    }
    
    public boolean isValid(Object annotation) {
        final BpelAnnotation bpelAnnotation = (BpelAnnotation) annotation;
        
        if (bpelAnnotation.getType().isForDiagram()) {
            if (bpelAnnotation.getBpelModel().getEntity(
                    bpelAnnotation.getBpelEntityId()) == null) {
                return false;
            }
        }
        
        return true;
    }
    
    public AnnotationType getAnnotationType(Object annotation) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation) annotation;
        return bpelAnnotation.getType();
    }
    
    public String getXpath(Object annotation) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation) annotation;
        return bpelAnnotation.getXpath();
    }
    
    public int getLineNumber(Object annotation) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation)annotation;
        
        return bpelAnnotation.getLineNumber();
    }
    
    public QName getProcessQName(String url) {
        BpelModel model = EditorUtil.getBpelModel(url);
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
    
    public QName getCurrentProcessQName() {
        final Node[] nodes = 
                WindowManager.getDefault().getRegistry().getActivatedNodes();
        if ((nodes == null) || (nodes.length == 0)) {
            return null;
        }
        
        final BpelModel model = nodes[0].getLookup().lookup(BpelModel.class);
        if (model == null) {
            return null;
        }
        
        try {
            return new QName(
                    model.getProcess().getTargetNamespace(),
                    model.getProcess().getName());
        } catch (Exception ex) {
            //TODO:no good, no good... 
            return null;
        }
    }
    
    /**{@inheritDoc}*/
    public boolean showSource(
            final String url, 
            final String xpath,
            final String view) {
        final DataObject dataObject = EditorUtil.getDataObject(url);
        if (dataObject == null) {
            Log.out("DataObject is null: " + url);
            return false;
        }
        
        final BpelModel model = EditorUtil.getBpelModel(dataObject);
        if (model == null) {
            Log.out("BpelModel is null: " + url);
            return false;
        }
        
        final UniqueId bpelEntityId = ModelUtil.getBpelEntityId(model, xpath);
        if (bpelEntityId == null) {
            Log.out("BPEL Entity not found: " + dataObject + ", " + xpath);
            return false;
        }
        
        showBpelEntity(bpelEntityId, view);
        
        return true;
    }

    public int translateBreakpointLine(
            final String url, 
            final int lineNumber) {
        return getBreakpointTranslator().translateBreakpointLine(
                url, lineNumber);
    }
    
    public synchronized void addAnnotationListener(
            final Object annotation, 
            final PropertyChangeListener l) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation)annotation;
        bpelAnnotation.addPropertyChangeListener(l);
    }

    public synchronized void removeAnnotationListener(
            final Object annotation, 
            final PropertyChangeListener l) {
        BpelAnnotation bpelAnnotation = (BpelAnnotation)annotation;
        bpelAnnotation.removePropertyChangeListener(l);
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private BreakpointTranslator getBreakpointTranslator() {
        if (myTranslator == null) {
            myTranslator = new BreakpointTranslator();
        }
        return myTranslator;
    }
    
    private void showBpelEntity(
            final UniqueId bpelEntityId, 
            final String view) {
        
        final BpelModel model = bpelEntityId.getModel();
        final BpelEntity bpelEntity = model.getEntity(bpelEntityId);
        
        final DataObject dataObject = 
                model.getModelSource().getLookup().lookup(DataObject.class);
        final LineCookie lineCookie = 
                dataObject.getCookie(LineCookie.class);
        final EditCookie editorCookie = 
                dataObject.getCookie(EditCookie.class);
        if ((lineCookie == null) || (editorCookie == null)) {
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            
            public void run() {
                editorCookie.edit();
                
                final TopComponent tc = 
                        WindowManager.getDefault().getRegistry().getActivated();
                final MultiViewHandler mvh = 
                        MultiViews.findMultiViewHandler(tc);
                
                if (mvh == null) {
                    return;
                }
                
                MultiViewPerspective mvp = mvh.getSelectedPerspective();
                
                if (view != null) {
                    for (MultiViewPerspective temp: mvh.getPerspectives()) {
                        if (temp.preferredID().equals(view)) {
                            mvp = temp;
                            break;
                        }
                    }
                    
                    mvh.requestVisible(mvp);
                    mvh.requestActive(mvp);
                } else {
                    final String currentId = 
                            mvh.getSelectedPerspective().preferredID();
                    
                    if (!currentId.equals("orch-designer") && 
                            !currentId.equals("bpelsource")) {
                        
                        for (MultiViewPerspective temp: mvh.getPerspectives()) {
                            if (temp.preferredID().equals("orch-designer")) {
                                mvp = temp;
                                break;
                            }
                        }
                        
                        mvh.requestVisible(mvp);
                        mvh.requestActive(mvp);
                    }
                }
                
                if (mvp.preferredID().equals("orch-designer")) {
                    final List<TopComponent> list = 
                            getAssociatedTopComponents(dataObject);
                    
                    for (TopComponent topComponent : list) {
                        // Make sure this is a multiview window, and not just
                        // some window that has our DataObject 
                        // (e.g. Projects, Files).
                        final MultiViewHandler handler = MultiViews
                                .findMultiViewHandler(topComponent);
                        
                        if (handler != null && topComponent != null) {
                            final SelectBpelElement selectElement = 
                                    topComponent.getLookup().lookup(
                                    SelectBpelElement.class);
                            
                            if (selectElement == null) {
                                return;
                            }
                            
                            selectElement.select(bpelEntity);
                        }
                    }
                } else if (mvp.preferredID().equals("bpelsource")) {
                    int lineNumber = ModelUtil.getLineNumber(bpelEntityId);
                    if (lineNumber < 0) {
                        return;
                    }
                    
                    final Line line = 
                            lineCookie.getLineSet().getCurrent(lineNumber - 1);
                    line.show(Line.SHOW_GOTO);
                }
            }
        });
    }
    
    private List<TopComponent> getAssociatedTopComponents(DataObject targetDO) {
        // Create a list of TopComponents associated with the
        // editor's schema data object, starting with the
        // active TopComponent. Add all open TopComponents in
        // any mode that are associated with the DataObject.
        // [Note that EDITOR_MODE does not contain editors in
        // split mode.]
        final List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
        final TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if ((activeTC != null) && (
                targetDO == activeTC.getLookup().lookup(DataObject.class))) {
            associatedTCs.add(activeTC);
        }
        
        final Set openTCs = TopComponent.getRegistry().getOpened();
        for (Object tc : openTCs) {
            final TopComponent tcc = (TopComponent) tc;
            
            if (targetDO == tcc.getLookup().lookup(DataObject.class)) {
                associatedTCs.add(tcc);
            }
        }
        
        return associatedTCs;
    }
}
