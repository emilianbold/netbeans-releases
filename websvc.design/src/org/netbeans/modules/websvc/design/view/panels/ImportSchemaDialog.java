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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
