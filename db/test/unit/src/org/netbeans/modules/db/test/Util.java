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

package org.netbeans.modules.db.test;

import java.io.IOException;
import org.netbeans.modules.db.explorer.DatabaseConnectionConvertor;
import org.netbeans.modules.db.explorer.driver.JDBCDriverConvertor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 *
 * @author Andrei Badea
 */
public class Util {
    
    private Util() {
    }
    
    public static void deleteConnectionFiles() throws IOException {
        deleteFileObjects(getConnectionsFolder().getChildren());
    }
    
    public static void deleteDriverFiles() throws IOException {
        deleteFileObjects(getDriversFolder().getChildren());
    }
    
    public static FileObject getConnectionsFolder() throws IOException {
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        return FileUtil.createFolder(root, DatabaseConnectionConvertor.CONNECTIONS_PATH);
    }
    
    public static FileObject getDriversFolder() throws IOException {
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        return FileUtil.createFolder(root, JDBCDriverConvertor.DRIVERS_PATH);
    }
    
    private static void deleteFileObjects(FileObject[] fos) throws IOException {
        for (int i = 0; i < fos.length; i++) {
            fos[i].delete();
        }
    }
}
