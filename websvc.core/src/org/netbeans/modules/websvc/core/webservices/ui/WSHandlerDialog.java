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

package org.netbeans.modules.websvc.core.webservices.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
// Retouche
//import org.netbeans.jmi.javamodel.JavaClass;
//import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.websvc.core.webservices.ui.panels.SelectHandlerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Roderico Cruz
 * Displays a Dialog for selecting web service message handler classes
 * that are in a project.
 */
public class WSHandlerDialog {
    private Dialog dialog;
    private SelectHandlerPanel sPanel;
    private AddMessageHandlerDialogDesc dlgDesc;
    private boolean isJaxWS;
    /**
     * Creates a new instance of WSHandlerDialog
     */
    public WSHandlerDialog(Project project, boolean isJaxWS) {
        this.isJaxWS = isJaxWS;
        sPanel = new SelectHandlerPanel(project);
        dlgDesc = new AddMessageHandlerDialogDesc(sPanel);
        dialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
    }
    
    public void show(){
        dialog.setVisible(true);
    }
    
    public boolean okButtonPressed(){
        return dlgDesc.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Set<String> getSelectedClasses(){
        Set<String> selectedClasses = new HashSet<String>();
        Node[] nodes = sPanel.getSelectedNodes();
        for(int i = 0; i < nodes.length; i++){
// Retouche
//            JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[i]);
//            selectedClasses.add(jc.getName());
        }
        return selectedClasses;
    }
    
    class AddMessageHandlerDialogDesc extends DialogDescriptor{
        Project project;
        final SelectHandlerPanel sPanel;
        
        private Object[] closingOptionsWithoutOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION};
        private Object[] closingOptionsWithOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION, DialogDescriptor.OK_OPTION};
        
        /**
         * Creates a new instance of AddMessageHandlerDialogDesc
         */
        public AddMessageHandlerDialogDesc(SelectHandlerPanel sPanel) {
            super(sPanel, NbBundle.getMessage(WSHandlerDialog.class, "TTL_SelectHandler"));
            this.sPanel = sPanel;
            this.setButtonListener(new AddMessageActionListener(sPanel));
        }
        
        class AddMessageActionListener implements ActionListener{
            SelectHandlerPanel sPanel;
            public AddMessageActionListener(SelectHandlerPanel sPanel){
                this.sPanel = sPanel;
            }
            public void actionPerformed(ActionEvent evt){
// Retouche
//                if(evt.getSource() == NotifyDescriptor.OK_OPTION){
//                    boolean accepted = true;
//                    String errMsg = null;
//                    Node[] selectedNodes = sPanel.getSelectedNodes();
//                    for(int i = 0; i < selectedNodes.length; i++){
//                        Node node = selectedNodes[i];
//                        JavaClass classElement = JMIUtils.getJavaClassFromNode(node);
//                        //FIX-ME: Improve this by filtering the Tree View to only include handlers
//                        if(classElement == null){
//                            errMsg = NbBundle.getMessage(WSHandlerDialog.class,
//                                    "NotJavaClass_msg");
//                            accepted = false;
//                            break;
//                        }
//                        
//                        if(!isHandler(classElement)) {
//                            errMsg = NbBundle.getMessage(WSHandlerDialog.class,
//                                    "NotHandlerClass_msg",
//                                    classElement.getName());
//                            accepted = false;
//                            break;
//                        }
//                    }
//                    if (!accepted) {
//                        NotifyDescriptor.Message notifyDescr =
//                                new NotifyDescriptor.Message(errMsg,
//                                NotifyDescriptor.ERROR_MESSAGE );
//                        DialogDisplayer.getDefault().notify(notifyDescr);
//                        AddMessageHandlerDialogDesc.this.setClosingOptions(closingOptionsWithoutOK);
//                    } else {
//                        // Everything was fine so allow OK
//                        AddMessageHandlerDialogDesc.this.setClosingOptions(closingOptionsWithOK);
//                    }
//                }
            }
        }
// Retouche
//        private boolean isHandler(JavaClass ce) {
//
//            if (ce != null) {
//                if(isJaxWS){
//                    JavaClass handlerClass = JMIUtils.findClass("javax.xml.ws.handler.Handler");
//                    JavaClass logicalHandlerClass = JMIUtils.findClass("javax.xml.ws.handler.LogicalHandler");
//                    return ce.isSubTypeOf(handlerClass) || ce.isSubTypeOf(logicalHandlerClass);
//                }
//                else{
//                    JavaClass handlerClass = JMIUtils.findClass("javax.xml.rpc.handler.Handler");
//                    return ce.isSubTypeOf(handlerClass);
//                }
//            }
//            return false;
//        }
    }
}
