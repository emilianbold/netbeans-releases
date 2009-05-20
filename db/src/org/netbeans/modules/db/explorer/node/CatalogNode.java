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

package org.netbeans.modules.db.explorer.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.api.db.explorer.node.ChildNodeFactory;
import org.netbeans.api.db.explorer.node.NodeProvider;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Catalog;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataElementHandle;
import org.netbeans.modules.db.metadata.model.api.MetadataModel;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Rob Englander
 */
public class CatalogNode extends BaseNode {
    private static final String ICONBASE = "org/netbeans/modules/db/resources/database.gif";
    private static final String FOLDER = "Catalog"; //NOI18N

    /**
     * Create an instance of CatalogNode.
     *
     * @param dataLookup the lookup to use when creating node providers
     * @return the CatalogNode instance
     */
    public static CatalogNode create(NodeDataLookup dataLookup, NodeProvider provider) {
        CatalogNode node = new CatalogNode(dataLookup, provider);
        node.setup();
        return node;
    }

    private String name = ""; // NOI18N
    private String htmlName = null;
    private final DatabaseConnection connection;
    private final MetadataElementHandle<Catalog> catalogHandle;

    @SuppressWarnings("unchecked")
    private CatalogNode(NodeDataLookup lookup, NodeProvider provider) {
        super(new ChildNodeFactory(lookup), lookup, FOLDER, provider);
        connection = getLookup().lookup(DatabaseConnection.class);
        catalogHandle = getLookup().lookup(MetadataElementHandle.class);
    }

    protected void initialize() {
        setupNames();

        connection.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(DatabaseConnection.PROP_DEFCATALOG)) {
                        updateProperties();
                    }
                }
            }
        );
    }

    private void setupNames() {
        MetadataModel metaDataModel = connection.getMetadataModel();
        boolean connected = !connection.getConnector().isDisconnected();
        if (connected && metaDataModel != null) {
            try {
                metaDataModel.runReadAction(
                    new Action<Metadata>() {
                        public void run(Metadata metaData) {
                            Catalog catalog = catalogHandle.resolve(metaData);
                            renderNames(catalog);
                        }
                    }
                );
            } catch (MetadataModelException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    @Override
    protected void updateProperties() {
        setupNames();
        super.updateProperties();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlName;
    }

    private void renderNames(Catalog catalog) {
        if (catalog == null) {
            name = "";
        } else {
            name = catalog.getName();
            if (name == null) {
                name = "Default"; // NOI18N
            }
        }

        if (catalog != null) {
            boolean isDefault = false;
            String def = connection.getDefaultCatalog();
            if (def != null) {
                isDefault = def.equals(name);
            } else {
                isDefault = catalog.isDefault();
            }

            if (isDefault) {
                htmlName = "<b>" + name + "</b>"; // NOI18N
            } else {
                htmlName = null;
            }
        }
    }

    @Override
    public String getIconBase() {
        return ICONBASE;
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage (CatalogNode.class, "ND_Catalog"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CatalogNode.class);
    }
}
