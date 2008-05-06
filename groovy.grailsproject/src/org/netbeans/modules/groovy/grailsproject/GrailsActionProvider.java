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

package org.netbeans.modules.groovy.grailsproject;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.groovy.grails.api.ExecutionSupport;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.actions.RunGrailsServerCommandAction;
import org.netbeans.spi.project.ActionProvider;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class GrailsActionProvider implements ActionProvider {

    private static final Logger LOGGER = Logger.getLogger(GrailsActionProvider.class.getName());
    
    private static final String[] supportedActions = {
        COMMAND_RUN,
        COMMAND_TEST
    };

    private final GrailsProject project;

    public GrailsActionProvider(GrailsProject project) {
        this.project = project;
    }


    public String[] getSupportedActions() {
        return supportedActions.clone();
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_RUN.equals(command)) {
            LifecycleManager.getDefault().saveAll();
            // FIXME this is just temporary hack - content of the action with stop
            // and rerun has to be wrapped to nice class
            new RunGrailsServerCommandAction(project).actionPerformed(null);
        } else if (COMMAND_TEST.equals(command)) {
            try {
                Process process = ExecutionSupport.getInstance().executeSimpleCommand("test-app", GrailsProjectConfig.forProject(project)); // NOI18N
                RequestProcessor.getDefault().post(new GrailsProcessRunnable(process,
                        null, command, new File[] {FileUtil.toFile(project.getProjectDirectory())}));
            } catch (Exception ex) {
                // FIXME
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        return true;
    }


}
