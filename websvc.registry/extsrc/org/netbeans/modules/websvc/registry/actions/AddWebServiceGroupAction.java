/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
