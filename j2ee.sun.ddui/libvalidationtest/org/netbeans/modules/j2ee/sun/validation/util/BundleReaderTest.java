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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.io.File;

import junit.framework.*;

import org.netbeans.modules.j2ee.sun.validation.Constants;
import org.netbeans.modules.j2ee.sun.validation.util.Utils;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class BundleReaderTest extends TestCase{
    /* A class implementation comment can go here. */

    public BundleReaderTest(String name){
        super(name);
    }


    public static void main(String args[]){
        junit.textui.TestRunner.run(suite());
    }


    public void testGetValue() {
        String str = BundleReader.getValue("non_existing_key");         //NOI18N
        assertTrue(str.equals("non_existing_key"));                     //NOI18N
        str = BundleReader.getValue("MSG_NumberConstraint_Failure");    //NOI18N
        assertTrue(!str.equals("MSG_NumberConstraint_Failure"));        //NOI18N
    }


    public void testCreate() {
        String bundleFile = "org/netbeans/modules/" +                   //NOI18N
            "j2ee/sun/validation/Bundle.properties";                    //NOI18N
        Utils utils = new Utils();
        boolean fileExists = utils.fileExists(bundleFile);
        String str = 
            BundleReader.getValue("MSG_NumberConstraint_Failure");      //NOI18N
        
        if(fileExists){
            assertTrue(!str.equals("MSG_NumberConstraint_Failure"));    //NOI18N
        } else {
            assertTrue(str.equals("MSG_NumberConstraint_Failure"));     //NOI18N
        }
    }


    /**
     * Define suite of all the Tests to run.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(BundleReaderTest.class);
        return suite;
    }


    /**
     * Initialize; allocate any resources needed to perform Tests.
     */
    protected void setUp() {
    }


    /**
     * Free all the resources initilized/allocated to perform Tests.
     */
    protected void tearDown() {
    }


    private void nyi() {
        ///fail("Not yet implemented");                                 //NOI18N
    }
}
