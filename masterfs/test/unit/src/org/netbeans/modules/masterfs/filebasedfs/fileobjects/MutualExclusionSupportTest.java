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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 * ResourcePoolTest.java
 * JUnit based test
 *
 * @author Radek Matous
 */
public class MutualExclusionSupportTest extends NbTestCase {

    public MutualExclusionSupportTest(String testName) {
        super(testName);
    }

    /**
     * Test of addResource method, of class org.netbeans.modules.masterfs.filebasedfs.streams.ResourcePool.
     */
    public void testAddResource() throws Exception  {
        Object key = new String ("key");
        
        MutualExclusionSupport.Closeable rem1 = MutualExclusionSupport.getDefault().addResource(key, true);        
        MutualExclusionSupport.Closeable rem2 = MutualExclusionSupport.getDefault().addResource(key, true);
        
        try {
            MutualExclusionSupport.Closeable rem3 = MutualExclusionSupport.getDefault().addResource(key, false);
            fail ();
        } catch (IOException iox) {}
        
        rem1.close();
        
        try {
            MutualExclusionSupport.Closeable rem3 = MutualExclusionSupport.getDefault().addResource(key, false);
            fail ();
        } catch (IOException iox) {}
        
        rem2.close();        
        
        try {
            MutualExclusionSupport.Closeable rem3 = MutualExclusionSupport.getDefault().addResource(key, false);            
            rem3.close();
        } catch (IOException iox) {
            fail ();
        }                        
        
        MutualExclusionSupport.Closeable rem4 = MutualExclusionSupport.getDefault().addResource(key, false);            

        try {
            rem1 = MutualExclusionSupport.getDefault().addResource(key, true);                    
            fail ();            
        } catch (IOException iox) {
        }                        
        
        try {
            MutualExclusionSupport.Closeable rem3 = MutualExclusionSupport.getDefault().addResource(key, false);            
            fail ();
        } catch (IOException iox) {
        }                        
        
        rem4.close();
        rem1 = MutualExclusionSupport.getDefault().addResource(key, true);                    
        rem1.close();
        MutualExclusionSupport.Closeable rem3 = MutualExclusionSupport.getDefault().addResource(key, false);            
        rem3.close();
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    
}
