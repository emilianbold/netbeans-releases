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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.dd.api.cmp;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

public interface Consistency extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String NONE = "None"; // NOI18N
    public static final String CHECK_MODIFIED_AT_COMMIT = "CheckModifiedAtCommit"; // NOI18N
    public static final String LOCK_WHEN_LOADED = "LockWhenLoaded"; // NOI18N
    public static final String CHECK_ALL_AT_COMMIT = "CheckAllAtCommit"; // NOI18N
    public static final String LOCK_WHEN_MODIFIED = "LockWhenModified"; // NOI18N
    public static final String LOCK_WHEN_MODIFIED2 = "LockWhenModified2"; // NOI18N
    public static final String CHECK_ALL_AT_COMMIT2 = "CheckAllAtCommit2"; // NOI18N
    public static final String CHECK_VERSION_OF_ACCESSED_INSTANCES = "CheckVersionOfAccessedInstances"; // NOI18N

    public void setNone(boolean value);
    public boolean isNone();

    public void setCheckModifiedAtCommit(boolean value);
    public boolean isCheckModifiedAtCommit();

    public void setLockWhenLoaded(boolean value);
    public boolean isLockWhenLoaded();

    public void setCheckAllAtCommit(boolean value);
    public boolean isCheckAllAtCommit();

    public void setLockWhenModified(boolean value);
    public boolean isLockWhenModified();

    public void setLockWhenModified2(boolean value) throws VersionNotSupportedException;
    public boolean isLockWhenModified2() throws VersionNotSupportedException;

    public void setCheckAllAtCommit2(boolean value);
    public boolean isCheckAllAtCommit2();

    public void setCheckVersionOfAccessedInstances(CheckVersionOfAccessedInstances value) throws VersionNotSupportedException;
    public CheckVersionOfAccessedInstances getCheckVersionOfAccessedInstances() throws VersionNotSupportedException;
    public CheckVersionOfAccessedInstances newCheckVersionOfAccessedInstances() throws VersionNotSupportedException;

}
