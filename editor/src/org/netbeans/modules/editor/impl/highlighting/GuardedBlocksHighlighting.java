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

package org.netbeans.modules.editor.impl.highlighting;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Vita Stejskal
 */
public final class GuardedBlocksHighlighting extends AbstractHighlightsContainer implements LookupListener {
    
    private static final Logger LOG = Logger.getLogger(GuardedBlocksHighlighting.class.getName());
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.oldlibbridge.GuardedBlocksHighlighting"; //NOI18N
    
    private final Document document;
    private final MimePath mimePath;
    private final Lookup.Result<FontColorSettings> lookupResult;

    private long version = 0;
    private AttributeSet attribs = null;
    
    /** Creates a new instance of NonLexerSytaxHighlighting */
    public GuardedBlocksHighlighting(Document document, String mimeType) {
        this.document = document;
        this.mimePath = MimePath.parse(mimeType);
        this.lookupResult = MimeLookup.getLookup(mimePath).lookup(new Lookup.Template<FontColorSettings>(FontColorSettings.class));
        this.lookupResult.addLookupListener(this);
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (document instanceof GuardedDocument) {
                MarkBlockChain guardedBlocks = ((GuardedDocument) document).getGuardedBlockChain();
                return new HSImpl(version, guardedBlocks, startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }
    }
    
    // ----------------------------------------------------------------------
    //  LookupListener implementation
    // ----------------------------------------------------------------------
    
    public void resultChanged(LookupEvent ev) {
        synchronized (this) {
            attribs = null;
            version++;
        }
        
        document.render(new Runnable() {
            public void run() {
                fireHighlightsChange(0, Integer.MAX_VALUE);
            }
        });
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private final class HSImpl implements HighlightsSequence {
        
        private final long version;
        private final MarkBlockChain guardedBlocks;
        private final int startOffset;
        private final int endOffset;

        private boolean init = false;
        private MarkBlock block;
        
        public HSImpl(long version, MarkBlockChain guardedBlocks, int startOffset, int endOffset) {
            this.version = version;
            this.guardedBlocks = guardedBlocks;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public boolean moveNext() {
            if (!init) {
                init = true;

                block = guardedBlocks.getChain();
                
                while(null != block) {
                    if (block.getEndOffset() > startOffset) {
                        break;
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Skipping block: " + block + //NOI18N
                            ", blockStart = " + block.getStartOffset() + //NOI18N
                            ", blockEnd = " + block.getEndOffset() + //NOI18N
                            ", startOffset = " + startOffset + //NOI18N
                            ", endOffset = " + endOffset //NOI18N
                        );
                    }
                    
                    block = block.getNext();
                }
            } else if (block != null) {
                block = block.getNext();
            }
            
            if (block != null && block.getStartOffset() > endOffset) {
                block = null;
            }
            
            if (LOG.isLoggable(Level.FINE)) {
                if (block != null) {
                    LOG.fine("Next block: " + block + //NOI18N
                        ", blockStart = " + block.getStartOffset() + //NOI18N
                        ", blockEnd = " + block.getEndOffset() + //NOI18N
                        ", startOffset = " + startOffset + //NOI18N
                        ", endOffset = " + endOffset //NOI18N
                    );
                } else {
                    LOG.fine("Next block: null"); //NOI18N
                }
            }
            
            return block != null;
        }

        public int getStartOffset() {
            synchronized (GuardedBlocksHighlighting.this) {
                checkVersion();
                
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                return Math.max(block.getStartOffset(), startOffset);
            }
        }

        public int getEndOffset() {
            synchronized (GuardedBlocksHighlighting.this) {
                checkVersion();
                
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                return Math.min(block.getEndOffset(), endOffset);
            }
        }

        public AttributeSet getAttributes() {
            synchronized (GuardedBlocksHighlighting.this) {
                checkVersion();
                
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                if (attribs == null) {
                    Collection<? extends FontColorSettings> allFcs = lookupResult.allInstances();
                    if (!allFcs.isEmpty()) {
                        FontColorSettings fcs = allFcs.iterator().next();
                        attribs = fcs.getFontColors(FontColorNames.GUARDED_COLORING);
                    }

                    if (attribs == null) {
                        attribs = SimpleAttributeSet.EMPTY;
                    } else {
                        attribs = AttributesUtilities.createImmutable(
                            attribs, 
                            AttributesUtilities.createImmutable(ATTR_EXTENDS_EOL, Boolean.TRUE)
                        );
                    }
                }
                
                return attribs;
            }
        }
        
        private void checkVersion() {
            if (this.version != GuardedBlocksHighlighting.this.version) {
                throw new ConcurrentModificationException();
            }
        }
    } // End of HSImpl class
    
}
