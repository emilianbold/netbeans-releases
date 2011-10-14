/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.utils;

import java.util.Vector;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;

/**
 *
 * @author
 * Egor Ushakov
 */
public class PsProviderTest {
    
    public PsProviderTest() {
    }
    
    private PsProvider.PsData prepareWinData() {
        PsProvider provider = new PsProvider.WindowsPsProvider(Host.getLocal());
        PsProvider.PsData data = provider.new PsData();
        data.setHeader(provider.parseHeader("      PID    PPID    PGID     WINPID  TTY  UID    STIME COMMAND"));
        return data;
    }

    @Test
    public void testWinPs1() {
        PsProvider.PsData data = prepareWinData();
        data.addProcess("     5408       0       0       5408    ?    0 11:36:17 C:\\Program Files (x86)\\totalcmd\\TOTALCMD.EXE");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("C:\\Program Files (x86)\\totalcmd\\TOTALCMD.EXE", res.get(0).get(data.commandColumnIdx()));
    }
    
    @Test
    public void testWinPs2() {
        PsProvider.PsData data = prepareWinData();
        data.addProcess("S    4316    6592    4316       5564    1 13352 13:54:32 /cygdrive/d/Projekty/moderngres-bin/bin/initdb");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("/cygdrive/d/Projekty/moderngres-bin/bin/initdb", res.get(0).get(data.commandColumnIdx()));
    }
    
    @Test
    public void testWinPs3() {
        PsProvider.PsData data = prepareWinData();
        data.addProcess("I    6484    6760    6484       6400    0 13352 13:39:04 /usr/bin/bash");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("/usr/bin/bash", res.get(0).get(data.commandColumnIdx()));
    }
    
    private PsProvider.PsData prepareSolarisData() {
        PsProvider provider = new PsProvider.SolarisPsProvider(Host.getLocal());
        PsProvider.PsData data = provider.new PsData();
        data.setHeader(provider.parseHeader("     UID   PID  PPID   C    STIME TTY         TIME CMD"));
        return data;
    }
    
    @Test
    public void testSolarisPs() {
        PsProvider.PsData data = prepareSolarisData();
        data.addProcess("    abcd 18719   994   1   Oct 05 pts/1     273:08 ./firefox");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("./firefox", res.get(0).get(data.commandColumnIdx()));
    }
    
    @Test
    public void testSolarisPsLong() {
        PsProvider.PsData data = prepareSolarisData();
        data.addProcess("longusername 18719   994   1   Oct 05 pts/1     273:08 ./firefox");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("./firefox", res.get(0).get(data.commandColumnIdx()));
    }
    
    private PsProvider.PsData prepareLinuxData() {
        PsProvider provider = new PsProvider.LinuxPsProvider(Host.getLocal());
        PsProvider.PsData data = provider.new PsData();
        data.setHeader(provider.parseHeader("UID        PID  PPID  C STIME TTY          TIME CMD"));
        return data;
    }
    
    @Test
    public void testLinuxPs() {
        PsProvider.PsData data = prepareLinuxData();
        data.addProcess("tester   29270 29241  0 20:15 pts/2    00:00:00 ps -ef");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("ps -ef", res.get(0).get(data.commandColumnIdx()));
    }
    
    @Test
    public void testLinuxPsLong() {
        PsProvider.PsData data = prepareLinuxData();
        data.addProcess("longusername 29270 29241  0 20:15 pts/2    00:00:00 ps -ef");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("ps -ef", res.get(0).get(data.commandColumnIdx()));
    }
    
    private PsProvider.PsData prepareMacData() {
        PsProvider provider = new PsProvider.MacOSPsProvider(Host.getLocal());
        PsProvider.PsData data = provider.new PsData();
        data.setHeader(provider.parseHeader("  UID   PID  PPID   C     STIME TTY           TIME CMD"));
        return data;
    }
    
    @Test
    public void testMacPs() {
        PsProvider.PsData data = prepareMacData();
        data.addProcess("    0   625   615   0   0:00.00 ttys000    0:00.00 ps -ef");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("ps -ef", res.get(0).get(data.commandColumnIdx()));
    }
    
    @Test
    public void testMacPsLong() {
        PsProvider.PsData data = prepareMacData();
        data.addProcess("longusername   625   615   0   0:00.00 ttys000    0:00.00 ps -ef");
        Vector<Vector<String>> res = data.processes(Pattern.compile(".*"));
        assertEquals("ps -ef", res.get(0).get(data.commandColumnIdx()));
    }
}
