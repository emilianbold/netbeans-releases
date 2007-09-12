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
