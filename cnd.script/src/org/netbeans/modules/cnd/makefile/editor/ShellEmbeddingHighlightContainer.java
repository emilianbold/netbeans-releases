/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makefile.editor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.text.NbDocument;

/**
 * @author Alexey Vladykin
 */
public class ShellEmbeddingHighlightContainer extends AbstractHighlightsContainer {

    public static ShellEmbeddingHighlightContainer get(Document doc) {
        ShellEmbeddingHighlightContainer l = (ShellEmbeddingHighlightContainer) doc.getProperty(ShellEmbeddingHighlightContainer.class);
        if (l == null) {
            doc.putProperty(ShellEmbeddingHighlightContainer.class, l = new ShellEmbeddingHighlightContainer(doc));
        }
        return l;
    }

    private final Document doc;
    private List<HighlightItem> highlights;

    private ShellEmbeddingHighlightContainer(Document doc) {
        this.doc = doc;
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        List<HighlightItem> highlightsCopy = highlights;
        return highlightsCopy == null || highlightsCopy.isEmpty()
                ? HighlightsSequence.EMPTY
                : new ShellHighlightsSequence(highlightsCopy, startOffset, endOffset);
    }

    /*package*/ void setHighlights(final List<HighlightItem> highlights) {
        NbDocument.runAtomic((StyledDocument) doc, new Runnable() {
            @Override
            public void run() {
                ShellEmbeddingHighlightContainer.this.highlights = highlights;
                fireHighlightsChange(0, doc.getLength());
            }
        });
    }

    public static final class HighlightItem {
        private final Position start;
        private final Position end;
        private final String category;

        public HighlightItem(Position start, Position end, String category) {
            this.start = start;
            this.end = end;
            this.category = category;
        }
    }

    private static final class ShellHighlightsSequence implements HighlightsSequence {

        private static final FontColorSettings SETTINGS = MimeLookup.getLookup(MimePath.get(MIMENames.SHELL_MIME_TYPE)).lookup(FontColorSettings.class);
        private final Iterator<HighlightItem> highlightIterator;
        private HighlightItem currentItem;

        private ShellHighlightsSequence(List<HighlightItem> highlights, int startOffset, int endOffset) {
            int startIdx = -1;
            int endIdx = -1;
            for (int i = 0; i < highlights.size(); ++i) {
                HighlightItem item = highlights.get(i);
                if (startIdx < 0 && startOffset < item.end.getOffset()) {
                    startIdx = i;
                }
                if (item.start.getOffset() < endOffset) {
                    endIdx = i;
                }
            }
            if (0 <= startIdx && 0 <= endIdx) {
                this.highlightIterator = highlights.subList(startIdx, endIdx).iterator();
            } else {
                this.highlightIterator = Collections.<HighlightItem>emptyList().iterator();
            }
        }

        @Override
        public boolean moveNext() {
            boolean hasNext = highlightIterator.hasNext();
            if (hasNext) {
                currentItem = highlightIterator.next();
            } else {
                currentItem = null;
            }
            return hasNext;
        }

        @Override
        public int getStartOffset() {
            if (currentItem == null) {
                throw new NoSuchElementException();
            }
            return currentItem.start.getOffset();
        }

        @Override
        public int getEndOffset() {
            if (currentItem == null) {
                throw new NoSuchElementException();
            }
            return currentItem.end.getOffset();
        }

        @Override
        public AttributeSet getAttributes() {
            if (currentItem == null) {
                throw new NoSuchElementException();
            }
            return SETTINGS.getTokenFontColors(currentItem.category);
        }
    }

    public static final class LayerFactory implements HighlightsLayerFactory {

        @Override
        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[]{
                HighlightsLayer.create(
                        ShellEmbeddingProvider.class.getName(),
                        ZOrder.SYNTAX_RACK, false, // must be below makefile syntax
                        ShellEmbeddingHighlightContainer.get(context.getDocument()))
            };
        }
    }
}
