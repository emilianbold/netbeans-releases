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
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskScheduler;
import org.openide.util.Lookup;


/**
 * This class maintains cache of parser instance, snapshot and nested embeddings
 * for some block of code (or top level source).
 * 
 * @author Jan Jancura
 */
public class SourceCache {
    
    private Source          source;
    private Embedding       embedding;
    private String          mimeType;

    public SourceCache (
        Source              source,
        Embedding           embedding
    ) {
        assert source != null;
        assert mimeType == null;
        this.source = source;
        this.embedding = embedding;
        mimeType = embedding != null ?
            embedding.getMimeType () :
            source.getMimeType ();
        if (mimeType == null) throw new NullPointerException ();
    }

    public void setEmbedding (
        Embedding           embedding
    ) {
        this.embedding = embedding;
    }
    
    private Snapshot        snapshot;

    public synchronized Snapshot getSnapshot () {
        if (snapshot == null)
            snapshot = embedding == null ?
                source.createSnapshot () :
                embedding.getSnapshot ();
        return snapshot;
    }

    private boolean         parserInitialized = false;
    private Parser          parser;
    
    public synchronized Parser getParser () {
        if (!parserInitialized) {
            parserInitialized = true;
            Lookup lookup = MimeLookup.getLookup (mimeType);
            ParserFactory parserFactory = lookup.lookup (ParserFactory.class);
            if (parserFactory == null) return null;
            final Collection<Snapshot> _tmp = Collections.singleton (getSnapshot ());
            parser = parserFactory.createParser (_tmp);
        }
        return parser;
    }
    
    private boolean         parsed = false;
    
    public Result getResult (
        Task                task,
        SchedulerEvent      event
    ) throws ParseException {
        Parser parser = getParser ();
        if (parser == null) return null;
        if (!parsed) {
            parsed = true;
            parser.parse (getSnapshot (), task, event);
        }
        return parser.getResult (task, null);
    }
    
    public void invalidate () {
        snapshot = null;
        embedding = null;
        parsed = false;
        embeddings = null;
        upToDateEmbeddingProviders = new HashSet<EmbeddingProvider> ();
        for (SourceCache sourceCache : embeddingToCache.values ())
            sourceCache.invalidate ();
    }
    
    boolean isValid () {
        return snapshot != null;
    }
    
//    private Collection<SchedulerTask> 
//                            shedulerTasks;
//    
//    private Collection<SchedulerTask> getSchedulerTasks () {
//        if (shedulerTasks == null) {
//            shedulerTasks = new ArrayList<SchedulerTask> ();
//            for (TaskFactory taskFactory : MimeLookup.getLookup (
//                getSnapshot ().getMimeType ()
//            ).lookupAll (TaskFactory.class)) {
//                shedulerTasks.addAll (taskFactory.create (getSnapshot ()));
//            }
//        }
//        return shedulerTasks;
//    }
    
    private Collection<Embedding> 
                            embeddings;
    private Map<EmbeddingProvider,List<Embedding>>
                            embeddingProviderToEmbedings = new HashMap<EmbeddingProvider,List<Embedding>> ();
    
    public Iterable<Embedding> getAllEmbeddings () {
        if (this.embeddings == null) {
            this.embeddings = new ArrayList<Embedding> ();
            for (SchedulerTask schedulerTask : createTasks ()) {
                if (schedulerTask instanceof EmbeddingProvider) {
                    EmbeddingProvider embeddingProvider = (EmbeddingProvider) schedulerTask;
                    if (upToDateEmbeddingProviders.contains (embeddingProvider)) {
                        List<Embedding> embeddings = embeddingProviderToEmbedings.get (embeddingProvider);
                        this.embeddings.addAll (embeddings);
                    } else {
                        List<Embedding> embeddings = embeddingProvider.getEmbeddings (getSnapshot ());
                        List<Embedding> oldEmbeddings = embeddingProviderToEmbedings.get (embeddingProvider);
                        updateEmbeddings (embeddings, oldEmbeddings, embeddingProvider, false, null);
                        embeddingProviderToEmbedings.put (embeddingProvider, embeddings);
                        this.embeddings.addAll (embeddings);
                    }
                }
            }
        }
        return this.embeddings;
    }

    private Set<EmbeddingProvider> upToDateEmbeddingProviders = new HashSet<EmbeddingProvider> ();
    
    void refresh (EmbeddingProvider embeddingProvider, Class schedulerType) {
        List<Embedding> embeddings = embeddingProvider.getEmbeddings (getSnapshot ());
        List<Embedding> oldEmbeddings = embeddingProviderToEmbedings.get (embeddingProvider);
        updateEmbeddings (embeddings, oldEmbeddings, embeddingProvider, true, schedulerType);
        embeddingProviderToEmbedings.put (embeddingProvider, embeddings);
        upToDateEmbeddingProviders.add (embeddingProvider);
    }
    
    private void updateEmbeddings (
            List<Embedding> embeddings,
            List<Embedding> oldEmbeddings,
            EmbeddingProvider
                            embeddingProvider,
            boolean         updateTasks,
            Class           schedulerType
    ) {
        if (oldEmbeddings != null && embeddings.size () == oldEmbeddings.size ()) {
            for (int i = 0; i < embeddings.size (); i++) {
                SourceCache cache = embeddingToCache.remove (oldEmbeddings.get (i));
                cache.setEmbedding (embeddings.get (i));
                embeddingToCache.put (embeddings.get (i), cache);
                if (updateTasks)
                    cache.scheduleTasks (schedulerType);
            }
        } else {
            if (oldEmbeddings != null)
                for (Embedding embedding : oldEmbeddings) {
                    SourceCache cache = embeddingToCache.remove (embedding);
                    cache.removeTasks ();
                }
            if (updateTasks)
                for (Embedding embedding : embeddings) {
                    SourceCache cache = getCache (embedding);
                    cache.scheduleTasks (schedulerType);
                }
        }
    }
    
    private Map<Embedding,SourceCache>
                            embeddingToCache = new HashMap<Embedding,SourceCache> ();
    
    public SourceCache getCache (Embedding embedding) {
        SourceCache sourceCache = embeddingToCache.get (embedding);
        if (sourceCache == null) {
            sourceCache = new SourceCache (source, embedding);
            embeddingToCache.put (embedding, sourceCache);
        }
        return sourceCache;
    }

    
    // tasks management ........................................................
    
    private List<SchedulerTask> 
                            tasks;
    private Set<SchedulerTask> 
                            pendingTasks;
    
    private Collection<SchedulerTask> createTasks (
    ) {
        if (tasks == null) {
            tasks = new ArrayList<SchedulerTask> ();
            pendingTasks = new HashSet<SchedulerTask> ();
            Lookup lookup = MimeLookup.getLookup (mimeType);
            for (TaskFactory factory : lookup.lookupAll (TaskFactory.class)) {
                Collection<SchedulerTask> newTasks = factory.create (getSnapshot ());
                if (newTasks != null) {
                    tasks.addAll (newTasks);
                    pendingTasks.addAll (newTasks);
                }
            }
        }
        return tasks;
    }
    
    public void scheduleTasks (Class schedulerType) {
        if (tasks == null)
            createTasks ();
        List<SchedulerTask> reschedule = new ArrayList<SchedulerTask> ();
        List<SchedulerTask> add = new ArrayList<SchedulerTask> ();
        for (SchedulerTask task : tasks)
            if (task.getSchedulerClass () == schedulerType ||
                //(
                task instanceof EmbeddingProvider //&&
                 //!upToDateEmbeddingProviders.contains ((EmbeddingProvider) task))
            ) {
                if (pendingTasks.remove (task))
                    add.add (task);
                else
                    reschedule.add (task);
            }
        if (!add.isEmpty ())
            TaskProcessor.addPhaseCompletionTasks (add, this, schedulerType);
        if (!reschedule.isEmpty ())
            TaskProcessor.rescheduleTasks (reschedule, source, schedulerType);
    }
    
    private void removeTasks () {
        if (tasks != null)
            for (SchedulerTask task : tasks)
                TaskProcessor.removePhaseCompletionTask (task, source);
        tasks = null;
    }
}




