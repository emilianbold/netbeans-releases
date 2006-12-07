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

package org.netbeans.junit;

/**
 *
 * @author Alexander Pepin
 */
public abstract class MultiTestCase extends NbTestCase{
    
    /**
     * Creates a new instance of MultiTestCase.
     * Set the class name as a name of the testcase.  
     */
    public MultiTestCase() {
        super(null);
        setName(this.getClass().getSimpleName());
    }
    
    /**
     * Creates a new instance of MultiTestCase with the given name.
     */
    public MultiTestCase(String name) {
        super(name);
    }
    
    private Throwable err = null;
    /**
     * Internal method to set an error occured while preparation for executing the testcase.
     */
    void setError(Throwable e){
        err = e;
    }
    
    /**
     * Internal method overriding the method of the TestCase class.
     * @exception Throwable if any exception is thrown
     */
    protected void runTest() throws Throwable {
        if(err != null)
            throw err;
        System.out.println("MultiTestCase:runTest "+getName());
        execute();
    }

    /**
     * Is a method to be executed to perform testing. 
     */
    protected abstract void execute();
    
}
