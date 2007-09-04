/*
 * ConfigureEjbGroupAction.java
 *
 * Created on February 17, 2005, 5:11 PM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import org.netbeans.modules.visualweb.ejb.nodes.EjbGroupNode;
import org.netbeans.modules.visualweb.ejb.ui.ConfigureMethodsDialog;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * 
 * @author  cao
 */
public class ConfigureMethodsAction extends NodeAction {
    
    /** Creates a new instance of ConfigureEjbGroupAction */
    public ConfigureMethodsAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return EjbLoaderHelper.isEnableAction();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        // TODO help needed
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( ConfigureMethodsAction.class, "CONFIGURE_EJB_METHODS" );
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
            ConfigureMethodsDialog dialog = new ConfigureMethodsDialog( ejbGrp );
            dialog.showDialog();
        }
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
}
