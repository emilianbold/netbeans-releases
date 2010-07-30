/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.explorer.oracle;

import java.util.Arrays;
import javax.swing.Action;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.node.NodeDataLookup;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Rechtacek
 */
public class PredefinedConnectionNode extends BaseNode implements Comparable<Node> {

    private static final String PREDEFINED_CONNECTION_BASE = "org/netbeans/modules/db/resources/addConnection.png"; // NOI18N
    private static final String FOLDER = "Root/PredefinedConnection"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(PredefinedConnectionNode.class.getName());
    private final PredefinedConnectionProvider.PredefinedConnection connection;
    private String dn;
   
    private PredefinedConnectionNode(NodeDataLookup lookup, NodeProvider provider) {
        super(lookup, FOLDER, provider);
        connection = getLookup().lookup(PredefinedConnectionProvider.PredefinedConnection.class);
        //lookup.add(DatabaseConnectionAccessor.DEFAULT.createDatabaseConnection(connection));
        
        // XXX: allow to add non-connection nodes for predefined templates
    }

    public static PredefinedConnectionNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        PredefinedConnectionNode node = new PredefinedConnectionNode(dataLookup, provider);
        node.setup();
        return node;
    }
    
    @Override
    protected void initialize() {
        if (connection instanceof PredefinedConnectionProvider.OracleConnection) {
            dn = NbBundle.getMessage(PredefinedConnectionNode.class, "OracleConnectionName");
        } else if (connection instanceof PredefinedConnectionProvider.MySQLConnection) {
            dn = NbBundle.getMessage(PredefinedConnectionNode.class, "MySQLConnectionName");
        } else {
            assert false : "No PredefinedConnection found in lookup.";
        }
    }

    @Override
    public String getIconBase() {
        return PREDEFINED_CONNECTION_BASE;
    }

    @Override
    public String getName() {
        return dn;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);
    }

    @Override
    public Action getPreferredAction() {
        assert getActions(false) != null : "Some action found.";
        assert getActions(false).length == 1 : "Only one action found, but " + Arrays.asList(getActions(false));
        return getActions(false) [0];
    }

    @Override
    public int compareTo(Node o) {
        return o.getName().compareTo(this.getName());
    }

}
