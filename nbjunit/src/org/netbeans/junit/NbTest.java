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
