/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.MutualExclusionSupport;

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

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(MutualExclusionSupportTest.class);
        
        return suite;
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
