/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.core;

import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/** Basic tests on NbClipboard
 *
 * @author Jaroslav Tulach
 */
public class NbClipboardTest extends NbTestCase {

    public NbClipboardTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        System.getProperties().remove("netbeans.slow.system.clipboard.hack");
    }

    protected void tearDown() throws Exception {
    }

    public void testDefaultOnJDK15AndLater() {
        if (System.getProperty("java.version").startsWith("1.4")) {
            return;
        }
        
        NbClipboard ec = new NbClipboard();
        assertTrue("By default we still do use slow hacks", ec.slowSystemClipboard);
    }
    public void testPropOnJDK15AndLater() {
        if (System.getProperty("java.version").startsWith("1.4")) {
            return;
        }
        
        System.setProperty("netbeans.slow.system.clipboard.hack", "false");
        
        NbClipboard ec = new NbClipboard();
        assertFalse("Property overrides default", ec.slowSystemClipboard);
        assertEquals("sun.awt.datatransfer.timeout is now 1000", "1000", System.getProperty("sun.awt.datatransfer.timeout"));
    }
    public void testOnMacOSX() throws Exception {
        String prev = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Darwin");
            Field f = Class.forName(Utilities.class.getName()).getDeclaredField("operatingSystem");
            f.setAccessible(true);
            f.set(null, -1);
            assertTrue("Is mac", Utilities.isMac());

            NbClipboard ec = new NbClipboard();
            assertFalse("MAC seems to have fast clipboard", ec.slowSystemClipboard);
        } finally {
            System.setProperty("os.name", prev);
        }
    }
    
    public void testMemoryLeak89844() throws Exception {
        class Safe implements Runnable {
            WeakReference<Object> ref;
            Window w;
            TopComponent tc;
            
            
            public void beforeAWT() throws InterruptedException {
                NbClipboard ec = new NbClipboard();
                
                tc = new TopComponent();
                tc.open();
                
                for(;;) {
                    w = SwingUtilities.getWindowAncestor(tc);
                    if (w != null && w.isVisible()) {
                        break;
                    }
                    Thread.sleep(100);
                }
                
                tc.close();
                w.dispose();
                
                // opening new frame shall clear all the AWT references to previous frame
                JFrame f = new JFrame("Focus stealer");
                f.setVisible(true);
                f.pack();
                f.toFront();
                f.requestFocus();
                f.requestFocusInWindow();
            }
            
            public void run() {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                
                ref = new WeakReference<Object>(w);
                w = null;
                tc = null;
            }
}
        
        Safe safe = new Safe();
        
        safe.beforeAWT();
        SwingUtilities.invokeAndWait(safe);
        
        try {
            assertGC("Top component can disappear", safe.ref);
        } catch (junit.framework.AssertionFailedError ex) {
            if (ex.getMessage().indexOf("NbClipboard") >= 0) {
                throw ex;
            }
            Logger.getAnonymousLogger().log(Level.WARNING, "Cannot do GC, but not due to NbClipboard itself", ex);
        }
    }
    
    public void testBusySystemClipboard139616() {
        BusyClipboard busyClipboard = new BusyClipboard();
        NbClipboard nbClipboard = new NbClipboard(busyClipboard);
        Transferable data = new Transferable() {

            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { DataFlavor.stringFlavor };
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.stringFlavor.equals(flavor);
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if( !isDataFlavorSupported(flavor) )
                    throw new UnsupportedFlavorException(flavor);
                return "unit_test";
            }
        };
        final Object LOCK = new Object();
        busyClipboard.addFlavorListener(new FlavorListener() {

            public void flavorsChanged(FlavorEvent e) {
                synchronized( LOCK ) {
                    LOCK.notifyAll();
                }
            }
        });
        nbClipboard.setContents(data, null);
        
        synchronized( LOCK ) {
            try {
                LOCK.wait(10*1000);
                assertEquals(2, busyClipboard.setContentsCounter);
                assertSame(data, busyClipboard.getContents(this));
            } catch( InterruptedException e ) {
                fail( "clipboard content was not updated" );
            }
        }
    }
    
    
    
    private static void waitEQ(final Window w) throws Exception {
        class R implements Runnable {
            boolean visible;
            
            public void run() {
                visible = w.isShowing();
            }
        }
        R r = new R();
        while (!r.visible) {
            SwingUtilities.invokeAndWait(r);
        }
    }
    
    private static class BusyClipboard extends Clipboard {

        public BusyClipboard() {
            super( "unit_test" );
        }
        private boolean pretendToBeBusy = true;
        private int setContentsCounter = 0;

        @Override
        public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
            setContentsCounter++;
            if( pretendToBeBusy ) {
                pretendToBeBusy = false;
                throw new IllegalStateException();
            }
            super.setContents(contents, owner);
        }
        
    }
}
