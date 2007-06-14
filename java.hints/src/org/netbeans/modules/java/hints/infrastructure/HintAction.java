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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.infrastructure;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

public abstract class HintAction extends AbstractAction implements PropertyChangeListener {
    
    protected HintAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        
        TopComponent.getRegistry().addPropertyChangeListener(WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        
        updateEnabled();
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
        JTextComponent pane = null;
        
        if (tc instanceof CloneableEditorSupport.Pane) {
            pane = ((CloneableEditorSupport.Pane) tc).getEditorPane();
        }
        
        if (pane == null)
            return null;
        
        if (span != null) {
            span[0] = pane.getSelectionStart();
            span[1] = pane.getSelectionEnd();
            
            if (span[0] == span[1] && requiresSelection())
                return null;
        }
        
        Document doc = pane.getDocument();
        Object stream = doc.getProperty(Document.StreamDescriptionProperty);
        
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
