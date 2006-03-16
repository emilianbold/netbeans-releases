/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.xml;

import java.io.File;
import java.io.OutputStream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.loader.AntProjectDataLoader;
import org.apache.tools.ant.module.loader.AntProjectDataObject;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Document;

// XXX testBasicParsing
// XXX testMinimumChangesFired

/**
 * Test {@link AntProjectSupport} parsing functionality.
 * @author Jesse Glick
 */
public class AntProjectSupportTest extends NbTestCase {
    
    static {
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        ((Lkp) Lookup.getDefault()).init();
    }
    
    public static final class Lkp extends ProxyLookup {
        public Lkp() {
            super(new Lookup[0]);
        }
        public void init() {
            setLookups(new Lookup[] {
                Lookups.singleton(SharedClassObject.findObject(AntProjectDataLoader.class, true)),
                Lookups.metaInfServices(Lkp.class.getClassLoader()),
            });
        }
    }
    
    public AntProjectSupportTest(String name) {
        super(name);
    }
    
    private FileObject scratch;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File scratchF = getWorkDir();
        scratch = FileUtil.toFileObject(scratchF);
        assertNotNull("FO for " + scratchF, scratch);
    }
    
    public void testInitiallyInvalidScript() throws Exception {
        FileObject fo = scratch.createData("build.xml");
        assertEquals("it is an APDO", AntProjectDataObject.class, DataObject.find(fo).getClass());
        AntProjectCookie apc = new AntProjectSupport(fo);
        TestCL l = new TestCL();
        apc.addChangeListener(l);
        assertNull("invalid", apc.getDocument());
        assertNotNull("invalid", apc.getParseException());
        FileLock lock = fo.lock();
        try {
            OutputStream os = fo.getOutputStream(lock);
            try {
                os.write("<project default='x'><target name='x'/></project>".getBytes("UTF-8"));
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
        assertTrue("got a change", l.expect(5000));
        Thread.sleep(1000); // XXX why??
        assertEquals("now valid (no exc)", null, apc.getParseException());
        Document doc = apc.getDocument();
        assertNotNull("now valid (have doc)", doc);
        assertEquals("one target", 1, doc.getElementsByTagName("target").getLength());
    }

    /**
     * Change listener that can be polled.
     * Handles asynchronous changes.
     */
    private static final class TestCL implements ChangeListener {
        
        private boolean fired;
        
        public TestCL() {}
        
        public synchronized void stateChanged(ChangeEvent e) {
            fired = true;
            notify();
        }
        
        /**
         * Check whether a change has occurred by now (do not block).
         * Also resets the flag so the next call will expect a new change.
         * @return true if a change has occurred
         */
        public synchronized boolean expect() {
            boolean f = fired;
            fired = false;
            return f;
        }
        
        /**
         * Check whether a change has occurred by now or occurs within some time.
         * Also resets the flag so the next call will expect a new change.
         * @param timeout a maximum amount of time to wait, in milliseconds
         * @return true if a change has occurred
         */
        public synchronized boolean expect(long timeout) throws InterruptedException {
            if (!fired) {
                wait(timeout);
            }
            return expect();
        }
        
    }
    
}
