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

package org.netbeans.modules.websvc.registry.actions;

import java.awt.Dialog;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;

import org.netbeans.modules.websvc.registry.nodes.*;
import org.netbeans.modules.websvc.registry.nodes.WebServiceGroupNode;
import org.openide.nodes.Node;

/** Add a webservice group node to the root node
 * @author  Winston Prakash
 */
public class AddWebServiceGroupAction extends NodeAction {
    
    protected boolean enable(org.openide.nodes.Node[] node) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(AddWebServiceGroupAction.class, "ADD_GROUP");
    }
    
    protected void performAction(Node[] nodes) {
        WebServiceListModel wsNodeModel = WebServiceListModel.getInstance();
        AddWSGroupPanel innerPanel = new AddWSGroupPanel();
        DialogDescriptor dialogDesc = new DialogDescriptor(innerPanel,
                NbBundle.getMessage(AddWebServiceGroupAction.class, "TTL_AddWSGroup"));
        MyDocListener dl = new MyDocListener(dialogDesc,wsNodeModel);
        innerPanel.getTFDocument().addDocumentListener(dl);      
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
        dialogDesc.setValid(false);
        dialog.setVisible(true);
        if (NotifyDescriptor.OK_OPTION.equals(dialogDesc.getValue())) {
            WebServiceGroup wsGroup =  new WebServiceGroup();
            String groupName = innerPanel.getGroupName();
            wsGroup.setName(groupName);
            wsNodeModel.addWebServiceGroup(wsGroup); 
        }
        innerPanel.getTFDocument().removeDocumentListener(dl);
        dialog.dispose();
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
    /** Listener that checks if group name is not empty or duplicite
     */
    private class MyDocListener implements DocumentListener {
        
        private DialogDescriptor dd;
        private WebServiceListModel wsNodeModel;
        
        MyDocListener(DialogDescriptor dd, WebServiceListModel wsNodeModel) {
            this.dd=dd;
            this.wsNodeModel=wsNodeModel;
        }
              
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }
        
        public void update(javax.swing.event.DocumentEvent e) {
            Document doc = e.getDocument();
            try {
                String text = doc.getText(0,doc.getLength()).trim();
                if (text.length()==0 || wsNodeModel.findWebServiceGroup(text) !=null) {
                    dd.setValid(false);
                    return;
                }
            } catch (BadLocationException ex){}
            dd.setValid(true);
        }
        
    }
    
}
