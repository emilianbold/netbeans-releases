/*
 * ModifyEjbGroup.java
 *
 * Created on May 7, 2004, 12:32 PM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import org.netbeans.modules.visualweb.ejb.nodes.EjbGroupNode;
import org.netbeans.modules.visualweb.ejb.ui.AddEjbGroupDialog;
import org.netbeans.modules.visualweb.ejb.ui.ModifyEjbGroupDialog;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * The action invoked when the user clicks on the "Modify ..."
 * menu item on an EJB group
 *
 * @author  cao
 */
public class ModifyEjbGroupAction extends NodeAction {
    
    public ModifyEjbGroupAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        // Only works for single node
        boolean isSingle = activatedNodes.length == 1;
        return isSingle && EjbLoaderHelper.isEnableAction();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( ModifyEjbGroupAction.class, "MODIFY_EJB_GROUP" );
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        
        if( activatedNodes != null && activatedNodes.length > 0 )
        {
            Node node = null;
            if(activatedNodes[0] instanceof FilterNode){
                node = (Node) activatedNodes[0].getCookie(EjbGroupNode.class);
            }else{
                node = activatedNodes[0];
            }
            EjbGroup ejbGrp = ((EjbGroupNode)node).getEjbGroup();
            ModifyEjbGroupDialog dialog = new ModifyEjbGroupDialog( ejbGrp );
            dialog.showDialog();
        }
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
}
