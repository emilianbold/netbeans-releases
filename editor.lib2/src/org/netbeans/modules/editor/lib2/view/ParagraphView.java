/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Shape;
import java.util.logging.Logger;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;


/**
 * View of a visual line.
 * <br/>
 * It is capable to do a word-wrapping (see {@link ParagraphWrapView}.
 * <br/>
 * It is defined over an element either line element
 * or an artificial element that spans multiple lines (e.g. in case of code folding).
 * 
 * @author Miloslav Metelka
 */

public class ParagraphView extends EditorBoxView<EditorView> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ParagraphView.level=FINE
    private static final Logger LOG = Logger.getLogger(ParagraphView.class.getName());

    private Position startPos; // 40 + 4 = 44 bytes

    private int length; // 44 + 4 = 48 bytes

    public ParagraphView(Position startPos) {
        super(null);
        this.startPos = startPos;
    }

    @Override
    public int getMajorAxis() {
        return View.X_AXIS;
    }

    @Override
    public int getStartOffset() {
        return startPos.getOffset();
    }

    @Override
    public int getEndOffset() {
        return getStartOffset() + getLength();
    }

    @Override
    public AttributeSet getAttributes() {
        return null;
    }

    @Override
    public int getLength() { // Total length of contained child views
        return length;
    }

    @Override
    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public int getRawOffset() {
        return -1;
    }

    @Override
    public void setRawOffset(int rawOffset) {
        throw new IllegalStateException("setRawOffset() must not be called on ParagraphView."); // NOI18N
    }
    
    @Override
    protected EditorBoxViewChildren<EditorView> createChildren(int capacity) {
        return new ParagraphViewChildren(capacity);
    }

    DocumentView getDocumentView() {
        return (DocumentView) getParent();
    }

    @Override
    public void setParent(View parent) {
        super.setParent(parent);
        // Set minor axis span to default line height here when children
        // are not initialized yet since this way there should be no need
        // to notify parent about preferenceChange later (unless there's e.g. a word wrap).
        if (parent instanceof EditorBoxView) {
            DocumentView documentView = getDocumentView();
            if (documentView != null && getMinorAxisSpan() == 0f) { // Not inited yet
                setMinorAxisSpan(documentView.getDefaultLineHeight());
            }
        }
    }
    
    /**
     * Find next visual position in Y direction.
     * In case of no line-wrap the method should return -1 for a given valid offset.
     * and a valid offset when -1 is given as parameter.
     * @param offset offset inside line or -1 to "enter" a line at the given x.
     * @param x x-position corresponding to magic caret position.
     */
    int getNextVisualPositionY(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet, double x) {
        return ((ParagraphViewChildren)children).getNextVisualPositionY(this, offset, bias, alloc, direction, biasRet, x);
    }
    
    /**
     * Find next visual position in Y direction.
     * In case of no line-wrap the method should return -1 for a given valid offset.
     * and a valid offset when -1 is given as parameter.
     * @param offset offset inside line or -1 to "enter" a line at the given x.
     */
    int getNextVisualPositionX(int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet) {
        switch (direction) {
            case SwingConstants.EAST:
            case SwingConstants.WEST:
                int index = getViewIndex(offset);
                int viewCount = getViewCount(); // Should always be >0
                int increment = (direction == SwingConstants.EAST) ? 1 : -1;
                int retOffset = -1;
                for (; retOffset == -1 && index >= 0 && index < viewCount; index += increment) {
                    EditorView view = getEditorViewChildrenValid(index); // Ensure valid children
                    Shape viewAlloc = getChildAllocation(index, alloc);
                    retOffset = view.getNextVisualPositionFromChecked(offset, bias, viewAlloc, direction, biasRet);
                    if (retOffset == -1) {
                        offset = -1; // Continue by entering the paragraph from outside
                    }
                }
                return retOffset;

            case SwingConstants.NORTH: // Should be handled elsewhere
            case SwingConstants.SOUTH: // Should be handled elsewhere
                throw new IllegalStateException("Not intended to handle EAST and WEST directions"); // NOI18N
            default:
                throw new IllegalArgumentException("Bad direction: " + direction); // NOI18N
        }
    }

    @Override
    protected void releaseChildren() {
        releaseTextLayouts();
        super.releaseChildren();
    }
    
    void releaseTextLayouts() {
        DocumentView docView = getDocumentView();
        if (docView != null) { // Only when actively used by docView
            int viewCount = getViewCount(); // would be 0 for children == null
            for (int i = 0; i < viewCount; i++) {
                EditorView view = getEditorView(i);
                if (view instanceof HighlightsView) {
                    ((HighlightsView)view).setLayout(null);
                }
            }
            docView.getTextLayoutCache().remove(this);
        }
    }
    
    void initTextLayouts() {
        // Init text layouts in children
        assert (children != null) : "Null children"; // NOI18N
        ViewStats.incrementInitTextLayouts();
        updateViews(0, getViewCount(), null);
    }

    void recomputeLayout() {
        if (children != null) {
            ((ParagraphViewChildren) children).recomputeLayout(this);
        }
    }

    @Override
    public String findIntegrityError() {
        String err = super.findIntegrityError();
        if (err != null) {
            return err;
        }
        if (children != null) {
            int childrenLength = children.getLength();
            if (getLength() != childrenLength) {
                return "length=" + getLength() + " != childrenLength=" + childrenLength; // NOI18N
            }
            // Check layouts integrity
            err = HighlightsViewUtils.findLayoutIntegrityError(this);
        }
        return err;
    }

    @Override
    protected String getDumpName() {
        return "PV";
    }

}
