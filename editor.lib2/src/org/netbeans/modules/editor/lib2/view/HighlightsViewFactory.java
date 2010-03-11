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

import java.awt.Color;
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
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * View factory returning highlights views. It is specific in that it always
 * covers the whole document area by views even if there are no particular highlights
 *
 * @author Miloslav Metelka
 */

public final class HighlightsViewFactory extends EditorViewFactory implements HighlightsChangeListener {

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsViewFactory.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightsViewFactory.class.getName());

    private int offset;

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

    public HighlightsViewFactory(JTextComponent component) {
        super(component);
        highlightsContainer = HighlightingManager.getInstance().getHighlights(textComponent(), null);
        highlightsContainer.addHighlightsChangeListener(this);
    }

    @Override
    public void restart(int startOffset) {
        this.offset = startOffset;
        Document doc = textComponent().getDocument();
        docText = DocumentUtilities.getText(doc);
        lineElementRoot = doc.getDefaultRootElement();
        lineIndex = lineElementRoot.getElementIndex(offset);
        fetchLineInfo();

        highlightsSequence = highlightsContainer.getHighlights(startOffset, doc.getLength());
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
                    + ", length=" + length + "highlight: <" + highlightStartOffset // NOI18N
                    + "," + highlightEndOffset // NOI18N
                    + ">, newlineOffset=" + newlineOffset); // NOI18N
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
        if (highlightsSequence.moveNext()) {
            highlightStartOffset = highlightsSequence.getStartOffset();
            highlightEndOffset = highlightsSequence.getEndOffset();
            highlightAttributes = highlightsSequence.getAttributes();
            assert (highlightStartOffset < highlightEndOffset) : "Empty highlight at offset=" + highlightStartOffset; // NOI18N
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.fine("Highlight: <" + highlightStartOffset + "," + highlightEndOffset + // NOI18N
                        "> " + ViewUtils.toString(highlightAttributes) + '\n');
            }

        } else {
            highlightStartOffset = Integer.MAX_VALUE;
            highlightEndOffset = Integer.MAX_VALUE;
            highlightAttributes = null;
        }
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
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("highlightChanged: event:<" + event.getStartOffset() + ',' + event.getEndOffset() + ">\n"); // NOI18N
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.INFO, "Highlight Change Thread Dump", new Exception());
            }
        }
        fireEvent(Collections.singletonList(createChange(event.getStartOffset(), event.getEndOffset())));
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
