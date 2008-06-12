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

package org.netbeans.modules.cnd.remote.server;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.explorer.nodes.RemoteServerNode;

/**
 *
 * @author gordonp
 */
public class RemoteServerList extends ArrayList<RemoteServerRecord> implements ServerList {
    
    public static final Object PROP_ACTIVE = "active"; // NOI18N
    public static final Object PROP_ADD = "add"; // NOI18N
    public static final Object PROP_REMOVE = "remove"; // NOI18N
    
    private static RemoteServerList serverList = null;
    
    private PropertyChangeSupport pcs;
    
    public static RemoteServerList getInstance() {
        if (serverList == null) {
            serverList = new RemoteServerList();
        }
        return serverList;
    }
    
    public RemoteServerList() {
        add(new RemoteServerRecord()); // creates the "localhost" record
        pcs = new PropertyChangeSupport(this);
    }
    
    public void add(String server, String user, boolean active) {
        RemoteServerRecord record = new RemoteServerRecord(server, user, active);
        RemoteServerNode node = new RemoteServerNode(record);
        add(record);
    }
    
    public void add(String server, String user) {
        add(server, user, false);
    }

    public ServerRecord getActive() {
        for (RemoteServerRecord record : this) {
            if (record.isActive()) {
                return record;
            }
        }
        return null;
    }

    public RemoteServerRecord getLocalhostRecord() {
        return get(0);
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    
    public void remotePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
}
