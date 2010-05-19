/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javacard.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.NbBundle;

/**
 * Parses the bytes/chars the user enters in accordance with the specification
 * in the console help:
 *
 * 0xNN - hex
 * 0NN - octal
 * NN - integer
 * other - byte value of char or a unicode-escaped 6-character string
 *
 * @author Tim Boudreau
 */
final class APDUParser {

    private final String text;
    final Pattern SPLIT = Pattern.compile("([a-zA-Z_0-9\\\\]*)\\s*", Pattern.DOTALL | Pattern.MULTILINE); //NOI18N
    final Pattern UNICODE_SPLIT = Pattern.compile("\\\\u(\\p{XDigit}{4}+)"); //NOI18N
    final Pattern NUMBER = Pattern.compile("[0-9]*"); //NOI18N
    final Pattern OCTAL = Pattern.compile("0[0-9]*"); //NOI18N
    final Pattern HEX = Pattern.compile("0x[0-9A-F]++", Pattern.CASE_INSENSITIVE); //NOI18N
    APDUParser(String text) {
        this.text = text;
    }

    final Matcher matcher() {
        return SPLIT.matcher(text);
    }

    public byte[] bytes() throws ShellException {
        List<Byte> bytes = new ArrayList<Byte>(20);
        Matcher m = matcher();
        while (m.find()) {
            if (m.groupCount() == 1) {
                String s = m.group(1);
                if (s != null && !"".equals(s)) {
                    tokenize(s, bytes);
                }
            }
        }
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }

    private void tokenize(String s, List<Byte> result) throws ShellException {
        if (HEX.matcher(s).matches()) {
            result.add (parseHex(s));
        } else if (NUMBER.matcher(s).matches()) {
            if (OCTAL.matcher(s).matches()) {
                result.add (parseOctal(s));
            } else {
                result.add (parseInteger(s));
            }
        } else if (s.length() == 1) {
            char c = s.charAt(0);
            result.add (parseChar(s, c));
        } else {
            parseString (s, result);
        }
    }

    private void parseString(String s, List<Byte> result) throws ShellException {
        int len = s.length();
        Matcher um = UNICODE_SPLIT.matcher(s);
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                if (um.find(i)) {
                    String rawHex = um.group(1);
                    result.add(parseRawHex(rawHex));
                    i+= 6;
                } else {
                    result.add (parseChar(s, c));
                }
            } else {
                result.add (parseChar(s, c));
            }
        }
    }

    private byte parseOctal(String s) throws ShellException {
        int result = Integer.parseInt(s.substring(1), 8);
        checkRange (s, result);
        return (byte) result;
    }

    private byte parseHex(String s) throws ShellException {
        return parseRawHex (s.substring(2));
    }

    private byte parseRawHex(String s) throws ShellException {
        int result = Integer.parseInt(s, 16);
        checkRange (s, result);
        return (byte) result;
    }

    private byte parseInteger(String s) throws ShellException {
        int result = Integer.parseInt(s);
        checkRange (s, result);
        return (byte) result;
    }
    
    private byte parseChar(String s, char c) throws ShellException {
        assert !Character.isWhitespace(c) : "Whitespace parsed as char in '" + //NOI18N
                s + "'"; //NOI18N
        checkRange (s, c);
        return (byte) c;
    }

    private void checkRange(String s, int result) throws ShellException {
        if (result > 255) {
            tooBig (s, result);
        } else if (result < 0) {
            tooSmall (s, result);
        }
    }

    private void tooBig(String s, int result) throws ShellException {
        throw new ShellException (NbBundle.getMessage(APDUParser.class, 
                "ERR_VAL_TOO_LARGE", s, result)); //NOI18N
    }

    private void tooSmall(String s, int result) throws ShellException {
        throw new ShellException (NbBundle.getMessage(APDUParser.class, 
                "ERR_VAL_TOO_SMALL", s, result)); //NOI18N
    }

}
