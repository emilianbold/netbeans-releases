package org.netbeans.modules.mobility.project.ui;

import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.modules.mobility.project.ui.customizer.CloneConfigurationPanel;
import org.netbeans.modules.mobility.project.ui.customizer.J2MEProjectProperties;
import org.netbeans.modules.mobility.project.ui.customizer.VisualConfigSupport;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.mobility.project.ProjectConfigurationsHelper.PROP_CONFIGURATION_ACTIVE;

final class OneConfigurationNode extends DecoratedNode implements PropertyChangeListener, AntProjectListener {
    private final ProjectConfiguration config;
    private static final String KEY_RESOURCES = "Resources"; //NOI18N

    OneConfigurationNode(J2MEProject project, ProjectConfiguration config) {
        super(Children.create(new ConfigurationChildren(project, config), false), Lookups.fixed(project, 
                config, AbilitiesPanel.hintInstance));
        this.config = config;
        setIconBaseWithExtension("org/netbeans/modules/mobility/project/ui/resources/config.gif"); //NOI18N
        String name = config.getDisplayName();
        setDisplayName(name);
        setName(name);
        ProjectConfiguration currConfig = project.getConfigurationHelper().getActiveConfiguration();
        setValue(BOLD, currConfig != null && name.equals(currConfig.getDisplayName()));
        ProjectConfigurationsHelper helper = project.getConfigurationHelper();
        helper.addPropertyChangeListener(WeakListeners.propertyChange(this, helper));

        JavaPlatformManager mgr = JavaPlatformManager.getDefault();
        mgr.addPropertyChangeListener(WeakListeners.propertyChange(this, mgr));
        LibraryManager lmgr = LibraryManager.getDefault();
        lmgr.addPropertyChangeListener(WeakListeners.propertyChange(this, lmgr));
        AntProjectHelper antHelper = project.getLookup().lookup(AntProjectHelper.class);
        antHelper.addAntProjectListener(WeakListeners.create (AntProjectListener.class, this, antHelper));
        checkBroken();
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        checkBroken();
    }

    public void propertiesChanged(AntProjectEvent ev) {
        checkBroken();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (PROP_CONFIGURATION_ACTIVE.equals(evt.getPropertyName())) {
            ProjectConfiguration active = (ProjectConfiguration) evt.getNewValue();
            boolean isActive = active != null && getName().equals(active.getDisplayName());
            setValue(BOLD, isActive);
        } else {
            checkBroken();
        }
    }

    @Override
    protected boolean isBroken() {
        J2MEProject project = getLookup().lookup(J2MEProject.class);
        boolean broken = project.isConfigBroken(config);
        return broken;
    }

    @Override
    public Action[] getActions(boolean ignored) {
        J2MEProject prj = getLookup().lookup (J2MEProject.class);
        ProjectConfigurationsHelper cfgHelper = prj.getConfigurationHelper();
        ProjectConfiguration defCfg = cfgHelper.getDefaultConfiguration();
        boolean isDefault = defCfg.equals(config);
        return new Action[]{
            NodeActions.RunConfigurationAction.getStaticInstance(),
            NodeActions.DebugConfigurationAction.getStaticInstance(),
            NodeActions.BuildConfigurationAction.getStaticInstance(),
            NodeActions.CleanAndBuildConfigurationAction.getStaticInstance(),
            NodeActions.CleanConfigurationAction.getStaticInstance(),
            NodeActions.DeployConfigurationAction.getStaticInstance(),
            null,
            SystemAction.get(CopyAction.class),
            null,
            NodeActions.SetConfigurationAction.getStaticInstance(),
            isDefault ? null : NodeActions.RemoveConfigurationAction.getStaticInstance()
        };
    }


    //XXX this is horrible and a likely leak - if you copy a configuration,
    //all projects in map are held until a paste.  If no paste...??? - Tim
    final Map<J2MEProject,HashSet<Node>> map=new WeakHashMap<J2MEProject,HashSet<Node>>();
    private PasteType pType;

    private PasteType getPasteType(final Transferable tr, DataFlavor[] flavors) {
        final String PRIMARY_TYPE = "application";   //NOI18N
        final String DND_TYPE = "x-java-openide-nodednd"; //NOI18N
        final String MULTI_TYPE = "x-java-openide-multinode"; //NOI18N

        class CfgPasteType extends PasteType {

            public Transferable paste() throws IOException {
                final J2MEProject projectDrop = getLookup().lookup(J2MEProject.class);
                if (projectDrop == null) {
                    return null;
                }
                final J2MEProjectProperties dropProperties = new J2MEProjectProperties(projectDrop,
                        projectDrop.getLookup().lookup(AntProjectHelper.class),
                        projectDrop.getLookup().lookup(ReferenceHelper.class),
                        projectDrop.getConfigurationHelper());
                final ArrayList<ProjectConfiguration> allNames = new ArrayList<ProjectConfiguration>(Arrays.asList(dropProperties.getConfigurations()));
                final int size = allNames.size();
                ProjectConfiguration oldCfg = null;
                ProjectConfiguration newCfg = null;

                for (J2MEProject project : map.keySet()) {
                    if (project == null) { //key cleared from WeakHashMap
                        continue;
                    }
                    HashSet<Node> set = map.get(project);
                    final ArrayList<String> allStrNames = new ArrayList<String>(allNames.size() + set.size());
                    final J2MEProjectProperties j2meProperties = new J2MEProjectProperties(project,
                            project.getLookup().lookup(AntProjectHelper.class),
                            project.getLookup().lookup(ReferenceHelper.class),
                            project.getConfigurationHelper());

                    for (ProjectConfiguration name : allNames) {
                        allStrNames.add(name.getDisplayName());
                    }

                    for (Node node : set) {
                        newCfg = oldCfg = node.getLookup().lookup(ProjectConfiguration.class);
                        //Check if configuration with the same name already exist
                        ProjectConfiguration exst = projectDrop.getConfigurationHelper().getConfigurationByName(oldCfg.getDisplayName());
                        if (exst != null) {
                            final CloneConfigurationPanel ccp = new CloneConfigurationPanel(allStrNames);
                            final DialogDescriptor dd = new DialogDescriptor(ccp, NbBundle.getMessage(VisualConfigSupport.class,
                                    "LBL_VCS_DuplConfiguration", oldCfg.getDisplayName()), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null); //NOI18N
                            ccp.setDialogDescriptor(dd);
                            final String newName = NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) ? ccp.getName() : null;
                            if (newName != null) {
                                newCfg = new ProjectConfiguration() {
                                    public String getDisplayName() {
                                        return newName;
                                    }
                                };
                                allStrNames.add(newName);
                            } else {
                                continue;
                            }
                        }
                        final String keys[] = j2meProperties.keySet().toArray(new String[j2meProperties.size()]);
                        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + oldCfg.getDisplayName();
                        for (int i = 0; i < keys.length; i++) {
                            if (keys[i].startsWith(prefix)) {
                                dropProperties.put(J2MEProjectProperties.CONFIG_PREFIX + newCfg.getDisplayName() + keys[i].substring(prefix.length()), j2meProperties.get(keys[i]));
                            }
                        }


                        allNames.add(newCfg);
                    }
                }
                map.clear();
                synchronized (CfgPasteType.this) {
                    pType = null;
                }
                //No configuration was added
                if (allNames.size() == size) {
                    return null;
                }

                dropProperties.setConfigurations(allNames.toArray(new ProjectConfiguration[allNames.size()]));
                // Store the properties
                final ProjectConfiguration lcfg = newCfg;

                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        assert lcfg != null;
                        try {
                            Children.MUTEX.writeAccess(new Runnable() {
                                public void run() {
                                    dropProperties.store();
                                }
                            });
                            projectDrop.getConfigurationHelper().setActiveConfiguration(lcfg);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
                return null;
            }
        }

        synchronized (this) {
            if (pType == null) {
                pType = new CfgPasteType();
            }
        }

        for (DataFlavor flavor : flavors) {
            if (PRIMARY_TYPE.equals(flavor.getPrimaryType())) {
                if (MULTI_TYPE.equals(flavor.getSubType())) {
                    Node nodes[] = NodeTransfer.nodes(tr, NodeTransfer.DND_COPY_OR_MOVE);
                    if (nodes == null) {
                        return null;
                    }
                    for (Node node : nodes) {
                        if (node instanceof OneConfigurationNode) {
                            J2MEProject project = node.getLookup().lookup(J2MEProject.class);
                            if (project != null) {
                                HashSet<Node> set = map.get(project);
                                if (set == null) {
                                    set = new HashSet<Node>();
                                    map.put(project, set);
                                }
                                set.add(node);
                            }
                        }
                    }
                    if (map.size() != 0) {
                        return pType;
                    }
                }
                if (DND_TYPE.equals(flavor.getSubType())) {
                    Node node = NodeTransfer.node(tr, NodeTransfer.DND_COPY_OR_MOVE);
                    if (node instanceof OneConfigurationNode) {
                        J2MEProject project = node.getLookup().lookup(J2MEProject.class);
                        if (project != null) {
                            HashSet<Node> set = map.get(project);
                            if (set == null) {
                                set = new HashSet<Node>();
                                map.put(project, set);
                            }
                            set.add(node);
                        }
                    }
                    if (map.size() != 0) {
                        return pType;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public PasteType getDropType(Transferable tr, int action, int index) {
        DataFlavor fr[] = tr.getTransferDataFlavors();
        PasteType type = getPasteType(tr, fr);
        return type;
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        PasteType pt = getDropType(t, 0, 0);
        if (pt != null) {
            s.add(pt);
        }
    }

    private static final class ConfigurationChildren extends ChildFactory <String> {
        private final J2MEProject project;
        private final ProjectConfiguration config;
        ConfigurationChildren (J2MEProject project, ProjectConfiguration config) {
            this.config = config;
            this.project = project;
        }

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            toPopulate.add (KEY_RESOURCES);
            return true;
        }

        @Override
        protected Node createNodeForKey(String key) {
            assert KEY_RESOURCES.equals(key);
            return new ResourcesNode(project, config);
        }
    }
}
