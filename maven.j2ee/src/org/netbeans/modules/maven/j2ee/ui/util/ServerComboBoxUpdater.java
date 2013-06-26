/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ui.util;

import org.netbeans.modules.maven.j2ee.utils.ServerUtils;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.customizer.support.ComboBoxUpdater;
import org.netbeans.modules.maven.j2ee.ExecutionChecker;
import org.netbeans.modules.maven.j2ee.utils.Server;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;

/**
 *
 * @author Martin Janicek
 */
public final class ServerComboBoxUpdater extends ComboBoxUpdater<Server> {

    private final Project project;
    private final Server defaultValue;


    private ServerComboBoxUpdater(Project project, JComboBox serverCBox, JLabel serverLabel, J2eeModule.Type projectType) {
        super(serverCBox, serverLabel);
        assert (project != null);
        assert (serverCBox != null);

        serverCBox.setModel(new DefaultComboBoxModel(ServerUtils.findServersFor(projectType).toArray()));

        this.project = project;
        this.defaultValue = getValue();

        serverCBox.setSelectedItem(defaultValue);
    }

    /**
     * Factory method encapsulating ComboBoxUpdater creation. Typically client don't
     * want to do anything with a new instance so this makes more sense than creating
     * it using "new" keyword.
     *
     * @param project Current project
     * @param serverCBox Server selection combo box for which we want to create updater
     * @param serverLabel Server selection label typically just before combo box
     */
    public static void create(Project project, JComboBox serverCBox, JLabel serverLabel, J2eeModule.Type projectType) {
        new ServerComboBoxUpdater(project, serverCBox, serverLabel, projectType);
    }

    @Override
    public Server getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setValue(Server newServer) {
        if (newServer == null) {
            MavenProjectSupport.setServerInstanceID(project, defaultValue.getServerInstanceID());
        } else {
            String serverID = newServer.getServerInstanceID();

            if (ExecutionChecker.DEV_NULL.equals(serverID)) {
                MavenProjectSupport.setServerInstanceID(project, null);
            } else {
                MavenProjectSupport.setServerInstanceID(project, serverID);
            }
        }
    }

    @Override
    public Server getValue() {
        return ServerUtils.findServer(project);
    }
}
