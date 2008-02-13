/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

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
import org.openide.awt.Actions;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;

public final class GenerateAllAction extends NodeAction {
    
    private final Logger LOG = Logger.getLogger(GenerateAllAction.class.getName());
    
    protected void performAction(Node[] activatedNodes) {
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        
        GrailsProject prj = (GrailsProject)FileOwnerQuery.getOwner(dataObject.getFolder().getPrimaryFile());
        String command = "generate-all " + dataObject.getPrimaryFile().getName();

        assert prj != null;
        new PublicSwingWorker(prj, command).start();

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
}

