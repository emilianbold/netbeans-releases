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

package org.netbeans.modules.bpel.debugger.ui.action;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;

import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/**
 * Taken from EditorContextImpl from jpda debugger.
 * 
 * @author Jan Jancura
 */
public class RunToCursorUtil {
    
    private Object currentLock = new Object();
    private EditorCookie currentEditorCookie = null;
    private PropertyChangeListener  editorObservableListener;
    private PropertyChangeSupport   pcs;
    
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
