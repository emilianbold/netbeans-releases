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

package org.netbeans.modules.jira.util;

import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.User;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.issue.JiraIssueFinder;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.commands.GetRepositoryTasksCommand;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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

    public static void notifyErrorMessage(String msg) {
        NotifyDescriptor nd =
                new NotifyDescriptor(
                    msg,
                    NbBundle.getMessage(JiraUtils.class, "LBLError"),    // NOI18N
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[] {NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }

    /**
     * Returns Task for the given issue id or null if an error occurred
     * @param repository
     * @param key
     * @return
     */
    public static NbTask getRepositoryTask (final JiraRepository repository, final String key, boolean handleExceptions) {
        MylynSupport supp = MylynSupport.getInstance();
        try {
            GetRepositoryTasksCommand cmd = supp.getCommandFactory()
                    .createGetRepositoryTasksCommand(repository.getTaskRepository(), Collections.<String>singleton(key));
            repository.getExecutor().execute(cmd, handleExceptions);
            if(cmd.hasFailed()) {
                Jira.LOG.log(Level.FINE, cmd.getErrorMessage());
            }
            if (cmd.getTasks().isEmpty()) {
                // fallback on local
                NbTask task = supp.getTask(repository.getTaskRepository().getRepositoryUrl(), key);
                if (cmd.hasFailed() && task != null) {
                    return task;
                }
            } else {
                return cmd.getTasks().iterator().next();
            }
        } catch (CoreException ex) {
            Jira.LOG.log(Level.INFO, null, ex);
        }
        return null;
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

    public static boolean isCloseOperation(String operationLabel) {
        return "close issue".equals(operationLabel.toLowerCase());    //NOI18N
    }

    public static boolean isStartProgressOperation(String operationLabel) {
        return "start progress".equals(operationLabel.toLowerCase());    //NOI18N
    }

    public static boolean isStopProgressOperation(String operationLabel) {
        return "stop progress".equals(operationLabel.toLowerCase());    //NOI18N
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

    public static boolean isAssertEnabled() {
        boolean retval = false;
        assert retval = true;
        return retval;
    }

    public static String[] toStrings(IssueType[] types) {
        String[] ret = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ret[i] = types[i].getName();
        }
        return ret;
    }

    public static String[] toStrings(Priority[] prios) {
        String[] ret = new String[prios.length];
        for (int i = 0; i < prios.length; i++) {
            ret[i] = prios[i].getName();
        }
        return ret;
    }

    public static String[] toStrings(JiraStatus[] statuses) {
        String[] ret = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            ret[i] = statuses[i].getName();
        }
        return ret;
    }

    public static String[] toStrings(Resolution[] resolutions) {
        String[] ret = new String[resolutions.length];
        for (int i = 0; i < resolutions.length; i++) {
            ret[i] = resolutions[i].getName();
        }
        return ret;
    }

    public static String getWorkLogText(int seconds, int daysPerWeek, int hoursPerDay, boolean isRemainingEstimate) {
        ResourceBundle bundle = NbBundle.getBundle(JiraUtils.class);
        if (seconds == 0) {
            return bundle.getString(isRemainingEstimate ? "WorkLog.emptyWorkLog1" : "WorkLog.emptyWorkLog2"); // NOI18N
        }
        int minutes = seconds/60;
        int hours = minutes/60;
        minutes = minutes%60;
        int days = hours/hoursPerDay;
        hours = hours%hoursPerDay;
        int weeks = days/daysPerWeek;
        days = days%daysPerWeek;
        String format = bundle.getString("WorkLog.textPattern"); // NOI18N
        String work = MessageFormat.format(format, weeks, days, hours, minutes);
        // Removing trailing space and comma
        if (work.length() > 0 && work.charAt(work.length()-1) == ' ') {
            work = work.substring(0, work.length()-2);
        }
        return work;
    }

    public static String getWorkLogCode(long seconds, int daysPerWeek, int hoursPerDay) {
        ResourceBundle bundle = NbBundle.getBundle(JiraUtils.class);
        long minutes = seconds/60;
        long hours = minutes/60;
        minutes = minutes%60;
        long days = hours/hoursPerDay;
        hours = hours%hoursPerDay;
        long weeks = days/daysPerWeek;
        days = days%daysPerWeek;
        String format = bundle.getString("WorkLog.codePattern"); // NOI18N
        String work = MessageFormat.format(format, weeks, days, hours, minutes);
        // Removing trailing space
        if (work.length() > 0 && work.charAt(work.length()-1) == ' ') {
            work = work.substring(0, work.length()-1);
        }
        return work;
    }

    public static int getWorkLogSeconds(String code, int daysPerWeek, int hoursPerDay) {
        int seconds = 0;
        code = code.trim();
        while (code.length() > 0) {
            char c = code.charAt(code.length()-1);
            int index = code.lastIndexOf(' ');
            String numTxt = code.substring(index+1, code.length()-1);
            code = code.substring(0,index+1).trim();
            try {
                int num = Integer.parseInt(numTxt);
                switch (c) {
                    case 'w': 
                        seconds += num*daysPerWeek*hoursPerDay*3600;
                        break;
                    case 'd':
                        seconds += num*hoursPerDay*3600;
                        break;
                    case 'h':
                        seconds += num*3600;
                        break;
                    case 'm':
                        seconds += num*60;
                        break;
                    default:
                        return -1;
                }
            } catch (NumberFormatException nfex) {
                return -1;
            }
        }
        return seconds;
    }

    public static String dateByMillis(String text, boolean includeTime) {
        if (text.trim().length() > 0) {
            try {
                long millis = Long.parseLong(text);
                DateFormat format = includeTime ? DateFormat.getDateTimeInstance() : DateFormat.getDateInstance();
                return format.format(new Date(millis));
            } catch (NumberFormatException nfex) {
                nfex.printStackTrace();
            }
        }
        return ""; // NOI18N
    }

    public static Date dateByMillis(String text) {
        if (text.trim().length() > 0) {
            try {
                long millis = Long.parseLong(text);
                return new Date(millis);
            } catch (NumberFormatException nfex) {
                nfex.printStackTrace();
            }
        }
        return null;
    }

    public static Repository getRepository(JiraRepository jiraRepository) {
        Repository repository = RepositoryManager.getInstance().getRepository(JiraConnector.ID, jiraRepository.getID());
        if(repository == null) {
            repository = createRepository(jiraRepository);
        }
        return repository;
    }

    public static Repository createRepository(JiraRepository jiraRepository) {
        return Jira.getInstance().getBugtrackingFactory().createRepository(
                jiraRepository,
                Jira.getInstance().getStatusProvider(),
                Jira.getInstance().getSchedulingProvider(), 
                Jira.getInstance().getPriorityProvider(jiraRepository),
                JiraIssueFinder.getInstance());
    }
    
    public static void openIssue(NbJiraIssue jiraIssue) {
        Jira.getInstance().getBugtrackingFactory().openIssue(jiraIssue.getRepository(), jiraIssue);
    }

    public static void openQuery(JiraQuery jiraQuery) {
        Jira.getInstance().getBugtrackingFactory().editQuery(jiraQuery.getRepository(), jiraQuery);
    }    
    
    public static boolean isLeaveOperation (TaskOperation value) {
        return "leave".equals(value.getOperationId());
    }

    public static String toReadable (JiraConfiguration config, String projectId, NbJiraIssue.IssueField field, String value) {
        if (config != null) {
            switch (field) {
                case TYPE:
                    IssueType type = config.getIssueTypeById(value);
                    if (type != null) {
                        value = type.getName();
                    }
                    break;
                case STATUS:
                    JiraStatus status = config.getStatusById(value);
                    if (status != null) {
                        value = status.getName();
                    }
                    break;
                case RESOLUTION:
                    Resolution res = config.getResolutionById(value);
                    if (res != null) {
                        value = res.getName();
                    }
                    break;
                case PRIORITY:
                    Priority priority = config.getPriorityById(value);
                    if (priority != null) {
                        value = priority.getName();
                    }
                    break;
                case COMPONENT:
                    if (!projectId.isEmpty()) {
                        com.atlassian.connector.eclipse.internal.jira.core.model.Component comp = config.getComponentById(projectId, value);
                        if (comp != null) {
                            value = comp.getName();
                        }
                    }
                    break;
                case FIXVERSIONS:
                case AFFECTSVERSIONS:
                    if (!projectId.isEmpty()) {
                        Version version = config.getVersionById(projectId, value);
                        if (version != null) {
                            value = version.getName();
                        }
                    }
                    break;
                case ASSIGNEE:
                    User user = config.getUser(value);
                    if (user != null) {
                        value = user.getFullName();
                    }
                    break;
                case PROJECT:
                    Project project = config.getProjectById(value);
                    if (project != null) {
                        value = project.getName();
                    }
                    break;
                default:
                    break;
            }
        }
        return value;
    }

    public static String mergeValues (List<String> values) {
        String newValue;
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (sb.length() != 0) {
                sb.append(',');
            }
            sb.append(value);
        }
        newValue = sb.toString();
        return newValue;
    }
}
