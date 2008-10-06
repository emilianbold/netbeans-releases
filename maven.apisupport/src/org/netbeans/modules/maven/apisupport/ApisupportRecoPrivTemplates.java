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

package org.netbeans.modules.maven.apisupport;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;

/**
 * apisupport specific part of RecommendedTemplates and PrivilegedTemplates,
 * @author Milos Kleint
 */
public class ApisupportRecoPrivTemplates implements RecommendedTemplates, PrivilegedTemplates {
    
    private Project project;
    
    ApisupportRecoPrivTemplates(Project proj) {
        project = proj;
    }
    
        private static final String[] NBM_PRIVILEGED_NAMES = new String[] {
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            //"Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/JUnit/SimpleJUnitTest.java", // NOI18N
            "Templates/NetBeansModuleDevelopment/newAction", // NOI18N
            "Templates/NetBeansModuleDevelopment/newLoader", // NOI18N
            "Templates/NetBeansModuleDevelopment/newWindow", // NOI18N
            "Templates/NetBeansModuleDevelopment/newWizard", // NOI18N
            //"Templates/Other/properties.properties", // NOI18N
        };
        private static final String[] NBM_TYPES = new String[] {         
            "java-classes",         // NOI18N
            "java-main-class",      // NOI18N
            "java-forms",           // NOI18N
            "java-beans",           // NOI18N
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "junit",                // NOI18N                    
            "simple-files",         // NOI18N
            "nbm-specific"         // NOI18N
        };
        
    
    public String[] getRecommendedTypes() {
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        String packaging = watcher.getPackagingType();
        if (packaging == null) {
            packaging = NbMavenProject.TYPE_JAR;
        }
        packaging = packaging.trim();
        if (NbMavenProject.TYPE_NBM.equals(packaging)) {
            return NBM_TYPES;
        }
        return new String[0];
    }
    
    public String[] getPrivilegedTemplates() {
        NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
        String packaging = watcher.getPackagingType();
        if (packaging == null) {
            packaging = NbMavenProject.TYPE_JAR;
        }
        packaging = packaging.trim();
        if (NbMavenProject.TYPE_NBM.equals(packaging)) {
            return NBM_PRIVILEGED_NAMES;
        }
        
        return new String[0];
    }
    
}
