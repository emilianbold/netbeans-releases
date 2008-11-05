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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * extending the default maven project lookup.
 * @author  Milos Kleint 
 */
@LookupProvider.Register(projectType="org-netbeans-modules-maven")
public class MavenApisupportLookupProvider implements LookupProvider {
    
    /** Creates a new instance of MavenApisupportLookupProvider */
    public MavenApisupportLookupProvider() {
    }
    
    public Lookup createAdditionalLookup(Lookup baseLookup) {
        Project project = baseLookup.lookup(Project.class);
        assert project != null;
//        // if there's more items later, just do a proxy..
        InstanceContent ic = new InstanceContent();
        ic.add(new ApisupportRecoPrivTemplates(project));
        ic.add(new ExecutionChecker(project));
        return new Provider(project, ic);
    }
    
    private static class Provider extends AbstractLookup implements  PropertyChangeListener {
        private Project project;
        private InstanceContent content;
        private String lastType = NbMavenProject.TYPE_JAR;
        private MavenNbModuleImpl lastInstance = null;
        private AccessQueryImpl lastAccess = null;
        public Provider(Project proj, InstanceContent cont) {
            super(cont);
            project = proj;
            content = cont;
            checkNbm();
            NbMavenProject.addPropertyChangeListener(project, this);
        }
        
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (NbMavenProject.PROP_PROJECT.equals(propertyChangeEvent.getPropertyName())) {
                checkNbm();
            }
        }
        
        private void checkNbm() {
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            String packaging = watcher.getPackagingType();
            if (packaging == null) {
                packaging = NbMavenProject.TYPE_JAR;
            }
            if (NbMavenProject.TYPE_NBM.equals(packaging) && !lastType.equals(packaging)) {
                if (lastInstance == null) {
                    lastInstance = new MavenNbModuleImpl(project);
                }
                content.add(lastInstance);
                if (lastAccess == null) {
                    lastAccess = new AccessQueryImpl(project);
                }
                content.add(lastAccess);
            } else if (lastInstance != null && !(
                    NbMavenProject.TYPE_NBM.equals(packaging)))
            {
                content.remove(lastInstance);
                content.remove(lastAccess);
            }
            lastType = packaging;
        }
        
    }

}
