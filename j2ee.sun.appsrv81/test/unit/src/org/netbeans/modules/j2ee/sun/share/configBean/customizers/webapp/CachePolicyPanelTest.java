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
 * CachePolicyPanelTest.java
 * JUnit based test
 *
 * Created on March 18, 2004, 5:37 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import junit.framework.*;

import org.netbeans.modules.j2ee.sun.share.configbean.ASDDVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;

/**
 *
 * @author vkraemer
 */
public class CachePolicyPanelTest extends TestCase {
    
    public void testCreate() {
        CachePolicyPanel foo70 =
            new CachePolicyPanel(ASDDVersion.SUN_APPSERVER_7_0, StorageBeanFactory.getDefault().createCacheMapping());
        
        CachePolicyPanel foo81 =
            new CachePolicyPanel(ASDDVersion.SUN_APPSERVER_8_1, StorageBeanFactory.getDefault().createCacheMapping());
    }
    
    public CachePolicyPanelTest(java.lang.String testName) {
        super(testName);
    }
    
}
