/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.openide.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Useful static methods to provide and work with memory efficient CharSequence 
 * implementations for ASCII strings.
 *
 * @since 8.3
 * @author Alexander Simon
 * @author Vladimir Voskresensky
 */
public final class CharSequences {

    /**
     * Provides compact char sequence object like {@link String#String(char[], int, int)}
     */
    public static CharSequence create(char buf[], int start, int count) {
        if (start < 0) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        // Note: offset or count might be near -1>>>1.
        if (start > buf.length - count) {
            throw new StringIndexOutOfBoundsException(start + count);
        }
        int n = count;
        if (n == 0) {
            // special constant shared among all empty char sequences
            return EMPTY;
        }
        byte[] b = new byte[n];
        boolean bytes = true;
        int o;
        // check 2 bytes vs 1 byte chars
        for (int i = 0; i < n; i++) {
            o = buf[start + i];
            if ((o & 0xFF) != o) {
                // can not compact this char sequence
                bytes = false;
                break;
            }
            b[i] = (byte) o;
        }
        if (bytes) {
            return createFromBytes(b, n);
        }
        char[] v = new char[count];
        System.arraycopy(buf, start, v, 0, count);
        return new CharBasedSequence(v);
    }

    /**
     * Provides compact char sequence object like {@link String#String(String)}
     */
    public static CharSequence create(CharSequence s) {
        if (s == null) {
            return null;
        }
        // already compact instance
        if (s instanceof CompactCharSequence) {
            return s;
        }
        int n = s.length();
        if (n == 0) {
            // special constant shared among all empty char sequences
            return EMPTY;
        }
        byte[] b = new byte[n];
        boolean bytes = true;
        int o;
        for (int i = 0; i < n; i++) {
            o = s.charAt(i);
            if ((o & 0xFF) != o) {
                bytes = false;
                break;
            }
            b[i] = (byte) o;
        }
        if (bytes) {
            return createFromBytes(b, n);
        }
        char[] v = new char[n];
        for (int i = 0; i < n; i++) {
            v[i] = s.charAt(i);
        }
        return new CharBasedSequence(v);
    }

    /**
     * Provides optimized char sequences comparator
     * @return comparator
     */
    public static Comparator<CharSequence> comparator() {
        return Comparator;
    }
    
    /**
     * Returns object to represent empty sequence ""
     * @return char sequence to represent empty sequence
     */
    public static CharSequence empty() {
        return EMPTY;
    }

    /**
     * Predicate to check if provides char sequence is based on compact implementation
     * @param cs char sequence object to check
     * @return true if compact implementation, false otherwise
     */
    public static boolean isCompact(CharSequence cs) {
        return cs instanceof CompactCharSequence;
    }
    
    /**
     * Implementation of {@link String#indexOf(String)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq) {
        return indexOf(text, seq, 0);
    }

    /**
     * Implementation of {@link String#indexOf(String,int)} for character sequences.
     */
    public static int indexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
        if (fromIndex >= textLength) {
            return (seqLength == 0 ? textLength : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (seqLength == 0) {
            return fromIndex;
        }

        char first = seq.charAt(0);
        int max = textLength - seqLength;

        for (int i = fromIndex; i <= max; i++) {
            // look for first character
            if (text.charAt(i) != first) {
                while (++i <= max && text.charAt(i) != first) {
                }
            }

            // found first character, now look at the rest of seq
            if (i <= max) {
                int j = i + 1;
                int end = j + seqLength - 1;
                for (int k = 1; j < end && text.charAt(j) == seq.charAt(k); j++, k++) {
                }
                if (j == end) {
                    // found whole sequence
                    return i;
                }
            }
        }
        return -1;
    }

    private static CompactCharSequence createFromBytes(byte[] b, int n) {
        if (n < 8) {
            return new Fixed_0_7(b, n);
        } else if (n < 16) {
            return new Fixed_8_15(b, n);
        } else if (n < 24) {
            return new Fixed_16_23(b, n);
        }
        return new ByteBasedSequence(b);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Memory efficient implementations of CharSequence
    // Comparision between Fixed and String memory consumption:
    // 32-bit JVM
    //String    String CharSequence
    //Length     Size    Size
    //1..2        40      16
    //3..6        48      16
    //7..7        56      16
    //8..10       56      24
    //11..14      64      24
    //15..15      72      24
    //16..18      72      32
    //19..22      80      32
    //23..23      88      32
    //24..26      88      56
    //27..28      96      56
    //29..30      96      64
    //31..34     104      64
    //35..36     112      64
    //37..38     112      72
    //39..42     120      72
    //......................
    //79..82   - 200     112
    //
    // 64-bit JVM
    //1           72      24
    //2           72      24
    //3           80      24
    //4           80      24
    //5           80      24
    //6           80      24
    //7           88      24
    //8           88      32
    //9           88      32
    //11          96      32
    //13          96      32
    //15         104      32
    //16         104      40
    //17         104      40
    //18         112      40
    //19         112      40
    //20         112      40
    //22         120      40
    //23         120      40
    //24         120      80
    //25         120      88
    //26         128      88
    //60         192     120
    //100        272     160

    //<editor-fold defaultstate="collapsed" desc="Private Classes">

    /**
     * compact char sequence implementation for strings in range 0-7 characters
     * 8 + 2*4 = 16 bytes for all strings vs String impl occupying 
     */
    private static final class Fixed_0_7 implements CompactCharSequence, Comparable<CharSequence> {

        private final int i1;
        private final int i2;

        @SuppressWarnings("fallthrough")
        private Fixed_0_7(byte[] b, int n) {
            int a1 = n;
            int a2 = 0;
            switch (n) {
                case 7:
                    a2 += (b[6] & 0xFF) << 24;
                case 6:
                    a2 += (b[5] & 0xFF) << 16;
                case 5:
                    a2 += (b[4] & 0xFF) << 8;
                case 4:
                    a2 += b[3] & 0xFF;
                case 3:
                    a1 += (b[2] & 0xFF) << 24;
                case 2:
                    a1 += (b[1] & 0xFF) << 16;
                case 1:
                    a1 += (b[0] & 0xFF) << 8;
                case 0:
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            i1 = a1;
            i2 = a2;
        }

        @Override
        public int length() {
            return i1 & 0xFF;
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (i1 & 0xFF00) >> 8;
                    break;
                case 1:
                    r = (i1 & 0xFF0000) >> 16;
                    break;
                case 2:
                    r = (i1 >> 24) & 0xFF;
                    break;
                case 3:
                    r = i2 & 0xFF;
                    break;
                case 4:
                    r = (i2 & 0xFF00) >> 8;
                    break;
                case 5:
                    r = (i2 & 0xFF0000) >> 16;
                    break;
                case 6:
                    r = (i2 >> 24) & 0xFF;
                    break;
            }
            return (char) r;
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                r[i] = charAt(i);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed_0_7) {
                Fixed_0_7 otherString = (Fixed_0_7) object;
                return i1 == otherString.i1 && i2 == otherString.i2;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            for (int i = 0; i < length(); i++) {
                hash = 31 * hash + charAt(i);
            }
            return hash;
            //            return (i1 >> 4) + (i1 >> 8) + (i2 << 5) - i2;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    /**
     * compact char sequence implementation for strings in range 8-15 characters
     * size: 8 + 4*4 = 24 bytes for all strings vs String impl occupying
     */
    private static final class Fixed_8_15 implements CompactCharSequence, Comparable<CharSequence> {

        private final int i1;
        private final int i2;
        private final int i3;
        private final int i4;

        @SuppressWarnings("fallthrough")
        private Fixed_8_15(byte[] b, int n) {
            int a1 = n;
            int a2 = 0;
            int a3 = 0;
            int a4 = 0;
            switch (n) {
                case 15:
                    a4 += (b[14] & 0xFF) << 24;
                case 14:
                    a4 += (b[13] & 0xFF) << 16;
                case 13:
                    a4 += (b[12] & 0xFF) << 8;
                case 12:
                    a4 += b[11] & 0xFF;
                case 11:
                    a3 += (b[10] & 0xFF) << 24;
                case 10:
                    a3 += (b[9] & 0xFF) << 16;
                case 9:
                    a3 += (b[8] & 0xFF) << 8;
                case 8:
                    a3 += b[7] & 0xFF;
                case 7:
                    a2 += (b[6] & 0xFF) << 24;
                case 6:
                    a2 += (b[5] & 0xFF) << 16;
                case 5:
                    a2 += (b[4] & 0xFF) << 8;
                case 4:
                    a2 += b[3] & 0xFF;
                case 3:
                    a1 += (b[2] & 0xFF) << 24;
                case 2:
                    a1 += (b[1] & 0xFF) << 16;
                case 1:
                    a1 += (b[0] & 0xFF) << 8;
                case 0:
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            i1 = a1;
            i2 = a2;
            i3 = a3;
            i4 = a4;
        }

        @Override
        public int length() {
            return i1 & 0xFF;
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (i1 & 0xFF00) >> 8;
                    break;
                case 1:
                    r = (i1 & 0xFF0000) >> 16;
                    break;
                case 2:
                    r = (i1 >> 24) & 0xFF;
                    break;
                case 3:
                    r = i2 & 0xFF;
                    break;
                case 4:
                    r = (i2 & 0xFF00) >> 8;
                    break;
                case 5:
                    r = (i2 & 0xFF0000) >> 16;
                    break;
                case 6:
                    r = (i2 >> 24) & 0xFF;
                    break;
                case 7:
                    r = i3 & 0xFF;
                    break;
                case 8:
                    r = (i3 & 0xFF00) >> 8;
                    break;
                case 9:
                    r = (i3 & 0xFF0000) >> 16;
                    break;
                case 10:
                    r = (i3 >> 24) & 0xFF;
                    break;
                case 11:
                    r = i4 & 0xFF;
                    break;
                case 12:
                    r = (i4 & 0xFF00) >> 8;
                    break;
                case 13:
                    r = (i4 & 0xFF0000) >> 16;
                    break;
                case 14:
                    r = (i4 >> 24) & 0xFF;
                    break;
            }
            return (char) r;
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                r[i] = charAt(i);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed_8_15) {
                Fixed_8_15 otherString = (Fixed_8_15) object;
                return i1 == otherString.i1 && i2 == otherString.i2 && i3 == otherString.i3 && i4 == otherString.i4;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return i1 + 31 * (i2 + 31 * (i3 + 31 * i4));
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    /**
     * compact char sequence implementation for strings in range 16-23 characters
     * size: 8 + 3*8 = 32 bytes for all strings vs String impl occupying 
     */
    private static final class Fixed_16_23 implements CompactCharSequence, Comparable<CharSequence> {

        private final long i1;
        private final long i2;
        private final long i3;

        @SuppressWarnings("fallthrough")
        private Fixed_16_23(byte[] b, int n) {
            long a1 = 0;
            long a2 = 0;
            long a3 = 0;
            switch (n) {
                case 23:
                    a3 += (b[22] & 0xFF) << 24;
                case 22:
                    a3 += (b[21] & 0xFF) << 16;
                case 21:
                    a3 += (b[20] & 0xFF) << 8;
                case 20:
                    a3 += (b[19] & 0xFF);
                    a3 <<= 32;
                case 19:
                    a3 += (b[18] & 0xFF) << 24;
                case 18:
                    a3 += (b[17] & 0xFF) << 16;
                case 17:
                    a3 += (b[16] & 0xFF) << 8;
                case 16:
                    a3 += b[15] & 0xFF;
                case 15:
                    a2 += (b[14] & 0xFF) << 24;
                case 14:
                    a2 += (b[13] & 0xFF) << 16;
                case 13:
                    a2 += (b[12] & 0xFF) << 8;
                case 12:
                    a2 += (b[11] & 0xFF);
                    a2 <<= 32;
                case 11:
                    a2 += (b[10] & 0xFF) << 24;
                case 10:
                    a2 += (b[9] & 0xFF) << 16;
                case 9:
                    a2 += (b[8] & 0xFF) << 8;
                case 8:
                    a2 += b[7] & 0xFF;
                case 7:
                    a1 += (b[6] & 0xFF) << 24;
                case 6:
                    a1 += (b[5] & 0xFF) << 16;
                case 5:
                    a1 += (b[4] & 0xFF) << 8;
                case 4:
                    a1 += (b[3] & 0xFF);
                    a1 <<= 32;
                case 3:
                    a1 += (b[2] & 0xFF) << 24;
                case 2:
                    a1 += (b[1] & 0xFF) << 16;
                case 1:
                    a1 += (b[0] & 0xFF) << 8;
                case 0:
                    a1 += n;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            i1 = a1;
            i2 = a2;
            i3 = a3;
        }

        @Override
        public int length() {
            return (int) (i1 & 0xFF);
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch (index) {
                case 0:
                    r = (int) ((i1 >> 8) & 0xFFL);
                    break;
                case 1:
                    r = (int) ((i1 >> 16) & 0xFFL);
                    break;
                case 2:
                    r = (int) ((i1 >> 24) & 0xFFL);
                    break;
                case 3:
                    r = (int) ((i1 >> 32) & 0xFFL);
                    break;
                case 4:
                    r = (int) ((i1 >> 40) & 0xFFL);
                    break;
                case 5:
                    r = (int) ((i1 >> 48) & 0xFFL);
                    break;
                case 6:
                    r = (int) ((i1 >> 56) & 0xFFL);
                    break;
                case 7:
                    r = (int) (i2 & 0xFFL);
                    break;
                case 8:
                    r = (int) ((i2 >> 8) & 0xFFL);
                    break;
                case 9:
                    r = (int) ((i2 >> 16) & 0xFFL);
                    break;
                case 10:
                    r = (int) ((i2 >> 24) & 0xFFL);
                    break;
                case 11:
                    r = (int) ((i2 >> 32) & 0xFFL);
                    break;
                case 12:
                    r = (int) ((i2 >> 40) & 0xFFL);
                    break;
                case 13:
                    r = (int) ((i2 >> 48) & 0xFFL);
                    break;
                case 14:
                    r = (int) ((i2 >> 56) & 0xFFL);
                    break;
                case 15:
                    r = (int) (i3 & 0xFFL);
                    break;
                case 16:
                    r = (int) ((i3 >> 8) & 0xFFL);
                    break;
                case 17:
                    r = (int) ((i3 >> 16) & 0xFFL);
                    break;
                case 18:
                    r = (int) ((i3 >> 24) & 0xFFL);
                    break;
                case 19:
                    r = (int) ((i3 >> 32) & 0xFFL);
                    break;
                case 20:
                    r = (int) ((i3 >> 40) & 0xFFL);
                    break;
                case 21:
                    r = (int) ((i3 >> 48) & 0xFFL);
                    break;
                case 22:
                    r = (int) ((i3 >> 56) & 0xFFL);
                    break;
            }
            return (char) r;
        }

        @Override
        public String toString() {
            int n = length();
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                r[i] = charAt(i);
            }
            return new String(r);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Fixed_16_23) {
                Fixed_16_23 otherString = (Fixed_16_23) object;
                return i1 == otherString.i1 && i2 == otherString.i2 && i3 == otherString.i3;
            }
            return false;
        }

        @Override
        public int hashCode() {
            long res = i1 + 31 * (i2 + 31 * i3);
            res = (res + (res >> 32)) & 0xFFFFFFFFL;
            return (int) res;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequences.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    /**
     * compact char sequence implementation based on char[] array
     * size: 8 + 4 + 4 (= 16 bytes) + sizeof ('value')
     * it is still more effective than String, because string stores length in field
     * and it costs 20 bytes aligned into 24
     */
    private final static class CharBasedSequence implements CompactCharSequence, Comparable<CharSequence> {

        private final char[] value;
        private int hash;

        private CharBasedSequence(char[] v) {
            value = v;
        }

        @Override
        public int length() {
            return value.length;
        }

        @Override
        public char charAt(int index) {
            return value[index];
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof CharBasedSequence) {
                CharBasedSequence otherString = (CharBasedSequence) object;
                if (hash != 0 && otherString.hash != 0) {
                    if (hash != otherString.hash) {
                        return false;
                    }
                }
                return Arrays.equals(value, otherString.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                int n = value.length;
                for (int i = 0; i < n; i++) {
                    h = 31 * h + value[i];
                }
                hash = h;
            }
            return h;
        }

        @Override
        public CharSequence subSequence(int beginIndex, int endIndex) {
            return CharSequences.create(value, beginIndex, endIndex-beginIndex);
        }

        @Override
        public String toString() {
            return new String(value);
        }

        @Override
        public int compareTo(CharSequence o) {
            return CharSequenceComparator.compareCharBasedWithOther(this, o);
        }
    }

    /**
     * compact char sequence implementation based on byte[]
     * size: 8 + 4 + 4 (= 16 bytes) + sizeof ('value')
     */
    private final static class ByteBasedSequence implements CompactCharSequence, Comparable<CharSequence> {

        private final byte[] value;
        private int hash;

        private ByteBasedSequence(byte[] b) {
            value = b;
        }

        @Override
        public int length() {
            return value.length;
        }

        @Override
        public char charAt(int index) {
            int r = value[index] & 0xFF;
            return (char) r;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof ByteBasedSequence) {
                ByteBasedSequence otherString = (ByteBasedSequence) object;
                if (hash != 0 && otherString.hash != 0) {
                    if (hash != otherString.hash) {
                        return false;
                    }
                }
                return Arrays.equals(value, otherString.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                int n = value.length;
                for (int i = 0; i < n; i++) {
                    h = 31 * h + value[i];
                }
                hash = h;
            }
            return h;
        }

        @Override
        public CharSequence subSequence(int beginIndex, int endIndex) {
            return CharSequences.create(toChars(), beginIndex, endIndex-beginIndex);
        }

        @Override
        public String toString() {
            char[] r = toChars();
            return new String(r);
        }

        private char[] toChars() {
            int n = value.length;
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                int c = value[i] & 0xFF;
                r[i] = (char) c;
            }
            return r;
        }

        @Override
        public int compareTo(CharSequence o) {
            return CharSequenceComparator.compareByteBasedWithOther(this, o);
        }
    }

    private static final CompactCharSequence EMPTY = new Fixed_0_7(new byte[0], 0);
    private static final CharSequenceComparator Comparator = new CharSequenceComparator();

    /**
     * performance tuned comparator to prevent charAt calls when possible
     */
    private static class CharSequenceComparator implements Comparator<CharSequence> {

        @Override
        public int compare(CharSequence o1, CharSequence o2) {
            if (o1 instanceof ByteBasedSequence) {
                return compareByteBasedWithOther((ByteBasedSequence)o1, o2);
            } else if (o2 instanceof ByteBasedSequence) {
                return -compareByteBasedWithOther((ByteBasedSequence) o2, o1);
            } else if (o1 instanceof CharBasedSequence) {
                return compareCharBasedWithOther((CharBasedSequence)o1, o2);
            } else if (o2 instanceof CharBasedSequence) {
                return -compareCharBasedWithOther((CharBasedSequence)o2, o1);
            }
            int len1 = o1.length();
            int len2 = o2.length();
            int n = Math.min(len1, len2);
            int k = 0;
            while (k < n) {
                char c1 = o1.charAt(k);
                char c2 = o2.charAt(k);
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }
        
        //<editor-fold defaultstate="collapsed" desc="Private methods">
        private static int compareByteBased(ByteBasedSequence bbs1, ByteBasedSequence bbs2) {
            int len1 = bbs1.value.length;
            int len2 = bbs2.value.length;
            int n = Math.min(len1, len2);
            int k = 0;
            while (k < n) {
                if (bbs1.value[k] != bbs2.value[k]) {
                    return (bbs1.value[k] & 0xFF) - (bbs2.value[k] & 0xFF);
                }
                k++;
            }
            return len1 - len2;
        }

        private static int compareCharBased(CharBasedSequence cbs1, CharBasedSequence cbs2) {
            int len1 = cbs1.value.length;
            int len2 = cbs2.value.length;
            int n = Math.min(len1, len2);
            int k = 0;
            while (k < n) {
                if (cbs1.value[k] != cbs2.value[k]) {
                    return cbs1.value[k] - cbs2.value[k];
                }
                k++;
            }
            return len1 - len2;
        }

        private static int compareByteBasedWithCharBased(ByteBasedSequence bbs1, CharBasedSequence cbs2) {
            int len1 = bbs1.value.length;
            int len2 = cbs2.value.length;
            int n = Math.min(len1, len2);
            int k = 0;
            while (k < n) {
                int c1 = bbs1.value[k] & 0xFF;
                int c2 = cbs2.value[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }

        private static int compareByteBasedWithOther(ByteBasedSequence bbs1, CharSequence o2) {
            if (o2 instanceof ByteBasedSequence) {
                return compareByteBased(bbs1, (ByteBasedSequence) o2);
            } else if (o2 instanceof CharBasedSequence) {
                return compareByteBasedWithCharBased(bbs1, (CharBasedSequence) o2);
            }
            int len1 = bbs1.value.length;
            int len2 = o2.length();
            int n = Math.min(len1, len2);
            int k = 0;
            int c1, c2;
            while (k < n) {
                c1 = bbs1.value[k] & 0xFF;
                c2 = o2.charAt(k);
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }

        private static int compareCharBasedWithOther(CharBasedSequence cbs1, CharSequence o2) {
            if (o2 instanceof CharBasedSequence) {
                return compareCharBased(cbs1, (CharBasedSequence) o2);
            } else if (o2 instanceof ByteBasedSequence) {
                return -compareByteBasedWithCharBased((ByteBasedSequence) o2, cbs1);
            }
            int len1 = cbs1.value.length;
            int len2 = o2.length();
            int n = Math.min(len1, len2);
            int k = 0;
            int c1, c2;
            while (k < n) {
                c1 = cbs1.value[k];
                c2 = o2.charAt(k);
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }
        //</editor-fold>
    }

    /**
     * marker interface for compact char sequence implementations
     */
    private interface CompactCharSequence extends CharSequence {
    }

    /**
     * private constructor for utilities class
     */
    private CharSequences() {
    }

    //</editor-fold>
}
