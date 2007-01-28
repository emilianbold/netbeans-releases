/*
 * DeleteEjbGroupAction.java
 *
 * Created on May 7, 2004, 12:23 AM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.nodes.EjbGroupNode;
import java.util.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * The action invoked when the user clicks on the "Delete"
 * menu item on an EJB group node
 *
 * @author  cao
 */
public class DeleteEjbGroupAction extends NodeAction 
{
    public DeleteEjbGroupAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( DeleteEjbGroupAction.class, "DELETE" );
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) 
    {
        // If the user only selected one EJB group to delete, the message will be like
        // Are you sure you want to delete <EJB group name>?
        // If the user selected multiple EJB groups to delete, the message will be like
        // Are you sure you want to delete these <number> items?
        // This makes the dialog consitent with the one popped up from netbeans when the user hit delete key
        
        String grpNameMsg = null;
        String msg = null;
        if( activatedNodes.length == 1 )
        {
            grpNameMsg = ((EjbGroupNode)activatedNodes[0]).getEjbGroup().getName();
            msg = NbBundle.getMessage( DeleteEjbGroupAction.class, "DELETE_CONFIRMATION", grpNameMsg );
        }
        else
            msg = NbBundle.getMessage( DeleteEjbGroupAction.class, "DELTE_MULTIPLE_EJB_SETS", Integer.toString(activatedNodes.length) );
       
        // Confirm Object Deletion for deleting one EJB group and
        // Confirm Mulitple Object Deletion for deleting mulitple EJB groups
        
        String title = null;
        if( activatedNodes.length == 1 )
            title = NbBundle.getMessage( DeleteEjbGroupAction.class, "DELETE_DIALOG_TITLE" );
        else
            title = NbBundle.getMessage( DeleteEjbGroupAction.class, "DELETE_DIALOG_TITLE_MUL" );
        
        NotifyDescriptor d = new NotifyDescriptor.Confirmation( msg, title, NotifyDescriptor.YES_NO_OPTION );
        Object response = DialogDisplayer.getDefault().notify(d);
        
        if( response != null && response.equals( NotifyDescriptor.YES_OPTION ) ) 
        {
            Collection removeGroups = new ArrayList();
            for( int i = 0; i < activatedNodes.length; i ++ ){
                Node node = null;
                if(activatedNodes[i] instanceof FilterNode){
                    node = (Node) activatedNodes[i].getCookie(EjbGroupNode.class);
                }else{
                    node = activatedNodes[i];
                }
                removeGroups.add( ((EjbGroupNode)node).getEjbGroup() );
            }
            
            EjbDataModel.getInstance().removeEjbGroups( removeGroups );
        }
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
    
}
