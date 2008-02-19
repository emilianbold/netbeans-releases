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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql;

import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Delete a database
 * 
 * @author David Van Couvering
 */
public class DeleteDatabaseAction extends CookieAction {
    private static final Class[] COOKIE_CLASSES = new Class[] {
        DatabaseModel.class
    };

    public DeleteDatabaseAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
        
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(DeleteDatabaseAction.class).
                getString("LBL_DeleteDatabaseAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(DeleteDatabaseAction.class);
    }
    
    @Override
    public boolean enable(Node[] activatedNodes) {
        return true;
    }


    @Override
    protected int mode() {
        return MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return COOKIE_CLASSES;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for ( Node node : activatedNodes ) {
            DatabaseModel model = node.getCookie(DatabaseModel.class);
            ServerInstance server = model.getServer();
            String dbname = model.getDbName();
            
            try {
                server.dropDatabase(dbname);
                
                // Delete all the connections for this database, they
                // are no longer valid
                deleteConnections(server, dbname);
            } catch ( DatabaseException dbe ) {
                String msg = NbBundle.getMessage(DeleteDatabaseAction.class,
                        "MSG_ErrorDeletingDatabase", model.getDbName());
                Utils.displayError("MSG_ErrorDeletingDatabase", dbe);
            }
        }        
    }

    private void deleteConnections(ServerInstance server, String dbname) {
        // TODO - this requires API support from DB Explorer
        /*
        List<DatabaseConnection> conns = 
                DatabaseUtils.findDatabaseConnections(
                    server.getURL(dbname));
        
        for ( DatabaseConnection conn : conns ) {
            ConnectionManager.getDefault().
        }
         */
    }
}
