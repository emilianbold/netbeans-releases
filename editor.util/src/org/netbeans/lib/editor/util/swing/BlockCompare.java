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

package org.netbeans.lib.editor.util.swing;

/**
 * Comparing of position block X to position block Y.
 * For example {@link #contains()} means that block X fully contains Y.
 *
 * @author Miloslav Metelka
 * @since 1.6
 */

public final class BlockCompare {

    /**
     * Compare block X and Y.
     *
     * @param xStartOffset start offset of block X.
     * @param xEndOffset end offset of block X must be &gt;=xStartOffset.
     * @param yStartOffset start offset of block Y.
     * @param yEndOffset end offset of block Y must be &gt;=yStartOffset.
     * @return instance of block comparing of X to Y.
     */
    public static BlockCompare get(int xStartOffset, int xEndOffset, int yStartOffset, int yEndOffset) {
        return new BlockCompare(resolve(xStartOffset, xEndOffset, yStartOffset, yEndOffset));
    }

    private static final int BEFORE = 1;
    private static final int AFTER = (BEFORE << 1);
    private static final int INSIDE = (AFTER << 1);
    private static final int CONTAINS = (INSIDE << 1);
    private static final int OVERLAP_START = (CONTAINS << 1);
    private static final int OVERLAP_END = (OVERLAP_START << 1);
    private static final int EMPTY_X = (OVERLAP_END << 1);
    private static final int EMPTY_Y = (EMPTY_X << 1);

    private final int value;

    private BlockCompare(int value) {
        this.value = value;
    }

    /**
     * Check if block X is before block Y.
     *
     * @return true if end offset of block X is &lt;= start offset of block Y.
     */
    public boolean before() {
        return (value & BEFORE) != 0;
    }

    /**
     * Check if block X is after block Y.
     *
     * @return true if start offset of block X is &gt;= end offset of block Y.
     */
    public boolean after() {
        return (value & AFTER) != 0;
    }

    /**
     * Check if block X is contained in block Y.
     *
     * @return true if block X is contained inside block Y.
     */
    public boolean inside() {
        return (value & INSIDE) != 0;
    }

    /**
     * Check if X is inside Y but X and Y are not equal.
     *
     * @return true if X is inside Y but they are not equal.
     */
    public boolean insideStrict() {
        return (value & (CONTAINS | INSIDE)) == INSIDE;
    }

    /**
     * Check if block X contains block Y.
     *
     * @return true if block X contains block Y.
     */
    public boolean contains() {
        return (value & CONTAINS) != 0;
    }

    /**
     * Check if X contains Y but X and Y are not equal.
     *
     * @return true if X contains Y but they are not equal.
     */
    public boolean containsStrict() {
        return (value & (CONTAINS | INSIDE)) == CONTAINS;
    }

    /**
     * Check if block X has the same boundaries as block Y.
     *
     * @return true if start and end offsets of block X are equal to start and end offsets of block Y.
     */
    public boolean equal() {
        return (value & (CONTAINS | INSIDE)) == (CONTAINS | INSIDE);
    }

    /**
     * Check if there's an overlap at start or end.
     *
     * @return true <code>overlapStart() || overlapEnd()</code>.
     */
    public boolean overlap() {
        return (value & (OVERLAP_START | OVERLAP_END)) != 0;
    }

    /**
     * Check if block X overlaps block Y at its begining.
     *
     * @return true if start offset of block X is before start offset of block Y
     *  and end offset of block X is inside block Y.
    */
    public boolean overlapStart() {
        return ((value & OVERLAP_START) != 0);
    }

    /**
     * Check if block X overlaps block Y at its end.
     *
     * @return true if start offset of block X is inside block Y
     *  and end offset of block X is above end of block Y.
    */
    public boolean overlapEnd() {
        return ((value & OVERLAP_END) != 0);
    }

    /**
     * Check if block X is empty.
     *
     * @return true if start offset of block X equals to end offset of block X.
     */
    public boolean emptyX() {
        return (value & EMPTY_X) != 0;
    }

    /**
     * Check if block Y is empty.
     *
     * @return true if start offset of block Y equals to end offset of block Y.
     */
    public boolean emptyY() {
        return (value & EMPTY_Y) != 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        appendBit(sb, BEFORE, "BEFORE");
        appendBit(sb, AFTER, "AFTER");
        appendBit(sb, CONTAINS, "CONTAINS");
        appendBit(sb, INSIDE, "INSIDE");
        appendBit(sb, OVERLAP_START, "OVERLAP_START");
        appendBit(sb, OVERLAP_END, "OVERLAP_END");
        appendBit(sb, EMPTY_X, "EMPTY_X");
        appendBit(sb, EMPTY_Y, "EMPTY_Y");
        return sb.toString();
    }

    private void appendBit(StringBuilder sb, int bitValue, String bitText) {
        if ((value & bitValue) != 0) {
            if (sb.length() != 0)
                sb.append('|');
            sb.append(bitText);
        }
    }

    private static int resolve(int xStartOffset, int xEndOffset, int yStartOffset, int yEndOffset) {
        assert (xStartOffset <= xEndOffset) : "xStartOffset=" + xStartOffset +
                " > xEndOffset=" + xEndOffset;
        assert (yStartOffset <= yEndOffset) : "yStartOffset=" + yStartOffset +
                " > yEndOffset=" + yEndOffset;
        int value;
        if (xEndOffset < yStartOffset) {
            value = BEFORE;
            if (xStartOffset == xEndOffset)
                value |= EMPTY_X;
            if (yStartOffset == yEndOffset)
                value |= EMPTY_Y;

        } else if (xEndOffset == yStartOffset) { // X right-before Y
            if (xStartOffset == xEndOffset) { // X empty && right-before Y
                if (yStartOffset == yEndOffset) { // X and Y empty
                    value = EMPTY_X | EMPTY_Y | BEFORE | AFTER | INSIDE | CONTAINS;
                } else { // X empty && right-before Y; Y non-empty
                    value = EMPTY_X | BEFORE | CONTAINS;
                }
            } else { // X non-empty; right-before Y
                if (yStartOffset == yEndOffset) { // X non-empty && right-before Y; Y empty
                    value = EMPTY_Y | BEFORE | INSIDE;
                } else { // X non-empty && right-before Y; Y non-empty
                    value = BEFORE;
                }
            }

        } else { // xEndOffset > yStartOffset
            if (xStartOffset > yEndOffset) {
                value = AFTER;
                if (xStartOffset == xEndOffset) {
                    value |= EMPTY_X;
                }
                if (yStartOffset == yEndOffset) {
                    value |= EMPTY_Y;
                }

            } else if (xStartOffset == yEndOffset) { // X right-after Y
                if (xStartOffset == xEndOffset) { // X empty && right-after Y
                    if (yStartOffset == yEndOffset) { // X and Y empty
                        value = EMPTY_X | EMPTY_Y | BEFORE | AFTER | INSIDE | CONTAINS;
                    } else { // X empty && right-after Y; Y non-empty
                        value = EMPTY_X | AFTER | CONTAINS;
                    }
                } else { // X non-empty && right-after Y
                    if (yStartOffset == yEndOffset) { // X and Y empty
                        value = EMPTY_Y | BEFORE | AFTER | INSIDE | CONTAINS;
                    } else { // X non-empty && right-after Y; Y non-empty
                        value = AFTER;
                    }
                }

            } else { // xStartOffset < yEndOffset && xEndOffset > yStartOffset
                if (xStartOffset < yStartOffset) {
                    if (xEndOffset < yEndOffset) {
                        value = OVERLAP_START;
                    } else { // xEndOffset >= yEndOffset
                        value = CONTAINS;
                    }
                } else if (xStartOffset == yStartOffset) {
                    if (xEndOffset < yEndOffset) {
                        value = INSIDE;
                    } else if (xEndOffset == yEndOffset) {
                        value = INSIDE | CONTAINS;
                    } else { // xEndOffset > yEndOffset
                        value = CONTAINS;
                    }
                } else { // xStartOffset > yStartOffset
                    if (xEndOffset <= yEndOffset) {
                        value = INSIDE;
                    } else { // xEndOffset > yEndOffset
                        value = OVERLAP_END;
                    }
                }
                if (xStartOffset == xEndOffset) {
                    value |= EMPTY_X;
                }
                if (yStartOffset == yEndOffset) {
                    value |= EMPTY_Y;
                }
            }
        }
        return value;
    }

}
