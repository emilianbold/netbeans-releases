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
/*
 * AssertionFileFailedError.java
 *
 * Created on February 15, 2001, 10:53 AM
 */

package org.netbeans.junit;

import junit.framework.AssertionFailedError;

/** Error thrown from assertFile file functions.
 * It describes the failure and holds the name of result of the file comapre process.
 * Generally, the error description should contain names of compared files.
 * @author  vstejskal
 * @version 1.0
 */
public class AssertionFileFailedError extends AssertionFailedError {
    protected String diffFile;
    /** Creates new AssertionFileFailedError 
     *  @param diffFile Fully-qualified name of the file containing differences (result of the file-diff).
     */
    public AssertionFileFailedError(String diffFile) {
        this(null, diffFile);
    }
    /** Creates new AssertionFileFailedError 
     *  @param message The error description menssage.
     *  @param diffFile Fully-qualified name of the file containing differences (result of the file-diff).
     */
    public AssertionFileFailedError(String message, String diffFile) {
        super(message);
        this.diffFile = diffFile;
    }
    public String getDiffFile() {
        return null != diffFile ? diffFile : "";
    }
}
