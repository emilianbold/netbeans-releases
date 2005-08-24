/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Feature;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;


/**
 * Provide action for calling another EJB
 * @author Chris Webster
 * @author Martin Adamek
 */
public class CallEjbAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        JavaClass beanClass = JMIUtils.getJavaClassFromNode(nodes[0]);
        CallEjbDialog callEjbDialog = new CallEjbDialog();
        callEjbDialog.open(beanClass, NbBundle.getMessage(CallEjbAction.class, "LBL_CallEjbAction")); //NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(CallEjbAction.class, "LBL_CallEjbAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(CallEjbAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
	JavaClass jc = JMIUtils.getJavaClassFromNode(nodes[0]);
        return jc == null ? false : !jc.isInterface();
    }
    
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(CallEjbAction.class, "HINT_Action"));
     * }
     */

}
