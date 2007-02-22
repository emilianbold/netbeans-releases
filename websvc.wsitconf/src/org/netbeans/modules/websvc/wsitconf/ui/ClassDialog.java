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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
//import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.websvc.wsitconf.util.JMIUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 * Displays a Dialog for selecting classes that are in a project.
 */
public class ClassDialog {
    
    private Dialog dialog;
    private SelectClassPanel sPanel;
    private SelectClassDialogDesc dlgDesc;
    
    /**
     * Creates a new instance of ClassDialog
     */
    public ClassDialog(Project project, String extendingClass) {
        sPanel = new SelectClassPanel(project);
        dlgDesc = new SelectClassDialogDesc(sPanel, extendingClass);
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
//            JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[i]);
//            selectedClasses.add(jc.getName());
        }
        return selectedClasses;
    }
    
    class SelectClassDialogDesc extends DialogDescriptor{
        Project project;
        String extendingClass;
        final SelectClassPanel sPanel;
        
        private Object[] closingOptionsWithoutOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION};
        private Object[] closingOptionsWithOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION, DialogDescriptor.OK_OPTION};
        
        /**
         * Creates a new instance of SelectClassDialogDesc
         */
        public SelectClassDialogDesc(SelectClassPanel sPanel, String extendingClass) {
            super(sPanel, "Select Class");  //NOI18N
            this.extendingClass = extendingClass;
            this.sPanel = sPanel;
            this.setButtonListener(new AddClassActionListener(sPanel));
        }
        
        class AddClassActionListener implements ActionListener{
            SelectClassPanel sPanel;
            public AddClassActionListener(SelectClassPanel sPanel){
                this.sPanel = sPanel;
            }
            public void actionPerformed(ActionEvent evt){
                if(evt.getSource() == NotifyDescriptor.OK_OPTION){
                    boolean accepted = true;
                    String errMsg = null;
                    Node[] selectedNodes = sPanel.getSelectedNodes();
                    for(int i = 0; i < selectedNodes.length; i++){
                        Node node = selectedNodes[i];
//                        JavaClass classElement = JMIUtils.getJavaClassFromNode(node);
//                        if(classElement == null){
//                            errMsg = NbBundle.getMessage(ClassDialog.class, "TXT_NotJavaClass_msg");    //NOI18N
//                            accepted = false;
//                            break;
//                        }
//                        
//                        if(!isWantedClass(classElement)) {
//                            errMsg = NbBundle.getMessage(ClassDialog.class, "TXT_NotWantedClass_msg",   //NOI18N
//                                    classElement.getName(), extendingClass);
//                            accepted = false;
//                            break;
//                        }
                    }
                    if (!accepted) {
                        NotifyDescriptor.Message notifyDescr =
                                new NotifyDescriptor.Message(errMsg,
                                NotifyDescriptor.ERROR_MESSAGE );
                        DialogDisplayer.getDefault().notify(notifyDescr);
                        SelectClassDialogDesc.this.setClosingOptions(closingOptionsWithoutOK);
                    } else {
                        // Everything was fine so allow OK
                        SelectClassDialogDesc.this.setClosingOptions(closingOptionsWithOK);
                    }
                }
            }
        }
//        private boolean isWantedClass(JavaClass ce) {
//            if (ce != null) {
//                JavaClass wantedClass = JMIUtils.findClass(extendingClass);
//                return ce.isSubTypeOf(wantedClass);
//            }
//            return false;
//        }
    }
}
