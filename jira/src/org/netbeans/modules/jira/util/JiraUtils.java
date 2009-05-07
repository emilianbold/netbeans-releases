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

package org.netbeans.modules.jira.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.commands.JiraCommand;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class JiraUtils {

    public static boolean show(JPanel panel, String title, String okName, HelpCtx helpCtx) {
        JButton ok = new JButton(okName);
        JButton cancel = new JButton(NbBundle.getMessage(JiraUtils.class, "LBL_Cancel")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor (
                panel,
                title,
                true,
                new Object[] {ok, cancel},
                ok,
                DialogDescriptor.DEFAULT_ALIGN,
                helpCtx,
                null);
        return DialogDisplayer.getDefault().notify(descriptor) == ok;
    }

    // XXX merge with bugzilla
    /**
     * Returns TaskData for the given issue key or null if an error occured
     * @param repository
     * @param key
     * @return
     */
    public static TaskData getTaskDataByKey(final JiraRepository repository, final String key) {
        return getTaskDataByKey(repository, key, true);
    }

    /**
     * Returns TaskData for the given issue key or null if an error occured
     * @param repository
     * @param key
     * @return
     */
    public static TaskData getTaskDataByKey(final JiraRepository repository, final String key, boolean handleExceptions) {
        final TaskData[] taskData = new TaskData[1];
        JiraCommand cmd = new JiraCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                taskData[0] = Jira.getInstance().getRepositoryConnector().getTaskData(repository.getTaskRepository(), key, new NullProgressMonitor());
            }
        };
        repository.getExecutor().execute(cmd, handleExceptions);
        if(cmd.hasFailed() && Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.log(Level.FINE, cmd.getErrorMessage());
        }
        return taskData[0];
    }

    // XXX merge with bugzilla
    /**
     * Returns TaskData for the given issue id or null if an error occured
     * @param repository
     * @param id
     * @return
     */
    public static TaskData getTaskDataById(final JiraRepository repository, final String id) {
        return getTaskDataById(repository, id, true);
    }

    public static TaskData getTaskDataById(final JiraRepository repository, final String id, boolean handleExceptions) {
        final TaskData[] taskData = new TaskData[1];
        JiraCommand cmd = new JiraCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                taskData[0] = Jira.getInstance().getRepositoryConnector().getTaskDataHandler().getTaskData(repository.getTaskRepository(), id, new NullProgressMonitor());
            }
        };
        repository.getExecutor().execute(cmd, handleExceptions);
        if(cmd.hasFailed() && Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.log(Level.FINE, cmd.getErrorMessage());
        }
        return taskData[0];
    }

    /**
     * Determines if the operation could be a resolve operation
     * @param operationLabel operation name
     * @return
     */
    public static boolean isResolveOperation(String operationLabel) {
        return "resolve issue".equals(operationLabel.toLowerCase());    //NOI18N
    }

    public static boolean isReopenOperation(String operationLabel) {
        return "reopen issue".equals(operationLabel.toLowerCase());    //NOI18N
    }

    public static String getMappedValue(TaskAttribute a, String key) {
        TaskAttribute ma = a.getMappedAttribute(key);
        if (ma != null) {
            return ma.getValue();
        }
        return null;
    }

    /**
     * Returns a resolution instance for given resolutionName
     * @param repository 
     * @param resolutionName
     * @return
     * @throws IllegalStateException if there is no such resolution available
     */
    public static Resolution getResolutionByName(JiraRepository repository, String resolutionName) {
        Resolution[] resolutions = repository.getConfiguration().getResolutions();
        for (Resolution r : resolutions) {
            if(r.getName().equals(resolutionName)) return r;
        }
        throw new IllegalStateException("Unknown resolution type: " + resolutionName); //NOI18N
    }

    /**
     * Copies all content from the supplied reader to the supplies writer and closes both streams when finished.
     *
     * @param writer where to write
     * @param reader what to read
     * @throws IOException if any I/O operation fails
     */
    public static void copyStreamsCloseAll(OutputStream writer, InputStream reader) throws IOException {
        byte [] buffer = new byte[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
}
