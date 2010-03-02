/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.impl.highlighting;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class AnnotationsHighlighting extends AbstractHighlightsContainer implements Annotations.AnnotationsListener, HighlightsChangeListener {

    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.oldlibbridge.AnnotationsHighlighting"; //NOI18N

    public AnnotationsHighlighting(Document document) {
        if (document instanceof BaseDocument) {
            this.document = (BaseDocument) document;
            this.annotations = this.document.getAnnotations();
            this.annotations.addAnnotationsListener(WeakListeners.create(Annotations.AnnotationsListener.class, this, this.annotations));
            this.bag = new OffsetsBag(document, true);
            this.bag.addHighlightsChangeListener(this);
        } else {
            this.document = null;
            this.annotations = null;
            this.bag = null;
        }
    }

    public @Override HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (bag != null) {
            return bag.getHighlights(startOffset, endOffset);
        } else {
            return HighlightsSequence.EMPTY;
        }
    }

    // ----------------------------------------------------------------------
    //  HighlightsChangeListener implementation
    // ----------------------------------------------------------------------

    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
    }

    // ----------------------------------------------------------------------
    //  AnnotationsListener implementation
    // ----------------------------------------------------------------------

    public void changedLine(final int line) {
        changedAll();
    }

    public void changedAll() {
        synchronized (this) {
            if (refreshAllLinesTask == null) {
                refreshAllLinesTask = RP.post(new Runnable() {
                    public void run() {
                        refreshAllLines();
                        synchronized (AnnotationsHighlighting.this) {
                            refreshAllLinesTask = null;
                        }
                    }
                });
            } else {
                refreshAllLinesTask.schedule(DELAY);
            }
        }
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AnnotationsHighlighting.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(LAYER_TYPE_ID);
    private static final int DELAY = 43; //ms

    private final BaseDocument document;
    private final Annotations annotations;
    private final OffsetsBag bag;
    private final Map<AnnotationType, AttributeSet> cache = new WeakHashMap<AnnotationType, AttributeSet>();
    
    private RequestProcessor.Task refreshAllLinesTask = null;

    private void refreshAllLines() {
        final OffsetsBag b = new OffsetsBag(document, true);
        
        try {
            for(int line = annotations.getNextLineWithAnnotation(0); line != -1; line = annotations.getNextLineWithAnnotation(line + 1)) {
                refreshLine(line, b, -1, -1);
            }
        } catch (Exception e) {
            // ignore, refreshLine is intentionally called outside of the document lock
            // in order not to block editing
            return;
        }

        document.render(new Runnable() {
            public void run() {
                bag.setHighlights(new FilteringHighlightsSequence(b.getHighlights(0, document.getLength())));
            }
        });
    }

    private void refreshLine(int line, OffsetsBag b, int lineStartOffset, int lineEndOffset) {
        LOG.log(Level.FINE, "Refreshing line {0}", line); //NOI18N

        AnnotationDesc [] allPassive = annotations.getPasiveAnnotations(line);
        if (allPassive != null) {
            for(AnnotationDesc passive : allPassive) {
                AttributeSet attribs = getAttributes(passive.getAnnotationTypeInstance());
                if (passive.isVisible()) {
                    if (passive.isWholeLine()) {
                        if (lineStartOffset == -1 || lineEndOffset == -1) {
                            Element lineElement = document.getDefaultRootElement().getElement(line);
                            lineStartOffset = lineElement.getStartOffset();
                            lineEndOffset = lineElement.getEndOffset();
                        }
                        b.addHighlight(lineStartOffset, lineEndOffset, attribs);
                    } else {
                        b.addHighlight(passive.getOffset(), passive.getOffset() + passive.getLength(), attribs);
                    }
                }
            }
        }

        AnnotationDesc active = annotations.getActiveAnnotation(line);
        if (active != null && active.isVisible()) {
            AttributeSet attribs = getAttributes(active.getAnnotationTypeInstance());
            if (active.isWholeLine()) {
                if (lineStartOffset == -1 || lineEndOffset == -1) {
                    Element lineElement = document.getDefaultRootElement().getElement(line);
                    lineStartOffset = lineElement.getStartOffset();
                    lineEndOffset = lineElement.getEndOffset();
                }
                b.addHighlight(lineStartOffset, lineEndOffset, attribs);
            } else {
                b.addHighlight(active.getOffset(), active.getOffset() + active.getLength(), attribs);
            }
        }
    }

    private AttributeSet getAttributes(AnnotationType annotationType) {
        synchronized (cache) {
            AttributeSet attrs = cache.get(annotationType);
            if (attrs == null) {
                attrs = AttributesUtilities.createImmutable(
                    StyleConstants.Foreground, !annotationType.isInheritForegroundColor() ? annotationType.getForegroundColor() : null,
                    StyleConstants.Background, annotationType.isUseHighlightColor() ? annotationType.getHighlight() : null,
                    EditorStyleConstants.WaveUnderlineColor, annotationType.isUseWaveUnderlineColor() ? annotationType.getWaveUnderlineColor() : null,
                    HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE, Boolean.valueOf(annotationType.isWholeLine()),
                    HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.valueOf(annotationType.isWholeLine())
                );
                cache.put(annotationType, attrs);
            }
            return attrs;
        }
    }

    private static final class FilteringHighlightsSequence implements HighlightsSequence {
        private final HighlightsSequence delegate;

        public FilteringHighlightsSequence(HighlightsSequence delegate) {
            this.delegate = delegate;
        }

        public boolean moveNext() {
            return delegate.moveNext();
        }

        public int getStartOffset() {
            return delegate.getStartOffset();
        }

        public int getEndOffset() {
            return delegate.getEndOffset();
        }

        public AttributeSet getAttributes() {
            AttributeSet attrs = delegate.getAttributes();
            List<Object> attrsList = new ArrayList<Object>();

            for (Enumeration<?> en = attrs.getAttributeNames(); en.hasMoreElements(); ) {
                Object key = en.nextElement();
                Object v = attrs.getAttribute(key);

                if (v != null) {
                    attrsList.add(key);
                    attrsList.add(v);
                }
            }

            AttributeSet filtered = AttributesUtilities.createImmutable(attrsList.toArray(new Object[attrsList.size()]));

            return filtered;
        }

    }
}
