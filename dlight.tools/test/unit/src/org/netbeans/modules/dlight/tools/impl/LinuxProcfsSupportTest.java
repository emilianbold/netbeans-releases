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

import java.math.BigInteger;
import org.junit.Test;
import org.netbeans.modules.dlight.tools.impl.LinuxProcfsSupport.CpuStat;
import org.netbeans.modules.dlight.tools.impl.LinuxProcfsSupport.ProcessStat;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class LinuxProcfsSupportTest {

    public LinuxProcfsSupportTest() {
    }

    @Test
    public void testParseCpuStat() {
        CpuStat cpu = LinuxProcfsSupport.parseCpuStat("cpu  328580 17 62773 2588401 225175 2687 3643 0 0");
        assertEquals(BigInteger.valueOf(328580), cpu.user());
        assertEquals(BigInteger.valueOf(17), cpu.nice());
        assertEquals(BigInteger.valueOf(62773), cpu.system());
        assertEquals(BigInteger.valueOf(2588401), cpu.idle());
        assertEquals(BigInteger.valueOf(225175), cpu.iowait());
        assertEquals(BigInteger.valueOf(2687), cpu.irq());
        assertEquals(BigInteger.valueOf(3643), cpu.softirq());
        assertEquals(BigInteger.ZERO, cpu.steal());
        assertEquals(BigInteger.ZERO, cpu.guest());
        assertEquals(BigInteger.valueOf(3211276), cpu.all());
    }

    @Test
    public void testParseCpuStatMandatory() {
        CpuStat cpu = LinuxProcfsSupport.parseCpuStat("cpu  166207 9 31650 1308271");
        assertEquals(BigInteger.valueOf(166207), cpu.user());
        assertEquals(BigInteger.valueOf(9), cpu.nice());
        assertEquals(BigInteger.valueOf(31650), cpu.system());
        assertEquals(BigInteger.valueOf(1308271), cpu.idle());
        assertNull(cpu.iowait());
        assertNull(cpu.irq());
        assertNull(cpu.softirq());
        assertNull(cpu.steal());
        assertNull(cpu.guest());
        assertEquals(BigInteger.valueOf(1506137), cpu.all());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseCpuStatIncomplete() {
        LinuxProcfsSupport.parseCpuStat("cpu 1234 5678 9000");
    }

    @Test(expected = NullPointerException.class)
    public void testParseCpuStatNull() {
        LinuxProcfsSupport.parseCpuStat(null);
    }

    @Test
    public void testParseProcessStat() {
        ProcessStat proc = LinuxProcfsSupport.parseProcessStat("28601 (java) S 28470 2698 2698 0 -1 4202496 1263184 338414 45 57 21887 2104 1177 195 20 0 30 0 1568161 1366188032 165844 18446744073709551615 1073741824 1073778376 140734855863392 18446744073709551615 139712515037029 0 0 536870914 16801021 18446744073709551615 0 0 17 0 0 0 0 0 0");
        assertEquals(28601, proc.pid());
        assertEquals("java", proc.comm());
        assertEquals('S', proc.state());
        assertEquals(BigInteger.valueOf(21887), proc.utime());
        assertEquals(BigInteger.valueOf(2104), proc.stime());
        assertEquals(BigInteger.valueOf(1177), proc.cutime());
        assertEquals(BigInteger.valueOf(195), proc.cstime());
        assertEquals(28470, proc.ppid());
        assertEquals(20, proc.priority());
        assertEquals(0, proc.nice());
        assertEquals(30, proc.num_threads());
        assertEquals(BigInteger.valueOf(1568161), proc.starttime());
        assertEquals(BigInteger.ZERO, proc.delayacct_blkio_ticks());
        assertEquals(BigInteger.ZERO, proc.guest_time());
        assertEquals(BigInteger.ZERO, proc.cguest_time());
    }

    @Test
    public void testParseProcessStatComm() {
        ProcessStat proc = LinuxProcfsSupport.parseProcessStat("28601 ( )( )( ) S 28470 2698 2698 0 -1 4202496 1263184 338414 45 57 21887 2104 1177 195 20 0 30 0 1568161 1366188032 165844 18446744073709551615 1073741824 1073778376 140734855863392 18446744073709551615 139712515037029 0 0 536870914 16801021 18446744073709551615 0 0 17 0 0 0 0 0 0");
        assertEquals(28601, proc.pid());
        assertEquals(" )( )( ", proc.comm());
        assertEquals('S', proc.state());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseProcessStatIncomplete() {
        LinuxProcfsSupport.parseProcessStat("28601 (java) S 28470 2698 2698 0 -1 4202496 1263184 338414 45 57 21887 2104 1177 195 20 0 30 0 1568161 1366188032 165844 18446744073709551615 1073741824 1073778376 140734855863392 18446744073709551615 139712515037029 0 0 536870914 16801021 18446744073709551615 0");
    }

    @Test(expected = NullPointerException.class)
    public void testParseProcessStatNull() {
        LinuxProcfsSupport.parseProcessStat(null);
    }
}
