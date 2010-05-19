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
package org.netbeans.modules.javacard.common;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Utilities;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class UtilsTest {
    @Test
    public void testShortCommandLineWithSpaces() {
        String cmdline = "c:\\Program Files\\Java\\cjcre.exe -resume -e2pfile c:\\Documents And Settings\\Joe Blow\\My Documents\\Java Card\\foo.eeprom";
        String[] expect = new String[]{
            "c:\\Program Files\\Java\\cjcre.exe",
            "-resume",
            "-e2pfile",
            "c:\\Documents And Settings\\Joe Blow\\My Documents\\Java Card\\foo.eeprom"
        };
        String[] got = Utils.shellSplit(cmdline);
        assertFalse("".equals(got[0]));
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    private void assertLengthsSame (String[] expected, String[] got) {
        if (expected.length > got.length) {
            fail ("Expected " + expected.length + " items, got " + got.length + " - extras: " + Arrays.asList(expected).subList(got.length, expected.length));
        } else if (got.length > expected.length) {
            fail ("Expected " + expected.length + " items, got " + got.length + " - extras: " + Arrays.asList(got).subList(expected.length, got.length));
        }
    }

    @Test
    public void testSplitIntoAtomicArgs() {
        String cmdline = "c:\\netbeans\\bin\\foo.exe {{{bar}}} baz baf {{{boom}}} whatzit";
        String[] expect = new String[]{
            "c:\\netbeans\\bin\\foo.exe ",
            "bar",
            " baz baf ",
            "boom",
            " whatzit"
        };
        String[] got = new AtomicElementRepairer(Arrays.asList(cmdline)).restoreAtomicItems().toArray(new String[0]);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertEquals (expect.length, got.length);

        cmdline = "c:\\netbeans\\bin\\foo.exe {{{bar}}} baz baf {{{boom}}} {{{fwee}}}";
        expect = new String[]{
                    "c:\\netbeans\\bin\\foo.exe ",
                    "bar",
                    " baz baf ",
                    "boom",
                    "fwee"
                };
        got = new AtomicElementRepairer(Arrays.asList(cmdline)).restoreAtomicItems().toArray(new String[0]);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertEquals (expect.length, got.length);
    }

    @Test
    public void testAtomicRestoreComplex() {
        List<String> l = new ArrayList<String>(Arrays.asList(new String[]{
                    "foo",
                    "foo{{{bar}}}trailer",
                    "baz{{{split",
                    "lines}}}",
                    "two{{{first}}}three{{{second}}}four{{{third}}}five{{{fourth}}}",
                    "six{{{sixth}}}seven{{{seventh ",
                    "which goes on for a while}}}trailer"
                }));
        String[] expect = new String[]{
            "foo",
            "foo",
            "bar",
            "trailer",
            "baz",
            "splitlines",
            "two"
            , "first"
            , "three"
            , "second"
            , "four"
            , "third"
            , "five"
            , "fourth",
            "six"
            , "sixth"
            , "seven"
            , "seventh which goes on for a while"
            , "trailer"
        };
        List<String> g = new AtomicElementRepairer(l).restoreAtomicItems();
        String[] got = g.toArray(new String[0]);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testSplitPointsAreSaneRestore() {
        assertTrue(true);
        AtomicElementRepairer a = new AtomicElementRepairer(null);
        AtomicElementRepairer.OneStringHandler h = a.createHandler("foo{{{bar", false);
        assertFalse(h.isClose());
        assertTrue(h.isOpen());
        assertTrue(h.involvesQuoting());
        assertTrue(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertTrue(h.hasContents());
        assertEquals("foo", h.getLeader());
        assertEquals("bar", h.getContents());

        h = a.createHandler("foo{{{bar", true);
        assertFalse(h.involvesQuoting());
        assertFalse(h.isOpen());
        assertFalse(h.isClose());
        assertFalse(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertTrue(h.hasContents());
        assertEquals("foo{{{bar", h.getContents());

        h = a.createHandler("foo{{{bar}}}baz", false);
        assertTrue(h.isClose());
        assertTrue(h.isOpen());
        assertTrue(h.involvesQuoting());
        assertTrue(h.hasLeader());
        assertTrue(h.hasTrailer());
        assertTrue(h.hasContents());
        assertEquals("foo", h.getLeader());
        assertEquals("bar", h.getContents());
        assertEquals("baz", h.getTrailer());

        h = a.createHandler("foo{{{bar}}}baz", true);
        assertTrue(h.isClose());
        assertFalse(h.isOpen());
        assertTrue(h.involvesQuoting());
        assertFalse(h.hasLeader());
        assertTrue(h.hasTrailer());
        assertTrue(h.hasContents());
        assertEquals("foo{{{bar", h.getContents());
        assertEquals("baz", h.getTrailer());

        h = a.createHandler("foo{{{bar}}}baz{{{boom}}}bam", false);
        assertTrue(h.isClose());
        assertTrue(h.isOpen());
        assertTrue(h.involvesQuoting());
        assertTrue(h.hasLeader());
        assertTrue(h.hasTrailer());
        assertTrue(h.hasContents());
        assertEquals("foo", h.getLeader());
        assertEquals("bar", h.getContents());
        assertEquals("baz{{{boom}}}bam", h.getTrailer());

        h = a.createHandler("nothing going on", false);
        assertFalse(h.isClose());
        assertFalse(h.isOpen());
        assertFalse(h.involvesQuoting());
        assertTrue(h.hasContents());
        assertFalse(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertEquals("nothing going on", h.getContents());

        h = a.createHandler("nothing going on", true);
        assertFalse(h.isClose());
        assertFalse(h.isOpen());
        assertFalse(h.involvesQuoting());
        assertTrue(h.hasContents());
        assertFalse(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertEquals("nothing going on", h.getContents());

        h = a.createHandler("nothing going on{{{", true);
        assertFalse(h.isClose());
        assertFalse(h.isOpen());
        assertFalse(h.involvesQuoting());
        assertTrue(h.hasContents());
        assertFalse(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertNull(h.getLeader());
        assertEquals("nothing going on{{{", h.getContents());

        h = a.createHandler("nothing going on{{{", false);
        assertFalse(h.isClose());
        assertTrue(h.isOpen());
        assertTrue(h.involvesQuoting());
        assertFalse(h.hasContents());
        assertTrue(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertEquals("nothing going on", h.getLeader());
        assertNull(h.getContents());

        h = a.createHandler("{{{", false);
        assertFalse(h.isClose());
        assertTrue(h.isOpen());
        assertTrue(h.involvesQuoting());
        assertFalse(h.hasContents());
        assertFalse(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertNull(h.getLeader());
        assertNull(h.getContents());

        h = a.createHandler("}}}", false);
        assertFalse(h.isClose());
        assertFalse(h.isOpen());
        assertFalse(h.involvesQuoting());
        assertTrue(h.hasContents());
        assertFalse(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertNull(h.getLeader());
        assertEquals("}}}", h.getContents());

        h = a.createHandler("}}}", true);
        assertTrue(h.isClose());
        assertFalse(h.isOpen());
        assertTrue(h.involvesQuoting());
        assertFalse(h.hasContents());
        assertFalse(h.hasLeader());
        assertFalse(h.hasTrailer());
        assertNull(h.getLeader());
        assertNull(h.getContents());
        assertNull(h.getTrailer());
    }

    @Test
    public void testAtomicRestore() {
        String s = "foo{{{bar}}}baz{{{boom}}}bam";
        List<String> nue = new AtomicElementRepairer(Collections.singletonList(s)).restoreAtomicItems();
        assertFalse(nue.isEmpty());
        assertEquals(5, nue.size());
        assertEquals(Arrays.asList(new String[]{"foo", "bar", "baz", "boom", "bam"}), nue);
    }

    @Test
    public void testAtomicArguments() {
        String cmdline = "foo/bar --one two{{{three four five}}}six -seven eight nine -ten eleven{{{twelve}}}thirteen";
        String[] expect = new String[]{
            "foo/bar",
            "--one",
            "two",
            "three four five",
            "six",
            "-seven",
            "eight nine",
            "-ten",
            "eleven",
            "twelve",
            "thirteen"
        };
        String[] got = Utils.shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testBasicCommandLine() {
        if (!Utilities.isWindows()) { //Pending - create a unix example
                                      //with appropriate file separator
            return;
        }
        String cmdline = "cmd /c " + "c:\\foo\\cjcre.exe "
                + "-ramsize 2K "
                + "-e2psize 2K "
                + "-corsize 1K "
                + "-e2pfile c:\\bar\\foo.eeprom "
                + "-debug false "
                + "-loggerlevel SEVERE "
                + "-httpport 8080 "
                + "-contactedport 2650 "
                + "-contactedprotocol T1 "
                + "-contactlessport 2651 "
                + "-bogus C:\\Progam Files\\foo\\bar.foo "
                + "-debuggerport 2652 "
                + "-suspend false ";

        String[] expect = new String[]{
            "cmd",
            "/c",
            "c:\\foo\\cjcre.exe",
            "-ramsize",
            "2K",
            "-e2psize",
            "2K",
            "-corsize",
            "1K",
            "-e2pfile",
            "c:\\bar\\foo.eeprom",
            "-debug",
            "false",
            "-loggerlevel",
            "SEVERE",
            "-httpport",
            "8080",
            "-contactedport",
            "2650",
            "-contactedprotocol",
            "T1",
            "-contactlessport",
            "2651",
            "-bogus",
            "C:\\Progam Files\\foo\\bar.foo",
            "-debuggerport",
            "2652",
            "-suspend",
            "false",};

        String[] got = shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testMultiDashCommandLine() {
        if (!Utilities.isWindows()) { //FIXME
            return;
        }
        String cmdline = "cmd /c C:\\Java Card\\JCDK3.0.2\\bin\\debugproxy.bat " + //NOI18N
                "--listen 2026 " + //NOI18N
                "--remote localhost:3225 " + //NOI18N
                "--classpath C:\\Some Where\\a.jar;C:\\Some Where Else\\b.jar"; //NOI18N
        String[] expect = new String[]{
            "cmd",
            "/c",
            "C:\\Java Card\\JCDK3.0.2\\bin\\debugproxy.bat",
            "--listen",
            "2026",
            "--remote",
            "localhost:3225",
            "--classpath",
            "C:\\Some Where\\a.jar;C:\\Some Where Else\\b.jar"
        };
        String[] got = shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testDashesInPaths() {
        if (!Utilities.isWindows()) { //FIXME
            return;
        }
        String cmdline = "cmd /c c:\\Program Files\\Java\\Java Card\\cjcre.exe "
                + "-ramsize 2K "
                + "-e2psize 2K "
                + "-corsize 1K "
                + "-e2pfile C:\\space\\nbsrc\\nbbuild\\testuserdir\\config\\org-netbeans-modules-javacard\\eeproms\\javacard_default\\Default Device.eprom "
                + "-debug "
                + "-loggerlevel SEVERE "
                + "-httpport 8080 "
                + "-contactedport 2650 "
                + "-contactedprotocol T1 "
                + "-contactlessport 2651 "
                + "-debuggerport 2652 "
                + "-nosuspend";

        String[] expect = new String[]{
            "cmd",
            "/c",
            "c:\\Program Files\\Java\\Java Card\\cjcre.exe",
            "-ramsize",
            "2K",
            "-e2psize",
            "2K",
            "-corsize",
            "1K",
            "-e2pfile",
            "C:\\space\\nbsrc\\nbbuild\\testuserdir\\config\\org-netbeans-modules-javacard\\eeproms\\javacard_default\\Default Device.eprom",
            "-debug",
            "-loggerlevel",
            "SEVERE",
            "-httpport",
            "8080",
            "-contactedport",
            "2650",
            "-contactedprotocol",
            "T1",
            "-contactlessport",
            "2651",
            "-debuggerport",
            "2652",
            "-nosuspend"
        };

        String[] got = shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testArgValueSplit() {
        String val = "-e2pfile C:\\space\\nbsrc\\nbbuild\\testuserdir\\config\\org-netbeans-modules-javacard\\eeproms\\javacard_default\\Default Device.eprom";
        List<String> s = new ArrayList<String>();
        assertTrue(Utils.splitArgs(val, s));
        assertEquals(2, s.size());
    }

    @Test
    public void testCantSplitFileName() {
        String val = "C:\\space\\nbsrc\\nbbuild\\testuserdir\\config\\org-netbeans-modules-javacard\\eeproms\\javacard_default\\Default Device.eprom";
        List<String> s = new ArrayList<String>();
        if (!Utils.splitArgs(val, s)) {
            s.add(val);
        }
        assertEquals(1, s.size());
    }

    @Test
    public void testMiniSplit() {
        String val = "-ramsize 2K -e2psize 2K -corsize 1K";
        List<String> s = new ArrayList<String>();
        assertTrue(Utils.splitArgs(val, s));
        assertEquals("Should have gotten 6 parts: " + s, 6, s.size());
        assertEquals("-ramsize", s.get(0));
        assertEquals("2K", s.get(1));
        assertEquals("-e2psize", s.get(2));
        assertEquals("2K", s.get(3));
        assertEquals("-corsize", s.get(4));
        assertEquals("1K", s.get(5));
    }

    @Test
    public void testShortCommandLineNoSpaces() {
        String cmdline = "cjcre.exe -resume -e2pfile c:\\foo.eprom";
        String[] expect = new String[]{
            "cjcre.exe",
            "-resume",
            "-e2pfile",
            "c:\\foo.eprom"
        };
        String[] got = shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testOneElementCommandLine() {
        String cmdline = "cjcre.exe";
        String[] expect = new String[]{"cjcre.exe"};
        String[] got = shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testTwoElementCommandLine() {
        String cmdline = "cjcre.exe -resume";
        String[] expect = new String[]{"cjcre.exe", "-resume"};
        String[] got = shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testSplitRegexp() {
        String val = "-e2pfile C:\\space\\nbsrc\\nbbuild\\testuserdir\\config\\org-netbeans-modules-javacard\\eeproms\\javacard_default\\Default Device.eprom";
        List<String> s = new ArrayList<String>();
        Matcher m = pattern().matcher(val);
        while (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                s.add(m.group(i));
            }
        }
        assertEquals("-e2pfile", s.get(0));
        assertEquals("C:\\space\\nbsrc\\nbbuild\\testuserdir\\config\\org-netbeans-modules-javacard\\eeproms\\javacard_default\\Default Device.eprom", s.get(1));
        assertEquals(2, s.size());
    }

    static Pattern pattern() {
        return Utils.ARG_VALUE_SPLIT;
    }
    //for dev time:
    static final Pattern ARG_VALUE_SPLIT = Pattern.compile(
            "^\\s*(\\-\\S*)\\s*?((?!\\-)\\S.*?)?(?:\\s\\-|$)"); //NOI18N

    @Test
    public void testMiniSplitRegexp() {
        String val = "-ramsize 2K -e2psize 2K -corsize 1K";
        List<String> s = new ArrayList<String>();
        Matcher m = pattern().matcher(val);
        while (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                s.add(m.group(i));
            }
        }
        assertEquals("-ramsize", s.get(0));
        assertEquals("2K", s.get(1));
        assertEquals(2, s.size());
    }

    @Test
    public void testSplitSoloSwitches() {
        String val = "-resume -e2pfile c:\\Documents And Settings\\Joe Blow\\My Documents\\Java Card\\foo.eeprom";
        List<String> s = new ArrayList<String>();
        Utils.splitArgs(val, s);
        assertEquals("Not split in 3: " + s, 3, s.size());
    }

    @Test
    public void testSoloSwitches() {
        String val = "-resume -e2pfile c:\\Documents And Settings\\Joe Blow\\My Documents\\Java Card\\foo.eeprom";
        List<String> s = new ArrayList<String>();
        Matcher m = pattern().matcher(val);
        while (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                if (m.group(i) != null) {
                    s.add(m.group(i));
                }
            }
        }
        assertEquals("-resume", s.get(0));
        assertEquals(1, s.size());
    }

    private static final String[] shellSplit(String s) {
        return Utils.shellSplit(s);
    }

    @Test
    public void testSpacesInPaths() {
        if (!Utilities.isWindows()) { //FIXME
            return;
        }
        String cmdline = "cmd /c c:\\Program Files\\Java\\Java Card\\cjcre.exe "
                + "-ramsize 2K "
                + "-e2psize 2K "
                + "-corsize 1K "
                + "-e2pfile c:\\Documents And Settings\\Joe Blow\\My Documents\\Java Card\\foo.eeprom "
                + "-debug "
                + "-loggerlevel SEVERE "
                + "-httpport 8080 "
                + "-contactedport 2650 "
                + "-contactedprotocol T1 "
                + "-contactlessport 2651 "
                + "-debuggerport 2652 "
                + "-nosuspend";

        String[] expect = new String[]{
            "cmd",
            "/c",
            "c:\\Program Files\\Java\\Java Card\\cjcre.exe",
            "-ramsize",
            "2K",
            "-e2psize",
            "2K",
            "-corsize",
            "1K",
            "-e2pfile",
            "c:\\Documents And Settings\\Joe Blow\\My Documents\\Java Card\\foo.eeprom",
            "-debug",
            "-loggerlevel",
            "SEVERE",
            "-httpport",
            "8080",
            "-contactedport",
            "2650",
            "-contactedprotocol",
            "T1",
            "-contactlessport",
            "2651",
            "-debuggerport",
            "2652",
            "-nosuspend"
        };

        String[] got = shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testLeadingSpaces() {
        if (!Utilities.isWindows()) { //FIXME
            return;
        }
        String cmdline = "  cmd /c c:\\Program Files\\Java\\Java Card\\cjcre.exe "
                + "-ramsize 2K "
                + "-e2psize 2K "
                + "-corsize 1K "
                + "-e2pfile c:\\Documents And Settings\\Joe Blow\\My Documents\\Java Card\\foo.eeprom "
                + "-debug "
                + "-loggerlevel SEVERE "
                + "-httpport 8080 "
                + "-contactedport 2650 "
                + "-contactedprotocol T1 "
                + "-contactlessport 2651 "
                + "-debuggerport 2652 "
                + "-nosuspend";

        String[] expect = new String[]{
            "cmd",
            "/c",
            "c:\\Program Files\\Java\\Java Card\\cjcre.exe",
            "-ramsize",
            "2K",
            "-e2psize",
            "2K",
            "-corsize",
            "1K",
            "-e2pfile",
            "c:\\Documents And Settings\\Joe Blow\\My Documents\\Java Card\\foo.eeprom",
            "-debug",
            "-loggerlevel",
            "SEVERE",
            "-httpport",
            "8080",
            "-contactedport",
            "2650",
            "-contactedprotocol",
            "T1",
            "-contactlessport",
            "2651",
            "-debuggerport",
            "2652",
            "-nosuspend"
        };

        String[] got = shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertLengthsSame(expect, got);
    }

    @Test
    public void testSwitchesWithEqualsFollowedBySpacesInPath() {
        if (!Utilities.isWindows()) { //Pending - create a unix example
                                      //with appropriate file separator
            return;
        }
        String cmdline = "C:\\Program Files\\Java\\jdk1.6.0_14\\jre/bin/java"
                + " -classpath H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\api_connected.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\api.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\romizer.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\tools.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\asm-all-3.1.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\bcel-5.2.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\commons-logging-1.1.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\commons-httpclient-3.0.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\commons-codec-1.3.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\commons-cli-1.0.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\ant-contrib-1.0b3.jar "
                + "{{{-Djc.home=H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition}}} "
                + "com.sun.javacard.debugproxy.Main "
                + "{{{debug}}} "
                + "--listen "
                + "7020 "
                + "--remote "
                + "localhost:7019 "
                + "--classpath "
                + "C:\\Documents and Settings\\Administrator\\My Documents\\NetBeansProjects\\ClassicApplet46\\dist\\ClassicApplet46.cap";
        String[] expect = new String[]{
            "C:\\Program Files\\Java\\jdk1.6.0_14\\jre/bin/java",
            "-classpath",
            "H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\api_connected.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\api.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\romizer.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\tools.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\asm-all-3.1.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\bcel-5.2.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\commons-logging-1.1.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\commons-httpclient-3.0.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\commons-codec-1.3.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\commons-cli-1.0.jar;H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition\\lib\\ant-contrib-1.0b3.jar",
            "-Djc.home=H:\\NetBeans 6.8\\javacard1\\JCDK3.0.2_ConnectedEdition",
            "com.sun.javacard.debugproxy.Main",
            "debug",
            "--listen",
            "7020",
            "--remote",
            "localhost:7019",
            "--classpath",
            "C:\\Documents and Settings\\Administrator\\My Documents\\NetBeansProjects\\ClassicApplet46\\dist\\ClassicApplet46.cap"
        };
        String[] got = Utils.shellSplit(cmdline);
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
    }
}
