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
package org.netbeans.modules.gsfret.editor.semantic;

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
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.api.ColoringAttributes.Coloring;
import org.netbeans.modules.gsf.api.DataLoadersBridge;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.gsfret.hints.infrastructure.Pair;
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
public class MarkOccurrencesHighlighter implements CancellableTask<CompilationInfo> {
    
    private FileObject file;
    static Coloring MO = ColoringAttributes.add(ColoringAttributes.empty(), ColoringAttributes.MARK_OCCURRENCES);
    
    /** Creates a new instance of SemanticHighlighter */
    MarkOccurrencesHighlighter(FileObject file) {
        this.file = file;
    }
    
    public static final Color ES_COLOR = new Color( 175, 172, 102 ); // new Color(244, 164, 113);
    
    public Document getDocument() {
        return DataLoadersBridge.getDefault().getDocument(file);
    }
    
    public void run(CompilationInfo info) {
        resume();
        
        Document doc = getDocument();
        
        if (doc == null) {
            Logger.global.log(Level.INFO, "MarkOccurencesHighlighter: Cannot get document!");
            return ;
        }
        
        long start = System.currentTimeMillis();
        
        int caretPosition = MarkOccurrencesHighlighterFactory.getLastPosition(file);
        
        if (isCancelled())
            return;

        Pair<List<OffsetRange>,Language> pair = processImpl(info,/* node,*/ doc, caretPosition);

        if (isCancelled())
            return;
        
        List<OffsetRange> bag = pair.getA();
        Language language = pair.getB();
        
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
    Pair<List<OffsetRange>,Language> processImpl(CompilationInfo info,/* node,*/ Document doc, int caretPosition) {
        List<Language> list = LanguageRegistry.getInstance().getEmbeddedLanguages((BaseDocument)doc, caretPosition);
        Language language = null;
        for (Language l : list) {
            if (l.getOccurrencesFinder() != null) {
                language = l;
                break;
            }
        }

        if (language != null) {
            OccurrencesFinder finder = language.getOccurrencesFinder();
            assert finder != null;
        
            finder.setCaretPosition(caretPosition);
            OccurrencesFinder task = finder;
            if (task != null) {
                try {
                    task.run(info);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                
                if (isCancelled()) {
                    task.cancel();
                }
                
                
                Map<OffsetRange,ColoringAttributes> highlights = task.getOccurrences();
                if (highlights != null) {
                    return new Pair(new ArrayList<OffsetRange>(highlights.keySet()), language);
                }
            }
        }
        
        return new Pair(Collections.emptyList(), info.getLanguage());
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
