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

import junit.framework.*;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class ObjectFactoryTest extends TestCase{
    /* A class implementation comment can go here. */

    public ObjectFactoryTest(String name){
        super(name);
    }


    public static void main(String args[]){
        junit.textui.TestRunner.run(suite());
    }


    public void testCreate(){
        nyi();
    }


    /**
     * Define suite of all the Tests to run.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(ObjectFactoryTest.class);
        return suite;
    }
    
    
    /**
     * Initialize; allocate any resources needed to perform Tests.
     */
    protected void setUp(){
    }
    
    
    /**
     * Free all the resources initilized/allocated to perform Tests.
     */
    protected void tearDown(){
    }
    
    
    private void nyi(){
        ///fail("Not yet implemented");                                 //NOI18N
    }
}
