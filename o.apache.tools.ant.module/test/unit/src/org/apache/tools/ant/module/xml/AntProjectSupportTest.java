/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;

// XXX testBasicParsing
// XXX testMinimumChangesFired

/**
 * Test {@link AntProjectSupport} parsing functionality.
 * @author Jesse Glick
 */
public class AntProjectSupportTest extends NbTestCase {
    
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
        MockServices.setServices(AntProjectDataLoader.class);
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
