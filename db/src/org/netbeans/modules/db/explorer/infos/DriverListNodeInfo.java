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

package org.netbeans.modules.db.explorer.infos;
import java.util.Vector;
import java.text.MessageFormat;
import org.openide.util.RequestProcessor;

import org.openide.util.Utilities;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.DatabaseOption;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverListener;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.openide.ErrorManager;

public class DriverListNodeInfo extends DatabaseNodeInfo implements DriverOperations {
    
    static final long serialVersionUID =-7948529055260667590L;
    
    private JDBCDriverListener listener = new JDBCDriverListener() {
        public void driversChanged() {
            // fix for the deadlock in issue 69050: refresh in another thread
            // refreshChildren() acquires Children.MUTEX write access
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        refreshChildren();
                    } catch (DatabaseException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
        }
    };
    
    protected void initChildren(Vector children) throws DatabaseException {
        JDBCDriver[] drvs = JDBCDriverManager.getDefault().getDrivers();
        boolean win = Utilities.isWindows();
        String file;
        for (int i = 0; i < drvs.length; i++) {
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < drvs[i].getURLs().length; j++) {
                if (j != 0)
                    sb.append(", "); //NOI18N
                file = drvs[i].getURLs()[j].getFile();
                if (win)
                    file = file.substring(1);
                sb.append(file);
            }
            DatabaseDriver drv = new DatabaseDriver(drvs[i].getDisplayName(), drvs[i].getClassName(), sb.toString(), drvs[i]);
            DriverNodeInfo chinfo = (DriverNodeInfo) DatabaseNodeInfo.createNodeInfo(this, DatabaseNode.DRIVER);
            if (chinfo != null && drv != null) {
                chinfo.setDatabaseDriver(drv);
                children.add(chinfo);
            }
        }
    }

    /** Adds driver specified in drv into list.
    * Creates new node info and adds node into node children.
    */
    public void addDriver(DatabaseDriver drv) throws DatabaseException {
        DatabaseOption option = RootNode.getOption();
        Vector drvs = option.getAvailableDrivers();
        if (!drvs.contains(drv))
            drvs.add(drv);
        else {
            String message = MessageFormat.format(bundle().getString("EXC_DriverAlreadyExists"), new String[] {drv.toString()}); // NOI18N
            throw new DatabaseException(message);
        }

        DatabaseNodeChildren chld = (DatabaseNodeChildren)getNode().getChildren();
        DriverNodeInfo ninfo = (DriverNodeInfo)createNodeInfo(this, DatabaseNodeInfo.DRIVER);
        ninfo.setDatabaseDriver(drv);
        chld.createSubnode(ninfo, true);
    }
    
    public void setNode(DatabaseNode node) {
        super.setNode(node);
        JDBCDriverManager.getDefault().addDriverListener(listener);
    }
}
