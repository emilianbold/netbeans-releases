/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.nodes;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.soa.ldap.LDAP;
import org.netbeans.modules.soa.ldap.LDAPChangeEvent;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.netbeans.modules.soa.ldap.LDAPEvent;
import org.netbeans.modules.soa.ldap.LDAPListener;
import org.netbeans.modules.soa.ldap.nodes.actions.BrowseLDAPAction;
import org.netbeans.modules.soa.ldap.nodes.actions.EditLDAPConnectionAction;
import org.netbeans.modules.soa.ldap.properties.ConnectionPropertyType;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author anjeleevich
 */
public class LDAPConnectionNode extends AbstractNode implements LDAPListener {

    private LDAPConnection connection;

    public LDAPConnectionNode(LDAPConnection connection) {
        super(Children.LEAF, Lookups.singleton(connection));
        this.connection = connection;

        setIconBaseWithExtension(
                "org/netbeans/modules/soa/ldap/resources/ldap-connection.png"); // NOI18N

        LDAP.INSTANCE.addLDAPChangeListener(WeakListeners
                .create(LDAPListener.class, this, null));

        updateDisplayName();
    }

    private void updateDisplayName() {
        setDisplayName(connection.getDisplayName());
    }

    @Override
    public Action[] getActions(boolean arg0) {
        return new Action[] {
            SystemAction.get(BrowseLDAPAction.class),
            null,
            SystemAction.get(EditLDAPConnectionAction.class),
            null,
            SystemAction.get(DeleteAction.class),
        };
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(BrowseLDAPAction.class);
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        LDAP.INSTANCE.deleteConnection(connection);
    }

    public void ldapConnectionChanged(LDAPChangeEvent event) {
        if (event.getLDAPConnection() != connection) {
            return;
        }

        if (event.isPropertyChanged(ConnectionPropertyType.CONNECTION_NAME)) {
            updateDisplayName();
        }
    }

    public void ldapConnectionRemoved(LDAPEvent event) {
        // ignores this kind of events
    }

    public void ldapConnectionAdded(LDAPEvent event) {
        // ignore this kind of events
    }
}