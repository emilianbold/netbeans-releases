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
 * TargetServerNodeTest.java
 * JUnit based test
 *
 * Created on March 17, 2004, 10:26 AM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes;

import junit.framework.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.RegisterServerAction;
import java.util.Collection;
import org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel;

import java.util.Arrays;

import org.netbeans.modules.j2ee.sun.share.SunDeploymentFactory;
import org.netbeans.modules.j2ee.sun.share.SunDeploymentManager;

/**
 *
 * @author Ludo
 */
public class TargetServerNodeTest extends TestCase {
    

    
    public void testCreateSheet() {
     //   TargetServerNode node = new TargetServerNode(null,new SunDeploymentManager(null,null,"localhost",4848));
     //   assertTrue(node.createSheet() !=null);
        
    }
        

    
    public void testCreate() {
        TargetServerNode node = new TargetServerNode(null,new SunDeploymentManager(null,null,"localhost",4848));
        assertTrue(node !=null);
    }
        
    

    
    public TargetServerNodeTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TargetServerNodeTest.class);
        return suite;
    }
        
}
