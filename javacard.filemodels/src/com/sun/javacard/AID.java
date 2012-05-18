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
package com.sun.javacard;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an Application or Instance AID.
 *
 * @author Tim Boudreau
 */
public class AID {
    public static final String AID_AUTHORITY = "//aid/";
    private byte[] rid;
    private byte[] pix;
    private AID(byte[] rid, byte[] pix) {
        if (rid == null) {
            throw new NullPointerException("Null RID"); //NOI18N
        }
        if (pix == null) {
            throw new NullPointerException("Null PIX"); //NOI18N
        }
        if (rid.length != 5) {
            throw new IllegalArgumentException(
                    "RID must be exactly 5 bytes in length"); //NOI18N
        }
        if (pix.length > 11) {
            throw new IllegalArgumentException(
                    "PIX must be 0-11 bytes in length"); //NOI18N
        }
        this.rid = rid;
        this.pix = pix;
    }


    public AID increment() {
        byte[] newPix = getPix();
        if (newPix.length == 0) {
            newPix = new byte[] { 1 };
        } else {
            newPix[newPix.length -1]++;
        }
        return new AID (getRid(), newPix);
    }

    public byte[] getRid() {
        return rid.clone();
    }

    public byte[] getPix() {
        return pix.clone();
    }

    public String getRidAsString() {
        return getStringForByteArray(rid);
    }

    public String getPixAsString() {
        return getStringForByteArray(pix);
    }

    @Override
    public boolean equals (Object o) {
        if (o == this) return true;
        boolean res = o != null && AID.class == o.getClass();
        if (res) {
            AID other = (AID) o;
//            res = Arrays.equals(rid, other.rid) && Arrays.equals(pix,
//                    other.pix);
            res = other.toString().equals(toString());
        }
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 571;
        hash = 3881 * hash + Arrays.hashCode(this.rid);
        hash = 5209 * hash + Arrays.hashCode(this.pix);
        return hash;
    }

    @Override
    public String toString() {
        return AID_AUTHORITY + (getStringForByteArray(rid) +
                '/' + getStringForByteArray(pix)).toUpperCase(); //NOI18N
    }

//    static final Pattern AID_PARSE_PATTERN =
//            Pattern.compile("//aid/([0-9a-fA-F]+)/([0-9a-fA-F]{0,11})"); //NOI18N
    //The above pattern is more precise, but it will ignore trailing garbage
    //and bad characters in the PIX, giving us poorer error reporting
    static final Pattern AID_PARSE_PATTERN =
            Pattern.compile("//aid/((.+)/(.+)|(.+))"); //NOI18N

    public static AID parse(String aid) {
        if (aid == null) {
            throw new NullPointerException(Portability.getString(
                    "Null_aid")); //NOI18N
        }
        Matcher m = AID_PARSE_PATTERN.matcher(aid);
        if (m.lookingAt()) {
            if (m.groupCount() == 4) {
                String RID = m.group(2) != null ? m.group(2) : m.group(4);
                String PIX = m.group(3) != null ? m.group(3) : ""; //NOI18N
                if (RID.endsWith("/")) {
                    RID = RID.substring(0, RID.length() - 1);
                }
                if (RID.length() % 2 != 0) {
                    throw new IllegalArgumentException(
                            Portability.getString(
                            "RID_does_not_have_an_even_number_of_characters")); //NOI18N
                }
                if (PIX.length() % 2 != 0) {
                    throw new IllegalArgumentException(
                            Portability.getString(
                            "PIX_does_not_have_an_even_number_of_characters")); //NOI18N
                }
                if (RID.length() != 10) {
                    throw new IllegalArgumentException(
                            Portability.getString(
                            "RID_is_not_5_bytes_long")); //NOI18N
                }
                if (PIX.length() > 22) { //11 bytes is 22 hexadecimal digits
                    throw new IllegalArgumentException(
                            Portability.getString(
                            "Pix_too_long_-_must_be_0-11_bytes")); //NOI18N
                }
                byte[] rid = new byte[RID.length() / 2];
                try {
                    getByteArrayForString(RID, rid, 0);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(Portability.getString(
                            "BAD_RID_HEX", RID), nfe); //NOI18N
                }
                byte[] pix = new byte[PIX.length() / 2];
                try {
                    getByteArrayForString(PIX, pix, 0);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(Portability.getString(
                            "BAD_PIX_HEX", PIX), nfe); //NOI18N
                }
                for (char c : RID.toCharArray()) {
                    if (Character.isLowerCase(c)) {
                        String s = new String(new char[] { c });
                        throw new IllegalArgumentException(Portability.getString(
                                "LOWER_CASE_IN_RID", s)); //NOI18N
                    }
                }
                for (char c : PIX.toCharArray()) {
                    if (Character.isLowerCase(c)) {
                        String s = new String(new char[] { c });
                        throw new IllegalArgumentException(Portability.getString(
                                "LOWER_CASE_IN_PIX", s)); //NOI18N
                    }
                }
                return new AID(rid, pix);
            } else if (m.groupCount() == 1) {
                //0 length PIX is legal
                String RID = m.group(1);
                if (RID.length() % 2 != 0) {
                    throw new IllegalArgumentException(
                            Portability.getString(
                            "RID_does_not_have_an_even_number_of_characters")); //NOI18N
                }
                if (RID.length() != 10) {
                    throw new IllegalArgumentException(
                            Portability.getString(
                            "RID_is_not_5_bytes_long")); //NOI18N
                }
                for (char c : RID.toCharArray()) {
                    if (Character.isLowerCase(c)) {
                        String s = new String(new char[] { c });
                        throw new IllegalArgumentException(Portability.getString(
                                "LOWER_CASE_IN_RID", s)); //NOI18N
                    }
                }
                byte[] b = new byte[RID.length() / 2];
                try {
                    getByteArrayForString(RID, b, 0);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException(Portability.getString(
                            "BAD_RID_HEX", RID), nfe); //NOI18N
                }
                return new AID(b, new byte[0]);
            } else {
                throw new IllegalArgumentException(
                        Portability.getString(
                        "Could_not_find_an_AID_in_", aid)); //NOI18N
            }
        } else {
            throw new IllegalArgumentException(
                    Portability.getString(
                    "Could_not_find_an_AID_in_", aid)); //NOI18N
        }
    }

    public static AID generatePackageAid(byte[] RID, String packageName) {
        byte[] PIX = packageHash(packageName, 7);
        PIX[0] = firstByteOfPackagePIX(packageName.hashCode());
        return new AID(RID, PIX);
    }
    
    public static AID generateApplicationAid(byte[] RID, String fqn) {
        int ix = fqn.lastIndexOf('.');
        String pkg = null;
        String clazz = null;
        if (ix > 0) {
            pkg = fqn.substring(0, ix);
            if (ix != fqn.length() - 2) {
                clazz = fqn.substring(ix+1);
            } else {
                clazz = "Foobar";
            }
        } else {
            pkg = "com.foo.bar";
            clazz = "Baz";
        }
        return generateApplicationAid(RID, pkg, clazz);
    }

    public static AID generateApplicationAid(byte[] RID, String packageName, String clazz) {
        byte[] PIX = packageHash(packageName, 5);
        if (PIX.length == 0) { //Issue #177837 - if you enter . for the
            PIX = packageHash("com.foo.bar.baz.myapp", 5);
        }
        PIX[PIX.length - 1] = byteHash(clazz);
        return new AID(RID, PIX);
    }

    public static AID generateInstanceAid(byte[] RID, String packageName, String clazz) {
        byte[] PIX = new byte[7];
        Random r = new Random ((packageName + clazz).hashCode());
        r.nextBytes(PIX);
        PIX[0] = firstByteOfInstancePIX(clazz.hashCode());
        return new AID(RID, PIX);
    }

    private static byte firstByteOfInstancePIX(int seed) {
        //We want a random number, but we want it to be consistent, so use
        //the hash code of the string we're generating for
        Random r = new Random(seed);
        int range = 0xFE - 0x80;
        byte result = (byte) (r.nextInt(range) + 0x80);
        return result;
    }

    private static byte firstByteOfPackagePIX(int seed) {
        //We want a random number, but we want it to be consistent, so use
        //the hash code of the string we're generating for
        Random r = new Random(seed);
        int range = 0x7F;
        byte result = (byte) (r.nextInt(range));
        return result;
    }

    private static byte[] packageHash(String packageName, int maxBytes) {
        if (packageName.length() <= 1) { //default package, randomize
            Random r = new Random (System.currentTimeMillis());
            byte[] result = new byte[maxBytes];
            r.nextBytes(result);
            return result;
        }
        String[] parts = packageName.split("\\."); //NOI18N
        byte[] result = new byte[Math.min(maxBytes, parts.length)];
        for (int i = 0; i < result.length; i++) {
            result[i] = byteHash(parts[i]);
        }
        return result;
    }

    private static byte byteHash(String packageNamePart) {
        byte[] b = packageNamePart.getBytes();
        byte min = Byte.MAX_VALUE;
        byte max = Byte.MIN_VALUE;
        for (byte aB : b) {
            min = (byte) Math.min (min, aB);
            max = (byte) Math.max (max, aB);
        }
        byte multiplier;
        if (min != max) { //avoid /0 error
            multiplier = (byte) (0xFF / (max - min));
        } else {
            multiplier = 0x01;
        }
        byte result = 0;
        for (int i=0; i < b.length; i++) {
            byte next = (byte) (b[i] * multiplier);
            if ((i % 2) == 0) {
                next +=1;
            }
            result ^=next;
        }
        return result;
    }



    public static int getByteArrayForString(String number, byte[] outputArray, int offset) {
        int length = number.length();
        if ((length % 2 != 0)) {
            throw new NumberFormatException("Odd number of digits");
        }
        for (int startIndex = 0; startIndex < number.length(); startIndex += 2) {
            String smallNumber = number.substring(startIndex, startIndex + 2);
            outputArray[offset++] = (byte) Integer.parseInt(smallNumber, 16);
        }
        return offset;
    }

    public static String getStringForByteArray(byte[] input) {
        StringBuffer sb = new StringBuffer();
        for (byte num : input) {
            String hex = Integer.toHexString(num & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
        }
        return sb.toString().toUpperCase();
    }
}
