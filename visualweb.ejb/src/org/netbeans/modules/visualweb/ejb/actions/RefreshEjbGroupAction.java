/*
 * RefreshEjbGroupAction.java
 *
 * Created on September 2, 2004, 6:49 PM
 */

package org.netbeans.modules.visualweb.ejb.actions;

import java.io.IOException;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbDataModel;
import org.netbeans.modules.visualweb.ejb.load.EjbLoadException;
import org.netbeans.modules.visualweb.ejb.load.EjbLoaderHelper;
import org.netbeans.modules.visualweb.ejb.datamodel.EjbGroup;
import org.netbeans.modules.visualweb.ejb.load.EjbLoader;
import org.netbeans.modules.visualweb.ejb.nodes.EjbGroupNode;
import org.netbeans.modules.visualweb.ejb.nodes.EjbLibReferenceHelper;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * 
 * @author cao
 */
public class RefreshEjbGroupAction extends NodeAction {

    public RefreshEjbGroupAction() {
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return EjbLoaderHelper.isEnableAction();
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        // TODO help needed
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(RefreshEjbGroupAction.class, "REFRESH");
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            Node node = null;
            if (activatedNodes[0] instanceof FilterNode) {
                node = (Node) activatedNodes[0].getCookie(EjbGroupNode.class);
            } else {
                node = activatedNodes[0];
            }
            EjbGroup ejbGrp = ((EjbGroupNode) node).getEjbGroup();
            refresh(ejbGrp);
        }
    }

    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }

    private void refresh(EjbGroup ejbGroup) {
        // Refresh is to take care the case when the user has update the
        // client jar file with a new version. So, what we need to do here
        // is a) re-parse the DDs, b)re-generate the wrapper class

        // Clean up the session beans collection
        try {
            // Make a clone of the ejb group
            // The reload will be operated on the clone instead of the original copy
            // The reason for this is we do not want to mess up the original data
            // incase the reload fails
            EjbGroup grpClone = (EjbGroup) ejbGroup.clone();

            // Try to reload the EjbGroup.
            // If the reload went fine, then refresh the original group
            EjbLoader ejbLoader = new EjbLoader(grpClone);
            if (ejbLoader.reload()) {
                // Able to load the ejb group sucessfully
                // Refresh the group to the EjbDataModel
                EjbGroup modifiedGrp = ejbLoader.getEjbGroup();
                ejbGroup.setSessionBeans(modifiedGrp.getSessionBeans());
                ejbGroup.setEntityBeans(modifiedGrp.getEntityBeans());
                ejbGroup.setMDBs(modifiedGrp.getMDBs());
                EjbDataModel.getInstance().refreshEjbGroup(ejbGroup);
            }

            // Update the corresponding jars in the current open projects
            EjbLibReferenceHelper.updateEjbGroupForProjects(OpenProjects.getDefault()
                    .getOpenProjects(), ejbGroup);
        } catch (Exception e) {
            String msg = NbBundle.getMessage(RefreshEjbGroupAction.class, "FAILED_TO_LOAD_EJBS",
                    ejbGroup.getName());
            NotifyDescriptor d = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
}
