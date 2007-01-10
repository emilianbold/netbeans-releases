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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions;
import java.io.IOException;
import org.netbeans.api.java.source.JavaSource;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.jmx.*;
import org.netbeans.modules.jmx.actions.dialog.AddOperationsInfoPanel;
import org.netbeans.modules.jmx.actions.dialog.AddOperationsPanel;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action used to add Operations to an existing MBean.
 * @author tl156378
 */
public class AddOpAction extends NodeAction {
    
    private DataObject dob;
    
    /**
     * Creates a new instance of UpdateAttrAction
     */
    public AddOpAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    public boolean asynchronous() {
        return true; // yes, this action should run asynchronously
        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes.length == 0) return false;
        dob = (DataObject) nodes[0].getLookup().lookup(DataObject.class);
        if(dob == null) return false;
        
        FileObject fo = dob.getPrimaryFile();
        if(fo == null)
            return false;
        
        JavaSource foClass = JavaModelHelper.getSource(fo);
        if (foClass == null) return false;
        try {
           return JavaModelHelper.canUpdateAttributesOrOperations(foClass);
        }catch(IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    protected void performAction(Node[] nodes) {
        try {
            DataObject dataObj = (DataObject)nodes[0].getCookie(DataObject.class);
            FileObject fo = null;
            if (dataObj != null) fo = dataObj.getPrimaryFile();
            
            // show configuration dialog
            // when dialog is canceled, escape the action
            AddOperationsPanel cfg = new AddOperationsPanel(nodes[0]);
            if (!cfg.configure()) {
                return;
            }
            //detect if implementation of operations exists
            JavaSource mbeanClass = cfg.getMBeanClass();
            String className = JavaModelHelper.getSimpleName(mbeanClass);
            MBeanOperation[] operations = cfg.getOperations();
            boolean hasExistOp = false;
            for (int i = 0; i < operations.length; i++) {
                MBeanOperation operationImplementation =
                        JavaModelHelper.searchOperationImplementation(mbeanClass, operations[i]);
                if (operationImplementation != null) {
                    operations[i] = operationImplementation;
                    hasExistOp = true;
                }
            }
            
            if (hasExistOp) {
                AddOperationsInfoPanel infoPanel =
                        new AddOperationsInfoPanel(className,
                        operations);
                if (!infoPanel.configure()) {
                    return;
                }
            }
            JavaModelHelper.addOperationsToMBean(mbeanClass, operations);
            EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
            ec.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(""); // NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(AddOpAction.class,"LBL_Action_AddMBeanOperation"); // NOI18N
    }
}
