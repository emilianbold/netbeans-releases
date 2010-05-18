/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.jmx.j2seproject.customizer;

import java.util.ResourceBundle;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jmx.common.runtime.J2SEProjectType;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

// This customizer should be located in existing Run category
@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", position=632)
public class ManagementCompositePanelProvider
        implements ProjectCustomizer.CompositeCategoryProvider {
    
    public static final String POLLING_PERIOD_KEY = "jmx.jconsole.period"; // NOI18N
    public static final String ATTACH_JCONSOLE_KEY = "jmx.jconsole.enabled"; // NOI18N
    public static final String ENABLE_RMI_KEY = "jmx.rmi.enabled"; // NOI18N
    public static final String RMI_USE_PORT_KEY = "jmx.rmi.use.port"; // NOI18N
    public static final String RMI_PORT_KEY = "jmx.rmi.port"; // NOI18N
    public static final String CONFIG_FILE_KEY = "jmx.config.file"; // NOI18N
    public static final String RESOLVE_CLASSPATH_KEY = "jmx.jconsole.use.classpath"; // NOI18N
    
    public static final String PLUGINS_CLASSPATH_KEY = "jmx.jconsole.classpath.plugins"; // NOI18N
    public static final String PLUGINS_PATH_KEY = "jmx.jconsole.plugins.path"; // NOI18N
    
    private static final String MANAGEMENT = "Management"; // NOI18N
    
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ResourceBundle bundle = NbBundle.getBundle(ManagementCompositePanelProvider.class);
        return ProjectCustomizer.Category.create(
                    MANAGEMENT,
                    bundle.getString("LBL_Config_Management"),// NOI18N
                    null);
    }
    
    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        Project project = context.lookup(Project.class);
        MonitoringPanel mp =
                new MonitoringPanel(project,
                J2SEProjectType.isPlatformGreaterThanJDK15(project));

        category.setOkButtonListener(mp);
        return mp;
    }
    
}
