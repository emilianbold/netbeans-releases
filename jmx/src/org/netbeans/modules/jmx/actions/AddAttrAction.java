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
import org.netbeans.modules.jmx.actions.dialog.AddAttributesInfoPanel;
import org.netbeans.modules.jmx.actions.dialog.AddAttributesPanel;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action used to add attributes to an existing MBean.
 * @author tl156378
 */
public class AddAttrAction extends NodeAction {
    
    private DataObject dob;
    
    /**
     * Creates a new instance of AddAttrAction
     */
    public AddAttrAction() {
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
        }
        return false;
    }
    
    protected void performAction(Node[] nodes) {
        try {
            // show configuration dialog
            // when dialog is canceled, escape the action
            DataObject dataObj = (DataObject)nodes[0].getCookie(DataObject.class);
            FileObject fo = null;
            if (dataObj != null) fo = dataObj.getPrimaryFile();
            //We need to do all MDR access in a transaction
            
            AddAttributesPanel cfg = new AddAttributesPanel(nodes[0]);
            if (!cfg.configure()) {
                return;
            }
            //detect if implementation of attributes exists
            JavaSource mbeanClass = cfg.getMBeanClass();
            String className = JavaModelHelper.getSimpleName(mbeanClass);
            MBeanAttribute[] attributes = cfg.getAttributes();
            boolean hasExistAttr = false;
            for (int i = 0; i < attributes.length; i++) {
                MBeanAttribute attributeImplementation =
                        JavaModelHelper.searchAttributeImplementation(mbeanClass, attributes[i]);
                if (attributeImplementation != null) {
                    attributes[i] = attributeImplementation;
                    hasExistAttr = true;
                }
            }
            
            if(hasExistAttr) {
                AddAttributesInfoPanel infoPanel =
                        new AddAttributesInfoPanel(className,
                        attributes);
                if (!infoPanel.configure()) {
                    return;
                }
            }
            JavaModelHelper.addAttributesToMBean(mbeanClass, attributes);
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
        return NbBundle.getMessage(AddAttrAction.class,"LBL_Action_AddMBeanAttribute"); // NOI18N
    }
}
