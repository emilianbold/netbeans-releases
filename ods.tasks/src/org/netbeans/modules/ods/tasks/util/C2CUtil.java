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
package org.netbeans.modules.ods.tasks.util;

import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.kenai.spi.KenaiUtil;
import org.netbeans.modules.bugtracking.util.ListValuePicker;
import org.netbeans.modules.ods.tasks.C2C;
import org.netbeans.modules.ods.tasks.C2CConnector;
import org.netbeans.modules.ods.tasks.issue.C2CIssue;
import org.netbeans.modules.ods.tasks.query.C2CQuery;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.mylyn.util.GetTaskDataCommand;
import org.netbeans.modules.ods.tasks.kenai.KenaiRepository;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class C2CUtil {
    
    public static final DateFormat DATE_TIME_FORMAT_DEFAULT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //NOI18N
    public static final String URL_FRAGMENT_TASK = "/task/"; //NOI18N
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

        C2CData clientData = getClientData(C2C.getInstance().getRepositoryConnector(), taskRepository);
        
        AbstractRepositoryConnector rc = C2C.getInstance().getRepositoryConnector();
        TaskAttributeMapper attributeMapper = rc.getTaskDataHandler().getAttributeMapper(taskRepository);
        TaskData data = new TaskData(attributeMapper, rc.getConnectorKind(), taskRepository.getRepositoryUrl(), "");
        
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.createMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
        ta = rta.createMappedAttribute(C2CData.ATTR_TASK_TYPE);
        ta = rta.createMappedAttribute(TaskAttribute.PRODUCT);
        ta = rta.createMappedAttribute(TaskAttribute.COMPONENT);
        ta = rta.createMappedAttribute(C2CData.ATTR_MILESTONE);
        ta = rta.createMappedAttribute(C2CData.ATTR_ITERATION);
        ta = rta.createMappedAttribute(TaskAttribute.PRIORITY);
        ta = rta.createMappedAttribute(TaskAttribute.SEVERITY);
        ta = rta.createMappedAttribute(TaskAttribute.STATUS);
        ta = rta.createMappedAttribute(TaskAttribute.RESOLUTION);
//        ta.setValue(clientData.getResolutions("UNCONFIRMED").getValue());
        ta = rta.createMappedAttribute(C2CData.ATTR_DUPLICATE_OF);
        ta = rta.createMappedAttribute(C2CData.ATTR_DUEDATE);
        ta = rta.createMappedAttribute(C2CData.ATTR_FOUND_IN_RELEASE);
        ta = rta.createMappedAttribute(C2CData.ATTR_TAGS);
        ta = rta.createMappedAttribute(C2CData.ATTR_EXTERNAL_LINKS);
        ta = rta.createMappedAttribute(C2CData.ATTR_OWNER);
        ta = rta.createMappedAttribute(C2CData.ATTR_VERSION);
        ta = rta.createMappedAttribute(C2CData.ATTR_ESTIMATE_WITH_UNITS);
        ta = rta.createMappedAttribute(C2CData.ATTR_REPORTER);
        ta = rta.createMappedAttribute(C2CData.ATTR_CC);
        ta = rta.createMappedAttribute(C2CData.ATTR_NEWCC);
        ta = rta.createMappedAttribute(C2CData.ATTR_PARENT);
        ta = rta.createMappedAttribute(C2CData.ATTR_SUBTASK);
        ta = rta.createMappedAttribute(C2CData.ATTR_NEWCOMMENT);
        
        
//        ta.setValue(clientData.get().get(0).getValue());
        
//        ta = rta.createMappedAttribute(BugzillaAttribute.PRODUCT.getKey());
//        ta.setValue(TEST_PROJECT);
//
//        String platform = client.getRepositoryConfiguration().getPlatforms().get(0);
//        ta = rta.createMappedAttribute(BugzillaAttribute.REP_PLATFORM.getKey());
//        ta.setValue(platform);
//
//        String version = client.getRepositoryConfiguration().getVersions(TEST_PROJECT).get(0);
//        ta = rta.createMappedAttribute(BugzillaAttribute.VERSION.getKey());
//        ta.setValue(version);
//
//        String component = client.getRepositoryConfiguration().getComponents(TEST_PROJECT).get(0);
//        ta = rta.createMappedAttribute(BugzillaAttribute.COMPONENT.getKey());
//        ta.setValue(component);

        return data;
    }
     
    public static RepositoryResponse postTaskData(AbstractRepositoryConnector cfcrc, TaskRepository repository, TaskData data) throws CoreException {
        C2C.LOG.log(Level.FINE, " dataRoot before post {0}", data.getRoot().toString());
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        return postTaskData(cfcrc, repository, data, attrs);
    }

    public static RepositoryResponse postTaskData(AbstractRepositoryConnector cfcrc, TaskRepository repository, TaskData data, Set<TaskAttribute> attrs) throws CoreException {
        RepositoryResponse rr = cfcrc.getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
        return rr;
    }
    
    /**
     * Returns TaskData for the given issue id or null if an error occured
     * @param repository
     * @param id
     * @return
     */
    public static TaskData getTaskData(final C2CRepository repository, final String id) {
        return getTaskData(repository, id, true);
    }

    /**
     * Returns TaskData for the given issue id or null if an error occured
     * @param repository
     * @param id
     * @return
     */
    public static TaskData getTaskData(final C2CRepository repository, final String id, boolean handleExceptions) {
        GetTaskDataCommand cmd = new GetTaskDataCommand(C2C.getInstance().getRepositoryConnector(), repository.getTaskRepository(), id);
        repository.getExecutor().execute(cmd, true, handleExceptions);
        if(cmd.hasFailed() && C2C.LOG.isLoggable(Level.FINE)) {
            C2C.LOG.log(Level.FINE, cmd.getErrorMessage());
        }
        return cmd.getTaskData();
    }
    
    public static void openIssue(C2CIssue c2cIssue) {
        C2C.getInstance().getBugtrackingFactory().openIssue(getRepository(c2cIssue.getRepository()), c2cIssue);
    }
    
    public static void openQuery(C2CQuery c2cQuery) {
        C2C.getInstance().getBugtrackingFactory().openQuery(getRepository(c2cQuery.getRepository()), c2cQuery);
    }

    public static Repository getRepository(C2CRepository c2cRepository) {
        //TODO review this, team projects were always initialized again and again
        //this caused problems with listeners
        Repository repository = null;
        if (c2cRepository instanceof KenaiRepository) {
            repository = KenaiUtil.getRepository(((KenaiRepository) c2cRepository).getKenaiProject());
        }
        if (repository == null) {
            repository = createRepository(c2cRepository);
        }
        return repository;
    }

    public static Repository createRepository (C2CRepository c2cRepository) {
        Repository repository = C2C.getInstance().getBugtrackingFactory().getRepository(C2CConnector.ID, c2cRepository.getID());
        if(repository == null) {
            repository = C2C.getInstance().getBugtrackingFactory().createRepository(
                    c2cRepository, 
                    C2C.getInstance().getRepositoryProvider(), 
                    C2C.getInstance().getQueryProvider(),
                    C2C.getInstance().getIssueProvider());
        }
        return repository;
    }

    public static TaskResolution getResolutionByValue(C2CData cd, String value) {
        List<TaskResolution> resolutions = cd.getResolutions();
        for (TaskResolution r : resolutions) {
            if(r.getValue().equals(value)) {
                return r;
            }
        }
        return null;
    }

    public static String getKeywords(String message, String keywordString, C2CRepository repository) {
        String[] ks = keywordString.split(","); // NOI18N
        if(ks == null || ks.length == 0) {
            return null;
        }

        try {
            C2CData cd = getClientData(C2C.getInstance().getRepositoryConnector(), repository.getTaskRepository());
            if(cd == null /* XXX */) {
                return keywordString;
            }
            Collection<Keyword> keywords = cd.getKeywords(); 
            List<String> keywordsList = new ArrayList<String>(keywords.size());
            for (Keyword keyword : keywords) {
                keywordsList.add(keyword.getName());
            }
            return ListValuePicker.getValues(
                    NbBundle.getMessage(C2CUtil.class, "CTL_KeywordsTitle"), 
                    NbBundle.getMessage(C2CUtil.class, "LBL_Keywords"), 
                    message, 
                    keywordString, 
                    keywordsList);
        } catch (Exception ex) {
            C2C.LOG.log(Level.SEVERE, null, ex);
            return keywordString;
        }       
    }
    
    public static String getUsers(String message, String usersString, C2CRepository repository) {
        String[] users = usersString.split(","); // NOI18N
        if(users == null || users.length == 0) {
            return null;
        }

        try {
            C2CData cd = getClientData(C2C.getInstance().getRepositoryConnector(), repository.getTaskRepository());
            if(cd == null /* XXX */) {
                return usersString;
            }
            List<TaskUserProfile> userProfiles = cd.getUsers();
            List<ListValuePicker.ListValue> usersList = new ArrayList<ListValuePicker.ListValue>(userProfiles.size());
            for (TaskUserProfile up : userProfiles) {
                usersList.add(new ListValuePicker.ListValue(up.getRealname() + " (" + up.getLoginName() + ")", up.getLoginName()));
            }
            return ListValuePicker.getValues(
                    NbBundle.getMessage(C2CUtil.class, "CTL_UsersTitle"), 
                    NbBundle.getMessage(C2CUtil.class, "LBL_Users"), 
                    message, 
                    usersString, 
                    usersList.toArray(new ListValuePicker.ListValue[usersList.size()]));
        } catch (Exception ex) {
            C2C.LOG.log(Level.SEVERE, null, ex);
            return usersString;
        }       
    }

    public static Date parseDate (String text) {
        return parseDate(text, new DateFormat[0]);
    }
    
    public static Date parseDate (String text, DateFormat[] additionalFormats) {
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
            C2C.LOG.log(Level.FINE, "Cannot parse date: {0}", text);
        }
        return date;
    }

    // XXX remove this - is the same as C2C.getclientData();
    public static C2CData getClientData (AbstractRepositoryConnector repositoryConnector, TaskRepository taskRepository) {
        return C2CExtender.getData(repositoryConnector, taskRepository, false);
    }
}
