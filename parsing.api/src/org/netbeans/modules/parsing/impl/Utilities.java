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

import java.util.Collections;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.event.EventSupport;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.openide.util.Parameters;

/**
 * Temporary helpe functions needed by the java.source
 * @author Tomas Zezula
 */
public class Utilities {
    
    private Utilities () {}

    //Helpers for java reformatter, may be removed when new reformat api will be done
    public static void acquireParserLock () {
        TaskProcessor.acquireParserLock();
    }

    public static void releaseParserLock () {
        TaskProcessor.releaseParserLock();
    }

    //Helpers for asserts in java.source    
    public static boolean holdsParserLock () {
        return TaskProcessor.holdsParserLock();
    }

    /**
     * Returns true if given thread is a TaskProcessor dispatch thread.
     * @param Thread thread
     * @return boolean
     */
    public static boolean isTaskProcessorThread (final Thread thread) {
        Parameters.notNull("thread", thread);
        return TaskProcessor.factory.isDispatchThread(thread);
    }

    //Helpers for indexing in java.source, will be removed when indexing will be part of parsing api
    /**
     * Temporary may be replaced by scheduler, hepefully.
     */
    public static void scheduleSpecialTask (final SchedulerTask task) {
        TaskProcessor.scheduleSpecialTask(task);
    }

    /**
     * Sets the {@link IndexingStatus}
     * @param st an {@link IndexingStatus}
     */
    public static void setIndexingStatus (final IndexingStatus st) {
        assert st != null;
        assert status == null;
        status = st;
    }

    /**
     * Asks the {@link IndexingStatus} about state of indexing
     * @return true when indexing is active
     */
    public static boolean isScanInProgress () {
        if (status == null) {
            return RepositoryUpdater.getDefault().isScanInProgress();
        } else {
            return status.isScanInProgress();
        }
    }
    //where
    private static volatile IndexingStatus status;

    /**
     * Provides state of indexing
     */
    public static interface IndexingStatus {
        boolean isScanInProgress ();
    }

    //Helpers to bridge java.source factories into parsing.api
    public static void revalidate (final Source source) {
        final EventSupport support = SourceAccessor.getINSTANCE().getEventSupport(source);
        assert support != null;
        support.resetState(true, -1, -1);
    }
    
    public static void addParserResultTask (final ParserResultTask<?> task, final Source source) {
        Parameters.notNull ("task", task);
        Parameters.notNull ("source", source);
        SourceCache cache = SourceAccessor.getINSTANCE ().getCache (source);
        TaskProcessor.addPhaseCompletionTasks (Collections.<SchedulerTask>singleton (task), cache, true, null);
    }
    
    public static void removeParserResultTask (final ParserResultTask<?> task, final Source source) {
        Parameters.notNull ("task", task);
        Parameters.notNull ("source", source);
        TaskProcessor.removePhaseCompletionTasks(Collections.singleton(task), source);
    }
    
    public static void rescheduleTask (final ParserResultTask<?> task, final Source source) {
        Parameters.notNull ("task", task);
        Parameters.notNull ("source", source);
        TaskProcessor.rescheduleTasks (Collections.<SchedulerTask>singleton (task), source, null);
    }
}
