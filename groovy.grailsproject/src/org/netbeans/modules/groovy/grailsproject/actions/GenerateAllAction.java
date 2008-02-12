/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grails.api.GrailsServer;
import org.netbeans.modules.groovy.grails.api.GrailsServerFactory;
import org.openide.awt.Actions;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

public final class GenerateAllAction extends NodeAction {
    
    private final Logger LOG = Logger.getLogger(GenerateAllAction.class.getName());
    GrailsProject prj = null;
    String command = null;
    
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        
        prj = (GrailsProject)FileOwnerQuery.getOwner(dataObject.getFolder().getPrimaryFile());
        command = "generate-all " + dataObject.getPrimaryFile().getName();

        assert prj != null;
        new PrivateSwingWorker(this).start();

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
        OutputWriter writer =  null;
        GenerateAllAction parent;

        public PrivateSwingWorker(GenerateAllAction parent) {
            this.parent = parent;
        }
        
        public void run() {

        try {
            String lineString = null;

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

            procOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            assert procOutput != null;
            assert writer != null;

            GrailsProjectConfig prjConfig = new GrailsProjectConfig(prj);
            
            while ((lineString = procOutput.readLine()) != null) {
                writer.println(lineString);
   
            }
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

