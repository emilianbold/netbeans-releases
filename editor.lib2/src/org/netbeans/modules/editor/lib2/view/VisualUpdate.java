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

import java.awt.Rectangle;
import java.awt.Shape;

public final class VisualUpdate<V extends EditorView> {

    private static final int MAJOR_SPAN_CHANGED = 1;

    private static final int MINOR_SPAN_CHANGED = 2;

    private static final int WIDTH_CHANGED = 4;

    private static final int HEIGHT_CHANGED = 8;

    private static final int TABS_CHANGED = 16;
    
    private static final int IN_CACHE = 32; // Whether line is in text layout cache or not

    private static final int NEW_LINE_VIEW_CHANGED = 64; // Whether line is in text layout cache or not
    
    private final EditorBoxView<V> boxView;

    Rectangle repaintBounds;

    private int statusBits;

    int visualIndex;

    int endVisualIndex;

    double visualOffset;

    /** Visual offset corresponding to endVisualIndex (newly computed). */
    double endVisualOffset;
    
    public VisualUpdate(EditorBoxView<V> boxView) {
        this.boxView = boxView;
    }
    
    public EditorBoxView<V> getView() {
        return boxView;
    }

    public Rectangle getRepaintBounds() {
        return repaintBounds;
    }

    public boolean isPreferenceChanged() {
        return (statusBits & (MAJOR_SPAN_CHANGED | MINOR_SPAN_CHANGED |
                WIDTH_CHANGED | HEIGHT_CHANGED | TABS_CHANGED)) != 0;
    }

    public boolean isMajorChildrenSpanChanged() {
        return (statusBits & MAJOR_SPAN_CHANGED) != 0;
    }

    void markMajorChildrenSpanChanged() {
        statusBits |= MAJOR_SPAN_CHANGED;
    }

    public boolean isMinorChildrenSpanChanged() {
        return (statusBits & MINOR_SPAN_CHANGED) != 0;
    }

    void markMinorChildrenSpanChanged() {
        statusBits |= MINOR_SPAN_CHANGED;
    }

    public boolean isWidthChanged() {
        return (statusBits & WIDTH_CHANGED) != 0;
    }

    void markWidthChanged() {
        statusBits |= WIDTH_CHANGED;
    }

    public boolean isHeightChanged() {
        return (statusBits & HEIGHT_CHANGED) != 0;
    }

    void markHeightChanged() {
        statusBits |= HEIGHT_CHANGED;
    }

    public boolean isTabsChanged() {
        return (statusBits & TABS_CHANGED) != 0;
    }

    void markTabsChanged() {
        statusBits |= TABS_CHANGED;
    }
    
    public boolean isInCache() {
        return (statusBits & IN_CACHE) != 0;
    }
    
    void markInCache() {
        statusBits |= IN_CACHE;
    }

    public boolean isNewLineViewChanged() {
        return (statusBits & NEW_LINE_VIEW_CHANGED) != 0;
    }

    void markNewLineViewChanged() {
        statusBits |= NEW_LINE_VIEW_CHANGED;
    }

    /**
     * Span that needs to be repainted because it was modified.
     */
    double changedMajorSpan() {
        return endVisualOffset - visualOffset;
    }

    void updateSpansAndLayout(Shape alloc) {
        boxView.updateSpansAndLayout(this, alloc);
    }

    public String toString() {
        return "[" + visualIndex + "," + endVisualIndex + "]" + // NOI18N
                ", " + ViewUtils.toStringAxis(boxView.getMajorAxis()) + // NOI18N
                "[" + ViewUtils.toStringPrec1(visualOffset) + "," + // NOI18N
                ViewUtils.toStringPrec1(endVisualOffset) + // NOI18N
                "], repaintBnds=" + repaintBounds + // NOI18N
                "\n    major/minorChSpanChged=" + isMajorChildrenSpanChanged() + // NOI18N
                "/" + isMinorChildrenSpanChanged() + // NOI18N
                ", width/heightChged=" + isWidthChanged() + // NOI18N
                "/" + isHeightChanged() + // NOI18N
                ", tabsChged=" + isTabsChanged(); // NOI18N
    }

}
