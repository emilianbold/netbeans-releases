/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.MessageFormat;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.SQLExecuteCookie;
import org.netbeans.modules.db.spi.sql.editor.SQLEditorProvider;
import org.netbeans.modules.db.sql.editor.ui.actions.ConnectionAction;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
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
                ErrorManager.getDefault().notify(e);
            }
        }
        
        FileObject sqlFo = null;
        
        int i = 1;
        for (;;) {
            String nameFmt = NbBundle.getMessage(SQLEditorProviderImpl.class, "LBL_SqlCommandFileName");
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
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
                    writer.write(sql);
                    writer.close();
                } finally {
                    stream.close();
                }
            } finally {
                lock.releaseLock();
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        DataObject sqlDo;
        try {
            sqlDo = DataObject.find(sqlFo);
        } catch (DataObjectNotFoundException e) {
            ErrorManager.getDefault().notify(e);
            return;
        }
        
        OpenCookie openCookie = (OpenCookie)sqlDo.getCookie(OpenCookie.class);
        openCookie.open();
        
        SQLExecuteCookie sqlCookie = (SQLExecuteCookie)sqlDo.getCookie(SQLExecuteCookie.class);
        ConnectionAction.setConnectionForCookie(sqlCookie, dbconn);
        
        if (execute) {
            try {
                sqlCookie.executeSQL(dbconn);
            } catch (SQLException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
}
