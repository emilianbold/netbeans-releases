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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.LinkButton;

/**
 * Panel displaying names of saved queries.
 * The list of query names wraps automatically if it does not fit to the
 * <em>visible</em> part of the form.
 *
 * @author Marian Petras
 */
class QueryLinksPanel extends ViewportWidthAwarePanel implements FocusListener {

    private static final int SEPARATOR_WIDTH = 2;
    private static final int SPACE_AROUND_SEPARATOR = 5;
    private static final int TOTAL_SEPARATOR_WIDTH
                             = SEPARATOR_WIDTH + (2 * SPACE_AROUND_SEPARATOR);

    private static final int INTERLINE_SPACING = 2;

    private Repository repository;
    private QueryButton[] buttons;

    private Dimension[] buttonPrefSizes;
    private int[] baselines;
    private int[] rowNumbers;
    private int baseline = -1;
    private int rowHeight = -1;
    private int rowCount = 0;

    private Dimension prefSize;
    private boolean verticalMetricsKnown;

    public QueryLinksPanel() {
        super(null);
        setOpaque(false);
    }

    void setQueries(Query[] queries) {
        if ((queries != null) && (queries.length == 0)) {
            queries = null;
        }

        boolean queriesChanged = (this.buttons != null) || (queries != null);
        if (!queriesChanged) {
            return;
        }

        invalidateVerticalMetrics();
        invalidatePrefSize();

        if (buttons != null) {
            removeAll();
        }
        if (queries == null) {
            buttons = null;
            rowNumbers = null;
        } else {
            buttons = new QueryButton[queries.length];
            rowNumbers = new int[queries.length];
            QueryTopComponent queryTopC = (QueryTopComponent)
                                          SwingUtilities.getAncestorOfClass(
                                                 QueryTopComponent.class, this);
            for (int i = 0; i < queries.length; i++) {
                Query query = queries[i];
                query.addPropertyChangeListener(queryTopC);
                QueryButton button = new QueryButton(repository, query);
                button.setText(query.getDisplayName());
                button.addFocusListener(this);
                add(button);
                buttons[i] = button;
            }
        }
    }

    boolean isEmpty() {
        return (buttons == null);
    }

    boolean isWrapped() {
        validatePrefSize();
        return rowCount > 1;
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if ((buttons != null) && (buttons.length > 1)) {

            /* paint the separators: */

            Graphics sg = g.create();
            sg.setColor(Color.BLACK);
            final Point tmpPoint = new Point();

            int lastRowNum = 0;
            for (int i = 1; i < buttons.length; i++) {
                int rowNum = rowNumbers[i];
                if (rowNum == lastRowNum) {
                    int x = buttons[i].getLocation(tmpPoint).x
                            - (SPACE_AROUND_SEPARATOR + SEPARATOR_WIDTH);
                    int y = rowNum * (rowHeight + INTERLINE_SPACING);
                    g.fillRect(x, y, SEPARATOR_WIDTH, rowHeight);
                }
                lastRowNum = rowNum;
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        validatePrefSize();
        return prefSize;
    }

    private void validatePrefSize() {
        if (prefSize == null) {
            computeLayout(false);
        }
    }

    @Override
    public void invalidate() {
        invalidatePrefSize();
        super.invalidate();
    }

    @Override
    protected void visibleWidthChanged(int newWidth) {
        super.visibleWidthChanged(newWidth);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                //revalidate();
            }
        });
        invalidate();
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (mgr != null) {
            throw new UnsupportedOperationException(
                "QueriesListPanel does not support layout managers.");  //NOI18N
        }
    }

    @Override
    public void doLayout() {
        computeLayout(true);
    }

    private Dimension computeLayout(boolean setBounds) {
        int width, height;

        Insets insets = getInsets();
        if (buttons == null) {
            width = insets.left + insets.right;
            height = insets.top + insets.bottom;
            rowCount = 0;
        } else {
            validateVerticalMetrics();
            int horizPos = insets.left;
            int horizPosLimit = getAvailableWidth() - insets.right;
            int rowBaseline = insets.top + baseline;
            int rowNum = 0;
            height = rowHeight;
            rowCount = 1;

            QueryButton button = buttons[0];
            int buttonBaseline = baselines[0];
            Dimension buttonPrefSize = buttonPrefSizes[0];
            int startHorizPos = horizPos;
            int endHorizPos = startHorizPos + buttonPrefSize.width;
            int maxEndHorizPos = endHorizPos;
            if (setBounds) {
                button.setBounds(startHorizPos, rowBaseline - buttonBaseline,
                                 buttonPrefSize.width, buttonPrefSize.height);
            }
            horizPos = endHorizPos;
            rowNumbers[0] = rowNum;
            for (int i = 1; i < buttons.length; i++) {
                button = buttons[i];
                buttonBaseline = baselines[i];
                buttonPrefSize = buttonPrefSizes[i];
                startHorizPos = horizPos + TOTAL_SEPARATOR_WIDTH;
                endHorizPos = startHorizPos + buttonPrefSize.width;
                if (endHorizPos > horizPosLimit) {
                    startHorizPos = insets.left;
                    endHorizPos = startHorizPos + buttonPrefSize.width;
                    rowBaseline += INTERLINE_SPACING + rowHeight;
                    height += INTERLINE_SPACING + rowHeight;
                    rowCount++;
                    rowNum++;
                }
                if (setBounds) {
                    button.setBounds(startHorizPos, rowBaseline - buttonBaseline,
                                     buttonPrefSize.width, buttonPrefSize.height);
                }
                maxEndHorizPos = Math.max(maxEndHorizPos, endHorizPos);
                horizPos = endHorizPos;
                rowNumbers[i] = rowNum;
            }
            width = maxEndHorizPos + insets.right;
        }
        prefSize = new Dimension(width, height);
        return prefSize;
    }

    /*
     * To make it work correctly with GroupLayout.
     */
    @Override
    public int getBaseline(int width, int height) {
        return getBaseline();
    }

    int getBaseline() {
        validateVerticalMetrics();
        return baseline;
    }

    void validateVerticalMetrics() {
        if (verticalMetricsKnown) {
            return;
        }

        int maxAboveBaseline = 0;
        int maxBelowBaseline = 0;
        if (buttons == null) {
            baselines = null;
            buttonPrefSizes = null;
        } else {
            baselines = new int[buttons.length];
            buttonPrefSizes = new Dimension[buttons.length];
            for (int i = 0; i < buttons.length; i++) {
                QueryButton button = buttons[i];
                Dimension buttonPrefSize = button.getPreferredSize();

                int aboveBaseline = button.getBaseline(buttonPrefSize.width,buttonPrefSize.height);
                int belowBaseline = buttonPrefSize.height - aboveBaseline;

                buttonPrefSizes[i] = buttonPrefSize;
                baselines[i] = aboveBaseline;

                maxAboveBaseline = Math.max(maxAboveBaseline, aboveBaseline);
                maxBelowBaseline = Math.max(maxBelowBaseline, belowBaseline);
            }
        }
        baseline = maxAboveBaseline;
        rowHeight = maxAboveBaseline + maxBelowBaseline;

        verticalMetricsKnown = true;
    }

    /*
     * To make it work correctly with GroupLayout.
     */
    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return BaselineResizeBehavior.CONSTANT_ASCENT;
    }

    @Override
    protected void notifyChildrenOfVisibleWidth() {
        //no-op
    }

    public void focusGained(FocusEvent e) {
        final Component c = e.getComponent();
        if (c instanceof JComponent) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    QueryLinksPanel.this.scrollRectToVisible(c.getBounds());
                }
            });
        }
    }

    public void focusLost(FocusEvent e) {
        //do nothing
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
        invalidateVerticalMetrics();
    }

    private void invalidateVerticalMetrics() {
        verticalMetricsKnown = false;
        buttonPrefSizes = null;
        baseline = -1;
        baselines = null;
        rowHeight = 0;
    }

    private void invalidatePrefSize() {
        prefSize = null;
    }

    private class QueryButton extends LinkButton {
        public QueryButton(final Repository repo, final Query query) {
            super();
            setText(query.getDisplayName());
            getAccessibleContext().setAccessibleDescription(query.getTooltip());
            setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    QueryAction.openQuery(query, repo);
                }
            });
        }
    }

}
