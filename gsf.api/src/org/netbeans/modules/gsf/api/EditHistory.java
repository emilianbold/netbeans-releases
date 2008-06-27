/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.gsf.api;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * <p>The EditHistory object contains information about a set of edits that
 * have occurred in a given Document recently. This is typically used to
 * support incremental parsing. The IDE infrastructure will hand a parser
 * its old parse tree along with an EditHistory. The EditHistory represents
 * edits made since the previous parse.  If the parser supports incremental
 * parsing, it can use the edit history to determine if it can parse just
 * a sub-portion of the buffer (for example, just the current method body)
 * and therefore do a lot less work. More importantly, it can record this
 * information in its ParserResult, and features that are driven off of the
 * parse tree can do a lot less work.
 * </p>
 * <p>
 * The EditHistory tracks edits accurately, so you can use the
 * {@link #convertOldToNew(int)} method to translate a pre-edits offsets
 * to a post-edits offsets.  However, the EditHistory maintains a couple
 * of attributes that usually more interesting:
 * <ol>
 * <li> The offset</li>
 * <li> The old size</li>
 * <li> The new size</li>
 * </ol>
 * These three parameters indicate that in the old document, the text between
 * <code>offset</code> and <code>offset+oldSize</code> has been modified,
 * and after the edits, this region corresponds to
 * <code>offset</code> to <code>offset+newSize</code>. Put another way,
 * all document positions below <code>offset</code> are unaffected by the edits,
 * and all document positions above <code>offset+oldSize</code> are uniformly
 * shifted up by a delta of <code>newSize-oldSize</code> (which can be negative,
 * when more text was deleted than added).
 * </p>
 * <p>
 * Here's how this works. Consider the following document:
 * <pre>
 *   Offsets:     0123456789ABCDEF
 *   Document:    Hello World!
 * </pre>
 * Let's apply 3 edits: removing the "e" character", the "r" character,
 * and inserting an extra space character in the middle. We now end up with:
 * <pre>
 *   Offsets:     0123456789ABCDEF
 *   Old Doc:     Hello World!
 *   New Doc:     Hllo  Wold!
 * </pre>
 * As you can see, some characters in the middle here have been edited.
 * The affected block is shown in bold as follows:
 * <pre>
 *   Offsets:     0123456789ABCDEF
 *   Old Doc:     H<b>ello Wor</b>ld!
 *   New Doc:     H<b>llo  Wo</b>ld!
 * </pre>
 * Therefore, in this document, the affected range begins at offset 1,
 * and in the original document the affected block had size 8, and in
 * the edited document the affected block size is 7. The delta is -1.
 * Incremental parsing clients can use this to traverse their data, and
 * seeing if it is in the affected region. If not, they can simply adjust
 * their offsets (by adding delta for offsets above the affected region,
 * and nothing for offsets below the affected region).
 * </p>
 *
 * @author Tor Norbye
 */
public class EditHistory implements DocumentListener {
    private int start = -1;
    private int oldEnd = -1;
    private int newEnd = -1;
    private List<Edit> edits = new ArrayList<Edit>(4);
    private int delta = 0;

    /**
     * The beginning position of the damaged region.
     */
    public int getStart() {
        return start;
    }

    /**
     * Check if the position is in the damaged region (inclusive)
     * @param pos The position
     * @return True iff the position is inside the damaged region
     */
    public boolean isInDamagedRegion(int pos) {
        if (start == -1) {
            return false;
        }
        return (pos >= start && pos <= newEnd);
    }

    /**
     * Check if the range overlaps the damaged region (inclusive)
     * @param range The range
     * @return True iff the range overlaps the damaged region
     */
    public boolean isInDamagedRegion(OffsetRange range) {
        if (start == -1) {
            return false;
        }
        return range.getStart() < newEnd && range.getEnd() > start;
    }

    /**
     * The size of the edits. Could be negative (when more text has been
     * deleted than added). The key rule is that
     * <pre>
     *   oldText[i] = newText[i]   for i &lt; offset, and
     *   oldText[i] = newText[i+newSize]  for i &gt;= offset
     * </pre>
     */
    public int getNewSize() {
        return newEnd - start;
    }

    public int getOldEnd() {
        return oldEnd;
    }

    public int getNewEnd() {
        return newEnd;
    }

    public int getSizeDelta() {
        return delta;
    }

    /**
     * The old size of the region that was damaged. The first character
     * after offset+oldSize before the edits, corresponds to the character
     * at offset+newSize after the edits.
     */
    public int getOldSize() {
        return oldEnd - start;
    }

    /**
     * Convert a position before edits to a corresponding position after edits.
     * @param oldPos The position in the unedited document
     * @return The corresponding position after the edits
     */
    public int convertOldToNew(int oldPos) {
        if (start == -1 || oldPos <= start) {
            return oldPos;
        }

        if (oldPos >= oldEnd) {
            return oldPos+delta;
        }

        // Perform more accurate translation:
        // Apply individual edits (which will usually just involve a couple of operations)

        List<Edit> list = edits;
        int len = list.size();
        if (len == 0) {
            return oldPos;
        }
        for (int i = 0; i < len; i++) {
            Edit edit = list.get(i);
            if (oldPos > edit.offset) {
                if (edit.insert) {
                    oldPos += edit.len;
                } else if (oldPos < edit.offset+edit.len) {
                    oldPos = edit.offset;
                } else {
                    oldPos -= edit.len;
                }
            }
        }

        if (oldPos < 0) {
            oldPos = 0;
        }

        return oldPos;
    }

    /**
     * Convert a position post-edits to a corresponding position pre-edits
     * @param newPos The position in the edited document
     * @return The corresponding position prior to the edits
     */
    public int convertNewToOld(int newPos) {
        List<Edit> list = edits;
        int len = list.size();
        if (len == 0) {
            return newPos;
        }
        for (int i = len-1; i >= 0; i--) {
            Edit edit = list.get(i);
            if (edit.insert) {
                if (newPos > edit.offset) {
                    if (newPos < edit.offset+edit.len) {
                        // If it's anywhere INSIDE this block it was newly
                        // added by this insert - decide if I want to handle
                        // this differently.
                        newPos = edit.offset;
                    } else {
                        newPos -= edit.len;
                    }
                } // else: offset unaffected by the insert
            } else {
                // Remove
                if (newPos >= edit.offset) {
                    newPos += edit.len;
                }
            }
        }

        if (newPos < 0) {
            newPos = 0;
        }

        return newPos;
    }

    /**
     * Notify the EditHistory of a document edit (insert).
     */
    public void insertUpdate(DocumentEvent e) {

        int pos = e.getOffset();
        int length = e.getLength();

        // TODO - synchronize?
        edits.add(new Edit(pos, length, true));

        if (start == -1) {
            start = pos;
            oldEnd = pos;
            newEnd = pos+length;
            delta = length;
        } else {
            // Compute history backwards
            int original = convertNewToOld(pos);
            if (original > oldEnd) {
                oldEnd = original;
            }
            if (pos < start) {
                start = pos;
            }
            if (pos+length > newEnd) {
                newEnd = pos+length;
            } else {
                newEnd += length;
            }
            delta = getNewSize()-getOldSize();
        }
    }

    /**
     * Notify the EditHistory of a document edit (remove).
     */
    public void removeUpdate(DocumentEvent e) {
        int pos = e.getOffset();
        int length = e.getLength();

        // TODO - synchronize?
        edits.add(new Edit(pos, length, false));
        
        if (start == -1) {
            start = pos;
            oldEnd = pos+length;
            newEnd = pos;
            delta = -length;
        } else {
            // TODO
            int original = convertNewToOld(pos);
            if (original > oldEnd) {
                oldEnd = original;
            }

            if (pos < start) {
                start = pos;
            }

            if (pos > newEnd) {
                newEnd = pos;
            } else {
                newEnd -= length;
            }
            delta = getNewSize()-getOldSize();
        }
    }

    /**
     * Notify the EditHistory of a document edit (change). Attribute changes
     * are not tracked by the EditHistory.
     */
    public void changedUpdate(DocumentEvent e) {
    }

    @Override
    public String toString() {
        return "EditHistory(offset=" + start + ", oldSize=" + getOldSize() + ", newSize=" + getNewSize() + ", delta=" + delta + ")"; // NOI18N
    }

    /**
     * An Edit is a modification (insert/remove) we've been notified about from the document
     * since the last time we updated our "colorings" object.
     * The list of Edits lets me quickly compute the current position of an original
     * position in the "colorings" object. This is typically going to involve only a couple
     * of edits (since the colorings object is updated as soon as the user stops typing).
     * This is probably going to be more efficient than updating all the colorings offsets
     * every time the document is updated, since the colorings object can contain thousands
     * of ranges (e.g. for every highlight in the whole document) whereas asking for the
     * current positions is typically only done for the highlights visible on the screen.
     */
    private class Edit {
        public Edit(int offset, int len, boolean insert) {
            this.offset = offset;
            this.len = len;
            this.insert = insert;
        }

        int offset;
        int len;
        boolean insert; // true: insert, false: delete
    }
}
