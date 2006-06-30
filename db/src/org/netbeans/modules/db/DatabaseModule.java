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

package org.netbeans.modules.db;

import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseNodeChildren;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;

public class DatabaseModule extends ModuleInstall {
    
    public void close () {
        // XXX this method is called in the event thread and could take long
        // to execute
        
        // disconnect all connected connections
        // but try to not initialize the nodes if they haven't been initialized yet
        DatabaseNodeChildren rootNodeChildren = (DatabaseNodeChildren)RootNode.getInstance().getChildren();
        if (rootNodeChildren.getChildrenInitialized()) {
            DBConnection[] conns = ConnectionList.getDefault().getConnections();
            for (int i = 0; i < conns.length; i++) {
                try {
                    ((DatabaseConnection)conns[i]).disconnect();
                } catch (Exception e) {
                    // cf. issue 64185 exceptions should only be logged
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
        
        // stop all running runtimes
        DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes();
        for (int i = 0; i < runtimes.length; i++) {
            if (runtimes[i].isRunning()) {
                try {
                    runtimes[i].stop();
                } catch (Exception e) {
                    // cf. issue 64185 exceptions should only be logged                    
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
        }
    }
}
