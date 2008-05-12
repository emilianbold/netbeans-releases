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

package org.netbeans.modules.parsing.impl;

import java.util.ArrayList;
import java.util.Collection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskScheduler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 *
 * @author Jan Jancura
 */
public class Scheduler {

    private static Collection<? extends TaskScheduler> taskSchedulers;
    
    static void init () {
        System.out.println("Scheduler.init");
        taskSchedulers = Lookup.getDefault ().lookupAll (TaskScheduler.class);
        System.out.println("  schedullers " + taskSchedulers.size ());
    }
    
    private static Map<TaskScheduler,Map<Source,Collection<SchedulerTask>>> tasks = new HashMap<TaskScheduler, Map<Source, Collection<SchedulerTask>>> (); 
    
    public static void schedule (
        TaskScheduler       taskScheduler,
        Collection<Source>  sources
    ) {
        System.err.println("Scheduler.schedule " + taskScheduler + ":" + sources);
        Map<Source,Collection<SchedulerTask>> sourceToTasks = tasks.get (taskScheduler);
        if (sourceToTasks == null) {
            sourceToTasks = new HashMap<Source,Collection<SchedulerTask>> ();
            tasks.put (taskScheduler, sourceToTasks);
        }
        System.err.println("1");
        Set<Source> oldSources = new HashSet<Source> (sourceToTasks.keySet ());
        System.err.println("2");
        for (Source source : sources) {
            Collection<SchedulerTask> tasks = sourceToTasks.get (source);
            if (tasks == null) {
                System.err.println("createTasks");
                tasks = createTasks (source, taskScheduler);
                System.err.println("createTasks end");
                sourceToTasks.put (source, tasks);
                for (SchedulerTask task : tasks)
                    TaskProcessor.addPhaseCompletionTask (task, source);
            } else
                for (SchedulerTask task : tasks)
                    TaskProcessor.rescheduleTask (task, source);
            oldSources.remove (source);
        }
        System.err.println("3");
        for (Source source : oldSources) {
            Collection<SchedulerTask> tasks = sourceToTasks.remove (source);
            for (SchedulerTask task : tasks)
                TaskProcessor.removePhaseCompletionTask (task, source);
        }
        System.err.println("4");
    }
    
    private static Collection<SchedulerTask> createTasks (
        Source              source, 
        TaskScheduler       taskScheduler
    ) {
        List<SchedulerTask> tasks = new ArrayList<SchedulerTask> ();
        String mimeType = source.getMimeType ();
        Lookup lookup = getLookup (mimeType);
        System.out.println("getLookup end ");
        for (TaskFactory factory : lookup.lookupAll (TaskFactory.class)) {
            for (SchedulerTask task : factory.create (source))
                if (task.getSchedulerClass () == taskScheduler.getClass ())
                    tasks.add (task);
        }
        return tasks;
    }
    
    private static Lookup getLookup (String mimeType) {
        System.err.println("getLookup " + mimeType);
        if (mimeType.equals ("content/unknown"))
            return Lookup.EMPTY;
//        FileSystem fileSystem = Repository.getDefault ().getDefaultFileSystem ();
//        FileObject fileObject1 = fileSystem.findResource ("Editors");
//        DataFolder dataFolder1 = fileObject1 == null ? null : DataFolder.findFolder (fileObject1);
//        FileObject fileObject2 = fileSystem.findResource ("Editors/" + mimeType);
//        DataFolder dataFolder2 = fileObject2 == null ? null : DataFolder.findFolder (fileObject2);
//        System.err.println("getLookup 2 " + dataFolder1 + ":" + dataFolder2);
        return new ProxyLookup (
            Lookups.forPath ("Editors"),
            Lookups.forPath ("Editors" + mimeType)
        );
//        return dataFolder2 == null ? 
//            (Lookup) (dataFolder1 == null ? 
//                Lookup.EMPTY :
//                new FolderLookup (dataFolder1).getLookup ()) :
//            new ProxyLookup (
//                new FolderLookup (dataFolder1).getLookup (),
//                new FolderLookup (dataFolder2).getLookup ()
//            );
    }
}



