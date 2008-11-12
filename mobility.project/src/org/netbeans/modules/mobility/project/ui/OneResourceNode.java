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

import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.LibrariesSourceGroup;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import static org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem.TYPE_JAR;
import static org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem.TYPE_ARTIFACT;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.actions.CopyAction;
import org.openide.actions.EditAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.actions.ViewAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Node representing one JAR file, etc. under a ResourcesNode.
 *
 * @author Tim Boudreau
 */
final class OneResourceNode extends LazyInitFilterNode implements FileMonitor {

    static final String ARCHIVE_ICON = "org/netbeans/modules/mobility/project/ui/resources/libraries.gif"; //NOI18N
    private final File root;
    private ProjectConfiguration config;
    private boolean multi;

    OneResourceNode(J2MEProject project, ProjectConfiguration config, VisualClassPathItem item, File root, boolean multi) {
        this(Lookups.fixed(project, AbilitiesPanel.hintInstance, config == null ? project.getConfigurationHelper().getActiveConfiguration() : config, item), root);
        this.config = config;
        setValue("VCPI", item);
        setValue("resource", Boolean.TRUE);
        this.multi = multi;
        if (multi) {
            setDisplayName(NbBundle.getMessage(OneResourceNode.class, "LBL_JarFromMultiJarLibrary", new Object[]{ //NOI18N
                        item.getDisplayName(),
                        root.getName()
                    }));
        } else {
            setDisplayName(item.getDisplayName());
        }
    }

    private OneResourceNode(Lookup lookup, File root) {
        super(lookup);
        if (root == null) {
            throw new NullPointerException("Null root");
        }
        this.root = root;
        setValue("resource", Boolean.TRUE);
        setValue(DecoratedNode.ERROR, !root.exists());
        change();
        MissingFileDetector.register(root, this);
    }

    @Override
    public PropertySet[] getPropertySets() {
        Sheet.Set set = Sheet.createPropertiesSet();
        set.put(new NameProperty());
        set.put(new PathProperty());
        return new PropertySet[]{set};
    }

    @Override
    public Action[] getActions(boolean context) {
        J2MEProject project = getLookup().lookup(J2MEProject.class);
        assert project != null;
        boolean showActions = project.canModifyLibraries(config);
        return showActions ? new Action[]{
                    NodeActions.RemoveResourceAction.getStaticInstance(),
                    SystemAction.get(CopyAction.class),} : new Action[] {
                    SystemAction.get (CopyAction.class)
        };
    }

    @Override
    public Image getIcon(int type) {
        return getLookup().lookup(VisualClassPathItem.class).getImage();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    private void change() {
        boolean doesNotExist = !root.exists();
        setValue(DecoratedNode.ERROR, doesNotExist);
        boolean changeChildren = !isLazyChildren() && (Children.LEAF == getChildren() && !doesNotExist);
        if (changeChildren) {
            reinit();
        }
    }

    public void fileCreated() {
        change();
    }

    public void fileDeleted() {
        change();
    }

    public void fileChanged() {
        //will refresh children if active
        reinit();
    }

    @Override
    protected FilterNode.Children createRealChildren() {
        if (root == null) {
            //Called from superclass constructor because of hot-replace.
            //Returning null will cause the children swap to be run
            //later on the event queue
            return null;
        }
        FileObject file;
        try {
            file = FileUtil.toFileObject(root);
        } catch (NullPointerException e) {
            IllegalStateException ise = new IllegalStateException("NPE from " +
                    "toFileObject for " + root, e);
            throw ise;
        }
        assert root != null;
        if (file != null) {
            Node nd = findNodeForRoot(file);
            return new RemoveActionsFilterChildren(nd);
        } else {
            return new FilterNode.Children(new AbstractNode(createLazyChildren()));
        }
    }

    private Node findNodeForRoot(FileObject file) {
        Icon icon;
        Icon openedIcon;
        Node result = null;
        try {
            //Add a jar file
            if ("jar".equals(file.getURL().getProtocol())) { //NOI18N
                file = FileUtil.getArchiveFile(file);
                icon = openedIcon = new ImageIcon(ImageUtilities.loadImage(ARCHIVE_ICON));
                result = PackageView.createPackageView(new LibrariesSourceGroup(file, file.getNameExt(), icon, openedIcon));
            } else {
                if (file.isFolder()) {
                    result = DataFolder.findFolder(file).getNodeDelegate();
                } else {
                    try {
                        result = DataObject.find(file).getNodeDelegate();
                    } catch (DataObjectNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            final VisualClassPathItem item = getLookup().lookup(VisualClassPathItem.class);

            if (item != null) {
                Lookup lookup = getLookup();
                J2MEProject project = getLookup().lookup(J2MEProject.class);
                assert project != null;
                boolean usingDefaultLibs = project.isUsingDefaultLibs(config);
                result.setValue("VCPI", item);
                result = new FNode(result, lookup,
                        usingDefaultLibs ? null : new Action[0], item);
                result.setDisplayName(item.getDisplayName());
                result.setValue(DecoratedNode.GRAY, usingDefaultLibs);
                result.setValue("resource", "Resource");
            }
            return result;
        } catch (Exception e) {
            return new AbstractNode(createLazyChildren());
        }
    }


    private class NameProperty extends PropertySupport.ReadOnly<String> {

        NameProperty() {
            super("name", String.class, NbBundle.getMessage(NameProperty.class, "LBL_LibraryNameProperty"),
                    NbBundle.getMessage(NameProperty.class, "DESC_LibraryNameProperty"));
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            VisualClassPathItem item = getLookup().lookup(VisualClassPathItem.class);
            if (item.getType() == TYPE_JAR || item.getType() == TYPE_ARTIFACT) {
                String s = item.getRawText();
                int ix = s.lastIndexOf('/');
                if (ix > 0) {
                    s = s.substring(ix);
                }
                return s;
            } else {
                return item.getDisplayName();
            }
        }
    }

    private class PathProperty extends PropertySupport.ReadOnly<String> {

        PathProperty() {
            super("path", String.class, NbBundle.getMessage(NameProperty.class, "LBL_LibraryPathProperty"),
                    NbBundle.getMessage(NameProperty.class, "DESC_LibraryPathProperty"));
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return root.getPath();
        }
    }

    private static final class RemoveActionsFilterNode extends FilterNode {

        RemoveActionsFilterNode(Node n) {
            super(n);
        }

        @Override
        public Action[] getActions(boolean ignored) {
            List<Action> result = new LinkedList<Action>();
            if (getLookup().lookupItem(new Lookup.Template(OpenCookie.class)) != null) {
                result.add(SystemAction.get(OpenAction.class));
            }
            if (getLookup().lookupItem(new Lookup.Template(EditCookie.class)) != null) {
                result.add(SystemAction.get(EditAction.class));
            }
            if (getLookup().lookupItem(new Lookup.Template(ViewCookie.class)) != null) {
                result.add(SystemAction.get(ViewAction.class));
            }
            if (!result.isEmpty()) {
                result.add(null);
            }
            result.add(SystemAction.get(ToolsAction.class));
            result.add(SystemAction.get(PropertiesAction.class));
            Action[] actions = (Action[]) result.toArray(new Action[result.size()]);
            return actions;
        }
    }

    private static final class RemoveActionsFilterChildren extends FilterNode.Children {

        RemoveActionsFilterChildren(Node orig) {
            super(orig);
        }

        @Override
        protected Node copyNode(Node node) {
            return new RemoveActionsFilterNode(node);
        }
    }
}
