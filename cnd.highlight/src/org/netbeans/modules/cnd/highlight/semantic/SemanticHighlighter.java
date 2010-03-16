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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences.Visitor;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository.Interrupter;
import org.netbeans.modules.cnd.highlight.InterrupterImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.cnd.modelutil.NamedEntityOptions;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;

/**
 * Semantic C/C++ code highlighter responsible for "graying out"
 * inactive code due to preprocessor definitions and highlighting of unobvious
 * language elements.
 *
 * @author Sergey Grinev
 */
public final class SemanticHighlighter extends HighlighterBase {

    private static final Logger LOG = Logger.getLogger(SemanticHighlighter.class.getName());

    public SemanticHighlighter(Document doc) {
        super(doc); 
        init(doc);
    }

    @Override
    protected void updateFontColors(FontColorProvider provider) {
        for (SemanticEntity semanticEntity : SemanticEntitiesProvider.instance().get()) {
            semanticEntity.updateFontColors(provider);
        }
    }

    public static PositionsBag getHighlightsBag(Document doc) {
        if (doc == null) {
            return null;
        }

        PositionsBag bag = (PositionsBag) doc.getProperty(SemanticHighlighter.class);

        if (bag == null) {
            doc.putProperty(SemanticHighlighter.class, bag = new PositionsBag(doc));
        }

        return bag;
    }

    private static final int MAX_LINE_NUMBER;

    static {
        String limit = System.getProperty("cnd.semantic.line.limit"); // NOI18N
        int userInput = 5000;
        if (limit != null) {
            try {
                userInput = Integer.parseInt(limit);
            } catch (Exception e) {
                // skip
            }
        }
        MAX_LINE_NUMBER = userInput;
    }

    public static boolean isVeryBigDocument(Document doc) {
        if (!(doc instanceof BaseDocument) || MAX_LINE_NUMBER < 0) {
            return false;
        }
        try {
            if (doc.getLength() < MAX_LINE_NUMBER) {
                return false;
            }
            return Utilities.getLineOffset((BaseDocument)doc, doc.getLength() - 1) > MAX_LINE_NUMBER;
        } catch (BadLocationException ex) {
            // skip
            return true;
        }
    }

    private void update(final Interrupter interrupter) {
        BaseDocument doc = getDocument();
        if (doc != null) {
            DocumentListener listener =  null;
            if (interrupter instanceof InterrupterImpl) {
                listener = new DocumentListener(){
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        ((InterrupterImpl)interrupter).cancel();
                    }
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        ((InterrupterImpl)interrupter).cancel();
                    }
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                    }
                };
                doc.addDocumentListener(listener);
            }
            try {
                update(doc, interrupter);
            } finally {
                if (listener != null) {
                    doc.removeDocumentListener(listener);
                }
            }
        }
    }

    private void update(BaseDocument doc, final Interrupter interrupter) {
        boolean macroExpansionView = (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null);
        PositionsBag newBag = new PositionsBag(doc);
        newBag.clear();
        final CsmFile csmFile = CsmUtilities.getCsmFile(doc, false, false);
        long start = System.currentTimeMillis();
        if (csmFile != null && csmFile.isParsed()) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Semantic Highlighting update() have started for file {0}", csmFile.getAbsolutePath());
            }
            final List<SemanticEntity> entities = new ArrayList<SemanticEntity>(SemanticEntitiesProvider.instance().get());
            final List<ReferenceCollector> collectors = new ArrayList<ReferenceCollector>(entities.size());
            // the following loop deals with entities without collectors
            // and gathers collectors for the next step
            for (Iterator<SemanticEntity> i = entities.iterator(); i.hasNext(); ) {
                SemanticEntity se = i.next();
                if (NamedEntityOptions.instance().isEnabled(se) && 
                        (!macroExpansionView || !se.getName().equals("macros"))) { // NOI18N
                    ReferenceCollector collector = se.getCollector();
                    if (collector != null) {
                        // remember the collector for future use
                        collectors.add(collector);
                    } else {
                        // this is simple entity without collector,
                        // let's add its blocks right now
                        addHighlightsToBag(newBag, se.getBlocks(csmFile), se);
                        i.remove();
                    }
                } else {
                    // skip disabled entity
                    i.remove();
                }
            }
            // to show inactive code and macros first
            PositionsBag old = getHighlightsBag(doc);
            if (old != null) {
                // this is done to prevent loss of other highlightings during adding ones managed by this highlighter
                // otherwise document will "blink" on editing
                PositionsBag tempBag = new PositionsBag(doc);
                tempBag.addAllHighlights(newBag);
                HighlightsSequence seq = newBag.getHighlights(0, Integer.MAX_VALUE);
                Set<AttributeSet> set = new HashSet<AttributeSet>();
                while (seq.moveNext()) {
                    set.add(seq.getAttributes());
                }
                seq = old.getHighlights(0, Integer.MAX_VALUE);
                while (seq.moveNext()) {
                    if (!set.contains(seq.getAttributes())) {
                        int startOffset = getDocumentOffset(doc, seq.getStartOffset());
                        int endOffset = getDocumentOffset(doc, seq.getEndOffset());
                        if (startOffset < doc.getLength() && endOffset > 0) {
                            addHighlightsToBag(tempBag, startOffset, endOffset, seq.getAttributes(), "cached"); //NOI18N
                        }
                    }
                }
                getHighlightsBag(doc).setHighlights(tempBag);
            } else {
                getHighlightsBag(doc).setHighlights(newBag);
            }
            // here we invoke the collectors
            // but not for huge documents
            if (!entities.isEmpty() && !isVeryBigDocument(doc)) {
                CsmFileReferences.getDefault().accept(csmFile, new Visitor() {
                    @Override
                    public void visit(CsmReferenceContext context) {
                        CsmReference ref = context.getReference();
                        for (ReferenceCollector c : collectors) {
                            if (interrupter.cancelled()) {
                                break;
                            }
                            c.visit(ref, csmFile);
                        }
                    }
                }, CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE_AND_PREPROCESSOR);
                // here we apply highlighting to discovered blocks
                for (int i = 0; i < entities.size(); ++i) {
                    addHighlightsToBag(newBag, collectors.get(i).getReferences(), entities.get(i));
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Semantic Highlighting update() done in {0}ms for file {1}", new Object[]{System.currentTimeMillis() - start, csmFile.getAbsolutePath()});
            }
        }
        if (!interrupter.cancelled()){
            getHighlightsBag(doc).setHighlights(newBag);
        }
    }

    private void addHighlightsToBag(PositionsBag bag, List<? extends CsmOffsetable> blocks, SemanticEntity entity) {
        Document doc = getDocument();
        if (doc != null) {
            for (CsmOffsetable block : blocks) {
                int startOffset = getDocumentOffset(doc, block.getStartOffset());
                int endOffset = block.getEndOffset();

                endOffset = getDocumentOffset(doc, endOffset == Integer.MAX_VALUE ? doc.getLength() + 1 : endOffset);
                if (startOffset < doc.getLength() && endOffset > 0) {
                    addHighlightsToBag(bag, startOffset, endOffset, entity.getAttributes(block), entity.getName());
                }
            }
        }
    }

    private void addHighlightsToBag(PositionsBag bag, int start, int end, AttributeSet attr, String nameToStateInLog) {
        try {
            Document doc = getDocument();
            if (doc != null) {
                bag.addHighlight(doc.createPosition(start), doc.createPosition(end), attr);
            }
        } catch (BadLocationException ex) {
            LOG.log(Level.FINE, "Can't add highlight <" + start + ", " + end + ", " + nameToStateInLog + ">", ex);
        }
    }

    private static int getDocumentOffset(Document doc, int fileOffset) {
        return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
    }

    // PhaseRunner
    @Override
    public void run(Phase phase) {
        if (phase == Phase.PARSED || phase == Phase.INIT || phase == Phase.PROJECT_PARSED) {
            InterrupterImpl interrupter = new InterrupterImpl();
            try {
                addCancelListener(interrupter);
                update(interrupter);
            } catch (AssertionError ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                removeCancelListener(interrupter);
            }
        } else if (phase == Phase.CLEANUP) {
            BaseDocument doc = getDocument();
            if (doc != null) {
                //System.err.println("cleanAfterYourself");
                getHighlightsBag(doc).clear();
            }
        }
    }
    
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isHighPriority() {
        return false;
    }
}
