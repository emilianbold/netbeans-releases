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
package org.netbeans.modules.java.hints.introduce;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

public final class IntroduceAction extends AbstractAction implements PropertyChangeListener {
    
    private IntroduceKind type;

    private IntroduceAction(IntroduceKind type) {
        this.type = type;
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        switch (type) {
            case CREATE_CONSTANT:
                putValue(NAME, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction"));
                break;
            case CREATE_VARIABLE:
                putValue(NAME, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"));
                break;
            case CREATE_FIELD:
                putValue(NAME, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction"));
                break;
            case CREATE_METHOD:
                putValue(NAME, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"));
                break;
        }
        
        TopComponent.getRegistry().addPropertyChangeListener(WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        
        updateEnabled();
    }
    
    private void updateEnabled() {
        setEnabled(getCurrentFile(null) != null);
    }

    public void actionPerformed(ActionEvent e) {
        String error = doPerformAction();
        
        if (error != null) {
            String errorText = NbBundle.getMessage(IntroduceAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }
    
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
            
            if (span[0] == span[1])
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
    
    private String doPerformAction() {
        final int[] span = new int[2];
        FileObject file = getCurrentFile(span);
        
        if (file == null)
            return "ERR_Not_Selected"; //NOI18N
        
        JavaSource js = JavaSource.forFileObject(file);
        
        if (js == null)
            return "ERR_Not_Supported"; //NOI18N
        
        final Map<IntroduceKind, Fix> fixes = new EnumMap<IntroduceKind, Fix>(IntroduceKind.class);
        final Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);
        
        try {
            js.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {}
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);
                    IntroduceHint.computeError(parameter, span[0], span[1], fixes, errorMessages);
                }
            }, true);
            
            Fix fix = fixes.get(type);
            
            if (fix != null) {
                fix.implement();
                
                return null;
            }
            
            String errorMessage = errorMessages.get(type);
            
            if (errorMessage != null)
                return errorMessage;
            
            return "ERR_Invalid_Selection"; //XXX  //NOI18N
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }
    
    public static IntroduceAction createVariable() {
        return new IntroduceAction(IntroduceKind.CREATE_VARIABLE);
    }
    
    public static IntroduceAction createConstant() {
        return new IntroduceAction(IntroduceKind.CREATE_CONSTANT);
    }
    
    public static IntroduceAction createField() {
        return new IntroduceAction(IntroduceKind.CREATE_FIELD);
    }

    public static IntroduceAction createMethod() {
        return new IntroduceAction(IntroduceKind.CREATE_METHOD);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        updateEnabled();
    }
    
}
