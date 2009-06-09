/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.csl.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Task provider which provides tasks for the tasklist corresponding
 * to hints in files.
 * 
 * @todo Register via instanceCreate to ensure this is a singleton
 *   (Didn't work - see uncommented code below; try to fix.)
 * @todo Exclude tasks that are not Rule#showInTaskList==true
 * 
 * Much of this class is based on the similar JavaTaskProvider in
 * java/source by Stanislav Aubrecht and Jan Lahoda
 * 
 * @author Jan Jancura
 */
public class GsfTaskProvider extends PushTaskScanner  {

    private static GsfTaskProvider
                                INSTANCE;

    private Callback            callback;

    public GsfTaskProvider () {
        this (getAllLanguageNames ());
        INSTANCE = this;
    }

    private GsfTaskProvider (String languageList) {
        super (
            NbBundle.getMessage (GsfTaskProvider.class, "GsfTasks", languageList),
            NbBundle.getMessage (GsfTaskProvider.class, "GsfTasksDesc", languageList),
            null
        );
    }

    @Override
    public synchronized void setScope (TaskScanningScope scope, Callback callback) {
        this.callback = callback;
        
        if (scope == null || callback == null) return;
        
        for (FileObject file : scope.getLookup ().lookupAll (FileObject.class))
            refreshImpl (file);
        
        for (Project project : scope.getLookup ().lookupAll (Project.class)) {
            Collection<FileObject> fileObjects = QuerySupport.findRoots (
                project,
                Collections.<String> singleton (ClassPath.SOURCE),
                Collections.<String> emptyList (),
                Collections.<String> emptyList ()
            );
            try {
                QuerySupport querySupport = QuerySupport.forRoots (
                    "TLIndexer",
                    1,
                    fileObjects.toArray (new FileObject [fileObjects.size ()])
                );
                Collection<? extends IndexResult> results = querySupport.query (
                    "description",
                    "",
                    Kind.PREFIX,
                    "description",
                    "lineNumber",
                    "groupName"
                );
                final Map<FileObject,List<Task>> tasksMap = new HashMap<FileObject,List<Task>> ();
                for (IndexResult result : results) {
                    FileObject file = result.getFile ();
                    if (file == null) continue;
                    if (result.getValue ("groupName") == null) continue;
                    List<Task> tasksList = tasksMap.get (file);
                    if (tasksList == null) {
                        tasksList = new ArrayList<Task> ();
                        tasksMap.put (file, tasksList);
                    }
                    int lineNumber = 1;
                    try {
                        lineNumber = Integer.parseInt (
                            result.getValue ("lineNumber")
                        );
                    } catch (NumberFormatException ex) {
                    }
                    tasksList.add (Task.create (
                        file,
                        result.getValue ("groupName"),
                        result.getValue ("description"),
                        lineNumber
                    ));
                }
                for (Entry<FileObject,List<Task>> entry : tasksMap.entrySet ())
                    callback.setTasks (entry.getKey (), entry.getValue ());
            } catch (IOException ex) {
                Exceptions.printStackTrace (ex);
            }
        }
    }

    public static void refresh (FileObject file) {
        if (INSTANCE != null) {
            INSTANCE.refreshImpl (file);
        }
    }
    
    private synchronized void refreshImpl (FileObject file) {
        if (callback == null) return;
        Project project = FileOwnerQuery.getOwner (file);
        Collection<FileObject> fileObjects = QuerySupport.findRoots (
            project,
            Collections.<String> singleton (ClassPath.SOURCE),
            Collections.<String> emptyList (),
            Collections.<String> emptyList ()
        );
        try {
            QuerySupport querySupport = QuerySupport.forRoots (
                "TLIndexer",
                1,
                fileObjects.toArray (new FileObject [fileObjects.size ()])
            );
            Collection<? extends IndexResult> results = querySupport.query (
                "description",
                "",
                Kind.PREFIX,
                "description",
                "lineNumber",
                "groupName"
            );
            final List<Task> tasks = new ArrayList<Task> ();
            for (IndexResult result : results) {
                FileObject file2 = result.getFile ();
                if (file2 == null) continue;
                if (!file2.equals (file)) continue;
                if (result.getValue ("description") == null)
                    continue;
                int lineNumber = 1;
                try {
                    lineNumber = Integer.parseInt (
                        result.getValue ("lineNumber")
                    );
                } catch (NumberFormatException ex) {
                }
                tasks.add (Task.create (
                    file,
                    result.getValue ("groupName"),
                    result.getValue ("description"),
                    lineNumber
                ));
            }
            callback.setTasks (file, tasks);
        } catch (IOException ex) {
            Exceptions.printStackTrace (ex);
        }
    }

    /* package */ static String getAllLanguageNames () {
        StringBuilder sb = new StringBuilder ();
        for (Language language : LanguageRegistry.getInstance ()) {
            if (sb.length () > 0) {
                sb.append (", "); //NOI18N
            }
            sb.append (language.getDisplayName ());
        }

        return sb.toString ();
    }
}

