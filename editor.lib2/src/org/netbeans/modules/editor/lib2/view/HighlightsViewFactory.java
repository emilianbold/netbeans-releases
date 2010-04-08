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

package org.netbeans.modules.editor.lib2.view;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.openide.util.RequestProcessor;

/**
 * View factory returning highlights views. It is specific in that it always
 * covers the whole document area by views even if there are no particular highlights
 * <br/>
 * Currently the factory coalesces highlights change requests from non-AWT thread.
 *
 * @author Miloslav Metelka
 */

public final class HighlightsViewFactory extends EditorViewFactory implements HighlightsChangeListener {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsViewFactory.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsViewFactory.class.getName());

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsViewFactory.stack=true
    private static final boolean dumpHighlightChangeStack =
            Boolean.getBoolean(HighlightsViewFactory.class.getName() + ".stack");

    private static final RequestProcessor RP = 
            new RequestProcessor("Highlights-Coalescing", 1, false, false); // NOI18N

//    private static final int COALESCE_DELAY = 10; // Delay before highlights will be rendered

    private static final boolean SYNC_HIGHLIGHTS =
            Boolean.getBoolean("org.netbeans.editor.sync.highlights"); //NOI18N

    private Document doc;

    private Element lineElementRoot;

    private int lineIndex;

    private Element lineElement;

    /**
     * Next newline offset. There a special NewlineView must be used.
     */
    private int newlineOffset;

    private CharSequence docText;

    private HighlightsContainer highlightsContainer;

    private HighlightsSequence highlightsSequence;

    private int highlightStartOffset;

    private int highlightEndOffset;

    private AttributeSet highlightAttributes;

    private int highlightAreaEndOffset;

    private int highlightAreaEndLineIndex;

    private int affectedStartOffset = Integer.MAX_VALUE;

    private int affectedEndOffset;

    /*private*/ final Object affectedRangeMonitor = new String("affected-range-lock"); // NOI18N

    private Runnable affectedRangePendingRunnable;

    public HighlightsViewFactory(JTextComponent component) {
        super(component);
    }

    @Override
    public void restart(int startOffset, int matchOffset) {
        doc = textComponent().getDocument();
        docText = DocumentUtilities.getText(doc);
        highlightsContainer = HighlightingManager.getInstance().getHighlights(textComponent(), null);
        highlightsContainer.addHighlightsChangeListener(this);
        lineElementRoot = doc.getDefaultRootElement();
        lineIndex = lineElementRoot.getElementIndex(startOffset);
        fetchLineInfo();

        if (matchOffset <= newlineOffset + 1) { // within same line
            highlightAreaEndLineIndex = lineIndex;
            highlightAreaEndOffset = newlineOffset + 1;
        } else {
            highlightAreaEndLineIndex = lineElementRoot.getElementIndex(matchOffset);
            highlightAreaEndOffset = lineElementRoot.getElement(highlightAreaEndLineIndex).getEndOffset();
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("GetHighlights: <" + startOffset + "," + highlightAreaEndOffset + ">\n");
        }
        highlightsSequence = highlightsContainer.getHighlights(startOffset, highlightAreaEndOffset);
        assert (highlightsSequence != null);
        highlightStartOffset = highlightEndOffset = -1;
        fetchNextHighlight();
    }

    @Override
    public int nextViewStartOffset(int offset) {
        // This layer returns a view for any given offset
        // since it must cover all the offset space with views.
        return offset;
    }

    @Override
    public EditorView createView(int startOffset, int limitOffset) {
        updateHighlight(startOffset);
        updateLine(startOffset);
        if (startOffset == newlineOffset) {
            return new NewlineView(startOffset, (startOffset >= highlightStartOffset) &&
                    (startOffset + 1 <= highlightEndOffset) ? highlightAttributes : null);
        } else if (startOffset < highlightStartOffset) { // Before highlight
            int endOffset = Math.min(Math.min(highlightStartOffset, limitOffset), newlineOffset);
            return createHighlightsView(startOffset, endOffset - startOffset, null);
        } else { // Inside highlight
            int endOffset = Math.min(Math.min(highlightEndOffset, limitOffset), newlineOffset);
            return createHighlightsView(startOffset, endOffset - startOffset, highlightAttributes);
        }
    }

    private EditorView createHighlightsView(int startOffset, int length, AttributeSet attrs) {
        if (length <= 0) {
            throw new IllegalStateException("startOffset=" + startOffset // NOI18N
                    + ", length=" + length + ", highlight: <" + highlightStartOffset // NOI18N
                    + "," + highlightEndOffset // NOI18N
                    + ">, newlineOffset=" + newlineOffset + ", docText.length()=" + docText.length()); // NOI18N
        }
        boolean tabs = (docText.charAt(startOffset) == '\t');
        for (int i = 1; i < length; i++) {
            if (tabs != (docText.charAt(startOffset + i) == '\t')) {
                length = i;
                break;
            }
        }
        return tabs
                ? new TabView(startOffset, length, attrs)
                : new HighlightsView(startOffset, length, attrs);
    }

    private void updateHighlight(int offset) {
        while (highlightEndOffset <= offset) {
            fetchNextHighlight();
        }
    }

    private void updateLine(int offset) {
        while (newlineOffset < offset && lineIndex + 1 < lineElementRoot.getElementCount()) {
            lineIndex++;
            fetchLineInfo();
        }
    }

    private void fetchLineInfo() {
        lineElement = lineElementRoot.getElement(lineIndex);
        newlineOffset = lineElement.getEndOffset() - 1;
    }

    private void fetchNextHighlight() {
        boolean done;
        do {
            done = true;
            if (highlightsSequence != null) {
                if (highlightsSequence.moveNext()) {
                    highlightStartOffset = highlightsSequence.getStartOffset();
                    highlightEndOffset = highlightsSequence.getEndOffset();
                    highlightAttributes = highlightsSequence.getAttributes();
                    // Empty highlight occurred (Highlights API does not comment such case) so possibly re-call.
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.fine("Highlight: <" + highlightStartOffset + "," + highlightEndOffset + // NOI18N
                                "> " + ViewUtils.toString(highlightAttributes) + '\n');
                    }
                    if (highlightStartOffset >= highlightEndOffset) { // Empty highlight
                        done = false; // Fetch next highlight from the same highlightsSequence
                    }
                } else {
                    if (++highlightAreaEndLineIndex < lineElementRoot.getElementCount()) {
                        int startOffset = highlightAreaEndOffset;
                        highlightAreaEndOffset = lineElementRoot.getElement(highlightAreaEndLineIndex).getEndOffset();
                        assert(startOffset <= highlightAreaEndOffset) :
                            "startOffset=" + startOffset + " <= highlightAreaEndOffset=" + highlightAreaEndOffset; // NOI18N
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Extra-GetHighlights: <" + startOffset + "," + highlightAreaEndOffset + ">\n");
                        }
                        highlightsSequence = highlightsContainer.getHighlights(startOffset, highlightAreaEndOffset);
                        done = false;
                    } else {
                        highlightsSequence = null;
                        // Leave original HS => no more highlights will be fetched
                        highlightStartOffset = Integer.MAX_VALUE;
                        highlightEndOffset = Integer.MAX_VALUE;
                        highlightAttributes = null;
                    }
                }
            }
        } while (!done);
    }

    public void finish() {
        lineElementRoot = null;
        lineElement = null;
        highlightsSequence = null;
        highlightAttributes = null;
    }

    @Override
    public Change insertUpdate(DocumentEvent evt) {
        return null;
    }

    @Override
    public Change removeUpdate(DocumentEvent evt) {
        return null;
    }

    @Override
    public Change changedUpdate(DocumentEvent evt) {
        return null;
    }

    @Override
    public void highlightChanged(HighlightsChangeEvent event) {
        int startOffset = event.getStartOffset();
        int endOffset = event.getEndOffset();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINER, "highlightChanged: event:<{0}{1}{2}>, thread:{3}\n",
                    new Object[] {startOffset, ',', endOffset, Thread.currentThread()}); // NOI18N
            if (dumpHighlightChangeStack) {
                LOG.log(Level.INFO, "Highlight Change Thread Dump for <" + // NOI18N
                        startOffset + "," + endOffset + ">", new Exception()); // NOI18N
            }
        }
        if (endOffset > startOffset) { // May possibly be == e.g. for cut-line action
            // Coalesce highglihts events by reposting to RP and then to EDT
            extendAffectedRange(startOffset, endOffset);
            // Post affected range update
            synchronized (affectedRangeMonitor) {
                affectedRangePendingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        boolean lastPending;
                        synchronized (affectedRangeMonitor) {
                            lastPending = (affectedRangePendingRunnable == this);
                        }
                        if (lastPending) {
                            if (SwingUtilities.isEventDispatchThread()) { // Already posted to AWT
                                if (doc instanceof AbstractDocument) {
                                    AbstractDocument adoc = (AbstractDocument) doc;
                                    adoc.readLock();
                                    try {
                                        checkFireAffectedAreaChange();
                                    } finally {
                                        adoc.readUnlock();
                                    }
                                }
                            } else { // Came from RP => repost to EDT
                                SwingUtilities.invokeLater(this);
                            }
                        }
                    }
                };
                if (SYNC_HIGHLIGHTS) {
                    affectedRangePendingRunnable.run(); // Run views rebuild synchronously
                } else {
                    RP.post(affectedRangePendingRunnable);
                }
            }
        }
    }

    void checkFireAffectedAreaChange() {
        int[] range = getAndClearAffectedRange();
        if (range != null) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.fine("coallesced-event: <" + range[0] + ',' + range[1] + ">\n"); // NOI18N
            }
            fireEvent(Collections.singletonList(createChange(range[0], range[1])));
        }
    }

    void extendAffectedRange(int startOffset, int endOffset) {
        synchronized (affectedRangeMonitor) {
            if (affectedStartOffset == Integer.MAX_VALUE) {
                affectedStartOffset = startOffset;
                affectedEndOffset = endOffset;
            } else {
                affectedStartOffset = Math.min(affectedStartOffset, startOffset);
                affectedEndOffset = Math.max(affectedEndOffset, endOffset);
            }
        }
    }

    int[] getAndClearAffectedRange() {
        synchronized (affectedRangeMonitor) {
            int[] range;
            if (affectedStartOffset == Integer.MAX_VALUE) {
                range = null;
            } else {
                range = new int[] { affectedStartOffset, affectedEndOffset };
                affectedStartOffset = Integer.MAX_VALUE;
            }
            return range;
        }
    }

    public static final class HighlightsFactory implements EditorViewFactory.Factory {

        @Override
        public EditorViewFactory createEditorViewFactory(JTextComponent component) {
            return new HighlightsViewFactory(component);
        }

        @Override
        public int importance() {
            return 0;
        }

    }

}
