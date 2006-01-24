package org.netbeans.modules.subversion.ui.browser;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.actions.CallableSystemAction;
import java.awt.Dialog;
import java.net.MalformedURLException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

public final class BrowserAction extends CallableSystemAction {
    
    public void performAction() {
        BrowserPanel panel = new BrowserPanel();
        
        panel.setup(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "LBL_RepositoryBrowser"), 
                    createRepositoryNode(), 
                    org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "ACSN_RepositoryTree"), 
                    org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "ACSD_RepositoryTree"));
        
        DialogDescriptor dd = new DialogDescriptor(panel, "test dialog");
        dd.setModal(true);
        dd.setValid(true);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            SVNUrl[] urls = panel.getSelectedURLs();
            for (int i = 0; i < urls.length; i++) {
                System.out.println(" url " + urls[i]);
            }
        } else {
            // XXX
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(BrowserAction.class, "CTL_BrowserAction");
    }
    
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    private Node createRepositoryNode() {
        ISVNClientAdapter svnClient;       
        try {   
            CmdLineClientAdapterFactory.setup();
        } catch (SVNClientException ex) {
            ex.printStackTrace();
        }
        svnClient = CmdLineClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
        SVNUrl svnURL = null;
        try {
            svnURL = new SVNUrl("http://peterp.czech.sun.com/svn"); //file:///data/subversion/");
        } catch (MalformedURLException ex) {
            ex.printStackTrace(); 
        }
        
        return RepositoryPathNode.create(svnClient, svnURL);
    }
}