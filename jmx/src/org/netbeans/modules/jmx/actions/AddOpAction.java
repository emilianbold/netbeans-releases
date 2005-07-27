/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.actions;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.netbeans.modules.jmx.*;
import org.netbeans.modules.jmx.actions.dialog.AddOperationsInfoPanel;
import org.netbeans.modules.jmx.actions.dialog.AddOperationsPanel;
import org.netbeans.modules.jmx.mbeanwizard.generator.StdMBeanClassGen;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;

/**
 * Action used to add Operations to an existing MBean.
 * @author tl156378
 */
public class AddOpAction extends CookieAction {
    
    private Resource rc;
    private DataObject dob;
    
    /**
     * Creates a new instance of UpdateAttrAction 
     */
    public AddOpAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    protected Class[] cookieClasses() {
        return new Class[] { SourceCookie.class };
    }
     
    protected int mode() {
        // allow multiple selected nodes (classes, packages)
        return MODE_EXACTLY_ONE;    
    }
    
    public boolean asynchronous() {
        return true; // yes, this action should run asynchronously
        // would be better to rewrite it to synchronous (running in AWT thread),
        // just replanning test generation to RequestProcessor
    }  
    
    protected boolean enable (Node[] nodes) {
        if (!super.enable(nodes)) return false;
        if (nodes.length == 0) return false;

        dob = (DataObject)nodes[0].getCookie(DataObject.class);
        FileObject fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        rc = JavaModel.getResource(fo);
        JavaClass foClass = WizardHelpers.getJavaClass(rc,fo.getName());
        boolean isMBean = Introspector.isStandard(foClass);
        
        return isMBean;
    }
    
    protected void performAction (Node[] nodes) {
        // show configuration dialog
        // when dialog is canceled, escape the action
        AddOperationsPanel cfg = new AddOperationsPanel(nodes[0]);
        if (!cfg.configure()) {
            return;
        }
        //detect if implementation of operations exists
        JavaClass mbeanClass = cfg.getMBeanClass();
        MBeanOperation[] operations = cfg.getOperations();
        boolean hasExistOp = false;
        for (int i = 0; i < operations.length; i++) {
            boolean opExists = 
                    Introspector.hasOperation(mbeanClass,rc,operations[i]);
            operations[i].setMethodExists(opExists);
            hasExistOp = hasExistOp || opExists;
        }
        if (hasExistOp) {
            AddOperationsInfoPanel infoPanel = 
                    new AddOperationsInfoPanel(mbeanClass.getSimpleName(),
                        operations);
            if (!infoPanel.configure()) {
                return;
            }
        }
        
        StdMBeanClassGen generator = new StdMBeanClassGen();
        try {
            generator.updateOperations(mbeanClass,rc,operations);
            EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
            ec.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx(""); // NOI18N
    }
    
    public String getName () {
        return NbBundle.getMessage(AddOpAction.class,"LBL_Action_AddMBeanOperation"); // NOI18N
    }
}
