/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.spi.editor.highlighting.AttributesUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

/**
 *
 * @author Vita Stejskal, Miloslav Metelka
 */
public final class ProxyHighlightsContainer extends AbstractHighlightsContainer {

    private static final Logger LOG = Logger.getLogger(ProxyHighlightsContainer.class.getName());
    
    private HighlightsContainer[] layers;
    private long version = 0;

    private final String LOCK = new String("ProxyHighlightsContainer.LOCK"); //NOI18N
    private final LayerListener listener = new LayerListener(this);

    public ProxyHighlightsContainer() {
        this(null);
    }
    
    public ProxyHighlightsContainer(HighlightsContainer[] layers) {
        setLayers(layers);
    }
    
    /**
     * Gets the list of <code>Highlight</code>s from this layer in the specified
     * area. The highlights are obtained as a merge of the highlights from all the
     * delegate layers. The following rules must hold true for the parameters
     * passed in:
     * 
     * <ul>
     * <li>0 <= <code>startOffset</code> <= <code>endOffset</code></li>
     * <li>0 <= <code>endOffset</code> <= <code>document.getLength() - 1<code></li>
     * <li>Optionally, <code>endOffset</code> can be equal to Integer.MAX_VALUE
     * in which case all available highlights will be returned.</li>
     * </ul>
     *
     * @param startOffset    The beginning of the area.
     * @param endOffset      The end of the area.
     *
     * @return The <code>Highlight</code>s in the area between <code>startOffset</code>
     * and <code>endOffset</code>.
     */
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        assert 0 <= startOffset : "offsets must be greater than or equal to zero"; //NOI18N
        assert startOffset <= endOffset : "startOffset must be less than or equal to endOffset; " + //NOI18N
            "startOffset = " + startOffset + " endOffset = " + endOffset; //NOI18N
        
        synchronized (LOCK) {
            if (layers == null || layers.length == 0 || startOffset == endOffset) {
                return HighlightsSequence.EMPTY;
            }
        
            HighlightsSequence seq [] = new HighlightsSequence[layers.length];

            for(int i = 0; i < layers.length; i++) {
                seq[i] = layers[layers.length - i - 1].getHighlights(startOffset, endOffset);
            }

            return new ProxySeq(version, seq, startOffset, endOffset);
        }
    }

    /**
     * Gets the delegate layers.
     *
     * @return The layers, which this proxy layer delegates to.
     */
    public HighlightsContainer[] getLayers() {
        synchronized (LOCK) {
            return layers;
        }
    }
    
    /**
     * Sets the delegate layers. The layers are merged in the same order in which
     * they appear in the array passed into this method. That means that the first
     * layer in the array is the less important (i.e. the bottom of the z-order) and
     * the last layer in the array is the most visible one (i.e. the top of the z-order).
     *
     * <p>If you want the layers to be merged according to their real z-order sort
     * the array first by using <code>ZOrder.sort()</code>.
     *
     * @param layers    The new delegate layers. Can be <code>null</code>.
     * @see org.netbeans.api.editor.view.ZOrder#sort(HighlightLayer [])
     */
    public void setLayers(HighlightsContainer[] layers) {
        synchronized (LOCK) {
            // Remove the listener from the current layers
            if (this.layers != null) {
                for (int i = 0; i < this.layers.length; i++) {
                    this.layers[i].removeHighlightsChangeListener(listener);
                }
            }
    
            this.layers = layers;
            this.version++;

            // Add the listener to the new layers
            if (this.layers != null) {
                for (int i = 0; i < this.layers.length; i++) {
                    this.layers[i].addHighlightsChangeListener(listener);
                }
            }
        }
        
        fireHighlightsChange(0, Integer.MAX_VALUE);
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------
    
    private void layerChanged(HighlightsContainer layer, int changeStartOffset, int changeEndOffset) {
        synchronized (LOCK) {
            version++;
        }
        
        // Fire an event
        fireHighlightsChange(changeStartOffset, changeEndOffset);
    }

    private static final class LayerListener implements HighlightsChangeListener {
        
        private WeakReference<ProxyHighlightsContainer> ref;
        
        public LayerListener(ProxyHighlightsContainer container) {
            ref = new WeakReference<ProxyHighlightsContainer>(container);
        }
        
        public void highlightChanged(HighlightsChangeEvent event) {
            ProxyHighlightsContainer container = ref.get();
            if (container != null) {
                container.layerChanged(
                    (HighlightsContainer)event.getSource(), 
                    event.getStartOffset(), 
                    event.getEndOffset());
            }
        }
    } // End of Listener class

    private final class ProxySeq implements HighlightsSequence {
        
        private Sequence2Marks [] marks;
        private int index1 = -1;
        private int index2 = -1;
        private AttributeSet compositeAttributes = null;
        private long version;
        
        public ProxySeq(long version, HighlightsSequence [] seq, int startOffset, int endOffset) {
            this.version = version;
            
            // Initialize marks
            marks = new Sequence2Marks [seq.length];
            for (int i = 0; i < seq.length; i++) {
                marks[i] = new Sequence2Marks(seq[i], startOffset, endOffset);
                marks[i].moveNext();
            }
            
            this.index2 = findLowest();
        }

        public boolean moveNext() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                checkVersion();

                do {
                    // Move to the next mark
                    index1 = index2;
                    if (index2 != -1) {
                        marks[index2].moveNext();
                        index2 = findLowest();
                    }

                    if (index1 == -1 || index2 == -1) {
                        break;
                    }

                    compositeAttributes = findAttributes();
                    
                } while (compositeAttributes == null);
                
                return index1 != -1 && index2 != -1;
            }
        }

        public int getStartOffset() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                checkVersion();
                
                if (index1 == -1 || index2 == -1) {
                    throw new NoSuchElementException();
                }

                return marks[index1].getPreviousMarkOffset();
            }
        }

        public int getEndOffset() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                checkVersion();
                
                if (index1 == -1 || index2 == -1) {
                    throw new NoSuchElementException();
                }

                return marks[index2].getMarkOffset();
            }
        }

        public AttributeSet getAttributes() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                checkVersion();
                
                if (index1 == -1 || index2 == -1) {
                    throw new NoSuchElementException();
                }

                return compositeAttributes;
            }
        }
        
        private int findLowest() {
            int lowest = Integer.MAX_VALUE;
            int idx = -1;
            
            for(int i = 0; i < marks.length; i++) {
                if (marks[i].isFinished()) {
                    continue;
                }
                
                int offset = marks[i].getMarkOffset();
                if (offset < lowest) {
                    lowest = offset;
                    idx = i;
                }
            }
            
            return idx;
        }

        private AttributeSet findAttributes() {
            ArrayList<AttributeSet> list = new ArrayList<AttributeSet>();

            for(int i = 0; i < marks.length; i++) {
                if (marks[i].getPreviousMarkAttributes() != null) {
                    list.add(marks[i].getPreviousMarkAttributes());
                }
            }

            if (!list.isEmpty()) {
                return AttributesUtilities.createComposite(list.toArray(new AttributeSet[list.size()]));
            } else {
                return null;
            }
        }
        
        private void checkVersion() {
            if (this.version != ProxyHighlightsContainer.this.version) {
                throw new ConcurrentModificationException();
            }
        }
    } // End of ProxySeq class
    
    /* package */ static final class Sequence2Marks {
        
        private HighlightsSequence seq;
        private int startOffset;
        private int endOffset;
        
        private boolean hasNext = false;
        private boolean useStartOffset = true;
        private boolean finished = true;

        private int lastEndOffset = -1;
        
        private int previousMarkOffset = -1;
        private AttributeSet previousMarkAttributes = null;
        
        public Sequence2Marks(HighlightsSequence seq, int startOffset, int endOffset) {
            this.seq = seq;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public boolean isFinished() {
            return finished;
        }
        
        public boolean moveNext() {
            if (!useStartOffset || hasNext) {
                previousMarkOffset = getMarkOffset();
                previousMarkAttributes = getMarkAttributes();
            }
            
            if (useStartOffset) {
                // Move to the next highlighted area
                while(true == (hasNext = seq.moveNext())) {
                    if (seq.getEndOffset() > startOffset) {
                        break;
                    }
                }
                
                if (hasNext && seq.getStartOffset() > endOffset) {
                    hasNext = false;
                }
                
                if (hasNext) {
                    if (lastEndOffset != -1 && lastEndOffset < seq.getStartOffset()) {
                        useStartOffset = false;
                    } else {
                        lastEndOffset = seq.getEndOffset();
                    }
                } else {
                    if (lastEndOffset != -1) {
                        useStartOffset = false;
                    }
                }
            } else {
                if (hasNext) {
                    lastEndOffset = seq.getEndOffset();
                }
                useStartOffset = true;
            }
            
            finished = useStartOffset && !hasNext;
            return !finished;
        }
        
        public int getMarkOffset() {
            if (finished) {
                throw new NoSuchElementException();
            }
            
            return useStartOffset ? 
                Math.max(startOffset, seq.getStartOffset()) : 
                Math.min(endOffset, lastEndOffset);
        }
        
        public AttributeSet getMarkAttributes() {
            if (finished) {
                throw new NoSuchElementException();
            }
            
            return useStartOffset ? seq.getAttributes() : null;
        }
        
        public int getPreviousMarkOffset() {
            return previousMarkOffset;
        }
        
        public AttributeSet getPreviousMarkAttributes() {
            return previousMarkAttributes;
        }
    } // End of Sequence2Marks class
}
