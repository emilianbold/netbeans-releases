/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.server.loader;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.Card;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.propdos.PropertiesAdapter;
import org.netbeans.modules.javacard.card.ReferenceImplementation;
import org.netbeans.modules.javacard.card.loader.CardDataObject;
import org.netbeans.modules.javacard.constants.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.platform.DevicePropertiesPanel;
import org.netbeans.modules.javacard.platform.KeysAndValues;
import org.netbeans.modules.javacard.platform.loader.JavacardPlatformDataObject;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Tim Boudreau
 */
public class CardDataObjectTest extends NbTestCase {

    public CardDataObjectTest(String x) {
        super(x);
    }
    private FileObject dataFile;
    private Properties props;
    private FileSystem dfs;
    private FileObject platformFo;
    private Properties platformProps;

    @Before
    @Override
    public void setUp() throws IOException, InterruptedException, InvocationTargetException {
        MockLookup.setLayersAndInstances(CardDataObjectTest.class.getClassLoader(),
                new DFS(dfs = FileUtil.createMemoryFileSystem()), new PDL());

        FileObject platformsfolder = FileUtil.createFolder(dfs.getRoot(),
                CommonSystemFilesystemPaths.SFS_JAVA_PLATFORMS_FOLDER);

        String platformName = FileUtil.findFreeFileName(platformsfolder, "wookie", null);

        platformFo = platformsfolder.createData(platformName, JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION);
        platformProps = new Properties();
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_CLASSPATH, "lib/foo.jar");
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_CLASSIC_BOOT_CLASSPATH, "lib/foo.jar");
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_DEVICE_FILE_EXTENSION, JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_KIND, JavacardPlatformKeyNames.PLATFORM_KIND_RI);
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME, platformName);
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH, "lib/bar.jar");
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_HOME, System.getProperty("java.io.tmpdir"));
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_EMULATOR_PATH, "bin/foo.exe");
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_VENDOR, "Me");
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_MAJORVERSION, "1");
        platformProps.setProperty(JavacardPlatformKeyNames.PLATFORM_MINORVERSION, "6");
        OutputStream o = platformFo.getOutputStream();
        try {
            platformProps.store(o, "xyz");
        } finally {
            o.flush();
            o.close();
        }

        FileObject fld = Utils.sfsFolderForDeviceConfigsForPlatformNamed(platformName, true);
        String deviceName = FileUtil.findFreeFileName(fld, "device", JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
        dataFile = fld.createData(deviceName, JCConstants.JAVACARD_DEVICE_FILE_EXTENSION);
        props = new Properties();
        props.setProperty(JavacardDeviceKeyNames.DEVICE_DISPLAY_NAME, deviceName);
        props.setProperty(JavacardDeviceKeyNames.DEVICE_RAMSIZE, "24K");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_E2PSIZE, "512K");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_CORSIZE, "4K");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_HTTPPORT, "8081");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_PROXY2CJCREPORT, "8082");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_PROXY2IDEPORT, "8083");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT, "8084");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_CONTACTEDPROTOCOL, "T0");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_APDUTOOL_CONTACTEDPROTOCOL, "-t0");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_CONTACTLESSPORT, "8085");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_LOGGERLEVEL, "debug");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_SECUREMODE, "true");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_SERVERURL, "http://localhost:8081");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_CARDMANAGERURL, "http://localhost:8080/cardManager");
        props.setProperty(JavacardDeviceKeyNames.DEVICE_DONT_SUSPEND_THREADS_ON_STARTUP, Boolean.TRUE.toString());
        OutputStream out = dataFile.getOutputStream();
        try {
            props.store(out, "foo");
            System.err.println("Wrote " + props + " to " + dataFile.getPath());
        } finally {
            out.close();
        }
        dfs.refresh(false);
    }

    @Test
    public void testPropertiesAreCorrect() throws DataObjectNotFoundException {
        Node n = DataObject.find(dataFile).getNodeDelegate();
        PropertiesAdapter adap = n.getLookup().lookup(PropertiesAdapter.class);
        assertNotNull(adap);
        ObservableProperties lprops = adap.asProperties();
        assertNotNull(lprops);
        for (Object key : props.keySet()) {
            assertTrue("Key not found: " + key, lprops.containsKey(key));
            assertEquals("Non match on " + key, props.get(key), lprops.get(key));
        }
    }

    public void testPlatformValid() throws Exception {
        DataObject dob = DataObject.find(platformFo);
        assertNotNull(dob);
        assertTrue("Expected a " + JavacardPlatformDataObject.class.getName() +
                " but got a " + dob.getClass().getName(), dob instanceof JavacardPlatformDataObject);

        assertNotNull("No properties adapter found", dob.getLookup().lookup(PropertiesAdapter.class));
        assertNotNull("Properties adapter returns null", dob.getLookup().lookup(PropertiesAdapter.class).asProperties());
        JavacardPlatform p = dob.getLookup().lookup(JavacardPlatform.class);
        assertNotNull(p);
        assertTrue(p + " not valid", p.isValid());
        assertTrue(p.isRI());
        assertEquals("Me", p.getVendor());
    }

    @Test
    public void testIsCorrectType() throws DataObjectNotFoundException {
        assertTrue(DataObject.find(dataFile) instanceof CardDataObject);
    }

    @Test
    public void testCreateNodeDelegate() throws DataObjectNotFoundException {
        Node n = DataObject.find(dataFile).getNodeDelegate();
        Card card = n.getLookup().lookup(Card.class);
        assertNotNull(card);
        assertTrue(card.isValid());
        Reference<Card> cardRef = new WeakReference<Card>(card);
        card = null;
        assertGC("Card instance still referenced", cardRef);
    }

    @Test
    public void testPropertiesChangeTriggersFileWrite() throws DataObjectNotFoundException, InterruptedException {
        class X extends FileChangeAdapter {

            volatile boolean changed;

            @Override
            public void fileChanged(FileEvent fe) {
                changed = true;
                synchronized (this) {
                    notifyAll();
                }
            }

            public void waitFor() throws InterruptedException {
                while (!changed) {
                    synchronized (this) {
                        wait(20000);
                    }
                }
            }
        }
        X x = new X();
        dataFile.addFileChangeListener(x);
        DataObject.find(dataFile).getLookup().lookup(PropertiesAdapter.class).asProperties().setProperty(JavacardDeviceKeyNames.DEVICE_CORSIZE, "28K");
        x.waitFor();
        assertTrue(x.changed);
    }

    @Test
    public void testCardIsReplacedWhenFileModifiedByCustomizer() throws DataObjectNotFoundException, InterruptedException, InvocationTargetException {
        final Node n = DataObject.find(dataFile).getNodeDelegate();
        FileChangeAdapter adap = new FileChangeAdapter() {

            @Override
            public void fileChanged(FileEvent fe) {
                synchronized (this) {
                    notifyAll();
                }
            }
        };
        dataFile.addFileChangeListener(adap);
        Card old = n.getLookup().lookup(Card.class);
        EventQueue.invokeAndWait(new Runnable() {

            public void run() {
                DevicePropertiesPanel p = (DevicePropertiesPanel) n.getCustomizer();
                p.setCorSize("4K");
                p.write(new KeysAndValues.PropertiesAdapter(n.getLookup().lookup(PropertiesAdapter.class).asProperties()));
            }
        });
        synchronized (adap) {
            adap.wait(5000);
        }
        Card nue = n.getLookup().lookup(Card.class);
        assertNotSame(old, nue);
    }

    @Test
    public void testCardIsReplacedWhenFileModified() throws DataObjectNotFoundException, InterruptedException {
        FileChangeAdapter adap = new FileChangeAdapter() {

            @Override
            public void fileChanged(FileEvent fe) {
                synchronized (this) {
                    notifyAll();
                }
            }
        };
        dataFile.addFileChangeListener(adap);

        Node n = DataObject.find(dataFile).getNodeDelegate();
        Card card = n.getLookup().lookup(Card.class);
        assertNotNull(card);
        assertTrue(card.isValid());
        ObservableProperties oprops = n.getLookup().lookup(PropertiesAdapter.class).asProperties();
        P p = new P();
        oprops.addPropertyChangeListener(p);
        oprops.setProperty(JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT, "2000");
        oprops.setProperty(JavacardDeviceKeyNames.DEVICE_CONTACTLESSPORT, "3000");
        oprops.setProperty(JavacardDeviceKeyNames.DEVICE_HTTPPORT, "7070");
        p.waitFor();

        Card nue = n.getLookup().lookup(Card.class);
        assertNotSame(card, nue);
        assertEquals("2000", ((ReferenceImplementation) nue).getContactedPort());

        Reference<Card> old = new WeakReference<Card>(nue);
        nue = null;
        assertGC("Old card not collected", old);
    }

    @Test
    public void testPropertiesAreUpdated() throws DataObjectNotFoundException, InterruptedException, FileNotFoundException, IOException {
        Node n = DataObject.find(dataFile).getNodeDelegate();
        PropertiesAdapter adap = n.getLookup().lookup(PropertiesAdapter.class);
        assertNotNull(adap);
        ObservableProperties lprops = adap.asProperties();
        assertNotNull(lprops);
        P p = new P();
        lprops.addPropertyChangeListener(p);
        p.waitFor();

        lprops.setProperty("foo", "bar");
        p.waitFor();
        p.assertChanged("foo");
        Properties nue = new Properties();
        InputStream in = dataFile.getInputStream();
        try {
            nue.load(in);
        } finally {
            in.close();
        }
        assertEquals("bar", nue.getProperty("foo"));
        lprops.setProperty(JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT, "2905");
        p.waitFor();
        p.assertChanged(JavacardDeviceKeyNames.DEVICE_CONTACTEDPORT);
        p.assertNewValue("2905");
        Card card = n.getLookup().lookup(Card.class);
        assertNotNull(card);
        assertTrue("Expected instance of " + ReferenceImplementation.class.getName() +
                " but got " + card.getClass().getName(),
                card instanceof ReferenceImplementation);
        ReferenceImplementation ri = (ReferenceImplementation) card;
        assertEquals("2905", ri.getContactedPort());
    }

    private static final class P implements PropertyChangeListener {

        private String prop;
        private Object val;

        public void assertChanged(String propName) {
            String old = prop;
            prop = null;
            assertNotNull("Looked for change in " + propName + " but no changes fired", old);
            assertEquals(propName, old);
        }

        public void waitFor() throws InterruptedException {
            int ct = 0;
            synchronized (this) {
                while (prop == null && ct++ < 5) {
                    wait(2000);
                }
            }
        }

        public void assertNewValue(Object val) {
            Object old = this.val;
            this.val = null;
            assertNotNull(old);
            assertEquals(val, old);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            prop = evt.getPropertyName();
            val = evt.getNewValue();
            System.err.println("PROPERTY CHANGE " + prop + " " + evt.getOldValue() + " -> " + evt.getNewValue());
//            Thread.dumpStack();
            synchronized (this) {
                notifyAll();
            }
        }
    }

    private static final class DFS extends Repository {

        DFS(FileSystem fs) {
            super(fs);
        }
    }

    private static final class PDL extends MultiFileLoader {

        PDL() {
            super(JavacardPlatformDataObject.class.getName());
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            return fo;
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject fo) throws DataObjectExistsException, IOException {
            if (JCConstants.JAVACARD_DEVICE_FILE_EXTENSION.equals(fo.getExt())) {
                return new CardDataObject(fo, this);
            } else if (JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION.equals(fo.getExt())) {
                return new JavacardPlatformDataObject(fo, this);
            }
            return null;
        }

        @Override
        protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return obj.getPrimaryEntry();
        }

        @Override
        protected Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return null;
        }
    }

    @Test
    public void testObjectFromInstanceContentConverterDisappearsIfNotReferencedIssue164431() {
        InstanceContent ic = new InstanceContent();
        Conv converter = new Conv("foo");
        ic.add(converter, converter);
        Lookup lkp = new AbstractLookup(ic);
        StringBuilder sb = lkp.lookup(StringBuilder.class);
        assertNotNull(sb);
        int hash = System.identityHashCode(sb);
        assertEquals("foo", sb.toString());
        Reference<StringBuilder> r = new WeakReference<StringBuilder>(sb);
        sb = null;
        assertGC("Lookup held onto object", r);
        sb = lkp.lookup(StringBuilder.class);
        assertNotSame(hash, System.identityHashCode(sb));
        r = new WeakReference<StringBuilder>(sb);
        sb = null;
        assertGC("Lookup held onto object", r);
        ic.remove(converter, converter);
        Reference<InstanceContent.Convertor> cref = new WeakReference<InstanceContent.Convertor>(converter);
        converter = null;
        assertGC("Converter still referenced", cref);  //FAILS HERE

        sb = lkp.lookup(StringBuilder.class);
        assertNull("Converter removed from lookup, but object it " +
                "created still present:'" + sb + "'", sb);  //ALSO FAILS HERE
        converter = new Conv("bar");
        ic.add(converter, converter);
        assertNotNull(lkp.lookup(StringBuilder.class));
        assertEquals("bar", lkp.lookup(StringBuilder.class).toString());
    }

    private static class Conv implements InstanceContent.Convertor<Conv, StringBuilder> {

        private final String str;

        private Conv(String str) {
            this.str = str;
        }

        public StringBuilder convert(Conv obj) {
            return new StringBuilder(str);
        }

        public Class<? extends StringBuilder> type(Conv obj) {
            return StringBuilder.class;
        }

        public String id(Conv obj) {
            return "FOo";
        }

        public String displayName(Conv obj) {
            return "Foo";
        }
    }

    private static class B {
    }

    private static final class A implements InstanceContent.Convertor<A, B> {

        public B convert(A obj) {
            return new B();
        }

        public Class<? extends B> type(A obj) {
            return B.class;
        }

        public String id(A obj) {
            return "" + System.identityHashCode(this);
        }

        public String displayName(A obj) {
            return id(obj);
        }
    }
}