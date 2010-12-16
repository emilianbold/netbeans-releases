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
package org.netbeans.modules.dlight.visualizers.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.DataRowNode;
import org.netbeans.modules.dlight.visualizers.api.impl.AdvancedTableViewVisualizerConfigurationAccessor;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author ak119685
 */
public final class AdvancedTableDataRowNodeFactory extends AbstractDataRowNodeFactory {

    private final static Image NULL_ICON = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private final DataTableMetadata metadata;
    private final NodeActionsProvider nodeActionsProvider;
    private final String nodeColumnName;
    private final String iconColumnID;
    private final String resourceID;
    private final Lookup lookup;

    public AdvancedTableDataRowNodeFactory(AdvancedTableViewVisualizerConfiguration configuration, Lookup lookup) {
        this.lookup = lookup;
        metadata = configuration.getMetadata();
        AdvancedTableViewVisualizerConfigurationAccessor accessor = AdvancedTableViewVisualizerConfigurationAccessor.getDefault();
        nodeActionsProvider = accessor.getNodeActionProvider(configuration);
        nodeColumnName = accessor.getRowNodeColumnName(configuration);
        iconColumnID = accessor.getIconColumnID(configuration);
        resourceID = iconColumnID == null ? null : accessor.getIconPath(configuration);
    }

    @Override
    public DataRowNode createNode(DataRow row) {
        return new AdvancedDataRowNode(row);
    }

    private class AdvancedDataRowNode extends DataRowNode {

        private String displayName;
        private Action defaultAction;

        AdvancedDataRowNode(final DataRow row) {
            super(row, lookup);
        }

        @Override
        protected Sheet createSheet() {
            Sheet result = new Sheet();
            Sheet.Set set = new Sheet.Set();

            for (String columnName : getDataRow().getColumnNames()) {
                final Column c = metadata.getColumnByName(columnName);

                @SuppressWarnings("unchecked")
                PropertySupport.ReadOnly property = new PropertySupport.ReadOnly(
                        columnName, c.getColumnClass(),
                        c.getColumnUName(), c.getColumnLongUName()) {

                    @Override
                    public Object getValue() throws IllegalAccessException, InvocationTargetException {
                        return getDataRow().getData(c.getColumnName());
                    }
                };
                set.put(property);
            }

            result.put(set);
            return result;
        }

        @Override
        public synchronized Action getPreferredAction() {
            if (defaultAction == null) {
                defaultAction = new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (nodeActionsProvider != null) {
                            try {
                                nodeActionsProvider.performDefaultAction(AdvancedDataRowNode.this);
                            } catch (UnknownTypeException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }

                    @Override
                    public boolean isEnabled() {
                        return nodeActionsProvider != null;
                    }
                };
            }

            return defaultAction;
        }

        @Override
        public Action[] getActions(boolean context) {
            if (nodeActionsProvider == null) {
                return new Action[0];
            }

            try {
                return nodeActionsProvider.getActions(this);
            } catch (UnknownTypeException ex) {
                Exceptions.printStackTrace(ex);
            }

            return null;
        }

        @Override
        public Image getIcon(int type) {
            if (iconColumnID == null) {
                return NULL_ICON;
            }

            return ImageUtilities.loadImage(resourceID + "/" + getDataRow().getStringValue(iconColumnID) + ".png"); // NOI18N
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public synchronized String getDisplayName() {
            if (displayName == null) {
                displayName = getDataRow().getData(nodeColumnName) + "";
            }

            return displayName;
        }
    }
}
