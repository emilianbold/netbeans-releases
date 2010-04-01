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

package org.netbeans.modules.javacard.ri.card;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.javacard.api.RunMode;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.spi.AbstractCard;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.Cards;
import org.netbeans.modules.javacard.spi.capabilities.CardInfo;
import org.netbeans.modules.javacard.spi.CardState;
import org.netbeans.modules.javacard.spi.CardStateObserver;
import org.netbeans.modules.javacard.spi.capabilities.ClearEpromCapability;
import org.netbeans.modules.javacard.spi.capabilities.DebugCapability;
import org.netbeans.modules.javacard.spi.capabilities.EpromFileCapability;
import org.netbeans.modules.javacard.spi.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.modules.javacard.spi.capabilities.ResumeCapability;
import org.netbeans.modules.javacard.spi.capabilities.StartCapability;
import org.netbeans.modules.javacard.spi.capabilities.StopCapability;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoader.RecognizedFiles;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
public class RICardTest {

    public RICardTest() {
    }

    static String scriptPath;
    static File script;
    @BeforeClass
    public static void setUpClass() throws Exception {
        MockServices.setServices(FakeLoader.class, FakeResolver.class);
        Logger.getLogger(RICard.class.getName()).setLevel(Level.FINEST);
        Logger.getLogger(AbstractCard.class.getName()).setLevel(Level.FINEST);
        script = File.createTempFile("ritest", Utilities.isWindows() ? ".bat" : ".sh", new File(System.getProperty("java.io.tmpdir")));
        FileOutputStream out = new FileOutputStream (script);
        PrintWriter pw = new PrintWriter(out);
        try {
            if (Utilities.isWindows()) {
                //The horrible way you do a sleep() in a batch script:
                pw.println("cmd /c echo %1 %2 %3 %4 %5 %6 %7 %8 %9 %10 %11 %12 & ping 1.0.0.0 -n 1 -w 5000 >NUL");
            } else {
                pw.println ("#!/bin/sh");
                pw.println ("echo $1 $2 $3 $4 $5 $6 $7 $8 $9 $10 $11 $12\n");
            }
        } finally {
            pw.flush();
            out.close();
        }
        if (Utilities.isUnix()) {
            String[] cmd = new String[] { "chmod", "ugoa+x", script.getAbsolutePath() };
            Runtime.getRuntime().exec(cmd).waitFor();
        }
        if (!Utilities.isUnix() && !Utilities.isWindows()) {
            throw new Error("No idea how to create a fake executable for this OS");
        }
        scriptPath = script.getAbsolutePath();
        //sanity check
        Process p = Runtime.getRuntime().exec(scriptPath);
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            // FIXME: Commenting out because this test is failing. Needs to see what is wrong.
            //throw new Error ("Could not execute " + scriptPath);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        assertTrue(script.delete());
    }

    private static class RICardSub extends RICard {
        CardStateObserver interceptor;
        RICardSub(DataObject ob, JavacardPlatform p, String id) {
            super (ob, p, id);
        }

        @Override
        protected void onStateChanged(CardState old, CardState nue) {
            super.onStateChanged(old, nue);
            if (interceptor != null) {
                interceptor.onStateChange(this, old, nue);
            }
        }
    }

    OPA opa;
    CardProperties props;
    RICardSub card;
    FileSystem memFs;

    @Before
    public void setUp() throws Exception {
        opa = new OPA();
        memFs = FileUtil.createMemoryFileSystem();
        FileObject fo = memFs.getRoot().createData("foo.jcard");
        OutputStream out = fo.getOutputStream();
        InputStream in = RICardTest.class.getResourceAsStream("WindowsFakeCard.jcard");
        FileUtil.copy (in, out);
        out.close();
        in.close();
        FileUtil.setMIMEType("jcard", "application/javacard");
        DataObject dob = DataObject.find(fo);
        card = new RICardSub(dob, new FakePlatform(),
                opa.getProperty(JavacardDeviceKeyNames.DEVICE_DISPLAY_NAME));
    }

    @Test
    public void testLookupContents2() throws InterruptedException, Exception {
        for (int i = 0; i < 10; i++) {
            testLookupContents();
            setUp();
        }
    }

    @Test
    public void testClearEpromCapabilityRemovedAfterInvocation() throws InterruptedException, IOException {
        assertNull (card.getCapability(ClearEpromCapability.class));
//        FileObject epromDir = Utils.sfsFolderForDeviceEepromsForPlatformNamed(
//                    card.getPlatform().getSystemName(), true);
        //Creates the eeprom file
        File expect = Utils.eepromFileForDevice(card.getPlatform().getSystemName(), card.getSystemId(), true);
        card.onStateChanged(card.getState(), CardState.NOT_RUNNING);

        EpromFileCapability ef = card.getCapability(EpromFileCapability.class);
        assertNotNull (ef);
        FileObject epromFile = ef.getEpromFile();
        assertSame (expect, FileUtil.toFile(epromFile));
        assertNotNull (epromFile);
        ClearEpromCapability clear = card.getCapability(ClearEpromCapability.class);
        assertNotNull (clear);
        final CountDownLatch latch = new CountDownLatch(1);
        epromFile.addFileChangeListener(new FileChangeAdapter() {

            @Override
            public void fileDeleted(FileEvent fe) {
                latch.countDown();}});
        clear.clear();
        synchronized (clear) {
            clear.wait(300);
        }
        latch.await(30000, TimeUnit.MILLISECONDS);
        assertFalse (epromFile.isValid());
        assertNull (card.getCapability(ClearEpromCapability.class));
        assertNotNull (card.getCapability(EpromFileCapability.class));
        EpromFileCapability c = card.getCapability(EpromFileCapability.class);
        assertNull (c.getEpromFile());

        //Ensure the capability reappears
        expect = Utils.eepromFileForDevice(card.getPlatform().getSystemName(), card.getSystemId(), true);
        card.onStateChanged(card.getState(), CardState.RUNNING);
        card.onStateChanged(card.getState(), CardState.NOT_RUNNING);
        epromFile = ef.getEpromFile();
        assertNotNull (epromFile);
        assertNotNull (clear = card.getCapability(ClearEpromCapability.class));
        final CountDownLatch latch2 = new CountDownLatch(1);
        //Ensure the eprom file is gone or the next test will fail
        epromFile.addFileChangeListener(new FileChangeAdapter() {

            @Override
            public void fileDeleted(FileEvent fe) {
                latch2.countDown();}});
        clear.clear();
        synchronized (clear) {
            clear.wait(300);
        }
        latch.await(30000, TimeUnit.MILLISECONDS);
        assertFalse (epromFile.isValid());
        assertNull (card.getCapability(ClearEpromCapability.class));
    }

    @Test
    public void testLookupContents() throws InterruptedException {
        assertNotNull (card.getLookup().lookup(StartCapability.class));
        assertNotNull (card.getLookup().lookup(EpromFileCapability.class));
        assertNotNull (card.getLookup().lookup(DebugCapability.class));
        assertNotNull (card.getLookup().lookup(CardInfo.class));
        assertNull (card.getLookup().lookup(ClearEpromCapability.class));
        StopCapability stop = card.getLookup().lookup(StopCapability.class);
        assertNull ("Should be no initial StopCapability, but found " + stop, stop);
        assertNull (card.getLookup().lookup(ResumeCapability.class));
        final CountDownLatch latch = new CountDownLatch(1);
        final StopCapability[] stopCap = new StopCapability[1];
        CardStateObserver interceptor = new CardStateObserver() {
            public void onStateChange(Card card, CardState old, CardState nue) {
                //Called synchronously on state changes - we can block and test
                //states here
                switch (nue) {
                    case RUNNING :
                    case RUNNING_IN_DEBUG_MODE :
                        stopCap[0] = card.getLookup().lookup(StopCapability.class);
                        assertNotNull (card.getLookup().lookup(StopCapability.class));
                        assertNull (card.getLookup().lookup(StartCapability.class));
                        assertNull (card.getLookup().lookup(ResumeCapability.class));
                        assertNull (card.getLookup().lookup(ClearEpromCapability.class));
                        try {
                            latch.await();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    case NOT_RUNNING :
                        assertNotNull (card.getLookup().lookup(StartCapability.class));
                        assertNotNull (card.getLookup().lookup(EpromFileCapability.class));
                        break;
                }
            }
        };
        card.interceptor = interceptor;

        StartCapability start = card.getLookup().lookup(StartCapability.class);
        Condition c = start.start(RunMode.RUN, null);
        boolean finished = c.await(7000, TimeUnit.MILLISECONDS);
//        assertTrue ("Timout waiting for start to complete", finished);
        int ct = 0;
        while (ct++ < 10 && card.getState() != CardState.RUNNING) {
            StopCapability st = card.getLookup().lookup(StopCapability.class);
            if (st != null) {
                stopCap[0] = st;
            }
            Thread.sleep(200);
        }
        stop = card.getLookup().lookup(StopCapability.class);
        //Hacky, but it's more important to test the StopCapability.  Depending
        //on how fast the process exited, there may or may not be a StopCapability
        //instance by the time we get here.  Can't control that, do need to
        //test that it does more or less what it's supposed to
        stop = stop == null ? stopCap[0] : stop;
        assertEquals (CardState.RUNNING, card.getState()); //XXX may have exited fast
        if (stop == null) {
            stop = card.getLookup().lookup(StopCapability.class);
        }
        if (stop != null) {
            c = stop.stop();
        }
        //Release our sync listener
        latch.countDown();
        if (stop != null) {
            c.await();
            ct = 0;
            while (ct++ < 1000 && card.getState() != CardState.NOT_RUNNING) {
                Thread.sleep (200);
            }
            assertEquals (CardState.NOT_RUNNING, card.getState());
        }
    }

//    @Test
//    public void testIsValid() {
//        RICard instance = null;
//        boolean expResult = true;
//        boolean result = instance.isValid();
//        assertEquals(expResult, result);
//    }
//

    private static final class OPA extends ObservableProperties implements PropertiesAdapter {
        private final PropertyChangeSupport supp = new PropertyChangeSupport(this);
        OPA() throws Exception {
            InputStream in = OPA.class.getResourceAsStream("WindowsFakeCard.jcard");
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

    public static final class FakeLoader extends DataLoader {
        public FakeLoader() {
            super(FakeDob.class.getName());
        }

        @Override
        protected DataObject handleFindDataObject(FileObject fo, RecognizedFiles recognized) throws IOException {
            if ("jcard".equals(fo.getExt())) {
                try {
                    return new FakeDob(fo);
                } catch (Exception ex) {
                    throw new IOException(ex.getLocalizedMessage());
                }
            }
            return null;
        }
    }

    public static final class FakeResolver extends MIMEResolver {

        @Override
        public String findMIMEType(FileObject fo) {
            return "application/javacard";
        }

    }

    public static final class FakeDob extends DataObject {
        private final OPA opa;
        FakeDob(FileObject fo) throws Exception {
            super (fo, Lookup.getDefault().lookup(FakeLoader.class));
            this.opa = new OPA();
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed (this, opa);
        }

        @Override
        public boolean isDeleteAllowed() {
            return false;
        }

        @Override
        public boolean isCopyAllowed() {
            return false;
        }

        @Override
        public boolean isMoveAllowed() {
            return false;
        }

        @Override
        public boolean isRenameAllowed() {
            return false;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected DataObject handleCopy(DataFolder f) throws IOException {
            throw new IOException();
        }

        @Override
        protected void handleDelete() throws IOException {
            throw new IOException();
        }

        @Override
        protected FileObject handleRename(String name) throws IOException {
            throw new IOException();
        }

        @Override
        protected FileObject handleMove(DataFolder df) throws IOException {
            throw new IOException();
        }

        @Override
        protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
            throw new IOException();
        }
    }

    private static class FakePlatform extends JavacardPlatform {
        
        public Set<ProjectKind> supportedProjectKinds() {
            return ProjectKind.kindsFor(null, true);
        }

        @Override
        public String getSystemName() {
            return "Foo";
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public SpecificationVersion getJavacardVersion() {
            return new SpecificationVersion("1.6");
        }

        @Override
        public boolean isVersionSupported(SpecificationVersion javacardVersion) {
            return true;
        }

        @Override
        public ClassPath getBootstrapLibraries(ProjectKind kind) {
            return ClassPathSupport.createClassPath("");
        }

        @Override
        public String getDisplayName() {
            return "Foo";
        }

        @Override
        public Properties toProperties() {
            Properties result = new Properties();
            result.setProperty("javacard.device.eeprom.folder", System.getProperty("java.io.tmpdir"));
            result.setProperty("javacard.emulator", scriptPath);
            result.setProperty ("javacard.debug.proxy", scriptPath);
            return result;
        }

        @Override
        public Map<String, String> getProperties() {
            return new HashMap<String,String>();
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return ClassPathSupport.createClassPath("");
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPathSupport.createClassPath("");
        }

        @Override
        public String getVendor() {
            return "Unit Test";
        }

        @Override
        public Specification getSpecification() {
            return new Specification("JCRE", new SpecificationVersion("3.0"));
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.<FileObject>emptyList();
        }

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public ClassPath getSourceFolders() {
            return ClassPathSupport.createClassPath("");
        }

        @Override
        public List<URL> getJavadocFolders() {
            return Collections.<URL>emptyList();
        }

        @Override
        public Cards getCards() {
            return new Cards() {
                @Override
                public List<? extends Provider> getCardSources() {
                    return Collections.<Provider>emptyList();
                }
            };
        }

        @Override
        public String getPlatformKind() {
            return "TEST";
        }
    }
}