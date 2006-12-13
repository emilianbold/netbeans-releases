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
 * Extension to MultiTestCase class.
 * @author Alexander Pepin
 */
public abstract class ParametricTestCase extends MultiTestCase{
    
    /**
     * Creates a new instance of ParametricTestCase.
     */
    public ParametricTestCase() {
        super();
    }
    
    /**
     * Creates a new instance of ParametricTestCase with the given name.
     */
    public ParametricTestCase(String name) {
        super(name);
    }
    
    
    /**
     * Is called by ParametricTestSuite before calling <code>execute()</code>.
     * Can be overridden to perform some initializing.
     *
     * @param initializing parameter of type <code>Object</code>.
     */
    protected void parametrize(Object parameter){
    }

}
