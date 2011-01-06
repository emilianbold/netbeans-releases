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
package org.netbeans.modules.dlight.visualizers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.spi.support.TableDataProvider;
import org.netbeans.modules.dlight.util.ui.DualPaneSupport;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.DataRowNode;
import org.netbeans.modules.dlight.visualizers.api.VisualizerToolbarComponentsProvider;
import org.netbeans.modules.dlight.visualizers.api.impl.AdvancedTableViewVisualizerConfigurationAccessor;
import org.netbeans.modules.dlight.visualizers.ui.AdvancedDataRowTableOutline;
import org.netbeans.modules.dlight.visualizers.ui.AdvancedTableDataRowNodeChildren;
import org.netbeans.modules.dlight.visualizers.ui.TableViewNodeChildren;
import org.openide.nodes.Node;

/**
 *
 * @author ak119685
 */
public final class AdvancedTableViewVisualizer extends TableViewVisualizer<AdvancedTableViewVisualizerConfiguration, DataRow> {

    private final TableDataProvider provider;
    private final JComponent dualViewPane;
    private final String nodeColumnName;
    private final String emptyAnalyzeMessage;
    private final String emptyRunningMessage;
    private final String nodeColumnUName;
 
    public AdvancedTableViewVisualizer(final TableDataProvider provider, final AdvancedTableViewVisualizerConfiguration configuration) {
        super(provider, configuration);
        
        this.provider = provider;

        final AdvancedTableViewVisualizerConfigurationAccessor accessor = AdvancedTableViewVisualizerConfigurationAccessor.getDefault();

        this.nodeColumnName = accessor.getRowNodeColumnName(configuration);
        this.nodeColumnUName = accessor.getNodeColumnName(configuration);
        emptyAnalyzeMessage = accessor.getEmptyAnalyzeMessage(configuration);
        emptyRunningMessage = accessor.getEmptyRunningMessage(configuration);
        boolean dualPaneMode = accessor.isDualPaneMode(configuration);
        dualViewPane = dualPaneMode ? createDualViewPane() : null;
    }

    @Override
    public JComponent getComponent() {
        return dualViewPane == null ? this : dualViewPane;
    }

    @Override
    protected TableViewNodeChildren<DataRow> initChildren() {
        return new AdvancedTableDataRowNodeChildren(getVisualizerConfiguration(), getLookup());
    }

    @Override
    protected Component initTableView() {
        return new AdvancedDataRowTableOutline(nodeColumnUName, getVisualizerConfiguration());
    }

    @Override
    protected List<DataRow> getUpdatedData() {
        return provider.queryData(getMetadata());
    }

    @Override
    protected boolean matchesFilter(String filter, DataRow data) {
        return data.getData(nodeColumnName).toString().contains(filter);
    }

    @Override
    protected String getEmptyAnalyzeMessage() {
        return emptyAnalyzeMessage;
    }

    @Override
    protected String getEmptyRunningMessage() {
        return emptyRunningMessage;
    }

    private JComponent createDualViewPane() {
        AdvancedTableViewVisualizerConfigurationAccessor accessor =
                AdvancedTableViewVisualizerConfigurationAccessor.getDefault();

        DualView result = new DualView(this);
        result.add(DualPaneSupport.forExplorerManager(
                AdvancedTableViewVisualizer.this, super.getExplorerManager(),
                accessor.getDetailsRenderer(getVisualizerConfiguration()),
                new DualPaneSupport.DataAdapter<Node, DataRow>() {

                    @Override
                    public DataRow convert(Node obj) {
                        if (obj instanceof DataRowNode) {
                            return ((DataRowNode) obj).getDataRow();
                        } else {
                            return null;
                        }
                    }
                }), BorderLayout.CENTER);
        return result;
    }

    private final static class DualView extends JPanel implements VisualizerToolbarComponentsProvider {

        private final AdvancedTableViewVisualizer orig;

        public DualView(AdvancedTableViewVisualizer orig) {
            this.orig = orig;
            setLayout(new BorderLayout());
            setFocusable(false);
        }

        @Override
        public List<Component> getToolbarComponents() {
            return orig.getToolbarComponents();
        }

        @Override
        public void requestFocus() {
            orig.requestFocus();
        }
    }
}
