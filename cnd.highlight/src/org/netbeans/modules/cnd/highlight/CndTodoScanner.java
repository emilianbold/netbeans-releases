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
package org.netbeans.modules.cnd.highlight;

import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.tasklist.impl.FileScanningWorker;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner.Callback;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Egor Ushakov <gorrus@netbeans.org>
 */
@ServiceProvider(service=PushTaskScanner.class, path="TaskList/Scanners")
public class CndTodoScanner extends PushTaskScanner {
    private final RequestProcessor RP = new RequestProcessor("CND todo scanner", 1); //NOI18N
    private final TaskManagerImpl taskManager = TaskManagerImpl.getInstance();
    private final FileScanningWorker worker = new FileScanningWorker(taskManager.getTasks(), taskManager.getFilter());
    
    public CndTodoScanner() {
        super(
            NbBundle.getMessage(CndTodoScanner.class, "CndTodoTasks"), //NOI18N
            NbBundle.getMessage(CndTodoScanner.class, "CndTodoTasksDesc"), //NOI18N
            null);  //NOI18N
        RP.submit(worker);
    }
    
    @Override
    public void setScope(TaskScanningScope scope, Callback callback) {
        if (scope == null || callback == null) {
            return;
        }
        
        final Set<FileObject> files = new WeakSet<FileObject>();
        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            files.add(file);
        }

        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            NativeProject prj = p.getLookup().lookup(NativeProject.class);
            if (prj != null) {
                for (NativeFileItem nativeFileItem : prj.getAllFiles()) {
                    files.add(nativeFileItem.getFileObject());
                }
            }
        }
        worker.scan(files.iterator(), taskManager.getFilter());
    }
}
