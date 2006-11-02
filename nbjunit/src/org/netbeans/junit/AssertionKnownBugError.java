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

import junit.framework.AssertionFailedError;

/** Error thrown in case of test failed by known bug.
 * It describes the failure and holds the bug ID number.
 * @author  kaktus
 * @version 1.0
 */
public class AssertionKnownBugError extends AssertionFailedError {
    protected int bugID = 0;
    
    /** Creates new AssertionKnownBugError 
     *  @param bugID The bug number according bug report system.
     */
    public AssertionKnownBugError(int bugID) {
        this(bugID, null);
    }
    
    /** Creates new AssertionKnownBugError 
     *  @param bugID The bug number according bug report system.
     *  @param message The error description message.
     */
    public AssertionKnownBugError(int bugID, String message) {
        super(message);
        this.bugID = bugID;
    }
    
    /** Returnes bug ID number
     *  @return bugID The bug number according bug report system.
     */
    public int getBugID() {
        return bugID;
    }
}
