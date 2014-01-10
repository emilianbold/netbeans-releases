/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences.Visitor;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.highlight.semantic.debug.InterrupterImpl;
import org.netbeans.modules.cnd.model.tasks.CndParserResult;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.ui.NamedOption;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;

/**
 * Semantic C/C++ code highlighter responsible for "graying out"
 * inactive code due to preprocessor definitions and highlighting of unobvious
 * language elements.
 *
 * @author Sergey Grinev
 */
public final class SemanticHighlighter extends HighlighterBase {
    private static final String SLOW_POSITION_BAG = "CndSemanticHighlighterSlow"; // NOI18N
    private static final String FAST_POSITION_BAG = "CndSemanticHighlighterFast"; // NOI18N
    private static final Logger LOG = Logger.getLogger(SemanticHighlighter.class.getName());
    
    private InterrupterImpl interrupter = new InterrupterImpl();

    public SemanticHighlighter(String mimeType) {
        init(mimeType);
    }

    @Override
    protected void updateFontColors(FontColorProvider provider) {
        for (SemanticEntity semanticEntity : SemanticEntitiesProvider.instance().get()) {
            semanticEntity.updateFontColors(provider);
        }
    }

    public static PositionsBag getHighlightsBag(Document doc, boolean fast) {
        if (doc == null) {
            return null;
        }
        final String name = fast ? FAST_POSITION_BAG : SLOW_POSITION_BAG;

        PositionsBag bag = (PositionsBag) doc.getProperty(name);

        if (bag == null) {
            doc.putProperty(name, bag = new PositionsBag(doc));
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

    private void update(Document doc, final InterrupterImpl interrupter) {
        if (doc != null) {
            updateImpl(doc, interrupter);
        }
    }
    
    public static PositionsBag getSemanticBagForTests(Document doc, InterrupterImpl interrupter, boolean fast) {
        final SemanticHighlighter semanticHighlighter = new SemanticHighlighter(DocumentUtilities.getMimeType(doc));
        semanticHighlighter.update(doc, interrupter);
        return getHighlightsBag(doc, fast);
    }

    private void updateImpl(Document doc, final InterrupterImpl interrupter) {
        boolean macroExpansionView = (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) != null);
        PositionsBag newBagFast = new PositionsBag(doc);
        PositionsBag newBagSlow = new PositionsBag(doc);
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
                if (NamedOption.getAccessor().getBoolean(se.getName()) && 
                        (!macroExpansionView || !se.getName().equals(SemanticEntitiesProvider.MacrosCodeProvider.NAME))) { // NOI18N
                    ReferenceCollector collector = se.getCollector();
                    if (collector != null) {
                        // remember the collector for future use
                        collectors.add(collector);
                    } else {
                        // this is simple entity without collector,
                        // let's add its blocks right now
                        addHighlightsToBag(doc, newBagFast, se.getBlocks(csmFile), se);
                        i.remove();
                    }
                } else {
                    // skip disabled entity
                    i.remove();
                }
            }
            // to show inactive code and macros first
            getHighlightsBag(doc, true).setHighlights(newBagFast);
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
                    addHighlightsToBag(doc, newBagSlow, collectors.get(i).getReferences(), entities.get(i));
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Semantic Highlighting update() done in {0}ms for file {1}", new Object[]{System.currentTimeMillis() - start, csmFile.getAbsolutePath()});
            }
            if (!interrupter.cancelled()){
                getHighlightsBag(doc, false).setHighlights(newBagSlow);
            }
        }
    }

    private void addHighlightsToBag(Document doc, PositionsBag bag, List<? extends CsmOffsetable> blocks, SemanticEntity entity) {
        if (doc != null) {
            String mimeType = DocumentUtilities.getMimeType(doc);
            if (mimeType == null) {
                mimeType = MIMENames.CPLUSPLUS_MIME_TYPE;
            }
           for (CsmOffsetable block : blocks) {
                int startOffset = getDocumentOffset(doc, block.getStartOffset());
                int endOffset = block.getEndOffset();

                endOffset = getDocumentOffset(doc, endOffset == Integer.MAX_VALUE ? doc.getLength() + 1 : endOffset);
                if (startOffset < doc.getLength() && endOffset > 0) {
                    final AttributeSet attributes = entity.getAttributes(block, mimeType);
                    if (attributes == null) {
                        assert false : "Color attributes set is not found for MIME "+mimeType+". Document "+doc;
                        return;
                    }
                    addHighlightsToBag(doc, bag, startOffset, endOffset, attributes, entity.getName());
                }
            }
        }
    }

    private void addHighlightsToBag(Document doc, PositionsBag bag, int start, int end, AttributeSet attr, String nameToStateInLog) {
        try {
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

    @Override
    public void run(CndParserResult result, SchedulerEvent event) {
        synchronized(this) {
            this.interrupter = new InterrupterImpl();
        }
        update(result.getSnapshot().getSource().getDocument(false), interrupter);
    }

    @Override
    public synchronized void cancel() {
        interrupter.cancel();
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }
    
    @Override
    public String toString() {
        return "SemanticHighlighter runner"; //NOI18N
    }
}
