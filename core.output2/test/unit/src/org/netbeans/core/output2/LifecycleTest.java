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

package org.netbeans.core.output2;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.OutputWriter;

/**
 *
 * @author tim
 */
public class LifecycleTest extends NbTestCase {

    public LifecycleTest(String testName) {
        super(testName);
    }

    private IOContainer container;
    private NbIO io;
    JFrame jf = null;

    OutputTab tab = null;
    OutputPane pane = null;
    @Override
    protected void setUp() throws java.lang.Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                container = IOContainer.getDefault();
                jf = new JFrame();
                jf.getContentPane().setLayout(new BorderLayout());
                jf.getContentPane().add(getIOWindow(), BorderLayout.CENTER);
                jf.setBounds(20, 20, 700, 300);
                jf.setVisible(true);
                io = (NbIO) new NbIOProvider().getIO("Test", false);
                io.select();
                tab = (OutputTab) container.getSelected();
                pane = (OutputPane) tab.getOutputPane();
            }
        });
        if (tab == null) {
            fail("Failed in setup - selected tab was null");
        }
    }
    
    @Override
    protected void tearDown() {
        tab = null;
        pane = null;
        if (jf != null) {
            jf.dispose();
        }
        jf = null;
        if (io != null) {
            NbIOProvider.dispose(io);
        }
        io.closeInputOutput();
        io = null;
        container = null;
        sleep();
    }
    
    private final void sleep() {
        dosleep();
        dosleep();
        dosleep();
        int ct = 0;
        while (IOEvent.pendingCount > 0) {
            dosleep();
            ct++;
            if (ct > 1000) {
                fail ("After 1000 cycles on the event queue, there is still some IOEvent which was not processed");
            }
        }
    }
    
    private final void dosleep() {
        try {
            Thread.sleep(200);
            SwingUtilities.invokeAndWait (new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });
            Thread.sleep(200);
        } catch (Exception e) {
            fail (e.getMessage());
        }
    }

    public void testGetErr() throws Exception {
        System.out.println("testGetOut");
        ErrWriter err = io.writer().err();
        assertNull ("Error output should not be created yet", err);
        err = io.writer().getErr();
        assertNotNull ("Error output should never be null from getErr()", err);
        assertTrue ("Error output should initially be closed", err.isClosed());
        err.println ("Hello");
        assertFalse ("Error output should not be closed after writing to it", err.isClosed());
        err.close();
        assertTrue ("Error output be closed after calling close()", err.isClosed());
    }

    public void testClose() throws Exception {
        System.out.println("testClose");
        NbWriter writer = (NbWriter) io.getOut();
        ErrWriter err = writer.getErr();
        OutWriter out = writer.out();

        writer.reset();
        sleep();

        err.println ("hello");
        sleep();
        writer.println ("world");
        sleep();

//        assertTrue("Text in container not correct:\"" + pane.getTextView().getText() +"\"",
//            pane.getTextView().getText().equals ("hello\nworld\n\n\n"));

        assertFalse ("Err should not be closed", err.isClosed());
        assertFalse ("Writer should not be closed", writer.isClosed());
//        assertFalse ("Out should not be closed", out.isClosed());

        err.close();
        sleep();
        assertFalse ("Out is open, err is closed, writer should return false from isClosed()", writer.isClosed());

        writer.close();
        sleep();
        assertTrue ("Out should be closed after calling close() on it", out.isClosed());
        assertTrue ("Out and err are closed, but writer says it is not", writer.isClosed());

        assertTrue ("Output's storage is not closed", writer.out().getStorage().isClosed());

        writer.reset();
        sleep();

        assertTrue ("After reset, err should be closed", err.isClosed());
        assertTrue ("After reset, writer should be closed", writer.isClosed());
        assertTrue ("After reset, out should be closed", out.isClosed());

        err.println ("goodbye");
        writer.println ("world");
        sleep();

        assertFalse ("Err should not be closed", err.isClosed());
        assertFalse ("Writer should not be closed", writer.isClosed());
//        assertFalse ("Out should not be closed", out.isClosed());

        //Now close err & out in the opposite order
        writer.close();
        sleep();
        assertTrue ("Out should  be closed after calling close() on it", writer.isClosed());

        err.close();
        sleep();
        assertTrue ("Out is closed, err is closed, writer should return true from isClosed()", writer.isClosed());
        assertTrue ("Out and err are closed, but writer says it is not", writer.isClosed());

        assertTrue ("Output's storage is not closed", writer.out().getStorage().isClosed());

        err.println("I should be reopened now");
        sleep();

        assertFalse ("Err should be open", err.isClosed());
    }

    public void testReset() throws Exception {
        System.out.println("testReset");
        ErrWriter err = io.writer().getErr();
        OutWriter out = io.writer().out();
        NbWriter writer = io.writer();

        OutputDocument doc = (OutputDocument) pane.getDocument();
        assertNotNull ("Document should not be null", doc);

        err.println ("hello");
        writer.println ("world");
        sleep();
        writer.reset();
        sleep();

        assertTrue ("Same writer object should be used after a reset", io.writer() == writer);
        assertTrue ("Same err object should be used after a reset", io.writer().err() == err);
        assertTrue ("Different output should be used afer a reset", out != io.writer().out());

        assertNull ("Old document's Lines object not disposed - that means neither was its writer", doc.getLines());

        Exception e = null;
        try {
            out.getStorage();
        } catch (Exception exc) {
            e = exc;
        }
        assertNotNull ("OutWriter should have thrown an exception on trying to " +
            "fetch its storage after it was disposed.  It appears it wasn't disposed.", e);
    }

    public void testCloseInputOutput() throws Exception {

        System.out.println("testCloseInputOutput");
        ErrWriter err = io.writer().getErr();
        OutWriter out = io.writer().out();
        NbWriter writer = io.writer();

        err.println ("joy to the world");
        writer.println ("all the boys and girls");
        err.close();
        sleep();
        writer.close();
        sleep();
        io.closeInputOutput();
        sleep();
        assertNull ("Should be no selected tab after closeInputOutput", getSelectedTab());
    }

    public void testFilesCleanedUp() throws Exception {
        System.out.println("testFilesCleanedUp");
        NbWriter writer = io.writer();
        ErrWriter err = writer.getErr();
        OutWriter out = writer.out();

        err.println ("hello");
        writer.println ("world");
        sleep();

        assertTrue ("Output should not have changed - was " + out + " now " + io.writer().out(), io.writer().out() == out);
        FileMapStorage storage = (FileMapStorage) writer.out().getStorage();
        String fname = storage.toString();
        assertTrue ("FileMapStorage should be returning a file name", fname.indexOf("[") == -1);
        assertTrue ("FileMapStorage should be pointing to an existing file", new File(fname).exists());

        err.close();
        sleep();
        writer.close();
        sleep();
        io.closeInputOutput();
        sleep();

        assertTrue (out.isDisposed());
        sleep();
//        assertFalse ("FileMapStorage's file should have been deleted", new File(fname).exists());
    }

    public void testFastResets() throws IOException, InterruptedException {
        System.out.println("testFastResets");
        OutputWriter out = io.getOut();
        for (int i = 0; i < 100; i++) {
            for (int k = 0; k < 10; k++) {
                out.println(i + " " + k);
            }
            Thread.sleep(10);
            out.close();
            out.reset();
        }
    }

    public void testMultipleResetsAreHarmless() throws Exception {
        System.out.println("testMultipleResetsAreHarmless");
        NbWriter writer = io.writer();
        ErrWriter err = writer.getErr();
        OutWriter out = writer.out();

        assertTrue ("Before any writes, out should be empty", out.isEmpty());

        writer.reset();
        sleep();
        assertTrue ("Reset on an unused writer should not replace its output", writer.out() == out);

        writer.reset();
        writer.reset();
        writer.reset();
        sleep();
        assertTrue ("Reset on an unused writer should not replace its output", writer.out() == out);

        writer.println ("Now there is data");
        writer.reset();
        sleep();

        assertFalse ("Reset on a used writer should replace its underlying output", writer.out() == out);

    }

    static JComponent getIOWindow() {
        IOContainer container = IOContainer.getDefault();
        JComponent comp = null;
        try {
            try {
                Field f = container.getClass().getDeclaredField("provider");
                f.setAccessible(true);
                IOContainer.Provider prov = (IOContainer.Provider) f.get(container);
                Method m = prov.getClass().getDeclaredMethod("impl", new Class[0]);
                m.setAccessible(true);
                comp = (JComponent) m.invoke(prov);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NoSuchMethodException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return comp;
    }

    JComponent getSelectedTab() {
        class R implements Runnable {
            JComponent tab;
            public void run() {
                tab = container.getSelected();
            }
        }
        R r = new R();
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return r.tab;
    }
    
}
