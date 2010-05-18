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

package org.netbeans.modules.compapp.projects.jbi.api;

import org.openide.filesystems.FileObject;

import org.openide.util.Lookup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;

import java.util.*;
import org.openide.filesystems.FileUtil;


/**
 * DOCUMENT ME!
 *
 * @author
 * @version
 */
public class JbiInstalledProjectPluginInfo {

    private static JbiInstalledProjectPluginInfo singleton = null;

    // a list of Jbi Extension Info known at design time
    private List<InternalProjectTypePlugin> extensionList = new ArrayList<InternalProjectTypePlugin>();
    private List<InternalProjectTypePlugin> paletteList = new ArrayList<InternalProjectTypePlugin>();
    private List<InternalProjectTypePlugin> nonPaletteList = new ArrayList<InternalProjectTypePlugin>();


    private JbiInstalledProjectPluginInfo() {
    }

    /**
     * Factory method for the default component list object
     *
     * @return the default component list object
     */
    public static JbiInstalledProjectPluginInfo getProjectPluginInfo() {
        if (singleton == null) {
            try {
                singleton = new JbiInstalledProjectPluginInfo();

                Collection<? extends InternalProjectTypePlugin> projectPlugins =
                        Lookup.getDefault().lookupAll(InternalProjectTypePlugin.class);

                for (InternalProjectTypePlugin projectPlugin : projectPlugins) {
                    singleton.extensionList.add(projectPlugin);
                    String category = projectPlugin.getCategoryName();
                    if (category != null) {
                        singleton.paletteList.add(projectPlugin);
                    } else {
                        singleton.nonPaletteList.add(projectPlugin);
                    }
                }

            } catch (Exception ex) {
                // failed... return withopt changing the selector content.
                ex.printStackTrace();
            }
        }

        return singleton;
    }

    /**
     * Getter for the installed project plug-in info list
     *
     * @return the default installed project plug-in info list
     */
    public List<InternalProjectTypePlugin> getProjectPluginList() {
        return extensionList;
    }

    /**
     * Getter for the categorized project plug-in info list
     *
     * @return the default categorized project plug-in info list
     */
    public List<InternalProjectTypePlugin> getCategorizedProjectPluginList() {
        return paletteList;
    }

    /**
     * Getter for the uncategorized project plug-in info list
     *
     * @return the default uncategorized project plug-in info list
     */
    public List<InternalProjectTypePlugin> getUncategorizedProjectPluginList() {
        return nonPaletteList;
    }

    /**
     * Check if the child project is an internal subproject of parent
     *
     * @param parent  the parent project
     * @param child the child project
     * @return true, if it is an internal subproject
     */
    public static boolean isInternalSubproject(Project parent, Project child) {
        if ((parent != null) && (child != null)) {
            FileObject jbiPrjDir = parent.getProjectDirectory();
            FileObject projDir = child.getProjectDirectory();
            if (FileUtil.isParentOf(jbiPrjDir, projDir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the plugin project type from a project
     *
     * @param project a project
     * @return  project plugin
     */
    public static InternalProjectTypePlugin getPlugin(Project project) {
        if (project == null) {
            return null;
        }

        // get target component name
        AntArtifactProvider prov = project.getLookup().lookup(AntArtifactProvider.class);
        String target = null;
        if (prov != null) {
            AntArtifact[] artifacts = prov.getBuildArtifacts();
            if (artifacts != null) {
                for (int i = 0; i < artifacts.length; i++) {
                    String type = artifacts[i].getType();
                    // System.out.println(i+": "+type);
                    if (type.startsWith(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA)) {
                        target = type.substring(JbiProjectConstants.ARTIFACT_TYPE_JBI_ASA.length()+1);
                    }
                }
            }
        }
        if (target == null) {
            return null;
        }

        // loop thru plugin list
        JbiInstalledProjectPluginInfo info = JbiInstalledProjectPluginInfo.getProjectPluginInfo();
        if (info != null) {
            List<InternalProjectTypePlugin> plugins = info.getProjectPluginList();
            for (InternalProjectTypePlugin plugin : plugins) {
                if (target.equalsIgnoreCase(plugin.getJbiTargetName())) {
                    return plugin;
                }
            }
        }
        return null;
    }
}
