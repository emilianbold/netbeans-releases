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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.lib2.highlighting;

/**
 * The implementation of <code>AbstractOffsetGapList</code> with
 * <code>Offset</code> elements.
 * 
 * @author Vita Stejskal
 */
public final class OffsetGapList<E extends OffsetGapList.Offset> extends AbstractOffsetGapList<E> {
    
    /** Creates a new instance of SimpleOffsetGapList */
    public OffsetGapList() {
    }

    protected int elementRawOffset(E elem) {
        return elem.getRawOffset();
    }

    protected void setElementRawOffset(E elem, int rawOffset) {
        elem.setRawOffset(rawOffset);
    }

    protected int attachElement(E elem) {
        return elem.attach(this);
    }

    protected void detachElement(E elem) {
        elem.detach(this);
    }

    protected E getAttachedElement(Object o) {
        if ((o instanceof Offset) && ((Offset) o).checkOwner(this)) {
            @SuppressWarnings("unchecked") //NOI18N
            E element = (E) o;
            return element; 
        } else {
            return null;
        }
    }

    /**
     * An offset gap list element. The <code>OffsetGapList</code> can accomodate
     * either instances of this class or any of its subclass.
     */
    public static class Offset {
        
        private int originalOrRawOffset;
        private OffsetGapList list;
        
        /**
         * Creates a new <code>Offset</code> object and sets its original offset
         * to the value passed in.
         * 
         * @param offset The original offset of this <code>Offset</code> object.
         */
        public Offset(int offset) {
            this.originalOrRawOffset = offset;
        }

        /**
         * Gets the offset of this <code>Offset</code> object. The offset is
         * either the original offset passed to the constructor if this <code>Offset</code>
         * instance has not been attached to a list yet or it is the real
         * offset of this instance, which reflects all offset updates in the list
         * (i.e. it gets updated when {@link AbstractOffsetGapList#defaultInsertUpdate} or
         * {@link AbstractOffsetGapList#defaultRemoveUpdate} is called).
         * 
         * @return The offset of this <code>Offset</code> instance.
         */
        public final int getOffset() {
            if (list == null) {
                return originalOrRawOffset;
            } else {
                return list.raw2Offset(getRawOffset());
            }
        }
        
        private int attach(OffsetGapList list) {
            assert this.list == null : "Offset instances can only be added to one OffsetGapList."; //NOI18N
            this.list = list;
            return originalOrRawOffset;
        }

        private void detach(OffsetGapList list) {
            assert this.list == list : "Can't detach from a foreign list."; //NOI18N
            this.list = null;
        }
        
        private boolean checkOwner(OffsetGapList list) {
            return this.list == list;
        }
        
        private int getRawOffset() {
            return originalOrRawOffset;
        }
        
        private void setRawOffset(int rawOffset) {
            this.originalOrRawOffset = rawOffset;
        }
    } // End of Offset class
}
