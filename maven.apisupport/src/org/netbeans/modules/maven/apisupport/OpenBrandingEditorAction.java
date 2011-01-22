/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.BrandingUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * Opens branding editor window for 'branding' sub-project of a Maven app suite.
 * 
 * @author S. Aubrecht
 */
@ActionID(id = "org.netbeans.modules.maven.apisupport.OpenBrandingEditorAction", category = "Project")
@ActionRegistration(displayName = "#LBL_OpenBrandingEditor")
@ActionReference(position = 3150, path = "Projects/org-netbeans-modules-maven/Actions")
@Messages("LBL_OpenBrandingEditor=Branding...")
public class OpenBrandingEditorAction extends AbstractAction implements ContextAwareAction {

    private final Lookup context;

    public OpenBrandingEditorAction() {
        this( Lookup.EMPTY );
    }

    private OpenBrandingEditorAction( Lookup context ) {
        super( LBL_OpenBrandingEditor() ); //NOI18N
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = context.lookup(Project.class);
        MavenProject mavenProject = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        BrandingUtils.openBrandingEditor(mavenProject.getName(), project, brandingPath(mavenProject));
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new OpenBrandingEditorAction(actionContext);
    }

    public @Override boolean isEnabled() {
        Project project = context.lookup(Project.class);
        if (project == null) {
            return false;
        }
        NbMavenProject mproject = project.getLookup().lookup(NbMavenProject.class);
        if (mproject == null) {
            return false;
        }
        return project.getProjectDirectory().getFileObject(brandingPath(mproject.getMavenProject())) != null;
    }

    private String brandingPath(MavenProject mavenProject) {
        String brandingPath = PluginPropertyUtils.getPluginProperty(mavenProject, MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN, "brandingSources", "branding"); //NOI18N
        return brandingPath != null ? brandingPath : "src/main/nbm-branding"; //NOI18N
    }

}
