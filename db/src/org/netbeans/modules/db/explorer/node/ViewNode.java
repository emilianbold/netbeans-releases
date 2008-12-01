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

package org.netbeans.modules.db.explorer.node;

import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.View;

/**
 *
 * @author rob
 */
public class ViewNode extends BaseNode {
    private static final String ICONBASE = "org/netbeans/modules/db/resources/view.gif";
    private static final String FOLDER = "View"; //NOI18N

    /**
     * Create an instance of ViewNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the ViewNode instance
     */
    public static ViewNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        ViewNode node = new ViewNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private DatabaseConnection connection;
    private Metadata metaData;
    private MetadataElementHandle<View> viewHandle;

    private ViewNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
    }

    protected void initialize() {
        // get the connection from the lookup
        connection = getLookup().lookup(DatabaseConnection.class);
        metaData = getLookup().lookup(Metadata.class);
        viewHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    @Override
    public String getName() {
        View view = viewHandle.resolve(metaData);
        if (view == null) {
            return "";
        }

        return view.getName();
    }

    @Override
    public String getDisplayName() {
        View view = viewHandle.resolve(metaData);
        if (view == null) {
            return "";
        }

        return view.getName();
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }
}
