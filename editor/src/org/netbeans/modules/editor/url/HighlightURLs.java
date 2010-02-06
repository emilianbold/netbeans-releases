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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.editor.url;

import java.util.LinkedList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;


/**
 * Highlights URLs in the source editor
 * (This was based on the stripwhitespace module by Andrei Badea and the TODO highlighter)
 *
 * @author Andrei Badea
 * @author Tor Norbye
 * @author Jan Lahoda
 */
public final class HighlightURLs implements HighlightsContainer {

    private static final Logger LOG = Logger.getLogger(HighlightURLs.class.getName());
    
    private final BaseDocument doc;
    private final AttributeSet coloring;

    public HighlightURLs(BaseDocument doc) {
        this.doc = doc;
        String mimeType = DocumentUtilities.getMimeType(doc);
        FontColorSettings fcs = mimeType == null ? null : MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        this.coloring = fcs == null ? null : fcs.getTokenFontColors("url"); // NOI18N
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (coloring == null) {
            return HighlightsSequence.EMPTY;
        }
        
        List<int[]> highlights = new LinkedList<int[]>();

        try {
            startOffset = Utilities.getRowStart(doc, startOffset);
            endOffset = Math.min(doc.getLength(), endOffset);
            endOffset = Utilities.getRowEnd(doc, endOffset);

            CharSequence text = DocumentUtilities.getText(doc, startOffset, endOffset - startOffset);

            for (int[] span : Parser.recognizeURLs(text)) {
                highlights.add(new int[] {startOffset + span[0], startOffset + span[1]});
            }

            return new SeqImpl(highlights, coloring);
        } catch (BadLocationException e) {
            LOG.log(Level.WARNING, null, e);
            return HighlightsSequence.EMPTY;
        }
    }

    @Override
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
    }

    @Override
    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
    }
    
    private static final class SeqImpl implements HighlightsSequence {
        
        private int[] current;
        private final List<int[]> highlights;
        private final AttributeSet as;

        public SeqImpl(List<int[]> highlights, AttributeSet as) {
            this.highlights = highlights;
            this.as = as;
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("SeqImpl:"); // NOI18N
                for (int[] span : highlights) {
                    LOG.fine("span: " + span[0] + "-" + span[1]); // NOI18N
                }
            }
        }

        public @Override boolean moveNext() {
            if (highlights.isEmpty())
                return false;
            
            current = highlights.remove(0);
            
            return true;
        }

        public @Override int getStartOffset() {
            return current[0];
        }

        public @Override int getEndOffset() {
            return current[1];
        }

        public @Override AttributeSet getAttributes() {
            return as;
        }
    } // End of SeqImpl class
    
    public static final class FactoryImpl implements HighlightsLayerFactory {

        @Override
        public HighlightsLayer[] createLayers(Context context) {
            Document doc = context.getDocument();
            
            if (!(doc instanceof BaseDocument)) {
                return null;
            } else {
                HighlightsContainer c = new HighlightURLs((BaseDocument) doc);
                return new HighlightsLayer[] {
                    HighlightsLayer.create(HighlightURLs.class.getName(), ZOrder.SYNTAX_RACK.forPosition(4950), false, c),
                };
            }
        }
        
    } // End of FactoryImpl class
}
