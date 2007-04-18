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
package org.netbeans.modules.bpel.editors.api.ui.valid;

import java.util.concurrent.Callable;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.ui.form.valid.AbstractDialogDescriptor;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.ErrorManager;

/**
 * Special kind of the SOA dialog descriptor which support XML models' transactions. 
 *
 * @author nk160297
 */
public class NodeEditorDescriptor extends AbstractDialogDescriptor {
    
    private Callable<Boolean> okButtonProcessor;
    private boolean subscribed = true; // intended to prevent double unsubscription;
    
    public NodeEditorDescriptor(CustomNodeEditor editor, String title) {
        super(editor, title);
    }
    
    public void processOkButton() {
        try {
            final CustomNodeEditor editor = getEditor();
            Object modelEntity = editor.getEditedObject();
            if (modelEntity instanceof BpelEntity) {
                //
                // Save changes to the BPEL model
                BpelModel model = ((BpelEntity)modelEntity).getBpelModel();
                model.invoke(new Callable<Object>() {
                    public Object call() throws java.lang.Exception {
                        processOkButtonImpl(editor);
                        return null;
                    }
                }, this);
            } else if (modelEntity instanceof WSDLComponent){
                //
                // Save changes to the WSDL model
                WSDLModel model = ((WSDLComponent)modelEntity).getModel();
                model.startTransaction();
                try {
                    processOkButtonImpl(editor);
                } finally {
                    model.endTransaction();
                }
            } else {
                processOkButtonImpl(editor);
            }
        } catch (java.lang.Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    private void processOkButtonImpl(CustomNodeEditor editor) {
        boolean success = false;
        //
        // Stop listening events
        editor.unsubscribeListeners();
        subscribed = false;
        try {
            success = editor.doValidateAndSave();
            if (success) {
                if (okButtonProcessor != null) {
                    success = okButtonProcessor.call();
                }
            }
        } catch (java.lang.Exception ex) {
            success = false;
            ErrorManager.getDefault().notify(ex);
        } finally {
            if (success){
                setOptionClosable(btnOk, true);
            } else {
                // Start listening events again
                editor.subscribeListeners();
                subscribed = true;
            }
        }
    }
    
    public void processWindowClose() {
        CustomNodeEditor editor = getEditor();
        if (subscribed) {
            editor.unsubscribeListeners();
            subscribed = false;
        }
        editor.afterClose();
    }
    
    public CustomNodeEditor getEditor() {
        return (CustomNodeEditor)super.getMessage();
    }
    
    public Callable<Boolean> getOkButtonProcessor() {
        return okButtonProcessor;
    }
    
    public void setOkButtonProcessor(Callable<Boolean> processor) {
        this.okButtonProcessor = processor;
    }
    
    public void setMessage(Object innerPane) {
        assert innerPane instanceof CustomNodeEditor;
        //
        super.setMessage(innerPane);
    }
    
}
