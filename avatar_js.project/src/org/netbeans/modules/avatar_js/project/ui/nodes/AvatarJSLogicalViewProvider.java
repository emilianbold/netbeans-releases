/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project.ui.nodes;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.avatar_js.project.AvatarJSProject;
import org.netbeans.modules.java.api.common.project.ui.LogicalViewProvider2;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Martin
 */
public class AvatarJSLogicalViewProvider implements LogicalViewProvider2 {
    
    private final AvatarJSProject project;
    
    public AvatarJSLogicalViewProvider(AvatarJSProject project) {
        this.project = project;
    }

    @Override
    public Node createLogicalView() {
        InstanceContent ic = createInstanceContent();
        return new RootNode(new AbstractLookup(ic));
    }

    @Override
    public Node findPath(Node root, Object target) {
        Project prj = root.getLookup().lookup(Project.class);
        if (prj == null) {
            return null;
        }

        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;
            if (isOtherProjectSource(fo, prj)) {
                return null; // Don't waste time if project does not own the fo among sources
            }

            for (Node n : root.getChildren().getNodes(true)) {
                Node result = PackageView.findPath(n, target);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
    
    private static boolean isOtherProjectSource(
            @NonNull final FileObject fo,
            @NonNull final Project me) {
        final Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            return false;
        }
        if (me.equals(owner)) {
            return false;
        }
        for (SourceGroup sg : ProjectUtils.getSources(owner).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (FileUtil.isParentOf(sg.getRootFolder(), fo)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void testBroken() {
        
    }
    
    private InstanceContent createInstanceContent() {
        final InstanceContent ic = new InstanceContent();
        ic.add(project);
        ic.add(project, new InstanceContent.Convertor<Project, FileObject>() {
            @Override
            public FileObject convert(Project obj) {
                return obj.getProjectDirectory();
            }
            @Override
            public Class<? extends FileObject> type(Project obj) {
                return FileObject.class;
            }
            @Override
            public String id(Project obj) {
                final FileObject fo = obj.getProjectDirectory();
                return fo == null ? "" : fo.getPath();  //NOI18N
            }
            @Override
            public String displayName(Project obj) {
                return obj.toString();
            }
        });
        ic.add(project, new InstanceContent.Convertor<Project, DataObject>() {
            @Override
            public DataObject convert(Project obj) {
                try {
                    final FileObject fo = obj.getProjectDirectory();
                    return fo == null ? null : DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    return null;
                }
            }
            @Override
            public Class<? extends DataObject> type(Project obj) {
                return DataObject.class;
            }
            @Override
            public String id(Project obj) {
                final FileObject fo = obj.getProjectDirectory();
                return fo == null ? "" : fo.getPath();  //NOI18N
            }
            @Override
            public String displayName(Project obj) {
                return obj.toString();
            }
        });
        return ic;
    }
    
    private class RootNode extends AbstractNode {
        
        private final ProjectInformation info;
        
        public RootNode(Lookup lkp) {
            super(NodeFactorySupport.createCompositeChildren(
                        project,
                        "Projects/"+AvatarJSProject.ID+"/Nodes"),  //NOI18N
                  lkp);
            final ProjectInformation pi = ProjectUtils.getInformation(project);
            info = pi;
        }
        
        @NbBundle.Messages({"# {0} - Path of the project directory",
                            "HINT_project_root_node=Avatar.js project in {0}"})
        @Override
        public String getShortDescription() {
            String prjDirDispName = FileUtil.getFileDisplayName(project.getProjectDirectory());
            return Bundle.HINT_project_root_node(prjDirDispName);
        }
        
        @Override
        public Action[] getActions( boolean context ) {
            return CommonProjectActions.forType(AvatarJSProject.ID);
        }
        
        @Override
        public String getName() {
            return info.getDisplayName();
        }
        
        @Override
        public Image getIcon(int type) {
            final Icon icon = info.getIcon();
            final Image img = icon == null ?
                super.getIcon(type) :
                ImageUtilities.icon2Image(icon);
            /*return !broken && compileOnSaveDisabled ?
                ImageUtilities.mergeImages(img, compileOnSaveDisabledBadge, 8, 0) :
                img;*/
            return img;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(AvatarJSProject.TYPE+".ui.AvatarJSLogicalViewProvider.AvatarJSLogicalViewRootNode");    //NOI18N
        }
    }

}
