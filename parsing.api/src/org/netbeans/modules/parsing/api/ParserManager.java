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
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.impl.ParserAccessor;
import org.netbeans.modules.parsing.impl.ResultIteratorAccessor;
import org.netbeans.modules.parsing.impl.SourceAccessor;
import org.netbeans.modules.parsing.impl.SourceCache;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.UserTaskImpl;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.util.Lookup;
import org.openide.util.Mutex;


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
     * Priority request for parsing of some source. This task in called for 
     * the most embedded source / language on given position. Task is notified 
     * when parsing is finished. 
     * This method is blocking. It means that only one parsing request per time
     * is allowed. But you can call another parsing request 
     * from your Task. This secondary parsing request is called 
     * immediately in the same thread (current thread).
     * <p>
     * This method is typically called as a response on some user request - 
     * during code completion for example. But you have access to parse result
     * created for one block of some embedded (or top level) language only.
     * Use second parse method if you need access to parse results of some other
     * language plocks too.
     * 
     * @param source        A source that should be parsed.
     * @param userTask      A task that will be started when parsing is done.
     * @param offset        A offset that identifies some block of code.
     * @throws ParseException encapsulating the user exception
     */
    public static void parse (
        Source              source, 
        UserTask            userTask,
        int                 offset
    ) throws ParseException {
        parse (
            Collections.<Source>singletonList (source),
            new UserTaskImpl (source, userTask, offset)
        );
    }

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
        final Collection<Source>  
                            sources, 
        final MultiLanguageUserTask 
                            userTask
    ) throws ParseException {
        //tzezula: ugly, Hanzy isn't here a nicer solution to distinguish single source from multi source?
        if (sources.size() == 1) {
            SourceAccessor.getINSTANCE().assignListeners(sources.iterator().next());
        }
        TaskProcessor.runUserTask (new Mutex.ExceptionAction<Void>() {
            public Void run () throws Exception {
                //tzezula: Wrong - doesn't work for multiple files!
                for (Source source : sources) {
                    SourceCache sourceCache = SourceAccessor.getINSTANCE ().getCache (source);
                    final ResultIterator resultIterator = new ResultIterator (sourceCache, userTask);
                    try {
                        userTask.run (resultIterator);
                    } finally {
                        ResultIteratorAccessor.getINSTANCE().invalidate(resultIterator);
                    }
                    
                }
                return null;
            }
        }, sources);
    }
    
    /**
     * Runs given task in parser thread.
     * @param mimetype      specifying the parser
     * @param userTask      a user task
     * @throws ParseException encapsulating the user exception
     */
    public static void parse (
        final String mimeType,
        final UserTask     userTask
    ) throws ParseException {
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
        final Parser pf = p;
        TaskProcessor.runUserTask (new Mutex.ExceptionAction<Void>() {
            public Void run() throws Exception {
                pf.parse(null, userTask, null);
                Parser.Result result = pf.getResult(userTask, null);
                try {
                    userTask.run(result);
                } finally {
                    ParserAccessor.getINSTANCE().invalidate(result);
                }
                return null;
            }
        }, Collections.<Source>emptyList ());
    }
    //where
    private static Map<String,Reference<Parser>> cachedParsers = new HashMap<String,Reference<Parser>>();
}




