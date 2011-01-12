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


/**
 * Immutable pair of start and end offset.
 *
 * @author Miloslav Metelka
 */

public final class OffsetRegion {
    
    private static final OffsetRegion EMPTY = new OffsetRegion(Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    public static OffsetRegion empty() {
        return EMPTY;
    }
    
    public static OffsetRegion create(int startOffset, int endOffset) {
        return new OffsetRegion(startOffset, endOffset);
    }

    private static void checkBounds(int startOffset, int endOffset) {
        if (startOffset > endOffset) {
            throw new IllegalArgumentException("startOffset=" + startOffset + " > endOffset=" + endOffset); // NOI18N
        }
    }

    private final int startOffset;
    
    private final int endOffset;
    
    private OffsetRegion(int startOffset, int endOffset) {
        checkBounds(startOffset, endOffset);
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
    
    public int startOffset() {
        return startOffset;
    }
    
    public int endOffset() {
        return endOffset;
    }
    
    public int length() {
        return endOffset - startOffset;
    }
    
    public boolean isEmpty() {
        return (startOffset == endOffset);
    }

    /**
     * Return union of this region with the given region.
     * <br/>
     * If the given region is empty then return "this" region.
     * 
     * @param startOffset region's start offset
     * @param endOffset region's end offset.
     * @return new region instance which is union of the given bounds
     */
    public OffsetRegion union(int startOffset, int endOffset) {
        if (startOffset == endOffset) {
            return this;
        }
        if (isEmpty()) {
            return new OffsetRegion(startOffset, endOffset);
        }
        checkBounds(startOffset, endOffset);
        return unionImpl(startOffset, endOffset);
    }
    
    /**
     * Return union of this region with the given region.
     * <br/>
     * If the given region is empty then return "this" region.
     * 
     * @param region region to union with.
     * @return new region instance which is union of the given bounds
     */
    public OffsetRegion union(OffsetRegion region) {
        if (region.isEmpty()) {
            return this;
        }
        if (isEmpty()) {
            return region;
        }
        return unionImpl(region.startOffset(), region.endOffset());
    }

    private OffsetRegion unionImpl(int startOffset, int endOffset) {
        if (startOffset >= this.startOffset) {
            if (endOffset <= this.endOffset) {
                return this; // Included
            } else { // endOffset > this.endOffset
                return new OffsetRegion(this.startOffset, endOffset);
            }
        } else { // startOffset < this.startOffset
            return new OffsetRegion(startOffset, Math.max(endOffset, this.endOffset));
        }
    }

    public OffsetRegion intersection(int startOffset, int endOffset) {
        checkBounds(startOffset, endOffset);
        return intersectionImpl(startOffset, endOffset);
    }

    private OffsetRegion intersectionImpl(int startOffset, int endOffset) {
        startOffset = Math.max(startOffset, this.startOffset);
        endOffset = Math.min(endOffset, this.endOffset);
        if (startOffset >= endOffset) {
            return empty();
        }
        if (startOffset == this.startOffset && endOffset == this.endOffset) {
            return this;
        }
        return new OffsetRegion(startOffset, endOffset);
    }

    @Override
    public String toString() {
        return isEmpty()
                ? "<E:" + startOffset + ">" // NOI18N
                : "<" + startOffset + "," + endOffset + ">"; // NOI18N
    }

}
