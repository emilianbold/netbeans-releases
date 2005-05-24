/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * MFolderNodeTest.java
 * JUnit based test
 *
 * Created on May 20, 2004, 4:19 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes;

import junit.framework.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.PropertiesAction;
import org.openide.util.RequestProcessor;
import java.util.TreeSet;
import javax.management.ObjectName;

import javax.management.NotificationListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.Util;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.RefreshAction;

/**
 *
 * @author vkraemer
 */
public class MFolderNodeTest extends TestCase {
    
    public void testStaticMethods() {
        MFolderNode.getMgmtApplicationQueries("foo");
        MFolderNode.getMgmtConnectorQueries("foo");
        MFolderNode.getMgmtJMSQueries("foo");
        MFolderNode.getMgmtJdbcQueries("foo");
        MFolderNode.getMgmtJndiQueries("foo");
    }
    
    public MFolderNodeTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MFolderNodeTest.class);
        return suite;
    }
    
}
