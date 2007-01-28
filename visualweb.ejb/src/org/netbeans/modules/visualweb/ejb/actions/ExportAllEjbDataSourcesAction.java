/*
 * ExportEjbDataSource.java
 *
 * Created on August 30, 2004, 3:38 PM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.ui.ExportEjbDataSourceDialog;
import java.util.Set;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * To export EJB datasources
 *
 * @author  cao
 */
public class ExportAllEjbDataSourcesAction extends NodeAction {
    
    public ExportAllEjbDataSourcesAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        // Enable it only if there are ejb groups
        
        Set ejbGrps = EjbDataModel.getInstance().getEjbGroups();
        
        if( ejbGrps != null && !ejbGrps.isEmpty() )
            return true;
        else
            return false;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        // TODO help needed
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( ExportAllEjbDataSourcesAction.class, "EXPORT_ALL_EJB_DATASOURCES" );
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        ExportEjbDataSourceDialog exportDialog = new ExportEjbDataSourceDialog();
        exportDialog.showDialog();
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
}
