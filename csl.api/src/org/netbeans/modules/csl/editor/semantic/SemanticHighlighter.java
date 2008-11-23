/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.csl.editor.semantic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.ColoringAttributes.Coloring;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.ErrorManager;


/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public class SemanticHighlighter extends ParserResultTask<ParserResult> {

    private static final Logger LOG = Logger.getLogger(SemanticHighlighter.class.getName());
    
    private final Snapshot snapshot;
    
    /** Creates a new instance of SemanticHighlighter */
    SemanticHighlighter(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public Document getDocument() {
        return snapshot.getSource().getDocument();
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass () {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    private boolean cancelled;

    public final synchronized void cancel() {
        cancelled = true;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public @Override void run(ParserResult info) {
        resume();
        
        Document doc = getDocument();

        if (doc == null) {
            Logger.global.log(Level.INFO, "SemanticHighlighter: Cannot get document!");
            return;
        }

        process(info, doc/*, ERROR_DESCRIPTION_SETTER*/);
    }
    

    boolean process(ParserResult info, final Document doc/*, ErrorDescriptionSetter setter*/) {
        final SortedSet<SequenceElement> newColoring = new TreeSet<SequenceElement>();

// XXX: parsingapi
        if (isCancelled()) { //  || info.hasInvalidResults()
            return true;
        }

        //final Map<OffsetRange, Coloring> oldColors = GsfSemanticLayer.getLayer(SemanticHighlighter.class, doc).getColorings();
        //final Set<OffsetRange> removedTokens = new HashSet<OffsetRange>(oldColors.keySet());
        //final Set<OffsetRange> addedTokens = new HashSet<OffsetRange>();
        //if (isCancelled()) {
        //    return true;
        //}


        long start = System.currentTimeMillis();        
        Set<String> mimeTypes = getEmbeddedMimeTypes(info.getSnapshot().getSource());
        final GsfSemanticLayer layer = GsfSemanticLayer.getLayer(SemanticHighlighter.class, doc);

// XXX: parsingapi
//        EditHistory currentHistory = info.getHistory();
//        final int version = currentHistory.getVersion();
//
//
//        // Attempt to do less work in embedded scenarios: Only recompute hints for regions
//        // that have changed
//        LanguageRegistry registry = LanguageRegistry.getInstance();
//        SortedSet<SequenceElement> colorings = layer.getColorings();
//        int previousVersion = layer.getVersion();
//// XXX: parsingapi
//        if (mimeTypes.size() > 1 && colorings.size() > 0) { // && info.hasUnchangedResults()
//
//            // Sort elements into buckets per language
//            Map<Language,List<SequenceElement>> elements = new HashMap<Language,List<SequenceElement>>();
//            List<SequenceElement> prevList = null;
//            Language prevLanguage = null;
//            for (SequenceElement element : colorings) {
//                List<SequenceElement> list;
//                if (element.language == prevLanguage) {
//                    list = prevList;
//                } else {
//                    list = elements.get(element.language);
//                    if (list == null) {
//                        list = new ArrayList<SequenceElement>();
//                        elements.put(element.language, list);
//                        prevLanguage = element.language;
//                        prevList = list;
//                    }
//                }
//                list.add(element);
//            }
//
//            // Recompute lists for languages that have changed
//            EditHistory history = EditHistory.getCombinedEdits(previousVersion, currentHistory);
//            if (history != null) {
//            int offset = history.getStart();
//            for (String mimeType : mimeTypes) {
//                if (isCancelled()) {
//                    return true;
//                }
//                Language language = registry.getLanguageByMimeType(mimeType);
//                if (language == null) {
//                    continue;
//                }
//
//                // Unchanged result?
//                ParserResult result = info.getEmbeddedResult(mimeType, 0);
//                if (result != null && result.getUpdateState().isUnchanged()) {
//
//                    // This section was not edited in the last parse tree,
//                    // so just grab the previous elements, and use them
//                    // (after tweaking the offsets)
//                    List<SequenceElement> list = elements.get(language);
//
//                    if (list != null) {
//                        for (SequenceElement element : list) {
//                            if (element.language == language) {
//                                OffsetRange range = element.range;
//                                if (range.getStart() > offset) {
//                                    element.range = new OffsetRange(history.convertOriginalToEdited(range.getStart()),
//                                            history.convertOriginalToEdited(range.getEnd()));
//                                }
//                                newColoring.add(element);
//                            }
//                        }
//                    }
//
//                    continue;
//                } else {
//                    // We need to recompute the semantic highlights for this language
//                    ColoringManager manager = language.getColoringManager();
//                    SemanticAnalyzer task = language.getSemanticAnalyzer();
//                    if (task != null) {
//                        // Allow language plugins to do their own analysis too
//                        try {
//                            task.run(info);
//                        } catch (Exception ex) {
//                            ErrorManager.getDefault().notify(ex);
//                        }
//
//                        if (isCancelled()) {
//                            task.cancel();
//                            return true;
//                        }
//
//                        Map<OffsetRange,Set<ColoringAttributes>> highlights = task.getHighlights();
//                        if (highlights != null) {
//                            for (OffsetRange range : highlights.keySet()) {
//
//                                Set<ColoringAttributes> colors = highlights.get(range);
//                                if (colors == null) {
//                                    continue;
//                                }
//
//                                Coloring c = manager.getColoring(colors);
//
//                                //newColoring.put(range, c);
//                                newColoring.add(new SequenceElement(language, range, c));
//                            }
//                        }
//                    }
//                }
//            }
//
//            layer.setColorings(newColoring, version);
//            return true;
//            }
//        }
        
        for (String mimeType : mimeTypes) {
            if (isCancelled()) {
                return true;
            }

            long startTime = System.currentTimeMillis();
            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(mimeType);
            if (language == null) {
                continue;
            }
            ColoringManager manager = language.getColoringManager();
            SemanticAnalyzer task = language.getSemanticAnalyzer();
            if (task != null) {
                // Allow language plugins to do their own analysis too
                try {
                    task.run(info);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }

                if (isCancelled()) {
                    task.cancel();
                    return true;
                }

                Map<OffsetRange,Set<ColoringAttributes>> highlights = task.getHighlights();
                if (highlights != null) {
                    for (OffsetRange range : highlights.keySet()) {

                        Set<ColoringAttributes> colors = highlights.get(range);
                        if (colors == null) {
                            continue;
                        }
                        
                        Coloring c = manager.getColoring(colors);

                        //newColoring.put(range, c);
                        newColoring.add(new SequenceElement(language, range, c));

                        //if (!removedTokens.remove(range)) {
                        //    addedTokens.add(range);
                        //}
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            Logger.getLogger("TIMER").log(Level.FINE, "Semantic (" + mimeType + ")",
                    new Object[] {info.getSnapshot().getSource().getFileObject(), endTime - startTime});
        }

        SwingUtilities.invokeLater(new Runnable () {
            public void run() {
// XXX: parsingapi
                layer.setColorings(newColoring, -1); //version
            }                
        });            
        
//        Logger.getLogger("TIMER").log(Level.FINE, "Semantic",
//            new Object[] {((DataObject) doc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile(), System.currentTimeMillis() - start});

        return false;
    }

    private static Set<String> getEmbeddedMimeTypes(Source source) {
        final Set<String> mimeTypes = new HashSet<String>();

        try {
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                public @Override void run(ResultIterator resultIterator) {
                    mimeTypes.add(resultIterator.getSnapshot().getMimeType());
                    for(Embedding e : resultIterator.getEmbeddings()) {
                        run(resultIterator.getResultIterator(e));
                    }
                }
            });
        } catch (ParseException e) {
            LOG.log(Level.WARNING, null, e);
        }

        return mimeTypes;
    }
}
