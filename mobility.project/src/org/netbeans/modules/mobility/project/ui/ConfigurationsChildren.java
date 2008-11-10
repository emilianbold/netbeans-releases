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

package org.netbeans.modules.mobility.project.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.netbeans.spi.project.ProjectConfiguration;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Tim Boudreau
 */
final class ConfigurationsChildren extends ChildFactory<ProjectConfiguration> {
    private final J2MEProject project;
    ConfigurationsChildren (J2MEProject project) {
        this.project = project;
    }

    @Override
    protected boolean createKeys(List<ProjectConfiguration> toPopulate) {
        toPopulate.addAll (project.getConfigurationHelper().getConfigurations());
        Collections.sort (toPopulate, new ConfigurationComparator());
        return true;
    }

    private static final class ConfigurationComparator implements Comparator<ProjectConfiguration> {
        public int compare(ProjectConfiguration o1, ProjectConfiguration o2) {
            String nm1 = o1 == null ? "" : o1.getDisplayName();
            String nm2 = o2 == null ? "" : o2.getDisplayName();
            if (nm1.equals(nm2)) {
                return 0;
            }
            if (nm1.equals(ProjectConfigurationsHelper.DEFAULT_CONFIGURATION_NAME)) {
                return -1;
            }
            if (nm2.equals(ProjectConfigurationsHelper.DEFAULT_CONFIGURATION_NAME)) {
                return 1;
            }
            return nm1.compareToIgnoreCase(nm2);
        }
    }


    @Override
    protected Node createNodeForKey(ProjectConfiguration config) {
        return new OneConfigurationNode (project, config);
    }

    void update() {
        refresh (true);
    }
}
