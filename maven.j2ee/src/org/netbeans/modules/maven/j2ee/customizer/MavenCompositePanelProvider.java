/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Janicek
 */
public class MavenCompositePanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String FRAMEWORKS = "Frameworks"; // NOI18N
    private static final String RUN = "Run"; // NOI18N
    
    private AbstractCustomizer frameworkCustomizer;
    private AbstractCustomizer runCustomizer;
    

    private String type;
    
    private MavenCompositePanelProvider(String type) {
        this.type = type;
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-maven", position=257)
    public static MavenCompositePanelProvider createFrameworks() {
        return new MavenCompositePanelProvider(FRAMEWORKS);
    }
    
    @ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-maven", position=301)
    public static MavenCompositePanelProvider createRun() {
        return new MavenCompositePanelProvider(RUN);
    }
    
    
    @Override
    public Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(type, NbBundle.getMessage(MavenCompositePanelProvider.class,"PNL_" + type), null); // NOI18N
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        String name = category.getName();
        ModelHandle handle = context.lookup(ModelHandle.class);
        Project project = context.lookup(Project.class);
        
        AbstractCustomizer customizer = null;
        if (FRAMEWORKS.equals(name)) {
            customizer = new CustomizerFrameworks(category, handle, project);
            frameworkCustomizer = customizer;
        }
        if (RUN.equals(name)) {
            String projectType = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
            
            if (NbMavenProject.TYPE_WAR.equalsIgnoreCase(projectType)) {
                customizer = new CustomizerRunWeb(handle, project);
            }
            if (NbMavenProject.TYPE_EJB.equalsIgnoreCase(projectType)) {
                customizer = new CustomizerRunEjb(handle, project);
            }
            if (NbMavenProject.TYPE_EAR.equalsIgnoreCase(projectType)) {
                customizer = new CustomizerRunEar(handle, project);
            }
            runCustomizer = customizer;
        }

        category.setOkButtonListener(listenerAWT);
        category.setStoreListener(listenerNonAWT);
        
        return customizer;
    }
    
    private ActionListener listenerAWT = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (runCustomizer != null) {
                runCustomizer.applyChangesInAWT();
            }
            if (frameworkCustomizer != null) {
                frameworkCustomizer.applyChangesInAWT();
            }
        }
    };
    
    private ActionListener listenerNonAWT = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (runCustomizer != null) {
                runCustomizer.applyChanges();
            }
            if (frameworkCustomizer != null) {
                frameworkCustomizer.applyChanges();
            }
        }
    };
}
