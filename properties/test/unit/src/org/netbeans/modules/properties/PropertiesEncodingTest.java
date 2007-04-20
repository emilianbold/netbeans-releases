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

package org.netbeans.modules.properties;

import java.nio.charset.CharacterCodingException;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.properties.PropertiesEncoding.PropCharset;
import org.netbeans.modules.properties.PropertiesEncoding.PropCharsetEncoder;
import org.netbeans.modules.properties.PropertiesEncoding.PropCharsetDecoder;

/**
 * 
 * @author  Marian Petras
 */
public class PropertiesEncodingTest extends NbTestCase {
    
    public PropertiesEncodingTest() {
        super("Encoding test");
    }
    
    public void testCharEncodingOfSingleChar() {
        final PropCharsetEncoder encoder
                = new PropCharsetEncoder(new PropCharset());
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x5c),    /* backslash */
                new byte[] {'\\', '\\'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x09),    /* tab */
                new byte[] {'\\', 't'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0c),    /* FF */
                new byte[] {'\\', 'f'}));
//        assertTrue(Arrays.equals(
//                encoder.encodeCharForTests((char) 0x0a),    /* NL */
//                new byte[] {'\\', 'n'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0d),    /* CR */
                new byte[] {'\\', 'r'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x00),
                new byte[] {'\\', 'u', '0', '0', '0', '0'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x01),
                new byte[] {'\\', 'u', '0', '0', '0', '1'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0b),
                new byte[] {'\\', 'u', '0', '0', '0', 'b'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x19),
                new byte[] {'\\', 'u', '0', '0', '1', '9'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x20),
                new byte[] {' '}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x21),
                new byte[] {'!'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x32),
                new byte[] {'2'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x43),
                new byte[] {'C'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x54),
                new byte[] {'T'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x65),
                new byte[] {'e'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x78),
                new byte[] {'x'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x7d),
                new byte[] {'}'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x7e),
                new byte[] {'~'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x7f),
                new byte[] {'\\', 'u', '0', '0', '7', 'f'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x81),
                new byte[] {'\\', 'u', '0', '0', '8', '1'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x89),
                new byte[] {'\\', 'u', '0', '0', '8', '9'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x8f),
                new byte[] {'\\', 'u', '0', '0', '8', 'f'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x90),
                new byte[] {'\\', 'u', '0', '0', '9', '0'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xa0),
                new byte[] {'\\', 'u', '0', '0', 'a', '0'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xad),
                new byte[] {'\\', 'u', '0', '0', 'a', 'd'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xcb),
                new byte[] {'\\', 'u', '0', '0', 'c', 'b'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xd6),
                new byte[] {'\\', 'u', '0', '0', 'd', '6'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xec),
                new byte[] {'\\', 'u', '0', '0', 'e', 'c'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xf0),
                new byte[] {'\\', 'u', '0', '0', 'f', '0'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xfd),
                new byte[] {'\\', 'u', '0', '0', 'f', 'd'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xfe),
                new byte[] {'\\', 'u', '0', '0', 'f', 'e'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xff),
                new byte[] {'\\', 'u', '0', '0', 'f', 'f'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0100),
                new byte[] {'\\', 'u', '0', '1', '0', '0'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0101),
                new byte[] {'\\', 'u', '0', '1', '0', '1'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x016e),
                new byte[] {'\\', 'u', '0', '1', '6', 'e'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0777),
                new byte[] {'\\', 'u', '0', '7', '7', '7'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0778),
                new byte[] {'\\', 'u', '0', '7', '7', '8'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0877),
                new byte[] {'\\', 'u', '0', '8', '7', '7'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x0a46),
                new byte[] {'\\', 'u', '0', 'a', '4', '6'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x774d),
                new byte[] {'\\', 'u', '7', '7', '4', 'd'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x8000),
                new byte[] {'\\', 'u', '8', '0', '0', '0'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0x8800),
                new byte[] {'\\', 'u', '8', '8', '0', '0'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xabcd),
                new byte[] {'\\', 'u', 'a', 'b', 'c', 'd'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xfffe),
                new byte[] {'\\', 'u', 'f', 'f', 'f', 'e'}));
        assertTrue(Arrays.equals(
                encoder.encodeCharForTests((char) 0xffff),
                new byte[] {'\\', 'u', 'f', 'f', 'f', 'f'}));
    }
    
    public void testCharEncodingOfString() throws CharacterCodingException {
        final PropCharsetEncoder encoder
                = new PropCharsetEncoder(new PropCharset());
        compare(encoder.encodeStringForTests(""),
                new byte[] {});
        compare(encoder.encodeStringForTests("a"),
                new byte[] {'a'});
        compare(encoder.encodeStringForTests("\\"),     //pending character
                new byte[] {'\\', '\\'});
        compare(encoder.encodeStringForTests("\\\\"),
                new byte[] {'\\', '\\'});
        compare(encoder.encodeStringForTests("\\t"),
                new byte[] {'\\', 't'});
    }
    
    public void testCharDecoding() {
        final PropCharsetDecoder decoder
                = new PropCharsetDecoder(new PropCharset());
        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', '\\'}),   /* backslash */
                new char[] {'\\', '\\'}));
        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 't'}),    /* tab */
                new char[] {'\\', 't'}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'f'}),    /* FF */
                new char[] {'\\', 'f'}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'n'}),    /* NL */
                new char[] {'\\', 'n'}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'r'}),    /* CR */
                new char[] {'\\', 'r'}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '0'}),
                new char[] {(char) 0x00}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', '1'}),
                new char[] {(char) 0x01}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '0', 'b'}),
                new char[] {(char) 0x0b}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '1', '9'}),
                new char[] {(char) 0x19}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {' '}),
                new char[] {(char) 0x20}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'!'}),
                new char[] {(char) 0x21}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'2'}),
                new char[] {(char) 0x32}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'C'}),
                new char[] {(char) 0x43}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'T'}),
                new char[] {(char) 0x54}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'e'}),
                new char[] {(char) 0x65}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'x'}),
                new char[] {(char) 0x78}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'}'}),
                new char[] {(char) 0x7d}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'~'}),
                new char[] {(char) 0x7e}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '7', 'f'}),
                new char[] {(char) 0x7f}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '8', '1'}),
                new char[] {(char) 0x81}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '8', '9'}),
                new char[] {(char) 0x89}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '8', 'f'}),
                new char[] {(char) 0x8f}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', '9', '0'}),
                new char[] {(char) 0x90}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'a', '0'}),
                new char[] {(char) 0xa0}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'a', 'd'}),
                new char[] {(char) 0xad}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'c', 'b'}),
                new char[] {(char) 0xcb}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'd', '6'}),
                new char[] {(char) 0xd6}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'e', 'c'}),
                new char[] {(char) 0xec}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'f', '0'}),
                new char[] {(char) 0xf0}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'f', 'd'}),
                new char[] {(char) 0xfd}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'f', 'e'}),
                new char[] {(char) 0xfe}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '0', 'f', 'f'}),
                new char[] {(char) 0xff}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '1', '0', '0'}),
                new char[] {(char) 0x0100}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '1', '0', '1'}),
                new char[] {(char) 0x0101}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '1', '6', 'e'}),
                new char[] {(char) 0x016e}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '7', '7', '7'}),
                new char[] {(char) 0x0777}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '7', '7', '8'}),
                new char[] {(char) 0x0778}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', '8', '7', '7'}),
                new char[] {(char) 0x0877}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '0', 'a', '4', '6'}),
                new char[] {(char) 0x0a46}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '7', '7', '4', 'd'}),
                new char[] {(char) 0x774d}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '8', '0', '0', '0'}),
                new char[] {(char) 0x8000}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', '8', '8', '0', '0'}),
                new char[] {(char) 0x8800}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'a', 'b', 'c', 'd'}),
                new char[] {(char) 0xabcd}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'f', 'f', 'f', 'e'}),
                new char[] {(char) 0xfffe}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'f', 'f', 'f', 'f'}),
                new char[] {(char) 0xffff}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'A', 'B', 'C', 'D'}),
                new char[] {(char) 0xabcd}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'F', 'F', 'F', 'E'}),
                new char[] {(char) 0xfffe}));

        assertTrue(Arrays.equals(
                decoder.decodeBytesForTests(new byte[] {'\\', 'u', 'F', 'F', 'F', 'F'}),
                new char[] {(char) 0xffff}));

    }

    private void compare(byte[] actual, byte[] expected) {
        if (!Arrays.equals(expected, actual)) {
            fail("byte arrays do not match"
                 + " - expected: " + showByteArray(expected)
                 + ", actual: " + showByteArray(actual));
        }
    }
    
    private static String showByteArray(final byte[] arr) {
        StringBuilder buf = new StringBuilder(3 * arr.length + 5);
        buf.append('{');
        for (int i = 0; i < arr.length - 1; i++) {
            buf.append(getVisualRepresentation(arr[i]));
            buf.append(',');
        }
        if (arr.length != 0) {
            buf.append(getVisualRepresentation(arr[arr.length - 1]));
        }
        buf.append('}');
        return buf.toString();
    }
    
    private static char[] getVisualRepresentation(byte b) {
        if (b < 0x20) {
            char[] result = new char[4];
            int off = 0;
            result[off++] = '<';
            result[off++] = getCharForHexadec((b >>> 4) & 0x0f);
            result[off++] = getCharForHexadec(    b     & 0x0f);
            result[off++] = '>';
            return result;
        } else {
            return new char[] {(char) b};
        }
    }
    
    private static char getCharForHexadec(int b) {
        assert b < 0x10;
        return (b < 10) ? (char) ('0' + (b & 0x0f))
                        : (char) ('a' + ((b & 0x0f) - 10));
    }

}
