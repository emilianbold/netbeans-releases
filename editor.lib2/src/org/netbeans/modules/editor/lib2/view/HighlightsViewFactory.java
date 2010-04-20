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
import org.openide.util.RequestProcessor;
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

    // -J-Dorg.netbeans.modules.editor.lib2.view.HighlightsViewFactory.stack=true
    private static final boolean dumpHighlightChangeStack =
            Boolean.getBoolean(HighlightsViewFactory.class.getName() + ".stack");

    private static final RequestProcessor RP = 
            new RequestProcessor("Highlights-Coalescing", 1, false, false); // NOI18N

    private static final boolean SYNC_HIGHLIGHTS = 
            Boolean.getBoolean("org.netbeans.editor.sync.highlights"); //NOI18N

    private final HighlightsContainer highlightsContainer;

    private CharSequence docText;
    private Element lineElementRoot;

    private int lineIndex;
    private int newLineOffset;

    private HighlightsSequence highlightsSequence;
    private int highlightStartOffset;
    private int highlightEndOffset;
    private AttributeSet highlightAttributes;

    private final Object dirtyRegionLock = new String("dirty-region-lock"); //NOI18N
    private int dirtyReqionStartOffset = Integer.MAX_VALUE;
    private int dirtyReqionEndOffset = Integer.MIN_VALUE;

    private final RequestProcessor.Task dirtyRegionTask = RP.create(new Runnable() {
        private boolean insideRender = false;
        public @Override void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                if (insideRender) {
                    int[] region = getAndClearDirtyRegion();
                    if (region != null) {
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.fine("coallesced-event: <" + region[0] + ',' + region[1] + ">\n"); // NOI18N
                        }
                        fireEvent(Collections.singletonList(createChange(region[0], region[1])));
                    }
                } else {
                    insideRender = true;
                    try {
                        Document doc = textComponent().getDocument();
                        doc.render(this);
                    } finally {
                        insideRender = false;
                    }
                }
            } else {
                try {
                    SwingUtilities.invokeAndWait(this);
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
        }
    });

    public HighlightsViewFactory(JTextComponent component) {
        super(component);

        highlightsContainer = HighlightingManager.getInstance().getHighlights(component, null);
        highlightsContainer.addHighlightsChangeListener(WeakListeners.create(HighlightsChangeListener.class, this, highlightsContainer));
    }

    @Override
    public void restart(int startOffset, int matchOffset) {
        Document doc = textComponent().getDocument();
        docText = DocumentUtilities.getText(doc);

        lineElementRoot = doc.getDefaultRootElement();
        lineIndex = lineElementRoot.getElementIndex(startOffset);
        newLineOffset = lineElementRoot.getElement(lineIndex).getEndOffset() - 1;

        highlightsSequence = highlightsContainer.getHighlights(startOffset, Integer.MAX_VALUE);
        fetchNextHighlight(startOffset);
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
        updateNewLineOffset(startOffset);
        if (startOffset == newLineOffset) {
            return new NewlineView(startOffset, (startOffset >= highlightStartOffset) &&
                    (startOffset + 1 <= highlightEndOffset) ? highlightAttributes : null);
        } else if (startOffset < highlightStartOffset) { // Before highlight
            int endOffset = Math.min(Math.min(highlightStartOffset, limitOffset), newLineOffset);
            return createHighlightsView(startOffset, endOffset - startOffset, null);
        } else { // Inside highlight
            int endOffset = Math.min(Math.min(highlightEndOffset, limitOffset), newLineOffset);
            return createHighlightsView(startOffset, endOffset - startOffset, highlightAttributes);
        }
    }

    private EditorView createHighlightsView(int startOffset, int length, AttributeSet attrs) {
        if (length <= 0) {
            throw new IllegalStateException("startOffset=" + startOffset // NOI18N
                    + ", length=" + length + ", highlight: <" + highlightStartOffset // NOI18N
                    + "," + highlightEndOffset // NOI18N
                    + ">, newLineOffset=" + newLineOffset + ", docText.length()=" + docText.length()); // NOI18N
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

    private void updateHighlight(int offset) {
        while (highlightEndOffset <= offset) {
            fetchNextHighlight(offset);
        }
    }

    private void updateNewLineOffset(int offset) {
        while (newLineOffset < offset && lineIndex + 1 < lineElementRoot.getElementCount()) {
            lineIndex++;
            newLineOffset = lineElementRoot.getElement(lineIndex).getEndOffset() - 1;
        }
    }

    private void fetchNextHighlight(int offset) {
        while (highlightsSequence != null) {
            while (highlightsSequence instanceof HighlightsSequenceEx && ((HighlightsSequenceEx) highlightsSequence).isStale()) {
                highlightsSequence = highlightsContainer.getHighlights(offset, Integer.MAX_VALUE);
            }

            if (highlightsSequence.moveNext()) {
                highlightStartOffset = highlightsSequence.getStartOffset();
                highlightEndOffset = highlightsSequence.getEndOffset();
                highlightAttributes = highlightsSequence.getAttributes();
                offset = highlightEndOffset;

                if (highlightStartOffset < highlightEndOffset) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.fine("Highlight: <" + highlightStartOffset + "," + highlightEndOffset + "> " // NOI18N
                            + ViewUtils.toString(highlightAttributes) + "\n"); //NOI18N
                    }
                    // great, we have a proper highlight now
                    break;
                }
            } else {
                highlightsSequence = null;
                highlightAttributes = null;
                highlightStartOffset = Integer.MAX_VALUE;
                highlightEndOffset = Integer.MAX_VALUE;
            }
        }
    }

    @Override
    public void finish() {
        docText = null;
        lineElementRoot = null;
        lineIndex = -1;
        newLineOffset = -1;
        highlightsSequence = null;
        highlightStartOffset = Integer.MAX_VALUE;
        highlightEndOffset = Integer.MAX_VALUE;
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
        final int startOffset = event.getStartOffset();
        final int endOffset = event.getEndOffset();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINER, "highlightChanged: event:<{0}{1}{2}>, thread:{3}\n", //NOI18N
                    new Object[] {startOffset, ',', endOffset, Thread.currentThread()}); // NOI18N
            if (dumpHighlightChangeStack) {
                LOG.log(Level.INFO, "Highlight Change Thread Dump for <" + // NOI18N
                        startOffset + "," + endOffset + ">", new Exception()); // NOI18N
            }
        }

        if (endOffset > startOffset) { // May possibly be == e.g. for cut-line action
            if (SYNC_HIGHLIGHTS) {
                // firing directly is ok, because highlightChanged events are delivered under the document read-lock
                Runnable r = new Runnable() {
                    public @Override void run() {
                        fireEvent(Collections.singletonList(createChange(startOffset, endOffset)));
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    r.run();
                } else {
                    SwingUtilities.invokeLater(r);
                }
            } else {
                // Coalesce highglihts events by reposting to RP and then to EDT
                extendDirtyRegion(startOffset, endOffset);
                dirtyRegionTask.schedule(0);
            }
        }
    }

    private void extendDirtyRegion(int startOffset, int endOffset) {
        synchronized (dirtyRegionLock) {
            dirtyReqionStartOffset = Math.min(dirtyReqionStartOffset, startOffset);
            dirtyReqionEndOffset = Math.max(dirtyReqionEndOffset, endOffset);
        }
    }
    
    private int[] getAndClearDirtyRegion() {
        synchronized (dirtyRegionLock) {
            if (dirtyReqionStartOffset == Integer.MAX_VALUE || dirtyReqionEndOffset == Integer.MIN_VALUE) {
                return null;
            } else {
                int [] region = new int[] { dirtyReqionStartOffset, dirtyReqionEndOffset };
                dirtyReqionStartOffset = Integer.MAX_VALUE;
                dirtyReqionEndOffset = Integer.MIN_VALUE;
                return region;
            }
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
