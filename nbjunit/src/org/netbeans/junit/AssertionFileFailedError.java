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
