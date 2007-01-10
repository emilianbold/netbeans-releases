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
import org.netbeans.modules.jmx.actions.dialog.AddRegistIntfPanel;
import org.netbeans.modules.jmx.mbeanwizard.generator.AddRegistIntfGenerator;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action used to add MBeanRegistration implementation to an existing MBean.
 * @author tl156378
 */
public class AddRegisterIntfAction extends NodeAction {
    
    private DataObject dob;
    
    /**
     * Creates a new instance of UpdateAttrAction
     */
    public AddRegisterIntfAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        
    }
    
    public boolean asynchronous() {
        return false; // yes, this action should run asynchronously
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
            boolean isMBean = JavaModelHelper.testMBeanCompliance(foClass);
            boolean hasMBeanRegistIntf = JavaModelHelper.implementsMBeanRegistrationItf(foClass);
            
            return isMBean && !hasMBeanRegistIntf;
        }catch(IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    protected void performAction(Node[] nodes) {
        try {
            FileObject fo = null;
            if (dob != null) fo = dob.getPrimaryFile();
            
            // show configuration dialog
            // when dialog is canceled, escape the action
            AddRegistIntfPanel cfg = new AddRegistIntfPanel(nodes[0]);
            if (!cfg.configure()) {
                return;
            }
            AddRegistIntfGenerator generator = new AddRegistIntfGenerator();
            
            generator.update(fo, cfg.getKeepRefSelected());
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
        return NbBundle.getMessage(AddRegisterIntfAction.class,"LBL_Action_AddMBeanRegistrationIntf"); // NOI18N
    }
}
