/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.db.mysql.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.util.NbBundle;

/**
 * This class encapsulates a database connection and serializes
 * interaction with this connection through a blocking queue.
 *
 * @author David Van Couvering
 */
public class ConnectionProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ConnectionProcessor.class.getName());
    final BlockingQueue<Runnable> inqueue;
    
    private Connection conn;
    private Thread taskThread;
    
    void setConnection(Connection conn) {
        this.conn = conn;
    }
    
    Connection getConnection() {
        return this.conn;
    }

    void validateConnection() throws DatabaseException {
        try {
            if (conn == null || conn.isClosed()) {
                conn = null;
                throw new DatabaseException(NbBundle.getMessage(ConnectionProcessor.class, "MSG_ConnectionLost"));
            }

            // Send a command to the server, if it fails we know the connection is invalid.
            conn.getMetaData().getTables(null, null, " ", new String[] { "TABLE" }).close();
        } catch (SQLException e) {
            conn = null;
            LOGGER.log(Level.FINE, null, e);
            throw new DatabaseException(NbBundle.getMessage(ConnectionProcessor.class, "MSG_ConnectionLost"), e);
        }
    }
    
    boolean isConnected() {
        return conn != null;
    }

    boolean isConnProcessorThread() {
        return Thread.currentThread().equals(taskThread);
    }
    
    public ConnectionProcessor(BlockingQueue<Runnable> inqueue) {
        this.inqueue = inqueue;
    } 
    
    public void run() {
        taskThread = Thread.currentThread();
        for ( ; ; ) {
            try {              
                Runnable command = inqueue.take();
                
                command.run();                
            } catch ( InterruptedException ie ) {
                return;
            }
        }
    }    
}
