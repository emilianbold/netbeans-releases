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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class APDUSenderTest {
    
    @Test
    public void testRegexp() {
        String[] contents = new String[] {
            "0x80",
            "0xCA",
            "0x00",
            "0x00",
            "4",
            "T",
            "E",
            "S",
            "T\\u000A",
        };
        Matcher m = new APDUParser("0x80 0xCA 0x00 0x00 4, T, E, S, T\\u000A").matcher();
        List<String> all = new ArrayList<String>();
        while (m.find()) {
            if (m.group(1) != null && !"".equals(m.group(1))){
                all.add(m.group(1));
            }
        }
        assertFalse (all.isEmpty());
        assertEquals (contents.length, all.size());
        assertEquals (Arrays.asList(contents), all);
    }

    @Test
    public void testParseString() throws ShellException {
        String test = "0x80 0xCA 0x00 0x00 4, T, E, S, T\\u000A";
        byte[] expect = new byte[] {
            (byte) 0x000080,
            (byte) 0x0000CA,
            0,
            0,
            4,
            (byte) 'T',
            (byte) 'E',
            (byte) 'S',
            (byte) 'T',
            (byte) 0x00000A,
        };
        byte[] got = new APDUParser(test).bytes();
        assertEquals (compareArrays(expect, got), expect.length, got.length);
        assertTrue (compareArrays(expect, got), Arrays.equals(expect, got));
    }

    private String compareArrays(byte[] a, byte[] b) {
        StringBuilder sb = new StringBuilder();
        int max = Math.min(a.length, b.length);
        for (int i=0; i < max; i++) {
            sb.append (a[i]);
            sb.append (':');
            sb.append (b[i]);
            if (i != max - 1) {
                sb.append (',');
            }
        }
        return sb.toString();
    }
//
//    @Test
//    public void testParseToken() throws ShellException {
//        byte expect = (byte) 0xFF;
//        assertEquals (expect, APDUSender.parseToken("0xFF"));
//        expect = (byte) 'T';
//        assertEquals (expect, APDUSender.parseToken("T"));
//        expect = 23;
//        assertEquals (expect, APDUSender.parseToken("23"));
//    }
}