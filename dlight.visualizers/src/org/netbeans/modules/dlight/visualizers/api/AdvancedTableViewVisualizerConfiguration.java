/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.visualizers.api;

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
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.spi.SourceFileInfoProvider;
import org.netbeans.modules.dlight.visualizers.SourceSupportProvider;
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
public final class AdvancedTableViewVisualizerConfiguration implements VisualizerConfiguration {

    private final SourceSupportProvider sourceSupportProvider = Lookup.getDefault().lookup(SourceSupportProvider.class);
    private final String nodeColumnName;
    private final String nodeRowColumnID;
    private final DataTableMetadata dataTableMetadata;
    private NodeActionsProvider nodeActionProvider;
    private TableModel tableModelImpl;
    private String emptyRuntimeMessage;
    private String emptyAnalyzeMessage;
    private List<String> hiddenColumns;

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

    }
}
