/*
 * RangeMap.java -- map ranges of long values to objects.
 * Copyright (C) 2006, 2008  Casey Marshall
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.netbeans.modules.cnd.gizmo.addr2line.dwarf2line;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public final class RangeMap {

    private SortedMap<Range, Object> ranges;

    public RangeMap() {
        ranges = new TreeMap<Range, Object>(new RangeComparator());
    }

    /**
     * Put, or update, a mapping between a range of values and an
     * object. The <code>begin</code> and <code>end</code> values are
     * treated as unsigned 64-bit integers.
     */
    public void put(long begin, long end, Object value) {
        Range r1 = new Range(begin, end);

        SortedMap m = ranges.tailMap(r1);
        if (!m.isEmpty()) {
            Range r2 = (Range) m.firstKey();
            if (r2.overlaps(r1)) {
                ranges.remove(r2);
                ranges.put(r1.mergeWith(r2), value);
                return;
            }
        }
        ranges.put(r1, value);
    }

    /**
     * Get the object that is mapped to a range containing the argument,
     * or <code>null</code> if no range is mapped.
     */
    public Object get(long value) {
        Range r1 = new Range(value, value);
        SortedMap m = ranges.tailMap(r1);
        if (m.isEmpty()) {
            return null;
        }

        Range r2 = (Range) m.firstKey();
        if (r2.contains(value)) {
            return m.get(r2);
        }

        return null;
    }

    public int size() {
        return ranges.size();
    }

    @Override
    public String toString() {
        return ranges.toString();
    }

    private static class RangeComparator implements Comparator<Range> {

        public int compare(Range r1, Range r2) {
            if (r1.overlaps(r2)) {
                return 0;
            }

            if (ucomp(r1.begin, r2.end) > 0) {
                return 1;
            }
            return -1;
        }
    }

    private static class Range {

        final long begin;
        final long end;

        Range(final long begin, final long end) {
            if (ucomp(begin, end) > 0) {
                throw new IllegalArgumentException("begin is not less than end (unsigned)"); // NOI18N
            }
            this.begin = begin;
            this.end = end;
        }

        boolean contains(long value) {
            return (ucomp(begin, value) <= 0 && ucomp(end, value) >= 0);
        }

        boolean contains(Range that) {
            return (ucomp(this.begin, that.begin) <= 0 && ucomp(this.end, that.end) >= 0);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Range)) {
                return false;
            }
            return equals((Range) o);
        }

        boolean equals(Range that) {
            return (this.begin == that.begin && this.end == that.end);
        }

        @Override
        public int hashCode() {
            long hash = 3 * begin + 7 * end;
            return (int) (hash >> 32) | (int) hash;
        }

        boolean overlaps(Range that) {
            return ((ucomp(this.begin, that.begin) <= 0 && ucomp(this.end, that.begin) >= 0) || (ucomp(this.end, that.begin) >= 0 && ucomp(this.end, that.end) <= 0) || (ucomp(this.begin, that.end) <= 0 && ucomp(this.end, that.end) >= 0));
        }

        Range mergeWith(Range that) {
            if (!overlaps(that)) {
                throw new IllegalArgumentException("ranges don't overlap this=" + this + " that=" + that); // NOI18N
            }
            long begin = this.begin;
            if (ucomp(begin, that.begin) > 0) {
                begin = that.begin;
            }
            long end = this.end;
            if (ucomp(end, that.end) < 0) {
                end = that.end;
            }
            return new Range(begin, end);
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder(24);
            str.append("(0x"); // NOI18N

            String s = Long.toHexString(begin);
            int n = 16 - s.length();
            while ((n--) > 0) {
                str.append('0');
            }
            str.append(s);
            str.append(", 0x"); // NOI18N

            s = Long.toHexString(end);
            n = 16 - s.length();
            while ((n--) > 0) {
                str.append('0');
            }
            str.append(s);
            str.append(")"); // NOI18N
            return str.toString();
        }
    }

    static int ucomp(long l1, long l2) {
        if (l1 == l2) {
            return 0;
        }

        if (l1 < 0) {
            if (l2 < 0) {
                if (l1 < l2) {
                    return 1;
                } else {
                    return -1;
                }
            }
            return 1;
        } else {
            if (l2 >= 0) {
                if (l1 < l2) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return -1;
        }
    }
}
