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

import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Position;
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

public class ParagraphView extends EditorBoxView {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ParagraphView.level=FINE
    private static final Logger LOG = Logger.getLogger(ParagraphView.class.getName());

    private Position startPos; // 40 + 4 = 44 bytes

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
        return (children != null) ? children.getLength() : 0;
    }

    @Override
    public boolean setLength(int length) {
        // Ignore the length setting
        return true; // ParagraphView may grow
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
    protected EditorBoxViewChildren createChildren(int capacity) {
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
            if (documentView != null) {
                setMinorAxisSpan(documentView.getDefaultLineHeight());
            }
        }
    }

    void recomputeSpans() {
        ((ParagraphViewChildren)children).recomputeSpans(this);
    }

    @Override
    protected String getDumpName() {
        return "PV";
    }

}
