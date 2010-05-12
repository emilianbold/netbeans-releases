/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.infrastructure;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.api.java.source.JavaSource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

public abstract class HintAction extends TextAction implements PropertyChangeListener {
    
    protected HintAction() {
        super(null);
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        
        TopComponent.getRegistry().addPropertyChangeListener(WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        
        if (!SwingUtilities.isEventDispatchThread()) {
            setEnabled(false);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateEnabled();
                }
            });
        } else {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        setEnabled(getCurrentFile(null) != null);
    }

    public void actionPerformed(ActionEvent e) {
        String error = doPerform();
        
        if (error != null) {
            String errorText = NbBundle.getMessage(HintAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }
    
    private String doPerform() {
        int[] span = new int[2];
        FileObject file = getCurrentFile(span);
        
        if (file == null) {
            if (span[0] != span[1])
                return "ERR_Not_Selected"; //NOI18N
            else
                return "ERR_No_Selection"; //NOI18N
        }
        
        JavaSource js = JavaSource.forFileObject(file);
        
        if (js == null)
            return  "ERR_Not_Supported"; //NOI18N
        
        perform(js, span);
        
        return null;
    }
    
    protected abstract void perform(JavaSource js, int[] selection);
    
    private FileObject getCurrentFile(int[] span) {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        Lookup l = tc != null ? tc.getLookup() : null;
        EditorCookie ec = l != null ? l.lookup(EditorCookie.class) : null;
        JTextComponent pane = ec != null ? NbDocument.findRecentEditorPane(ec) : null;
        
        if(pane == null)
            return null;
        if (span != null) {
            span[0] = pane.getSelectionStart();
            span[1] = pane.getSelectionEnd();
            
            if (span[0] == span[1] && requiresSelection())
                return null;
        }
        
        Document doc = pane.getDocument();
        Object stream = doc != null ? doc.getProperty(Document.StreamDescriptionProperty) : null;
        
        if (!(stream instanceof DataObject))
            return null;
        
        DataObject dObj = (DataObject) stream;
        FileObject result = dObj.getPrimaryFile();
        
        if ("text/x-java".equals(FileUtil.getMIMEType(result))) //NOI18N
            return result;
        else
            return null;
    }
    
    protected boolean requiresSelection() {
        return true;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        updateEnabled();
    }

}
