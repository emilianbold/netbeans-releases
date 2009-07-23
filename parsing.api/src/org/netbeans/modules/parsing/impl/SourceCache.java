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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;


/**
 * This class maintains cache of parser instance, snapshot and nested embeddings
 * for some block of code (or top level source).
 *
 * Threading: Instances of SourceCache and Source are synchronized using TaskProcessor.INTERNAL_LOCK
 * 
 * @author Jan Jancura
 */
//@ThreadSafe
public final class SourceCache {
    
    private final Source    source;
    //@GuardedBy(this)
    private Embedding       embedding;
    private final String    mimeType;    

    public SourceCache (
        Source              source,
        Embedding           embedding
    ) {
        assert source != null;
        this.source = source;
        this.embedding = embedding;
        mimeType = embedding != null ?
            embedding.getMimeType () :
            source.getMimeType ();
        mimeType.getClass();
    }

    private void setEmbedding (
        Embedding           embedding
    ) {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            assert embedding.getMimeType ().equals (mimeType);
            this.embedding = embedding;
            snapshot = null;
        }
    }
    //@GuardedBy(this)
    private Snapshot        snapshot;

    public Snapshot getSnapshot () {
        boolean isEmbedding;
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (snapshot != null) {
                return snapshot;
            }
            isEmbedding = embedding != null;
        }

        final Snapshot _snapshot = createSnapshot (isEmbedding);
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (snapshot == null) {
                snapshot = _snapshot;
            }
            return snapshot;
        }
    }

    Snapshot createSnapshot (long[] idHolder) {
        assert idHolder != null;
        assert idHolder.length == 1;
        boolean isEmbedding;
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            isEmbedding = embedding != null;
        }
        idHolder[0] = SourceAccessor.getINSTANCE ().getLastEventId (this.source);
        return createSnapshot (isEmbedding);
    }

    private Snapshot createSnapshot (boolean isEmbedding) {
        Snapshot _snapshot = isEmbedding ? embedding.getSnapshot () : source.createSnapshot ();
        assert mimeType.equals (_snapshot.getMimeType ());
        return _snapshot;
    }

    //@GuarderBy(this)
    private boolean         parserInitialized = false;
    //@GuardedBy(this)
    private Parser          parser;
    
    public Parser getParser () {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (parserInitialized) {
                return parser;
            }
        }
        Parser _parser = null;
        Lookup lookup = MimeLookup.getLookup (mimeType);
        ParserFactory parserFactory = lookup.lookup (ParserFactory.class);
        if (parserFactory != null) {
            final Snapshot _snapshot = getSnapshot ();
            final Collection<Snapshot> _tmp = Collections.singleton (_snapshot);
            _parser = parserFactory.createParser (_tmp);
        }

        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (!parserInitialized) {                                                                                
                parser = _parser;
                parserInitialized = true;
            }
            return parser;
        }
    }
    
    //@GuardedBy(this)
    private boolean                     parsed = false;
    
    public Result getResult (
        final Task                      task
    ) throws ParseException {
        assert TaskProcessor.holdsParserLock();
        Parser _parser = getParser ();
        if (_parser == null) return null;
        boolean _parsed;
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            _parsed = this.parsed;
            this.parsed = true; //Optimizstic update
        }
        if (!_parsed) {
            boolean parseSuccess = false;
            try {
                final Snapshot _snapshot = getSnapshot ();
                final FileObject file = _snapshot.getSource().getFileObject();
                if (file != null && !file.isValid()) {
                    return null;
                }
                SourceModificationEvent event = SourceAccessor.getINSTANCE ().getSourceModificationEvent (source);
                _parser.parse (_snapshot, task, event);
                SourceAccessor.getINSTANCE ().parsed (source);
                parseSuccess = true;
            } finally {
                if (!parseSuccess) {
                    synchronized (TaskProcessor.INTERNAL_LOCK) {
                        parsed = false;
                    }
                }
            }
        }
        return _parser.getResult (task);
    }
    
    public void invalidate () {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            snapshot = null;
            parsed = false;
            embeddings = null;
            upToDateEmbeddingProviders.clear();
            for (SourceCache sourceCache : embeddingToCache.values ())
                sourceCache.invalidate ();
        }
    }

    public void invalidate (final Snapshot preRendered) {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            invalidate();
            snapshot = preRendered;
        }
    }
                
    //@GuardedBy(this)
    private Collection<Embedding> 
                            embeddings;
    //@GuardedBy(this)
    private final Map<EmbeddingProvider,List<Embedding>>
                            embeddingProviderToEmbedings = new HashMap<EmbeddingProvider,List<Embedding>> ();
    
    public Iterable<Embedding> getAllEmbeddings () {
        Collection<SchedulerTask> tsks = createTasks();
        Snapshot snpsht = getSnapshot();
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (this.embeddings == null) {
                this.embeddings = new ArrayList<Embedding> ();
                for (SchedulerTask schedulerTask : tsks) {
                    if (schedulerTask instanceof EmbeddingProvider) {
                        EmbeddingProvider embeddingProvider = (EmbeddingProvider) schedulerTask;
                        if (upToDateEmbeddingProviders.contains (embeddingProvider)) {
                            List<Embedding> _embeddings = embeddingProviderToEmbedings.get (embeddingProvider);
                            this.embeddings.addAll (_embeddings);
                        } else {
                            List<Embedding> _embeddings = embeddingProvider.getEmbeddings (snpsht);
                            List<Embedding> oldEmbeddings = embeddingProviderToEmbedings.get (embeddingProvider);
                            updateEmbeddings (_embeddings, oldEmbeddings, false, null);
                            embeddingProviderToEmbedings.put (embeddingProvider, _embeddings);
                            upToDateEmbeddingProviders.add (embeddingProvider);
                            this.embeddings.addAll (_embeddings);
                        }
                    }
                }
            }
            return this.embeddings;
        }
    }

    //@GuardedBy(this)
    private final Set<EmbeddingProvider> upToDateEmbeddingProviders = new HashSet<EmbeddingProvider> ();

    
    void refresh (EmbeddingProvider embeddingProvider, Class<? extends Scheduler> schedulerType) {
        List<Embedding> _embeddings = embeddingProvider.getEmbeddings (getSnapshot ());
        List<Embedding> oldEmbeddings;
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            oldEmbeddings = embeddingProviderToEmbedings.get (embeddingProvider);
        }
        updateEmbeddings (_embeddings, oldEmbeddings, true, schedulerType);
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            embeddingProviderToEmbedings.put (embeddingProvider, _embeddings);
            upToDateEmbeddingProviders.add (embeddingProvider);
        }
    }
    
    private void updateEmbeddings (
            List<Embedding>                 embeddings,
            List<Embedding>                 oldEmbeddings,
            boolean                         updateTasks,
            Class<? extends Scheduler>      schedulerType
    ) {
        List<SourceCache> toBeSchedulled = new ArrayList<SourceCache> ();
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if (oldEmbeddings != null && embeddings.size () == oldEmbeddings.size ()) {
                for (int i = 0; i < embeddings.size (); i++) {
                    if (embeddings.get (i) == null)
                        throw new NullPointerException ();
                    SourceCache cache = embeddingToCache.remove (oldEmbeddings.get (i));
                    if (cache != null) {
                        cache.setEmbedding (embeddings.get (i));
                        assert embeddings.get (i).getMimeType ().equals (cache.getSnapshot ().getMimeType ());
                        embeddingToCache.put (embeddings.get (i), cache);
                    } else {
                        cache = getCache(embeddings.get(i));
                    }

                    if (updateTasks)
                        toBeSchedulled.add (cache);
                }
            } else {
                if (oldEmbeddings != null)
                    for (Embedding _embedding : oldEmbeddings) {
                        SourceCache cache = embeddingToCache.remove (_embedding);
                        if (cache != null) {
                            cache.removeTasks ();
                        }
                    }
                if (updateTasks)
                    for (Embedding _embedding : embeddings) {
                        SourceCache cache = getCache (_embedding);
                        toBeSchedulled.add (cache);
                    }
            }
        }
        for (SourceCache cache : toBeSchedulled)
            cache.scheduleTasks (schedulerType);
    }
    
    //@GuardedBy(this)
    private final Map<Embedding,SourceCache>
                            embeddingToCache = new HashMap<Embedding,SourceCache> ();
    
    public SourceCache getCache (Embedding embedding) {
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            SourceCache sourceCache = embeddingToCache.get (embedding);
            if (sourceCache == null) {
                sourceCache = new SourceCache (source, embedding);
                assert embedding.getMimeType ().equals (sourceCache.getSnapshot ().getMimeType ());
                embeddingToCache.put (embedding, sourceCache);
            }
            return sourceCache;
        }
    }

    
    // tasks management ........................................................
    
    //@GuardedBy(this)
    private List<SchedulerTask> 
                            tasks;
    //@GuardedBy(this)
    private Set<SchedulerTask> 
                            pendingTasks;
    
    private Collection<SchedulerTask> createTasks () {
        List<SchedulerTask> tasks1 = null;
        Set<SchedulerTask> pendingTasks1 = null;
        if (tasks == null) {
            tasks1 = new ArrayList<SchedulerTask> ();
            pendingTasks1 = new HashSet<SchedulerTask> ();
            Lookup lookup = MimeLookup.getLookup (mimeType);
            Collection<? extends TaskFactory> factories = lookup.lookupAll (TaskFactory.class);
            //Issue #162990 workaround >>>
            Collection<? extends TaskFactory> resortedFactories = resortTaskFactories(factories);
            //<<< End of workaround
            for (TaskFactory factory : resortedFactories) {
                Collection<? extends SchedulerTask> newTasks = factory.create (getSnapshot());
                if (newTasks != null) {
                    tasks1.addAll (newTasks);
                    pendingTasks1.addAll (newTasks);
//                    for (SchedulerTask task : newTasks)
//                        System.out.println("  createTask " + task);
                }
            }
        }
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            if ((tasks == null) && (tasks1 != null)) {
                tasks = tasks1;
                pendingTasks = pendingTasks1;
            }
            if (tasks != null) {
                // this should be normal return in most cases
                return tasks;
            }
        }
        // recurse and hope
        return createTasks();
    }

    //Issue #162990 workaround >>>
    //Move the JsEmbeddingProvider$Factory to the beginning of the factories list
    private Collection<? extends TaskFactory> resortTaskFactories(Collection<? extends TaskFactory> factories) {
        List<TaskFactory> resorted = new ArrayList<TaskFactory>(factories);
        for(TaskFactory tf : resorted) {
            if(tf.getClass().getName().equals("org.netbeans.modules.javascript.editing.embedding.JsEmbeddingProvider$Factory")) { //NOI18N
                resorted.remove(tf); 
                resorted.add(0, tf);
                break;
            }
        }
        return resorted;
    }
    //<<< End of workaround

    //tzezula: probably has race condition
    public void scheduleTasks (Class<? extends Scheduler> schedulerType) {
        //S ystem.out.println("scheduleTasks " + schedulerType);
        final List<SchedulerTask> remove = new ArrayList<SchedulerTask> ();
        final List<SchedulerTask> add = new ArrayList<SchedulerTask> ();
        Collection<SchedulerTask> tsks = createTasks();
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            for (SchedulerTask task : tsks) {
                if (schedulerType == null ||
                    task.getSchedulerClass () == schedulerType ||
                    task instanceof EmbeddingProvider
                ) {
                    if (pendingTasks.remove (task)) {
                        add.add (task);
                    }
                    else {
                        remove.add (task);
                        //S ystem.out.println ("  remove: " + task);
                        add.add (task);
                    }
                    //S ystem.out.println ("  add: " + task);
                }
            }
        }
        if (!add.isEmpty ()) {
            TaskProcessor.updatePhaseCompletionTask(add, remove, source, this, schedulerType);
        }
    }

    //jjancura: probably has race condition too
    public void sourceModified () {
        SourceModificationEvent sourceModificationEvent = SourceAccessor.getINSTANCE ().getSourceModificationEvent (source);
        if (sourceModificationEvent == null)
            return;
        Map<Class<? extends Scheduler>,SchedulerEvent> schedulerEvents = new HashMap<Class<? extends Scheduler>, SchedulerEvent> ();
        for (Scheduler scheduler : Schedulers.getSchedulers ()) {
            SchedulerEvent schedulerEvent = SchedulerAccessor.get ().createSchedulerEvent (scheduler, sourceModificationEvent);
            if (schedulerEvent != null)
                schedulerEvents.put (scheduler.getClass (), schedulerEvent);
        }
        SourceAccessor.getINSTANCE ().setSchedulerEvents (source, schedulerEvents);
        if (schedulerEvents.isEmpty ())
            return;
        final List<SchedulerTask> remove = new ArrayList<SchedulerTask> ();
        final List<SchedulerTask> add = new ArrayList<SchedulerTask> ();
        Collection<SchedulerTask> tsks = createTasks();
        synchronized (TaskProcessor.INTERNAL_LOCK) {
            for (SchedulerTask task : tsks) {
                Class<? extends Scheduler> schedulerClass = task.getSchedulerClass ();
                if (schedulerClass != null &&
                    !schedulerEvents.containsKey (schedulerClass)
                )
                    continue;
                if (pendingTasks.remove (task)) {
                    add.add (task);
                }
                else {
                    remove.add (task);
                    //S ystem.out.println ("  remove: " + task);
                    add.add (task);
                }
                //S ystem.out.println ("  add: " + task);
            }
        }
        if (!add.isEmpty ()) {
            TaskProcessor.updatePhaseCompletionTask (add, remove, source, this, null);
        }
    }
    
    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder ("SourceCache ");
        sb.append (hashCode ());
        sb.append (": ");
        Snapshot _snapshot = getSnapshot ();
        Source _source = _snapshot.getSource ();
        FileObject fileObject = _source.getFileObject ();
        if (fileObject != null)
            sb.append (fileObject.getNameExt ());
        else
            sb.append (mimeType).append (" ").append (_source.getDocument (false));
        if (!_snapshot.getMimeType ().equals (_source.getMimeType ())) {
            sb.append ("( ").append (_snapshot.getMimeType ()).append (" ");
            sb.append (_snapshot.getOriginalOffset (0)).append ("-").append (_snapshot.getOriginalOffset (_snapshot.getText ().length () - 1)).append (")");
        }
        return sb.toString ();
    }
    
    //@NotThreadSafe - has to be called in GuardedBy(this)
    private void removeTasks () {
        if (tasks != null) {
            TaskProcessor.removePhaseCompletionTasks (tasks, source);
        }
        tasks = null;
    }
}




