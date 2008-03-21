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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.compapp.projects.jbi.ui;

import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.JbiProjectCookie;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;

import org.openide.filesystems.*;

import org.openide.loaders.DataFolder;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

import java.awt.Image;
import java.awt.event.ActionEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;

import javax.swing.Action;
import org.netbeans.modules.compapp.projects.jbi.api.InternalProjectTypePlugin;
import org.netbeans.modules.compapp.projects.jbi.api.JbiInstalledProjectPluginInfo;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectActionPerformer;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;

/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class JbiModuleViewNode extends AbstractNode {

    private static Image JBI_MODULES_BADGE = Utilities.loadImage(
            "org/netbeans/modules/compapp/projects/jbi/ui/resources/compositeApplicationBadge.png", true); // NOI18N

    private final DataFolder aFolder;
    private final JbiProject project;
    private JbiModuleViewChildren children;
    private FileChangeAdapter projectXmlChangeAdapter;

    /**
     * Creates a new JbiModuleViewNode object.
     *
     * @param epp DOCUMENT ME!
     * @param project DOCUMENT ME!
     */
    public JbiModuleViewNode(JbiProjectProperties epp, JbiProject project) {
        super(new JbiModuleViewChildren(project));
        this.project = project;

        DataFolder projectFolder = null;
        FileObject projectDir = project.getProjectDirectory();

        if (projectDir.isFolder()) {
            projectFolder = DataFolder.findFolder(projectDir);
        }

        this.aFolder = projectFolder;

        // Set FeatureDescriptor stuff:
        setName("preferablyUniqueNameForThisNodeAmongSiblings"); // NOI18N or, super.setName if needed

        setDisplayName(NbBundle.getMessage(JbiModuleViewNode.class, "LBL_ModuleViewNode")); // NOI18N

        //setShortDescription(NbBundle.getMessage(JbiModuleViewNode.class, "HINT_LogicalViewNode"));
        getCookieSet().add(new JbiProjectCookie(project));

        // set the model listener
        projectXmlChangeAdapter = new FileChangeAdapter() {

            @Override
            public void fileChanged(FileEvent ev) {
                RequestProcessor.getDefault().post(
                        new Runnable() {

                            public void run() {
                                updateChildren();
                            }
                        });
            }
        };

        FileObject projectXmlFO = project.getProjectDirectory().
                getFileObject("nbproject/project.xml"); // NOI18N

        projectXmlFO.addFileChangeListener(
                FileUtil.weakFileChangeListener(projectXmlChangeAdapter, projectXmlFO));

        children = (JbiModuleViewChildren) this.getChildren();
    }

    private void updateChildren() {
        if (children != null) {
            children.addNotify();
        }

    //JbiModuleViewChildren ch = new JbiModuleViewChildren(project.getProjectProperties());
    //this.setChildren(ch);
    // ch.addNotify();
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    private Image computeIcon(boolean opened, int type) {
        if (aFolder != null) {
            Node folderNode = aFolder.getNodeDelegate();
            Image image = opened ? folderNode.getOpenedIcon(type) : folderNode.getIcon(type);

            return Utilities.mergeImages(image, JBI_MODULES_BADGE, 7, 7);
        } else {
            return JBI_MODULES_BADGE;
        }
    }

    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        ResourceBundle bundle = NbBundle.getBundle(JbiModuleViewNode.class);
        int actCnt = 0;
        Action[] actions = null;
        Action addJBIModule = new AbstractAction(bundle.getString("LBL_AddProjectAction_Name"), null) {

            public void actionPerformed(ActionEvent e) {
                new AddProjectAction().perform(project);
            }
        };

        // Add Actions added by other modules.        
        JbiInstalledProjectPluginInfo plugins = JbiInstalledProjectPluginInfo.getProjectPluginInfo();
        List<JbiProjectActionPerformer> pluginActions = new ArrayList<JbiProjectActionPerformer>();

        if (plugins != null) {
            List<InternalProjectTypePlugin> plist = plugins.getUncategorizedProjectPluginList();
            for (InternalProjectTypePlugin plugin : plist) {
                List<JbiProjectActionPerformer> acts = plugin.getProjectActions();
                for (JbiProjectActionPerformer act : acts) {
                    if (act.getActionType().equalsIgnoreCase(JbiProjectActionPerformer.ACT_ADD_PROJECT)) {
                        pluginActions.add(act);
                    }
                }
            }

            if (pluginActions.size() > 0) {
                actions = new Action[pluginActions.size() + 1];
                actions[actCnt++] = addJBIModule;
                for (final JbiProjectActionPerformer act : pluginActions) {
                    actions[actCnt++] = new AbstractAction( act.getLabel() 

                           , 
                            act.getIcon()
                        
                    
                
            
        
           ) {

                        public void actionPerformed(ActionEvent e) {
                            act.perform(project);
                        }
                    };
                }
            }
        }

        if (actions == null) {
            actions = new Action[1];
            actions[0] = addJBIModule;
        }

        return actions;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

    // When you have help, change to:
    // return new HelpCtx(LogicalViewNode.class);
    }

    private void log(String str) {
        OutputWriter out = IOProvider.getDefault().getStdOut();
        out.println(str);
        out.flush();
    }

    public JbiProject getProject() {
        return project;
    }
}
