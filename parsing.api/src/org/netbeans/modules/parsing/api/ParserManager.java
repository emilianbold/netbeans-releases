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

package org.netbeans.modules.parsing.api;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.impl.ParserAccessor;
import org.netbeans.modules.parsing.impl.ResultIteratorAccessor;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceCache;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.indexing.lucene.LMListener;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Parameters;


/**
 * ParserManager allows to start priority parsing request for one or more 
 * sources. 
 * 
 * @author Jan Jancura
 * @author Tomas Zezula
 */
public final class ParserManager {

    private ParserManager () {}

    /**
     * Priority request for parsing of list of {@link Source}s. Implementator 
     * of this task have full control over the process of parsing of embedded 
     * languages. You can scan tree of embedded sources and start parsing for
     * all of them, or for some of them only.
     * This method is blocking. It means that only one parsing request per time
     * is allowed. But you can call another parsing request 
     * from your Task. This secondary parsing request is called 
     * immediately in the same thread (current thread).
     * <p>
     * This method is typically called as a response on some user request - 
     * during code completion for example. 
     * 
     * @param sources       A list of sources that should be parsed.
     * @param userTask      A task that will be started when parsing is done.
     * @throws ParseException encapsulating the user exception
     */
    public static void parse (
        @NonNull final Collection<Source>
                            sources, 
        @NonNull final UserTask
                            userTask
    ) throws ParseException {
        Parameters.notNull("sources", sources);     //NOI18N
        Parameters.notNull("userTask", userTask);   //NOI18N
        if (sources.size () == 1)
            TaskProcessor.runUserTask (new UserTaskAction (sources.iterator ().next (), userTask), sources);
        else
            TaskProcessor.runUserTask (new MultiUserTaskAction (sources, userTask), sources);
    }

    /**
     * Performs the given task when the scan finished. When the background scan is active
     * the task is enqueued and the method returns, the task is performed when the
     * background scan completes by the thread doing the background scan. When no background
     * scan is running the method behaves exactly like the {#link ParserManager#parse},
     * it performs the given task synchronously (in caller thread). If there is an another {@link UserTask}
     * running this method waits until it's completed.
     * @param sources A list of sources that should be parsed.
     * @param userTask A task started when parsing is done.
     * @return {@link Future} which can be used to find out the state of the task {@link Future#isDone} or {@link Future#isCancelled}.
     * The caller may cancel the task using {@link Future#cancel} or wait until the task is performed {@link Future#get}.
     * @throws ParseException encapsulating the user exception.
     */
    @NonNull
    public static Future<Void> parseWhenScanFinished (
            @NonNull final Collection<Source>  sources,
            @NonNull final UserTask userTask) throws ParseException {
        Parameters.notNull("sources", sources);     //NOI18N
        Parameters.notNull("userTask", userTask);   //NOI18N
        if (sources.size () == 1)
            return TaskProcessor.runWhenScanFinished (new UserTaskAction (sources.iterator ().next (), userTask), sources);
        else
            return TaskProcessor.runWhenScanFinished (new MultiUserTaskAction (sources, userTask), sources);
    }

    //where

    private static class UserTaskAction implements Mutex.ExceptionAction<Void> {

        private final UserTask userTask;
        private final Source source;

        public UserTaskAction (final Source source, final UserTask userTask) {
            assert source != null;
            assert userTask != null;
            this.userTask = userTask;
            this.source = source;
        }

        public Void run () throws Exception {
            SourceCache sourceCache = SourceAccessor.getINSTANCE ().getCache (source);
            final ResultIterator resultIterator = new ResultIterator (sourceCache, userTask);
            try {
                userTask.run (resultIterator);
            } finally {
                ResultIteratorAccessor.getINSTANCE().invalidate(resultIterator);
            }
            return null;
        }
    }

    private static class MultiUserTaskAction implements Mutex.ExceptionAction<Void> {

        private final UserTask userTask;
        private final Collection<Source> sources;

        public MultiUserTaskAction (final Collection<Source> sources, final UserTask userTask) {
            assert sources != null;
            assert userTask != null;
            this.userTask = userTask;
            this.sources = sources;
        }

        public Void run () throws Exception {
            LMListener lMListener = new LMListener ();
            Parser parser = null;
            final Collection<Snapshot> snapShots = new LazySnapshots(sources);
            for (Source source : sources) {
                if (parser == null) {
                    Lookup lookup = MimeLookup.getLookup (source.getMimeType ());
                    ParserFactory parserFactory = lookup.lookup (ParserFactory.class);
                    if (parserFactory != null) {
                        parser = parserFactory.createParser (snapShots);
                    }
                }
                final SourceCache uncachedSourceCache = new SourceCache(source, null);
                final ResultIterator resultIterator = new ResultIterator (uncachedSourceCache, parser, userTask);
                try {
                    userTask.run (resultIterator);
                } finally {
                    ResultIteratorAccessor.getINSTANCE().invalidate(resultIterator);
                }
                if (lMListener.isLowMemory ())
                    parser = null;
            }
            return null;
        }
    }

    //where
    private static class LazySnapshots implements Collection<Snapshot> {

        private final Collection<? extends Source> sources;

        public LazySnapshots (final Collection<? extends Source> sources) {
            assert sources != null;
            this.sources  = sources;
        }

        public int size() {
            return this.sources.size();
        }

        public boolean isEmpty() {
            return this.sources.isEmpty();
        }

        public boolean contains(final Object o) {
            if (!(o instanceof Snapshot)) {
                return false;
            }
            final Snapshot snap =(Snapshot) o;
            return this.sources.contains(snap.getSource());
        }

        public Iterator<Snapshot> iterator() {
            return new LazySnapshotsIt (this.sources.iterator());
        }

        public Object[] toArray() {
            final Object[] result = new Object[this.sources.size()];
            fill (result);
            return result;
        }

        public <T> T[] toArray(T[] a) {
            Class<?> arrayElementClass = a.getClass().getComponentType();
            if (!arrayElementClass.isAssignableFrom(Snapshot.class)) {
                throw new ArrayStoreException("Can't store Snapshot instances to an array of " + arrayElementClass.getName()); //NOI18N
            }

            final int size = this.sources.size();
            if (a.length < size) {
                @SuppressWarnings("unchecked") //NOI18N
                T[] arr = (T[])java.lang.reflect.Array.newInstance(arrayElementClass, size);
                a = arr;
            }

            fill (a);
            return a;
        }

        private void fill (Object[] array) {
            final Iterator<? extends Source> it = this.sources.iterator();
            for (int i=0; it.hasNext(); i++) {
                SourceCache sourceCache = SourceAccessor.getINSTANCE ().getCache (it.next());
                array[i] = sourceCache.getSnapshot();
            }
        }

        public boolean add(Snapshot o) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        public boolean containsAll(final Collection<?> c) {
            for (Object e : c) {
                if (!contains(e)) {
                    return false;
                }
            }
            return true;
        }

        public boolean addAll(Collection<? extends Snapshot> c) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        public void clear() {
            throw new UnsupportedOperationException("Read only collection."); //NOI18N
        }

        private static class LazySnapshotsIt implements Iterator<Snapshot> {

            private final Iterator<? extends Source> sourcesIt;

            public LazySnapshotsIt (final Iterator<? extends Source> sourcesIt) {
                assert sourcesIt != null;
                this.sourcesIt = sourcesIt;
            }

            public boolean hasNext() {
                return sourcesIt.hasNext();
            }

            public Snapshot next() {
                final SourceCache cache = SourceAccessor.getINSTANCE().getCache(sourcesIt.next());
                return cache.getSnapshot();
            }

            public void remove() {
                throw new UnsupportedOperationException("Read only collection."); //NOI18N
            }

        }

    }
    
    /**
     * Runs given task in parser thread.
     * @param mimetype      specifying the parser
     * @param userTask      a user task
     * @throws ParseException encapsulating the user exception
     */
    public static void parse (
        @NonNull final String mimeType,
        @NonNull final UserTask     userTask
    ) throws ParseException {
        Parameters.notNull("mimeType", mimeType);   //NOI18N
        Parameters.notNull("userTask", userTask);   //NOI18N
        final Parser pf = findParser(mimeType);
        TaskProcessor.runUserTask (new MimeTaskAction(pf, userTask), Collections.<Source>emptyList ());
    }

    /**
     * Performs the given task when the scan finished. When the background scan is active
     * the task is enqueued and the method returns, the task is performed when the
     * background scan completes by the thread doing the background scan. When no background
     * scan is running the method behaves exactly like the {#link ParserManager#parse},
     * it performs the given task synchronously (in caller thread). If there is an another {@link UserTask}
     * running this method waits until it's completed.
     * @param mimetype A mimetype specifying the parser.
     * @param userTask A task started when parsing is done.
     * @return {@link Future} which can be used to find out the state of the task {@link Future#isDone} or {@link Future#isCancelled}.
     * The caller may cancel the task using {@link Future#cancel} or wait until the task is performed {@link Future#get}.
     * @throws ParseException encapsulating the user exception.
     */
    @NonNull
    public static Future<Void> parseWhenScanFinished (
            @NonNull final String mimeType,
            @NonNull final UserTask userTask) throws ParseException {
        Parameters.notNull("mimeType", mimeType);   //NOI18N
        Parameters.notNull("userTask", userTask);   //NOI18N
        final Parser pf = findParser(mimeType);
        return TaskProcessor.runWhenScanFinished(new MimeTaskAction(pf, userTask), Collections.<Source>emptyList ());

    }

    //where

    private static class MimeTaskAction implements Mutex.ExceptionAction<Void> {

        private final UserTask userTask;
        private final Parser parser;

        public MimeTaskAction (final Parser parser, final UserTask userTask) {
            assert userTask != null;
            assert parser != null;
            this.userTask = userTask;
            this.parser = parser;
        }

        public Void run () throws Exception {
            parser.parse (null, userTask, null);
            Parser.Result result = parser.getResult (userTask);
            try {
                userTask.run (new ResultIterator (result));
            } finally {
                ParserAccessor.getINSTANCE ().invalidate (result);
            }
            return null;
        }
    }

    private static Parser findParser (final String mimeType) {
        Parser p = null;
        final Reference<Parser> ref = cachedParsers.get (mimeType);
        if (ref != null) {
            p = ref.get();
        }
        if (p == null) {
            final Lookup lookup = MimeLookup.getLookup (mimeType);
            final ParserFactory parserFactory = lookup.lookup (ParserFactory.class);
            if (parserFactory == null) {
                throw new IllegalArgumentException("No parser for mime type: " + mimeType);
            }
            p = parserFactory.createParser(Collections.<Snapshot>emptyList());
            cachedParsers.put(mimeType, new TimedWeakReference<Parser>(p));
        }
        return p;
    }
    //where
    private static Map<String,Reference<Parser>> cachedParsers = new HashMap<String,Reference<Parser>>();
}




