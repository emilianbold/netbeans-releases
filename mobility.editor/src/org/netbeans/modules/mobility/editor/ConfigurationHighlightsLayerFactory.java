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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mobility.editor;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.project.Project;
import org.netbeans.mobility.antext.preprocessor.PPBlockInfo;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.text.NbDocument;

public class ConfigurationHighlightsLayerFactory implements HighlightsLayerFactory {
    
    static String PROP_HIGLIGHT_HEADER_LAYER = "mobility-embedded-headers-highlighting-layer"; //NOI18N
    static String PROP_HIGLIGHT_BLOCKS_LAYER = "mobility-embedded-blocks-highlighting-layer"; //NOI18N

    private static final Logger LOG = Logger.getLogger(ConfigurationHighlightsLayerFactory.class.getName());

    public ConfigurationHighlightsLayerFactory() {
        super();
    }

    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[]{
            HighlightsLayer.create(PROP_HIGLIGHT_HEADER_LAYER,
            ZOrder.SYNTAX_RACK.forPosition(120),
            true,
            new HeadersHighlighting(context.getDocument())),
            HighlightsLayer.create(PROP_HIGLIGHT_BLOCKS_LAYER,
            ZOrder.BOTTOM_RACK.forPosition(110),
            true,
            new BlocksHighlighting(context.getDocument()))
        };
    }
    
    static interface Highlighting {
        void updateBags();
    }

    private static final class HeadersHighlighting extends AbstractHighlightsContainer implements Highlighting {
        private static final Pattern BLOCK_HEADER_PATTERN = Pattern.compile("^\\s*/((/#)|(\\*[\\$#]))\\S"); //NOI18N
        private final Document document;
        private final OffsetsBag headersBag;
        private final AttributeSet commandHighlight;
        
        public HeadersHighlighting(Document document) {
            this.document = document;            
            this.document.putProperty(PROP_HIGLIGHT_HEADER_LAYER, this);

            FontColorSettings settings = MimeLookup.getLookup("text/x-java-preprocessor").lookup(FontColorSettings.class); //NOI18N
            commandHighlight = settings.getTokenFontColors("pp-command"); //NOI18N

            if (commandHighlight != null) {
                headersBag = new OffsetsBag(document);
                headersBag.addHighlightsChangeListener(new HighlightsChangeListener() {
                    public void highlightChanged(HighlightsChangeEvent event) {
                        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
                    }
                });
                updateBags();
            } else {
                headersBag = null;
            }
        }

        @Override
        public HighlightsSequence getHighlights(final int startOffset, final int endOffset) {
            if (headersBag != null) {
                return headersBag.getHighlights(startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }

        public void updateBags() {
            final Project p = J2MEProjectUtils.getProjectForDocument(document);
            //TODO J2MEProject?
            if (p == null || !(p instanceof J2MEProject) || headersBag == null) {
                return;
            }

            document.render(new Runnable() {
                public void run() {
                    // XXX: why is this not done by LineParser ?
                    final OffsetsBag bag = new OffsetsBag(document, true);
                    Element root = NbDocument.findLineRootElement((StyledDocument) document);
                    int count = root.getElementCount();
                    try {
                        for (int i = 0; i < count; i++) {
                            Element elm = root.getElement(i);
                            if (BLOCK_HEADER_PATTERN.matcher(document.getText(elm.getStartOffset(), elm.getEndOffset() - elm.getStartOffset()).trim()).find()) {
                                bag.addHighlight(elm.getStartOffset(), elm.getEndOffset(), commandHighlight);
                            }
                        }
                    } catch (BadLocationException ex) {
                        //ignore
                        return;
                    }
                    headersBag.setHighlights(bag);
                }
            });
        }
    } // End of HeadersHighlighting class

    private static final class BlocksHighlighting extends AbstractHighlightsContainer implements Highlighting {
        private final Document document;
        private final OffsetsBag blocksBag;
        private final AttributeSet activeBlockHighlight;
        private final AttributeSet inactiveBlockHighlight;

        public BlocksHighlighting(Document document) {
            this.document = document;
            this.document.putProperty(PROP_HIGLIGHT_BLOCKS_LAYER, this);

            AttributeSet extendsEolEmptyLine = AttributesUtilities.createImmutable(
                HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE,
                HighlightsContainer.ATTR_EXTENDS_EMPTY_LINE, Boolean.TRUE);

            FontColorSettings settings = MimeLookup.getLookup("text/x-java-preprocessor").lookup(FontColorSettings.class); //NOI18N
            AttributeSet as = settings.getTokenFontColors("pp-active-block"); //NOI18N
            if (as != null) {
                activeBlockHighlight = AttributesUtilities.createImmutable(as, extendsEolEmptyLine);
            } else {
                activeBlockHighlight = null;
            }
            as = settings.getTokenFontColors("pp-inactive-block"); //NOI18N
            if (as != null) {
                inactiveBlockHighlight = AttributesUtilities.createImmutable(as, extendsEolEmptyLine);
            } else {
                inactiveBlockHighlight = null;
            }

            if (activeBlockHighlight != null && inactiveBlockHighlight != null) {
                blocksBag = new OffsetsBag(document, true);
                blocksBag.addHighlightsChangeListener(new HighlightsChangeListener() {
                    public void highlightChanged(HighlightsChangeEvent event) {
                        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
                    }
                });
                updateBags();
            } else {
                blocksBag = null;
            }
        }

        @Override
        public HighlightsSequence getHighlights(int startOffset, int endOffset) {
            if (blocksBag != null) {
                return blocksBag.getHighlights(startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }    

        public void updateBags() {
            final Project p = J2MEProjectUtils.getProjectForDocument(document);
            //TODO J2MEProject?
            if (p == null || !(p instanceof J2MEProject) || blocksBag == null) {
                return;
            }

            document.render(new Runnable() {
                public void run() {
                    List<PPBlockInfo> blockList = (List<PPBlockInfo>) document.getProperty(DocumentPreprocessor.PREPROCESSOR_BLOCK_LIST);
                    if (blockList == null) {
                        return;
                    }

                    final OffsetsBag bag = new OffsetsBag(document, true);
                    LOG.log(Level.FINE, "Dumping lineset({0})", blockList.size()); //NOI18N
                    for (PPBlockInfo b : blockList) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "lineBlock: type={0}, startLine={1}, endLine={2}, active={3}", //NOI18N
                                    new Object[]{b.getType(), b.getStartLine(), b.getEndLine(), b.isActive()});
                        }
                        StyledDocument doc = (StyledDocument) document;
                        bag.addHighlight(
                                NbDocument.findLineRootElement(doc).getElement(b.getStartLine() - 1).getStartOffset(),
                                NbDocument.findLineRootElement(doc).getElement(b.getEndLine() - 1).getEndOffset(),
                                b.isActive() ? activeBlockHighlight : inactiveBlockHighlight);
                    }
                    LOG.log(Level.FINE, "-------------------"); //NOI18N
                    blocksBag.setHighlights(bag);
                }
            });
        }
   } // End of BlocksHighlighting class

}
