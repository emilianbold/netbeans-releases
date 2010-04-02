/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.spi.Query;
import org.openide.util.NbBundle;
import static java.lang.Math.max;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.SwingConstants.WEST;

/**
 * Top part of the {@code QueryTopComponent} - displays the combo-box for
 * selection of the bug-tracking repository and the list of saved queries.
 *
 * @author Marian Petras
 */
class RepoPanel extends ViewportWidthAwarePanel {

    private static final int MIN_SPACE = 24;

    private final RepoSelectorPanel repoSelectorPanel;
    private final QueriesPanel queriesPanel;

    RepoPanel(JComponent repoSelector,
              JComponent newRepoButton) {
        super(null);
        repoSelectorPanel = new RepoSelectorPanel(repoSelector, newRepoButton);
        queriesPanel      = new QueriesPanel();
        queriesPanel.setVisible(false);

        queriesPanel.setBackground(new Color(224, 224, 224));

        LayoutStyle layoutStyle = LayoutStyle.getSharedInstance();
        setBorder(BorderFactory.createEmptyBorder(
                      0, layoutStyle.getContainerGap(this, WEST, getParent()),
                      0, layoutStyle.getContainerGap(this, EAST, getParent())));

        add(repoSelectorPanel);
        add(queriesPanel);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension repoSelectorPrefSize = repoSelectorPanel.getPreferredSize();

        int width = repoSelectorPrefSize.width;
        int height;

        Insets insets = getInsets();

        if (!queriesPanel.isVisible()) {
            height = repoSelectorPrefSize.height
                     + insets.top + insets.bottom;
        } else {
            Dimension queriesPanelPrefSize = queriesPanel.getPreferredSize();

            int aboveBaseline = getBaseline();

            int belowBaseline1 = repoSelectorPrefSize.height
                                 - repoSelectorPanel.getBaseline();
            int belowBaseline2 = queriesPanelPrefSize.height
                                 - queriesPanel.getBaseline();
            int belowBaseline = max(belowBaseline1, belowBaseline2);

            width += MIN_SPACE + queriesPanelPrefSize.width;
            height = aboveBaseline + belowBaseline
                     + insets.bottom;//top inset is included in the baseline
        }

        width += insets.left + insets.right;

        return new Dimension(width, height);
    }

    @Override
    public void doLayout() {
        int baseline = getBaseline();
        Insets insets = getInsets();

        int x;
        Dimension tmpPrefSize;
        int availableWidth = getAvailableWidth()
                             - insets.left
                             - insets.right;

        x = insets.left;
        tmpPrefSize = repoSelectorPanel.getPreferredSize();
        repoSelectorPanel.setBounds(x,
                                    baseline - repoSelectorPanel.getBaseline(),
                                    tmpPrefSize.width,
                                    tmpPrefSize.height);

        if (queriesPanel.isVisible()) {
            availableWidth -= tmpPrefSize.width;
            x              += tmpPrefSize.width;

            tmpPrefSize = queriesPanel.getPreferredSize();
            int space = !queriesPanel.isWrapped()
                        ? max(MIN_SPACE, availableWidth - tmpPrefSize.width)
                        : MIN_SPACE;
            x += space;
            queriesPanel.setBounds(x,
                                   baseline - queriesPanel.getBaseline(),
                                   tmpPrefSize.width,
                                   tmpPrefSize.height);
        }
    }

    public int getBaseline() {
        int baseline = queriesPanel.isVisible()
                   ? max(repoSelectorPanel.getBaseline(),
                         queriesPanel.getBaseline())
                   : repoSelectorPanel.getBaseline();
        baseline += getInsets().top;
        return baseline;
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, getPreferredSize().height);
    }

    void setQueries(Query[] queries) {
        assert EventQueue.isDispatchThread();

        if ((queries != null) && (queries.length == 0)) {
            queries = null;
        }

        boolean hadQueries = queriesPanel.hasQueries();
        boolean queriesChanged = hadQueries || (queries != null);

        if (queriesChanged) {
            if (queries == null) {
                queriesPanel.setVisible(false);
            }
            queriesPanel.setQueries(queries);
            if (!hadQueries && (queries != null)) {
                notifyChildrenOfVisibleWidth();
                queriesPanel.setVisible(true);
            }
        }
    }

    @Override
    protected void notifyChildrenOfVisibleWidth() {
        if (queriesPanel.hasQueries()) {
            int availableWidth = getAvailableWidth();
            int usedWidth = getInsets().left
                            + repoSelectorPanel.getPreferredSize().width;
            queriesPanel.setAvailableWidth(availableWidth - usedWidth - MIN_SPACE);
        }
    }

}
