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

package org.netbeans.modules.tasklist.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;

/**
 *
 * @author sa
 */
public class Loader implements Runnable, Cancellable {
    
    private boolean cancelled = false;
    private final TaskScanningScope scope;
    private final TaskFilter filter;
    private final TaskList taskList;

    public Loader( TaskScanningScope scope, TaskFilter filter, TaskList taskList ) {
        this.scope = scope;
        this.filter = filter;
        this.taskList = taskList;
    }

    public void run() {
        FileObject[] roots = scope.getLookup().lookup(FileObject[].class);
        if( null == roots || roots.length == 0 )
            return;
        ArrayList<Task> loadedTasks = new ArrayList<Task>(100);
        try {
            QuerySupport qs = QuerySupport.forRoots("TaskListIndexer", 1, roots);
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            for( FileTaskScanner scanner : ScannerList.getFileScannerList().getScanners() ) {
                if( cancelled )
                    return;
                if( !filter.isEnabled(scanner) )
                    continue;

                String scannerId = ScannerDescriptor.getType( scanner );
                Collection<? extends IndexResult> cache = qs.query("scanner", scannerId, QuerySupport.Kind.EXACT, "task");
                for( IndexResult ir : cache ) {
                    if( cancelled )
                        return;
                    FileObject fo = ir.getFile();
                    loadedTasks.clear();
                    String[] tasks = ir.getValues("task");
                    for( String encodedTask : tasks ) {
                        if( cancelled )
                            return;

                        Task t = TaskIndexer.decode(fo, encodedTask);
                        loadedTasks.add(t);
                    }
                    if( cancelled )
                        return;
                    taskList.update(scanner, fo, loadedTasks, filter);
                }
            }
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean cancel() {
        cancelled = true;
        return true;
    }

}
