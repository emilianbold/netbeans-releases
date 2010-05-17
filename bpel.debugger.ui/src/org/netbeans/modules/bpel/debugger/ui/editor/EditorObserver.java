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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.openide.cookies.EditorCookie;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Zgursky
 */
public class EditorObserver {
    private PropertyChangeListener myOuterListener;
    private EditorListener myEditorListener;
    private EditorDocumentListener myDocumentListener;
    
    private Document myDocument;
    private final EditorCookie.Observable myEditor;

    public EditorObserver(EditorCookie.Observable editor) {
        myEditor = editor;
    }

    public void subscribe(PropertyChangeListener listener) {
        if (myOuterListener != null) {
            unsubscribe();
        }
        
        myOuterListener = listener;
        myEditorListener = new EditorListener();
        myEditor.addPropertyChangeListener(myEditorListener);
        
        smartDocSubscribe();
    }

    public void unsubscribe() {
        if (myOuterListener == null) {
            return;
        }
        
        myEditor.removePropertyChangeListener(myEditorListener);
        myEditorListener = null;
        docUnsubscribe();
        myOuterListener = null;
    }
    
    private void docUnsubscribe() {
        if (myDocumentListener == null ) {
            return;
        }
        assert myDocument != null;
        myDocument.removeDocumentListener(myDocumentListener);
        myDocument = null;
        myDocumentListener = null;
    }
    
    private void smartDocSubscribe() {
        
        //this check is to ensure we don't subscribe to the document if we have
        //already unsubscribed from the editor
        if (myEditorListener == null) {
            return;
        }
        
        Document doc = myEditor.getDocument();
        if (doc == null && myDocumentListener != null) {
            assert myDocument != null;
            myDocument.removeDocumentListener(myDocumentListener);
            myDocument = null;
            myDocumentListener = null;
        } else if (doc != null && myDocumentListener == null) {
            assert myDocument == null;
            myDocument = doc;
            myDocumentListener = new EditorDocumentListener();
            myDocument.addDocumentListener(myDocumentListener);
        }
    }
    
    private class EditorListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            //TODO:find out why this property is never changed so this
            //handler in fact is never executed and we are still
            //listening on the Document changes while the editor could
            //be already closed.
            if (EditorCookie.Observable.PROP_DOCUMENT.
                    equals(evt.getPropertyName()))
            {
                smartDocSubscribe();
            } else if (EditorCookie.Observable.PROP_OPENED_PANES.
                    equals(evt.getPropertyName()))
            {
                smartDocSubscribe();
            }
        }
    }
    
    private class EditorDocumentListener implements DocumentListener {
        private RequestProcessor.Task task;
        
        public void insertUpdate(DocumentEvent e) {
            fireChangeEvent();
        }

        public void removeUpdate(DocumentEvent e) {
            fireChangeEvent();
        }

        public void changedUpdate(DocumentEvent e) {
            fireChangeEvent();
        }
        
        private void fireChangeEvent() {
            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
            
            final PropertyChangeListener outerListener = myOuterListener;
            
            if (outerListener == null) {
                return;
            }
            
            task = RequestProcessor.getDefault().post(new Runnable () {
                public void run () {
                    outerListener.propertyChange(new PropertyChangeEvent(
                            myEditor, null, null, null));
                }
            }, 1000);
        }
    }
}
