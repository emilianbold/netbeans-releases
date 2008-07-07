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

package org.netbeans.modules.cnd.remote.explorer.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.cnd.remote.actions.DeleteServerAction;
import org.netbeans.modules.cnd.remote.actions.DisplayPathMapperAction;
import org.netbeans.modules.cnd.remote.actions.SetDefaultAction;
import org.netbeans.modules.cnd.remote.server.RemoteServerList;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author gordonp
 */
public class RemoteServerNode extends AbstractNode implements PropertyChangeListener {
    
    private RemoteServerRecord record;
    private static final String SINGLE_SERVER_ICON = "org/netbeans/modules/cnd/remote/resources/single_server.png"; // NOI18N

    public RemoteServerNode(RemoteServerRecord record) {
        this(Children.LEAF, record);
        setName(record.getName());
        setIconBaseWithExtension(SINGLE_SERVER_ICON);
    }
    
    public RemoteServerNode(Children children, RemoteServerRecord record) {
        super(children);
        this.record = record;
        RemoteServerList.getInstance().addPropertyChangeListener(this);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action[] actions = {
            new DisplayPathMapperAction(record),
            new SetDefaultAction(record),
            null,
            new DeleteServerAction(record),
        };
        return actions;
    }
    
    @Override
    public String getHtmlDisplayName() {
        return record.isActive() ? "<b>" + getName() + "</b>" : getName(); // NOI18N
    }
    
    @Override
    public boolean canDestroy() {
        return !record.getName().equals("localhost"); // NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RemoteServerList.PROP_SET_AS_ACTIVE)) {
            if (record.isActive()) {
                fireDisplayNameChange(getName(), getHtmlDisplayName());
            } else {
                fireDisplayNameChange("", getName());
            }
        } else if (evt.getPropertyName().equals(RemoteServerList.PROP_DELETE_SERVER)) {
            try {
                destroy();
            } catch (IOException ex) {
            }
        }
    }
}
