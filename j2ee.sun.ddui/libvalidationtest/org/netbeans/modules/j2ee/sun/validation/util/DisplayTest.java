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
import org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintFailure;

import java.util.ArrayList;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class DisplayTest extends TestCase{
    /* A class implementation comment can go here. */

    public DisplayTest(String name){
        super(name);
    }


    public static void main(String args[]){
        junit.textui.TestRunner.run(suite());
    }


    public void testText() {
        CustomDisplay display = new CustomDisplay();

        
        ArrayList failureMessages = new ArrayList();
        ConstraintFailure failure_abc = 
            new ConstraintFailure("abc failed", "value_abc",            //NOI18N
                 "name_abc", "failureMessage_abc",                      //NOI18N
                    "genericFailureMessage_abc");                       //NOI18N
        ConstraintFailure failure_xyz = 
            new ConstraintFailure("constraint_xyz", "value_xyz",        //NOI18N
                "name_xyz", "failureMessage_xyz",                       //NOI18N
                    "genericFailureMessage_xyz");                       //NOI18N
        failureMessages.add(failure_abc);
        failureMessages.add(failure_xyz);
        display.text(failureMessages);

        //test to make sure text() reports error, if the Collection it is 
        //processing has objects that are not of type Failure.
        ArrayList failures = new ArrayList();
        failures.add(new Integer(5));
        failures.add("failure_message");                                //NOI18N
        display.text(failures);
    }


    /**
     * Define suite of all the Tests to run.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(DisplayTest.class);
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
    
    class CustomDisplay extends Display
    {
        CustomDisplay(){
            super();
        }

        protected void reportFailure(String message){
            assertTrue((message.equals("failureMessage_abc"))           //NOI18N
                    ||(message.equals("failureMessage_xyz")));          //NOI18N
        }

        protected void reportError(Object object){
            Class classObject = object.getClass();
            String objectType = classObject.getName();
            assertTrue((objectType.equals("java.lang.Integer"))         //NOI18N
                    ||(objectType.equals("java.lang.String")));         //NOI18N
        }
    }
}
