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
package org.netbeans.modules.websvc.rest.component.palette;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.rest.codegen.WsdlComponentGenerator;
import org.netbeans.modules.websvc.rest.codegen.RestComponentGenerator;
import org.netbeans.modules.websvc.rest.codegen.WadlComponentGenerator;
import org.netbeans.modules.websvc.rest.codegen.model.WsdlResourceBean;
import org.netbeans.modules.websvc.rest.support.Utils;
import org.netbeans.modules.websvc.rest.wizard.ProgressDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Owner
 */
public class RestComponentHandler implements ActiveEditorDrop {
    private FileObject targetFO;
    private RestComponentData data;
    private RequestProcessor.Task generatorTask;
 
    public RestComponentHandler() {
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        targetFO = getTargetFile(targetComponent);
        if (targetFO == null) {
            return false;
        }
        
        final List<Exception> errors = new ArrayList<Exception>();
        final Project project = FileOwnerQuery.getOwner(targetFO);
        Lookup pItem = RestPaletteFactory.getCurrentPaletteItem();
        Node n = pItem.lookup(Node.class);
        final RestComponentData data = RestPaletteFactory.getRestComponentData(n);
        final String type = data.getService().getMethods().get(0).getType();
        
        final ProgressDialog dialog = new ProgressDialog(NbBundle.getMessage(
                RestComponentHandler.class, "LBL_RestComponentProgress",
                n.getName()));
        
        generatorTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                try {
                    RestComponentGenerator codegen = null;
                    if (RestComponentData.isWSDL(type))
                        codegen = new WsdlComponentGenerator(targetFO, data); 
                    else if (RestComponentData.isWADL(type))
                        codegen = new WadlComponentGenerator(targetFO, data);
                    if (codegen.needsInputs()) {
                        InputValuesPanel panel = new InputValuesPanel(codegen.getInputParameterTypes(), project);
                        DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(RestComponentHandler.class, "LBL_ConstantParams"));
                        Object response = DialogDisplayer.getDefault().notify(desc);
                        if (response.equals(NotifyDescriptor.YES_OPTION)) {
                            codegen.setConstantInputValues(panel.getInputParamValues());
                        } else { // cancel
                            return;
                        }     
                    }    
                    
                    codegen.generate(dialog.getProgressHandle());
                    Utils.showMethod(targetFO, codegen.getSubResourceLocator());
                } catch(Exception ioe) {
                    errors.add(ioe);
                } finally {
                    dialog.close();
                }
            }
        });
        
        generatorTask.schedule(50);
        
        dialog.open();
        
        if (errors.size() > 0) {
            for (Exception e : errors) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "handleTransfer", e);
            }
            return false;
        }
        return true;
    }
    
    public static FileObject getTargetFile(JTextComponent targetComponent) {
        if (targetComponent == null)
            return null;
        
        DataObject d = NbEditorUtilities.getDataObject(targetComponent.getDocument());
        if (d == null)
            return null;
        
        EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
        if(ec == null || ec.getOpenedPanes() == null)
            return null;
        
        return d.getPrimaryFile();
    }
    
}
