/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems;

import java.io.IOException;


/** Exception thrown to signal that external
* execution and compilation is not supported on a given filesystem.
*
* @author Jaroslav Tulach
* @deprecated Please use the <a href="@JAVA/API@/org/netbeans/api/java/classpath/api.html">ClassPath API</a> instead.
*/
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
