/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.junit;

import junit.framework.Test;

/**
 * NetBeans extension to JUnit Test interface
 */
public interface NbTest extends Test {

    /**
     * Checks if a test isn't filtered out by the active filter.
     * @return true if the test can run
     */
    public abstract boolean canRun();
    
    /**
     * Sets active filter.
     * @param filter Filter to be set as active for current test, null will reset filtering.
     */
    public abstract void setFilter(Filter filter);
    
    /**
     * Returns expected fail message.
     * @return expected fail message if it's expected this test fail, null otherwise.
     */
    public abstract String getExpectedFail(); 

}
