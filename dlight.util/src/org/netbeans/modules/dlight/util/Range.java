/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.util;

import java.util.LinkedList;
import java.util.List;

/**
 * Range of numeric values.
 *
 * @param <T> number class
 *
 * @author Alexey Vladykin
 */
public final class Range<T extends Number & Comparable<? super T>> {

    public static final String STRING_DELIMITER = ".."; // NOI18N

    private final T start;
    private final T end;

    /**
     * Creates a new range.
     * If both are non-null, then <code>start</code> must be less
     * than or equal to <code>end</code>.
     *
     * @param start  range start; <code>null</code> if range is unlimited
     * @param end  range end; <code>null</code> if range is unlimited
     * @throws IllegalArgumentException if mentioned constraints are violated
     */
    public Range(T start, T end) {
        if (start != null && end != null && 0 < start.compareTo(end)) {
            throw new IllegalArgumentException(start + " > " + end); // NOI18N
        }
        this.start = start;
        this.end = end;
    }

    /**
     * @return range start, may be <code>null</code>
     */
    public T getStart() {
        return start;
    }

    /**
     * @return range end, may be <code>null</code>
     */
    public T getEnd() {
        return end;
    }

    /**
     * Checks if this range contains given value.
     *
     * @param value  value to check
     * @return <code>true</code> if range contains <code>value</code>,
     *      <code>false</code> otherwise
     *
     * @throws NullPointerException if <code>value</code> is <code>null</code>
     */
    public boolean contains(T value) {
        return (start == null || start.longValue() <= value.longValue())
                && (end == null || value.longValue() <= end.longValue());
    }

    public boolean intersects(Range<T> timeInterval) {
        return !(timeInterval.end.longValue() < start.longValue() ||
                timeInterval.start.longValue() > end.longValue());
    }

    // Assume that list is sorted! (Use if union() guaranties this)
    // Function changes list's content!
    public void union(final List<Range<T>> intervals/*, final Range<T> interval*/) {
        int idx = 0;
        boolean consumed = false;
        final Range<T> interval = this;
        T start_time = interval.start;
        T end_time = interval.end;

        int size = intervals.size();
        while (idx < size && !consumed) {
            Range<T> i = intervals.get(idx);

            if (i.start.longValue() <= start_time.longValue() && i.end.longValue() >= end_time.longValue()) {
                return;
            }

            if (i.start.longValue() > end_time.longValue()) {
                intervals.add(idx, new Range<T>(start_time, end_time));
                return;
            }

            if (i.start.longValue() >= start_time.longValue() && i.end.longValue() <= end_time.longValue()) {
                intervals.remove(idx);
                size--;
                continue;
            }

            if (i.end.longValue() > end_time.longValue()) {
                intervals.set(idx, new Range<T>(start_time, i.end));
                return;
            }

            if (i.end.longValue() < start_time.longValue()) {
                idx++;
                continue;
            }

            if (i.start.longValue() > end_time.longValue()) {
                consumed = true;
                intervals.add(idx, new Range<T>(start_time, end_time));
                break;
            }

            int idx2 = idx + 1;
            while (idx2 < size && intervals.get(idx2).end.longValue() <= end_time.longValue()) {
                intervals.remove(idx2);
                size--;
            }

            if (idx2 < size && intervals.get(idx2).start.longValue() <= interval.end.longValue()) {
                end_time = intervals.get(idx2).end;
                intervals.remove(idx2);
                size--;
            }

            if (idx < size) {
                intervals.set(idx, new Range<T>(i.start, end_time));
            } else {
                intervals.add(new Range<T>(i.start, end_time));
            }

            consumed = true;
        }

        if (!consumed) {
            intervals.add(new Range<T>(start_time, end_time));
        }
    }

    public List<Range<T>> subtract(/*final Range<T> interval_param,*/ final List<Range<T>> intervals_subtract_from) {
        int idx = 0;
        final Range<T> interval_param = this;
        T intervalStart = interval_param.start;
        T intervalEnd = interval_param.end;
        LinkedList<Range<T>> result = new LinkedList<Range<T>>();
        boolean consumed = false;

        while (idx < intervals_subtract_from.size()) {
            Range<T> i = intervals_subtract_from.get(idx);

            if (i.start.longValue() <= intervalStart.longValue() && i.end.longValue() >= intervalEnd.longValue()) {
                consumed = true;
                break;
            }

            if (i.start.longValue() > intervalEnd.longValue()) {
                consumed = true;
                result.add(new Range<T>(intervalStart, intervalEnd));
                break;
            }

            if (i.start.longValue() > intervalStart.longValue()) {
                result.add(new Range<T>(intervalStart, i.start));
                intervalStart = i.start;
                continue;
            }

            if (i.end.longValue() <= intervalStart.longValue()) {
                idx++;
                continue;
            }

            if (i.start.longValue() >= intervalEnd.longValue()) {
                result.add(new Range<T>(intervalStart, intervalEnd));
                consumed = true;
                break;
            }

            if (i.end.longValue() >= intervalEnd.longValue()) {
                intervalEnd = i.start;
                result.add(new Range<T>(intervalStart, intervalEnd));
                consumed = true;
                break;
            } else if (i.end.longValue() > intervalStart.longValue()) {
                intervalStart = i.end;
                idx++;
            } else {
                result.add(new Range<T>(intervalStart, i.start));
                intervalStart = i.end;
                idx++;
            }
        }

        if (!consumed) {
            result.addLast(new Range<T>(intervalStart, intervalEnd));
        }

        return result;
    }

    /**
     * Extend current range to cover given range.
     *
     * @param range  range to cover
     * @return extended range
     */
    public Range<T> extend(Range<T> range) {
        return new Range<T>(DLightMath.<T>min(start, range.getStart()), DLightMath.<T>max(end, range.getEnd()));
    }

    @Override
    public String toString() {
        return String.valueOf(start) + STRING_DELIMITER + String.valueOf(end);
    }

    public String toString(String prefix, String startFormat, String glue, String endFormat, String suffix) {
        if (start != null || end != null) {
            StringBuilder buf = new StringBuilder();
            if (prefix != null) {
                buf.append(prefix);
            }
            if (start != null) {
                buf.append(String.format(startFormat, start));
            }
            if (start != null && end != null && glue != null) {
                buf.append(glue);
            }
            if (end != null) {
                buf.append(String.format(endFormat, end));
            }
            if (suffix != null) {
                buf.append(suffix);
            }
            return buf.toString();
        } else {
            return ""; // NOI18N
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Range<?>)) {
            return false;
        }
        final Range<?> other = (Range<?>) obj;
        if (this.start != other.start && (this.start == null || !this.start.equals(other.start))) {
            return false;
        }
        if (this.end != other.end && (this.end == null || !this.end.equals(other.end))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.start != null ? this.start.hashCode() : 0);
        hash = 53 * hash + (this.end != null ? this.end.hashCode() : 0);
        return hash;
    }
}
