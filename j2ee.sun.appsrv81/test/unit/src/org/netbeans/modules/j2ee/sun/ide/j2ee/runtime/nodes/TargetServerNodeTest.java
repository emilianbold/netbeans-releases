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
    
}
