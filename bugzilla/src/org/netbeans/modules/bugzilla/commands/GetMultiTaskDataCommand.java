/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugzilla.commands;

import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;

/**
 * Retrieves the TaskData for all given issue ids
 * 
 * @author Tomas Stupka
 */
public class GetMultiTaskDataCommand extends BugzillaCommand {

    private final BugzillaRepository repository;
    private final Set<String> ids;
    private final TaskDataCollector collector;

    public GetMultiTaskDataCommand(BugzillaRepository repository, Set<String> ids, TaskDataCollector collector) {
        this.repository = repository;
        this.ids = ids;
        this.collector = collector;
    }

    @Override
    public void execute() throws CoreException {
        if(Bugzilla.LOG.isLoggable(Level.FINER)) {
            Bugzilla.LOG.log(Level.FINER, "will retrieve data for issues: {0}", print(ids));    // NOI18N
        }
        Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().getMultiTaskData(
                repository.getTaskRepository(),
                ids,
                collector,
                new NullProgressMonitor());
    }

    private String print(Set<String> ids) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String string : ids) {
            sb.append(string);
            if(++i < ids.size()) {
                sb.append(",");                                                 // NOI18N
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GetMultiTaskDataCommand [repository=");                      // NOI18N
        sb.append(repository.getUrl());
        sb.append(",...]");                                                     // NOI18N
        return sb.toString();
    }

}
