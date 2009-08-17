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

import java.io.IOException;
import java.io.File;

import org.netbeans.modules.compapp.projects.jbi.ui.actions.DeleteModuleAction;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.api.JbiInstalledProjectPluginInfo;
import org.netbeans.modules.compapp.projects.jbi.api.InternalProjectTypePlugin;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectActionPerformer;
import org.netbeans.api.project.Project;
import org.openide.actions.DeleteAction;
import org.openide.actions.CustomizeAction;

import org.openide.nodes.*;

import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileUtil;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;


/**
 * A simple node with no children. Often used in conjunction with some kind of underlying data
 * model, where each node represents an element in that model. In this case, you should see the
 * Container Node template which will permit you to create a whole tree of such nodes with the
 * proper behavior.
 *
 * @author Tientien Li
 */
public class JbiModuleNode extends AbstractNode implements Node.Cookie {
    // static private Action[] actions = null;
    List<Action> actions = null;

    private VisualClassPathItem model;
    private InternalProjectTypePlugin plugin;
    private Project suProj;

    // will frequently accept an element from some data model in the constructor:
    public JbiModuleNode(VisualClassPathItem key) {
        super(Children.LEAF);
        model = key;
        setName("preferablyUniqueNameForThisNodeAmongSiblings"); // NOI18N or, super.setName if needed
        setDisplayName(key.getShortName());
        suProj = model.getAntArtifact().getProject();
        if (suProj != null) {
            plugin = JbiInstalledProjectPluginInfo.getPlugin(suProj);
        }
        //setShortDescription(NbBundle.getMessage(JbiModuleNode.class, "HINT_ModuleNode"));
    }

    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        if (null == actions) {
            actions = new ArrayList<Action>();
            actions.add(SystemAction.get(DeleteAction.class));
            if ((plugin != null) && (plugin.hasCustomizer())) {
                actions.add(SystemAction.get(CustomizeAction.class));
            }
            getCookieSet().add(this);
        }

        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();

        // remove internal su project files...
        if (plugin != null) {
            List<JbiProjectActionPerformer> acts = plugin.getProjectActions();
            for (JbiProjectActionPerformer act : acts) {
                if (act.getActionType().equalsIgnoreCase(JbiProjectActionPerformer.ACT_DELETE_PROJECT)) {
                    // perform plug-in specific delete action...
                    act.perform(suProj);
                    return;
                }
            }
        }

        DeleteModuleAction deleteModuleAction =
                SystemAction.get(DeleteModuleAction.class);
        deleteModuleAction.performAction(new Node[] {this});

        // clean up the internal plug-in project generated files..
        if (this.getParentNode() != null) { // TMP fix for NPE
            JbiProject jbiProj = ((JbiModuleViewNode) this.getParentNode()).getProject();
            if (JbiInstalledProjectPluginInfo.isInternalSubproject(jbiProj, suProj)) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        boolean ok = deleteDir(FileUtil.toFile(suProj.getProjectDirectory()));
                    }
                });
            }
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }


    private Image getProjIcon(){
        Icon ic = null;
        Image ret = null;
        if (this.model != null){
            ic = this.model.getProjectIcon();
            if (ic instanceof ImageIcon){
                ret = ((ImageIcon)ic).getImage();
            }
        }

        return ret;
    }

    @Override
    public boolean hasCustomizer() {
        if ((plugin != null) && (plugin.hasCustomizer())) {
            return true;
        }
        return false;
    }

    @Override
    public Component getCustomizer() {
        if ((plugin != null) && (plugin.hasCustomizer())) {
            return plugin.getCustomizer(suProj);
        }
        return null;
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
        Image ret = getProjIcon();
        if (ret == null){
            ret = ImageUtilities.loadImage("org/netbeans/modules/compapp/projects/jbi/ui/resources/jar.gif"); // NOI18N
        }

        return ret;
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
        return getIcon(type);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
