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

package org.openide.filesystems;

import java.io.IOException;

/** Exception thrown to signal that external
* execution and compilation is not supported on a given filesystem.
*
* @author Jaroslav Tulach
* @deprecated Please use the <a href="@org-netbeans-api-java@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
*/
@Deprecated
public class EnvironmentNotSupportedException extends IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1138390681913514558L;

    /** the throwing exception */
    private FileSystem fs;

    /**
    * @param fs filesystem that caused the error
    */
    public EnvironmentNotSupportedException(FileSystem fs) {
        this.fs = fs;
        assert FileUtil.assertDeprecatedMethod();
    }

    /**
    * @param fs filesystem that caused the error
    * @param reason text description for the error
    */
    public EnvironmentNotSupportedException(FileSystem fs, String reason) {
        super(reason);
        this.fs = fs;
        assert FileUtil.assertDeprecatedMethod();
    }

    /** Getter for the filesystem that does not support environment operations.
    */
    public FileSystem getFileSystem() {
        return fs;
    }
}
