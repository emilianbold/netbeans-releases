/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.tasks.ui.dashboard;

import org.netbeans.modules.tasks.ui.LinkButton;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.tasks.ui.model.Category;
import org.netbeans.modules.tasks.ui.actions.CloseCategoryNodeAction;
import org.netbeans.modules.tasks.ui.actions.DummyAction;
import org.netbeans.modules.tasks.ui.treelist.TreeLabel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class CategoryNode extends AbstractCategoryNode {

    private static ImageIcon iconClose = null;
    private static ImageIcon iconCloseOver = null;
    private JPanel panel;
    private TreeLabel lblName;
    private LinkButton btnClose;
    private final Object LOCK = new Object();
    private LinkButton btnRefresh;
    private TreeLabel counts;
    private CloseCategoryNodeAction categoryAction;

    public CategoryNode(Category category) {
        super(true, category);
        if (iconClose == null) {
            iconClose = ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/close.png", true); //NOI18N
            iconCloseOver = ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/close_over.png", true); //NOI18N
        }
    }

    @Override
    protected List<TreeListNode> createChildren() {
        List<TaskNode> children = getFilteredTaskNodes();
        Collections.sort(children);
        return new ArrayList<TreeListNode>(children);
    }

    @Override
    protected void refresh() {
        refreshChildren();
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        synchronized (LOCK) {
            if (panel == null) {
                panel = new JPanel(new GridBagLayout());
                panel.setOpaque(false);
                final JLabel iconLabel = new JLabel(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/category.png", true)); //NOI18N
                panel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

                lblName = new TreeLabel(getCategory().getName());
                panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

                counts = new TreeLabel(getCountText());
                panel.add(counts, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

                panel.add(new JLabel(), new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

                btnClose = new LinkButton(iconClose, new CloseCategoryNodeAction(this));
                btnClose.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_Close")); //NOI18N
                btnClose.setRolloverEnabled(true);
                btnClose.setRolloverIcon(iconCloseOver); // NOI18N
                panel.add(btnClose, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));

                btnRefresh = new LinkButton(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/refresh.png", true), new DummyAction()); //NOI18N
                btnRefresh.setToolTipText(NbBundle.getMessage(CategoryNode.class, "LBL_Refresh")); //NOI18N
                panel.add(btnRefresh, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
            }
            lblName.setText(getCategory().getName());
            lblName.setForeground(foreground);
            if (DashboardViewer.getInstance().containsActiveTask(this)) {
                lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
            } else {
                lblName.setFont(lblName.getFont().deriveFont(Font.PLAIN));
            }
            counts.setText(getCountText());
            counts.setForeground(foreground);
            btnClose.setForeground(foreground, isSelected);
            return panel;
        }
    }

    @Override
    protected Action getCategoryAction() {
        if (categoryAction == null) {
            categoryAction = new CloseCategoryNodeAction(this);
        }
        return categoryAction;
    }

    private String getCountText() {
        String bundleName = DashboardViewer.getInstance().expandNodes() ? "LBL_Matches" : "LBL_Total"; //NOI18N
        return "(" + getTotalCount() + " " + NbBundle.getMessage(CategoryNode.class, bundleName)
                + " | " + getModifiedCount() + " " + NbBundle.getMessage(CategoryNode.class, "LBL_Changed") + ")"; //NOI18N
    }
}