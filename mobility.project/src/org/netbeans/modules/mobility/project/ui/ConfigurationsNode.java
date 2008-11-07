/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mobility.project.ui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
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
import org.openide.actions.PasteAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.mobility.project.ProjectConfigurationsHelper.PROP_CONFIGURATIONS;

/**
 * Parent node for all project configurations under a mobility project.
 *
 * @author Tim Boudreau
 */
final class ConfigurationsNode extends DecoratedNode implements PropertyChangeListener, AntProjectListener {
    private final ConfigurationsChildren kids;

    ConfigurationsNode(J2MEProject project) {
        this(project, new ConfigurationsChildren(project));
    }

    private ConfigurationsNode(J2MEProject project, ConfigurationsChildren kids) {
        super(Children.create(kids, true), Lookups.singleton(project));
        this.kids = kids;
        setDisplayName(NbBundle.getMessage(ConfigurationsNode.class, "LBL_ProjectConfigurations")); //NOI18N
        setIconBaseWithExtension("org/netbeans/modules/mobility/project/ui/resources/configs.gif"); //NOI18N
        ProjectConfigurationsHelper configHelper = project.getConfigurationHelper();
        configHelper.addPropertyChangeListener(WeakListeners.propertyChange(this, configHelper));
        AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
        helper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, helper));
        JavaPlatformManager platformMgr = JavaPlatformManager.getDefault();
        platformMgr.addPropertyChangeListener(WeakListeners.propertyChange(this, platformMgr));
        checkBroken();
    }

    @Override
    public Action[] getActions(boolean ignored) {
        if (getLookup().lookup(Project.class).getProjectDirectory().isValid()) {
            return new Action[]{
                        NodeActions.AddConfigurationAction.getStaticInstance(),
                        null,
                        NodeActions.SelectConfigurationAction.getStaticInstance(),
                        null,
                        SystemAction.get(PasteAction.class)
                    };
        } else {
            return new Action[0];
        }
    }

    public void configurationXmlChanged(final AntProjectEvent ev) {
        checkBroken();
    }

    public void propertiesChanged(final AntProjectEvent ev) {
        checkBroken();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof ProjectConfigurationsHelper) {
            if (PROP_CONFIGURATIONS.equals(evt.getPropertyName()) && isAlive()) {
                kids.update();
            }
        }
        checkBroken();
    }

    @Override
    protected boolean isBroken() {
        J2MEProject project = getLookup().lookup(J2MEProject.class);
        return project.hasBrokenLinks();
    }
    
    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }
    
    private final Map<J2MEProject, HashSet<Node>> map = new HashMap<J2MEProject, HashSet<Node>>();
    private PasteType pType;

    private static class PrototypeConfiguration implements ProjectConfiguration {
        private final String name;
        PrototypeConfiguration(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return name;
        }
    }

    private PasteType getPasteType(final Transferable tr, DataFlavor[] flavors) {
        final String PRIMARY_TYPE = "application";   //NOI18N
        final String DND_TYPE = "x-java-openide-nodednd"; //NOI18N
        final String MULTI_TYPE = "x-java-openide-multinode"; //NOI18N


        class CfgPasteType extends PasteType {

            public Transferable paste() throws IOException {
                final J2MEProject targetProject = ConfigurationsNode.this.getLookup().lookup(J2MEProject.class);
                if (targetProject == null) {
                    return null;
                }
                final J2MEProjectProperties targetProperties = new J2MEProjectProperties(targetProject,
                        targetProject.getLookup().lookup(AntProjectHelper.class),
                        targetProject.getLookup().lookup(ReferenceHelper.class),
                        targetProject.getConfigurationHelper());
                final ArrayList<ProjectConfiguration> allNames = new ArrayList<ProjectConfiguration>(Arrays.asList(targetProperties.getConfigurations()));
                final int size = allNames.size();
                ProjectConfiguration oldCfg = null;
                ProjectConfiguration newCfg = null;

                for (J2MEProject srcProject : map.keySet()) {
                    HashSet<Node> set = map.get(srcProject);
                    final ArrayList<String> allStrNames = new ArrayList<String>(allNames.size() + set.size());
                    final J2MEProjectProperties j2meProperties = new J2MEProjectProperties(srcProject,
                            srcProject.getLookup().lookup(AntProjectHelper.class),
                            srcProject.getLookup().lookup(ReferenceHelper.class),
                            srcProject.getConfigurationHelper());

                    for (ProjectConfiguration name : allNames) {
                        allStrNames.add(name.getDisplayName());
                    }

                    for (Node node : set) {
                        newCfg = oldCfg = node.getLookup().lookup(ProjectConfiguration.class);
                        boolean usingDefaultLibs = srcProject.isUsingDefaultLibs(newCfg);

                        //Check if configuration with the same name already exist
                        ProjectConfiguration exst = targetProject.getConfigurationHelper().getConfigurationByName(oldCfg.getDisplayName());
                        if (exst != null) {
                            final CloneConfigurationPanel ccp = new CloneConfigurationPanel(allStrNames);
                            final DialogDescriptor dd = new DialogDescriptor(ccp, 
                                    NbBundle.getMessage(VisualConfigSupport.class,
                                    "LBL_VCS_DuplConfiguration", //NOI18N
                                    oldCfg.getDisplayName()), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null);
                            ccp.setDialogDescriptor(dd);
                            final String newName = NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dd)) ? ccp.getName() : null;
                            if (newName != null) {
                                newCfg = new PrototypeConfiguration (newName);
                                allStrNames.add(newName);
                            } else {
                                continue;
                            }
                        }
                        final String keys[] = j2meProperties.keySet().toArray(new String[j2meProperties.size()]);
                        final String prefix = J2MEProjectProperties.CONFIG_PREFIX + oldCfg.getDisplayName();
                        for (int i = 0; i < keys.length; i++) {
                            if (keys[i].startsWith(prefix)) {
                                targetProperties.put(J2MEProjectProperties.CONFIG_PREFIX + newCfg.getDisplayName() + keys[i].substring(prefix.length()), j2meProperties.get(keys[i]));
                            }
                        }
                        //If this config inherits from default config on
                        //foreign project, need to dup the foreign project's
                        //default cp config, or this will just create a new
                        //entry that inherits from the local default config
                        if (usingDefaultLibs) {
                            targetProperties.put(J2MEProjectProperties.CONFIG_PREFIX + newCfg.getDisplayName() + ".libs.classpath", j2meProperties.get("libs.classpath"));
                            targetProperties.put(J2MEProjectProperties.CONFIG_PREFIX + newCfg.getDisplayName() + ".extra.classpath", j2meProperties.get("extra.classpath"));
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

                targetProperties.setConfigurations(allNames.toArray(new ProjectConfiguration[allNames.size()]));
                // Store the properties
                final ProjectConfiguration lcfg = newCfg;

                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        assert lcfg != null;
                        try {
                            Children.MUTEX.writeAccess(new Runnable() {

                                public void run() {
                                    targetProperties.store();
                                }
                            });
                            targetProject.getConfigurationHelper().setActiveConfiguration(lcfg);
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
}
