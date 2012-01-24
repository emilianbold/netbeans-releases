/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.internalserver;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.api.phpmodule.PhpInterpreter;
import org.netbeans.modules.php.api.phpmodule.PhpProgram.InvalidPhpProgramException;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Manager of internal web server (available in PHP 5.4+)
 * for the given {@link PhpProject}.
 */
public final class InternalWebServer implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(InternalWebServer.class.getName());

    private static final String WEB_SERVER_PARAM = "-S"; // NOI18N
    private static final String WEB_ROOT_PARAM = "-t"; // NOI18N

    private final PhpProject project;

    // @GuardedBy(this)
    private Future<Integer> process = null;


    private InternalWebServer(PhpProject project) {
        this.project = project;
    }

    public static InternalWebServer createForProject(PhpProject project) {
        InternalWebServer server = new InternalWebServer(project);
        ProjectPropertiesSupport.getPropertyEvaluator(project).addPropertyChangeListener(server);
        return server;
    }

    public synchronized boolean isRunning() {
        return process != null && !process.isDone();
    }

    public synchronized void start() {
        if (isRunning()) {
            LOGGER.log(Level.INFO, "Internal web server already running for project {0}", project.getName());
            return;
        }
        process = createProcess();
    }

    @NbBundle.Messages({
        "# 0 - project name",
        "InternalWebServer.error.cancelProcess=Cannot cancel running internal web server for project {0}."
    })
    public synchronized void stop() {
        if (isRunning()
                && !process.cancel(true)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    Bundle.InternalWebServer_error_cancelProcess(project.getName()),
                    NotifyDescriptor.WARNING_MESSAGE));
        }
        reset();
    }

    public synchronized void restart() {
        stop();
        start();
    }

    void reset() {
        assert Thread.holdsLock(this);
        process = null;
    }

    @NbBundle.Messages({
        "# 0 - project name",
        "InternalWebServer.output.title=Internal WebServer [{0}]"
    })
    private Future<Integer> createProcess() {
        PhpInterpreter phpInterpreter;
        try {
            phpInterpreter = PhpInterpreter.getDefault();
        } catch (InvalidPhpProgramException ex) {
            UiUtils.invalidScriptProvided(ex.getLocalizedMessage());
            return null;
        }
        ExternalProcessBuilder externalProcessBuilder = phpInterpreter.getProcessBuilder()
                .workingDirectory(FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(project)))
                .addArgument(WEB_SERVER_PARAM)
                // XXX
                .addArgument("localhost:8000"); // NOI18N
        FileObject sourceDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject webRootDirectory = ProjectPropertiesSupport.getWebRootDirectory(project);
        if (!sourceDirectory.equals(webRootDirectory)) {
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(WEB_ROOT_PARAM)
                    .addArgument(ProjectPropertiesSupport.getWebRoot(project));
        }
        // XXX router script
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .controllable(true)
                .frontWindow(true)
                .frontWindowOnError(true)
                .optionsPath(UiUtils.OPTIONS_PATH + "/" + UiUtils.GENERAL_OPTIONS_SUBCATEGORY); // NOI18N
        return PhpInterpreter.executeLater(externalProcessBuilder, executionDescriptor, Bundle.InternalWebServer_output_title(project.getName()));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (isRunning()) {
            restart();
        }
    }

}
