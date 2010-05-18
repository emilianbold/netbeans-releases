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

package org.netbeans.modules.bpel.debugger.ui.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Original code is taken from EditorContextImpl from jpda debugger.
 */
public class RunToCursorUtil {
    
    private Object currentLock = new Object();
    private EditorCookie currentEditorCookie = null;
    private PropertyChangeListener  editorObservableListener;
    //TODO:ugly hack
    public PropertyChangeSupport   pcs;
    
    private String currentURL = null;
    private Lookup.Result resDataObject;
    
    {
        pcs = new PropertyChangeSupport (this);
        
        resDataObject = Utilities.actionsGlobalContext().lookup(new Lookup.Template(DataObject.class));
        resDataObject.addLookupListener(new EditorLookupListener(DataObject.class));
    }
    
    
    private EditorCookie getCurrentEditorCookie () {
        synchronized (currentLock) {
            if (currentEditorCookie == null) {
                TopComponent tc = TopComponent.getRegistry().getActivated();
                if (tc != null) {
                    currentEditorCookie = (EditorCookie) tc.getLookup().lookup(EditorCookie.class);
                }
                // Listen on open panes if currentEditorCookie implements EditorCookie.Observable
                if (currentEditorCookie instanceof EditorCookie.Observable) {
                    if (editorObservableListener == null) {
                        editorObservableListener = new EditorLookupListener(EditorCookie.Observable.class);
                    }
                    ((EditorCookie.Observable) currentEditorCookie).addPropertyChangeListener(editorObservableListener);
                }
            }
            return currentEditorCookie;
        }
    }
    
    private JEditorPane getCurrentEditor () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane[] op = e.getOpenedPanes ();
        // We listen on open panes if e implements EditorCookie.Observable
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
    
    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public LineBreakpoint createBreakpointAtCursor() {
        Node node = getCurrentNode();
        if (node == null) {
            return null;
        }
        
        DataObject dataObject = getDataObject(node);
        if (dataObject == null) {
            return null;
        }
        
        String url = FileUtil.toFile(dataObject.getPrimaryFile()).getPath();
        if (!url.endsWith(".bpel")) {
            return null;
        }
        //TODO:consider using FileUtil.normalizeFile()
        url = url.replace("\\", "/"); // NOI18N
        
        UniqueId bpelEntityId = null;
        int lineNumber = -1;
        if ((node instanceof InstanceRef) && !isInSourceEditor()) {
            Object modelReference = ((InstanceRef)node).getReference();
            
            if (modelReference == null) {
                return null;
            }

            if (!(modelReference instanceof Activity)) {
                return null;
            }
            
            Activity activity = (Activity)modelReference;
            bpelEntityId = activity.getUID();
            lineNumber = ModelUtil.getLineNumber(bpelEntityId);
        } else {
            lineNumber = getCurrentLineNumber(node);
            if (lineNumber > 0) {
                int translatedLineNumber =
                        EditorContextBridge.translateBreakpointLine(url, lineNumber);
                
                //TODO:optimize it - we have already obtained document in the getCurrentLineNumber
                StyledDocument doc = EditorUtil.getDocument(dataObject);
                BpelModel model = EditorUtil.getBpelModel(dataObject);
                if (doc != null && model != null && translatedLineNumber > 0) {
                    int offset = EditorUtil.findOffset(doc, translatedLineNumber);
                    bpelEntityId = ModelUtil.getBpelEntityId(model, offset);
                }
                
                lineNumber = translatedLineNumber;
            }
            
        }
        
        if (lineNumber == -1) {
            return null;
        }
        
        String xpath = null;
        if (bpelEntityId != null) {
            xpath = ModelUtil.getXpath(bpelEntityId);
        }
        
        return LineBreakpoint.create(url, xpath, lineNumber);
    }
    
    private Node getCurrentNode() {
        Node [] nodes = WindowManager.getDefault().getRegistry().getCurrentNodes();
        
        if (nodes == null) {
            return null;
        }
        if (nodes.length == 0) {
            return null;
        }
        if (nodes.length != 1) {
            return null;
        }
        return nodes [0];
    }
    
    private boolean isInSourceEditor() {
        TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
        if (tc == null) {
            return false;
        }
        
        MultiViewHandler mvh = MultiViews.findMultiViewHandler(tc);
        if (mvh == null) {
            return false;
        }

        MultiViewPerspective mvp = mvh.getSelectedPerspective();
        return mvp.preferredID().equals("bpelsource");
    }
    
    private int getCurrentLineNumber(Node node) {
        EditorCookie editorCookie =
            (EditorCookie) node.getLookup().lookup(EditorCookie.class);
        
        if (editorCookie == null) {
            return -1;
        }
        StyledDocument document = editorCookie.getDocument();
        
        if (document == null) {
            return -1;
        }
        
        JEditorPane[] editorPanes = editorCookie.getOpenedPanes();

        if (editorPanes == null) {
            return -1;
        }
        if (editorPanes.length == 0) {
            return -1;
        }
        Caret caret = editorPanes [0].getCaret();

        if (caret == null) {
            return -1;
        }
        int offset = caret.getDot();

        return NbDocument.findLineNumber(document, offset) + 1;
    }
    
    private DataObject getDataObject(Node node) {
        if (node == null) {
            return null;
        }
        return (DataObject)node.getLookup().lookup(DataObject.class);
    }
    
    public int getCurrentLineNumber () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return -1;
        JEditorPane ep = getCurrentEditor ();
        if (ep == null) return -1;
        StyledDocument d = e.getDocument ();
        if (d == null) return -1;
        Caret caret = ep.getCaret ();
        if (caret == null) return -1;
        int ln = NbDocument.findLineNumber (
            d,
            caret.getDot ()
        );
        return ln + 1;
    }
    
    
    /**
     * Returns URL of source currently selected in editor or empty string.
     *
     * @return URL of source currently selected in editor or empty string
     */
    public String getCurrentFile() {
        synchronized (currentLock) {
            if (currentURL == null) {
                DataObject[] nodes = (DataObject[])resDataObject.allInstances().toArray(new DataObject[0]);

                currentURL = "";
                if (nodes.length != 1)
                    return currentURL;
                
                DataObject dO = nodes[0];
                if (dO instanceof DataShadow)
                    dO = ((DataShadow) dO).getOriginal ();

                // return the file path (not URL)
                currentURL = dO.getPrimaryFile().getPath();
            }

            return currentURL;
        }
    }
    
    
    
    private class EditorLookupListener extends Object implements LookupListener, PropertyChangeListener {
        
        private Class type;
        
        public EditorLookupListener(Class type) {
            this.type = type;
        }
        
        public void resultChanged(LookupEvent ev) {
            if (type == DataObject.class) {
                synchronized (currentLock) {
                    currentURL = null;
                    if (currentEditorCookie instanceof EditorCookie.Observable) {
                        ((EditorCookie.Observable) currentEditorCookie).
                                removePropertyChangeListener(editorObservableListener);
                    }
                    currentEditorCookie = null;
                }
                pcs.firePropertyChange (TopComponent.Registry.PROP_CURRENT_NODES, null, null);
            } else if (type == EditorCookie.class) {
                synchronized (currentLock) {
                    currentURL = null;
                    if (currentEditorCookie instanceof EditorCookie.Observable) {
                        ((EditorCookie.Observable) currentEditorCookie).
                                removePropertyChangeListener(editorObservableListener);
                    }
                    currentEditorCookie = null;
                }
                pcs.firePropertyChange (TopComponent.Registry.PROP_CURRENT_NODES, null, null);
            } else if (type == Node.class) {
                pcs.firePropertyChange (TopComponent.Registry.PROP_CURRENT_NODES, null, null);
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(EditorCookie.Observable.PROP_OPENED_PANES)) {
                pcs.firePropertyChange (EditorCookie.Observable.PROP_OPENED_PANES, null, null);
            }
        }
        
    }
}
