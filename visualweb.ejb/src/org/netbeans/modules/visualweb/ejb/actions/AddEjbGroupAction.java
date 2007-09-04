/*
 * AddEjbGroupAction.java
 *
 * Created on April 30, 2004, 4:06 PM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import org.netbeans.modules.visualweb.ejb.ui.AddEjbGroupDialog;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * The action invoked when the user tries to add a new EJB group
 *
 * @author  dongmei cao
 */
public class AddEjbGroupAction extends NodeAction {
    
    public AddEjbGroupAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
    	return EjbLoaderHelper.isEnableAction();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        // TODO help needed
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( AddEjbGroupAction.class, "ADD_EJB_GROUP" );
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        
        AddEjbGroupDialog addDialog = new AddEjbGroupDialog();
        addDialog.showDialog();
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
    protected String iconResource () {
        return "org/netbeans/modules/visualweb/ejb/resources/ejb_modul_project.png"; // NOI18N
    }
    
}
