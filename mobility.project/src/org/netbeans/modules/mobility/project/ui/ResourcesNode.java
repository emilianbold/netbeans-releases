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
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.project.ProjectConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.openide.actions.PasteAction;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * The node that shows resources of a mobility project or one of its
 * configurations.
 */
final class ResourcesNode extends DecoratedNode implements ChangeListener, PropertyChangeListener, Runnable {

    private static final String PLATFORM_ICON =
            "org/netbeans/modules/mobility/cldcplatform/resources/platform.gif"; //NOI18N
    private final ResourcesChildren children;
    private final InstanceContent content;
    private final ProjectConfiguration config;
    private final L l = new L();

    protected ResourcesNode(J2MEProject project, ProjectConfiguration config) {
        this(project, config, new ResourcesChildren(project, config), new InstanceContent());
    }

    private ResourcesNode(J2MEProject project, ProjectConfiguration config, ResourcesChildren childFactory, InstanceContent content) {
        //Make a dynamic lookup containing the project and current configuration
        super(Children.create(childFactory, true), new ProxyLookup(Lookups.fixed(project,
                AbilitiesPanel.hintInstance),
                new AbstractLookup(content)));
        this.config = config;
        this.children = childFactory;
        this.content = content;
        assert children != null;
        assert project != null;
        content.set(Arrays.asList(config == null ? project.getConfigurationHelper().getActiveConfiguration() :
            config), null);
        setIconBaseWithExtension(PLATFORM_ICON);
        setDisplayName(NbBundle.getMessage(ResourcesNode.class,
                "LBL_NodeCache_Resources")); //NOI18N
        change();
        AntProjectHelper helper = project.getLookup().lookup(AntProjectHelper.class);
        PropertyProvider pp = helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        pp.addChangeListener(WeakListeners.change(this, pp));
        project.getConfigurationHelper().addPropertyChangeListener(
                WeakListeners.propertyChange(this, pp));

        helper.addAntProjectListener(WeakListeners.create (AntProjectListener.class, l, helper));
        JavaPlatformManager platformMgr = JavaPlatformManager.getDefault();
        platformMgr.addPropertyChangeListener(WeakListeners.propertyChange(l, platformMgr));
        LibraryManager lmgr = LibraryManager.getDefault();
        lmgr.addPropertyChangeListener(WeakListeners.propertyChange(this, l));
    }

    private class L implements PropertyChangeListener, AntProjectListener{
        public void propertyChange(PropertyChangeEvent evt) {
            checkBroken();
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            checkBroken();
        }

        public void propertiesChanged(AntProjectEvent ev) {
            checkBroken();
        }
    }
    
    @Override
    protected boolean isBroken() {
        J2MEProject project = getLookup().lookup (J2MEProject.class);
        boolean broken = project.isConfigBroken(config);
        return broken;
    }

    private void change() {
        //Called when configurations or libraries changed/updated
        J2MEProject project = getLookup().lookup(J2MEProject.class);
        assert project != null;
        //Update the configuration in the lookup - used by project actions
        ProjectConfiguration currConfig = this.config == null ? project.getConfigurationHelper().getActiveConfiguration() : this.config;
        //can be null if misconfigured
        content.set(currConfig == null ? Collections.EMPTY_SET : Collections.singleton(currConfig), null);
        //Update display name this later to avoid ProjectManager.MUTEX/Children.MUTEX conflict)
        EventQueue.invokeLater(this);
        //asynchronously have the children update
        children.update();
    }

    public void run() {
        updateDisplayName();
    }

    private void updateDisplayName() {
        J2MEProject project = getLookup().lookup(J2MEProject.class);
        assert project != null;
        boolean usingDefaultLibs = project.isUsingDefaultLibs(config);
        setValue(DecoratedNode.GRAY, usingDefaultLibs);
        if (usingDefaultLibs) {
            ProjectConfiguration def = project.getConfigurationHelper().getDefaultConfiguration();
            if (def != null) {
                String defaultCfgName =
                        project.getConfigurationHelper().getDefaultConfiguration().getDisplayName();
                setDisplayName(NbBundle.getMessage(ResourcesNode.class,
                        "LBL_NodeCache_InheritedResources", defaultCfgName)); //NOI18N
                return;
            } else {
                //No configuration - something is wrong - disable actions
                setValue(DecoratedNode.GRAY, true);
            }
        }
        //fall through if no config or using default libs
        setDisplayName(NbBundle.getMessage(ResourcesNode.class,
                "LBL_NodeCache_Resources")); //NOI18N
    }

    @Override
    public Action[] getActions(boolean ignored) {
        boolean gray = Boolean.TRUE.equals(getValue(DecoratedNode.GRAY));
        return gray ? new Action[0] : new Action[]{
                    NodeActions.AddProjectAction.getStaticInstance(),
                    NodeActions.AddJarAction.getStaticInstance(),
                    NodeActions.AddFolderAction.getStaticInstance(),
                    NodeActions.AddLibraryAction.getStaticInstance(),
                    null,
                    SystemAction.get(PasteAction.class),};
    }

    public void stateChanged(ChangeEvent e) {
        change();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        change();
    }

    @Override
    public Image getIcon(int type) {
        Image libBadge = ImageUtilities.loadImage(
                "org/netbeans/modules/mobility/project/ui/resources/" + //NOI18N
                "libraries-badge.png"); //NOI18N
        return ImageUtilities.mergeImages(super.getIcon(type), libBadge, 6, 8);
    }

    private PasteType getPasteType(final Transferable tr, DataFlavor[] flavors) {
        final String PRIMARY_TYPE = "application";   //NOI18N
        final String LIST_TYPE = "x-java-file-list"; //NOI18N
        final String DND_TYPE = "x-java-openide-nodednd"; //NOI18N
        final String MULTI_TYPE = "x-java-openide-multinode"; //NOI18N
        final HashSet<VisualClassPathItem> set = new HashSet<VisualClassPathItem>();

        class NDPasteType extends PasteType {

            public Transferable paste() throws IOException {
                if (set.size() != 0) {
                    NodeActions.NodeAction.pasteAction(set, ResourcesNode.this);
                    set.clear();
                }
                return tr;
            }
        }

        for (DataFlavor flavor : flavors) {
            if (PRIMARY_TYPE.equals(flavor.getPrimaryType())) {
                if (LIST_TYPE.equals(flavor.getSubType())) {
                    List<File> files;
                    try {
                        files = (List<File>) tr.getTransferData(flavor);
                        for (File file : files) {
                            final String s = file.getName().toLowerCase();
                            if (file.isDirectory()) {
                                file = FileUtil.normalizeFile(file);
                                set.add(new VisualClassPathItem(file,
                                        VisualClassPathItem.TYPE_FOLDER,
                                        null,
                                        file.getPath()));
                            } else if (s.endsWith(".zip") || s.endsWith(".jar")) { //NOI18N
                                file = FileUtil.normalizeFile(file);
                                set.add(new VisualClassPathItem(file,
                                        VisualClassPathItem.TYPE_JAR,
                                        null,
                                        file.getPath()));
                            } else {
                                set.clear();
                                continue;
                            }
                        }
                        return set.size() == 0 ? null : new NDPasteType();

                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        return null;
                    }

                }

                if (MULTI_TYPE.equals(flavor.getSubType())) {
                    Node nodes[] = NodeTransfer.nodes(tr, NodeTransfer.DND_COPY_OR_MOVE);
                    if (nodes == null) {
                        return null;
                    }
                    for (Node node : nodes) {
                        if (node != null && node.getValue("resource") != null) {
                            VisualClassPathItem item = (VisualClassPathItem) node.getValue("VCPI"); //NOI18N
                            if (item != null) {
                                set.add(item);
                            }
                        } //Node is not of correct type
                        else {
                            set.clear();
                            continue;
                        }
                    }
                    return set.size() == 0 ? null : new NDPasteType();
                }

                if (DND_TYPE.equals(flavor.getSubType())) {
                    Node node = NodeTransfer.node(tr, NodeTransfer.DND_COPY_OR_MOVE);
                    if (node != null && node.getValue("resource") != null) {
                        VisualClassPathItem item = (VisualClassPathItem) node.getValue("VCPI"); //NOI18N
                        if (item != null) {
                            set.add(item);
                        }
                    } //Node is not of correct type
                    else {
                        set.clear();
                        continue;
                    }
                    return set.size() == 0 ? null : new NDPasteType();
                }
            }
        }
        return null;
    }

    @Override
    public PasteType getDropType(Transferable tr, int action, int index) {
        final Boolean gray = Boolean.TRUE.equals(this.getValue(DecoratedNode.GRAY));
        if (!gray) {
            DataFlavor fr[] = tr.getTransferDataFlavors();
            PasteType type = getPasteType(tr, fr);
            return type;
        }
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        PasteType pt = getDropType(t, 0, 0);
        if (pt != null) {
            s.add(pt);
        }
    }
}