/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trodademarks of their respective owners.
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
package org.netbeans.modules.odcs.tasks.util;

import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.awt.EventQueue;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevAttribute;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.team.spi.TeamProject;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.bugtracking.commons.ListValuePicker;
import org.netbeans.modules.bugtracking.util.SimpleIssueFinder;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.ODCSConnector;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue;
import org.netbeans.modules.odcs.tasks.query.ODCSQuery;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.commands.GetRepositoryTasksCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class ODCSUtil {
    
    public static final DateFormat DATE_TIME_FORMAT_DEFAULT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //NOI18N
    public static final String URL_FRAGMENT_TASK = "/task/"; //NOI18N
    public static final String URL_FRAGMENT_QUERY = "/tasks/"; //NOI18N
    private static final DateFormat[] dateFormats = new DateFormat[] { 
        DATE_TIME_FORMAT_DEFAULT,
        new SimpleDateFormat("yyyy-MM-dd HH:mm"), //NOI18N
        new SimpleDateFormat("yyyy-MM-dd") //NOI18N
    };
    
    @NbBundle.Messages({"CTL_Error=Error"})
    public static void notifyErrorMsg(String msg) {
        NotifyDescriptor nd =
            new NotifyDescriptor(
                msg,
                Bundle.CTL_Error(),    
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(nd);
    }
    
    public static TaskData createTaskData(TaskRepository taskRepository) {
        
        // XXX is this all we need and how we need it?

        AbstractRepositoryConnector rc = ODCS.getInstance().getRepositoryConnector();
        TaskAttributeMapper attributeMapper = rc.getTaskDataHandler().getAttributeMapper(taskRepository);
        TaskData data = new TaskData(attributeMapper, rc.getConnectorKind(), taskRepository.getRepositoryUrl(), "");
        
        TaskAttribute rta = data.getRoot();
        rta.createMappedAttribute(TaskAttribute.USER_ASSIGNED);
        rta.createMappedAttribute(TaskAttribute.SUMMARY);
        rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
        rta.createMappedAttribute(CloudDevAttribute.TASK_TYPE.getTaskName());
        rta.createMappedAttribute(TaskAttribute.PRODUCT);
        rta.createMappedAttribute(TaskAttribute.COMPONENT);
        rta.createMappedAttribute(CloudDevAttribute.MILESTONE.getTaskName());
        rta.createMappedAttribute(CloudDevAttribute.ITERATION.getTaskName());
        rta.createMappedAttribute(TaskAttribute.PRIORITY);
        rta.createMappedAttribute(TaskAttribute.SEVERITY);
        rta.createMappedAttribute(TaskAttribute.STATUS);
        rta.createMappedAttribute(TaskAttribute.RESOLUTION);
//        ta.setValue(clientData.getResolutions("UNCONFIRMED").getValue());
        rta.createMappedAttribute(CloudDevAttribute.DUPLICATE_OF.getTaskName());
        rta.createMappedAttribute(TaskAttribute.DATE_DUE);
        rta.createMappedAttribute(CloudDevAttribute.FOUND_IN_RELEASE.getTaskName());
        rta.createMappedAttribute(TaskAttribute.KEYWORDS);
//         rta.createMappedAttribute(CloudDevAttribute.EXTERNAL_LINKS);
        rta.createMappedAttribute(TaskAttribute.USER_ASSIGNED);
         rta.createMappedAttribute(TaskAttribute.VERSION);
        rta.createMappedAttribute(CloudDevAttribute.ESTIMATED_TIME.getTaskName());
        rta.createMappedAttribute(CloudDevAttribute.CC.getTaskName());
        rta.createMappedAttribute(CloudDevAttribute.PARENT_TASK.getTaskName());
        rta.createMappedAttribute(CloudDevAttribute.SUBTASKS.getTaskName());
        rta.createMappedAttribute(TaskAttribute.COMMENT_NEW);
        
        TaskAttribute ta = rta.createMappedAttribute(CloudDevAttribute.REPORTER.getTaskName());
        ta.setValue(taskRepository.getUserName());
        
        return data;
    }
     
    public static RepositoryResponse postTaskData(AbstractRepositoryConnector rc, TaskRepository repository, TaskData data) throws CoreException {
        ODCS.LOG.log(Level.FINE, " dataRoot before post {0}", data.getRoot().toString());
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); 
        return postTaskData(rc, repository, data, attrs);
    }

    public static RepositoryResponse postTaskData(AbstractRepositoryConnector rc, TaskRepository repository, TaskData data, Set<TaskAttribute> attrs) throws CoreException {
        RepositoryResponse rr = rc.getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
        return rr;
    }
    
    public static void openIssue(ODCSIssue odcsIssue) {
        ODCS.getInstance().getBugtrackingFactory().openIssue(odcsIssue.getRepository(), odcsIssue);
    }
    
    public static void openQuery(ODCSQuery odcsQuery) {
        ODCS.getInstance().getBugtrackingFactory().editQuery(odcsQuery.getRepository(), odcsQuery);
    }

    public static Repository getRepository(ODCSRepository odcsRepository) {
        //TODO review this, team projects were always initialized again and again
        //this caused problems with listeners
        assert odcsRepository.getKenaiProject() != null : "looks like repository " + odcsRepository.getDisplayName() + " wasn't porperly inititalized via team support."; // NOI18N
        TeamProject teamProject = odcsRepository.getKenaiProject();
        Repository repository = null;
        // It is posible to bypass the generaly contract that it isn't possible 
        // to create an ODCS repository by hand (in such case there always should 
        // be a teamProject available). 
        // for more info see o.n.m.bugtracking.DelegatingConnector#OVERRIDE_REPOSITORY_MANAGEMENT
        if(teamProject != null) {
            repository = TeamUtil.getRepository(teamProject);
        }
        if (repository == null) {
            repository = RepositoryManager.getInstance().getRepository(ODCSConnector.ID, odcsRepository.getID());
            if(repository == null) {
                repository = createRepository(odcsRepository);
            }
        }
        return repository;
    }

    public static Repository createRepository(ODCSRepository odcsRepository) {
        return ODCS.getInstance().getBugtrackingFactory().createRepository(
                odcsRepository,
                ODCS.getInstance().getStatusProvider(),
                null, 
                ODCS.getInstance().getPriorityProvider(odcsRepository),
                SimpleIssueFinder.getInstance());
    }

    public static TaskResolution getResolutionByValue(RepositoryConfiguration rc, String value) {
        List<TaskResolution> resolutions = rc.getResolutions();
        for (TaskResolution r : resolutions) {
            if(r.getValue().equals(value)) {
                return r;
            }
        }
        return null;
    }
    
    public static TaskStatus getStatusByValue(RepositoryConfiguration rc, String value) {
        List<TaskStatus> statuses = rc.getStatuses();
        for (TaskStatus taskStatus : statuses) {
            if(taskStatus.getValue().equals(value)) {
                return taskStatus;
            }
        }
        return null;
    }
    
    public static Priority getPriorityByValue(RepositoryConfiguration rc, String value) {
        List<Priority> priorities = rc.getPriorities();
        for (Priority p : priorities) {
            if(p.getValue().equals(value)) {
                return p;
            }
        }
        return null;
    }
    
    public static TaskSeverity getSeverityByValue(RepositoryConfiguration rc, String value) {
        List<TaskSeverity> severities = rc.getSeverities();
        for (TaskSeverity s : severities) {
            if(s.getValue().equals(value)) {
                return s;
            }
        }
        return null;
    }

    public static Iteration getIterationByValue(RepositoryConfiguration rc, String value) {
        List<Iteration> iterations = rc.getIterations();
        for (Iteration i : iterations) {
            if(i.getValue().equals(value)) {
                return i;
            }
        }
        return null;
    }


    public static Milestone getMilestoneByValue(RepositoryConfiguration rc, String value) {
        List<Milestone> milestones = rc.getMilestones();
        for (Milestone m : milestones) {
            if(m.getValue().equals(value)) {
                return m;
            }
        }
        return null;
    }

    
    public static String getKeywords(String message, String keywordString, ODCSRepository repository) {
        String[] ks = keywordString.split(","); // NOI18N
        if(ks == null || ks.length == 0) {
            return null;
        }

        try {
            RepositoryConfiguration rc = repository.getRepositoryConfiguration(false);
            if(rc == null) {
                return keywordString;
            }
            Collection<Keyword> keywords = rc.getKeywords(); 
            List<String> keywordsList = new ArrayList<String>(keywords.size());
            for (Keyword keyword : keywords) {
                keywordsList.add(keyword.getName());
            }
            return ListValuePicker.getValues(
                    NbBundle.getMessage(ODCSUtil.class, "CTL_KeywordsTitle"), 
                    NbBundle.getMessage(ODCSUtil.class, "LBL_Keywords"), 
                    message, 
                    keywordString, 
                    keywordsList);
        } catch (Exception ex) {
            ODCS.LOG.log(Level.SEVERE, null, ex);
            return keywordString;
        }       
    }
    
    public static String getUsers(String message, String usersString, ODCSRepository repository) {
        String[] users = usersString.split(","); // NOI18N
        if(users == null || users.length == 0) {
            return null;
        }

        try {
            RepositoryConfiguration rc = repository.getRepositoryConfiguration(false);
            if(rc == null) {
                return usersString;
            }
            List<TaskUserProfile> userProfiles = rc.getUsers();
            List<ListValuePicker.ListValue> usersList = new ArrayList<ListValuePicker.ListValue>(userProfiles.size());
            for (TaskUserProfile up : userProfiles) {
                usersList.add(new ListValuePicker.ListValue(up.getRealname() + " (" + up.getLoginName() + ")", up.getLoginName()));
            }
            return ListValuePicker.getValues(
                    NbBundle.getMessage(ODCSUtil.class, "CTL_UsersTitle"), 
                    NbBundle.getMessage(ODCSUtil.class, "LBL_Users"), 
                    message, 
                    usersString, 
                    usersList.toArray(new ListValuePicker.ListValue[usersList.size()]));
        } catch (Exception ex) {
            ODCS.LOG.log(Level.SEVERE, null, ex);
            return usersString;
        }       
    }

    public static Date parseLongDate (String text) {
        return parseLongDate(text, null);
    }
    
    public static Date parseLongDate (String text, DateFormat[] additionalFormats) {
        Date date = null;
        try {
            date = new Date(Long.parseLong(text));
        } catch (NumberFormatException nfe) {
            for (DateFormat format : dateFormats) {
                try {
                    date = format.parse(text);
                    break;
                } catch (ParseException ex) {
                }
            }
            if (additionalFormats != null) {
                for (DateFormat format : additionalFormats) {
                    try {
                        date = format.parse(text);
                        break;
                    } catch (ParseException ex) {
                    }
                }
            }
        }
        if (date == null) {
            ODCS.LOG.log(Level.FINE, "Cannot parse date: {0}", text);
        }
        return date;
    }
    
    public static Date parseTextDate (String text, DateFormat... formats) {
        Date date = null;
        for (DateFormat format : formats) {
            try {
                date = format.parse(text);
                break;
            } catch (ParseException ex) {
            }
        }
        if (date == null) {
            ODCS.LOG.log(Level.FINE, "Cannot parse date: {0}", text);
        }
        return date;
    }

    @NbBundle.Messages({"LBL_Mine=Assigned to me",
                        "LBL_Related=Related to me",
                        "LBL_Recent=Recently changed",
                        "LBL_Open=Open tasks",
                        "LBL_All=All tasks"})
    public static String getPredefinedQueryName(PredefinedTaskQuery ptq) {
        switch(ptq) {
            case ALL:
                return Bundle.LBL_All();
            case MINE:              
                return Bundle.LBL_Mine();
            case OPEN:              
                return Bundle.LBL_Open();
            case RECENT:              
                return Bundle.LBL_Recent();
            case RELATED:              
                return Bundle.LBL_Related();
            default:
                throw new IllegalStateException("unexpected PredefinedTaskQuery value [" + ptq + "]"); // NOI18N
        }
    }

    /**
     * Returns Task for the given issue id or null if an error occurred
     * @param repository
     * @param id
     * @return
     */
    public static NbTask getRepositoryTask (final ODCSRepository repository, final String id, boolean handleExceptions) {
        MylynSupport supp = MylynSupport.getInstance();
        try {
            GetRepositoryTasksCommand cmd = supp.getCommandFactory()
                    .createGetRepositoryTasksCommand(repository.getTaskRepository(), Collections.<String>singleton(id));
            repository.getExecutor().execute(cmd, true, handleExceptions);
            if(cmd.hasFailed()) {
                ODCS.LOG.log(Level.FINE, cmd.getErrorMessage());
            }
            if (cmd.getTasks().isEmpty()) {
                // fallback on local
                NbTask task = supp.getTask(repository.getTaskRepository().getRepositoryUrl(), id);
                if (cmd.hasFailed() && task != null) {
                    return task;
                }
            } else {
                return cmd.getTasks().iterator().next();
            }
        } catch (CoreException ex) {
            ODCS.LOG.log(Level.INFO, null, ex);
        }
        return null;
    }
    
    public static void runInAwt(Runnable r) {
        if(EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }
    
}
