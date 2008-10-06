/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.bluej.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.bluej.BluejProject;
import org.netbeans.bluej.export.ConvertToJ2SEAction;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.ErrorManager;
import org.openide.actions.FindAction;
import org.openide.actions.ToolsAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author mkleint
 */
public class BluejLogicalViewRootNode extends AbstractNode {
    
    /** Creates a new instance of BluejLogicalViewRootNode */
    public BluejLogicalViewRootNode(Lookup look) {
        super(new FilterChildrenImpl(look), look);
        setIconBaseWithExtension("org/netbeans/bluej/resources/bluejproject.png"); // NOI18N
    }
    
    public String getName() {
        return getProjectInfo(getLookup()).getName();
    }
    
    public String getDisplayName() {
        return getProjectInfo(getLookup()).getDisplayName();
    }
    
    private static BluejProject getProject(Lookup lkp) {
        return (BluejProject)lkp.lookup(BluejProject.class);
    }
    
    private static ProjectInformation getProjectInfo(Lookup lkp) {
        return (ProjectInformation)getProject(lkp).getLookup().lookup(ProjectInformation.class);
    }
    
    
    private static SourceGroup getSourceGroup(Lookup lkp) {
        BluejProject prj = getProject(lkp);
        Sources srcs = ProjectUtils.getSources(prj);
        SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        assert grps.length > 0;
        return grps[0];
    }
    
    public Action[] getActions(boolean context) {
        
        ResourceBundle bundle = NbBundle.getBundle(BluejLogicalViewRootNode.class);
        
        List actions = new ArrayList();
        
        actions.add(CommonProjectActions.newFileAction());
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_BUILD, bundle.getString("LBL_BuildAction_Name"), null)); // NOI18N
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_REBUILD, bundle.getString("LBL_RebuildAction_Name"), null)); // NOI18N
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_CLEAN, bundle.getString("LBL_CleanAction_Name"), null)); // NOI18N
        actions.add(ProjectSensitiveActions.projectCommandAction(JavaProjectConstants.COMMAND_JAVADOC, bundle.getString("LBL_JavadocAction_Name"), null)); // NOI18N
        actions.add(null);
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_RUN, bundle.getString("LBL_RunAction_Name"), null)); // NOI18N
        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_DEBUG, bundle.getString("LBL_DebugAction_Name"), null)); // NOI18N
////        actions.add(ProjectSensitiveActions.projectCommandAction(ActionProvider.COMMAND_TEST, bundle.getString("LBL_TestAction_Name"), null)); // NOI18N
        actions.add(null);
         actions.add(new ConvertToJ2SEAction(getProject(getLookup())));
        actions.add(CommonProjectActions.setAsMainProjectAction());
//        actions.add(CommonProjectActions.openSubprojectsAction());
        actions.add(CommonProjectActions.closeProjectAction());
        actions.add(null);
        actions.add(SystemAction.get(FindAction.class));
        
        // honor 57874 contact
        
        try {
            FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource("Projects/Actions"); // NOI18N
            if (fo != null) {
                DataObject dobj = DataObject.find(fo);
                FolderLookup actionRegistry = new FolderLookup((DataFolder)dobj);
                Lookup.Template query = new Lookup.Template(Object.class);
                Lookup lookup = actionRegistry.getLookup();
                Iterator it = lookup.lookup(query).allInstances().iterator();
                if (it.hasNext()) {
                    actions.add(null);
                }
                while (it.hasNext()) {
                    Object next = it.next();
                    if (next instanceof Action) {
                        actions.add(next);
                    } else if (next instanceof JSeparator) {
                        actions.add(null);
                    }
                }
            }
        } catch (DataObjectNotFoundException ex) {
            // data folder for existing fileobject expected
            ErrorManager.getDefault().notify(ex);
        }
        
        actions.add(null);
        actions.add(SystemAction.get(ToolsAction.class));
        actions.add(null);
        actions.add(CommonProjectActions.customizeProjectAction());
        
        return (Action[]) actions.toArray(new Action[actions.size()]);
    }
    
    private static class FilterChildrenImpl extends FilterNode.Children {
        FilterChildrenImpl(Lookup lkp) {
            this(PackageView.createPackageView(getSourceGroup(lkp)));
        }
        
        FilterChildrenImpl(Node original) {
            super(original);
        }
        
        protected Node[] createNodes(Node orig) {
            DataObject dobj = (DataObject)orig.getLookup().lookup(DataObject.class);
            if (dobj != null) {
                FileObject fo = dobj.getPrimaryFile();
                if ("bluej.pkg".equals(fo.getNameExt()) || // NOI18N
                        "build.xml".equals(fo.getNameExt()) || // NOI18N
                        "bluej.pkh".equals(fo.getNameExt()) || // NOI18N
                        ("+libs".equals(fo.getName()) && fo.isFolder()) || // NOI18N
                        "ctxt".equals(fo.getExt()) || // NOI18N
                        "class".equals(fo.getExt()) || // NOI18N
                        ".DS_STORE".equals(fo.getNameExt()) || // NOI18N
                        (fo.isFolder() && fo.getFileObject("bluej.pkg") == null)) { // NOI18N
                    return new Node[0];
                }
                  return new Node[] {new MyFilterWithHtml(orig, new FilterChildrenImpl(orig))};
            }
            return new Node[] {new FilterNode(orig)};
        }
        
        
        public void doRefresh(Node original) {
            refreshKey(original);
        }
    }
    
    private static class MyFilterWithHtml extends FilterNode {
        MyFilterWithHtml(Node orig, Children children) {
            super(orig, children);
        }
        
        MyFilterWithHtml(Node orig) {
            super(orig);
        }

        public String getHtmlDisplayName() {
            //for some reason the delegating to package view "<default package>" returns the
            // value in htmdisplayname and causes an error. workarounding..
            return null;
        }
        
    }
    
    
}
