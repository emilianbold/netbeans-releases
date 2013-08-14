/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.openide.util.WeakListeners;

/**
 * Compound highlights-layer container that does non-cached direct merging
 * of individual layers' highlights.
 * <br/>
 * It's somewhat similar to a view building process in view hierarchy which also maintains
 * next-change-offset where a change in the particular layer occurs and needs to be processed.
 *
 * @author Miloslav Metelka
 */
public final class DirectMergeContainer implements HighlightsContainer, HighlightsChangeListener {
    
    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.DirectMergeContainer.level=FINE
    private static final Logger LOG = Logger.getLogger(DirectMergeContainer.class.getName());
    
    /**
     * Maximum number of empty highlights (returned from HighlightsSequence)
     * after which the particular layer will no longer be used for compound highlight sequence.
     * This is set to ensure that the whole code won't end up in an infinite loop.
     */
    static final int MAX_EMPTY_HIGHLIGHT_COUNT = 10000;

    private final HighlightsContainer[] layers;
    
    private final List<HighlightsChangeListener> listeners = new CopyOnWriteArrayList<HighlightsChangeListener>();
    
    private final List<Reference<HlSequence>> activeHlSeqs = new ArrayList<Reference<HlSequence>>();
    
    private HighlightsChangeEvent layerEvent;
    
    public DirectMergeContainer(HighlightsContainer[] layers) {
        this.layers = layers;
        for (int i = 0; i < layers.length; i++) {
            HighlightsContainer layer = layers[i];
            layer.addHighlightsChangeListener(WeakListeners.create(HighlightsChangeListener.class, this, layer));
        }
    }
    
    public HighlightsContainer[] getLayers() {
        return layers;
    }
    
    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        HlSequence hs = new HlSequence(layers, startOffset, endOffset);
        synchronized (activeHlSeqs) {
            activeHlSeqs.add(new WeakReference<HlSequence>(hs));
        }
        return hs;
    }

    @Override
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void highlightChanged(HighlightsChangeEvent event) {
        layerEvent = event;
        try {
            if (!listeners.isEmpty()) {
                event = new HighlightsChangeEvent(this, event.getStartOffset(), event.getEndOffset());
                for (HighlightsChangeListener l : listeners) {
                    l.highlightChanged(event);
                }
            }
            synchronized (activeHlSeqs) {
                for (Reference<HlSequence> hlSeqRef : activeHlSeqs) {
                    HlSequence seq = hlSeqRef.get();
                    if (seq != null) {
                        seq.notifyLayersChanged();
                    }
                }
                activeHlSeqs.clear();
            }
        } finally {
            layerEvent = null;
        }
    }
    
    /**
     * Get event from a contained layer which caused highlight change
     * (mainly for debugging purposes).
     * <br/>
     * The information is only available during firing to change listeners registered by
     * {@link #addHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)}.
     * 
     * @return event sent by a layer.
     */
    public HighlightsChangeEvent layerEvent() {
        return layerEvent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        int digitCount = ArrayUtilities.digitCount(layers.length);
        for (int i = 0; i < layers.length; i++) {
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append(layers[i]);
            sb.append('\n');
        }
        return sb.toString();
    }
    
    static final class HlSequence implements HighlightsSequenceEx {

        /**
         * Wrappers around layers used to compute merged highlights.
         */
        private final Wrapper[] wrappers;
        
        private int topWrapperIndex;
        
        private final int endOffset;
        
        int mergedHighlightStartOffset;
        
        int mergedHighlightEndOffset;
        
        AttributeSet mergedAttrs;
        
        volatile boolean finished; // Either no more highlights or layers were changed;
        
        public HlSequence(HighlightsContainer[] layers, int startOffset, int endOffset) {
            this.endOffset = endOffset;
            // Initially set an empty highlight (the values are undefined anyway)
            this.mergedHighlightStartOffset = startOffset;
            this.mergedHighlightEndOffset = startOffset;
            wrappers = new Wrapper[layers.length];
            for (int i = 0; i < layers.length; i++) {
                HighlightsContainer container = layers[i];
                HighlightsSequence hlSequence = container.getHighlights(startOffset, endOffset);
                Wrapper wrapper = new Wrapper(container, hlSequence, startOffset);
                if (!wrapper.isFinished()) { // For no-highlight wrapper do not include it at all in the array
                    wrappers[topWrapperIndex++] = wrapper;
                }
            }
            topWrapperIndex--;
            updateMergeVars(-1, startOffset); // Update all layers to fetch correct values
        }
        
        @Override
        public boolean moveNext() {
            if (finished || topWrapperIndex < 0) {
                return false;
            }
            Wrapper topWrapper;
            int lastHighlightEndOffset = mergedHighlightEndOffset;
            while ((topWrapper = nextMerge(lastHighlightEndOffset)) != null) {
                int nextChangeOffset = topWrapper.mNextChangeOffset;
                if (nextChangeOffset <= lastHighlightEndOffset) { // No advance in change offset => Finished
                    finished = true;
                    return false;
                }
                AttributeSet attrs = topWrapper.mAttrs;
                if (attrs != null) {
                    mergedHighlightStartOffset = lastHighlightEndOffset;
                    mergedHighlightEndOffset = nextChangeOffset;
                    mergedAttrs = attrs;
                    return true;
                }
                lastHighlightEndOffset = nextChangeOffset;
            }
            return false;
        }

        @Override
        public int getStartOffset() {
            return mergedHighlightStartOffset;
        }

        @Override
        public int getEndOffset() {
            return mergedHighlightEndOffset;
        }

        @Override
        public AttributeSet getAttributes() {
            return mergedAttrs;
        }

        @Override
        public boolean isStale() {
            return finished;
        }
        
        void notifyLayersChanged() { // Notify that layers were changed => stop iteration
            finished = true;
        }

        /**
         * Do merge above the given offset.
         *
         * @param offset end of last merged highlight.
         * @return top wrapper containing info about the performed merge or null
         *  if there is zero wrappers.
         */
        Wrapper nextMerge(int offset) {
            int i = topWrapperIndex;
            for (; i >= 0 && wrappers[i].mNextChangeOffset <= offset; i--) { }
            // i contains first layer which has mNextChangeOffset > offset
            return updateMergeVars(i, offset);
        }
        
        /**
         * Update merged vars of wrappers at (startIndex+1) and above.
         *
         * @param startIndex index of first wrapper which has mNextChangeOffset &lt; offset.
         *  All wrappers above it will have their mNextChangeOffset and mAttrs updated.
         * @param offset.
         * @return mNextChangeOffset of the top wrapper.
         */
        Wrapper updateMergeVars(int startIndex, int offset) {
            Wrapper wrapper = null;
            int nextChangeOffset;
            AttributeSet lastAttrs;
            if (startIndex < 0) { // No valid layers
                nextChangeOffset = endOffset;
                lastAttrs = null;
            } else {
                wrapper = wrappers[startIndex];
                nextChangeOffset = wrapper.mNextChangeOffset;
                lastAttrs = wrapper.mAttrs;
            }
            startIndex++; // Move to first wrapper that needs to be updated
            for (; startIndex <= topWrapperIndex; startIndex++) {
                wrapper = wrappers[startIndex];
                if (wrapper.nextChangeOffset <= offset) {
                    if (wrapper.updateCurrentState(offset)) { // Requires next highlight fetch
                        if (!wrapper.fetchNextHighlight(offset)) { // Finished all highlights in sequence
                            removeWrapper(startIndex); // Remove this wrapper
                            // Ensure that the wrapper returned from method is correct after removeWrapper()
                            // topWrapperIndex already decreased by removeWrapper()
                            startIndex--; // Compensate for addition in for(;;)
                            if (startIndex == topWrapperIndex) { // Would be no more iterations
                                // Previous wrapper or null
                                wrapper = (startIndex >= 0) ? wrappers[startIndex] : null;
                            }
                            continue; // Use next wrapper (now at same index i)
                        }
                        wrapper.updateCurrentState(offset); // Update state to just fetched highlight
                    }
                }
                if (wrapper.nextChangeOffset < nextChangeOffset) {
                    nextChangeOffset = wrapper.nextChangeOffset;
                }
                wrapper.mNextChangeOffset = nextChangeOffset;
                lastAttrs = (lastAttrs != null)
                        ? ((wrapper.currentAttrs != null)
                            ? AttributesUtilities.createComposite(wrapper.currentAttrs, lastAttrs) // first prior second
                            : lastAttrs)
                        : wrapper.currentAttrs;
                wrapper.mAttrs = lastAttrs;
            }
            return wrapper;
        }
        
        private void removeWrapper(int index) {
            System.arraycopy(wrappers, index + 1, wrappers, index, topWrapperIndex - index);
            wrappers[topWrapperIndex] = null;
            topWrapperIndex--;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append("endO=").append(endOffset);
            if (finished) {
                sb.append("; FINISHED");
            }
            sb.append('\n');
            int digitCount = ArrayUtilities.digitCount(topWrapperIndex + 1);
            for (int i = 0; i <= topWrapperIndex; i++) {
                sb.append("  ");
                ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
                sb.append(wrappers[i]);
                sb.append('\n');
            }
            return sb.toString();
        }

    }


    static final class Wrapper {

        /**
         * Layer over which hlSequence is constructed (for debugging purposes).
         */
        final HighlightsContainer layer;
        
        /**
         * Highlights sequence for layer corresponding to this wrapper.
         */
        final HighlightsSequence hlSequence;
        
        /**
         * Start offset of the last fetched highlight.
         */
        int hlStartOffset;
        
        /**
         * End offset of the last fetched highlight.
         */
        int hlEndOffset;
        
        /**
         * Attributes of the last fetched highlight.
         */
        AttributeSet hlAttrs;
        
        /**
         * Offset where a change in highlighting for the current layer will occur.
         * If an offset is below hlStartOffset then the value is hlStartOffset.
         * Otherwise it will be hlEndOffset.
         */
        int nextChangeOffset;
        
        /**
         * Attributes for an offset: when before hlStartOffset it's null.
         * Otherwise it's hlAttrs.
         */
        AttributeSet currentAttrs;
        
        /**
         * Merged next change offset: minimum of nextChangeOffset from all
         * wrappers below this one in the wrappers array.
         */
        int mNextChangeOffset;
        
        /**
         * Merged attributes: merge of currentAttrs from all
         * wrappers below this one in the wrappers array.
         */
        AttributeSet mAttrs;
        
        private int emptyHighlightCount;
        
        
        public Wrapper(HighlightsContainer layer, HighlightsSequence hlSequence, int startOffset) {
            this.layer = layer;
            this.hlSequence = hlSequence;
            fetchNextHighlight(startOffset);
            updateCurrentState(startOffset);
            this.mNextChangeOffset = startOffset; // Will cause recomputation
        }
        
        boolean isFinished() { // Whether no more highlights from 
            return (hlStartOffset == Integer.MAX_VALUE);
        }
        
        /**
         * Update currentAttrs and nextChangeOffset according to given offset.
         * @param offset offset to which to update
         * @return true if the offset is >= hlEndOffset and so fetchNextHighlight() is necessary.
         */
        boolean updateCurrentState(int offset) {
            if (offset < hlStartOffset) { // before hl start
                currentAttrs = null;
                nextChangeOffset = hlStartOffset;
                return false;
            } else if (offset < hlEndOffset) { // inside hl (assuming call after fetchNextHighlight())
                currentAttrs = hlAttrs;
                nextChangeOffset = hlEndOffset;
                return false;
            } // else: offset >= hlEndOffset
            return true;
        }
        
        /**
         * Fetch a next highlight for this wrapper.
         * @param offset
         * @return true if highlight fetched successfully or false if there are no more highlights.
         */
        boolean fetchNextHighlight(int offset) {
            assert (hlStartOffset != Integer.MAX_VALUE);
            do {
                if (hlSequence.moveNext()) {
                    hlStartOffset = hlSequence.getStartOffset();
                    if (hlStartOffset < hlEndOffset) { // Invalid layer: next highlight overlaps previous one
                        // To prevent infinite loops finish this HL
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Disabled an invalid highlighting layer: hlStartOffset=" + hlStartOffset + // NOI18N
                                " < previous hlEndOffset=" + hlEndOffset + " for layer=" + layer); // NOI18N
                        }
                        hlStartOffset = hlEndOffset = Integer.MAX_VALUE;
                        return false;
                    }
                    hlEndOffset = hlSequence.getEndOffset();
                    if (hlEndOffset <= hlStartOffset) {
                        if (hlEndOffset < hlStartOffset) { // Invalid highlight: end offset before start offset
                            // To prevent infinite loops finish this HL
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Disabled an invalid highlighting layer: hlStartOffset=" + hlStartOffset + // NOI18N
                                    " > hlEndOffset=" + hlEndOffset + " for layer=" + layer); // NOI18N
                            }
                            hlStartOffset = hlEndOffset = Integer.MAX_VALUE;
                            return false;
                        }
                        emptyHighlightCount++;
                        if (emptyHighlightCount >= MAX_EMPTY_HIGHLIGHT_COUNT) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Disabled an invalid highlighting layer: too many empty highlights=" + emptyHighlightCount); // NOI18N
                            }
                            hlStartOffset = hlEndOffset = Integer.MAX_VALUE;
                            return false;
                        }
                    }
                    hlAttrs = hlSequence.getAttributes();
                } else {
                    hlStartOffset = hlEndOffset = Integer.MAX_VALUE; // Signal that sequence is finished
                    return false;
                }
            } while (hlEndOffset <= offset);
            return true; // Valid highlight fetched
        }

        @Override
        public String toString() {
            return  "M[" + mNextChangeOffset + ",A=" + mAttrs + // NOI18N
                    "]  Next[" + nextChangeOffset + ",A=" + currentAttrs + // NOI18N
                    "]  HL:<" + hlStartOffset + "," + hlEndOffset + ">,A=" + hlAttrs; // NOI18N
        }

    }
    
}
