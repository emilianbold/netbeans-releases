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

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.bugtracking.spi.Query;
import org.openide.awt.Mnemonics;
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

    final int defaultFontSize;

    private int baseline;
    private boolean baselineValid;

    private Dimension prefSize;
    private Dimension maxSize;
    private Insets cachedInsets;

    RepoPanel(JComponent repoSelector,
              JComponent newRepoButton) {
        super(null);
        JLabel title             = new JLabel();
        repoSelectorPanel = new RepoSelectorPanel(repoSelector, newRepoButton);
        queriesPanel      = new QueriesPanel();
        queriesPanel.setVisible(false);

        Font titleFont = title.getFont();
        defaultFontSize = titleFont.getSize();
        title.setFont(titleFont.deriveFont(1.7f * defaultFontSize));

        queriesPanel.setBackground(new Color(224, 224, 224));

        Mnemonics.setLocalizedText(
                title,
                getText("QueryTopComponent.findIssuesLabel.text"));     //NOI18N

        LayoutStyle layoutStyle = LayoutStyle.getSharedInstance();
        setBorder(BorderFactory.createEmptyBorder(
                      0, layoutStyle.getContainerGap(this, WEST, getParent()),
                      0, layoutStyle.getContainerGap(this, EAST, getParent())));

        add(repoSelectorPanel);
        add(queriesPanel);
    }

    @Override
    public void invalidate() {
        invalidatePrefSize();
        super.invalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        if (prefSize == null) {
            Dimension repoSelectorPrefSize = repoSelectorPanel.getPreferredSize();

            int width = repoSelectorPrefSize.width;
            int height;

            Insets insets = getCachedInsets();

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

            prefSize = new Dimension(width, height);
        }
        return prefSize;
    }

    @Override
    public void doLayout() {
        validateBaseline();

        int x;
        Dimension tmpPrefSize;
        int availableWidth = getAvailableWidth()
                             - getCachedInsets().left
                             - getCachedInsets().right;

        x = getCachedInsets().left;
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
        validateBaseline();
        return baseline;
    }

    private void validateBaseline() {
        if (!baselineValid) {
            baseline = queriesPanel.isVisible()
                       ? max(repoSelectorPanel.getBaseline(),
                             queriesPanel.getBaseline())
                       : repoSelectorPanel.getBaseline();
            baseline += getCachedInsets().top;
            baselineValid = true;
        }
    }

    private Insets getCachedInsets() {
        if (cachedInsets == null) {
            cachedInsets = super.getInsets();
        }
        return cachedInsets;
    }

    int getDefaultFontSize() {
        return defaultFontSize;
    }

    @Override
    public Dimension getMaximumSize() {
        if (maxSize == null) {
            maxSize = new Dimension(Short.MAX_VALUE, getPreferredSize().height);
        }
        return maxSize;
    }

    void setQueries(Query[] queries) {
        assert EventQueue.isDispatchThread();

        if ((queries != null) && (queries.length == 0)) {
            queries = null;
        }

        boolean hadQueries = queriesPanel.hasQueries();
        boolean queriesChanged = hadQueries || (queries != null);

        if (queriesChanged) {
            invalidateBaseline();
            invalidatePrefSize();
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

    private static String getText(String key) {
        return NbBundle.getMessage(RepoPanel.class, key);
    }

    @Override
    protected void notifyChildrenOfVisibleWidth() {
        if (queriesPanel.hasQueries()) {
            int availableWidth = getAvailableWidth();
            int usedWidth = getCachedInsets().left
                            + repoSelectorPanel.getPreferredSize().width;
            queriesPanel.setAvailableWidth(availableWidth - usedWidth - MIN_SPACE);
        }
    }

    @Override
    public void setUI(PanelUI ui) {
        super.setUI(ui);
        invalidateUiDependentValues();
    }

    @Override
    protected void setUI(ComponentUI newUI) {
        super.setUI(newUI);
        invalidateUiDependentValues();
    }

    private void invalidateUiDependentValues() {
        cachedInsets = null;
        invalidateBaseline();
        invalidatePrefSize();
    }

    private void invalidateBaseline() {
        baselineValid = false;
    }

    private void invalidatePrefSize() {
        prefSize = null;
        invalidateMaxSize();
    }

    private void invalidateMaxSize() {
        maxSize = null;
    }

}
