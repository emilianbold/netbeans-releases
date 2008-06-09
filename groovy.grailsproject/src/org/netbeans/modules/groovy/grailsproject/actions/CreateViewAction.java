/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.groovy.grailsproject.actions;

import java.awt.Dialog;
import java.util.Set;
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
import org.netbeans.modules.groovy.grails.api.GrailsRuntime;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.ui.wizards.NewArtifactWizardIterator;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.Actions;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;

public final class CreateViewAction extends NodeAction {
    
    private final Logger LOG = Logger.getLogger(CreateViewAction.class.getName());

    protected void performAction(Node[] activatedNodes) {
        final GrailsRuntime runtime = GrailsRuntime.getInstance();
        if (!runtime.isConfigured()) {
            ConfigSupport.showConfigurationWarning(runtime);
            return;
        }
        
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        GrailsProject project =  (GrailsProject)FileOwnerQuery.getOwner(dataObject.getFolder().getPrimaryFile());

        
        assert project != null;
        
        WizardDescriptor wiz =  null;

        String artifactName = dataObject.getPrimaryFile().getName();
        NewArtifactWizardIterator it = new NewArtifactWizardIterator(project, SourceCategory.VIEWS, artifactName);

        wiz = new WizardDescriptor(it);
        
        assert wiz != null;
        
        wiz.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
        wiz.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
        wiz.putProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N

        wiz.setTitleFormat(new java.text.MessageFormat("{0}")); // NOI18N
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog(wiz);
        
        try {
                dlg.setVisible(true);
                if (wiz.getValue() == WizardDescriptor.FINISH_OPTION) {
                    Set result = wiz.getInstantiatedObjects();
                }
            } finally {
                dlg.dispose();
            }
    }

    public String getName() {
        return NbBundle.getMessage(CreateViewAction.class, "CTL_CreateViewAction");
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
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
        // LOG.log(Level.WARNING, "CreateViewAction.enable(): " + name);
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

