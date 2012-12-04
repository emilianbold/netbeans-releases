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
package org.netbeans.modules.parsing.impl.indexing;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Zezula
 */
class VisibilitySupport implements ChangeListener {

    private static final int VISIBILITY_CHANGE_WINDOW = 500;
    private static final Logger LOGGER = Logger.getLogger(VisibilitySupport.class.getName());
    
    //@GuardedBy("visibilityCache")
    private final Map<FileObject,Boolean> visibilityCache = Collections.synchronizedMap(new WeakHashMap<FileObject, Boolean>());    
    private final AtomicReference<LogContext> visibilityLogCtx = new AtomicReference<LogContext>();
    private final RequestProcessor.Task visibilityChanged;
    
    private VisibilitySupport(
            @NonNull final RepositoryUpdater ru,
            @NonNull final RequestProcessor worker) {
        this.visibilityChanged = worker.create(new SlidingTask(ru));
    }

    void start() {
        VisibilityQuery.getDefault().addChangeListener(this);
    }
    
    void stop() {
        VisibilityQuery.getDefault().removeChangeListener(this);
    }
    
    boolean isVisible(
        @NonNull FileObject file,
        @NullAllowed final FileObject root) {
        long st = 0L;
        if (LOGGER.isLoggable(Level.FINER)) {
            st = System.currentTimeMillis();
        }
        try {
            final VisibilityQuery vq = VisibilityQuery.getDefault();
            final Deque<FileObject> fta = new ArrayDeque<FileObject>();
            Boolean vote = null;
            boolean folder = false;
            while (root != null && !root.equals(file)) {
                vote = visibilityCache.get(file);
                if (vote != null) {
                    break;
                }
                if (folder || file.isFolder()) {
                    fta.offer(file);
                }
                if (!vq.isVisible(file)) {
                    vote = Boolean.FALSE;
                    break;
                }
                file = file.getParent();
                folder = true;
            }
            if (vote == null) {
                vote = vq.isVisible(file);
                fta.offer(file);
            }
            if (!fta.isEmpty()) {
                synchronized(visibilityCache) {
                    for (FileObject nf : fta) {
                        visibilityCache.put(nf, vote);
                    }
                }
            }
            return vote;
        } finally {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(
                    Level.FINER,
                    "reportVisibilityOverhead: {0}",    //NOI18N
                    (System.currentTimeMillis() - st));
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        visibilityCache.clear();
        if (Crawler.listenOnVisibility()) {
            if (visibilityLogCtx.get()==null) {
                visibilityLogCtx.compareAndSet(null, LogContext.create(LogContext.EventType.FILE, null));
            }
            visibilityChanged.schedule(VISIBILITY_CHANGE_WINDOW);
        }
    }

    private class SlidingTask implements Runnable {

        private final RepositoryUpdater ru;

        SlidingTask(@NonNull final RepositoryUpdater ru) {
            this.ru = ru;
        }

        @Override
        public void run() {
            LOGGER.fine ("VisibilityQuery changed, reindexing");    //NOI18N
            ru.refreshAll(false, false, true, visibilityLogCtx.getAndSet(null));
        }
    }

    @NonNull
    static VisibilitySupport create(
        @NonNull final RepositoryUpdater ru,
        @NonNull final RequestProcessor worker) {
        Parameters.notNull("ru", ru);   //NOI18N
        Parameters.notNull("worker", worker);   //NOI18N
        return new VisibilitySupport(ru, worker);
    }


}
