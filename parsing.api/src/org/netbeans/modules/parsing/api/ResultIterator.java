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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.impl.ParserManagerImpl;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceFlags;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.util.Lookup;


/**
 * ResultIterator allows to iterate tree of embedded blocks of sources, and 
 * request parse results on different levels. You can force parsing of all 
 * blocks of embedded {@link Source}s, or you can find your favourite one and
 * skip parsing of rest of them. In some situations you can even parse embedded 
 * language without parsing of surrounding block.
 * 
 * @author Jan Jancura
 */
public final class ResultIterator {
    
    private Snapshot        snapshot;
    private MultiLanguageUserTask
                            task;

    ResultIterator (
        Snapshot            snapshot,
        MultiLanguageUserTask
                            task
    ) {
        this.snapshot = snapshot;
        this.task = task;
    }
    
    public Snapshot getSnapshot () {
        return snapshot;
    }
    
    /**
     * Returns parse {@link Result} for current source.
     * 
     * @return              parse {@link Result} for current source.
     */
    public Result getParserResult () throws ParseException {
        String mimeType = snapshot.getMimeType ();
        Parser parser = null;
        if (mimeType.equals (snapshot.getSource ().getMimeType ()))
            parser = ParserManagerImpl.getParser (snapshot.getSource ());
        if (parser == null) {
            Lookup lookup = MimeLookup.getLookup (mimeType);
            final Collection <? extends ParserFactory> parserFactories = lookup.lookupAll(ParserFactory.class);
            final Collection<Snapshot> _tmp = Collections.singleton (snapshot);
            for (final ParserFactory parserFactory : parserFactories) {
                parser = parserFactory.createParser (_tmp);
                if (parser != null) {
                    break;
                }
            }           
        }
        if (parser == null) throw new ParseException ();
        final Source source = snapshot.getSource();
        boolean invalid;        
        synchronized (source) {
            invalid = SourceAccessor.getINSTANCE().getFlags(source).remove(SourceFlags.INVALID);
        }        
        Result result;
        if (!invalid) {
            result = parser.getResult (task, null);
        }
        else {
            boolean parseSuccess = false;
            try {
                parser.parse (snapshot, task, null);
                result = parser.getResult (task, null);
                parseSuccess = true;
            } finally {
               if (invalid && !parseSuccess) {
                   synchronized (source ) {
                       SourceAccessor.getINSTANCE().getFlags(source).add(SourceFlags.INVALID); //Rollback of optimistic update
                   }
               }
            }
        }
        return result;
    }
    
    /**
     * Allows iterate all embedded sources.
     * 
     * @return              {@link Iterator} of all embeddings.
     */
    public Iterable<Embedding> getEmbeddings () {
        return new Iterable<Embedding> () {
            public Iterator<Embedding> iterator () {
                return new CompoundIterator<SchedulerTask,Embedding> (
                    new CompoundIterator<TaskFactory,SchedulerTask> (
                            MimeLookup.getLookup (
                                snapshot.getMimeType ()
                            ).lookupAll (TaskFactory.class).iterator ()
                        ) {
                            @Override
                            protected Iterator<SchedulerTask> getIterator (TaskFactory factory) {
                                Collection<SchedulerTask> tasks = factory.create(snapshot.getSource());
                                
                                if (tasks != null) {
                                    return tasks.iterator();
                                } else {
                                    return Collections.<SchedulerTask>emptyList().iterator();
                                }
                            }
                        }                
                ) {
                    @Override
                    protected Iterator<Embedding> getIterator (SchedulerTask schedulerTask) {
                        if (schedulerTask instanceof EmbeddingProvider)
                            return ((EmbeddingProvider) schedulerTask).getEmbeddings (snapshot).iterator ();
                        return Collections.<Embedding>emptyList ().iterator ();
                    }
                };
            }
        };
    }
    
    /**
     * Returns {@link ResultIterator} for one {@link Embedding}.
     * 
     * @param embedding     A embedding.
     * @return              {@link ResultIterator} for one {@link Embedding}.
     */
    public ResultIterator getResultIterator (Embedding embedding) {
        return new ResultIterator (embedding.getSnapshot (), task);
    }
    
    private static abstract class CompoundIterator<A,B> implements Iterator<B> {

        private Iterator<? extends A> iteratorA;
        private Iterator<? extends B> iteratorB;

        public CompoundIterator (Iterator<? extends A> iteratorA) {
            this.iteratorA = iteratorA;
        }
        
        protected abstract Iterator<? extends B> getIterator (A a);
        
        public boolean hasNext () {
            if (iteratorB == null) {
                if (!iteratorA.hasNext ()) return false;
                iteratorB = getIterator (iteratorA.next ());
            }
            while (!iteratorB.hasNext ()) {
                if (!iteratorA.hasNext ()) return false;
                iteratorB = getIterator (iteratorA.next ());
            }
            return true;
        }

        public B next () {
            if (!hasNext()) {
                return null; //XXX should throw exception
            }
            return iteratorB.next ();
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
}
