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

package org.netbeans.modules.tasklist.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.modules.tasklist.impl.TaskManagerImpl;
import org.netbeans.modules.tasklist.trampoline.Accessor;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author sa
 */
public class TaskIndexer extends CustomIndexer {

    private final TaskList taskList;

    public TaskIndexer( TaskList taskList ) {
        this.taskList = taskList;
    }

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        try {
            //TODO create lazily
            TaskManagerImpl tm = TaskManagerImpl.getInstance();
            TaskFilter filter = tm.getFilter();
            TaskScanningScope scope = tm.getScope();
            ArrayList<FileTaskScanner> scanners = new ArrayList<FileTaskScanner>( 20 );
            for( FileTaskScanner s : tm.getFileScanners() ) {
                if( filter.isEnabled(s) ) {
                    s.notifyPrepare();
                    scanners.add(s);
                }
            }
            IndexingSupport is = IndexingSupport.getInstance(context);
            System.out.println("---- Context: " + context.getRoot());
            for( Indexable idx : files ) {
                System.out.println("Indexing: " + idx.getRelativePath());
                ProxyFileObject fo = null;
                IndexDocument doc = null;
                for( FileTaskScanner scanner : scanners ) {
                    if( null == fo )
                        fo = new ProxyFileObject(context.getRoot(), idx);
                    List<? extends Task> tasks = scanner.scan(fo);
                    if( null == tasks || tasks.isEmpty() )
                        continue;
                    if( scope.isInScope(fo) )
                        taskList.update(scanner, fo, new ArrayList<Task>(tasks), filter);
                    if( null == doc ) {
                        doc = is.createDocument(idx);
                        is.addDocument(doc);
                        doc.addPair("scanner", ScannerDescriptor.getType(scanner), true, true);
                    }
                    for( Task t : tasks ) {
                        doc.addPair("task", encode(t), false, true);
                    }
                }
            }
            for( FileTaskScanner s : scanners ) {
                s.notifyFinish();
            }
        } catch( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static String encode( Task t ) {
        int line = Accessor.DEFAULT.getLine(t);
        String group = Accessor.DEFAULT.getGroup(t).getName();
        String description = Accessor.DEFAULT.getDescription(t);
        return String.valueOf(line) + "\n" + group + "\n" + description;
    }

    public static Task decode( FileObject fo, String encodedTask ) {
        int delimIndex = encodedTask.indexOf("\n");
        int lineNumber = Integer.valueOf(encodedTask.substring(0, delimIndex));
        encodedTask = encodedTask.substring(delimIndex+1);
        delimIndex = encodedTask.indexOf("\n");
        String groupName = encodedTask.substring(0, delimIndex);
        String description = encodedTask.substring(delimIndex+1);
        return Task.create(fo, groupName, description, lineNumber);
    }
}
