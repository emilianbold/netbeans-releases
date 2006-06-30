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
