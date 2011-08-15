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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import java.io.InputStream;
import java.util.ArrayList;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

/**
 * j2ee specific defaults for project running and debugging..
 * @author mkleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.maven.spi.actions.MavenActionsProvider.class, position=50)
public class J2eeActionsProvider extends AbstractMavenActionsProvider {

    private ArrayList<String> supported;
    private static final String ACT_RUN = ActionProvider.COMMAND_RUN_SINGLE + ".deploy";
    private static final String ACT_DEBUG = ActionProvider.COMMAND_DEBUG_SINGLE + ".deploy";
    /** Creates a new instance of J2eeActionsProvider */
    public J2eeActionsProvider() {
        supported = new ArrayList<String>();
        supported.add(NbMavenProject.TYPE_WAR);
        supported.add(NbMavenProject.TYPE_EAR);
        supported.add(NbMavenProject.TYPE_EJB);
        supported.add(NbMavenProject.TYPE_APPCLIENT);
    }
    
    
    public InputStream getActionDefinitionStream() {
        String path = "/org/netbeans/modules/maven/j2ee/webActionMappings.xml"; //NOI18N
        InputStream in = getClass().getResourceAsStream(path);
        assert in != null : "no instream for " + path;  //NOI18N
        return in;
    }

    @Override
    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        if (ACT_DEBUG.equals(action) ||
            ACT_RUN.equals(action)) {
            //only enable for doc root fileobjects..
            return true;
        } else if (ActionProvider.COMMAND_RUN.equals(action) || 
                   ActionProvider.COMMAND_DEBUG.equals(action)) {
            //performance, don't read the xml file to figure enablement..
            NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
            return supported.contains(mp.getPackagingType());
        } else {
            return false;
        }
    }
}
