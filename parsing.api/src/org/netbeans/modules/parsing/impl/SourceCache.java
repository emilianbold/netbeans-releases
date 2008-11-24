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

    public synchronized void setEmbedding (
        Embedding           embedding
    ) {
        this.embedding = embedding;
    }
    //@GuardedBy(this)
    private Snapshot        snapshot;

    public Snapshot getSnapshot () {
        boolean isEmbedding;
        synchronized (this) {
            if (snapshot != null) {
                return snapshot;
            }
            isEmbedding = embedding != null;
        }

        final Snapshot _snapshot = createSnapshot (isEmbedding);
        synchronized (this) {
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
        synchronized (this) {
            isEmbedding = embedding != null;
        }
        idHolder[0] = SourceAccessor.getINSTANCE ().getLastEventId (this.source);
        return createSnapshot (isEmbedding);
    }

    private Snapshot createSnapshot (boolean isEmbedding) {
        return isEmbedding ? embedding.getSnapshot () : source.createSnapshot ();
    }

    //@GuarderBy(this)
    private boolean         parserInitialized = false;
    //@GuardedBy(this)
    private Parser          parser;
    
    public Parser getParser () {
        final Snapshot _snapshot = getSnapshot ();
        synchronized (this) {
            if (!parserInitialized) {
                parserInitialized = true;
                Lookup lookup = MimeLookup.getLookup (mimeType);
                ParserFactory parserFactory = lookup.lookup (ParserFactory.class);
                if (parserFactory == null) return null;
                final Collection<Snapshot> _tmp = Collections.singleton (_snapshot);
                parser = parserFactory.createParser (_tmp);
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
        synchronized (this) {
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
                parseSuccess = true;
            } finally {
                if (!parseSuccess) {
                    synchronized (this) {
                        parsed = false;
                    }
                }
            }
        }
        return _parser.getResult (task);
    }
    
    public synchronized void invalidate () {
        snapshot = null;
        embedding = null;
        parsed = false;
        embeddings = null;
        upToDateEmbeddingProviders.clear();
        for (SourceCache sourceCache : embeddingToCache.values ())
            sourceCache.invalidate ();
    }

    public synchronized void invalidate (final Snapshot preRendered) {
        invalidate();
        snapshot = preRendered;
    }
                
    //@GuardedBy(this)
    private Collection<Embedding> 
                            embeddings;
    //@GuardedBy(this)
    private final Map<EmbeddingProvider,List<Embedding>>
                            embeddingProviderToEmbedings = new HashMap<EmbeddingProvider,List<Embedding>> ();
    
    public synchronized Iterable<Embedding> getAllEmbeddings () {
        if (this.embeddings == null) {
            this.embeddings = new ArrayList<Embedding> ();
            for (SchedulerTask schedulerTask : createTasks ()) {
                if (schedulerTask instanceof EmbeddingProvider) {
                    EmbeddingProvider embeddingProvider = (EmbeddingProvider) schedulerTask;
                    if (upToDateEmbeddingProviders.contains (embeddingProvider)) {
                        List<Embedding> _embeddings = embeddingProviderToEmbedings.get (embeddingProvider);
                        this.embeddings.addAll (_embeddings);
                    } else {
                        List<Embedding> _embeddings = embeddingProvider.getEmbeddings (getSnapshot ());
                        List<Embedding> oldEmbeddings = embeddingProviderToEmbedings.get (embeddingProvider);
                        updateEmbeddings (_embeddings, oldEmbeddings, false, null);
                        embeddingProviderToEmbedings.put (embeddingProvider, _embeddings);
                        this.embeddings.addAll (_embeddings);
                    }
                }
            }
        }
        return this.embeddings;
    }

    //@GuardedBy(this)
    private final Set<EmbeddingProvider> upToDateEmbeddingProviders = new HashSet<EmbeddingProvider> ();

    
    synchronized void refresh (EmbeddingProvider embeddingProvider, Class<? extends Scheduler> schedulerType) {
        List<Embedding> _embeddings = embeddingProvider.getEmbeddings (getSnapshot ());
        List<Embedding> oldEmbeddings = embeddingProviderToEmbedings.get (embeddingProvider);
        updateEmbeddings (_embeddings, oldEmbeddings, true, schedulerType);
        embeddingProviderToEmbedings.put (embeddingProvider, _embeddings);
        upToDateEmbeddingProviders.add (embeddingProvider);
    }
    
    //@NotThreadSafe - has to be called in GuardedBy(this)
    private void updateEmbeddings (
            List<Embedding> embeddings,
            List<Embedding> oldEmbeddings,
            boolean         updateTasks,
            Class<? extends Scheduler>           schedulerType
    ) {
        if (oldEmbeddings != null && embeddings.size () == oldEmbeddings.size ()) {
            for (int i = 0; i < embeddings.size (); i++) {
                SourceCache cache = embeddingToCache.remove (oldEmbeddings.get (i));
                cache.setEmbedding (embeddings.get (i));
                embeddingToCache.put (embeddings.get (i), cache);
                if (updateTasks) {
                    //S ystem.out.println ("\nSchedule embedded tasks (" + embeddings.get (i) + "):");
                    cache.scheduleTasks (schedulerType);
                }
            }
        } else {
            if (oldEmbeddings != null)
                for (Embedding _embedding : oldEmbeddings) {
                    SourceCache cache = embeddingToCache.remove (_embedding);
                    cache.removeTasks ();
                }
            if (updateTasks)
                for (Embedding _embedding : embeddings) {
                    SourceCache cache = getCache (_embedding);
                    //S ystem.out.println ("\nSchedule embedded tasks (" + _embedding + "):");
                    cache.scheduleTasks (schedulerType);
                }
        }
    }
    
    //@GuardedBy(this)
    private final Map<Embedding,SourceCache>
                            embeddingToCache = new HashMap<Embedding,SourceCache> ();
    
    public synchronized SourceCache getCache (Embedding embedding) {
        SourceCache sourceCache = embeddingToCache.get (embedding);
        if (sourceCache == null) {
            sourceCache = new SourceCache (source, embedding);
            embeddingToCache.put (embedding, sourceCache);
        }
        return sourceCache;
    }

    
    // tasks management ........................................................
    
    //@GuardedBy(this)
    private List<SchedulerTask> 
                            tasks;
    //@GuardedBy(this)
    private Set<SchedulerTask> 
                            pendingTasks;
    
    //@NotThreadSafe - has to be called in GuardedBy(this)
    private Collection<SchedulerTask> createTasks () {
        if (tasks == null) {
            tasks = new ArrayList<SchedulerTask> ();
            pendingTasks = new HashSet<SchedulerTask> ();
            Lookup lookup = MimeLookup.getLookup (mimeType);
            for (TaskFactory factory : lookup.lookupAll (TaskFactory.class)) {
                Collection<? extends SchedulerTask> newTasks = factory.create (getSnapshot());
                if (newTasks != null) {
                    tasks.addAll (newTasks);
                    pendingTasks.addAll (newTasks);
//                    for (SchedulerTask task : newTasks)
//                        System.out.println("  createTask " + task);
                }
            }
        }
        return tasks;
    }

    //tzezula: probably has race condition
    public void scheduleTasks (Class<? extends Scheduler> schedulerType) {
        final List<SchedulerTask> remove = new ArrayList<SchedulerTask> ();
        final List<SchedulerTask> add = new ArrayList<SchedulerTask> ();
        synchronized (this) {
            if (tasks == null)
                createTasks ();
            for (SchedulerTask task : tasks) {
                if (task.getSchedulerClass () == schedulerType || task instanceof EmbeddingProvider) {
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
            sb.append (mimeType).append (" ").append (_source.getDocument ());
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




