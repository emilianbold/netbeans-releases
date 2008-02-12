/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import java.io.BufferedReader;
import java.util.logging.Level;
import javax.swing.JComponent;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.awt.DynamicMenuContent;
import javax.swing.JMenuItem;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.netbeans.modules.groovy.grails.api.GrailsServerFactory;
import org.openide.awt.Actions;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.StreamInputThread;
import org.netbeans.modules.groovy.grailsproject.StreamRedirectThread;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

public final class GenerateAllAction extends NodeAction {
    
    private final Logger LOG = Logger.getLogger(GenerateAllAction.class.getName());
    
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        
        GrailsProject prj = (GrailsProject)FileOwnerQuery.getOwner(dataObject.getFolder().getPrimaryFile());
        String command = "generate-all " + dataObject.getPrimaryFile().getName();

        assert prj != null;
        new PrivateSwingWorker(prj, command).start();

    }

    public String getName() {
        return NbBundle.getMessage(GenerateAllAction.class, "CTL_GenerateAllAction");
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        
        if (dataObject == null) {
            return false;
        }
        String name = dataObject.getFolder().getName();
        return "domain".equals(name);
    }

    public JMenuItem getPopupPresenter() {
        class SpecialMenuItem extends JMenuItem implements DynamicMenuContent {

            public JComponent[] getMenuPresenters() {
                if(isEnabled()){
                    return new JComponent[] {this};
                    }
                else {
                    return new JComponent[] {};
                    }
            }
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                return getMenuPresenters();
            }
        }
        
        SpecialMenuItem menuItem = new SpecialMenuItem();
        
        Actions.connect(menuItem, (Action)this);
        return menuItem;
    }

    public class PrivateSwingWorker extends Thread {

        BufferedReader procOutput;
        OutputWriter writer = null;
        String command = null;
        GrailsProject prj = null;

        public PrivateSwingWorker(GrailsProject prj, String command) {
            this.prj = prj;
            this.command = command;
        }
        
        public void run() {

        try {
            String tabName = "Grails Server : " + prj.getProjectDirectory().getName() 
                                                + " (" + command +")";
            
            InputOutput io = IOProvider.getDefault().getIO(tabName, true);

            io.select();
            writer = io.getOut();

            GrailsServer server = GrailsServerFactory.getServer();
            Process process = server.runCommand(prj, command, io, null);

            if (process == null) {
                displayGrailsProcessError(server.getLastError());
                return;
            }

            assert process != null;

            (new StreamInputThread   (process.getOutputStream(), io.getIn())).start();
            (new StreamRedirectThread(process.getInputStream(),  io.getOut())).start();
            (new StreamRedirectThread(process.getErrorStream(),  io.getErr())).start();

            process.waitFor();

            } catch (Exception e) {
                LOG.log(Level.WARNING, "problem with process: " + e);
                LOG.log(Level.WARNING, "message " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        
    void displayGrailsProcessError(Exception reason) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
            "Problem creating Process: " + reason.getLocalizedMessage(),
            NotifyDescriptor.Message.WARNING_MESSAGE
            ));
        }        
        
    }
    
}

