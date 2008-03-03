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

import java.awt.Color;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.api.editor.settings.FontColorNames;

/**
 * Various draw layers are located here
 *
 * @author Miloslav Metelka
 * @version 1.00
 * 
 * @deprecated Please use Highlighting SPI instead, for details see
 *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
 */
public class DrawLayerFactory {

    private static final Logger LOG = Logger.getLogger(DrawLayerFactory.class.getName());
    
    /** Syntax draw layer name */
    public static final String SYNTAX_LAYER_NAME = "syntax-layer"; // NOI18N

    /** Syntax draw layer visibility */
    public static final int SYNTAX_LAYER_VISIBILITY = 1000;

    /** Annotation draw layer name */
    public static final String ANNOTATION_LAYER_NAME = "annotation-layer"; // NOI18N

    /** Annotation draw layer visibility */
    public static final int ANNOTATION_LAYER_VISIBILITY = 2100;
    
    /** Highlight search layer name */
    public static final String HIGHLIGHT_SEARCH_LAYER_NAME = "highlight-search-layer"; // NOI18N

    /** Highlight search layer visibility */
    public static final int HIGHLIGHT_SEARCH_LAYER_VISIBILITY = 9000;

    /** Incremental search layer name */
    public static final String INC_SEARCH_LAYER_NAME = "inc-search-layer"; // NOI18N

    /** Incremental search layer visibility */
    public static final int INC_SEARCH_LAYER_VISIBILITY = 9500;
    
    /** Search block layer name */
    public static final String BLOCK_SEARCH_LAYER_NAME = "block-search-layer"; // NOI18N

    /** Search block layer visibility */
    public static final int BLOCK_SEARCH_LAYER_VISIBILITY = 8500;

    /** Selection draw layer name */
    public static final String CARET_LAYER_NAME = "caret-layer"; // NOI18N

    /** Selection draw layer visibility */
    public static final int CARET_LAYER_VISIBILITY = 10000;


    /** Guarded layer name */
    public static final String GUARDED_LAYER_NAME = "guarded-layer"; // NOI18N

    /** Guarded layer visibility */
    public static final int GUARDED_LAYER_VISIBILITY = 1400;


    /** 
     * Layer that colors the text according to the tokens that were parsed.
     * It's active all the time.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class SyntaxLayer extends DrawLayer.AbstractLayer {

        public SyntaxLayer() {
            super(SYNTAX_LAYER_NAME);
        }

        public void init(DrawContext ctx) {
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            return true;
        }

        public void updateContext(DrawContext ctx) {
            // Get the token type and docColorings
            TokenID tokenID = ctx.getTokenID();
            TokenContextPath tcp = ctx.getTokenContextPath();
            if (tokenID != null && tcp != null) {
                // Get the coloring according the name of the token
                String fullName = tcp.getFullTokenName(tokenID);
                Coloring c = ctx.getEditorUI().getColoring(fullName);
                if (c != null) {
                    c.apply(ctx);

                } else { // Token coloring null, try category
                    TokenCategory cat = tokenID.getCategory();
                    if (cat != null) {
                        fullName = tcp.getFullTokenName(cat);
                        c = ctx.getEditorUI().getColoring(fullName);
                        if (c !=  null) {
                            c.apply(ctx);
                        }
                    }
                }
            }
        }

    }


    /** 
     * This layer colors the line by a color specified in constructor
     * It requires only activation mark since it deactivates automatically
     * at the end of line.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static abstract class ColorLineLayer extends DrawLayer.AbstractLayer {

        /** Coloring to use for highlighting */
        Coloring coloring;

        public ColorLineLayer(String name) {
            super(name);
        }

        public boolean extendsEOL() {
            return true;
        }

        public void init(DrawContext ctx) {
            coloring = null;
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            boolean active;
            if (mark == null) {
                View view = ctx instanceof DrawEngine.DrawInfo ? ((DrawEngine.DrawInfo)ctx).view.getParent() : null;
                if (view instanceof DrawEngineLineView) {
                    DrawEngineLineView delv = (DrawEngineLineView)view;
                    mark = getFoldedMark(delv, getName());
                    if (mark == null) {
                        view = delv.getView(delv.getViewCount() -1);
                        if (view instanceof DrawEngineLineView) {
                            delv = (DrawEngineLineView) view;
                            mark = getFoldedMark(delv, getName());
                        }
                    }
                }
            }
            if (mark != null) {
                active = (ctx.getEditorUI().getComponent() != null)
                    && mark.activateLayer;
                if (active) {
                    try {
                        BaseDocument doc = ctx.getEditorUI().getDocument();
                        int nextRowStartPos = Utilities.getRowStart(
                                doc, ctx.getFragmentOffset(), 1);
                        if (nextRowStartPos < 0) { // end of doc
                            nextRowStartPos = Integer.MAX_VALUE;
                        }
                        setNextActivityChangeOffset(nextRowStartPos);

                    } catch (BadLocationException e) {
                        active = false;
                    }
                }
            } else {
                active = false;
            }
            return active;
        }

        public void updateContext(DrawContext ctx) {
            if (coloring == null) {
                coloring = getColoring(ctx);
            }
            if (coloring != null) {
                coloring.apply(ctx);
            }
        }

        protected abstract Coloring getColoring(DrawContext ctx);
        
    }


    /** Layer that covers selection services provided by caret.
     * This layer assumes that both caretMark and selectionMark in
     * BaseCaret are properly served so that their active flags
     * are properly set.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class CaretLayer extends DrawLayer.AbstractLayer {

        Coloring coloring;

        public CaretLayer() {
            super(CARET_LAYER_NAME);
        }

        public boolean extendsEmptyLine() {
            return true;
        }

        public void init(DrawContext ctx) {
            coloring = null;
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            boolean active;
            if (mark != null) {
                active = mark.activateLayer;
            } else {
                JTextComponent c = ctx.getEditorUI().getComponent();
                active = (c != null) && c.getCaret().isSelectionVisible()
                         && ctx.getFragmentOffset() >= c.getSelectionStart()
                         && ctx.getFragmentOffset() < c.getSelectionEnd();
            }

            return active;
        }

        public void updateContext(DrawContext ctx) {
            if (coloring == null) {
                coloring = ctx.getEditorUI().getColoring(FontColorNames.SELECTION_COLORING);
            }
            if (coloring != null) {
                coloring.apply(ctx);
            }
        }

    }


    /** Highlight search layer highlights all occurences
     * of the searched string in text.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class HighlightSearchLayer extends DrawLayer.AbstractLayer {

        /** Pairs of start and end position of the found string */
        int blocks[] = new int[] { -1, -1 };

        /** Coloring to use for highlighting */
        Coloring coloring;

        /** Current index for painting */
        int curInd;

        /** Enabled flag */
        boolean enabled;

        public HighlightSearchLayer() {
            super(HIGHLIGHT_SEARCH_LAYER_NAME);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void init(DrawContext ctx) {
            if (enabled) {
                try {
                    BaseDocument doc = ctx.getEditorUI().getDocument();
                    blocks = FindSupport.getFindSupport().getBlocks(blocks,
                             doc, ctx.getStartOffset(), ctx.getEndOffset());
                } catch (BadLocationException e) {
                    blocks = new int[] { -1, -1 };
                }
                coloring = null; // reset so it will be re-read
                curInd = 0;
            }
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            boolean active;
            if (enabled) {
                int pos = ctx.getFragmentOffset();
                if (pos == blocks[curInd]) {
                    active = true;
                    setNextActivityChangeOffset(blocks[curInd + 1]);

                } else if (pos == blocks[curInd + 1]) {
                    active = false;
                    curInd += 2;
                    setNextActivityChangeOffset(blocks[curInd]);
                    if (pos == blocks[curInd]) { // just follows
                        setNextActivityChangeOffset(blocks[curInd + 1]);
                        active = true;
                    }

                } else {
                    setNextActivityChangeOffset(blocks[curInd]);
                    active = false;
                }
            } else {
                active = false;
            }

            return active;
        }

        public void updateContext(DrawContext ctx) {
            int pos = ctx.getFragmentOffset();
            if (pos >= blocks[curInd] && pos < blocks[curInd + 1]) {
                if (coloring == null) {
                    coloring = ctx.getEditorUI().getColoring(FontColorNames.HIGHLIGHT_SEARCH_COLORING);
                }
                if (coloring != null) {
                    coloring.apply(ctx);
                }
            }
        }

    }

    /** Layer covering incremental search. There are just two positions
     * begining and end of the searched string
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class IncSearchLayer extends DrawLayer.AbstractLayer {

        /** Position where the searched string begins */
        int pos;

        /** Length of area to highlight */
        int len;

        /** Whether this layer is enabled */
        boolean enabled;
        
        boolean invert;

        public IncSearchLayer() {
            super(INC_SEARCH_LAYER_NAME);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        void setArea(int pos, int len) {
            this.pos = pos;
            this.len = len;
        }

        public int getOffset() {
            return pos;
        }

        public int getLength() {
            return len;
        }

        public void init(DrawContext ctx) {
            setNextActivityChangeOffset(enabled ? pos : Integer.MAX_VALUE);
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            boolean active = false;
            if (enabled) {
                if (ctx.getFragmentOffset() == pos) {
                    active = true;
                    setNextActivityChangeOffset(pos + len);
                }
            }

            return active;
        }

        /** current INC_SEARCH_COLORING is used only in block search.
         *  if there is no search, use selection
         *  @param invert if true - selection  coloring is used
         */
        void setInversion(boolean invert){
            this.invert = invert;
        }
        
        public void updateContext(DrawContext ctx) {
            if (!invert) {
                Coloring coloring = ctx.getEditorUI().getColoring(FontColorNames.INC_SEARCH_COLORING);
                if (coloring != null) {
                    coloring.apply(ctx);
                }
            } else {
                Coloring invertedColoring = ctx.getEditorUI().getColoring(FontColorNames.SELECTION_COLORING);
                if (invertedColoring != null) {
                    invertedColoring.apply(ctx);
                }
            }
        }
    } // End of IncSearchLayer class

    /**
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class BlockSearchLayer extends DrawLayer.AbstractLayer {

        /** Position where the searched string begins */
        int pos;

        /** Length of area to highlight */
        int len;

        /** Whether this layer is enabled */
        boolean enabled;

        public BlockSearchLayer() {
            super(BLOCK_SEARCH_LAYER_NAME);
        }

        public boolean extendsEmptyLine(){
            return true;
        }
        
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        void setArea(int pos, int len) {
            this.pos = pos;
            this.len = len;
        }

        int getOffset() {
            return pos;
        }

        int getLength() {
            return len;
        }

        public void init(DrawContext ctx) {
            setNextActivityChangeOffset(enabled ? pos : Integer.MAX_VALUE);
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            boolean active = false;
            if (enabled) {
                int fragOffset = ctx.getFragmentOffset();
                if (fragOffset >= pos && fragOffset<(pos+len))  {
                    active = true;
                    try {
                        BaseDocument doc = ctx.getEditorUI().getDocument();
                        int nextRowStartPos = Utilities.getRowStart(
                                doc, fragOffset, 1);
                        if (nextRowStartPos < 0) { // end of doc
                            nextRowStartPos = Integer.MAX_VALUE;
                        }
                        setNextActivityChangeOffset(Math.min(nextRowStartPos,(pos+len)));

                    } catch (BadLocationException e) {
                        active = false;
                    }
                
                }
           }

            return active;
        }

        public void updateContext(DrawContext ctx) {
            Coloring coloring = ctx.getEditorUI().getColoring(FontColorNames.BLOCK_SEARCH_COLORING);
            if (coloring != null) {
                coloring.apply(ctx);
            }
        }
    } // End of BlockSearchLayer class

//
//  XXX: Deprecated and not used anymore. Can be removed.
//
//    /** Layer for guarded blocks 
//     *
//     * @deprecated Please use Highlighting SPI instead, for details see
//     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
//     */
//    static class GuardedLayer extends ColorLineLayer {
//
//        GuardedDocument doc;
//
//        GuardedLayer() {
//            super(GUARDED_LAYER_NAME);
//        }
//
//        public void init(DrawContext ctx) {
//            super.init(ctx);
//            doc = (GuardedDocument)ctx.getEditorUI().getDocument();
//        }
//
//        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
//            boolean active;
//            if (mark != null) {
//                active = mark.activateLayer;
//            } else {
//                active = doc.isPosGuarded(ctx.getFragmentOffset());
//            }
//
//            return active;
//        }
//
//        protected Coloring getColoring(DrawContext ctx) {
//            return ctx.getEditorUI().getColoring(SettingsNames.GUARDED_COLORING);
//        }
//
//    }

    
    
    /** 
     * Style layer getting color settings from particular style 
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class StyleLayer extends DrawLayer.AbstractLayer {

        protected Style style;

        protected MarkChain markChain;

        protected Color backColor;

        protected Color foreColor;

        public StyleLayer(String layerName, BaseDocument doc, Style style) {
            super(layerName);
            this.style = style;
            markChain = new MarkChain(doc, layerName);
        }

        public boolean extendsEOL() {
            return true;
        }

        public final MarkChain getMarkChain() {
            return markChain;
        }

        public void init(DrawContext ctx) {
            foreColor = StyleConstants.getForeground(style);
            backColor = StyleConstants.getBackground(style);
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            boolean active = false;
            if (mark != null) {
                active = (ctx.getEditorUI().getComponent() != null)
                    && mark.activateLayer;
                if (active) {
                    try {
                        BaseDocument doc = ctx.getEditorUI().getDocument();
                        int nextRowStartPos = Utilities.getRowStart(
                                doc, ctx.getFragmentOffset(), 1);
                        if (nextRowStartPos < 0) { // end of doc
                            nextRowStartPos = Integer.MAX_VALUE;
                        }

                        setNextActivityChangeOffset(nextRowStartPos);

                    } catch (BadLocationException e) {
                        active = false;
                    }
                }

            }

            return active;
        }

        public void updateContext(DrawContext ctx) {
            if (foreColor != null) {
                ctx.setForeColor(foreColor);
            }
            if (backColor != null) {
                ctx.setBackColor(backColor);
            }
        }

        public String toString() {
            return super.toString() + ((markChain != null) ? (", " + markChain) : ""); // NOI18N
        }

    }

    /** 
     * Test layer for coloring the specific words
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class WordColoringLayer extends DrawLayer.AbstractLayer {

        protected StringMap stringMap = new StringMap();

        public WordColoringLayer(String name) {
            super(name);
        }

        public void put(String s, Coloring c) {
            stringMap.put(s, c);
        }

        public void put(String[] strings, Coloring c) {
            for (int i = 0; i < strings.length; i++) {
                put(strings[i], c);
            }
        }

        public void put(List stringList, Coloring c) {
            String strings[] = new String[stringList.size()];
            stringList.toArray(strings);
            put(strings, c);
        }

        public void init(DrawContext ctx) {
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            return true;
        }

        public void updateContext(DrawContext ctx) {
            Coloring c = (Coloring)stringMap.get(ctx.getBuffer(),
                                 ctx.getTokenOffset(), ctx.getTokenLength());
            if (c != null) {
                c.apply(ctx);
            }
        }

    }

    // XXX: AnnotationLayer needs to be rewritten using the new Highlighting SPI.
    /** 
     * Annotation layer for drawing of annotations. Each mark which is stored in markChain has
     * corresponding Annotation. More than one Annotation can share one mark. In this case
     * the only one annotation is active and this must be drawn. 
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public static class AnnotationLayer extends DrawLayer.AbstractLayer {

        /** Current coloring */
        private Coloring coloring;
        
        /** Chain of marks attached to this layer */
        private MarkChain markChain;
                
        public AnnotationLayer(BaseDocument doc) {
            super(ANNOTATION_LAYER_NAME);
            coloring = null;
            markChain = new MarkChain(doc, ANNOTATION_LAYER_NAME);
        }

        /** Get chain of marks attached to this draw layer
         * @return mark chain */        
        public final MarkChain getMarkChain() {
            return markChain;
        }
        
        public boolean extendsEOL() {
            return true;
        }

        public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
            int nextActivityOffset;
            coloring = null;
            
            if (mark == null) {
                View view = ctx instanceof DrawEngine.DrawInfo ? ((DrawEngine.DrawInfo)ctx).view : null;
                if (view instanceof DrawEngineLineView) {
                    mark = getFoldedMark((DrawEngineLineView)view, getName());
                }
            }
            
            if (mark == null) {
                return false;
            }

            if (ctx.getEditorUI().getComponent() == null || !mark.activateLayer) {
                return false;
            }
            
            BaseDocument doc = ctx.getEditorUI().getDocument();
            
            // Gets the active annotation attached to this mark. It is possible that
            // no active annotation might exist for the mark, e.g. there can be
            // mark at the beginning of the line for a whole line annotation
            // and there can be mark in the middle of the line for a line-part annotation
            AnnotationDesc anno = doc.getAnnotations().getActiveAnnotation(mark);
            if (anno == null) {
                // if no active annotation was found for the given mark, check
                // whether we are not already drawing some other annotation. If that's
                // true we have to continue drawing it (means return true here)
                AnnotationDesc activeAnno = doc.getAnnotations().getLineActiveAnnotation(mark);
                if (activeAnno == null) {
                    return false;
                }
                if (ctx.getFragmentOffset() >= activeAnno.getOffset()) {
                    if (ctx.getFragmentOffset() < activeAnno.getOffset() + activeAnno.getLength() || activeAnno.isWholeLine()) {
                        coloring = activeAnno.getColoring();
                        return true;
                    }
                }
                return false;
            }

            if (anno.isWholeLine()) {
                try {
                    nextActivityOffset = Utilities.getRowEnd(doc, ctx.getFragmentOffset());
                } catch (BadLocationException ble) {
                    LOG.log(Level.FINE, null, ble);
                    return false;
                }
            } else {
                nextActivityOffset = anno.getOffset() + anno.getLength();
            }
            
            setNextActivityChangeOffset(nextActivityOffset);
            coloring = anno.getColoring();
            
//      The following code ensures that if active annotation does not 
//      have highlight the color of next one will be used.
//      It was decided that it will not be used
//            
//            if (coloring.getBackColor() == null) {
//                AnnotationDesc[] annos = doc.getAnnotations().getPasiveAnnotations(anno.getLine());
//                if (annos != null) {
//                    for (int i=0; i<annos.length; i++) {
//                        if (annos[i].getColoring().getBackColor() != null) {
//                            coloring = annos[i].getColoring();
//                            break;
//                        }
//                    }
//                }
//            }
            
            return true;
        }

        public void updateContext(DrawContext ctx) {
            if (coloring != null) {
                coloring.apply(ctx);
            }
        }

    }

    private static MarkFactory.DrawMark getFoldedMark(DrawEngineLineView view, String layerName) {
        BaseDocument doc = (BaseDocument)view.getDocument();
        int startPos = view.getStartOffset();
        int endPos = view.getEndOffset() - 1;
        MarkVector docMarksStorage = doc.marksStorage;
        synchronized (docMarksStorage) {
            int low = 0;
            int high = docMarksStorage.getMarkCount() - 1;

            while (low < high) { // Unlike regular binary search only iterate (low < high) - code below handles that correctly
                int mid = (low + high) >> 1;
                int cmp = docMarksStorage.getMarkOffsetInternal(mid) - endPos;
                if (cmp < 0)
                    low = mid + 1;
                else if (cmp > 0)
                    high = mid - 1;
                else { // found
                    while (++mid <= high && docMarksStorage.getMarkOffsetInternal(mid) == endPos) { }
                    low = high = mid - 1;
                }
            }

            while (low >= 0 && low < docMarksStorage.getMarkCount()) {
                MultiMark mm = (MultiMark)docMarksStorage.getMark(low--);
                if (mm.isValid() && mm.getOffset() <= endPos) {
                    if (mm.getOffset() < startPos) {
                        break;
                    }
                    Mark mrk = doc.marks.get(mm);
                    if (mrk instanceof MarkFactory.DrawMark && layerName.equals(((MarkFactory.DrawMark)mrk).layerName)) {
                        return (MarkFactory.DrawMark)mrk;
                    }
                }
            }
        }

        return null;
    }
}
