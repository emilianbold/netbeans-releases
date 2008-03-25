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

package org.netbeans.modules.db.explorer.nodes;


import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseMetaDataTransferAccessor;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.ExTransferable;

/**
* Node representing open or closed connection to database.
*/
    
public class ConnectionNode extends DatabaseNode implements ChangeListener {
    private static final Logger LOGGER = Logger.getLogger(
            ConnectionNode.class.getName());
    
    private boolean createPropSupport = true;
    
    public ConnectionNode(DatabaseNodeInfo info) {
        super(info);
    }
    
    public void setInfo(DatabaseNodeInfo nodeinfo) {
        super.setInfo(nodeinfo);
        getCookieSet().add(this);
        
        nodeinfo.addChangeListener(WeakListeners.create(
                ChangeListener.class, this, nodeinfo));
    }

    @Override
    public void stateChanged(ChangeEvent evt) {
        super.stateChanged(evt);
        update();
    }
    

    private boolean createPropSupport() {
        return createPropSupport;
    }
    
    private void setPropSupport(boolean value) {
        createPropSupport = value;
    }
    
    
    
    private void update() { 
        RequestProcessor.getDefault().post(new Runnable() {
            public void run () {
                DatabaseNodeInfo info = getInfo();
                boolean connecting = (info.getConnection() != null);
                setIconBase((String)info.get(connecting ? "activeiconbase" : "iconbase")); //NOI18N
                Sheet.Set set = getSheet().get(Sheet.PROPERTIES);
                
                try {
                    if (createPropSupport()) {
                        Node.Property dbprop = set.get(DatabaseNodeInfo.DATABASE);
                        PropertySupport newdbprop = createPropertySupport(dbprop.getName(), dbprop.getValueType(), dbprop.getDisplayName(), dbprop.getShortDescription(), info, !connecting);
                        set.put(newdbprop);
                        firePropertyChange("db",dbprop,newdbprop); //NOI18N

                        Node.Property drvprop = set.get(DatabaseNodeInfo.DRIVER);
                        PropertySupport newdrvprop = createPropertySupport(drvprop.getName(), drvprop.getValueType(), drvprop.getDisplayName(), drvprop.getShortDescription(), info, !connecting);
                        set.put(newdrvprop);
                        firePropertyChange("driver",drvprop,newdrvprop); //NOI18N

                        Node.Property schemaprop = set.get(DatabaseNodeInfo.SCHEMA);
                        PropertySupport newschemaprop = createPropertySupport(schemaprop.getName(), schemaprop.getValueType(), schemaprop.getDisplayName(), schemaprop.getShortDescription(), info, !connecting);
                        set.put(newschemaprop);
                        firePropertyChange("schema",schemaprop,newschemaprop); //NOI18N

                        Node.Property usrprop = set.get(DatabaseNodeInfo.USER);
                        PropertySupport newusrprop = createPropertySupport(usrprop.getName(), usrprop.getValueType(), usrprop.getDisplayName(), usrprop.getShortDescription(), info, !connecting);
                        set.put(newusrprop);
                        firePropertyChange("user",usrprop,newusrprop); //NOI18N

                        Node.Property rememberprop = set.get(DatabaseNodeInfo.REMEMBER_PWD);
                        PropertySupport newrememberprop = createPropertySupport(rememberprop.getName(), rememberprop.getValueType(), rememberprop.getDisplayName(), rememberprop.getShortDescription(), info, connecting);
                        set.put(newrememberprop);
                        firePropertyChange("rememberpwd",rememberprop,newrememberprop); //NOI18N

                        setPropSupport(false);
                    } else {
                        Node.Property dbprop = set.get(DatabaseNodeInfo.DATABASE);
                        set.put(dbprop);
                        firePropertyChange("db",null,dbprop); //NOI18N

                        Node.Property drvprop = set.get(DatabaseNodeInfo.DRIVER);
                        firePropertyChange("driver",null,drvprop); //NOI18N

                        Node.Property schemaprop = set.get(DatabaseNodeInfo.SCHEMA);
                        firePropertyChange("schema",null,schemaprop); //NOI18N

                        Node.Property usrprop = set.get(DatabaseNodeInfo.USER);
                        firePropertyChange("user",null,usrprop); //NOI18N

                        Node.Property rememberprop = set.get(DatabaseNodeInfo.REMEMBER_PWD);
                        firePropertyChange("rememberpwd",null,rememberprop); //NOI18N
                    }
                } catch ( Exception e ) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
        });
    }
    
    /**
    * Can be destroyed only if connection is closed.
    */
    @Override
    public boolean canDestroy() {
        return !getInfo().isConnected();
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("ND_Connection"); //NOI18N
    }

    public Transferable clipboardCopy() throws IOException {
        ExTransferable result = ExTransferable.create(super.clipboardCopy());
        ConnectionNodeInfo cni = (ConnectionNodeInfo)getInfo().getParent(DatabaseNode.CONNECTION);
        final DatabaseConnection dbconn = ConnectionList.getDefault().getConnection(cni.getDatabaseConnection());
        result.put(new ExTransferable.Single(DatabaseMetaDataTransfer.CONNECTION_FLAVOR) {
            protected Object getData() {
                return DatabaseMetaDataTransferAccessor.DEFAULT.createConnectionData(dbconn.getDatabaseConnection(), dbconn.findJDBCDriver());
            }
        });
        return result;
    }

}
