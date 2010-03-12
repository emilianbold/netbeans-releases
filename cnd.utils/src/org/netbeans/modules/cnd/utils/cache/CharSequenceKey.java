/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.utils.cache;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Alexander Simon
 */
public final class CharSequenceKey implements TinyCharSequence, Comparable<CharSequence> {
    private static final TinyCharSequence EMPTY = new CharSequenceKey(new byte[]{});
    public static final Comparator<CharSequence> Comparator = new CharSequenceComparator();
    public static final Comparator<CharSequence> ComparatorIgnoreCase = new CharSequenceComparatorIgnoreCase();
    private final Object value;
    private int hash;

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
            return EMPTY;
        }
        byte[] b = new byte[n];
        boolean bytes = true;
        int o;
        for (int i = 0; i < n; i++) {
            o = buf[start + i];
            if ((o & 0xFF) != o) {
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
        return new CharSequenceKey(v);
    }

    public static CharSequence create(CharSequence s) {
        if (s == null) {
            return null;
        }
        //return s.toString();
        if (s instanceof TinyCharSequence) {
            return s;
        }
        int n = s.length();
        if (n == 0) {
            return EMPTY;
        }
        byte[] b = new byte[n];
        boolean bytes = true;
        int o;
        for(int i = 0; i < n; i++){
            o = s.charAt(i);
            if ( (o & 0xFF) != o){
                bytes = false;
                break;
            }
            b[i] = (byte)o;
        }
        if (bytes) {
            return createFromBytes(b, n);
        }
        char[] v = new char[n];
        for(int i = 0; i < n; i++){
            v[i] = s.charAt(i);
        }
        return new CharSequenceKey(v);
    }

    public static CharSequence empty(){
        return EMPTY;
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
                while (++i <= max && text.charAt(i) != first) {}
            }

            // found first character, now look at the rest of seq
            if (i <= max) {
                int j = i + 1;
                int end = j + seqLength - 1;
                for (int k = 1; j < end && text.charAt(j) == seq.charAt(k); j++, k++) {}
                if (j == end) {
                    // found whole sequence
                    return i;
                }
            }
        }
        return -1;
    }

    public static String toString(CharSequence prefix, char separator, CharSequence postfix) {
        int prefLength = prefix.length();
        int postLength = postfix.length();
        char[] chars = new char[prefLength + 1 + postLength];
        int indx = 0;
        if (prefix instanceof String) {
            ((String)prefix).getChars(0, prefLength, chars, indx);
            indx = prefLength;
        } else {
            for (int i = 0; i < prefLength; i++) {
                chars[indx++] = prefix.charAt(i);
            }
        }
        chars[indx++] = separator;
        if (postfix instanceof String) {
            ((String)postfix).getChars(0, postLength, chars, indx);
        } else {
            for (int i = 0; i < postLength; i++) {
                chars[indx++] = postfix.charAt(i);
            }
        }
        return new String(chars);
    }

    private static TinyCharSequence createFromBytes(byte[] b, int n) {
        if (n < 8) {
            return new Fixed7CharSequenceKey(b, n);
        } else if (n < 16) {
            return new Fixed15CharSequenceKey(b, n);
        } else if (n < 24) {
            return new Fixed23CharSequenceKey(b, n);
        }
        return new CharSequenceKey(b);
    }

    private CharSequenceKey(byte[] b) {
        value = b;
    }

    private CharSequenceKey(char[] v) {
        value = v;
    }

    @Override
    public int length() {
        if (value instanceof byte[]) {
            return ((byte[]) value).length;
        }
        return ((char[]) value).length;
    }

    @Override
    public char charAt(int index) {
        if (value instanceof byte[]) {
            int r = ((byte[]) value)[index] & 0xFF;
            return (char) r;
        }
        return ((char[]) value)[index];
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof CharSequenceKey) {
            CharSequenceKey otherString = (CharSequenceKey)object;
                if (hash != 0 && otherString.hash != 0) {
                    if (hash != otherString.hash) {
                        return false;
                    }
                }
                if ((value instanceof byte[]) && (otherString.value instanceof byte[])) {
                    return Arrays.equals( (byte[])value, (byte[])otherString.value );
                } else if ((value instanceof char[]) && (otherString.value instanceof char[])) {
                    return Arrays.equals( (char[])value, (char[])otherString.value );
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            if (value instanceof byte[]) {
                byte[] v = (byte[])value;
                int n = v.length;
                for (int i = 0; i < n; i++) {
                    h = 31*h + v[i];
                }
            } else {
                char[] v = (char[])value;
                int n = v.length;
                for (int i = 0; i < n; i++) {
                    h = 31*h + v[i];
                }
            }
            hash = h;
        }
        return h;
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex) {
        return create(toString().substring(beginIndex, endIndex));
    }

    @Override
    public String toString() {
        if (value instanceof byte[]) {
            byte[] v = (byte[]) value;
            int n = v.length;
            char[] r = new char[n];
            for (int i = 0; i < n; i++) {
                int c = v[i] & 0xFF;
                r[i] = (char) c;
            }
            return new String(r);
        }
        char[] v = (char[]) value;
        return new String(v);
    }
    
    private static class CharSequenceComparator implements Comparator<CharSequence> {
        @Override
        public int compare(CharSequence o1, CharSequence o2) {
            if ((o1 instanceof CharSequenceKey)){
                if ((o2 instanceof CharSequenceKey)){
                    CharSequenceKey csk1 = (CharSequenceKey) o1;
                    CharSequenceKey csk2 = (CharSequenceKey) o2;
                    if ((csk1.value instanceof byte[]) &&
                        (csk2.value instanceof byte[])){
                        byte[] b1 = (byte[]) csk1.value;
                        byte[] b2 = (byte[]) csk2.value;
                        int len1 = b1.length;
                        int len2 = b2.length;
                        int n = Math.min(len1, len2);
                        int k = 0;
                        while (k < n) {
                            if (b1[k] != b2[k]) {
                                return (b1[k] & 0xFF) - (b2[k] & 0xFF);
                            }
                            k++;
                        }
                        return len1 - len2;
                    }
                } else {
                    CharSequenceKey csk1 = (CharSequenceKey) o1;
                    if ((csk1.value instanceof byte[])){
                        byte[] b1 = (byte[]) csk1.value;
                        int len1 = b1.length;
                        int len2 = o2.length();
                        int n = Math.min(len1, len2);
                        int k = 0;
                        int c1,c2;
                        while (k < n) {
                            c1 = b1[k] & 0xFF;
                            c2 = o2.charAt(k);
                            if (c1 != c2) {
                                return c1 - c2;
                            }
                            k++;
                        }
                        return len1 - len2;
                    }
                }
            } else if ((o2 instanceof CharSequenceKey)){
                CharSequenceKey csk2 = (CharSequenceKey) o2;
                if ((csk2.value instanceof byte[])){
                    byte[] b2 = (byte[]) csk2.value;
                    int len1 = o1.length();
                    int len2 = b2.length;
                    int n = Math.min(len1, len2);
                    int k = 0;
                    int c1,c2;
                    while (k < n) {
                        c1 = o1.charAt(k);
                        c2 = b2[k] & 0xFF;
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                        k++;
                    }
                    return len1 - len2;
                }
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
    }

    @Override
    public int compareTo(CharSequence o) {
        return Comparator.compare(this, o);
    }

    private static class CharSequenceComparatorIgnoreCase implements Comparator<CharSequence> {
        @Override
        public int compare(CharSequence o1, CharSequence o2) {
            int n1 = o1.length();
            int n2 = o2.length();
            for (int i1 = 0,  i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
                char c1 = o1.charAt(i1);
                char c2 = o2.charAt(i2);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1);
                    c2 = Character.toUpperCase(c2);
                    if (c1 != c2) {
                        c1 = Character.toLowerCase(c1);
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 - n2;
        }
    }

    private static final class Fixed7CharSequenceKey implements TinyCharSequence, Comparable<CharSequence> {
        private final int i1;
        private final int i2;

        @SuppressWarnings("fallthrough")
        private Fixed7CharSequenceKey(byte[] b, int n){
            int a1 = n;
            int a2 = 0;
            switch (n){
                case 7:
                    a2+=(b[6]&0xFF)<<24;
                case 6:
                    a2+=(b[5]&0xFF)<<16;
                case 5:
                    a2+=(b[4]&0xFF)<<8;
                case 4:
                    a2+=b[3]&0xFF;
                case 3:
                    a1+=(b[2]&0xFF)<<24;
                case 2:
                    a1+=(b[1]&0xFF)<<16;
                case 1:
                    a1+=(b[0]&0xFF)<<8;
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
            return i1&0xFF;
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch(index) {
                case 0:  r =(i1&0xFF00)>>8; break;
                case 1:  r =(i1&0xFF0000)>>16; break;
                case 2:  r =(i1>>24)&0xFF; break;
                case 3:  r =i2&0xFF; break;
                case 4:  r =(i2&0xFF00)>>8; break;
                case 5:  r =(i2&0xFF0000)>>16; break;
                case 6:  r =(i2>>24)&0xFF; break;
            }
            return (char)r;
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
            if (object instanceof Fixed7CharSequenceKey) {
                Fixed7CharSequenceKey otherString = (Fixed7CharSequenceKey)object;
                return i1 == otherString.i1 && i2 == otherString.i2;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 0;
            for (int i = 0; i < length(); i++) {
                hash = 31*hash+charAt(i);
            }
            return hash;
//            return (i1 >> 4) + (i1 >> 8) + (i2 << 5) - i2;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequenceKey.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    private static final class Fixed15CharSequenceKey implements TinyCharSequence, Comparable<CharSequence> {
        private final int i1;
        private final int i2;
        private final int i3;
        private final int i4;

        @SuppressWarnings("fallthrough")
        private Fixed15CharSequenceKey(byte[] b, int n){
            int a1 = n;
            int a2 = 0;
            int a3 = 0;
            int a4 = 0;
            switch (n){
                case 15:
                    a4+=(b[14]&0xFF)<<24;
                case 14:
                    a4+=(b[13]&0xFF)<<16;
                case 13:
                    a4+=(b[12]&0xFF)<<8;
                case 12:
                    a4+=b[11]&0xFF;
                case 11:
                    a3+=(b[10]&0xFF)<<24;
                case 10:
                    a3+=(b[9]&0xFF)<<16;
                case 9:
                    a3+=(b[8]&0xFF)<<8;
                case 8:
                    a3+=b[7]&0xFF;
                case 7:
                    a2+=(b[6]&0xFF)<<24;
                case 6:
                    a2+=(b[5]&0xFF)<<16;
                case 5:
                    a2+=(b[4]&0xFF)<<8;
                case 4:
                    a2+=b[3]&0xFF;
                case 3:
                    a1+=(b[2]&0xFF)<<24;
                case 2:
                    a1+=(b[1]&0xFF)<<16;
                case 1:
                    a1+=(b[0]&0xFF)<<8;
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
            return i1&0xFF;
        }

        @Override
        public char charAt(int index) {
            int r = 0;
            switch(index) {
                case 0:  r =(i1&0xFF00)>>8; break;
                case 1:  r =(i1&0xFF0000)>>16; break;
                case 2:  r =(i1>>24)&0xFF; break;
                case 3:  r =i2&0xFF; break;
                case 4:  r =(i2&0xFF00)>>8; break;
                case 5:  r =(i2&0xFF0000)>>16; break;
                case 6:  r =(i2>>24)&0xFF; break;
                case 7:  r =i3&0xFF; break;
                case 8:  r =(i3&0xFF00)>>8; break;
                case 9:  r =(i3&0xFF0000)>>16; break;
                case 10:  r =(i3>>24)&0xFF; break;
                case 11:  r =i4&0xFF; break;
                case 12:  r =(i4&0xFF00)>>8; break;
                case 13:  r =(i4&0xFF0000)>>16; break;
                case 14:  r =(i4>>24)&0xFF; break;
            }
            return (char)r;
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
            if (object instanceof Fixed15CharSequenceKey) {
                Fixed15CharSequenceKey otherString = (Fixed15CharSequenceKey)object;
                return i1 == otherString.i1 && i2 == otherString.i2 && i3 == otherString.i3 && i4 == otherString.i4;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return i1+31*(i2+ 31*(i3+31*i4));
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequenceKey.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }

    private static final class Fixed23CharSequenceKey implements TinyCharSequence, Comparable<CharSequence> {
        private final long i1;
        private final long i2;
        private final long i3;

        @SuppressWarnings("fallthrough")
        private Fixed23CharSequenceKey(byte[] b, int n){
            long a1 = 0;
            long a2 = 0;
            long a3 = 0;
            switch (n){
                case 23:
                    a3+=(b[22]&0xFF)<<24;
                case 22:
                    a3+=(b[21]&0xFF)<<16;
                case 21:
                    a3+=(b[20]&0xFF)<<8;
                case 20:
                    a3+=(b[19]&0xFF);
                    a3<<=32;
                case 19:
                    a3+=(b[18]&0xFF)<<24;
                case 18:
                    a3+=(b[17]&0xFF)<<16;
                case 17:
                    a3+=(b[16]&0xFF)<<8;
                case 16:
                    a3+=b[15]&0xFF;
                case 15:
                    a2+=(b[14]&0xFF)<<24;
                case 14:
                    a2+=(b[13]&0xFF)<<16;
                case 13:
                    a2+=(b[12]&0xFF)<<8;
                case 12:
                    a2+=(b[11]&0xFF);
                    a2<<=32;
                case 11:
                    a2+=(b[10]&0xFF)<<24;
                case 10:
                    a2+=(b[9]&0xFF)<<16;
                case 9:
                    a2+=(b[8]&0xFF)<<8;
                case 8:
                    a2+=b[7]&0xFF;
                case 7:
                    a1+=(b[6]&0xFF)<<24;
                case 6:
                    a1+=(b[5]&0xFF)<<16;
                case 5:
                    a1+=(b[4]&0xFF)<<8;
                case 4:
                    a1+=(b[3]&0xFF);
                    a1<<=32;
                case 3:
                    a1+=(b[2]&0xFF)<<24;
                case 2:
                    a1+=(b[1]&0xFF)<<16;
                case 1:
                    a1+=(b[0]&0xFF)<<8;
                case 0:
                    a1+=n;
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
            switch(index) {
                case 0: r = (int) ((i1 >> 8) & 0xFFL); break;
                case 1: r = (int) ((i1 >> 16) & 0xFFL); break;
                case 2: r = (int) ((i1 >> 24) & 0xFFL); break;
                case 3: r = (int) ((i1 >> 32) & 0xFFL); break;
                case 4: r = (int) ((i1 >> 40) & 0xFFL); break;
                case 5: r = (int) ((i1 >> 48) & 0xFFL); break;
                case 6: r = (int) ((i1 >> 56) & 0xFFL); break;
                case 7: r = (int) (i2 & 0xFFL); break;
                case 8: r = (int) ((i2 >> 8) & 0xFFL); break;
                case 9: r = (int) ((i2 >> 16) & 0xFFL); break;
                case 10: r = (int) ((i2 >> 24) & 0xFFL); break;
                case 11: r = (int) ((i2 >> 32) & 0xFFL); break;
                case 12: r = (int) ((i2 >> 40) & 0xFFL); break;
                case 13: r = (int) ((i2 >> 48) & 0xFFL); break;
                case 14: r = (int) ((i2 >> 56) & 0xFFL); break;
                case 15: r = (int) (i3 & 0xFFL); break;
                case 16: r = (int) ((i3 >> 8) & 0xFFL); break;
                case 17: r = (int) ((i3 >> 16) & 0xFFL); break;
                case 18: r = (int) ((i3 >> 24) & 0xFFL); break;
                case 19: r = (int) ((i3 >> 32) & 0xFFL); break;
                case 20: r = (int) ((i3 >> 40) & 0xFFL); break;
                case 21: r = (int) ((i3 >> 48) & 0xFFL); break;
                case 22: r = (int) ((i3 >> 56) & 0xFFL); break;
            }
            return (char)r;
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
            if (object instanceof Fixed23CharSequenceKey) {
                Fixed23CharSequenceKey otherString = (Fixed23CharSequenceKey)object;
                return i1 == otherString.i1 && i2 == otherString.i2 && i3 == otherString.i3;
            }
            return false;
        }

        @Override
        public int hashCode() {
            long res = i1+31*(i2+ 31*i3);
            res = (res + (res >>32))& 0xFFFFFFFFL;
            return (int) res;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return CharSequenceKey.create(toString().substring(start, end));
        }

        @Override
        public int compareTo(CharSequence o) {
            return Comparator.compare(this, o);
        }
    }
}
