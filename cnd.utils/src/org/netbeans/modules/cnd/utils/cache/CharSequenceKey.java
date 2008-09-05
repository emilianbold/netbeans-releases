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
public final class CharSequenceKey implements CharSequence, Comparable<CharSequenceKey> {
    private static final CharSequence EMPTY = create("");
    public static final Comparator<CharSequence> Comparator = new CharSequenceComparator();
    public static final Comparator<CharSequence> ComparatorIgnoreCase = new CharSequenceComparatorIgnoreCase();
    private final Object value;
    private int hash;

    public static CharSequence create(CharSequence s){
        if (s == null) {
            return null;
        }
        //return s.toString();
        if (s instanceof CharSequenceKey) {
            return (CharSequenceKey) s;
        } else if (s instanceof String) {
            return new CharSequenceKey((String)s);
        }
        return new CharSequenceKey(s.toString());
    }

    public static CharSequence empty(){
        return EMPTY;
    }
    
    private CharSequenceKey(String s) {
        char[] v = s.toCharArray();
        int n = v.length;
        byte[] b = new byte[n];
        for(int i = 0; i < n; i++){
            int o = v[i];
            if ( (o & 0xFF) != o){
                value = v;
                return;
            }
            b[i]= (byte)o;
        }
        value = b;
    }

    public int length() {
        if (value instanceof byte[]) {
            return ((byte[]) value).length;
        }
        return ((char[]) value).length;
    }

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

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return new CharSequenceKey(toString().substring(beginIndex, endIndex));
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
        public int compare(CharSequence o1, CharSequence o2) {
            if ((o1 instanceof CharSequenceKey) &&
                (o2 instanceof CharSequenceKey)){
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

    public int compareTo(CharSequenceKey o) {
        return Comparator.compare(this, o);
    }

    private static class CharSequenceComparatorIgnoreCase implements Comparator<CharSequence> {
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
}
