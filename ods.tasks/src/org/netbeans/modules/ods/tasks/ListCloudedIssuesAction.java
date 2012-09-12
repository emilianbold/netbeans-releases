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
package org.netbeans.modules.ods.tasks;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;

//@ActionID(
//    category = "Versioning",
//id = "org.netbeans.modules.c2c.tasks.ListCloudedIssuesAction")
//@ActionRegistration(
//    displayName = "#CTL_ListClouded")
//@ActionReference(path = "Menu/Versioning", position = -10, separatorBefore = -60, separatorAfter = 40)
//@Messages("CTL_ListClouded=List Clouded Issues")
public final class ListCloudedIssuesAction implements ActionListener {
    private static final Logger LOG = Logger.getLogger(ListCloudedIssuesAction.class.getName());
    private NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
    
    @Override
    public void actionPerformed(ActionEvent e) {
            listIssues();
    }
    
    private void listIssues() {
        TaskRepository taskRepository = DummyUtils.repository;
        AbstractRepositoryConnector rc = DummyUtils.rc;
        
        LOG.info(" ----------- query 1 ---------------- ");
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), "");            // NOI18N
        query.setAttribute(C2CData.ATTR_TASK_TYPE, "Defect,Feature");
        query.setAttribute("task.common.summary", "test");
        query.setAttribute("task.common.description", "test");
        TaskDataCollector collector = new TaskDataCollector() {
            @Override
            public void accept(TaskData td) {
                LOG.info(" issue " + td.getTaskId() + " " + td.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue());
            }
        };
        IStatus status = rc.performQuery(taskRepository, query, collector, null, new NullProgressMonitor());
        LOG.log(Level.INFO, "status {0}", status);
        LOG.info(" ----------- query 2 ---------------- ");
        LOG.info(" ++++ Query URL : " + query.getUrl());
        
        query = new RepositoryQuery(taskRepository.getConnectorKind(), "");            // NOI18N
        query.setAttribute(C2CData.ATTR_TASK_TYPE, "Defect,Feature");
        query.setAttribute("task.common.summary", "test");
        query.setAttribute("task.common.description", "test");
        
        status = rc.performQuery(taskRepository, query, collector, null, new NullProgressMonitor());
        
        LOG.info(" ++++ Query URL : " + query.getUrl());        
        
        LOG.info(" ----------- predefined query  ---------------- ");
        
        for (PredefinedTaskQuery predefinedId : PredefinedTaskQuery.values()) {
            IRepositoryQuery q = C2CExtender.getQuery(rc, predefinedId, predefinedId.getLabel(), rc.getConnectorKind());
            status = rc.performQuery(taskRepository, q, collector, null, new NullProgressMonitor());
        }
        
//        LOG.info(" ----------- mutitaskdata query  ---------------- ");
//        Set<String> s = new HashSet<String>();
//        s.add("1");
//        s.add("2");
//        s.add("3");
//        try {
//            rc.getTaskDataHandler().getMultiTaskData(taskRepository, s, collector, nullProgressMonitor);
//        } catch (CoreException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }
 
    
}
