/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.utils.cache;

/**
 * Utility methods related to character sequences.
 * @author Vladimir Kvashin
 */
public class CharSequenceUtils {

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

    /**
     * Implementation of {@link String#lastIndexOf(String)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq) {
        return lastIndexOf(text, seq, text.length());
    }

    /**
     * Implementation of {@link String#lastIndexOf(String,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, CharSequence seq, int fromIndex) {
        int textLength = text.length();
        int seqLength = seq.length();
        int rightIndex = textLength - seqLength;
	if (fromIndex < 0) {
	    return -1;
	}
	if (fromIndex > rightIndex) {
	    fromIndex = rightIndex;
	}
	// empty string always matches
	if (seqLength == 0) {
	    return fromIndex;
	}

        int strLastIndex = seqLength - 1;
	char strLastChar = seq.charAt(strLastIndex);
	int min = seqLength - 1;
	int i = min + fromIndex;

    startSearchForLastChar:
	while (true) {
	    while (i >= min && text.charAt(i) != strLastChar) {
		i--;
	    }

	    if (i < min) {
		return -1;
	    }
	    int j = i - 1;
	    int start = j - (seqLength - 1);
	    int k = strLastIndex - 1;

	    while (j > start) {
	        if (text.charAt(j--) != seq.charAt(k--)) {
		    i--;
		    continue startSearchForLastChar;
		}
	    }
	    return start + 1;
	}
    }

    /**
     * Implementation of {@link String#lastIndexOf(int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch) {
	return lastIndexOf(text, ch, text.length() - 1);
    }

    /**
     * Implementation of {@link String#lastIndexOf(int,int)} for character sequences.
     */
    public static int lastIndexOf(CharSequence text, int ch, int fromIndex) {
        if (fromIndex > text.length() - 1) {
            fromIndex = text.length() - 1;
        }
	while (fromIndex >= 0) {
	    if (text.charAt(fromIndex) == ch) {
		return fromIndex;
	    }
            fromIndex--;
	}
	return -1;
    }

    /**
     * Implementation of {@link String#startsWith(String)} for character sequences.
     */
    public static boolean startsWith(CharSequence text, CharSequence prefix) {
        int p_length = prefix.length();
        if (p_length > text.length()) {
            return false;
        }
        for (int x = 0; x < p_length; x++) {
            if (text.charAt(x) != prefix.charAt(x))
                return false;
        }
        return true;
    }

    /**
     * Implementation of {@link String#endsWith(String)} for character sequences.
     */
    public static boolean endsWith(CharSequence text, CharSequence suffix) {
        int s_length = suffix.length();
        int text_length = text.length();
        if (s_length > text_length) {
            return false;
        }
        for (int x = 0; x < s_length; x++) {
            if (text.charAt(text_length - s_length + x) != suffix.charAt(x))
                return false;
        }
        return true;
    }

}
