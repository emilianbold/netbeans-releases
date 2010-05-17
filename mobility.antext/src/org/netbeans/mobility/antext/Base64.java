/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.mobility.antext;
/** This class provides Base64 encoding/decoding.
 */
public class Base64
{
    private Base64()
    {
        //Avoid instantiation of this class
    }
    
    /** Encodes datas using base64 encoding.
     * @param abyte0 data for encoding
     * @return encoded string
     */
    static String encode(final byte abyte0[])
    {
        final StringBuffer stringbuffer = new StringBuffer();
        for(int i = 0; i < abyte0.length; i += 3)
            stringbuffer.append(encodeBlock(abyte0, i));
        
        return stringbuffer.toString();
    }
    
    private static char[] encodeBlock(final byte abyte0[], final int i)
    {
        int j = 0;
        final int k = abyte0.length - i - 1;
        final int l = k < 2 ? k : 2;
        for(int i1 = 0; i1 <= l; i1++)
        {
            final byte byte0 = abyte0[i + i1];
            final int j1 = byte0 >= 0 ? ((int) (byte0)) : byte0 + 256;
            j += j1 << 8 * (2 - i1);
        }
        
        char ac[] = new char[4];
        for(int k1 = 0; k1 < 4; k1++)
        {
            final int l1 = j >>> 6 * (3 - k1) & 0x3f;
            ac[k1] = getChar(l1);
        }
        
        if(k < 1)
            ac[2] = '=';
        if(k < 2)
            ac[3] = '=';
        return ac;
    }
    
    private static char getChar(final int i)
    {
        if(i >= 0 && i <= 25)
            return (char)(65 + i);
        if(i >= 26 && i <= 51)
            return (char)(97 + (i - 26));
        if(i >= 52 && i <= 61)
            return (char)(48 + (i - 52));
        if(i == 62)
            return '+';
        return i != 63 ? '?' : '/';
    }
    
    /** Decode string using Base64 encoding.
     * @param s string for decoding
     * @return decoded data
     */
    static byte[] decode(final String s)
    {
        if (s.length() == 0) return new byte[0];
        int i = 0;
        for(int j = s.length() - 1; j > 0 && s.charAt(j) == '='; j--)
            i++;
        
        final int k = (s.length() * 6) / 8 - i;
        byte abyte0[] = new byte[k];
        int l = 0;
        for(int i1 = 0; i1 < s.length(); i1 += 4)
        {
            final int j1 = (getValue(s.charAt(i1)) << 18) + (getValue(s.charAt(i1 + 1)) << 12) + (getValue(s.charAt(i1 + 2)) << 6) + getValue(s.charAt(i1 + 3));
            for(int k1 = 0; k1 < 3 && l + k1 < abyte0.length; k1++)
                abyte0[l + k1] = (byte)(j1 >> 8 * (2 - k1) & 0xff);
            
            l += 3;
        }
        return abyte0;
    }
    
    private static int getValue(final char c)
    {
        if(c >= 'A' && c <= 'Z')
            return c - 65;
        if(c >= 'a' && c <= 'z')
            return (c - 97) + 26;
        if(c >= '0' && c <= '9')
            return (c - 48) + 52;
        if(c == '+')
            return 62;
        if(c == '/')
            return 63;
        return c != '=' ? -1 : 0;
    }
}
