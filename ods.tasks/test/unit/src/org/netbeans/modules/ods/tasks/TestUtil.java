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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ods.tasks;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 *
 * @author tomas
 */
public class TestUtil {
    
    public static void handleException(Exception exception) throws Throwable {
        exception.printStackTrace();
        if (exception instanceof CoreException) {
            CoreException e = (CoreException) exception;
            IStatus status = e.getStatus();
            if (status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                String html = rs.getHtmlMessage();
                throw new Exception(html);
            }
            if (e.getStatus().getException() != null) {
                throw e.getStatus().getException();
            }
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        throw exception;
    }

    public static RepositoryResponse postTaskData(AbstractRepositoryConnector cfcrc, TaskRepository repository, TaskData data) throws CoreException {
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        return  cfcrc.getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
    }

    public static TaskData getTaskData(AbstractRepositoryConnector cfcrc, TaskRepository taskRepository, String id) throws CoreException {
        return cfcrc.getTaskData(taskRepository, id, new NullProgressMonitor());
    }

//    public static String createIssue(BugzillaRepository repo, String summary) throws MalformedURLException, CoreException {
//        BugzillaRepositoryConnector brc = Bugzilla.getInstance().getRepositoryConnector();
//        TaskRepository tr = repo.getTaskRepository();
//        TaskData data = TestUtil.createTaskData(brc, tr, summary, ISSUE_DESCRIPTION, ISSUE_SEVERITY);
//        RepositoryResponse rr = TestUtil.postTaskData(brc, tr, data);
//        return rr.getTaskId();
//    }
////
////    public static RepositoryResponse addComment(BugzillaRepository repository, TaskData data, String comment) throws CoreException {
////        return addComment(repository.getTaskRepository(), id, comment);
////    }
//
//    public static RepositoryResponse addComment(TaskRepository taskRepository, String id, String comment) throws CoreException {
//        TaskData data = getTaskData(taskRepository, id);
//        return addComment(taskRepository, data, comment);
//    }
//
//    public static RepositoryResponse addComment(TaskRepository taskRepository, TaskData data, String comment) throws CoreException {
//        TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
//        ta.setValue(comment);
//
//        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
//        attrs.add(ta);
//        return Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(taskRepository, data, attrs, new NullProgressMonitor());
//    }
//
//    public static BugzillaRepository getRepository(String name, String url, String user, String psswd) {
//        RepositoryInfo info = new RepositoryInfo(name, BugzillaConnector.ID, url, name, name, user, null, psswd.toCharArray(), null);
//        return new BugzillaRepository(info);
//    }
//
//    public static void validate(BugzillaRepositoryConnector brc, TaskRepository repository) throws Throwable {
//        try {
//            brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR).validate(NULL_PROGRESS_MONITOR);
//        } catch (Exception ex) {
//            handleException(ex);
//        }
//    }
    
}
