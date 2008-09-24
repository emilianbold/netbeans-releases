/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.editor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.View;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.editor.ext.ExtCaret;
import org.netbeans.editor.view.spi.LockView;
import org.netbeans.modules.editor.lib2.highlighting.CaretBasedBlockHighlighting.CaretRowHighlighting;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingSpiPackageAccessor;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsLayerAccessor;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsLayerFilter;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.openide.util.WeakListeners;

/**
 *
 * @author vita
 */
/* package */ final class HighlightingDrawLayer extends DrawLayer.AbstractLayer 
    implements HighlightsChangeListener, AtomicLockListener
{
    // -J-Dorg.netbeans.editor.HighlightingDrawLayer.level=FINE
    private static final Logger LOG = Logger.getLogger(HighlightingDrawLayer.class.getName());

    // Above CaretRowHighlighting.LAYER_TYPE_ID
    private static final String LAYER_A_NAME = "org-netbeans-lib-editor-nview-HighlightingDrawLayer/A"; //NOI18N
    // above ZOrder.SYNTAX_RACK and below (including) CaretRowHighlighting.LAYER_TYPE_ID
    // Using the original name for the caret row highlighting, some clients use it to remove the layer.
    private static final String LAYER_B_NAME = ExtCaret.HIGHLIGHT_ROW_LAYER_NAME; 
    // Only ZOrder.SYNTAX_RACK
    private static final String LAYER_C_NAME = "org-netbeans-lib-editor-nview-HighlightingDrawLayer/C"; //NOI18N
    // ZOrder.BOTTOM_RACK
    private static final String LAYER_D_NAME = "org-netbeans-lib-editor-nview-HighlightingDrawLayer/D"; //NOI18N
    
    // Above CaretRowHighlighting.LAYER_TYPE_ID
    private static final HighlightsLayerFilter FILTER_A = new HighlightsLayerFilter() {
        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            ArrayList<HighlightsLayer> filteredLayers = new ArrayList<HighlightsLayer>();
            boolean add = false;
            
            for(HighlightsLayer layer : layers) {
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                
                if (CaretRowHighlighting.LAYER_TYPE_ID.equals(layerAccessor.getLayerTypeId())) {
                    add = true;
                    continue;
                }
                
                if (add) {
                    filteredLayers.add(layer);
                }
            }

            return filteredLayers;
        }
    }; // End of FILTER_A constant
    
    // above ZOrder.SYNTAX_RACK and below (including) CaretRowHighlighting.LAYER_TYPE_ID
    private static final HighlightsLayerFilter FILTER_B = new HighlightsLayerFilter() {
        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            ArrayList<HighlightsLayer> filteredLayers = new ArrayList<HighlightsLayer>();
            
            int syntaxRack = HighlightingSpiPackageAccessor.get().getZOrderRack(ZOrder.SYNTAX_RACK);
            for(HighlightsLayer layer : layers) {
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                
                if (HighlightingSpiPackageAccessor.get().getZOrderRack(layerAccessor.getZOrder()) > syntaxRack) {
                    filteredLayers.add(layer);
                }
                
                if (CaretRowHighlighting.LAYER_TYPE_ID.equals(layerAccessor.getLayerTypeId())) {
                    break;
                }
            }

            return filteredLayers;
        }
    }; // End of FILTER_B constant
    
    // Only ZOrder.SYNTAX_RACK
    private static final HighlightsLayerFilter FILTER_C = new HighlightsLayerFilter() {
        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            ArrayList<HighlightsLayer> filteredLayers = new ArrayList<HighlightsLayer>();
            
            int syntaxRack = HighlightingSpiPackageAccessor.get().getZOrderRack(ZOrder.SYNTAX_RACK);
            for(HighlightsLayer layer : layers) {
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                
                if (HighlightingSpiPackageAccessor.get().getZOrderRack(layerAccessor.getZOrder()) < syntaxRack) {
                    continue;
                } else if (HighlightingSpiPackageAccessor.get().getZOrderRack(layerAccessor.getZOrder()) == syntaxRack) {
                    filteredLayers.add(layer);
                } else {
                    break;
                }
                
            }

            return filteredLayers;
        }
    }; // End of FILTER_C constant

    // ZOrder.BOTTOM_RACK
    private static final HighlightsLayerFilter FILTER_D = new HighlightsLayerFilter() {
        public List<? extends HighlightsLayer> filterLayers(List<? extends HighlightsLayer> layers) {
            ArrayList<HighlightsLayer> filteredLayers = new ArrayList<HighlightsLayer>();
            
            int syntaxRack = HighlightingSpiPackageAccessor.get().getZOrderRack(ZOrder.SYNTAX_RACK);
            for(HighlightsLayer layer : layers) {
                HighlightsLayerAccessor layerAccessor = 
                    HighlightingSpiPackageAccessor.get().getHighlightsLayerAccessor(layer);
                
                if (HighlightingSpiPackageAccessor.get().getZOrderRack(layerAccessor.getZOrder()) == syntaxRack) {
                    break;
                }
                
                filteredLayers.add(layer);
            }

            return filteredLayers;
        }
    }; // End of FILTER_D constant
    
    public static void hookUp(EditorUI eui) {
        DrawLayer layerA = eui.findLayer(LAYER_A_NAME);
        if (layerA == null) {
            layerA = new HighlightingDrawLayer(LAYER_A_NAME, FILTER_A);
            eui.addLayer(layerA, 10000); // the old text selection layer's z-order (DrawLayerFactory.CaretLayer)

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Successfully registered layerA in " + simpleToString(eui)); //NOI18N
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("LayerA is already registered in " + simpleToString(eui)); //NOI18N
            }
        }

        DrawLayer layerB = eui.findLayer(LAYER_B_NAME);
        if (layerB == null) {
            layerB = new HighlightingDrawLayer(LAYER_B_NAME, FILTER_B);
            eui.addLayer(layerB, 2050); // the old caret row highlight layer's z-order

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Successfully registered layerB in " + simpleToString(eui)); //NOI18N
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("LayerB is already registered in " + simpleToString(eui)); //NOI18N
            }
        }

        DrawLayer layerC = eui.findLayer(LAYER_C_NAME);
        if (layerC == null) {
            layerC = new HighlightingDrawLayer(LAYER_C_NAME, FILTER_C);
            eui.addLayer(layerC, 1000); // the old syntax draw layer's z-order (DrawLayerFactory.SyntaxLayer)

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Successfully registered layerC in " + simpleToString(eui)); //NOI18N
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("LayerC is already registered in " + simpleToString(eui)); //NOI18N
            }
        }

        DrawLayer layerD = eui.findLayer(LAYER_D_NAME);
        if (layerD == null) {
            layerD = new HighlightingDrawLayer(LAYER_D_NAME, FILTER_D);
            eui.addLayer(layerD, 500);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Successfully registered layerD in " + simpleToString(eui)); //NOI18N
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("LayerD is already registered in " + simpleToString(eui)); //NOI18N
            }
        }
    }
    
    private final HighlightsLayerFilter filter;

    private WeakReference<JTextComponent> paneRef = null;
    private HighlightsContainer highlights = null;
    
    private AttributeSet lastAttributeSet = null;
    
    // The end-of-line attributes cache
    private AttributeSet lastEOLAttribs = null;
    private AttributeSet lastELAttribs = null;
    private boolean theLittleSpitAtTheBeginningOfAnEmptyLineDrawn = false;
    
    /** Index of the last found line element in processOffset(). */
    private int lastLineIndex;
    
    private boolean atomicLockListeningResolved;
    private boolean inAtomicLock;
    
    private Position damageStartPos;
    private Position damageEndPos;
    
    private HighlightingDrawLayer(String name, HighlightsLayerFilter filter) {
        super(name);
        this.filter = filter;
    }

    public @Override void init(DrawContext ctx) {
        super.init(ctx);
        
        if (highlights == null) {
            // Initialize
            JTextComponent pane = ctx.getEditorUI().getComponent();
            
            // HACK: the component can be null when printing, so we will just
            // create a fake JEditorPane
            if (pane == null) {
                Document doc = ctx.getEditorUI().getDocument();
                
                // Get the document's mime type
                String mimeType = (String) doc.getProperty(BaseDocument.MIME_TYPE_PROP); //NOI18N
                assert mimeType != null : "Document's mime type can't be null: " + doc; //NOI18N

// HACK: can't set the kit on fakePane, because it needs to run in AWT, which
// the print actions generally don't. So, the fakePane has EditorKit with
// the wrong mime type (most likely text/plain). It does not matter much, because
// the SyntaxHighlighting and NonLexerSyntaxHighlighting layers are registred for
// all mime types and we do not care about other layers.
//
//                // Find the appropriate editor kit
//                EditorKit kit = MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(EditorKit.class);
//                assert kit != null : "Can't finde EditorKit for mime type '" + mimeType + "'";
                
                // Create a fake pane
                JEditorPane fakePane = new JEditorPane();
//                fakePane.setEditorKit(kit);
                fakePane.setDocument(doc);
                
                // Filter out all highlights layers, but syntax highlighting
                fakePane.putClientProperty("HighlightsLayerIncludes", new String [] { "^.*NonLexerSyntaxHighlighting$", "^.*SyntaxHighlighting$"}); //NOI18N
                
                pane = fakePane;
            }
            this.paneRef = new WeakReference<JTextComponent>(pane);

            HighlightingManager hm = HighlightingManager.getInstance();
            this.highlights = hm.getHighlights(pane, filter);
            this.highlights.addHighlightsChangeListener(this);

            if (LOG.isLoggable(Level.FINE)) {
                if (filter == FILTER_A) {
                    LOG.fine("CHC@" + Integer.toHexString(System.identityHashCode(highlights)) + " is for FILTER_A"); //NOI18N
                } else if (filter == FILTER_B) {
                    LOG.fine("CHC@" + Integer.toHexString(System.identityHashCode(highlights)) + " is for FILTER_B"); //NOI18N
                } else if (filter == FILTER_C) {
                    LOG.fine("CHC@" + Integer.toHexString(System.identityHashCode(highlights)) + " is for FILTER_C"); //NOI18N
                }
            }
        }
    
        lastAttributeSet = null;
        
        // Reset the end-of-line attributes cache
        lastEOLAttribs = null;
        lastELAttribs = null;
        theLittleSpitAtTheBeginningOfAnEmptyLineDrawn = false;
        
        if (!atomicLockListeningResolved) {
            atomicLockListeningResolved = true;
            BaseDocument doc = ctx.getEditorUI().getDocument();
            doc.addAtomicLockListener(WeakListeners.create(AtomicLockListener.class, this, doc));
        }
    }
    
    public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
        if (highlights != null) {
            return processOffset(ctx, false);
        } else {
            return false;
        }
    }

    public void updateContext(DrawContext ctx) {
        if (highlights != null) {
            if (ctx.isEOL() && ctx.isBOL()) {
                if (extendsEmptyLine() && !theLittleSpitAtTheBeginningOfAnEmptyLineDrawn) {
                    theLittleSpitAtTheBeginningOfAnEmptyLineDrawn = true;
                    Coloring coloring = Coloring.fromAttributeSet(lastELAttribs);
                    coloring.apply(ctx);
                } else {
                    if (extendsEOL()) {
                        Coloring coloring = Coloring.fromAttributeSet(lastEOLAttribs);
                        coloring.apply(ctx);
                    }
                }
            } else if (ctx.isEOL()) {
                if (extendsEOL()) {
                    Coloring coloring = Coloring.fromAttributeSet(lastEOLAttribs);
                    coloring.apply(ctx);
                }
            } else {
                processOffset(ctx, true);
            }
        }
    }

    public @Override boolean extendsEOL() {
        if (lastEOLAttribs == null && lastAttributeSet != null) {
            @SuppressWarnings("unchecked")
            List<AttributeSet> allSets = (List<AttributeSet>) lastAttributeSet.getAttribute("dismantled-structure"); //NOI18N
            AttributeSet [] arr = filter(allSets != null ? allSets : Collections.singletonList(lastAttributeSet));
            lastEOLAttribs = arr[0];
            lastELAttribs = arr[1];
        }
        
        boolean b = lastEOLAttribs != null && lastEOLAttribs != SimpleAttributeSet.EMPTY;
        if (LOG.isLoggable(Level.FINE) && filter == FILTER_A) {
            LOG.fine(simpleToString(this) + ".extendsEOL = " + b);
        }
        return b;
    }
    
    public @Override boolean extendsEmptyLine() {
        if (lastELAttribs == null && lastAttributeSet != null) {
            @SuppressWarnings("unchecked")
            List<AttributeSet> allSets = (List<AttributeSet>) lastAttributeSet.getAttribute("dismantled-structure"); //NOI18N
            AttributeSet [] arr = filter(allSets != null ? allSets : Collections.singletonList(lastAttributeSet));
            lastEOLAttribs = arr[0];
            lastELAttribs = arr[1];
        }

        boolean b = lastELAttribs != null && lastELAttribs != SimpleAttributeSet.EMPTY;
        if (LOG.isLoggable(Level.FINE) && filter == FILTER_A) {
            LOG.fine(simpleToString(this) + ".extendsEmptyLine = " + b);
        }
        return b;
    }
    
    // ----------------------------------------------------------------------
    //  HighlightsChangeListener implementation
    // ----------------------------------------------------------------------
    
    public void highlightChanged(final HighlightsChangeEvent event) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("BRIDGE-LAYER: changed area [" + event.getStartOffset() + ", " + event.getEndOffset() + "]"); //NOI18N
            
//            LOG.log(Level.FINE, "Dumping highlights: {");
//            HighlightsSequence seq = highlights.getHighlights(0, Integer.MAX_VALUE);
//            while(seq.moveNext()) {
//                StringBuilder sb = new StringBuilder();
//                sb.append("    <");
//                sb.append(seq.getStartOffset());
//                sb.append(", ");
//                sb.append(seq.getEndOffset());
//                sb.append(", {");
//                
//                Enumeration<?> attrNames = seq.getAttributes().getAttributeNames();
//                while(attrNames.hasMoreElements()) {
//                    Object attrName = attrNames.nextElement();
//                    Object attrValue = seq.getAttributes().getAttribute(attrName);
//                    
//                    sb.append(attrName == null ? "null" : attrName.toString());
//                    sb.append(" = ");
//                    sb.append(attrValue == null ? "null" : attrValue.toString());
//                    
//                    if (attrNames.hasMoreElements()) {
//                        sb.append(", ");
//                    }
//                }
//                
//                sb.append("}>");
//                LOG.log(Level.FINE, sb.toString());
//            }
//            LOG.log(Level.FINE, "--- End of Dumping highlights");
        }
        
        if (event.getStartOffset() == event.getEndOffset()) {
            return ;
        }
        
        if (inAtomicLock) {
            JTextComponent pane = paneRef.get();
            if (pane != null) {
                Document doc = pane.getDocument();
                if (doc != null) {
                    int startOffset = Math.max(0, Math.min(event.getStartOffset(), doc.getLength()));
                    int endOffset = Math.max(startOffset, Math.min(event.getEndOffset(), doc.getLength()));
                    try {
                        // Only extend the modified area
                        if (damageStartPos == null) {
                            damageStartPos = doc.createPosition(startOffset);
                        }
                        if (damageEndPos == null) {
                            damageEndPos = doc.createPosition(endOffset);
                        }

                        if (startOffset < damageStartPos.getOffset()) {
                           damageStartPos = doc.createPosition(startOffset);
                        }
                        if (endOffset > damageEndPos.getOffset()) {
                            damageEndPos = doc.createPosition(endOffset);
                        }
                    } catch (BadLocationException e) {
                        LOG.log(Level.WARNING, "Cannot set damaged range", e);
                        damageStartPos = null;
                        damageEndPos = null;
                    }
                }
            }
            if (LOG.isLoggable(Level.FINE) && damageStartPos != null && damageEndPos != null) {
                LOG.fine("highlightsChangeEvent: [" + event.getStartOffset() + ", " + event.getEndOffset() +
                    "], toDAMAGE: [" + damageStartPos.getOffset() + ", " + damageEndPos.getOffset() + "]\n");
            }
            return; // Wait for the atomic unlock
        }
        
        invokeDamageRange(event.getStartOffset(), event.getEndOffset());
    }
     
    private void invokeDamageRange(final int startOffset, final int endOffset) {
//        LOG.log(Level.INFO, "invokeDamageRange: [" + startOffset + ", " + endOffset + "]", new Exception());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setNextActivityChangeOffset(0);

                JTextComponent pane = paneRef.get();
                Document d;
                if (pane != null && (d = pane.getDocument()) instanceof AbstractDocument) {
                    AbstractDocument doc = (AbstractDocument)d;
                    doc.readLock();
                    try {
                        int rangeEnd = Math.min(endOffset, pane.getDocument().getLength() + 1);
                        int rangeStart = startOffset >= rangeEnd ? 0 : startOffset;

                        if (rangeStart < rangeEnd) {
                            try {
                                if (LOG.isLoggable(Level.FINE)) {
                                    LOG.fine("DamageRange: [" + rangeStart + ", " + rangeEnd + "]\n");
                                }
                                pane.getUI().damageRange(pane, rangeStart, rangeEnd);
                            } catch (Exception e) {
                                LOG.log(Level.INFO, "Can't update view: range = [" + rangeStart + ", " + rangeEnd + "]", e); //NOI18N
                            }

                            try {
                                // XXX: hack, we should use isFixedSize() flag of the layers
                                // this is blindely assuming that caret row highlighting and
                                // higher layers do not change text metrics (eg. font)
                                if (filter == FILTER_C) {
                                    notifyViews(pane.getUI().getRootView(pane), rangeStart, rangeEnd);
                                }
                            } catch (Exception e) {
                                LOG.log(Level.INFO, "Can't reset line views: range = [" + rangeStart + ", " + rangeEnd + "]", e); //NOI18N
                            }

                            // force caret repaint, see #100384
                            // XXX: not very efficient, should only be done for
                            // containers/events that affect metrics
                            Caret caret = pane.getCaret();
                            if (caret instanceof BaseCaret) {
                                ((BaseCaret) caret).changedUpdate(null);
                            }
                        }
                    } finally {
                        doc.readUnlock();
                    }
                }
            }
        });
    }

    public void atomicLock(AtomicLockEvent evt) {
        inAtomicLock = true;
    }

    public void atomicUnlock(AtomicLockEvent evt) {
        inAtomicLock = false;
        if (damageStartPos != null && damageEndPos != null) { // Accumulated damage range
            invokeDamageRange(damageStartPos.getOffset(), damageEndPos.getOffset());
        }
        damageStartPos = null;
        damageEndPos = null;
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------
    
    private void notifyViews(View view, int startOffset, int endOffset) {
        // Find DrawEngineLineView
        while (view != null) {
            int idx = view.getViewIndex(startOffset, Bias.Forward);
            if (idx != -1) {
                View v = view.getView(idx);
                if (v instanceof DrawEngineLineView) {
                    break;
                } else {
                    view = v;
                }
            } else {
                view = null;
            }
        }
        
        // If DrawEngineLineView found reset all of them between startOffset, endOffset
        if (view != null) {
            LockView lockView = LockView.get(view);
            lockView.lock();
            try {
                int firstViewIdx = view.getViewIndex(startOffset, Bias.Forward);
                int lastViewIdx = view.getViewIndex(endOffset, Bias.Forward);

                for(int i = firstViewIdx; i <= lastViewIdx; i++) {
                    View v = view.getView(i);
                    if (v instanceof DrawEngineLineView) {
                        ((DrawEngineLineView) v).highlightsChanged(
                            Math.max(startOffset, v.getStartOffset()),
                            Math.min(endOffset, v.getEndOffset())
                        );
                    }
                }
            } finally {
                lockView.unlock();
            }
        }
    }
    
    private int findLineEndOffset(Document doc, int offset) {
        Element lineRootElement = doc.getDefaultRootElement();
        int lineIndex = lastLineIndex;
        if (lineIndex < lineRootElement.getElementCount()) {
            Element lineElement = lineRootElement.getElement(lineIndex);
            if (offset >= lineElement.getStartOffset() && offset < lineElement.getEndOffset()) {
                return lineElement.getEndOffset();
            }
        }
        lineIndex = lineRootElement.getElementIndex(offset);
        lastLineIndex = lineIndex;
        return lineRootElement.getElement(lineIndex).getEndOffset();
    }
    
    private boolean processOffset(DrawContext ctx, boolean applyAttributes) {
        BaseDocument doc = ctx.getEditorUI().getDocument();
        int currentOffset = ctx.getFragmentOffset();
        int endOffset = findLineEndOffset(doc, currentOffset);
        
        if (endOffset >= doc.getLength()) {
            endOffset = Integer.MAX_VALUE;
        }
        
        HighlightsSequence hs = highlights.getHighlights(currentOffset, endOffset);
        boolean hasHighlight = hs.moveNext();

        if (hasHighlight) {
            if (hs.getStartOffset() <= currentOffset) {
                if (applyAttributes) {
                    Coloring coloring = Coloring.fromAttributeSet(hs.getAttributes());
                    coloring.apply(ctx);
                }
                
                lastAttributeSet = hs.getAttributes();
                setNextActivityChangeOffset(hs.getEndOffset());
            } else {
                setNextActivityChangeOffset(hs.getStartOffset());
            }

            return true;
        } else {
            return false;
        }
    }

    private AttributeSet [] filter(List<AttributeSet> sets) {
        ArrayList<AttributeSet> eolSets = new ArrayList<AttributeSet>();
        ArrayList<AttributeSet> elSets = new ArrayList<AttributeSet>();
        
        for(AttributeSet set : sets) {
            Object value = set.getAttribute(HighlightsContainer.ATTR_EXTENDS_EOL);
            
            if ((value instanceof Boolean) && ((Boolean) value).booleanValue()) {
                eolSets.add(set);
            }
            
            value = set.getAttribute(HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE);
            if ((value instanceof Boolean) && ((Boolean) value).booleanValue()) {
                elSets.add(set);
            }
        }

        AttributeSet eolAttribs;
        if (eolSets.size() > 1) {
            eolAttribs = AttributesUtilities.createComposite(eolSets.toArray(new AttributeSet[eolSets.size()]));
        } else if (eolSets.size() == 1) {
            eolAttribs = eolSets.get(0);
        } else {
            eolAttribs = SimpleAttributeSet.EMPTY;
        }

        AttributeSet elAttribs;
        if (elSets.size() > 1) {
            elAttribs = AttributesUtilities.createComposite(elSets.toArray(new AttributeSet[elSets.size()]));
        } else if (elSets.size() == 1) {
            elAttribs = elSets.get(0);
        } else {
            elAttribs = SimpleAttributeSet.EMPTY;
        }

        return new AttributeSet [] { eolAttribs, elAttribs };
    }
    
    private static String simpleToString(Object o) {
        return o == null ? "null" : o.getClass() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }
}
