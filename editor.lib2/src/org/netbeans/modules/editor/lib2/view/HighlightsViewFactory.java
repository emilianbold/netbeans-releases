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

import java.awt.Font;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.lib2.highlighting.DirectMergeContainer;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsReader;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
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

    private final HighlightingManager highlightingManager;

    private HighlightsContainer highlightsContainer;
    
    private HighlightsContainer paintHighlightsContainer;
    
    private HighlightsChangeListener weakHL;
    
    private HighlightsChangeListener paintWeakHL;

    private CharSequence docText;

    private Element lineElementRoot;

    private int lineIndex;
    
    private int lineEndOffset;
    
    /** Line index where tabs and highlights were last updated. */
    private int hlLineIndex;

    private HighlightsReader highlightsReader;
    
    private Font defaultFont;
    
    private int nextTabOffset;
    
    private int usageCount = 0; // Avoid nested use of the factory
    
    public HighlightsViewFactory(View documentView) {
        super(documentView);
        highlightingManager = HighlightingManager.getInstance(textComponent());
        highlightingManager.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                notifyStaleCreation();
                updateHighlightsContainer();
            }
        });
        updateHighlightsContainer();
    }

    private void updateHighlightsContainer() {
        if (highlightsContainer != null && weakHL != null) {
            highlightsContainer.removeHighlightsChangeListener(weakHL);
            paintHighlightsContainer.removeHighlightsChangeListener(paintWeakHL);
            weakHL = null;
            paintWeakHL = null;
        }
        highlightsContainer = highlightingManager.getBottomHighlights();
        highlightsContainer.addHighlightsChangeListener(weakHL = WeakListeners.create(
                HighlightsChangeListener.class, this, highlightsContainer));
        paintHighlightsContainer = highlightingManager.getTopHighlights();
        paintHighlightsContainer.addHighlightsChangeListener(paintWeakHL = WeakListeners.create(
                HighlightsChangeListener.class, this, paintHighlightsContainer));
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
        defaultFont = textComponent().getFont();
        highlightsReader = new HighlightsReader(highlightsContainer, startOffset, Integer.MAX_VALUE);
        hlLineIndex = lineIndex - 1; // Make it different for updateTabsAndHighlights()
    }

    @Override
    public int nextViewStartOffset(int offset) {
        // This layer returns a view for any given offset
        // since it must cover all the offset space with views.
        return offset;
    }

    @Override
    public EditorView createView(int startOffset, int limitOffset) {
        assert (startOffset < limitOffset) : "startOffset=" + startOffset + " >= limitOffset=" + limitOffset; // NOI18N
        // Possibly update lineEndOffset since updateHighlight() will read till it
        updateLineEndOffset(startOffset);
        updateTabsAndHighlights(startOffset);
        HighlightsList hList = highlightsReader.highlightsList();
        if (hList.startOffset() < startOffset) {
            hList.skip(startOffset);
        }
        if (startOffset == lineEndOffset - 1) {
            AttributeSet attrs = hList.cutSingleChar();
            return new NewlineView(startOffset, attrs);
        } else { // Regular view with possible highlight(s) or tab view
            if (startOffset == nextTabOffset) { // Create TabView
                int tabsEndOffset;
                for (tabsEndOffset = nextTabOffset + 1; tabsEndOffset < lineEndOffset - 1; tabsEndOffset++) {
                    if (docText.charAt(tabsEndOffset) != '\t') {
                        break;
                    }
                }
                AttributeSet attrs;
                if (limitOffset < tabsEndOffset) {
                    attrs = hList.cut(limitOffset);
                    nextTabOffset = limitOffset;
                } else {
                    attrs = hList.cut(tabsEndOffset);
                    limitOffset = tabsEndOffset;
                    for (nextTabOffset = tabsEndOffset; nextTabOffset < lineEndOffset - 1; nextTabOffset++) {
                        if (docText.charAt(nextTabOffset) == '\t') {
                            break;
                        }
                    }
                }
                return new TabView(startOffset, limitOffset - startOffset, attrs);

            } else { // Create regular view
                limitOffset = Math.min(limitOffset, Math.min(nextTabOffset, lineEndOffset - 1));
                AttributeSet attrs = hList.cutSameFont(defaultFont, limitOffset);
                int length = hList.startOffset() - startOffset;
                return createHighlightsView(startOffset, length, attrs);
            }
        }
    }

    @Override
    public int viewEndOffset(int startOffset, int limitOffset) {
        updateLineEndOffset(startOffset);
        return Math.min(lineEndOffset, limitOffset);
    }

    private EditorView createHighlightsView(int startOffset, int length, AttributeSet attrs) {
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
        // Several lines may be skipped at once in case there's e.g. a collapsed fold (FoldView gets created)
        while (offset >= lineEndOffset) {
            lineIndex++;
            Element line = lineElementRoot.getElement(lineIndex);
            lineEndOffset = line.getEndOffset();
        }
    }

    private void updateTabsAndHighlights(int offset) {
        if (hlLineIndex != lineIndex) {
            hlLineIndex = lineIndex;
            for (nextTabOffset = offset; nextTabOffset < lineEndOffset - 1; nextTabOffset++) {
                if (docText.charAt(nextTabOffset) == '\t') {
                    break;
                }
            }
            highlightsReader.readUntil(lineEndOffset);
        }
    }

    @Override
    public void finishCreation() {
        highlightsReader = null;
        docText = null;
        lineElementRoot = null;
        lineIndex = -1;
        lineEndOffset = -1;
        usageCount--;
    }

    @Override
    public void highlightChanged(HighlightsChangeEvent evt) {
        int startOffset = evt.getStartOffset();
        int endOffset = evt.getEndOffset();
        if (evt.getSource() == highlightsContainer) {
            if (usageCount != 0) { // When views are being created => notify stale creation
                notifyStaleCreation();
            }
            int docTextLength = document().getLength() + 1;
            assert (startOffset >= 0) : "startOffset=" + startOffset + " < 0"; // NOI18N
            assert (endOffset >= 0) : "startOffset=" + endOffset + " < 0"; // NOI18N
            startOffset = Math.min(startOffset, docTextLength);
            endOffset = Math.min(endOffset, docTextLength);
            if (ViewHierarchyImpl.CHANGE_LOG.isLoggable(Level.FINE)) {
                HighlightsChangeEvent layerEvent = (highlightsContainer instanceof DirectMergeContainer)
                        ? ((DirectMergeContainer) highlightsContainer).layerEvent()
                        : null;
                String layerInfo = (layerEvent != null)
                        ? " " + highlightingManager.findLayer((HighlightsContainer)layerEvent.getSource()) // NOI18N
                        : ""; // NOI18N
                ViewUtils.log(ViewHierarchyImpl.CHANGE_LOG, "VIEW-REBUILD-HC:<" + // NOI18N
                        startOffset + "," + endOffset + ">" + layerInfo + "\n"); // NOI18N
            }

            if (startOffset <= endOffset) { // May possibly be == e.g. for cut-line action
                fireEvent(Collections.singletonList(createChange(startOffset, endOffset)));
            }

        } else { // Paint highlights change
            assert (evt.getSource() == paintHighlightsContainer);
            if (ViewHierarchyImpl.CHANGE_LOG.isLoggable(Level.FINE)) {
                HighlightsChangeEvent layerEvent = (paintHighlightsContainer instanceof DirectMergeContainer)
                        ? ((DirectMergeContainer) paintHighlightsContainer).layerEvent()
                        : null;
                String layerInfo = (layerEvent != null)
                        ? " " + highlightingManager.findLayer((HighlightsContainer) layerEvent.getSource()) // NOI18N
                        : ""; // NOI18N
                ViewUtils.log(ViewHierarchyImpl.CHANGE_LOG, "REPAINT-HC:<" + // NOI18N
                        startOffset + "," + endOffset + ">" + layerInfo + "\n"); // NOI18N
            }

            offsetRepaint(startOffset, endOffset);
        }
    }

    public static final class HighlightsFactory implements EditorViewFactory.Factory {

        @Override
        public EditorViewFactory createEditorViewFactory(View documentView) {
            return new HighlightsViewFactory(documentView);
        }

        @Override
        public int importance() {
            return 0;
        }

    }

}
