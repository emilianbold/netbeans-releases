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
