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
import org.netbeans.modules.jmx.actions.dialog.AddRegistIntfPanel;
import org.netbeans.modules.jmx.mbeanwizard.generator.AddRegistIntfGenerator;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;

/**
 * Action used to add MBeanRegistration implementation to an existing MBean.
 * @author tl156378
 */
public class AddRegisterIntfAction extends CookieAction {
    
    private DataObject dob;
    private Resource rc;
    
    /**
     * Creates a new instance of UpdateAttrAction 
     */
    public AddRegisterIntfAction() {
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
        return false; // yes, this action should run asynchronously
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
        if (foClass == null)
            return false;
        boolean isMBean = Introspector.testCompliance(foClass);
        boolean hasMBeanRegistIntf = Introspector.hasMBeanRegistIntf(foClass);
        
        return isMBean && !hasMBeanRegistIntf;
    }
    
    protected void performAction (Node[] nodes) {
        // show configuration dialog
        // when dialog is canceled, escape the action
        AddRegistIntfPanel cfg = new AddRegistIntfPanel(nodes[0]);
        if (!cfg.configure()) {
            return;
        }
        AddRegistIntfGenerator generator = new AddRegistIntfGenerator();
        try {
            generator.update(cfg.getMBeanClass(),rc,cfg.getKeepRefSelected());
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
        return NbBundle.getMessage(AddRegisterIntfAction.class,"LBL_Action_AddMBeanRegistrationIntf"); // NOI18N
    }
}
