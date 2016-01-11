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
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.netbeans.spi.editor.highlighting.ShiftHighlightsSequence;
import org.openide.util.WeakListeners;

/**
 * Compound highlights-layer container that does non-cached direct merging
 * of individual layers' highlights.
 * <br/>
 * It's somewhat similar to a view building process in view hierarchy which also maintains
 * next-change-offset where a change in the particular layer occurs and needs to be processed.
 * <br/>
 * {@link ShiftHighlightsSequence} are supported and the highlights sequences returned by the container
 * are always instances of this interface.
 *
 * @author Miloslav Metelka
 */
public final class DirectMergeContainer implements HighlightsContainer, HighlightsChangeListener, ReleasableHighlightsContainer {
    
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
                HighlightsChangeEvent thisEvt = new HighlightsChangeEvent(this, event.getStartOffset(), event.getEndOffset());
                for (HighlightsChangeListener l : listeners) {
                    l.highlightChanged(thisEvt);
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

    @Override
    public void released() {
        for (HighlightsContainer layer : layers) {
            if (layer instanceof ReleasableHighlightsContainer) {
                ((ReleasableHighlightsContainer) layer).released();
            }
        }
    }
    
    static final class HlSequence implements ShiftHighlightsSequence {

        /**
         * Wrappers around layers used to compute merged highlights.
         */
        private final Wrapper[] wrappers;
        
        private int topWrapperIndex;
        
        private final int endOffset;
        
        int mergedHighlightStartOffset;
        
        int mergedHighlightStartShift;
        
        int mergedHighlightEndOffset;
        
        int mergedHighlightEndShift;
        
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
                if (wrapper.init(startOffset)) { // For no-highlight wrapper do not include it at all in the array
                    wrappers[topWrapperIndex++] = wrapper;
                }
            }
            topWrapperIndex--;
            updateMergeVars(-1, startOffset, 0); // Update all layers to fetch correct values
        }
        
        @Override
        public boolean moveNext() {
            if (finished) {
                return false;
            }
            Wrapper topWrapper;
            int nextHighlightStartOffset = mergedHighlightEndOffset;
            int nextHighlightStartShift = mergedHighlightEndShift;
            while ((topWrapper = nextMerge(nextHighlightStartOffset, nextHighlightStartShift)) != null) {
                int nextChangeOffset = topWrapper.mergedNextChangeOffset;
                int nextChangeShift = topWrapper.mergedNextChangeShift;
                AttributeSet attrs = topWrapper.mAttrs;
                if (attrs != null) { // Do not return regions with empty attrs (they are not highlights)
                    mergedHighlightStartOffset = nextHighlightStartOffset;
                    mergedHighlightStartShift = nextHighlightStartShift;
                    mergedHighlightEndOffset = nextChangeOffset;
                    mergedHighlightEndShift = nextChangeShift;
                    mergedAttrs = attrs;
                    return true;
                }
                nextHighlightStartOffset = nextChangeOffset;
                nextHighlightStartShift = nextChangeShift;
            }
            finished = true;
            return false;
        }

        @Override
        public int getStartOffset() {
            return mergedHighlightStartOffset;
        }

        @Override
        public int getStartShift() {
            return mergedHighlightStartShift;
        }
        
        @Override
        public int getEndOffset() {
            return mergedHighlightEndOffset;
        }

        @Override
        public int getEndShift() {
            return mergedHighlightEndShift;
        }
        
        @Override
        public AttributeSet getAttributes() {
            return mergedAttrs;
        }

        void notifyLayersChanged() { // Notify that layers were changed => stop iteration
            finished = true;
        }

        /**
         * Do merge above the given offset.
         *
         * @param offset end of last merged highlight.
         * @param shift end shift of last merged highlight (accompanying the offset).
         * @return top wrapper containing info about the performed merge or null
         *  if there is zero wrappers.
         */
        Wrapper nextMerge(int offset, int shift) {
            int i = topWrapperIndex;
            for (; i >= 0 && wrappers[i].isMergedNextChangeBelowOrAt(offset, shift); i--) { }
            // i contains first layer which has mergedNextChangeOffset > offset
            return updateMergeVars(i, offset, shift);
        }
        
        /**
         * Update merged vars of wrappers at (startIndex+1) and above.
         *
         * @param startIndex index of first wrapper which has mergedNextChangeOffset above given offset (and shift)
         *  or -1 if all wrappers need to be updated.
         *  All wrappers above this index will have their mergedNextChangeOffset and mAttrs updated.
         * @param offset current offset at which to update.
         * @param shift current shift "within" the char at offset.
         * @return top wrapper (wrapper at topWrapperIndex).
         */
        Wrapper updateMergeVars(int startIndex, int offset, int shift) {
            Wrapper wrapper = null;
            int nextChangeOffset;
            int nextChangeShift;
            AttributeSet lastAttrs;
            if (startIndex < 0) { // No valid layers
                nextChangeOffset = endOffset;
                nextChangeShift = 0;
                lastAttrs = null;
            } else {
                wrapper = wrappers[startIndex];
                nextChangeOffset = wrapper.mergedNextChangeOffset;
                nextChangeShift = wrapper.mergedNextChangeShift;
                lastAttrs = wrapper.mAttrs;
            }
            // Start with first wrapper that needs to be updated
            wrapperIteration:
            for (int i = startIndex + 1; i <= topWrapperIndex; i++) {
                wrapper = wrappers[i];
                if (wrapper.isNextChangeBelowOrAt(offset, shift)) {
                    while (wrapper.updateCurrentState(offset, shift)) { // Check if next highlight fetch is necessary
                        if (!wrapper.fetchNextHighlight()) { // Finished all highlights in sequence
                            removeWrapper(i); // Remove this wrapper (does topWrapperIndex--)
                            // Ensure that the wrapper returned from method is correct after removeWrapper()
                            // topWrapperIndex already decreased by removeWrapper()
                            i--; // Compensate wrapper removal in for(;;)
                            if (i == topWrapperIndex) {
                                // Since "wrapper" variable should return wrapper at current topWrapperIndex
                                // that in this particular case was just removed
                                // then assign current top wrapper explicitly.
                                wrapper = (i >= 0) ? wrappers[i] : null;
                                break wrapperIteration;
                            }
                            continue wrapperIteration;
                        }
                    }
                }
                if (wrapper.isNextChangeBelow(nextChangeOffset, nextChangeShift)) {
                    nextChangeOffset = wrapper.nextChangeOffset;
                    nextChangeShift = wrapper.nextChangeShift;
                }
                wrapper.mergedNextChangeOffset = nextChangeOffset;
                wrapper.mergedNextChangeShift = nextChangeShift;
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
         * Highlights sequence supporting coloring of characters inside tabs or newlines.
         */
        final ShiftHighlightsSequence shiftHLSequence;
        
        /**
         * Start offset of the last fetched highlight.
         */
        int hlStartOffset;
        
        /**
         * Possible shift accompanying hlStartOffset or zero for non-ShiftHighlightsSequence.
         */
        int hlStartShift;
        
        /**
         * End offset of the last fetched highlight.
         */
        int hlEndOffset;
        
        /**
         * Possible shift accompanying hlEndOffset or zero for non-ShiftHighlightsSequence.
         */
        int hlEndShift;

        /**
         * Attributes of the last fetched highlight.
         */
        AttributeSet hlAttrs;
        
        /**
         * Offset where a change in highlighting for the current layer will occur.
         * If currently processed offset is below current highlight (fetched into hlStartOffset and hlEndOffset)
         * then the value is set to hlStartOffset.
         * For an offset inside current highlight the value will be set to hlEndOffset.
         * Offset above hlEndOffset will trigger a next highlight fetching.
         */
        int nextChangeOffset;
        
        /**
         * If shiftHLSequence != null then (similarly to nextChangeOffset)
         * this is set either to start shift of the next highlight
         * or (if the offset and its shift are inside current highlight) then
         * this variable is set to end shift of the current highlight
         * or a next highlight will be fetched (if current offset and shift are above the highlight).
         */
        int nextChangeShift;
        
        /**
         * Attributes for an offset: when before hlStartOffset it's null.
         * Otherwise it's hlAttrs.
         */
        AttributeSet currentAttrs;
        
        /**
         * Merged next change offset: minimum of nextChangeOffset from all
         * wrappers below this one in the wrappers array.
         */
        int mergedNextChangeOffset;
        
        /**
         * Merged next change shift - possible shift accompanying mergedNextChangeOffset or zero.
         */
        int mergedNextChangeShift;
        
        /**
         * Merged attributes: merge of currentAttrs from all
         * wrappers below this one in the wrappers array.
         */
        AttributeSet mAttrs;
        
        private int emptyHighlightCount;
        
        
        public Wrapper(HighlightsContainer layer, HighlightsSequence hlSequence, int startOffset) {
            this.layer = layer;
            this.hlSequence = hlSequence;
            this.shiftHLSequence = (hlSequence instanceof ShiftHighlightsSequence) ? (ShiftHighlightsSequence) hlSequence : null;
        }
        
        boolean init(int startOffset) {
            do {
                if (!fetchNextHighlight()) {
                    return false;
                }
            } while (hlEndOffset <= startOffset); // Exclude any possible highlights ending below startOffset
            updateCurrentState(startOffset, 0);
            return true;
        }
        
        /**
         * Whether next change offset and shift of this wrapper are below the given parameters.
         *
         * @param offset current offset.
         * @param shift current shift (accompanying the current offset).
         * @return true if next change offset and shift of this wrapper are below the given parameters
         *  or false otherwise.
         */
        boolean isNextChangeBelow(int offset, int shift) {
            return nextChangeOffset < offset || (nextChangeOffset == offset && nextChangeShift < shift);
        }

        /**
         * Whether next change offset and shift of this wrapper are below the given parameters
         * or right at them.
         *
         * @param offset current offset.
         * @param shift current shift (accompanying the current offset).
         * @return true if next change offset and shift of this wrapper are below or right at the given parameters
         *  or false otherwise.
         */
        boolean isNextChangeBelowOrAt(int offset, int shift) {
            return nextChangeOffset < offset || (nextChangeOffset == offset && nextChangeShift <= shift);
        }

        /**
         * Whether merged next change offset and shift of this wrapper are below the given parameters
         * or right at them.
         *
         * @param offset current offset.
         * @param shift current shift (accompanying the current offset).
         * @return true if next change offset and shift of this wrapper are below or right at the given parameters
         *  or false otherwise.
         */
        boolean isMergedNextChangeBelowOrAt(int offset, int shift) {
            return mergedNextChangeOffset < offset || (mergedNextChangeOffset == offset && mergedNextChangeShift <= shift);
        }

        /**
         * Update currentAttrs and nextChangeOffset according to given offset.
         * @param offset offset to which to update
         * @param shift shift inside tab or newline character on the given offset.
         * @return true if the offset is >= hlEndOffset and so fetchNextHighlight() is necessary.
         */
        boolean updateCurrentState(int offset, int shift) {
            if (offset < hlStartOffset) { // offset before current hl start
                currentAttrs = null;
                nextChangeOffset = hlStartOffset;
                nextChangeShift = hlStartShift;
                return false;
            } else if (offset == hlStartOffset) { // inside hl (assuming call after fetchNextHighlight())
                if (shift < hlStartShift) {
                    currentAttrs = null;
                    nextChangeOffset = hlStartOffset;
                    nextChangeShift = hlStartShift;
                    return false;
                    
                } else { // Above (or at) highlight's start
                    if (offset < hlEndOffset || (offset == hlEndOffset && shift < hlEndShift)) {
                        currentAttrs = hlAttrs;
                        nextChangeOffset = hlEndOffset;
                        nextChangeShift = hlEndShift;
                        return false;
                    } else {
                        return true; // Fetch next highlight
                    }
                } // else: fetch next highlight
            } else if (offset < hlEndOffset || (offset == hlEndOffset && shift < hlEndShift)) {
                currentAttrs = hlAttrs;
                nextChangeOffset = hlEndOffset;
                nextChangeShift = hlEndShift;
                return false;
            } else { // Above hlEndOffset (or hlEndShift) => fetch next highlight
                return true; // Fetch next highlight
            }
        }
        
        /**
         * Fetch a next highlight for this wrapper.
         * @param offset
         * @return true if highlight fetched successfully or false if there are no more highlights.
         */
        boolean fetchNextHighlight() {
            if (hlSequence.moveNext()) {
                hlStartOffset = hlSequence.getStartOffset();
                if (hlStartOffset < hlEndOffset) { // Invalid layer: next highlight overlaps previous one
                    // To prevent infinite loops finish this HL
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Disabled an invalid highlighting layer: hlStartOffset=" + hlStartOffset + // NOI18N
                            " < previous hlEndOffset=" + hlEndOffset + " for layer=" + layer); // NOI18N
                    }
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
                        return false;
                    }
                    emptyHighlightCount++;
                    if (emptyHighlightCount >= MAX_EMPTY_HIGHLIGHT_COUNT) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Disabled an invalid highlighting layer: too many empty highlights=" + emptyHighlightCount); // NOI18N
                        }
                        return false;
                    }
                }
                if (shiftHLSequence != null) {
                    hlStartShift = shiftHLSequence.getStartShift();
                    hlEndShift = shiftHLSequence.getEndShift();
                    // Do not perform extra checking of validity (non-overlapping with previous highlight
                    //  and validity of shifts since it should not be crucial
                    //  for proper functioning of updateCurrentState() method.
                } // else hlStartShift and hlEndShift are always zero in the wrapper
                hlAttrs = hlSequence.getAttributes();
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.fine("Fetched highlight: <" + hlStartOffset + // NOI18N
                            "," + hlEndOffset + "> for layer=" + layer + '\n'); // NOI18N
                }
            } else {
                return false;
            }
            return true; // Valid highlight fetched
        }

        @Override
        public String toString() {
            return  "M[" + mergedNextChangeOffset + ",A=" + mAttrs + // NOI18N
                    "]  Next[" + nextChangeOffset + ",A=" + currentAttrs + // NOI18N
                    "]  HL:<" + hlStartOffset + "," + hlEndOffset + ">,A=" + hlAttrs; // NOI18N
        }

    }
    
}
