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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.query;

import javax.swing.LayoutStyle;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;
import org.netbeans.modules.bugtracking.spi.Query;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;

/**
 * Panel containing &quot;My Queries: My Issues | High Priority | ... &quot;.
 * It wraps automatically if it does not fit to the <em>visible part</em>
 * if the scrollpane's view.
 *
 * @author Marian Petras
 */
public class QueriesPanel extends ViewportWidthAwarePanel {

    private static final JLabel fakeLabel = new JLabel("fake");         //NOI18N

    private final JLabel queriesLabel;
    private final QueryLinksPanel queryLinksPanel;

    private int baseline;
    private boolean baselineValid;

    private int labelBaseline;
    private boolean labelBaselineValid;

    private int linksPanelOffset;
    private boolean linksPanelOffsetValid;

    private Dimension prefSize;
    private Insets cachedInsets;

    public QueriesPanel() {
        super(null);
        queriesLabel = new JLabel();
        queryLinksPanel = new QueryLinksPanel();
        
        queriesLabel.setLabelFor(queryLinksPanel);

        Mnemonics.setLocalizedText(
               queriesLabel,
               NbBundle.getMessage(getClass(),
                                   "QueryTopComponent.jLabel1.text_1"));//NOI18N

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(queriesLabel);
        add(queryLinksPanel);
    }

    void setQueries(Query[] queries) {
        invalidatePrefSize();
        queryLinksPanel.setQueries(queries);
    }

    boolean hasQueries() {
        return !queryLinksPanel.isEmpty();
    }

    boolean isWrapped() {
        return queryLinksPanel.isWrapped();
    }

    @Override
    protected void visibleWidthChanged(int newWidth) {
        invalidatePrefSize();
        super.visibleWidthChanged(newWidth);
    }

    @Override
    protected void notifyChildrenOfVisibleWidth() {
        queryLinksPanel.setAvailableWidth(
                getAvailableWidth() - getLinksPanelOffset()
                - getCachedInsets().left
                - getCachedInsets().right);
    }

    @Override
    public Dimension getPreferredSize() {
        if (prefSize == null) {
            Dimension labelPrefSize = queriesLabel.getPreferredSize();
            Dimension panelPrefSize = queryLinksPanel.getPreferredSize();

            int aboveBaseline = getBaseline();

            int belowBaseline1 = labelPrefSize.height - getLabelBaseline();
            int belowBaseline2 = panelPrefSize.height - queryLinksPanel.getBaseline();
            int belowBaseline = Math.max(belowBaseline1, belowBaseline2);

            int width = getLinksPanelOffset() + panelPrefSize.width;
            int height = aboveBaseline + belowBaseline;

            Insets insets = getCachedInsets();
            width += insets.left + insets.right;
            height += insets.bottom;    //top inset is included in the baseline

            prefSize = new Dimension(width, height);
        }
        return prefSize;
    }

    @Override
    public void invalidate() {
        invalidatePrefSize();
        super.invalidate();
    }

    @Override
    public void doLayout() {
        Dimension tmpPrefSize;

        if (queryLinksPanel.isEmpty()) {
            Insets insets = getCachedInsets();
            tmpPrefSize = queriesLabel.getPreferredSize();
            queriesLabel.setBounds(insets.left,
                                   insets.top,
                                   tmpPrefSize.width,
                                   tmpPrefSize.height);
            return;
        }

        validateBaseline();

        tmpPrefSize = queriesLabel.getPreferredSize();
        queriesLabel.setBounds(getCachedInsets().left,
                               baseline - getLabelBaseline(),
                               tmpPrefSize.width,
                               tmpPrefSize.height);

        tmpPrefSize = queryLinksPanel.getPreferredSize();
        queryLinksPanel.setBounds(getCachedInsets().left + getLinksPanelOffset(),
                                  baseline - queryLinksPanel.getBaseline(),
                                  tmpPrefSize.width,
                                  tmpPrefSize.height);
    }

    private int getLinksPanelOffset() {
        if (!linksPanelOffsetValid) {
            linksPanelOffset = queriesLabel.getPreferredSize().width
                               + LayoutStyle.getInstance()
                                 .getPreferredGap(queriesLabel,
                                                  fakeLabel,
                                                  RELATED,
                                                  SwingConstants.EAST,
                                                  this);
            linksPanelOffsetValid = true;
        }
        return linksPanelOffset;
    }

    private Insets getCachedInsets() {
        if (cachedInsets == null) {
            cachedInsets = super.getInsets();
        }
        return cachedInsets;
    }

    /*
     * To make it work correctly with GroupLayout.
     */
    @Override
    public int getBaseline(int width, int height) {
        return getBaseline();
    }

    public int getBaseline() {
        validateBaseline();
        return baseline;
    }

    private void validateBaseline() {
        if (!baselineValid) {
            queryLinksPanel.validateVerticalMetrics();

            baseline = Math.max(getLabelBaseline(),
                                queryLinksPanel.getBaseline())
                       + getCachedInsets().top;
            baselineValid = true;
        }
    }

    private int getLabelBaseline() {
        if (!labelBaselineValid) {
            Dimension size = queriesLabel.getPreferredSize();
            labelBaseline = queriesLabel.getBaseline(size.width, size.height);
            labelBaselineValid = true;
        }
        return labelBaseline;
    }

    /*
     * To make it work correctly with GroupLayout.
     */
    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    public void setUI(PanelUI ui) {
        invalidateUiDependentValues();
        super.setUI(ui);
    }

    @Override
    protected void setUI(ComponentUI newUI) {
        invalidateUiDependentValues();
        super.setUI(newUI);
    }

    private void invalidateUiDependentValues() {
        baselineValid = false;
        labelBaselineValid = false;
        linksPanelOffsetValid = false;
        cachedInsets = null;
        invalidatePrefSize();
    }

    private void invalidatePrefSize() {
        prefSize = null;
    }

}
