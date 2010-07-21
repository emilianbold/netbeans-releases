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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.OverlayLayout;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormLoaderSettings;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;
import org.netbeans.modules.form.VisualReplicator;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Grid designer.
 *
 * @author Jan Stola
 */
public class GridDesigner extends JPanel implements Customizer {
    /** Color of the selection. */
    public static final Color SELECTION_COLOR = FormLoaderSettings.getInstance().getSelectionBorderColor();
    public static final Image RESIZE_HANDLE = ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/resize_handle.png", false).getImage(); // NOI18N
    private JPanel innerPane;
    private GlassPane glassPane;
    private VisualReplicator replicator;
    private PropertySheet sheet;
    private JSplitPane splitPane;
    private GridCustomizer customizer;

    public GridDesigner() {
        setLayout(new BorderLayout());
        splitPane = new JSplitPane();
        innerPane = new JPanel() {
            @Override
            public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        innerPane.setLayout(new OverlayLayout(innerPane));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportView(innerPane);
        scrollPane.setPreferredSize(new Dimension(500,500));
        splitPane.setRightComponent(scrollPane);
        glassPane = new GlassPane(this);
        glassPane.setOpaque(false);
        add(splitPane);
    }

    private void setDesignedContainer(RADVisualContainer metaContainer) {
        FormModel formModel = metaContainer.getFormModel();
        replicator = new VisualReplicator(true, FormUtils.getViewConverters(), FormEditor.getBindingSupport(formModel));
        replicator.setTopMetaComponent(metaContainer);
        Object bean = (Container)replicator.createClone();
        Container container = metaContainer.getContainerDelegate(bean);
        innerPane.removeAll();
        // Estimate of the size of the header
        Dimension headerDim = new JLabel("99").getPreferredSize(); // NOI18N
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        GroupLayout layout = new GroupLayout(mainPanel);
        layout.setHonorsVisibility(false);
        GroupLayout.Group hGroup = layout.createSequentialGroup()
                .addGap(3*GlassPane.HEADER_GAP+headerDim.width)
                .addComponent(container, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setHorizontalGroup(hGroup);
        GroupLayout.Group vGroup = layout.createSequentialGroup()
                .addGap(2*GlassPane.HEADER_GAP+headerDim.height)
                .addComponent(container, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setVerticalGroup(vGroup);
        mainPanel.setLayout(layout);
        glassPane.setPanes(innerPane, container);
        configureGridInfo(replicator);
        initLeftColumn();
        innerPane.add(glassPane);
        innerPane.add(mainPanel);
    }

    private void configureGridInfo(VisualReplicator replicator) {
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        Object bean = replicator.getClonedComponent(metacont);
        Container container = metacont.getContainerDelegate(bean);
        LayoutManager layout = container.getLayout();
        GridManager gridManager = null;
        if (layout instanceof GridBagLayout) {
            gridManager = new GridBagManager(replicator);
        }
        glassPane.setGridManager(gridManager);
        customizer = gridManager.getCustomizer(glassPane);
    }
    
    private void initLeftColumn() {
        sheet = new PropertySheet();
        sheet.setPreferredSize(new Dimension(300, 500));
        JPanel leftPanel;
        if (customizer == null) {
            leftPanel = sheet;
        } else {
            leftPanel = new JPanel();
            leftPanel.setLayout(new BorderLayout());
            leftPanel.add(sheet);
            leftPanel.add(customizer.getComponent(), BorderLayout.PAGE_START);
        }
        splitPane.setLeftComponent(leftPanel);
    }

    @Override
    public void setObject(Object bean) {
        setDesignedContainer((RADVisualContainer)bean);
    }

    private RADVisualComponent selection;
    public void setSelection(Component selectedComp) {
        selection = null;
        RADVisualContainer metacont = (RADVisualContainer)replicator.getTopMetaComponent();
        for (RADVisualComponent metacomp : metacont.getSubComponents()) {
            Component comp = (Component)replicator.getClonedComponent(metacomp);
            if (comp == selectedComp) {
                selection = metacomp;
                break;
            }
        }
        updatePropertySheet();
        updateCustomizer();
    }

    private PropertyChangeListener selectedNodeListener;
    private PropertyChangeListener getSelectedNodeListener() {
        if (selectedNodeListener == null) {
            selectedNodeListener = createSelectedNodeListener();
        }
        return selectedNodeListener;
    }

    private PropertyChangeListener createSelectedNodeListener() {
        return new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!glassPane.isUserActionInProgress()) {
                    glassPane.updateLayout();
                    updateCustomizer();
                }
            }
        };
    }

    private Node selectedNode;
    private void updatePropertySheet() {
        if (selectedNode != null) {
            selectedNode.removePropertyChangeListener(getSelectedNodeListener());
        }
        Node[] nodes;
        if (selection == null) {
            nodes = new Node[0];
            selectedNode = null;
        } else {
            selectedNode = new LayoutConstraintsNode(selection.getNodeReference());
            selectedNode.addPropertyChangeListener(getSelectedNodeListener());
            nodes = new Node[] {selectedNode};
        }
        sheet.setNodes(nodes);
    }

    void updateCustomizer() {
        if (customizer != null) {
            DesignerContext context = glassPane.currentContext();
            customizer.setContext(context);
        }
    }

    static class LayoutConstraintsNode extends FilterNode {

        LayoutConstraintsNode(Node original) {
            super(original);
        }

        @Override
        public Node.PropertySet[] getPropertySets() {
            for (Node.PropertySet pSet : super.getPropertySets()) {
                String name = pSet.getName();
                if ("layout".equals(name)) { // NOI18N
                    final Node.PropertySet set = pSet;
                    String displayName = NbBundle.getMessage(GridDesigner.class, "GridDesigner.layoutConstraints"); // NOI18N
                    return new Node.PropertySet[] {new PropertySet(set.getName(), displayName, set.getShortDescription()) {
                        @Override
                        public Property<?>[] getProperties() {
                            return set.getProperties();
                        }
                    }};
                }
            }
            return new Node.PropertySet[0];
        }
    }

}
