/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.makeproject.launchers.actions;

import org.netbeans.modules.cnd.makeproject.launchers.Launcher;
import org.netbeans.modules.cnd.makeproject.launchers.LauncherExecutor;
import org.netbeans.modules.cnd.makeproject.launchers.LaunchersRegistryFactory;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Henk
 */
public class LauncherAction extends AbstractAction implements ContextAwareAction, Presenter.Menu, Presenter.Popup{

    private Project project;
    private JMenu subMenu = null;
    private boolean isEnabled;
    private final ProjectActionEvent.PredefinedType actionType;
    private final String displayName;

    public LauncherAction(ProjectActionEvent.PredefinedType actionType, String displayName) {
        this.actionType = actionType;
        this.displayName = displayName; 
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, Boolean.TRUE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {        
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        this.project = actionContext.lookup(Project.class);
        isEnabled = true;
        if (project == null) {
            isEnabled = false;
        }         
        isEnabled =  isEnabled && ConfigurationSupport.getProjectActiveConfiguration(project) != null && 
                LaunchersRegistryFactory.getInstance(project.getProjectDirectory()).hasLaunchers();        
        return this;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
        
    
    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return subMenu;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }

    // This method is shared between multiple actions
    private void createSubMenu() {
        subMenu = new JMenu(displayName);
        subMenu.setEnabled(isEnabled);
        subMenu.putClientProperty(DynamicMenuContent.HIDE_WHEN_DISABLED, getValue(DynamicMenuContent.HIDE_WHEN_DISABLED));
        for (Launcher launcher : LaunchersRegistryFactory.getInstance(project.getProjectDirectory()).getLaunchers()) {
            subMenu.add(new LauncherExecutableAction(launcher));
        }
    }
    
    private class LauncherExecutableAction extends AbstractAction {

        private LauncherExecutor executor;

        public LauncherExecutableAction(Launcher launcher) {
            super(launcher.getDisplayedName());
            this.executor = LauncherExecutor.createExecutor(launcher, actionType);
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            executor.execute(project);
        }
    }    
    
    
    public static LauncherAction runAsAction() {
        return new LauncherAction(ProjectActionEvent.PredefinedType.RUN, NbBundle.getMessage(LauncherAction.class, "LBL_RunAsAction_Name"));//NOI18N
    }
    
    public static LauncherAction debugAsAction() {
        return new LauncherAction(ProjectActionEvent.PredefinedType.DEBUG, NbBundle.getMessage(LauncherAction.class, "LBL_DebugAsAction_Name"));//NOI18N
    }
    
    public static LauncherAction stepIntoAction() {
        return new LauncherAction(ProjectActionEvent.PredefinedType.DEBUG_STEPINTO, NbBundle.getMessage(LauncherAction.class, "LBL_StepIntoAction_Name"));//NOI18N
    }
}
