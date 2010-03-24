/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.terminal;

import java.awt.BorderLayout;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.terminal.api.IOTerm;
import org.netbeans.modules.terminal.test.IOTest;
import org.netbeans.modules.terminal.api.IOVisibility;
import org.netbeans.modules.terminal.api.TerminalContainer;
import org.openide.util.Exceptions;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author ivan
 */
public class FirstTest extends NbTestCase {

    private JFrame frame;
    private JComponent actualContainer;
    private IOContainer ioContainer;
    private IOProvider ioProvider;
    private InputOutput io;

    boolean defaultContainer = false;
    // LATER: my IOContainer doesn't do well with output2 IOProvider
    boolean defaultProvider = false;


    public FirstTest(String testName) {
	super(testName);
    }

    private static void sleep(int seconds) {
	try {
	    Thread.sleep(seconds * 1000);
	} catch(InterruptedException x) {
	    fail("sleep interrupted");
	}
    }

    @Override
    protected void setUp() throws Exception {
	System.out.printf("setUp()\n");

	if (defaultContainer) {
	    ioContainer = IOContainer.getDefault();
	    actualContainer = defaultContainer(ioContainer);
	} else {
	    TerminalContainer tc = TerminalContainer.create(null, "Test");
	    actualContainer = tc;
	    ioContainer = tc.ioContainer();
	}

        SwingUtilities.invokeAndWait(new Runnable() {
	    @Override
            public void run() {

		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(actualContainer, BorderLayout.CENTER);
		frame.setBounds(20, 20, 700, 300);
		frame.setVisible(true);

	    }
	});

	if (defaultProvider) {
	    ioProvider = IOProvider.getDefault();
	    assertNotNull ("Could not find IOProvider", ioProvider);
	} else {
	    ioProvider = IOProvider.get("Terminal");
	    assertNotNull ("Could not find IOProvider", ioProvider);
	    assertTrue("Got default IOProvider", ioProvider != IOProvider.getDefault());
	}
	io = ioProvider.getIO("test", null, ioContainer);
	assertNotNull ("Could not get InputOutput", io);
	io.select();
    }

    @Override
    protected void tearDown() throws Exception {
	System.out.printf("tearDown()\n");

	io.closeInputOutput();
	io = null;
	ioProvider = null;
	ioContainer = null;
	actualContainer = null;

        SwingUtilities.invokeAndWait(new Runnable() {
	    @Override
            public void run() {
		frame.dispose();
		frame = null;
	    }
	});
    }

    public void testHello() {
	System.out.printf("testHello()\n");

	io.getOut().println("Hello to Out\r");
	io.getErr().println("Hello to Err\r");
	sleep(4);
    }

    public void testStreamClose() {
	// getIO(String, boolean newIO=false) reuses an IO
	// that is not stream-open (i.e. streams never started
	// or all closed.
	System.out.printf("testStreamClose()\n");

	InputOutput io1 = ioProvider.getIO("io1", null, ioContainer);

	InputOutput io2;
	InputOutput io3;

	// until we open any streams reusing getIO should find it
	io2 = ioProvider.getIO("io1", false);
	assertTrue("reusing getIO() didn't find unopened IO", io2 == io1);

	// after opening an io stream reusing getIO should create a new one.
	io1.select();		// so we can check the output
	io1.getOut().println("Hello to io1\r");
	sleep(4);
	io2 = ioProvider.getIO("io1", false);
	if (defaultProvider) {
	    // doesn't work as advertised
	    // the following will appear in "io1".
	    io2.getOut().println("Hello to io2\r");
	} else {
	    assertFalse("reusing getIO() found opened IO", io2 == io1);
	    // This will appear in a separate window, IOContainer.default().
	    // See BZ #182538().
	    // See BZ #
	    io2.select();
	    io2.getOut().println("Hello to io2\r");
	}
	sleep(2);

	// after closing io stream reusing getIO should find it
	io1.getOut().close();
	io3 = ioProvider.getIO("io1", false);
	assertTrue("reusing getIO() didn't find stream closed IO", io3 == io1);

	// at this point io1 and io3 point to the same io.

	// but we can't write to it because we've closed it
	io1.select();		// so we can check the output
	io1.getOut().println("Should not appear\r");
	sleep(3);

	// until we reset it
	try {
	    io1.getOut().reset();
	} catch (IOException ex) {
	    Exceptions.printStackTrace(ex);
	    fail("reset() failed");
	}
	io1.select();		// so we can check the output
	io1.getOut().println("Hello to io1 after reset\r");
	sleep(4);
    }

    public void testMultiStreamClose() {
	// One of getOut() or getErr() or IOTerm.connect()
	// will mark the stream as open.
	// Both getOut() and getErr() must be closed and
	// IOTerm must be disconnecetd for stream to be
	// considered closed.
	if (defaultProvider) {
	    System.out.printf("Skipped\n");
	    return;
	}

	// just with out
	assertTrue("IO not initially stream-closed",
		   ! IOTest.isStreamConnected(io));
	io.getOut().println("Hello to io1\r");
	assertTrue("IO still stream-closed after getOut()",
		   IOTest.isStreamConnected(io));
	io.getOut().close();
	sleep(1);
	assertTrue("IO not stream-closed after out close",
		   ! IOTest.isStreamConnected(io));

	// just with err

	// LATER
	// VV's fix for missing getErr() uses IOColorLines to
	// implement println() and IOColorLines uses getOut() !
	// io.getErr().println("Hello to io1\r");
	io.getErr();
	assertTrue("IO still stream-closed after getErr()",
		   IOTest.isStreamConnected(io));
	io.getErr().close();
	sleep(1);
	assertTrue("IO not stream-closed after err close",
		   ! IOTest.isStreamConnected(io));

	// using connect
	if (IOTerm.isSupported(io)) {
	    // just with IOTerm.connect()
	    IOTerm.connect(io, null, null, null);
	    assertTrue("IO still stream-closed after connect()",
		       IOTest.isStreamConnected(io));
	    IOTerm.disconnect(io, null);
	    sleep(1);
	    assertTrue("IO not stream-closed after disconnect",
		       ! IOTest.isStreamConnected(io));
	}


	// using all three
	assertTrue("IO should be stream-closed before \"all three\" test",
		   ! IOTest.isStreamConnected(io));
	io.getOut().println("Hello to io1\r");
	io.getErr();		// see above for why no print
	if (IOTerm.isSupported(io))
	    IOTerm.connect(io, null, null, null);
	assertTrue("IO should be stream-open after all 3 streams are open",
		   IOTest.isStreamConnected(io));

	if (IOTerm.isSupported(io))
	    IOTerm.disconnect(io, null);
	assertTrue("IO should still be stream-open after disconnect",
		   IOTest.isStreamConnected(io));
	io.getErr().close();
	assertTrue("IO should still be stream-open after closing err",
		   IOTest.isStreamConnected(io));
	io.getOut().close();
	assertTrue("IO should be stream-closed after closing out",
		   ! IOTest.isStreamConnected(io));
    }

    public void testWeakClose() {
	// weak closing removes IO from container
	// select() reinstalls it.

	System.out.printf("testWeakClose()\n");
	InputOutput ios[] = new InputOutput[4];
	ios[0] = io;
	sleep(1);
	// SHOULD not become visible unless we call select
	// BZ 181064
	ios[1] = ioProvider.getIO("test1", null, ioContainer);
	sleep(1);
	ios[2] = ioProvider.getIO("test2", null, ioContainer);
	sleep(1);
	ios[3] = ioProvider.getIO("test3", null, ioContainer);

	sleep(4);

	IOVisibility.setVisible(ios[3], false);
	sleep(1);
	IOVisibility.setVisible(ios[2], false);
	sleep(1);
	IOVisibility.setVisible(ios[1], false);
	// LATER ... who knows what wil happen:
	// IOTest.setVisible(ios[0], false);

	sleep(4);

	ios[3].select();
	sleep(1);

	ios[2].select();
	sleep(1);

	ios[1].select();
	sleep(1);

	sleep(3);
    }

    /**
     * Use reflection to extract private IOWindow instance so
     * we can embed it in a JFrame.
     */
    static JComponent defaultContainer(IOContainer ioContainer) {
        JComponent comp = null;
        try {
            try {
                Field f = ioContainer.getClass().getDeclaredField("provider");
                f.setAccessible(true);
                IOContainer.Provider prov = (IOContainer.Provider) f.get(ioContainer);
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
}