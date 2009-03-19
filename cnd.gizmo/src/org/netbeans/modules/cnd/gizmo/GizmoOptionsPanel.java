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

/*
 * GizmoOptionsPanel.java
 *
 * Created on Mar 17, 2009, 11:41:42 AM
 */
package org.netbeans.modules.cnd.gizmo;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightTool;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 *
 * @author mt154047
 */
public class GizmoOptionsPanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    private final ExplorerManager manager = new ExplorerManager();
    private final GizmoProjectOptions options;

    /** Creates new form GizmoOptionsPanel */
    public GizmoOptionsPanel(GizmoProjectOptions options) {
        initComponents();
        this.options = options;
        manager.setRootContext(new AbstractNode(new DLightConfigurationChildren(options.getSelectedTools())));
        dataCollectorName.setModel(new DefaultComboBoxModel(new String[]{"SunStudio", "DTrace"}));//NOI18N
        dataCollectorName.setSelectedItem(options.getDataCollectorName());
        useCollectorsCheckBox.setSelected(options.getDataCollectorEnabled());
        dataCollectorName.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GizmoOptionsPanel.this.options.setDataCollectorName("" + dataCollectorName.getSelectedItem());//NOI18N
                if ("DTrace".equals("" + dataCollectorName.getSelectedItem())) {//NOI18N
                    GizmoOptionsPanel.this.options.setUserInteractionRequiredActionsEnabled(true);
                } else {
                    GizmoOptionsPanel.this.options.setUserInteractionRequiredActionsEnabled(false);
                }
            }
        });

        useCollectorsCheckBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GizmoOptionsPanel.this.options.setDataCollectorEnabled(useCollectorsCheckBox.isSelected());
            }
        });
        //fill in toolsPanel with tools Names
        //if there is no tools in options we should get them from the GizmoConfigurationOptions

        btnRemove.setEnabled(false);
        btnAdd.setEnabled(false);
        btnUp.setEnabled(false);
        btnDown.setEnabled(false);
        btnRemove.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
                //remove from the list
                Node[] nodes  = manager.getSelectedNodes();
                if (nodes == null || nodes.length == 0){
                    return;
                }
                manager.getRootContext().getChildren().remove(nodes);
                //and now

                Node[] allNodes = manager.getRootContext().getChildren().getNodes();
                String[] result = new String[allNodes.length];
                for (int i = 0; i < result.length; i++){
                    result[i] = allNodes[i].getDisplayName();
                }
                GizmoOptionsPanel.this.options.setSelectedTools(result);
                manager.setRootContext(new AbstractNode(new DLightConfigurationChildren(GizmoOptionsPanel.this.options.getSelectedTools())));
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dataCollectorName = new javax.swing.JComboBox();
        useCollectorsCheckBox = new javax.swing.JCheckBox();
        btnAdd = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnUp = new javax.swing.JButton();
        btnDown = new javax.swing.JButton();
        toolsListView = new org.openide.explorer.view.ListView();

        useCollectorsCheckBox.setText(org.openide.util.NbBundle.getMessage(GizmoOptionsPanel.class, "GizmoOptionsPanel.useCollectorsCheckBox.text")); // NOI18N

        btnAdd.setText(org.openide.util.NbBundle.getMessage(GizmoOptionsPanel.class, "GizmoOptionsPanel.btnAdd.text")); // NOI18N

        btnRemove.setText(org.openide.util.NbBundle.getMessage(GizmoOptionsPanel.class, "GizmoOptionsPanel.btnRemove.text")); // NOI18N

        btnUp.setText(org.openide.util.NbBundle.getMessage(GizmoOptionsPanel.class, "GizmoOptionsPanel.btnUp.text")); // NOI18N

        btnDown.setText(org.openide.util.NbBundle.getMessage(GizmoOptionsPanel.class, "GizmoOptionsPanel.btnDown.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(toolsListView, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(btnRemove, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(btnUp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(btnDown, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(btnAdd, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(useCollectorsCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dataCollectorName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(btnAdd)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnRemove)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnUp)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnDown))
                    .add(toolsListView, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dataCollectorName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(useCollectorsCheckBox))
                .addContainerGap(131, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDown;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnUp;
    private javax.swing.JComboBox dataCollectorName;
    private org.openide.explorer.view.ListView toolsListView;
    private javax.swing.JCheckBox useCollectorsCheckBox;
    // End of variables declaration//GEN-END:variables

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    class DLightConfigurationChildren extends Children.Keys {
        private final  DLightConfiguration gizmoConfiguration = DLightConfigurationManager.getInstance().getConfigurationByName("Gizmo");//NOI18N
        private List<String> names;

        public DLightConfigurationChildren(String[] toolNames) {
            //if it is null then use DLightConfiguration
            if (toolNames != null){
                names = new ArrayList<String>();
                names.addAll(Arrays.asList(toolNames));
                return;
            }
            List<DLightTool> list = gizmoConfiguration.getToolsSet();
            names = new ArrayList<String>();
            for (int i = 0, size = list.size(); i < size; i ++){
                names.add(list.get(i).getName());
            }
            setKeys(names);
        }

        @Override
        protected Node[] createNodes(Object arg0) {
            return new Node[]{new DLightToolNode(gizmoConfiguration.getToolByName((String)arg0))};

        }

        @Override
        public boolean remove(Node[] nodes) {
            if (nodes == null || nodes.length == 0){
                return false;
            }
            boolean hasBeenRemoved = false;
            for (int i = 0; i < nodes.length; i++){
                String nodeName = nodes[i].getDisplayName();
                if (names.contains(nodeName)){
                    names.remove(nodeName);
                    hasBeenRemoved = true;
                }
            }
            if (hasBeenRemoved){
                setKeys(names);
            }
            return hasBeenRemoved;
            
        }




        @Override
        protected void addNotify() {
//    List<DTraceletProfile> keys = new ArrayList<DTraceletProfile>(list);
            setKeys(names);
        }
    }

    class DLightToolNode extends AbstractNode{

        private final DLightTool dlightTool ;

        DLightToolNode(DLightTool tool){
            super(Children.LEAF);
            this.dlightTool = tool;
        }

        @Override
        public String getDisplayName() {
            return dlightTool.getName();
        }

        @Override
        public String getHtmlDisplayName() {
            return "<h3>" + getDisplayName() + "</h3>";
        }



        @Override
        public Image getIcon(int type) {
            if (!dlightTool.hasIcon()){
                return super.getIcon(type);
            }
            return ImageUtilities.loadImage(dlightTool.getIconPath());
        }




    }

}
