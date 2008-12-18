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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.gsf.codecoverage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.codecoverage.api.CoverageType;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Highlight coverage lines
 *
 * @todo More efficient way to compute affected lines... instead of iterating over lines
 *
 *
 * @author Tor Norbye
 */
public class CoverageHighlightsContainer extends AbstractHighlightsContainer {
    private static final AttributeSet covered;
    private static final AttributeSet uncovered;
    private static final AttributeSet inferred;


    static {
        Color coveredBc = null;
        Color uncoveredBc = null;
        Color inferredBc = null;
        FontColorSettings fcs = MimeLookup.getLookup("text/plain").lookup(FontColorSettings.class);
        if (fcs != null) {
            coveredBc = getColoring(fcs, "covered"); // NOI18N
            uncoveredBc = getColoring(fcs, "uncovered"); // NOI18N
            inferredBc = getColoring(fcs, "inferred"); // NOI18N
        }
        if (coveredBc == null) {
            coveredBc = new Color(0xCC, 0xFF, 0xCC);
        }
        if (uncoveredBc == null) {
            uncoveredBc = new Color(0xFF, 0xCC, 0xCC);
        }
        if (inferredBc == null) {
            inferredBc = new Color(0xE0, 0xFF, 0xE0);
        }

        covered = coveredBc == null ? SimpleAttributeSet.EMPTY : AttributesUtilities.createImmutable(
                StyleConstants.Background, coveredBc,
                ATTR_EXTENDS_EOL, Boolean.TRUE, ATTR_EXTENDS_EMPTY_LINE, Boolean.TRUE);
        uncovered = uncoveredBc == null ? SimpleAttributeSet.EMPTY : AttributesUtilities.createImmutable(
                StyleConstants.Background, uncoveredBc,
                ATTR_EXTENDS_EOL, Boolean.TRUE, ATTR_EXTENDS_EMPTY_LINE, Boolean.TRUE);
        inferred = inferredBc == null ? SimpleAttributeSet.EMPTY : AttributesUtilities.createImmutable(
                StyleConstants.Background, inferredBc,
                ATTR_EXTENDS_EOL, Boolean.TRUE, ATTR_EXTENDS_EMPTY_LINE, Boolean.TRUE);
    }
    private final BaseDocument doc;
    private final String mimeType;
    private long version = 0;
    private FileObject fileObject;
    private Project project;

    CoverageHighlightsContainer(Document document) {
        if (document instanceof BaseDocument) {
            this.doc = (BaseDocument) document;
        } else {
            this.doc = null;
        }
        this.mimeType = (String) document.getProperty("mimeType");
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        CoverageManagerImpl manager = CoverageManagerImpl.getInstance();
        if (doc == null || manager == null || !manager.isEnabled(mimeType)) {
            return HighlightsSequence.EMPTY;
        }
        synchronized (this) {
            if (fileObject == null) {
                fileObject = GsfUtilities.findFileObject(doc);
                if (fileObject != null) {
                    project = FileOwnerQuery.getOwner(fileObject);
                } else {
                    project = null;
                }

                if (fileObject == null || project == null) {
                    return HighlightsSequence.EMPTY;
                }
            }
        }

        FileCoverageDetails details = manager.getDetails(project, fileObject, doc);
        if (details == null) {
            return HighlightsSequence.EMPTY;
        }

        return new Highlights(0, startOffset, endOffset, details);
    }

//    public void updatedCoveredLines(FileObject fo, FileCoverageDetails details) {
//        //fireHighlightsChange(evt.affectedStartOffset(), evt.affectedEndOffset());
//        fireHighlightsChange(0, doc.getLength());
//    }

    private static Color getColoring(FontColorSettings fcs, String tokenName) {
        AttributeSet as = fcs.getTokenFontColors(tokenName);
        if (as != null) {
            return (Color) as.getAttribute(StyleConstants.Background); //NOI18N
        }
        return null;
    }

    void refresh() {
        fireHighlightsChange(0, doc.getLength());
    }

    private class Highlights implements HighlightsSequence {
        private final List<Position> positions;
        private final List<CoverageType> types;
        private final long version;
        private final int startOffsetBoundary;
        private final int endOffsetBoundary;
        private int startOffset;
        private int endOffset;
        private AttributeSet attributeSet;
        private boolean finished = false;
        private int index;

        private Highlights(long version, int startOffset, int endOffset, FileCoverageDetails details) {
            this.version = version;
            this.startOffsetBoundary = startOffset;
            this.endOffsetBoundary = endOffset;

            positions = new ArrayList<Position>();
            types = new ArrayList<CoverageType>();
            for (int lineno = 0, maxLines = details.getLineCount(); lineno < maxLines; lineno++) {
                CoverageType type = details.getType(lineno);
                if (type == CoverageType.COVERED || type == CoverageType.INFERRED || type == CoverageType.NOT_COVERED) {
                    try {
                        int offset = Utilities.getRowStartFromLineOffset(doc, lineno);
                        Position pos = doc.createPosition(offset);
                        positions.add(pos);
                        types.add(type);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

        }

        private boolean _moveNext() {
            for (; index < positions.size(); index++) {
                Position pos = positions.get(index);
                int offset = pos.getOffset();
                if (offset >= startOffsetBoundary && offset <= endOffsetBoundary) {
                    startOffset = offset;
                    try {
                        endOffset = Utilities.getRowEnd(doc, offset);
                        if (endOffset < doc.getLength()) {
                            endOffset++; // Include end of line
                        }
                        CoverageType type = types.get(index);
                        switch (type) {
                            case COVERED:
                                attributeSet = covered;
                                break;
                            case NOT_COVERED:
                                attributeSet = uncovered;
                                break;
                            case INFERRED:
                                attributeSet = inferred;
                                break;
                            default:
                                throw new IllegalArgumentException();
                        }
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    index++;
                    return true;
                }
            }

            return false;
        }

        public boolean moveNext() {
            synchronized (CoverageHighlightsContainer.this) {
                if (checkVersion()) {
                    if (_moveNext()) {
                        return true;
                    }
                }
            }

            finished = true;
            return false;
        }

        public int getStartOffset() {
            synchronized (CoverageHighlightsContainer.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    return startOffset;
                }
            }
        }

        public int getEndOffset() {
            synchronized (CoverageHighlightsContainer.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    return endOffset;
                }
            }
        }

        public AttributeSet getAttributes() {
            synchronized (CoverageHighlightsContainer.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    return attributeSet;
                }
            }
        }

        private boolean checkVersion() {
            return this.version == CoverageHighlightsContainer.this.version;
        }
    }
}
