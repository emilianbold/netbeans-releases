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

package org.netbeans.modules.maven.execute;

import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.model.Profile;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ActionProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * checks if the run/debug actions with default mappings can be sucessfully executed or not.
 * checks the profile configuration...
 * @author mkleint
 * @deprecated just for compatibility reasons there, should not
 * be actually showing any UI, since the org.codehaus.mevenide:netbeans-run-plugin is no more default.
 */
public @Deprecated class JarPackagingRunChecker implements PrerequisitesChecker {
    
    private List applicableActions = Arrays.asList(new String[] {
        ActionProvider.COMMAND_RUN,
        ActionProvider.COMMAND_RUN_SINGLE,
        ActionProvider.COMMAND_DEBUG,
        ActionProvider.COMMAND_DEBUG_SINGLE
    });
    /** Creates a new instance of JarPackagingRunChecker */
    public JarPackagingRunChecker() {
    }

    public boolean checkRunConfig(RunConfig config) {
        String actionName = config.getActionName();
        if (applicableActions.contains(actionName)) {
            Iterator it = config.getGoals().iterator();
            while (it.hasNext()) {
                String goal = (String) it.next();
                if (goal.indexOf("org.codehaus.mevenide:netbeans-run-plugin") > -1) { //NOI18N
                    List profiles = config.getMavenProject().getModel().getProfiles();
                    Iterator it2 = profiles.iterator();
                    boolean warn = true;
                    while (it2.hasNext()) {
                        Profile prof = (Profile) it2.next();
                        if ("netbeans-public".equals(prof.getId())) { //NOI18N
                            // consider correct if profile is in..
                            warn = false;
                            break;
                        }
                    }
                    if (warn) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(JarPackagingRunChecker.class, "MSG_Need_Project_Customizer"));
                        DialogDisplayer.getDefault().notify(nd);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
}
