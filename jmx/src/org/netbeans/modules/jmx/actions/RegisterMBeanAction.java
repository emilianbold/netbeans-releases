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
import org.netbeans.modules.jmx.actions.dialog.RegisterMBeanPanel;
import org.netbeans.modules.jmx.actions.generator.RegisterMBeanGenerator;
import org.openide.cookies.EditorCookie;
import org.openide.util.NbBundle;

/**
 * Action used to add MBean registration code to an existing JMX Agent.
 * @author tl156378
 */
public class RegisterMBeanAction extends CookieAction {
    
    private FileObject fo;
    private DataObject dob;
    private Resource rc;
    
    /**
     * Creates a new instance of AddAttrAction 
     */
    public RegisterMBeanAction() {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N 
    }
    
    protected Class[] cookieClasses() {
        return new Class[] { SourceCookie.class };
    }
     
    protected int mode() {
        // allow one exactly class node
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
        fo = null;
        if (dob != null) fo = dob.getPrimaryFile();
        rc = JavaModel.getResource(fo);
        JavaClass foClass = WizardHelpers.getJavaClass(rc,fo.getName());
        
        boolean isAgent = Introspector.isGeneratedAgent(foClass);
        
        return isAgent;
    }
    
    protected void performAction(Node[] nodes) {
        // show configuration dialog
        // when dialog is canceled, escape the action
        RegisterMBeanPanel cfg = new RegisterMBeanPanel(nodes[0]);
        if (!cfg.configure()) {
            return;
        }
        RegisterMBeanGenerator generator = new RegisterMBeanGenerator();
        try {
            if (cfg.standardMBeanSelected()) {
                generator.generate(cfg.getJavaClass(),cfg.getMBeanObjectName(),
                        cfg.getClassName(), cfg.getInterfaceName());
            } else {
                generator.generate(cfg.getJavaClass(), cfg.getMBeanClass(),
                        cfg.getMBeanObjectName(), cfg.getConstructorSignature());
            }
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
        return NbBundle.getMessage (RegisterMBeanAction.class, 
                "LBL_Action_RegisterMBean"); // NOI18N
    }
}
