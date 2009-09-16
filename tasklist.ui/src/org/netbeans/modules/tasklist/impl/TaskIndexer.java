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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import org.netbeans.modules.tasklist.trampoline.Accessor;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;

/**
 * Called from Indexing API framework. Simply asks all registered and active
 * FileTaskProviders to scan files provided by Indexing framework.
 *
 * @author S. Aubrecht
 */
public class TaskIndexer extends CustomIndexer {

    private final TaskList taskList;
    private final static Logger LOG = Logger.getLogger(TaskIndexer.class.getName());

    static final String KEY_SCANNER = "scanner"; //NOI18N
    static final String KEY_TASK = "task"; //NOI18N

    public TaskIndexer( TaskList taskList ) {
        this.taskList = taskList;
    }

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        TaskManagerImpl tm = TaskManagerImpl.getInstance();
        if( !tm.isObserved() ) {
            tm.makeCacheDirty();
            return;
        }
        TaskFilter filter = tm.getFilter();
        if( null == filter )
            filter = TaskFilter.EMPTY;
        TaskScanningScope scope = tm.getScope();
        ArrayList<FileTaskScanner> scanners = null;
        try {
            IndexingSupport is = IndexingSupport.getInstance(context);
            for( Indexable idx : files ) {
                if (context.isCancelled()) {
                    LOG.log(Level.FINE, "Indexer cancelled"); //NOI18N
                    return;
                }
                if( null == scanners ) {
                    scanners = new ArrayList<FileTaskScanner>( 20 );
                    for( FileTaskScanner s : tm.getFileScanners() ) {
                        if( filter.isEnabled(s) ) {
                            s.notifyPrepare();
                            scanners.add(s);
                            LOG.fine("Using FileTaskScanner: " + s); //NOI18N
                        }
                    }
                }
                FileObject root = context.getRoot();
                if( null == root ) {
                    LOG.log(Level.FINE, "Context root not available");
                    return;
                }
                FileObject fo = root.getFileObject(idx.getRelativePath());
                if( null == fo ) {
                    LOG.log(Level.FINE, "Cannot find file [%0] under root [%1]", new Object[] {idx.getRelativePath(), root});
                    continue;
                }
                is.removeDocuments(idx);
                IndexDocument doc = null;
                for( FileTaskScanner scanner : scanners ) {
                    List<? extends Task> tasks = scanner.scan(fo);
                    if( null == tasks )
                        continue;
                    if( scope.isInScope(fo) )
                        taskList.update(scanner, fo, new ArrayList<Task>(tasks), filter);
                    if( !tasks.isEmpty() ) {
                        if( null == doc ) {
                            doc = is.createDocument(idx);
                            is.addDocument(doc);
                            doc.addPair(KEY_SCANNER, ScannerDescriptor.getType(scanner), true, true);
                        }
                        for( Task t : tasks ) {
                            doc.addPair(KEY_TASK, encode(t), false, true);
                        }
                    }
                }
            }
        } catch( IOException ioE ) {
            LOG.log(Level.INFO, "Error while scanning file for tasks.", ioE);
        } finally {
            if( null != scanners ) {
                for( FileTaskScanner s : scanners ) {
                    s.notifyFinish();
                }
            }
        }
    }

    private static String encode( Task t ) {
        StringBuffer res = new StringBuffer();
        URL url = Accessor.DEFAULT.getURL(t);
        if( null == url )
            res.append("-");
        else
            res.append(url.toExternalForm());
        res.append("\n");
        res.append( Accessor.DEFAULT.getLine(t) );
        res.append("\n");
        res.append( Accessor.DEFAULT.getGroup(t).getName() );
        res.append("\n");
        res.append( Accessor.DEFAULT.getDescription(t) );
        return res.toString();
    }

    public static Task decode( FileObject fo, String encodedTask ) {
        int delimIndex = encodedTask.indexOf("\n");
        String strUrl = encodedTask.substring(0, delimIndex);
        URL url = null;
        if( !"-".equals(strUrl) ) {
            try {
                url = new URL(strUrl);
            } catch( MalformedURLException ex ) {
                //ignore
            }
        }
        encodedTask = encodedTask.substring(delimIndex+1);
        delimIndex = encodedTask.indexOf("\n");

        int lineNumber = Integer.valueOf(encodedTask.substring(0, delimIndex));
        encodedTask = encodedTask.substring(delimIndex+1);
        delimIndex = encodedTask.indexOf("\n");

        String groupName = encodedTask.substring(0, delimIndex);
        String description = encodedTask.substring(delimIndex+1);
        if( null != url )
            return Task.create(url, groupName, description);
        return Task.create(fo, groupName, description, lineNumber);
    }
}
