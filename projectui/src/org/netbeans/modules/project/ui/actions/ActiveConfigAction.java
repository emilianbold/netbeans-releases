/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

package org.netbeans.modules.project.ui.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.Presenter;

/**
 * Action permitting selection of a configuration for the main project.
 * @author Greg Crawley, Adam Sotona, Jesse Glick
 */
@ActionID(id="org.netbeans.modules.project.ui.actions.ActiveConfigAction", category="Project")
@ActionRegistration(displayName="#ActiveConfigAction.label")
@ActionReferences({
    @ActionReference(path="Menu/BuildProject", position=300),
    @ActionReference(path="Toolbars/Build", position=80)
})
public class ActiveConfigAction extends CallableSystemAction implements LookupListener, PropertyChangeListener, ContextAwareAction {

    private static final Logger LOGGER = Logger.getLogger(ActiveConfigAction.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(ActiveConfigAction.class);

    private static final DefaultComboBoxModel EMPTY_MODEL = new DefaultComboBoxModel();
    private static final Object CUSTOMIZE_ENTRY = new Object();

    private final PropertyChangeListener lst;
    private final LookupListener looklst;
    private JComboBox configListCombo;
    private boolean listeningToCombo = true;

    private Project currentProject;
    private ProjectConfigurationProvider<?> pcp;
    @SuppressWarnings("rawtypes")
    private Lookup.Result<ProjectConfigurationProvider> currentResult;

    private final Lookup lookup;

    private void initConfigListCombo() {
        assert EventQueue.isDispatchThread();
        if (configListCombo != null) {
            return;
        }
        LOGGER.finest("initConfigListCombo");
        configListCombo = new JComboBox();
        configListCombo.addPopupMenuListener(new PopupMenuListener() {
            private Component prevFocusOwner = null;
            public @Override void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                prevFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                configListCombo.setFocusable(true);
                configListCombo.requestFocusInWindow();
            }
            public @Override void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                if (prevFocusOwner != null) {
                    prevFocusOwner.requestFocusInWindow();
                }
                prevFocusOwner = null;
                configListCombo.setFocusable(false);
            }
            public @Override void popupMenuCanceled(PopupMenuEvent e) {}
        });
        configListCombo.setRenderer(new ConfigCellRenderer());
        configListCombo.setToolTipText(org.openide.awt.Actions.cutAmpersand(getName()));
        configListCombo.setFocusable(false);
        configurationsListChanged(pcp == null ? null : getConfigurations(pcp));
        configListCombo.addActionListener(new ActionListener() {
            public @Override void actionPerformed(ActionEvent e) {
                if (!listeningToCombo) {
                    return;
                }
                Object o = configListCombo.getSelectedItem();
                if (o == CUSTOMIZE_ENTRY) {
                    activeConfigurationChanged(pcp != null ? getActiveConfiguration(pcp) : null);
                    pcp.customize();
                } else if (o != null) {
                    activeConfigurationSelected((ProjectConfiguration) o, null);
                }
            }
        });
    }

    @SuppressWarnings("LeakingThisInConstructor")
    public ActiveConfigAction() {
        super();
        putValue("noIconInMenu", true); // NOI18N
        EventQueue.invokeLater(new Runnable() {
            public @Override void run() {
                initConfigListCombo();
            }
        });
        lst = new PropertyChangeListener() {
            public @Override void propertyChange(PropertyChangeEvent evt) {
                if (ProjectConfigurationProvider.PROP_CONFIGURATIONS.equals(evt.getPropertyName())) {
                    configurationsListChanged(getConfigurations(pcp));
                } else if (ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName())) {
                    activeConfigurationChanged(getActiveConfiguration(pcp));
                }
            }
        };
        looklst = new LookupListener() {
            public @Override void resultChanged(LookupEvent ev) {
                activeProjectProviderChanged();
            }
        };

        OpenProjectList.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OpenProjectList.getDefault()));

        lookup = LookupSensitiveAction.LastActivatedWindowLookup.INSTANCE;
        Lookup.Result<Project> resultPrj = lookup.lookupResult(Project.class);
        Lookup.Result<DataObject> resultDO = lookup.lookupResult(DataObject.class);
        resultPrj.addLookupListener(WeakListeners.create(LookupListener.class, this, resultPrj));
        resultDO.addLookupListener(WeakListeners.create(LookupListener.class, this, resultDO));
        refreshView(lookup);
    }

    private synchronized void configurationsListChanged(Collection<? extends ProjectConfiguration> configs) {
        LOGGER.log(Level.FINER, "configurationsListChanged: {0}", configs);
        if (configs == null) {
            EventQueue.invokeLater(new Runnable() {
                public @Override void run() {
                    configListCombo.setModel(EMPTY_MODEL);
                    configListCombo.setEnabled(false); // possibly redundant, but just in case
                }
            });
        } else {
            final DefaultComboBoxModel model = new DefaultComboBoxModel(configs.toArray());
            if (pcp != null && pcp.hasCustomizer()) {
                model.addElement(CUSTOMIZE_ENTRY);
            }
            EventQueue.invokeLater(new Runnable() {
                public @Override void run() {
                    configListCombo.setModel(model);
                    configListCombo.setEnabled(true);
                }
            });
        }
        if (pcp != null) {
            activeConfigurationChanged(getActiveConfiguration(pcp));
        }
    }

    private synchronized void activeConfigurationChanged(final ProjectConfiguration config) {
        LOGGER.log(Level.FINER, "activeConfigurationChanged: {0}", config);
        EventQueue.invokeLater(new Runnable() {
            public @Override void run() {
                listeningToCombo = false;
                try {
                    configListCombo.setSelectedIndex(-1);
                    if (config != null) {
                        ComboBoxModel m = configListCombo.getModel();
                        for (int i = 0; i < m.getSize(); i++) {
                            if (config.equals(m.getElementAt(i))) {
                                configListCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } finally {
                    listeningToCombo = true;
                }
            }
        });
    }
    
    private synchronized void activeConfigurationSelected(final ProjectConfiguration cfg, final ProjectConfigurationProvider<?> ppcp) {
        final ProjectConfigurationProvider<?> lpcp = (ppcp != null) ? ppcp : pcp;
        RP.post(new Runnable() {
            @Override
            public void run() {
                LOGGER.log(Level.FINER, "activeConfigurationSelected: {0}", cfg);
                if (lpcp != null && cfg != null && !cfg.equals(getActiveConfiguration(lpcp))) {
                    try {
                        setActiveConfiguration(lpcp, cfg);
                    } catch (IOException x) {
                        LOGGER.log(Level.WARNING, null, x);
                    }
                }
            }
        });
    }
    
    public @Override HelpCtx getHelpCtx() {
        return new HelpCtx(ActiveConfigAction.class);
    }

    public @Override String getName() {
        return NbBundle.getMessage(ActiveConfigAction.class, "ActiveConfigAction.label");
    }

    public @Override void performAction() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public Component getToolbarPresenter() {
        // Do not return combo box directly; looks bad.
        JPanel toolbarPanel = new JPanel(new GridBagLayout());
        toolbarPanel.setOpaque(false); // don't interrupt JToolBar background
        toolbarPanel.setMaximumSize(new Dimension(150, 80));
        toolbarPanel.setMinimumSize(new Dimension(150, 0));
        toolbarPanel.setPreferredSize(new Dimension(150, 23));
        initConfigListCombo();
        // XXX top inset of 2 looks better w/ small toolbar, but 1 seems to look better for large toolbar (the default):
        toolbarPanel.add(configListCombo, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 6, 1, 5), 0, 0));
        return toolbarPanel;
    }

    class ConfigMenu extends JMenu implements DynamicMenuContent, ActionListener {

        private final Lookup context;

        @SuppressWarnings("LeakingThisInConstructor")
        public ConfigMenu(Lookup context) {
            this.context = context;
            if (context != null) {
                Mnemonics.setLocalizedText(this, NbBundle.getMessage(ActiveConfigAction.class, "ActiveConfigAction.context.label"));
            } else {
                Mnemonics.setLocalizedText(this, ActiveConfigAction.this.getName());
            }
        }

        private ProjectConfigurationProvider<?> findPCP() {
            if (context != null) {
                Collection<? extends Project> projects = context.lookupAll(Project.class);
                if (projects.size() == 1) {
                    return projects.iterator().next().getLookup().lookup(ProjectConfigurationProvider.class);
                } else {
                    // No selection, or multiselection.
                    return null;
                }
            } else {
                return ActiveConfigAction.this.pcp; // global menu item; take from main project
            }
        }
        
        public @Override JComponent[] getMenuPresenters() {
            removeAll();
            final ProjectConfigurationProvider<?> pcp = findPCP();
            if (pcp != null) {
                boolean something = false;
                ProjectConfiguration activeConfig = getActiveConfiguration(pcp);
                for (final ProjectConfiguration config : getConfigurations(pcp)) {
                    JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(config.getDisplayName(), config.equals(activeConfig));
                    jmi.addActionListener(new ActionListener() {
                        public @Override void actionPerformed(ActionEvent e) {
                            activeConfigurationSelected(config, findPCP());
                        }
                    });
                    add(jmi);
                    something = true;
                }
                if (pcp.hasCustomizer()) {
                    if (something) {
                        addSeparator();
                    }
                    something = true;
                    JMenuItem customize = new JMenuItem();
                    Mnemonics.setLocalizedText(customize, NbBundle.getMessage(ActiveConfigAction.class, "ActiveConfigAction.customize"));
                    customize.addActionListener(this);
                    add(customize);
                }
                setEnabled(something);
            } else {
                // No configurations supported for this project.
                setEnabled(false);
                // to hide entirely just use: return new JComponent[0];
            }
            return new JComponent[] {this};
        }

        public @Override JComponent[] synchMenuPresenters(JComponent[] items) {
            // Always rebuild submenu.
            // For performance, could try to reuse it if context == null and nothing has changed.
            return getMenuPresenters();
        }

        public @Override void actionPerformed(ActionEvent e) {
            ProjectConfigurationProvider<?> pcp = findPCP();
            if (pcp != null) {
                pcp.customize();
            }
        }

    }

    @Override
    public JMenuItem getMenuPresenter() {
        return new ConfigMenu(null);
    }

    @SuppressWarnings("serial")
    private static class ConfigCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        private Border defaultBorder = getBorder();
        
        public ConfigCellRenderer () {
            setOpaque(true);
        }

        public @Override Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            String label = null;
            if (value instanceof ProjectConfiguration) {
                label = ((ProjectConfiguration) value).getDisplayName();
                setBorder (defaultBorder);
            } else if (value == CUSTOMIZE_ENTRY) {
                label = org.openide.awt.Actions.cutAmpersand(
                        NbBundle.getMessage(ActiveConfigAction.class, "ActiveConfigAction.customize"));
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(1, 0, 0, 0,
                        UIManager.getColor("controlDkShadow")), defaultBorder));
            } else {
                assert value == null;
                label = null;
                setBorder (defaultBorder);
            }
            
            setText(label);
            setIcon(null);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }

    private synchronized void activeProjectChanged(Project p) {
        LOGGER.log(Level.FINER, "activeProjectChanged: {0} -> {1}", new Object[] {currentProject, p});
        if (currentResult != null) {
            currentResult.removeLookupListener(looklst);
        }
        currentResult = null;
        if (currentProject != p) {
            if (pcp != null) {
                pcp.removePropertyChangeListener(lst);
            }
            currentProject = p;
            if (currentProject != null) {
                currentResult = currentProject.getLookup().lookupResult(ProjectConfigurationProvider.class);
                pcp = currentResult.allInstances().isEmpty() ? null : currentResult.allInstances().iterator().next();
                currentResult.addLookupListener(looklst);
                if (pcp != null) {
                    pcp.addPropertyChangeListener(lst);
                } else {
                    LOGGER.log(Level.FINEST, "currentResult on {0} is empty", currentProject);
                }
            } else {
                LOGGER.finest("currentProject is null");
                pcp = null;
            }
            configurationsListChanged(pcp == null ? null : getConfigurations(pcp));

        }
    }
    
    private synchronized void activeProjectProviderChanged() {
        if (currentResult != null) {
            if (pcp != null) {
                pcp.removePropertyChangeListener(lst);
            }
            @SuppressWarnings("rawtypes")
            Collection<? extends ProjectConfigurationProvider> all = currentResult.allInstances();
            pcp = all.isEmpty() ? null : all.iterator().next();
            if (pcp != null) {
                pcp.addPropertyChangeListener(lst);
            } else {
                LOGGER.finest("currentResult is empty");
            }
            configurationsListChanged(pcp == null ? null : getConfigurations(pcp));
        }
    }
    

    public @Override Action createContextAwareInstance(final Lookup actionContext) {
        @SuppressWarnings("serial")
        class A extends AbstractAction implements Presenter.Popup {
            public @Override void actionPerformed(ActionEvent e) {
                assert false;
            }
            public @Override JMenuItem getPopupPresenter() {
                return new ConfigMenu(actionContext);
            }
        }
        return new A();
    }

    private static Collection<? extends ProjectConfiguration> getConfigurations(final ProjectConfigurationProvider<?> pcp) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Collection<? extends ProjectConfiguration>>() {
            public @Override Collection<? extends ProjectConfiguration> run() {
                Collection<? extends ProjectConfiguration> configs = pcp.getConfigurations();
                assert configs != null : pcp;
                return configs;
            }
        });
    }

    private static ProjectConfiguration getActiveConfiguration(final ProjectConfigurationProvider<?> pcp) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<ProjectConfiguration>() {
            public @Override ProjectConfiguration run() {
                return pcp.getActiveConfiguration();
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // XXX would not be necessary in case PCP had a method to get run-time type information: Class<C> configurationType();
    private static void setActiveConfiguration(ProjectConfigurationProvider<?> pcp, final ProjectConfiguration pc) throws IOException {
        final ProjectConfigurationProvider _pcp = pcp;
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws IOException {
                    _pcp.setActiveConfiguration(pc);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }

    private void refreshView(Lookup context) {
        // #185033: see MainProjectAction for basic logic.
        Project p = OpenProjectList.getDefault().getMainProject();
        if (p != null) {
            activeProjectChanged(p);
        } else {
            Project[] selected = ActionsUtil.getProjectsFromLookup(context, null);
            if (selected.length == 1) {
                activeProjectChanged(selected[0]);
            } else {
                Project[] open = OpenProjectList.getDefault().getOpenProjects();
                if (open.length == 1) {
                    activeProjectChanged(open[0]);
                } else {
                    activeProjectChanged(null);
                }
            }
        }
    }

    public @Override void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjectList.PROPERTY_MAIN_PROJECT) ||
            evt.getPropertyName().equals(OpenProjectList.PROPERTY_OPEN_PROJECTS) ) {
            refreshViewLater();
        }
    }

    public @Override void resultChanged(LookupEvent ev) {
        refreshViewLater();
    }

    private void refreshViewLater() {
        RP.post(new Runnable() {
            public @Override void run() {
                refreshView(lookup);
            }
        });
    }

}
