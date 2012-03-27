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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.tasks.ui.actions.OpenRepositoryNodeAction;
import org.netbeans.modules.tasks.ui.treelist.TreeLabel;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author jpeska
 */
public class ClosedRepositoryNode extends AbstractRepositoryNode {

    private final Object LOCK = new Object();
    private JPanel panel;
    private TreeLabel lblName;
    private LinkButton btnOpen;
    private OpenRepositoryNodeAction repositoryAction;
    private static ImageIcon iconOpen = null;
    private static ImageIcon iconOpenOver = null;

    public ClosedRepositoryNode(Repository repository) {
        super(false, repository);
        loadIcons();
    }

    public ClosedRepositoryNode(Repository repository, boolean loaded) {
        super(false, repository, loaded);
        loadIcons();
    }

    @Override
    protected List<TreeListNode> createChildren() {
        return Collections.emptyList();
    }

    @Override
    protected JComponent getComponent(Color foreground, Color background, boolean isSelected, boolean hasFocus, int rowWidth) {
        synchronized (LOCK) {
            if (panel == null) {
                panel = new JPanel(new GridBagLayout());
                panel.setOpaque(false);
                final JLabel iconLabel = new JLabel(ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/remote_repo.png", true)); //NOI18N
                panel.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
                lblName = new TreeLabel(getRepository().getDisplayName());
                panel.add(lblName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
                panel.add(new JLabel(), new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
                btnOpen = new LinkButton(iconOpen, getRepositoryAction());
                btnOpen.setToolTipText(NbBundle.getMessage(ClosedCategoryNode.class, "LBL_Open")); //NOI18N
                btnOpen.setRolloverEnabled(true);
                btnOpen.setRolloverIcon(iconOpenOver); // NOI18N
                panel.add(btnOpen, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 3, 0, 0), 0, 0));
                lblName.setForeground(foreground);
            }
            lblName.setForeground(foreground);
            btnOpen.setForeground(foreground, isSelected);
            return panel;
        }
    }

    @Override
    protected Action getRepositoryAction() {
        if (repositoryAction == null) {
            repositoryAction = new OpenRepositoryNodeAction(this);
        }
        return repositoryAction;
    }

    private void loadIcons() {
        if (iconOpen == null) {
            iconOpen = ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/open.png", true); //NOI18N
            iconOpenOver = ImageUtilities.loadImageIcon("org/netbeans/modules/tasks/ui/resources/open_over.png", true); //NOI18N
        }
    }

    @Override
    void updateContent() {
    }
}
