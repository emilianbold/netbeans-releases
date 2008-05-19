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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskScheduler;
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
        taskSchedulers = Lookup.getDefault ().lookupAll (TaskScheduler.class);
    }
    
    private static Map<TaskScheduler,Map<Source,Collection<SchedulerTask>>> tasks = new HashMap<TaskScheduler, Map<Source, Collection<SchedulerTask>>> (); 
    
    
    static Collection<? extends TaskScheduler> getTaskScheduledsForSource (final Source source) {        
        final Collection<TaskScheduler> result = new LinkedList<TaskScheduler>();
        synchronized (tasks) {
            for (final TaskScheduler schedulter : taskSchedulers) {
                final Map<Source,?> value = tasks.get(schedulter);
                if (value != null && value.keySet().contains(source)) {
                    result.add(schedulter);
                }
            }
        }
        return result;
    }
    
    //tzezula: Live fast, die young.
    //Shouldn't the tasks be guarded? 
    //Shouldn't the access to flags be guarded too?
    //How the lock ordering is defined?
    //Shouldn't it reset {@link SourceFlags.CHANGE+EXPECTED}?
    public static void schedule (
        TaskScheduler       taskScheduler,
        Collection<Source>  sources
    ) {
        Map<Source,Collection<SchedulerTask>> sourceToTasks = tasks.get (taskScheduler);
        if (sourceToTasks == null) {
            sourceToTasks = new HashMap<Source,Collection<SchedulerTask>> ();
            tasks.put (taskScheduler, sourceToTasks);
        }
        Set<Source> oldSources = new HashSet<Source> (sourceToTasks.keySet ());
        for (Source source : sources) {
            SourceAccessor.getINSTANCE ().getFlags (source).add (SourceFlags.INVALID);
            Collection<SchedulerTask> tasks = sourceToTasks.get (source);
            if (tasks == null) {
                tasks = createTasks (source, taskScheduler);
                sourceToTasks.put (source, tasks);
                for (SchedulerTask task : tasks)
                    TaskProcessor.addPhaseCompletionTask (task, source);
            } else
                for (SchedulerTask task : tasks)
                    TaskProcessor.rescheduleTask (task, source);
            oldSources.remove (source);
        }
        for (Source source : oldSources) {
            Collection<SchedulerTask> tasks = sourceToTasks.remove (source);
            for (SchedulerTask task : tasks)
                TaskProcessor.removePhaseCompletionTask (task, source);
        }
    }
    
    private static Collection<SchedulerTask> createTasks (
        Source              source, 
        TaskScheduler       taskScheduler
    ) {
        List<SchedulerTask> tasks = new ArrayList<SchedulerTask> ();
        String mimeType = source.getMimeType ();
        Lookup lookup = getLookup (mimeType);
        for (TaskFactory factory : lookup.lookupAll (TaskFactory.class)) {
            Collection<SchedulerTask> newTasks = factory.create (source);
            if (newTasks != null)
                for (SchedulerTask task : newTasks)
                    if (task.getSchedulerClass () == taskScheduler.getClass ())
                        tasks.add (task);
        }
        return tasks;
    }
    
    private static Lookup getLookup (String mimeType) {
//        if (mimeType.equals ("content/unknown"))
//            return Lookup.EMPTY;
        return new ProxyLookup (
            Lookups.forPath ("Editors/text/base"),
            Lookups.forPath ("Editors" + mimeType)
        );
    }
}



