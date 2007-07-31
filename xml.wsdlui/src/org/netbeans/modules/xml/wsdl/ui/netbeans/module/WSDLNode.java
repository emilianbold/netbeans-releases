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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;

import javax.swing.Action;
import javax.swing.undo.UndoManager;

import org.netbeans.modules.xml.refactoring.CannotRefactorException;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.ui.ModelProvider;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.WSDLViewOpenAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * A node to represent the WSDLDataObject.
 */
public class WSDLNode extends DataNode implements ModelProvider {

    public WSDLNode(WSDLDataObject obj) {
        this (obj, Children.LEAF);
    }

    private WSDLNode(DataObject obj, Children ch) {
        super (obj, ch);
        setIconBaseWithExtension("org/netbeans/modules/xml/wsdl/ui/netbeans/module/resources/wsdl_file.png");
    }

    @Override
	public Action getPreferredAction() {
        return SystemAction.get(WSDLViewOpenAction.class);
    }

    @Override
	public void setName(String name, boolean rename) {
        WSDLDataObject dobj = (WSDLDataObject) getDataObject();
        if (!rename || name != null && name.equals(dobj.getName())) {
            return;
        }

        try {
            WSDLEditorSupport editor = dobj.getWSDLEditorSupport();
            WSDLModel model = editor.getModel();
            UndoManager undo = editor.getUndoManager();
            // Modifying definitions component leaves an edit on the queue.
            model.removeUndoableEditListener(undo);
            //FileRenameRequest request = new FileRenameRequest(model, name);
            try {
            //    RefactoringManager.getInstance().execute(request, true);
                SharedUtils.silentFileRefactor(model, name, true);
                // Rename the definitions element in the model to follow
                // the name of the file.
                if (model.startTransaction()) {
                    model.getDefinitions().setName(name);
                }
            } catch (CannotRefactorException cre) {
               // AnalysisUtilities.showFileRenameRefactoringUI(model);
                SharedUtils.showFileRenameRefactoringUI(model);
            } finally {
                model.endTransaction();
            }
            model.addUndoableEditListener(undo);
            // Save the document after modifying the definitions component,
            // so the file is not left modified because of the file rename.
            editor.saveDocument();
        } catch (IOException ioe) {
            String msg = NbBundle.getMessage(WSDLDataObject.class,
                    "MSG_UnableToRename", ioe.getMessage());
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    public WSDLModel getModel() {
//        try {
            WSDLDataObject dobj = (WSDLDataObject) getDataObject();
            return dobj.getWSDLEditorSupport().getModel();
        /*} catch(IOException ex) {
            String msg = NbBundle.getMessage(WSDLDataObject.class, "MSG_UnableToLoadWsdl", ex.getMessage());
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
        return null;*/
    }
}
