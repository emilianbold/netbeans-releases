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
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsSequenceEx;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.openide.util.WeakListeners;

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

    private final HighlightsContainer highlightsContainer;

    private CharSequence docText;
    private Element lineElementRoot;

    private int lineIndex;

    private int lineEndOffset;

    private HighlightsSequence highlightsSequence;
    private int highlightStartOffset;
    private int highlightEndOffset;
    private AttributeSet highlightAttributes;

    private int usageCount = 0; // Avoid nested use of the factory

    public HighlightsViewFactory(JTextComponent component) {
        super(component);
        highlightsContainer = HighlightingManager.getInstance().getHighlights(component, null);
        highlightsContainer.addHighlightsChangeListener(WeakListeners.create(HighlightsChangeListener.class, this, highlightsContainer));
    }

    @Override
    public void restart(int startOffset, int matchOffset) {
        if (usageCount != 0) {
            throw new IllegalStateException("Race condition: usageCount = " + usageCount); // NOI18N
        }
        usageCount++;
        docText = DocumentUtilities.getText(document());
        lineElementRoot = document().getDefaultRootElement();
        assert (lineElementRoot != null) : "lineElementRoot is null."; // NOI18N
        lineIndex = lineElementRoot.getElementIndex(startOffset);
        lineEndOffset = lineElementRoot.getElement(lineIndex).getEndOffset();
        highlightEndOffset = Integer.MIN_VALUE; // Makes the highlightsSequence to be inited
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
        updateLineEndOffset(startOffset);
        if (startOffset == lineEndOffset - 1) {
            return new NewlineView(startOffset, (startOffset >= highlightStartOffset) &&
                    (startOffset + 1 <= highlightEndOffset) ? highlightAttributes : null);
        } else if (startOffset < highlightStartOffset) { // Before highlight
            int endOffset = Math.min(Math.min(highlightStartOffset, limitOffset), lineEndOffset - 1);
            // Prevent exception thrown from createHighlightsView() when
            // startOffset=1, newLineOffset=0 => endOffset=0, highlight: <2147483647,2147483647>, docText.length()=1
            return createHighlightsView(startOffset, endOffset - startOffset, null);
        } else { // Inside highlight
            int endOffset = Math.min(Math.min(highlightEndOffset, limitOffset), lineEndOffset - 1);
            return createHighlightsView(startOffset, endOffset - startOffset, highlightAttributes);
        }
    }

    @Override
    public int viewEndOffset(int startOffset, int limitOffset) {
        updateLineEndOffset(startOffset);
        return Math.min(lineEndOffset, limitOffset);
    }

    private EditorView createHighlightsView(int startOffset, int length, AttributeSet attrs) {
        if (length <= 0) {
            throw new IllegalStateException("startOffset=" + startOffset // NOI18N
                    + ", length=" + length + ", highlight: <" + highlightStartOffset // NOI18N
                    + "," + highlightEndOffset // NOI18N
                    + ">, lineEndOffset=" + lineEndOffset + ", docText.length()=" + docText.length()); // NOI18N
        }
        boolean tabs = (docText.charAt(startOffset) == '\t'); //NOI18N
        for (int i = 1; i < length; i++) {
            if (tabs != (docText.charAt(startOffset + i) == '\t')) { //NOI18N
                length = i;
                break;
            }
        }
        return tabs
                ? new TabView(startOffset, length, attrs)
                : new HighlightsView(startOffset, length, attrs);
    }

    private void updateLineEndOffset(int offset) {
        if (usageCount != 1) {
            throw new IllegalStateException("Missing factory restart: usageCount=" + usageCount);
        }
        while (lineEndOffset <= offset) { // && lineIndex + 1 < lineElementRoot.getElementCount()) {
            lineIndex++;
            Element line = lineElementRoot.getElement(lineIndex);
            lineEndOffset = line.getEndOffset();
        }
    }

    private void updateHighlight(int offset) {
        if (offset >= highlightEndOffset) { // Covers case when highlightEndOffset==Integer.MIN_VALUE at begining
            if (highlightsSequence == null && highlightEndOffset == Integer.MIN_VALUE) { // HS not yet created
                highlightsSequence = highlightsContainer.getHighlights(offset, Integer.MAX_VALUE);
            }
            while (highlightsSequence != null) {
                while (highlightsSequence instanceof HighlightsSequenceEx && ((HighlightsSequenceEx) highlightsSequence).isStale()) {
                    highlightsSequence = highlightsContainer.getHighlights(offset, Integer.MAX_VALUE);
                }

                if (highlightsSequence.moveNext()) {
                    highlightStartOffset = highlightsSequence.getStartOffset();
                    highlightEndOffset = highlightsSequence.getEndOffset();
                    highlightAttributes = highlightsSequence.getAttributes();

                    if (highlightStartOffset < highlightEndOffset) {
                        if (LOG.isLoggable(Level.FINEST)) {
                            LOG.fine("Highlight: <" + highlightStartOffset + "," + highlightEndOffset + "> " // NOI18N
                                + ViewUtils.toString(highlightAttributes) + "\n"); //NOI18N
                        }
                        if (offset < highlightEndOffset) {
                            break;
                        }
                    } else { // Invalid highlight -> Fetch next
                        if (highlightStartOffset > highlightEndOffset) {
                            LOG.info("Invalid highlight: <" + highlightStartOffset + "," + highlightEndOffset + ">\n"); // NOI18N
                        }
                    }
                } else {
                    highlightsSequence = null;
                    highlightAttributes = null;
                    highlightStartOffset = Integer.MAX_VALUE;
                    highlightEndOffset = Integer.MAX_VALUE; // Marks end of highlights traversal (together with highlightsSequence==null)
                }
            }
        }
    }

    @Override
    public void finish() {
        docText = null;
        lineElementRoot = null;
        lineIndex = -1;
        lineEndOffset = -1;
        highlightsSequence = null;
        highlightStartOffset = Integer.MAX_VALUE;
        highlightEndOffset = Integer.MAX_VALUE;
        highlightAttributes = null;
        usageCount--;
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
        int docTextLength = document().getLength() + 1;
        assert (startOffset >= 0) : "startOffset=" + startOffset + " < 0"; // NOI18N
        assert (endOffset >= 0) : "startOffset=" + endOffset + " < 0"; // NOI18N
        startOffset = Math.min(startOffset, docTextLength);
        endOffset = Math.min(endOffset, docTextLength);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINER, "highlightChanged: event:<{0}{1}{2}>, thread:{3}\n", //NOI18N
                    new Object[] {startOffset, ',', endOffset, Thread.currentThread()}); // NOI18N
        }

        if (startOffset <= endOffset) { // May possibly be == e.g. for cut-line action
            fireEvent(Collections.singletonList(createChange(startOffset, endOffset)));
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
