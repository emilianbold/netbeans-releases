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

package org.netbeans.modules.db.sql.editor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteCookie;
import org.netbeans.modules.db.spi.sql.editor.SQLEditorProvider;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class SQLEditorProviderImpl implements SQLEditorProvider {
    
    // TODO: should ensure that the number of the generated temporary file
    // is greater than any of the numbers of the existing files

    private static final String CMD_FOLDER = "Databases/SQLCommands"; // NOI18N
    
    public void openSQLEditor(DatabaseConnection dbconn, String sql, boolean execute) {
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject tmpFo = root.getFileObject(CMD_FOLDER);
        if (tmpFo == null) {
            try {
                tmpFo = FileUtil.createFolder(root, CMD_FOLDER );
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        FileObject sqlFo = null;
        
        int i = 1;
        for (;;) {
            String nameFmt = NbBundle.getMessage(SQLEditorProviderImpl.class, "LBL_SQLCommandFileName");
            String name = MessageFormat.format(nameFmt, new Object[] { new Integer(i) });
            try {
                sqlFo = tmpFo.createData(name);
            } catch (IOException e) {
                i++;
                continue;
            }
            break;
        }
        
        try {
            FileLock lock = sqlFo.lock();
            try {
                OutputStream stream = sqlFo.getOutputStream(lock);
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8")); // NOI18N
                    try {
                        writer.write(sql);
                    } finally {
                        writer.close();
                    }
                } finally {
                    stream.close();
                }
            } finally {
                lock.releaseLock();
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        DataObject sqlDo;
        try {
            sqlDo = DataObject.find(sqlFo);
        } catch (DataObjectNotFoundException e) {
            Exceptions.printStackTrace(e);
            return;
        }
        
        OpenCookie openCookie = (OpenCookie)sqlDo.getCookie(OpenCookie.class);
        openCookie.open();
        
        SQLExecuteCookie sqlCookie = (SQLExecuteCookie)sqlDo.getCookie(SQLExecuteCookie.class);
        sqlCookie.setDatabaseConnection(dbconn);
        if (execute) {
            sqlCookie.execute();
        }
    }
}
