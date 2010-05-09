/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.BrandingUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Opens branding editor window for 'branding' sub-project of a Maven app suite.
 * 
 * @author S. Aubrecht
 */
public class OpenBrandingEditorAction extends AbstractAction implements ContextAwareAction {

    private final Lookup context;

    public OpenBrandingEditorAction() {
        this( Lookup.EMPTY );
    }

    private OpenBrandingEditorAction( Lookup context ) {
        super( NbBundle.getMessage(OpenBrandingEditorAction.class, "LBL_OpenBrandingEditor") ); //NOI18N
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        this.context = context;
        enable( context );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = context.lookup(Project.class);
        if( null == project )
            return;
        
        NbMavenProject mproject = project.getLookup().lookup(NbMavenProject.class);
        if( null == mproject )
            return;

        String brandingPath = PluginPropertyUtils.getPluginProperty(mproject.getMavenProject(),
                BRANDING_GROUP_ID, BRANDING_ARTIFACT_ID, "brandingSources", BRANDING_GOAL); //NOI18N
        if( null == brandingPath ) {
            brandingPath = "src/main/nbm-branding"; //NOI18N
        }
        BrandingUtils.openBrandingEditor(mproject.getMavenProject().getName(), project, brandingPath);
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new OpenBrandingEditorAction(actionContext);
    }

    private void enable( Lookup context ) {
        boolean enable = false;
        Project project = context.lookup(Project.class);
        if( null != project ) {
            NbMavenProject mproject = project.getLookup().lookup(NbMavenProject.class);
            if( null != mproject ) {
                enable = isBrandingProject(mproject.getMavenProject());
            }
        }
        setEnabled(enable);
    }

    private static final String BRANDING_GROUP_ID = "org.codehaus.mojo"; //NOI18N
    private static final String BRANDING_ARTIFACT_ID = "nbm-maven-plugin"; //NOI18N
    private static final String BRANDING_GOAL = "branding"; //NOI18N

    private static boolean isBrandingProject(MavenProject prj) {
        if (prj.getBuildPlugins() == null) {
            return false;
        }
        for (Object obj : prj.getBuildPlugins()) {
            Plugin plug = (Plugin)obj;
            if (BRANDING_ARTIFACT_ID.equals(plug.getArtifactId()) &&
                   BRANDING_GROUP_ID.equals(plug.getGroupId())) {
                if (plug.getExecutions() != null) {
                    for (Object obj2 : plug.getExecutions()) {
                        PluginExecution exe = (PluginExecution)obj2;
                        if (exe.getGoals().contains(BRANDING_GOAL) ||
                                ("default-" + BRANDING_GOAL).equals(exe.getId())) { //this is a maven 2.2.0+ thing.. #179328 //NOI18N

                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
