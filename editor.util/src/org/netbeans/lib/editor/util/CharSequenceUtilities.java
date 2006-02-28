/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.util;

/**
 * Utility methods related to character sequences.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CharSequenceUtilities {
    
    private CharSequenceUtilities() {
        // no instances
    }

    /**
     * Compute {@link String}-like hashcode over given {@link CharSequence}.
     *
     * @param text character sequence for which the hashcode is being computed.
     * @return hashcode of the given character sequence.
     */
    public static int stringLikeHashCode(CharSequence text) {
        int len = text.length();

        int h = 0;
        for (int i = 0; i < len; i++) {
            h = 31 * h + text.charAt(i);
        }
        return h;
    }

    /**
     * Method that compares a given character sequence to another object.
     * The match is successful if the other object is a character sequence as well
     * and both character sequences contain the same characters.
     *
     * @param text character sequence being compared to the given object.
     *  It must not be <code>null</code>.
     * @param o object to be compared to the character sequence.
     *  It can be <code>null</code>.
     * @return true if both parameters are non-null
     *  and they are equal in String-like manner.
     */
    public static boolean equals(CharSequence text, Object o) {
        if (text == o) {
            return true;
        }

        if (o instanceof CharSequence) { // both non-null
            CharSequence text2 = (CharSequence)o;
            int len = text.length();
            if (len == text2.length()) {
                for (int i = len - 1; i >= 0; i--) {
                    if (text.charAt(i) != text2.charAt(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Test whether whether the given character sequences
     * represent the same text.
     * <br>
     * The match is successful if the contained characters
     * of the two character sequences are the same.
     *
     * @param text1 first character sequence being compared.
     *  It must not be <code>null</code>.
     * @param o object to be compared to the character sequence.
     *  It must not be <code>null</code>.
     * @return true if both parameters are equal in String-like manner.
     */
    public static boolean textEquals(CharSequence text1, CharSequence text2) {
        if (text1 == text2) {
            return true;
        }
        int len = text1.length();
        if (len == text2.length()) {
            for (int i = len - 1; i >= 0; i--) {
                if (text1.charAt(i) != text2.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Create a string from the given character sequence by first creating
     * a <code>StringBuffer</code> and appending the whole character sequence
     * char-by-char.
     * <br>
     * The method does not call <code>toString()</code> on the given character
     * sequence.
     *
     * @param text character sequence for which the <code>String</code> form
     *  should be created.
     * @return string representation of the character sequence.
     */
    public static String toString(CharSequence text) {
        StringBuffer sb = new StringBuffer(text.length());
        append(sb, text);
        return sb.toString();
    }

    public static String toString(CharSequence text, int start, int end) {
        if (start < 0) {
            throw new IndexOutOfBoundsException("start=" + start + " < 0"); // NOI18N
        }
        if (end < start) {
            throw new IndexOutOfBoundsException("end=" + end + " < start=" + start); // NOI18N
        }
        if (end > text.length()) {
            throw new IndexOutOfBoundsException("end=" + end + " > length()=" + text.length()); // NOI18N
        }

        StringBuffer sb = new StringBuffer(end - start);
        while (start < end) {
            sb.append(text.charAt(start++));
        }
        return sb.toString();
    }
    
    public static void append(StringBuffer sb, CharSequence text) {
        int textLength = text.length();
        for (int i = 0; i < textLength; i++) {
            sb.append(text.charAt(i));
        }
    }
    
    public static void append(StringBuffer sb, CharSequence text, int start, int end) {
        if (start < 0) {
            throw new IndexOutOfBoundsException("start=" + start + " < 0"); // NOI18N
        }
        if (end < start) {
            throw new IndexOutOfBoundsException("end=" + end + " < start=" + start); // NOI18N
        }
        if (end > text.length()) {
            throw new IndexOutOfBoundsException("end=" + end + " > length()=" + text.length()); // NOI18N
        }

        while (start < end) {
            sb.append(text.charAt(start++));
        }
    }

    public static int indexOf(CharSequence text, int ch) {
	return indexOf(text, ch, 0);
    }

    public static int indexOf(CharSequence text, int ch, int fromIndex) {
	int length = text.length();
	while (fromIndex < length) {
	    if (text.charAt(fromIndex) == ch) {
		return fromIndex;
	    }
            fromIndex++;
	}
	return -1;
    }
    
    public static int lastIndexOf(CharSequence text, int ch) {
	return lastIndexOf(text, ch, text.length() - 1);
    }

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

    public static void debugChar(StringBuffer sb, char ch) {
        switch (ch) {
            case '\n':
                sb.append("\n");
                break;
            case '\t':
                sb.append("\t");
                break;
            case '\r':
                sb.append("\r");
                break;
            case '\\':
                sb.append("\\");
                break;
            default:
                sb.append(ch);
                break;
        }
    }
    
    public static void debugText(StringBuffer sb, CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            debugChar(sb, text.charAt(i));
        }
    }
    
    public static String debugText(CharSequence text) {
        StringBuffer sb = new StringBuffer();
        debugText(sb, text);
        return sb.toString();
    }

}
