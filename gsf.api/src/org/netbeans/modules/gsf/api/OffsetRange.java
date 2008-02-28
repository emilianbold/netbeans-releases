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
package org.netbeans.modules.gsf.api;


/**
 * An offset range provides a range (start, end) pair of offsets
 * that indicate a range in a character buffer. The range represented
 * is {@code [start,end>}, which means that the range includes the
 * character at index=start, and ends right before the character at end.
 * Put yet another way, the starting offset is inclusive, and the ending
 * offset is exclusive.
 *
 * @author Tor Norbye
 */
public class OffsetRange {
    public static final OffsetRange NONE = new OffsetRange(0, 0);
    private final int start;
    private final int end;

    /** Creates a new instance of OffsetRange */
    public OffsetRange(int start, int end) {
        assert start >= 0;
        assert end >= start;

        this.start = start;
        this.end = end;
    }

    /** Get the start offset of offset range */
    public int getStart() {
        return start;
    }

    /** Get the end offset of offset range */
    public int getEnd() {
        return end;
    }
    
    /** Get the length of the offset range */
    public int getLength() {
        return getEnd()-getStart();
    }

    @Override
    public String toString() {
        if (this == NONE) {
            return "OffsetRange[NONE]";
        } else {
            return "OffsetRange[" + start + "," + end + ">"; // NOI18N
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (getClass() != o.getClass()) {
            return false;
        }

        final OffsetRange test = (OffsetRange)o;

        if (this.getStart() != test.getStart()) {
            return false;
        }

        if (this.getEnd() != test.getEnd()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = (23 * hash) + this.getStart();
        hash = (23 * hash) + this.getEnd();

        return hash;
    }

    /** Return true iff the given offset is within the bounds (or at the bounds) of the range */
    public boolean containsInclusive(int offset) {
        if (this == NONE) {
            return false;
        }

        return (offset >= getStart()) && (offset <= getEnd());
    }
}
