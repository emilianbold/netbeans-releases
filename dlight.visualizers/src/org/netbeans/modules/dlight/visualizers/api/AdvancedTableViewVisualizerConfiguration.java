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
package org.netbeans.modules.dlight.visualizers.api;

import org.netbeans.modules.dlight.util.ui.Renderer;
import org.netbeans.modules.dlight.visualizers.api.impl.OpenFunctionInEditorActionProvider;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.api.visualizer.TableBasedVisualizerConfiguration;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.spi.SourceSupportProvider;
import org.netbeans.modules.dlight.visualizers.api.impl.AdvancedTableViewVisualizerConfigurationAccessor;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mt154047
 */
public final class AdvancedTableViewVisualizerConfiguration implements TableBasedVisualizerConfiguration {

    private final SourceSupportProvider sourceSupportProvider = Lookup.getDefault().lookup(SourceSupportProvider.class);
    private final String nodeColumnName;
    private final String nodeRowColumnID;
    private  String iconColumnID;
    private  String iconPath;    
    private final DataTableMetadata dataTableMetadata;
    private NodeActionsProvider nodeActionProvider;
    private TableModel tableModelImpl;
    private String emptyRuntimeMessage;
    private String emptyAnalyzeMessage;
    private List<String> hiddenColumns;
    private boolean dualPaneMode;
    private Renderer<DataRow> dataRowRenderer;

    static{
        AdvancedTableViewVisualizerConfigurationAccessor.setDefault(new AdvancedTableViewVisualizerConfigurationAccessorImpl());
    }

    /**
     * 
     * @param dataTableMetadata
     * @param nodeColumnName
     * @param nodeRowColumnID 
     */
    public AdvancedTableViewVisualizerConfiguration(DataTableMetadata dataTableMetadata,
        String nodeColumnName, String nodeRowColumnID) {
        this.dataTableMetadata = dataTableMetadata;
        this.nodeColumnName = nodeColumnName;
        this.nodeRowColumnID = nodeRowColumnID;
        this.hiddenColumns = Collections.emptyList();
    }

    public void setEmptyRunningMessage(String emptyRuntimeMessage) {
        this.emptyRuntimeMessage = emptyRuntimeMessage;
    }

    public void setEmptyAnalyzeMessage(String emptyAnalyzeMessage) {
        this.emptyAnalyzeMessage = emptyAnalyzeMessage;
    }

    /**
     * Sets the column name which will be used to get the icon which will be displayed in the NODE (first) column.
     * The name for the icon displayed is formed for the DataRow <code>row</code> as following:
     * <pre>
     *    resourceID + "/" + row.getValueFor(iconColumnID) + ".png";
     * </pre>
     * @param iconColumnID icon column name
     * @param resourceID the resource id, <code>"org/netbeans/modules/dlight/visualizers/resources"</code>
     */
    public void setNodeColumnIcon(String iconColumnID, String resourceID){
        this.iconColumnID = iconColumnID;
        this.iconPath = resourceID;
    }

    private String getIconColumnID() {
        return iconColumnID;
    }

    private String getIconPath(){
        return iconPath;
    }

  
    String getEmptyRunningMessage(){
        return emptyRuntimeMessage;
    }

    String getEmptyAnalyzeMessage(){
        return emptyAnalyzeMessage;
    }

    public final void setDefaultActionProvider() {
        this.nodeActionProvider = new DefaultGoToSourceActionProvider();
    }

    public final void setNodesActionProvider(NodeActionsProvider nodeActionProvider) {
        this.nodeActionProvider = nodeActionProvider;
    }

    public final void setTableModel(TableModel tableModelImpl) {
        this.tableModelImpl = tableModelImpl;
    }

    public final void setHiddenColumnNames(List<String> hiddenColumns) {
        this.hiddenColumns = Collections.unmodifiableList(
                new ArrayList<String>(hiddenColumns));
    }

    public void setDualPaneMode(boolean dualPaneMode) {
        this.dualPaneMode = dualPaneMode;
    }

    public void setDataRowRenderer(Renderer<DataRow> dataRowRenderer) {
        this.dataRowRenderer = dataRowRenderer;
    }

    NodeActionsProvider getNodeActionProvider() {
        return nodeActionProvider;
    }

    String getNodeColumnName() {
        return nodeColumnName;
    }

    String getRowColumnID(){
        return nodeRowColumnID;
    }

    TableModel getTableModel() {
        return tableModelImpl;
    }

    List<String> getHiddenColumnNames() {
        return hiddenColumns;
    }

    public DataModelScheme getSupportedDataScheme() {
        return DataModelSchemeProvider.getInstance().getScheme("model:table");//NOI18N
    }

    public DataTableMetadata getMetadata() {
        return dataTableMetadata;
    }

    public String getID() {
        return VisualizerConfigurationIDsProvider.ADVANCED_TABLE_VISUALIZER;
    }

    private Renderer<DataRow> getDataRowRenderer() {
        return dataRowRenderer;
    }

    private boolean isDualPaneMode() {
        return dualPaneMode;
    }

    private void goToSource(DataRow dataRow) {
        String functionName = dataRow.getStringValue(nodeColumnName);
        OpenFunctionInEditorActionProvider.getInstance().openFunction(functionName);
    }

    private final class DefaultGoToSourceActionProvider implements NodeActionsProvider {

        public void performDefaultAction(Object node) throws UnknownTypeException {
            if (!(node instanceof DefaultMutableTreeNode)) {
                throw new UnknownTypeException(node);
            }
            if (!(((DefaultMutableTreeNode) node).getUserObject() instanceof DataRow)) {
                throw new UnknownTypeException(node);
            }
            DataRow dataRow = (DataRow) ((DefaultMutableTreeNode) node).getUserObject();
            goToSource(dataRow);

        }

        public Action[] getActions(Object node) throws UnknownTypeException {
            if (!(node instanceof DefaultMutableTreeNode)) {
                throw new UnknownTypeException(node);
            }
            if (!(((DefaultMutableTreeNode) node).getUserObject() instanceof DataRow)) {
                throw new UnknownTypeException(node);
            }
            DataRow dataRow = (DataRow) ((DefaultMutableTreeNode) node).getUserObject();
            return new Action[]{(new GoToSourceAction(dataRow))};
        }
    }

    final class GoToSourceAction extends AbstractAction {

        private DataRow row;

        GoToSourceAction(DataRow row) {
            super(NbBundle.getMessage(AdvancedTableViewVisualizerConfiguration.class, "GoToSourceActionName"));//NOI18N
            this.row = row;
        }

        @Override
        public boolean isEnabled() {
            return sourceSupportProvider != null &&
                Lookup.getDefault().lookup(SourceFileInfoProvider.class) != null;
        }

        public void actionPerformed(ActionEvent e) {
            goToSource(row);
        }
    }

    private static  final class AdvancedTableViewVisualizerConfigurationAccessorImpl extends AdvancedTableViewVisualizerConfigurationAccessor{

        @Override
        public NodeActionsProvider getNodeActionProvider(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getNodeActionProvider();
        }

        @Override
        public String getNodeColumnName(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getNodeColumnName();
        }

        @Override
        public String getRowNodeColumnName(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getRowColumnID();
        }

        @Override
        public TableModel getTableModel(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getTableModel();
        }

        @Override
        public String getEmptyRunningMessage(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getEmptyRunningMessage();
        }

        @Override
        public String getEmptyAnalyzeMessage(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getEmptyAnalyzeMessage();
        }

        @Override
        public List<String> getHiddenColumnNames(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getHiddenColumnNames();
        }

        @Override
        public boolean isDualPaneMode(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.isDualPaneMode();
        }

        @Override
        public Renderer<DataRow> getDetailsRenderer(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getDataRowRenderer();
        }

        @Override
        public String getIconColumnID(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getIconColumnID();
        }

        @Override
        public String getIconPath(AdvancedTableViewVisualizerConfiguration configuration) {
            return configuration.getIconPath();
        }

    }
}
