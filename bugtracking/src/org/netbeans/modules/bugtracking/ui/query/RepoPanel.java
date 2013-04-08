/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.query;

import javax.swing.LayoutStyle;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import static java.lang.Math.max;
import javax.swing.JPanel;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.SwingConstants.WEST;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.util.UIUtils;

/**
 * Top part of the {@code QueryTopComponent} - displays the combo-box for
 * selection of the bug-tracking repository and the list of saved queries.
 *
 * @author Marian Petras
 */
class RepoPanel extends JPanel {

    private static final int MIN_SPACE = 24;

    private final RepoSelectorPanel repoSelectorPanel;

    RepoPanel(JComponent repoSelector,
              JComponent newRepoButton) {
        super(null);
        repoSelectorPanel = new RepoSelectorPanel(repoSelector, newRepoButton);

        LayoutStyle layoutStyle = LayoutStyle.getInstance();
        setBorder(BorderFactory.createEmptyBorder(
                      0, layoutStyle.getContainerGap(this, WEST, getParent()),
                      0, layoutStyle.getContainerGap(this, EAST, getParent())));

        add(repoSelectorPanel);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension repoSelectorPrefSize = repoSelectorPanel.getPreferredSize();

        int width = repoSelectorPrefSize.width;
        int height;

        Insets insets = getInsets();

        height = repoSelectorPrefSize.height
                 + insets.top + insets.bottom;
        width += insets.left + insets.right;

        return new Dimension(width, height);
    }

    @Override
    public void doLayout() {
        int baseline = getBaseline();
        Insets insets = getInsets();

        int x = insets.left;
        Dimension tmpPrefSize = repoSelectorPanel.getPreferredSize();
        repoSelectorPanel.setBounds(x,
                                    baseline - repoSelectorPanel.getBaseline(),
                                    tmpPrefSize.width,
                                    tmpPrefSize.height);
    }

    public int getBaseline() {
        int baseline = repoSelectorPanel.getBaseline();
        baseline += getInsets().top;
        return baseline;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, getPreferredSize().height);
    }

}
