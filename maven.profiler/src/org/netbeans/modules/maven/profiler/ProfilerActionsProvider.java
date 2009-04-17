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

package org.netbeans.modules.maven.profiler;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.maven.spi.actions.MavenActionsProvider.class, position=72)
public class ProfilerActionsProvider extends AbstractMavenActionsProvider {
    final private Set<String> supportedTypes = new HashSet<String>() {
        {
            add(NbMavenProject.TYPE_JAR);
            add(NbMavenProject.TYPE_WAR);
            add(NbMavenProject.TYPE_EJB);
            add(NbMavenProject.TYPE_NBM);
            add(NbMavenProject.TYPE_NBM_APPLICATION);
        }
    };

    @Override
    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        if (!(action.equals("profile") || action.equals("profile-single") || action.equals("profile-tests"))) {
            return false;
        }
        NbMavenProject mavenprj = project.getLookup().lookup(NbMavenProject.class);
        String type = mavenprj.getPackagingType();
        if (supportedTypes.contains(type)) {
            return super.isActionEnable(action, project, lookup);
        }
        return false;
    }

    @Override
    protected InputStream getActionDefinitionStream() {
            String path = "/org/netbeans/modules/maven/profiler/ActionMappings.xml"; //NOI18N
            InputStream in = getClass().getResourceAsStream(path);
            assert in != null : "no instream for " + path; //NOI18N
            return in;
    }
}
