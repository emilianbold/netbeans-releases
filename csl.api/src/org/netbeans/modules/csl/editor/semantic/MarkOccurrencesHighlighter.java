/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OccurrencesFinder;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.api.ColoringAttributes.Coloring;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public class MarkOccurrencesHighlighter extends ParserResultTask<ParserResult> {

    private static final Logger LOG = Logger.getLogger(MarkOccurrencesHighlighter.class.getName());
    
    //private FileObject file;
    private final Language language;
    private final Snapshot snapshot;
    static Coloring MO = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.MARK_OCCURRENCES);
    
    /** Creates a new instance of SemanticHighlighter */
    MarkOccurrencesHighlighter(Language language, Snapshot snapshot) {
        this.language = language;
        this.snapshot = snapshot;
    }
    
    public static final Color ES_COLOR = new Color( 175, 172, 102 ); // new Color(244, 164, 113);
    
//    public Document getDocument() {
//        return snapshot.getSource().getDocument(false);
//    }
//
    public void run(ParserResult info, SchedulerEvent event) {
        resume();
        
        Document doc = snapshot.getSource().getDocument(false);
        
        if (doc == null) {
            LOG.log(Level.INFO, "MarkOccurencesHighlighter: Cannot get document!"); //NOI18N
            return ;
        }
        
        if (!(event instanceof CursorMovedSchedulerEvent)) {
            return;
        }

        long start = System.currentTimeMillis();

        int caretPosition = ((CursorMovedSchedulerEvent) event).getCaretOffset();
        
        if (isCancelled()) {
            return;
        }

        List<OffsetRange> bag = processImpl(info, doc, caretPosition);
        if(bag == null) {
            //the occurrences finder haven't found anything, just ignore the result
            //and keep the previous occurrences
            return ;
        }

        if (isCancelled()) {
            return;
        }
        
        //Logger.getLogger("TIMER").log(Level.FINE, "Occurrences",
        //    new Object[] {((DataObject) doc.getProperty(Document.StreamDescriptionProperty)).getPrimaryFile(), (System.currentTimeMillis() - start)});
        //
        
// TODO: Support KEEP_MARKS!        
//        if (bag == null) {
//            if (node.getBoolean(MarkOccurencesSettings.KEEP_MARKS, true)) {
//                return ;
//            }
//            
//            bag = new ArrayList<int[]>();
//        }

        if (bag.size() > 0) {
            Collections.sort(bag);
        }
        OffsetsBag obag = new OffsetsBag(doc);
        obag.clear();
        
        if (bag.size() > 0) {
            AttributeSet attributes = language.getColoringManager().getColoringImpl(MO);

            for (OffsetRange range : bag) {
                if (range != OffsetRange.NONE) {
                    obag.addHighlight(range.getStart(), range.getEnd(), attributes);
                }
            }
        }
        
        getHighlightsBag(doc).setHighlights(obag);
        OccurrencesMarkProvider.get(doc).setOccurrences(OccurrencesMarkProvider.createMarks(doc, bag, ES_COLOR, NbBundle.getMessage(MarkOccurrencesHighlighter.class, "LBL_ES_TOOLTIP")));
    }
    
    @NonNull
    List<OffsetRange> processImpl(ParserResult info, Document doc, int caretPosition) {
        OccurrencesFinder finder = language.getOccurrencesFinder();
        assert finder != null;
        
        finder.setCaretPosition(caretPosition);
        try {
            finder.run(info, null);
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }

        if (isCancelled()) {
            finder.cancel();
        }

        Map<OffsetRange, ColoringAttributes> highlights = finder.getOccurrences();

        return highlights == null ? null : new ArrayList<OffsetRange>(highlights.keySet());
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass () {
        return Scheduler.CURSOR_SENSITIVE_TASK_SCHEDULER;
    }
    
    private boolean canceled;
    
    public final synchronized void cancel() {
        canceled = true;
    }
    
    protected final synchronized boolean isCancelled() {
        return canceled;
    }
    
    protected final synchronized void resume() {
        canceled = false;
    }
    
    static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(MarkOccurrencesHighlighter.class);
        
        if (bag == null) {
            doc.putProperty(MarkOccurrencesHighlighter.class, bag = new OffsetsBag(doc, false));
            
            final OffsetsBag bagFin = bag;
            DocumentListener l = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }
                public void removeUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }
                public void changedUpdate(DocumentEvent e) {}
            };
            
            doc.addDocumentListener(l);
            
            Object stream = DataLoadersBridge.getDefault().getFileObject(doc);
            if (stream instanceof FileObject) {
                Logger.getLogger("TIMER").log(Level.FINE, "MarkOccurrences Highlights Bag", new Object[] {(FileObject) stream, bag}); //NOI18N
                Logger.getLogger("TIMER").log(Level.FINE, "MarkOccurrences Highlights Bag Listener", new Object[] {(FileObject) stream, l}); //NOI18N
            }
        }
        
        return bag;
    }
}
