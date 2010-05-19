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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.ri.card;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.ICardCapability;
import org.netbeans.modules.javacard.spi.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.spi.capabilities.AntTargetInterceptor;
import org.netbeans.modules.javacard.spi.capabilities.UrlCapability;
import org.netbeans.modules.javacard.spi.capabilities.CardContentsProvider;
import org.netbeans.modules.javacard.spi.capabilities.CardCustomizerProvider;
import org.netbeans.modules.javacard.spi.capabilities.ClearEpromCapability;
import org.netbeans.modules.javacard.spi.capabilities.DebugCapability;
import org.netbeans.modules.javacard.spi.capabilities.DeleteCapability;
import org.netbeans.modules.javacard.spi.capabilities.EpromFileCapability;
import org.netbeans.modules.javacard.spi.capabilities.PortProvider;
import org.netbeans.modules.javacard.spi.capabilities.ResumeCapability;
import org.netbeans.modules.javacard.spi.capabilities.StartCapability;
import org.netbeans.modules.javacard.spi.capabilities.StopCapability;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.openide.util.Utilities;

/**
 *
 * @author Tim
 */
public class CardPropertiesTest {

    OPA opa;
    CardProperties p;
    Properties platformProps;
    static boolean win = Utilities.isWindows();
    @Before
    public void setUp() throws Exception {
        opa = new OPA();
        p = new CardProperties(opa);
        platformProps = new Properties();
        platformProps.setProperty("javacard.debug.proxy",
                win ? "C:\\Java Card\\JCDK3.0.2\\bin\\debugproxy.bat" :
                    "usr/local/java/jcdk3.0.2/bin/debugproxy");
        platformProps.setProperty("javacard.emulator",
                win ? "C:\\Java Card\\JCDK3.0.2\\bin\\cjcre.exe" :
                    "usr/local/java/jcdk3.0.2/bin/cjcre");
//        platformProps.setProperty("javacard.device.eprom.path",
//                win ? "C:\\foo\\foo.eprom" :
//                    "/home/user/foo.eprom");
        platformProps.setProperty("javacard.device.eeprom.folder",
                win ? "C:\\foo" : "/home/user/foo");
        platformProps.setProperty("javacard.device.name", "foo");
        platformProps.setProperty("javacard.device.host", "localhost");
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_ID, "TestPlatform");
    }

    @Test
    public void testGetContactedPort() {
        assertEquals ("9027", p.getContactedPort());
        p.setContactedPort("3275");
        assertEquals("3275", p.getContactedPort());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT);
        Exception e = null;
        try {
            p.setContactedPort("NOT AN INTEGER");
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    public void testGetContactedProtocol() {
        assertEquals ("T=0", p.getContactedProtocol());
        p.setContactedProtocol("Foo");
        assertEquals("Foo", p.getContactedProtocol());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_CONTACTEDPROTOCOL);
    }

    @Test
    public void testGetContactlessPort() {
        assertEquals ("3215", p.getContactlessPort());
        p.setContactlessPort("3072");
        assertEquals("3072", p.getContactlessPort());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_CONTACTLESSPORT);
        Exception e = null;
        try {
            p.setContactlessPort("NOT AN INTEGER");
        } catch (Exception ex) {
            e = ex;
        }
    }

    @Test
    public void testGetCorSize() {
        assertEquals ("3K", p.getCorSize());
        p.setCorSize("32K");
        assertEquals ("32K", p.getCorSize());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_CORSIZE);
    }

    @Test
    public void testGetE2pSize() {
        assertEquals ("3M", p.getE2pSize());
        p.setE2pSize("32M");
        assertEquals ("32M", p.getE2pSize());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_E2PSIZE);
    }

    @Test
    public void testGetHttpPort() {
        assertEquals ("9019", p.getHttpPort());
        p.setHttpPort("9219");
        assertEquals ("9219", p.getHttpPort());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_HTTPPORT);
        Exception e = null;
        try {
            p.setHttpPort("NOT AN INTEGER");
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    public void testGetLoggerLevel() {
        assertEquals ("severe", p.getLoggerLevel());
        p.setLoggerLevel("debug");
        assertEquals ("debug", p.getLoggerLevel());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_LOGGERLEVEL);
    }

    @Test
    public void testIsNoSuspend() {
        assertEquals (false, p.isSuspend());
        p.setSuspend(false);
        assertEquals(false, p.isSuspend());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_SUSPEND_THREADS_ON_STARTUP);
    }

    @Test
    public void testGetProxy2cjcrePort() {
        assertEquals("9017", p.getProxy2cjcrePort());
        p.setProxy2cjcrePort("9746");
        assertEquals("9746", p.getProxy2cjcrePort());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_PROXY2CJCREPORT);
        Exception e = null;
        try {
            p.setProxy2cjcrePort("NOT AN INTEGER");
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    public void testGetProxy2idePort() {
        assertEquals("7022", p.getProxy2idePort());
        p.setProxy2idePort("6666");
        assertEquals("6666", p.getProxy2idePort());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_PROXY2IDEPORT);
        Exception e = null;
        try {
            p.setProxy2idePort("NOT AN INTEGER");
        } catch (Exception ex) {
            e = ex;
        }
        assertNotNull(e);
    }

    @Test
    public void testGetRamSize() {
        assertEquals("4M", p.getRamSize());
        p.setRamSize("5M");
        assertEquals("5M", p.getRamSize());
        opa.assertChanged(JavacardDeviceKeyNames.DEVICE_RAMSIZE);
    }

    @Test
    public void testGetPortsInUse() throws Exception {
        OPA o = new OPA();
        CardProperties pp = new CardProperties(o);
        Set<Integer> expect = new HashSet<Integer>();
        expect.addAll (Arrays.asList(new Integer[] {7022,9027,9019,3215,9017 }));
        assertEquals (expect, pp.getPorts());
    }

    @Test
    public void testGetDebugProxyCommandLine() {
        if (!Utilities.isWindows()) { //FIXME
            return;
        }
        String[] got = p.getDebugProxyCommandLine(platformProps, "C:\\foo\\a.jar;c:\\foo\b.jar");
        String[] expect = new String[] {
            "cmd",
            "/c",
            win ? "C:\\Java Card\\JCDK3.0.2\\bin\\debugproxy.bat" : "usr/local/java/jcdk3.0.2/bin/debugproxy",
            "debug",
            "--listen",
            "7022",
            "--remote",
            "localhost:9017",
            "--classpath",
            "C:\\foo\\a.jar;c:\\foo.jar",
        };
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertEquals(expect.length, got.length);
    }

    @Test
    public void testGetRunCommandLine() {
        if (!Utilities.isWindows()) { //FIXME
            return;
        }
        String winEepromFile = "org-netbeans-modules-javacard/eeproms/TestPlatform\\foo.eprom";
        String[] got = p.getRunCommandLine(platformProps, false, false, false);
        String[] expect = new String[] {
            win ? "C:\\Java Card\\JCDK3.0.2\\bin\\cjcre.exe" : "usr/local/java/jcdk3.0.2/bin/cjcre",
            "-debug",
            "false",
            "-suspend",
            "false",
            "-ramsize",
            "4M",
            "-e2psize",
            "3M",
            "-corsize",
            "3K",
            "-e2pfile",
            win ? winEepromFile : "/home/user/foo/foo.eprom",
            "-loggerlevel",
            "severe",
            "-httpport",
            "9019",
            "-contactedport",
            "9027",
            "-contactedprotocol",
            "T=0",
            "-contactlessport",
            "3215",
        };
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertEquals(expect.length, got.length);
    }

    @Test
    public void testGetResumeCommandLine() {
        if (!Utilities.isWindows()) { //FIXME
            return;
        }
        String winEepromFile = "org-netbeans-modules-javacard/eeproms/TestPlatform\\foo.eprom";
        assertNotNull (platformProps.get("javacard.instance.id"));
        String[] got = p.getRunCommandLine(platformProps, false, false, true);
        String[] expect = new String[] {
            win ? "C:\\Java Card\\JCDK3.0.2\\bin\\cjcre.exe" : "usr/local/java/jcdk3.0.2/bin/cjcre",
            "-resume",
            "-debug",
            "false",
            "-suspend",
            "false",
            "-e2pfile",
            win ? winEepromFile : "/home/user/foo/foo.eprom"
        };
        for (int i = 0; i < Math.min(expect.length, got.length); i++) {
            assertEquals("Expected '" + expect[i] + "' at " + i + " got '" + got[i] + "' :" + Arrays.asList(got), expect[i], got[i]);
        }
        assertEquals(expect.length, got.length);
    }

    @Test
    public void testDeclarableCapabilities() {
        Set <Class<? extends ICardCapability>> got = p.getSupportedCapabilityTypes();
        Set <Class<? extends ICardCapability>> expect = new HashSet<Class<? extends ICardCapability>>();
        expect.addAll(Arrays.asList(StartCapability.class, StopCapability.class, ResumeCapability.class,
                EpromFileCapability.class, ClearEpromCapability.class, DebugCapability.class, CardCustomizerProvider.class,
                CardContentsProvider.class, AntTargetInterceptor.class, PortProvider.class, UrlCapability.class, DeleteCapability.class));
        assertSetsEquals (expect, got);
    }

    private void assertSetsEquals (Set<?> expect, Set<?> got) {
        if (!expect.equals(got)) {
            Set expect1 = new HashSet<Object> (expect);
            Set got1 = new HashSet<Object> (got);
            Set expect2 = new HashSet<Object> (expect);
            Set got2 = new HashSet<Object> (got);
            got1.removeAll(expect1);
            expect2.removeAll(got2);
            assertTrue ("Unexpected objects: " + got1, got1.isEmpty());
            assertTrue ("Missing objects: " + expect2, expect2.isEmpty());
        }
    }

    private static final class OPA extends ObservableProperties implements PropertiesAdapter {
        private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
        OPA() throws Exception {
            InputStream in = OPA.class.getResourceAsStream("ServerTemplate.jcard");
            try {
                load(in);
            } finally {
                in.close();
            }
        }

        private String prop;
        public void assertChanged (String property) {
            String old = prop;
            prop = null;
            assertNotNull (old);
            assertEquals (property, old);
        }

        @Override
        public Object setProperty (String key, String val) {
            Object old = super.setProperty(key, val);
            prop = key;
            supp.firePropertyChange(prop, old, val);
            return old;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            supp.addPropertyChangeListener(pcl);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            supp.removePropertyChangeListener(pcl);
        }

        public ObservableProperties asProperties() {
            return this;
        }
    }
}
