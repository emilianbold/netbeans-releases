/*
 * ImportEjbDataSource.java
 *
 * Created on August 30, 2004, 3:38 PM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import org.netbeans.modules.visualweb.ejb.ui.ImportEjbDataSourcesDialog;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * To import EJB datasources
 *
 * @author  cao
 */
public class ImportEjbDataSourceAction extends NodeAction {
    
    /** Creates a new instance of ImportEjbDataSource */
    public ImportEjbDataSourceAction() {
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
    	return EjbLoaderHelper.isEnableAction();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        // TODO help needed
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage( ImportEjbDataSourceAction.class, "IMPORT_EJB_DATASOURCE" );
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        ImportEjbDataSourcesDialog importDialog = new ImportEjbDataSourcesDialog();
        importDialog.showDialog();
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
}
