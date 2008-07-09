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

import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.openide.util.Lookup;


/**
 *
 * @author Jan Jancura
 */
public class ParserManagerImpl {
    
    
    /**
     * Returns {@link Parser} for given {@link Source}
     * @param source for which the {@link Parser} should
     * be returned
     * @return a parser
     */
//    public static Parser getParser (final Source source) {
//        assert source != null;
//        Parser parser;
//        synchronized (source) {
//            parser = SourceAccessor.getINSTANCE().getParser(source);            
//        }
//        if (parser == null) {
//            String mimeType = source.getMimeType ();
//            Lookup lookup = MimeLookup.getLookup (mimeType);
//            final Collection <? extends ParserFactory> parserFactories = lookup.lookupAll(ParserFactory.class);
//            Snapshot snapshot = SourceAccessor.getINSTANCE().getCache (source).getSnapshot();
//            final Collection<Snapshot> _tmp = Collections.singleton (snapshot);
//            for (final ParserFactory parserFactory : parserFactories) {
//                parser = parserFactory.createParser (_tmp);
//                if (parser != null) {
//                    break;
//                }
//            }           
//        }
//        if (parser != null)
//            synchronized (source) {
//                if (SourceAccessor.getINSTANCE().getParser(source)==null) {
//                    SourceAccessor.getINSTANCE().setParser(source, parser);
//                }
//            }
//        return parser;
//    }
}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
//    private static Document                         currentDocument;
////    private static Map<Parser,List<PriorityParserListener>> parserToParserListeners;
//    private static Listener                         listener;
//    
//    /**
//     * Called from ParserManager.parseUserTask.
//     */
//    public static void parseUserTask (
//        List<Source>        sources, 
//        UserTask            parserListener
//    ) {
//        for (Source source:sources) {
//            Iterator<? extends Parser> it = getParsers ().iterator ();
//            while (it.hasNext ()) {
//                Parser parser = it.next ();
//                if (cache == null) continue;
//                Parser.Result result = cache.get (parser);
//                //Parser.Result result = parser.parseUserTask (document);
//                if (result != null)
//                    parserListener.parsed (result, source);
//            }
//        }
//    }
//    
//    /**
//     * Called from ParserManager.parseEmbedded.
//     */
//    public static void parseEmbedded (
//        Result              result, 
//        String              mimeType, 
//        SchedulerTask      parserListener
//    ) {
//        
//    }
//    
//    
//    
//    static void setDocument (
//        Document            document
//    ) {
//        if (document == currentDocument) return;
//        System.out.println("\nParserManager.setDocument " + document.hashCode () + " (" + document.getProperty("title") + ")");
//        if (currentDocument != null) {
////            dispose (parserToParserListeners);
//            currentDocument.removeDocumentListener (listener);
//        }
//        currentDocument = document;
//        parserToParserListeners = new HashMap<Parser,List<PriorityParserListener>> ();
//        if (document != null) {
//            Iterator<? extends Parser> it = getParsers ().iterator ();
//            while (it.hasNext ()) {
//                Parser parser = it.next ();
//                List<PriorityParserListener> listeners = new ArrayList<PriorityParserListener> ();
//                Iterator<? extends TaskFactory> it2 = getParserListeresFactories ().iterator ();
//                while (it2.hasNext ()) {
//                    TaskFactory parserListenerFactory = it2.next ();
//                    PriorityParserListener parserListener = parserListenerFactory.create (parser, document);
//                    if (parserListener != null) {
//                        listeners.add (parserListener);
//                        System.out.println("  listener created " + parserListener + " (" + parser + ")");
//                    }
//                }
//                Collections.sort (listeners, parserListenerComparator);
//                parserToParserListeners.put (parser, listeners);
//            }
//            parse ();
//            if (listener == null) listener = new Listener ();
//            document.addDocumentListener (listener);
//        }
//    }
//    
//    private static RequestProcessor         rp = new RequestProcessor ("org.netbeans.modules.parsing.impl.ParserManager");
//    private static RequestProcessor.SchedulerTask    parsingTask;
//    
//    private static void parseLater () {
//        if (parsingTask != null) {
//            parsingTask.cancel ();
//        }
//        parsingTask = rp.post (new Runnable () {
//            public void run () {
//                parse ();
//            }
//        }, 1000);
//    }
//    
//    private static void parse () {
//        ((AbstractDocument) currentDocument).readLock ();
//        try {
//            Source source = null;
//            try {
//                source = new Source (
//                    currentDocument.getText (0, currentDocument.getLength ()),
//                    (String) currentDocument.getProperty ("mimeType"),
//                    currentDocument
//                );
//            } finally {
//                ((AbstractDocument) currentDocument).readUnlock ();
//            }
//            parse (source);
//        } catch (BadLocationException ex) {
//            ex.printStackTrace ();
//        }
//    }
//        
//    private static Map<Parser,Parser.Result> cache;
//    
//    public static void parse (
//        Source              source
//    ) {
//        cache = new HashMap<Parser,Parser.Result> ();
//        Iterator<? extends Parser> it = parserToParserListeners.keySet ().iterator ();
//        while (it.hasNext ()) {
//            Parser parser = it.next ();
//            Parser.Result result = parser.parse (source);
//            cache.put (parser, result);
//            if (result == null) continue;
//            List<PriorityParserListener> listeners = parserToParserListeners.get (parser);
//            if (listeners.isEmpty ()) continue;
//            Iterator<PriorityParserListener> it2 = listeners.iterator ();
//            while (it2.hasNext ()) {
//                SchedulerTask parserListener = it2.next ();
//                parserListener.parsed (result, source);
//            }
//        }
//    }
//    
//    private static Lookup.Result<Parser>    parserLookupResult;
//    private static ParsersListener          parsersListener;
//    private static Set<Parser>              parsers;
//    
//    private static Lookup.Result getParsers (
//        String              mimeType, 
//        Class               cls,
//        ParsersListener     parsersListener
//    ) {
//        FileSystem fileSystem = Repository.getDefault ().getDefaultFileSystem ();
//        FileObject fileObject1 = fileSystem.findResource ("Editors");
//        DataFolder dataFolder1 = fileObject1 == null ? null : DataFolder.findFolder (fileObject1);
//        FileObject fileObject2 = fileSystem.findResource ("Editors/" + mimeType);
//        DataFolder dataFolder2 = fileObject2 == null ? null : DataFolder.findFolder (fileObject2);
//        Lookup lookup = dataFolder2 == null ? 
//            (Lookup) (dataFolder1 == null ? 
//                Lookup.EMPTY :
//                new FolderLookup (dataFolder1)) :
//            new ProxyLookup (
//                new FolderLookup (dataFolder1).getLookup (),
//                new FolderLookup (dataFolder2).getLookup ()
//            );
//
//        return parserLookupResult = Lookup.getDefault ().lookupResult (cls);
//    }
//
//    private static Lookup.Result<TaskFactory>   parserListenerFactoryLookupResult;
//    
//    private static Collection<? extends TaskFactory> getParserListeresFactories () {
//        if (parserListenerFactoryLookupResult == null) {
//            parserListenerFactoryLookupResult = Lookup.getDefault ().lookupResult (TaskFactory.class);
//        }
//        return parserListenerFactoryLookupResult.allInstances ();
//    }
//    
//    private static class ParsersListener implements ChangeListener, LookupListener {
//
//        public void stateChanged (
//            ChangeEvent     e
//        ) {
//            parse ();
//        }
//
//        public void resultChanged (
//            LookupEvent     ev
//        ) {
//            Set<Parser> remove = new HashSet<Parser> (parsers);
//            Set<Parser> newParsers = new HashSet<Parser> (parserLookupResult.allInstances ());
//            Iterator<Parser> it = newParsers.iterator ();
//            while (it.hasNext ()) {
//                Parser parser = it.next ();
//                if (!remove.remove (parser))
//                    parser.addChangeListener (parsersListener);
//            }
//            it = remove.iterator ();
//            while (it.hasNext ()) {
//                Parser parser = it.next ();
//                parser.removeChangeListener (parsersListener);
//            }
//            parsers = newParsers;
//            parse ();
//        }
//    }
//    
//    private static class Listener implements DocumentListener {
//
//        public void insertUpdate (
//            DocumentEvent   e
//        ) {
//            parseLater ();
//        }
//
//        public void removeUpdate (
//            DocumentEvent   e
//        ) {
//            parseLater ();
//        }
//
//        public void changedUpdate (
//            DocumentEvent   e
//        ) {
//            //parseUserTask ();
//        }
//    }
//    
//    private static final Comparator<PriorityParserListener> parserListenerComparator = new Comparator<PriorityParserListener> () {
//
//        public int compare (
//            PriorityParserListener o1, 
//            PriorityParserListener o2
//        ) {
//            return o1.getPriority () - o2.getPriority ();
//        }
//    };







