/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.db.explorer.infos;
import java.util.Vector;
import java.text.MessageFormat;
import org.openide.util.RequestProcessor;

import org.openide.util.Utilities;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseDriver;
import org.netbeans.modules.db.explorer.DatabaseOption;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverListener;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.openide.util.Exceptions;

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
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }
    };
    
    public DriverListNodeInfo() {
        JDBCDriverManager.getDefault().addDriverListener(listener);
    }
    
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
        DatabaseOption option = RootNodeInfo.getOption();
        Vector drvs = option.getAvailableDrivers();
        if (!drvs.contains(drv))
            drvs.add(drv);
        else {
            String message = MessageFormat.format(bundle().getString("EXC_DriverAlreadyExists"), new String[] {drv.toString()}); // NOI18N
            throw new DatabaseException(message);
        }

        DriverNodeInfo ninfo = (DriverNodeInfo)createNodeInfo(this, DatabaseNodeInfo.DRIVER);
        ninfo.setDatabaseDriver(drv);
        
        notifyChange();
    }  
    
    @Override
    public String getDisplayName() {
        return bundle().getString("NDN_Drivers"); //NOI18N

    }
    
    @Override
    public String getShortDescription() {
        return bundle().getString("ND_DriverList"); //NOI18N
    }    
}
