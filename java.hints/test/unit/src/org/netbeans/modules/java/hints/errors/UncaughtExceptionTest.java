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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import org.netbeans.modules.java.hints.infrastructure.HintsTestBase;

/**
 * Tests for Uncaught Exceptions (add throws, aurround with try-catch)
 * @author Max Sauer
 */
public class UncaughtExceptionTest extends HintsTestBase {

    public UncaughtExceptionTest(String name) {
	super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.doSetUp("org/netbeans/modules/java/hints/resources/layer.xml");
    }
    
    @Override
    protected boolean createCaches() {
        return false;
    }
    
    @Override
    protected String testDataExtension() {
        return "org/netbeans/test/java/hints/UncaughtExceptionTest/";
    }
    
    /** Surround with try catch as this() parameter */
    public void testBug113448() throws Exception {
	performTestDoNotPerform("Test", 8, 22);
    }
    
    /** Surround with try catch as this() parameter, deeper in path */
    public void testBug113448b() throws Exception {
	performTestDoNotPerform("Test", 12, 33);
    }
    
    /** 
     * Surround with try catch as this() parameter
     * exception-throwing-method call
     */
    public void testBug113448c() throws Exception {
	performTestDoNotPerform("Test", 19, 33);
    }
    
    /**
     * Field access inside ctor
     */
    public void testBug113812() throws Exception {
	performTestDoNotPerform("Test", 31, 23);
    }
    
    /** Surround with try catch inside ctor */
    public void testInsideCtor() throws Exception {
	performTestDoNotPerform("Test", 16, 21);
    }
    
    /** Surround with try catch as this() parameter, 
     * but inside anonymous class. Should offer in this case.
     */
    public void testThisParamInsideAnonymous() throws Exception {
	performTestDoNotPerform("Test", 24, 30);
    }

}
