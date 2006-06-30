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

package org.netbeans.modules.editor.java;

import java.io.File;
import java.net.URI;
import org.netbeans.editor.ext.DataAccessor;
//import org.netbeans.editor.ext.java.DAFileProvider;
//import org.netbeans.editor.ext.java.JCFileProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

/**
 * Utilities for accessing input data for tests. It mounts
 * test/unit/src/org/netbeans/modules/editor/java/data folder
 * as local FS and make its subfolders accessible for you in tests.
 * Call setupData() in test's setUp() and cleanupData() in test's tearDown().
 * Then you can you rest of the utility methods.
 *
 * @autor David Konecny
 */
public final class TestUtils {
    
    private static LocalFileSystem lfs;

    /** Returns FO for test/unit/src/org/netbeans/modules/editor/java/data */
    public static synchronized FileObject getDataFolder() {
        if (lfs == null) {
            return null;
        }
        return lfs.getRoot();
    }
    
    /** Returns FS with root test/unit/src/org/netbeans/modules/editor/java/data */
    public static synchronized FileSystem getDataFilesystem() {
        return lfs;
    }
    
    
}
