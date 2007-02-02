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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.test;

import java.util.Random;

public final class RandomCharDescriptor {

    public static RandomCharDescriptor singleChar(char ch, double ratio) {
        return new RandomCharDescriptor(new char[] {ch}, ratio);
    }

    public static RandomCharDescriptor charRange(char firstChar, char lastChar, double ratio) {
        char[] chars = new char[lastChar - firstChar + 1];
        int index = 0;
        while (firstChar <= lastChar) {
            chars[index++] = firstChar++;
        }
        return new RandomCharDescriptor(chars, ratio);
    }
    
    public static RandomCharDescriptor chars(char[] chars, double ratio) {
        return new RandomCharDescriptor(chars, ratio);
    }

    public static RandomCharDescriptor accepted(Acceptor acceptor, double ratio) {
        return new RandomCharDescriptor(acceptedChars(acceptor), ratio);
    }
    

    public static RandomCharDescriptor union(RandomCharDescriptor descriptor1,
    RandomCharDescriptor descriptor2, double ratio) {
        return new RandomCharDescriptor(mergeChars(descriptor1.chars, descriptor2.chars), ratio);
    }

    public static RandomCharDescriptor digit(double ratio) {
        return charRange('0', '9', ratio);
    }
    
    public static RandomCharDescriptor letter(double ratio) {
        return union(letterLowercase(ratio), letterUppercase(ratio), ratio);
    }
    
    public static RandomCharDescriptor letterLowercase(double ratio) {
        return charRange('a', 'z', ratio);
    }
    
    public static RandomCharDescriptor letterUppercase(double ratio) {
        return charRange('A', 'Z', ratio);
    }
    
    public static RandomCharDescriptor space(double ratio) {
        return singleChar(' ', ratio);
    }
    
    public static RandomCharDescriptor cr(double ratio) {
        return singleChar('\r', ratio);
    }
    
    public static RandomCharDescriptor lf(double ratio) {
        return singleChar('\n', ratio);
    }
    
    public static RandomCharDescriptor whitespace(double ratio) {
        return chars(new char[] { ' ', '\t', '\r', '\n' }, ratio);
    }
    
    public static RandomCharDescriptor letterUnicode(double ratio) {
        return accepted(new Acceptor() {
            public boolean isAccepted(char ch) {
                return Character.isLetter(ch);
            }
        }, ratio);
    }
    
    public static RandomCharDescriptor digitUnicode(double ratio) {
        return accepted(new Acceptor() {
            public boolean isAccepted(char ch) {
                return Character.isDigit(ch);
            }
        }, ratio);
    }

    public static RandomCharDescriptor whitespaceUnicode(double ratio) {
        return accepted(new Acceptor() {
            public boolean isAccepted(char ch) {
                return Character.isWhitespace(ch);
            }
        }, ratio);
    }
    
    public static RandomCharDescriptor javaIdentifierStart(double ratio) {
        return accepted(new Acceptor() {
            public boolean isAccepted(char ch) {
                return Character.isJavaIdentifierStart(ch);
            }
        }, ratio);
    }

    public static RandomCharDescriptor javaIdentifierPart(double ratio) {
        return accepted(new Acceptor() {
            public boolean isAccepted(char ch) {
                return Character.isJavaIdentifierPart(ch);
            }
        }, ratio);
    }

    public static RandomCharDescriptor anyChar(double ratio) {
        return accepted(new Acceptor() {
            public boolean isAccepted(char ch) {
                return true;
            }
        }, ratio);
    }

    private char[] chars;
    
    private double ratio;
    
    private RandomCharDescriptor(char[] chars, double ratio) {
        this.chars = chars;
        this.ratio = ratio;
    }
    
    /**
     * Return random char from the chars defined in this descriptor.
     */
    public char randomChar(Random random) {
        return chars[random.nextInt(chars.length)];
    }

    public double ratio() {
        return ratio;
    }
    
    private static char[] mergeChars(char[] chars1, char[] chars2) {
        char[] chars = new char[chars1.length + chars2.length];
        System.arraycopy(chars1, 0, chars, 0, chars1.length);
        System.arraycopy(chars2, 0, chars, chars1.length, chars2.length);
        return chars;
    }
    
    private static char[] acceptedChars(Acceptor acceptor) {
        int count = 0;
        // Cannot use for(;;) 0 <= ch <= Character.MAX_VALUE => would never break
        char ch = 0;
        do {
            if (acceptor.isAccepted(ch)) {
                count++;
            }
            ch++;
        } while (ch != 0);

        char chars[] = new char[count];
        count = 0;
        ch = 0;
        do {
            if (acceptor.isAccepted(ch)) {
                chars[count++] = ch;
            }
            ch++;
        } while (ch != 0);

        return chars;
    }

    public interface Acceptor {
        
        boolean isAccepted(char ch);

    }
    
}