/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.modules.editor.lib2.highlighting.CompoundAttributes;
import org.netbeans.modules.editor.lib2.highlighting.HighlightItem;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 * Special highlights sequence used for painting of individual views.
 * <br/>
 * It merges together highlights contained in views (as attributes) together
 * with extra painting highlights (from highlighting layers that do not change metrics).
 * <br/>
 * It "covers" even non-highlighted areas by returning null from {@link #getAttributes()}.
 * <br/>
 * The instance can only be used by a single thread.
 *
 * @author mmetelka
 */
class ViewPaintHighlights implements HighlightsSequence {

    /** All paint highlights in the area being painted. */
    private final HighlightsList paintHighlights;
    
    /** Current index in paint highlights. */
    private int phIndex;
    
    private int phStartOffset;
    
    private int phEndOffset;
    
    private AttributeSet phAttrs;
    
    private int viewEndOffset;
    
    /** Items of view's compound attributes. */
    private HighlightItem[] vaItems;
    
    /** Index of current compoundAttrs highlight. It's -1 for regular attrs or no attrs. */
    private int vaIndex;
    
    private int vaEndOffset;
    
    private AttributeSet vaAttrs;
    
    private int offsetDiff;
    
    private int hiStartOffset;
    
    private int hiEndOffset;
    
    private AttributeSet hiAttrs;

    ViewPaintHighlights(HighlightsList paintHighlights) {
        this.paintHighlights = paintHighlights;
        updatePH(0);
    }
    
    void reset(EditorView view, int shift) {
        assert (shift >= 0) : "shift=" + shift + " < 0"; // NOI18N
        int viewStartOffset = view.getStartOffset();
        viewEndOffset = viewStartOffset + view.getLength();
        AttributeSet attrs = view.getAttributes();
        int startOffset = viewStartOffset + shift;
        if (ViewUtils.isCompoundAttributes(attrs)) {
            CompoundAttributes cAttrs = (CompoundAttributes) attrs;
            offsetDiff = viewStartOffset - cAttrs.startOffset();
            vaItems = cAttrs.highlightItems();
            if (shift == 0) {
                vaIndex = 0;
            } else {
                vaIndex = findCAHIndex(startOffset);
            }
            HighlightItem cahItem = vaItems[vaIndex];
            vaEndOffset = cahItem.getEndOffset() + offsetDiff;
            vaAttrs = cahItem.getAttributes();
        } else { // Either regular or no attrs
            // offsetDiff will not be used
            vaItems = null;
            vaIndex = -1;
            vaEndOffset = viewEndOffset;
            if (attrs == null) {
                vaAttrs = null;
            } else { // regular attrs
                vaAttrs = attrs;
            }
        }
        // Update paint highlight if necessary
        if (startOffset < phStartOffset) { // Must go back
            updatePH(findPHIndex(startOffset));
        } else if (startOffset >= phEndOffset) { // Must fetch further
            // Should be able to fetch since it should not fetch beyond requested area size
            fetchNextPH();
            if (startOffset >= phEndOffset) {
                updatePH(findPHIndex(startOffset));
            }
        } // Within current PH
        hiStartOffset = hiEndOffset = startOffset;
    }
    
    @Override
    public boolean moveNext() {
        if (hiEndOffset >= viewEndOffset) {
            return false;
        }
        if (hiEndOffset >= phEndOffset) {
            fetchNextPH();
        }
        if (hiEndOffset >= vaEndOffset) {
            // Fetch next CAH
            vaIndex++;
            if (vaIndex >= vaItems.length) {
                return false;
            }
            HighlightItem hItem = vaItems[vaIndex];
            vaEndOffset = hItem.getEndOffset() + offsetDiff;
            vaAttrs = hItem.getAttributes();
        }
        // There will certainly be a next highlight
        hiStartOffset = hiEndOffset;
        // Decide whether paint highlight ends lower than compound attrs' one
        if (phEndOffset < vaEndOffset) {
            hiEndOffset = Math.min(phEndOffset, viewEndOffset);
        } else {
            hiEndOffset = vaEndOffset;
        }
        // Merge (possibly null) attrs (ph over cah)
        hiAttrs = vaAttrs;
        if (phAttrs != null) {
            hiAttrs = (hiAttrs != null) ? AttributesUtilities.createComposite(phAttrs, hiAttrs) : phAttrs;
        }
        return true;
    }

    @Override
    public int getStartOffset() {
        return hiStartOffset;
    }

    @Override
    public int getEndOffset() {
        return hiEndOffset;
    }

    @Override
    public AttributeSet getAttributes() {
        return hiAttrs;
    }

    private int findCAHIndex(int offset) {
        int low = 0;
        int high = vaItems.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1; // mid in the binary search
            int hEndOffset = vaItems[mid].getEndOffset() + offsetDiff;
            if (hEndOffset < offset) {
                low = mid + 1;
            } else if (hEndOffset > offset) {
                high = mid - 1;
            } else { // hEndOffset == offset
                low = mid + 1;
                break;
            }
        }
        return low;
    }

    private void updatePH(int index) {
        phIndex = index;
        phStartOffset = (phIndex > 0)
                ? paintHighlights.get(phIndex - 1).getEndOffset()
                : paintHighlights.startOffset();
        HighlightItem phItem = paintHighlights.get(phIndex);
        phEndOffset = phItem.getEndOffset();
        phAttrs = phItem.getAttributes();
    }

    private void fetchNextPH() {
        phStartOffset = phEndOffset;
        phIndex++;
        HighlightItem hItem = paintHighlights.get(phIndex);
        phEndOffset = hItem.getEndOffset();
        phAttrs = hItem.getAttributes();
    }
    
    private int findPHIndex(int offset) {
        int low = 0;
        int high = paintHighlights.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1; // mid in the binary search
            int hEndOffset = paintHighlights.get(mid).getEndOffset();
            if (hEndOffset < offset) {
                low = mid + 1;
            } else if (hEndOffset > offset) {
                high = mid - 1;
            } else { // hEndOffset == offset
                low = mid + 1;
                break;
            }
        }
        return low;
    }
    
}
