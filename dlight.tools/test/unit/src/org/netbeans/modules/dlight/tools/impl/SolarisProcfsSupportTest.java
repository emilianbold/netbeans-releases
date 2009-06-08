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
package org.netbeans.modules.dlight.tools.impl;

import org.junit.Test;
import org.netbeans.modules.dlight.tools.impl.SolarisProcfsSupport.Prusage;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class SolarisProcfsSupportTest {

    @Test
    public void testParsePrusage() {
        String[] dump = {
            "0000000 00000000 00000002 000008d9 1ce3a4f7",
            "0000020 000000f0 28a36465 00000000 00000000",
            "0000040 00000fd1 066405f8 0000006c 19941d43",
            "0000060 00000009 2ff62172 00000000 03cdbeab",
            "0000100 00000000 00ff9082 00000000 02403b5c",
            "0000120 00000000 001ecf10 00000002 202cecfa",
            "0000140 00000f57 31295da4 00000000 1726bb6c",
            "0000160 00000000 0000c598 00000000 00000000",
            "0000200 00000000 00000000 00000000 00000000",
            "0000220 00000000 00000000 00000000 00000000",
            "0000240 00000000 00000000 00000000 00000000",
            "0000260 00000000 00000000 00000000 00000050",
            "0000300 00000059 00000000 000062ae 00000f0b",
            "0000320 001cd024 21f65dd5 00000000 00000000",
            "0000340 00000000 00000000 00000000 00000000",
            "0000360 00000000 00000000 00000000 00000000",
            "0000400"
        };
        Prusage prusage = null;
        for (String line : dump) {
            Prusage prev = prusage;
            prusage = SolarisProcfsSupport.parsePrusage(line, prusage);
            if (prev == null) {
                assertNotNull(prusage);
            } else {
                assertSame(prev, prusage);
            }
        }
        assertNotNull(prusage);
        assertEquals(0, prusage.lwpid());
        assertEquals(2, prusage.count());
        assertNotNull(prusage.tstamp());
        assertEquals(0x000008d9, prusage.tstamp().sec());
        assertEquals(0x1ce3a4f7, prusage.tstamp().nsec());
        assertNotNull(prusage.rtime());
        assertEquals(0x00000fd1, prusage.rtime().sec());
        assertEquals(0x066405f8, prusage.rtime().nsec());
        assertNotNull(prusage.utime());
        assertEquals(0x0000006c, prusage.utime().sec());
        assertEquals(0x19941d43, prusage.utime().nsec());
        assertNotNull(prusage.stime());
        assertEquals(0x00000009, prusage.stime().sec());
        assertEquals(0x2ff62172, prusage.stime().nsec());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParsePrusageIncomplete() {
        SolarisProcfsSupport.parsePrusage("0000000 00000000 00000002 000008d9", null);
    }

    @Test(expected = NullPointerException.class)
    public void testParsePrusageNull() {
        SolarisProcfsSupport.parsePrusage(null, null);
    }
}
