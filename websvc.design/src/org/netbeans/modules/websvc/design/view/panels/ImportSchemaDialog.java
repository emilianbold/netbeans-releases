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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.view.panels;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Roderico Cruz, Milan Kuchtiak
 * Displays a Dialog for selecting imported Schemas
 */

public class ImportSchemaDialog {
    private Dialog dialog;
    private SelectSchemaPanel sPanel;
    private AddImportedSchemaDialogDesc dlgDesc;
    
    /**
     * CImportSchemaDialogstance of WSHandlerDialog
     */
    public ImportSchemaDialog(Project project) {
        sPanel = new SelectSchemaPanel(project);
        dlgDesc = new AddImportedSchemaDialogDesc(sPanel);
        dialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
    }
    
    public void show(){
        dialog.setVisible(true);
    }
    
    public boolean okButtonPressed(){
        return dlgDesc.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Set<Schema> getSelectedSchemas(){
        Set<Schema> selectedSchemas = new HashSet<Schema>();
        Node[] nodes = sPanel.getSelectedNodes();
        for(int i = 0; i < nodes.length; i++) {
            FileObject fo = getFileObjectFromNode(nodes[i]);
            if (fo!=null) {
                ModelSource ms = Utilities.getModelSource(fo, true);
                SchemaModel schemaModel = SchemaModelFactory.getDefault().getModel(ms);
                selectedSchemas.add(schemaModel.getSchema());
            }
        }
        return selectedSchemas;
    }
    
    private FileObject getFileObjectFromNode(Node n) {
        DataObject dObj = n.getCookie(DataObject.class);
        if (dObj!=null) return dObj.getPrimaryFile();
        return null;
    }
    
    class AddImportedSchemaDialogDesc extends DialogDescriptor{
        Project project;
        final SelectSchemaPanel sPanel;
        
        private Object[] closingOptionsWithoutOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION};
        private Object[] closingOptionsWithOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION, DialogDescriptor.OK_OPTION};
        
        /**
         * Creates a new instance of AddImportedSchemaDialogDesc
         */
        public AddImportedSchemaDialogDesc(SelectSchemaPanel sPanel) {
            super(sPanel, NbBundle.getMessage(ImportSchemaDialog.class, "TTL_SelectSchema"));
            this.sPanel = sPanel;
            this.setButtonListener(new AddImportedSchemaActionListener(sPanel));
        }
        
        class AddImportedSchemaActionListener implements ActionListener{
            SelectSchemaPanel sPanel;
            public AddImportedSchemaActionListener(SelectSchemaPanel sPanel){
                this.sPanel = sPanel;
            }
            public void actionPerformed(ActionEvent evt) {
                if(evt.getSource() == NotifyDescriptor.OK_OPTION){
                    boolean accepted = true;
                    String errMsg = null;
                    Node[] selectedNodes = sPanel.getSelectedNodes();
                    for(int i = 0; i < selectedNodes.length; i++){
                        Node node = selectedNodes[i];
                        FileObject schemaFo = getFileObjectFromNode(node);
                        
                    }
                    if (!accepted) {
                        NotifyDescriptor.Message notifyDescr =
                                new NotifyDescriptor.Message(errMsg,
                                NotifyDescriptor.ERROR_MESSAGE );
                        DialogDisplayer.getDefault().notify(notifyDescr);
                        AddImportedSchemaDialogDesc.this.setClosingOptions(closingOptionsWithoutOK);
                    } else {
                        // Everything was fine so allow OK
                        AddImportedSchemaDialogDesc.this.setClosingOptions(closingOptionsWithOK);
                    }
                }
            }
        }
    }
}
