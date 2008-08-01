/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/

package org.netbeans.modules.visualweb.gravy;

import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.jemmy.util.Dumper;

//import com.sun.rave.gravy.winsys.DockablePaneOperator;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyListener;

import java.io.*;

import java.util.Properties;

/**
 *
 * @author Alexandre (Shura) Iline (alexandre.iline@sun.com)
 */
public class RaveTestCase extends J2eeTestCase {

    TestProperties props;
    
    public RaveTestCase(String testName) {
	super(testName);
	props = new TestProperties();
	dumpScreen = true;
    }
    
    protected TestProperties getTestProperties() {
	return(props);
    }
    
//@todo
    
    protected boolean waitNoEvent = true;
    
// all code until end of class should be removed after JellyTestCase corresponding change - adding waitNoEvent member
    
    /** Overriden method from JUnit framework execution to perform conditional
     * screen shot and conversion from TimeoutExpiredException to AssertionFailedError. <br>
     * Waits a second before test execution.
     * @throws Throwable Throwable
     */
    public void runBare() throws Throwable {
	
	//close the damn window
//        if(Util.getMainWindow().
//           findSubComponent(new DockablePaneOperator.DockablePaneFinder("JUnitTestRunnerExecutor"))
//           != null) {
//            new DockablePaneOperator("JUnitTestRunnerExecutor").hide();
//        }
	// workaround for JDK bug 4924516 (see below)
	Toolkit.getDefaultToolkit().addAWTEventListener(distributingHierarchyListener,
	HierarchyEvent.HIERARCHY_EVENT_MASK);
	// wait
	if(waitNoEvent) {
	    new EventTool().waitNoEvent(1000);
	}
	try {
	    super.runBare();
	} catch (ThreadDeath td) {
	    // ThreadDead must be re-throwed immediately
	    throw td;
	} catch (Throwable th) {
	    // suite is notified about test failure so it can do some debug actions
	    try {
		failNotify(th);
	    } catch (Exception e3) {}
	    // screen capture is performed when test fails and in dependency on system property
	    if (captureScreen) {
		try {
		    PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screen.png");
		} catch (Exception e1) {}
	    }
	    // XML dump is performed when test fails and in dependency on system property
	    if (dumpScreen) {
		try {
		    Dumper.dumpAll(getWorkDir().getAbsolutePath()+File.separator+"screen.xml");
		} catch (Exception e2) {}
	    }
	    // closes all modal dialogs in dependency on systems property
	    if (closeAllModal) try {
		closeAllModal();
	    } catch (Exception e) {}
	    if (th instanceof JemmyException) {
		// all instancies of JemmyException are re-throwed as AssertionError (test failed)
		throw new AssertionFailedErrorException(th.getMessage(), th);
	    } else {
		throw th;
	    }
	} finally {
	    // workaround for JDK bug 4924516 (see below)
	    Toolkit.getDefaultToolkit().removeAWTEventListener(distributingHierarchyListener);
	}
    }

    public static Properties getDeploymentTargetProperties(String path_to_file) {
        Properties properties = new Properties();
        try {
            System.out.println("path to file is \"" + path_to_file + "\".");
            properties.load(new FileInputStream(path_to_file));
        } catch(Exception e) {
            System.out.println("Properties can not be loaded!");
            System.out.println("e=" + e.toString());
            return null;
        }
        return properties;
    }
    
    public static Properties getDeploymentTargetProperties() {
        String sep = File.separator;
        String path = System.getProperty("xtest.data.dir");
        path = path + sep + "DeploymentTargets.properties";
        return getDeploymentTargetProperties(path);
    }
    
    public static Properties getDefaultDeploymentTargetProperties() {
        String sep = File.separator;
        String path = System.getProperty("xtest.data.dir");
        path = path + sep + "DefaultDeploymentTargets.properties";
        return getDeploymentTargetProperties(path);
    }

    /* Workaround for JDK bug http://developer.java.sun.com/developer/bugParade/bugs/4924516.html.
     * Also see issue http://www.netbeans.org/issues/show_bug.cgi?id=32466.
     * ------------------------------------------------------------------------------------------
     * It can be removed when it is fixed (probably in JDK1.5.0). The following
     * listener is added to Toolkit at runBare() method and removed when it finishes.
     * It distributes HierarchyEvent to all listening components and its subcomponents.
     */
    private static final RaveTestCase.DistributingHierarchyListener
    distributingHierarchyListener = new RaveTestCase.DistributingHierarchyListener();
    
    private static class DistributingHierarchyListener implements AWTEventListener {
	
	public DistributingHierarchyListener() {
	}
	
	public void eventDispatched(java.awt.AWTEvent aWTEvent) {
	    HierarchyEvent hevt = null;
	    if (aWTEvent instanceof HierarchyEvent) {
		hevt = (HierarchyEvent) aWTEvent;
	    }
	    if (hevt != null && ((HierarchyEvent.SHOWING_CHANGED & hevt.getChangeFlags()) != 0)) {
		distributeShowingEvent(hevt.getComponent(), hevt);
	    }
	}
	
	private static void distributeShowingEvent(Component c, HierarchyEvent hevt) {
	    //HierarchyListener[] hierarchyListeners = c.getHierarchyListeners();
	    // Need to use component.getListeners because it is not synchronized
	    // and it not cause deadlock
	    HierarchyListener[] hierarchyListeners = (HierarchyListener[])(c.getListeners(HierarchyListener.class));
	    if (hierarchyListeners != null) {
		for (int i = 0; i < hierarchyListeners.length; i++) {
		    hierarchyListeners[i].hierarchyChanged(hevt);
		}
	    }
	    if (c instanceof Container) {
		Container cont = (Container) c;
		int n = cont.getComponentCount();
		for (int i = 0; i < n; i++) {
		    distributeShowingEvent(cont.getComponent(i), hevt);
		}
	    }
	}
    }
}
