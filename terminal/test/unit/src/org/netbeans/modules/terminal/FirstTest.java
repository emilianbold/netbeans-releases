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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.terminal.api.IONotifier;
import org.netbeans.modules.terminal.api.IOResizable;
import org.netbeans.modules.terminal.api.IOTerm;
import org.netbeans.modules.terminal.api.IOConnect;
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

    private static final class CloseVetoConfig {
	public final boolean isClosable;
	public final boolean registerVetoer;
	public final boolean closeStreamFirst;
	public final boolean closeIfDisconnected;
	public final boolean sayYes;

	public final boolean shouldSeeVetoable;
	public final boolean shouldBeClosed;

	public CloseVetoConfig(boolean isClosable,
			       boolean registerVetoer,
		               boolean closeStreamFirst,
			       boolean closeIfDisconnected,
			       boolean sayYes,
			       boolean shouldSeeVetoable,
	                       boolean shouldBeClosed) {
	    this.isClosable = isClosable;
	    this.registerVetoer = registerVetoer;
	    this.closeStreamFirst = closeStreamFirst;
	    this.closeIfDisconnected = closeIfDisconnected;
	    this.sayYes = sayYes;
	    this.shouldSeeVetoable = shouldSeeVetoable;
	    this.shouldBeClosed = shouldBeClosed;
	}

	public String toString() {
	    return String.format("isClosable %s\nregisterVetoer %b\ncloseStreamFirst %b\ncloseIfDisconnected %b\nsayYes %b\nshouldSeeVEtoable %b\nshouldBeClosed %b\n",
		    isClosable, registerVetoer, closeStreamFirst, closeIfDisconnected, sayYes, shouldSeeVetoable, shouldBeClosed);
	}
    }

    private static final CloseVetoConfig[] configs = new CloseVetoConfig[] {
	// Columns:
	//		isClosable	registerVetoer		sayYes		shouldSeeVetoable
	//					closeStreamFirst			shouldBeClosed
	//						closeIfDisconnected
	// AllowClose.NEVER
	// never see confirmer never close
	new CloseVetoConfig(false,	true,	false,	false,	false,		false,	false),
	new CloseVetoConfig(false,	true,	true,	false,	false,		false,	false),
	// no vetoer
	new CloseVetoConfig(false,	false,	false,	false,	false,		false,	false),
	new CloseVetoConfig(false,	false,	true,	false,	false,		false,	false),

	// AllowClose.ALWAYS
	new CloseVetoConfig(true,	true,	false, 	false,	false, 		true,	false),
	new CloseVetoConfig(true,	true,	false, 	false,	true, 		true,	true),
	new CloseVetoConfig(true,	true,	true, 	false,	false, 		true,	false),
	new CloseVetoConfig(true,	true,	true, 	false,	true, 		true,	true),

	// AllowClose.DISCONNECTED
	// still connected need confirmer
	new CloseVetoConfig(true,	true,	false,	true,	false, 		true,	false),
	new CloseVetoConfig(true,	true,	false,	true,	true, 		true,	true),

	// no longer connected see vetoable but no confirmer
	new CloseVetoConfig(true,	true,	true,	true,	false, 		true,	true),

	// no vetoer
	new CloseVetoConfig(true,	false,	true,	false,	false, 		false,	true),
	new CloseVetoConfig(true,	false,	false,	false,	false, 		false,	true),
    };

    private CloseVetoConfig currentCvc = null;
    private boolean sawVetoable = false;
    private boolean sawClose = false;

    private void testCloseVeto(CloseVetoConfig cvc) {

	VetoableChangeListener vcl = null;
	if (cvc.registerVetoer) {
	    vcl = new VetoableChangeListener() {
		public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		    if (evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY) &&
			evt.getNewValue().equals(Boolean.FALSE)) {

			sawVetoable = true;
			InputOutput src = (InputOutput) evt.getSource();
			if (currentCvc.closeIfDisconnected) {
			    if (IOConnect.isConnected(src)) {
				if (! currentCvc.sayYes)
				    throw new PropertyVetoException("don't close", evt);
			    } else {
				// close w/o confirming
			    }
			} else {
			    if (! currentCvc.sayYes)
				throw new PropertyVetoException("don't close", evt);
			}
		    }
		}
	    };
	}

	IONotifier.addVetoableChangeListener(io, vcl);
	currentCvc = cvc;
	sawVetoable = false;
	sawClose = false;
	try {

	    IOVisibility.setClosable(io, cvc.isClosable);
	    io.select();
	    io.getOut().println("Config X\r");
	    if (cvc.closeStreamFirst)
		io.getOut().close();

	    // This should first trigger a veto propery change followed by
	    // an actual property change
	    IOVisibility.setVisible(io, false);

	    // give it all time to settle down.
	    sleep(3);
	    assertTrue("sawVetoable != cvc.shouldSeeVetoable\n" + cvc, sawVetoable == cvc.shouldSeeVetoable);
	    assertTrue("sawClose != cvc.shouldSeeClose\n" + cvc, sawClose == cvc.shouldBeClosed);
	} finally {
	    IONotifier.removeVetoableChangeListener(io, vcl);
	}
    }



    public void testCloseVeto() {

	PropertyChangeListener pcl = new PropertyChangeListener() {
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY)) {
		    assertTrue("property change not on EDT", SwingUtilities.isEventDispatchThread());
		    assertTrue("Got event '" + evt.getPropertyName() + "' instead of PROP_VISIBILITY",
			evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY));
		    visible = (Boolean) evt.getNewValue();
		    if (visible == false)
			sawClose = true;
		} else if (evt.getPropertyName().equals(IOResizable.PROP_SIZE)) {
		} else {
		    System.out.printf("Unexpected event '%s'\n", evt.getPropertyName());
		}
	    }
	};

	IONotifier.addPropertyChangeListener(io, pcl);

	try {
	    for (CloseVetoConfig cvc : configs) {
		testCloseVeto(cvc);
	    }
	} finally {
	    IONotifier.removePropertyChangeListener(io, pcl);
	}
    }

    private void testTitleHelp(InputOutput tio) {
	// This test doesn't work very well visually because when running
	// under the testsuite tabs' names always appear in bold irregardless.

	// title should not be in bold
	tio.getOut().println("testTitle\r");

	// previous getOut() should cause title to become bold
	// tio.getOut().println("title should be in bold\r");
	sleep(4);

	if (!IOVisibility.isSupported(tio))
	    return;

	// will remove tab and attempt to adjust title
	// should not have problems
	IOVisibility.setVisible(tio, false);
	sleep(2);

	// Next time we become visible title should not be bold
	tio.getOut().close();
	sleep(2);
	assertTrue("getOut() still connected after close()", ! IOConnect.isConnected(tio));

	IOVisibility.setVisible(tio, true);
	sleep(4);

	// Currently unfortunately title is still in bold, but can't
	// figure why.
	// When I try in TerminalExamples by hand it works.
    }

    public void testTitle() {

	testTitleHelp(io);
	InputOutput io1 = ioProvider.getIO("io1", null, ioContainer);
	io1.select();
	testTitleHelp(io1);
	io1.closeInputOutput();
    }

    private boolean visible = true;

    public void testVisibilityNotification() {
	if (!IONotifier.isSupported(io))
	    return;

	PropertyChangeListener pcl = new PropertyChangeListener() {
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY)) {
		    assertTrue("property change not on EDT", SwingUtilities.isEventDispatchThread());
		    assertTrue("Got event '" + evt.getPropertyName() + "' instead of PROP_VISIBILITY",
			evt.getPropertyName().equals(IOVisibility.PROP_VISIBILITY));
		    visible = (Boolean) evt.getNewValue();
		} else if (evt.getPropertyName().equals(IOResizable.PROP_SIZE)) {
		} else {
		    System.out.printf("Unexpected event '%s'\n", evt.getPropertyName());
		}
	    }
	};
	IONotifier.addPropertyChangeListener(io, pcl);

	// setUp() calls select() so the terminal should be initially visible

	try {
	    // make it invisible
	    IOVisibility.setVisible(io, false);
	    sleep(1);
	    assertTrue("no visibility property change", visible == false);

	    // make it visible again
	    IOVisibility.setVisible(io, true);
	    sleep(1);
	    assertTrue("no visibility property change", visible == true);

	    // make it invisible again
	    IOVisibility.setVisible(io, false);
	    sleep(1);
	    assertTrue("no visibility property change", visible == false);

	    // make it visible again
	    IOVisibility.setVisible(io, true);
	    sleep(1);
	    assertTrue("no visibility property change", visible == true);
	} finally {
	    IONotifier.removePropertyChangeListener(io, pcl);
	}
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
	//
	// Both getOut() and getErr() must be closed and
	// IOTerm must be disconnecetd for stream to be
	// considered closed.

	if (defaultProvider) {
	    System.out.printf("testMultiStreamClose() Skipped\n");
	    return;
	}

	// just with out
	assertTrue("IO not initially stream-closed",
		   ! IOConnect.isConnected(io));
	io.getOut().println("Hello to io1\r");
	assertTrue("IO still stream-closed after getOut()",
		   IOConnect.isConnected(io));
	io.getOut().close();
	sleep(1);
	assertTrue("IO not stream-closed after out close",
		   ! IOConnect.isConnected(io));

	// just with err

	// LATER
	// VV's fix for missing getErr() uses IOColorLines to
	// implement println() and IOColorLines uses getOut() !
	// io.getErr().println("Hello to io1\r");
	io.getErr();
	assertTrue("IO still stream-closed after getErr()",
		   IOConnect.isConnected(io));
	io.getErr().close();
	sleep(1);
	assertTrue("IO not stream-closed after err close",
		   ! IOConnect.isConnected(io));

	// just using connect
	if (IOTerm.isSupported(io)) {
	    // just with IOTerm.connect()
	    IOTerm.connect(io, null, null, null);
	    assertTrue("IO still stream-closed after connect()",
		       IOConnect.isConnected(io));
	    IOTerm.disconnect(io, null);
	    sleep(1);
	    assertTrue("IO not stream-closed after disconnect",
		       ! IOConnect.isConnected(io));
	}


	// using all three close in one order
	assertTrue("IO should be stream-closed before \"all three\" test",
		   ! IOConnect.isConnected(io));
	io.getOut().println("Hello to io1\r");
	io.getErr();		// see above for why no print
	if (IOTerm.isSupported(io))
	    IOTerm.connect(io, null, null, null);
	assertTrue("IO should be stream-open after all 3 streams are open",
		   IOConnect.isConnected(io));

	if (IOTerm.isSupported(io))
	    IOTerm.disconnect(io, null);
	sleep(1);
	assertTrue("IO should still be stream-open after disconnect",
		   IOConnect.isConnected(io));
	io.getErr().close();
	sleep(1);
	assertTrue("IO should still be stream-open after closing err",
		   IOConnect.isConnected(io));
	io.getOut().close();
	sleep(1);
	assertTrue("IO should be stream-closed after closing out",
		   ! IOConnect.isConnected(io));
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