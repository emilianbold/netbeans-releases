/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.css2;

import java.util.Arrays;
import java.util.Comparator;

import org.openide.ErrorManager;
import org.w3c.dom.Element;


/**
 * Maintains a list of boxes, which can be sorted in z-order.
 * @author Tor Norbye
 * @todo Why is not standard <code>List</code> impl used.
 */
public class BoxList {
    private CssBox[] boxes;
    private int size = 0;

    /** Should the list be kept sorted? */
    private boolean keepSorted;

    /** Is the list already sorted? */
    private boolean isSorted;

    /** Field which indicates if we need to sort. This will
     * be set if the box list contains a box with a z index other
     * than "auto".
     */
    private boolean mustSort;

    /** Should we keep the parent indices on the box in sync with the
     * box list position? */
    private boolean syncParentIndices = true;

    /** Create a list of boxes. The initialSize parameter is a hint
     * as to how large to make the list, but more boxes than that
     * can be added to the lsit.
     */
    public BoxList(int initialSize) {
        if (initialSize < 1) {
            initialSize = 2;
        }

        boxes = new CssBox[initialSize];
    }

    /** Return the number of boxes in the list */
    public int size() {
        return size;
    }

    /** Set whether or not the box list should be sorted
     * according to the z-order attribute on boxes.
     * Default is false.
     */
    public void setKeepSorted(boolean keepSorted) {
        this.keepSorted = keepSorted;
    }

    /** Set whether the box list should keep the parent indices
     * for the boxes synced as the box list is manipulated.
     */
    public void setSyncParentIndices(boolean sync) {
        this.syncParentIndices = sync;
    }

    /** Return the box at the given position in the list.
     * When the list is sorted, the higher the index, the
     * further up in the stack / the closer the box will
     * appear (e.g. higher indices occlude lower indices when
     * their coordinates overlap.)
     */
    public CssBox get(int index) {
        if (keepSorted && !isSorted) {
            zsort();
        }

        //        if (!(index >= 0 && index < size)) {
        //            // Assertion check -- remove later
        //            System.out.println("Box List violation: index=" + index + " in box list of size " + size);
        //            System.out.println("Box list is: " + toString());
        //        }
//        assert (index >= 0) && (index < size) : "Invalid box index: " + index + ", size is " +
//        size + "; this list is " + toString(); // NOI18N
        // XXX #94677 Supress asserts, no real fix for flow layout issues.
        if (index < 0 || index >= size) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new IndexOutOfBoundsException("Index=" + index + ", expected to be in [0," + (size - 1) + "].")); // NOI18N
            return null;
        }

        return boxes[index];
    }

    /** Remove the given  box from the list and update parent indices.
     * Returns true if the box was deleted, false if it was not found.
     */
    public boolean remove(CssBox box) {
        isSorted = false;

        // If you remove the last box that has a z index it's
        // possible that we could clear the mustSort flag. However,
        // checking for that will cost, and is probably not worth the
        // occasional savings when this scenario occurs.
        int pos = 0;

        for (; pos < size; pos++) {
            if (boxes[pos] == box) {
                break;
            }
        }

        if (pos < size) {
            int numMoved = size - pos - 1;

            if (numMoved > 0) {
                System.arraycopy(boxes, pos + 1, boxes, pos, numMoved);
            }

            boxes[--size] = null; // Let gc do its work

            if (syncParentIndices) {
                for (int i = pos; i < size; i++) {
                    // Adjust parent indices
                    boxes[i].setParentIndex(i);
                }
            }
        } else {
            ErrorManager.getDefault().log("Didn't find box " + box +
                " in the box list - illegal remove call");

            return false;
        }

        if (syncParentIndices) {
            box.setParentIndex(-1);
        }

        return true;
    }

    /** Add a box to the list. This method will NOT preserve
     * the sortedness of the list, so if a sorted list is necessary,
     * the client must call sort again.  If both after and before are null,
     * the box will be appended.
     * @param after Add the box after the given box, if not null
     * @param before Add the box right before the given box, if not null
     * @todo Is this method unused?
     */
    public void add(CssBox box, CssBox after, CssBox before) {
        isSorted = false;

        if (keepSorted) {
            if (box.getElement() != null) {
                box.initializeZOrder();

                if (box.z != CssBox.AUTO) {
                    mustSort = true;
                }
            }
        }

        if ((after == null) && (before == null)) {
            // Simple case: just append
            ensureCapacity(size + 1);
            boxes[size++] = box;

            if (syncParentIndices) {
                box.setParentIndex(size - 1);
            }

            return;
        }

        int pos = 0;

        if (after != null) {
            for (; pos < size; pos++) {
                if (boxes[pos] == after) {
                    pos++;

                    break;
                }
            }
        }

        if (before != null) {
            for (; pos < size; pos++) {
                if (boxes[pos] == before) {
                    break;
                }
            }
        }

        if (pos < size) {
            // Insert at the given location
            ensureCapacity(size + 1);
            System.arraycopy(boxes, pos, boxes, pos + 1, size - pos);
            boxes[pos] = box;

            if (syncParentIndices) {
                box.setParentIndex(pos);
            }

            size++;

            if (syncParentIndices) {
                for (int i = pos + 1; i < size; i++) {
                    // Adjust parent indices
                    boxes[i].setParentIndex(i);
                }
            }
        } else {
            // XXX #111179 This assertion seems to be incorrect here.
            // Revise, does it mean there is some issue with the computation,
            // or the parameters are invalid? If former, fix the computation,
            // if latter, validate the parameters at the beginning.
//            // Add to end of the list
//            assert before == null;

            // Simple case: just append
            ensureCapacity(size + 1);
            boxes[size++] = box;

            if (syncParentIndices) {
                box.setParentIndex(size - 1);
            }

            return;
        }
    }

    // From java.util.ArrayList
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = boxes.length;

        if (minCapacity > oldCapacity) {
            Object[] oldData = boxes;
            int newCapacity = ((oldCapacity * 3) / 2) + 1;

            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }

            boxes = new CssBox[newCapacity];
            System.arraycopy(oldData, 0, boxes, 0, size);
        }
    }

    /** Sort the list in z order. */
    public void zsort() {
        if (!keepSorted || !mustSort || (boxes == null) || (size < 2)) {
            isSorted = true;

            return;
        }

        Arrays.sort(boxes, 0, size, new CssBoxComparator());

        if (syncParentIndices) {
            // Update indices
            for (int i = 0; i < size; i++) {
                boxes[i].setParentIndex(i);
            }
        }

        isSorted = true;
    }

    /** Truncate the boxlist to contain only the boxes up to and including
     * the given position.
     * @param pos The position of the last box to be left in the list. Must
     * be less than or equal to the size of the list.
     */
    public void truncate(int pos) {
        assert pos <= size;

        for (int i = pos + 1; i < size; i++) {
            boxes[i] = null; // let gc do its work
        }

        size = pos;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(", ");
            }

            CssBox box = get(i);
            String cls = box.getClass().getName();
            String boxName = cls.substring(cls.lastIndexOf('.') + 1);
            sb.append(boxName);
            sb.append(':');

            Element element = box.getElement();
            if (element != null) {
                sb.append(element.toString());
            } else if (box instanceof TextBox) {
                sb.append(((TextBox)box).getText());
            } else if (box instanceof SpaceBox) {
                sb.append(' ');
            } else {
                sb.append(box.toString());
            }
        }

        sb.append("}");

        return sb.toString();
    }
    
    private static class CssBoxComparator implements Comparator<CssBox> {
        public int compare(CssBox b1, CssBox b2) {
            if (b1.z == b2.z) {
                return 0;
            }

            if (b1.z == CssBox.AUTO) {
                return -1;
            } else if (b2.z == CssBox.AUTO) {
                return 1;
            } else {
                return b1.z - b2.z;
            }
        }
    } // End of CssBoxComparator.
    
}
