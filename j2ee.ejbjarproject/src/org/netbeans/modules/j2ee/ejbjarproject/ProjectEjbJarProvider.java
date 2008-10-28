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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class ProjectEjbJarProvider implements EjbJarProvider, EjbJarsInProject/*, ProjectPropertiesSupport*/ {
    
    private EjbJarProject project;
    
    public ProjectEjbJarProvider (EjbJarProject project) {
        this.project = project;
    }
    
    public EjbJar findEjbJar (FileObject file) {
        Project owner = FileOwnerQuery.getOwner (file);
        if (owner != null && owner instanceof EjbJarProject) {
            return ((EjbJarProject) owner).getAPIEjbJar();
        }
        return null;
    }

    public EjbJar[] getEjbJars() {
        return new EjbJar [] {project.getAPIEjbJar()};
    }

    public void disableSunCmpMappingExclusion() {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
        String metaInfExcludes = project.evaluator().getProperty(EjbJarProjectProperties.META_INF_EXCLUDES);
        if (metaInfExcludes == null) {
            return;
        }
        String[] tokens = metaInfExcludes.split(" |,");
        StringBuffer newMetaInfExcludes = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equals("sun-cmp-mappings.xml") || tokens[i].equals("")) { // NOI18N
                continue;
            }

            newMetaInfExcludes.append(tokens[i]);
            if (i < tokens.length - 1) {
                newMetaInfExcludes.append(" "); // NOI18N
            }
        }
        project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).
                put(EjbJarProjectProperties.META_INF_EXCLUDES, newMetaInfExcludes.toString());
        try {
            ProjectManager.getDefault().saveProject(project);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
            }
        });
    }
    
}
