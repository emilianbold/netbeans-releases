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
package org.netbeans.modules.dlight.extras.api.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.extras.api.ViewportAware;
import org.netbeans.modules.dlight.extras.api.Range;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.util.UIThread;

/**
 * @author Alexey Vladykin
 */
public final class ViewportManager extends JPanel
        implements AdjustmentListener, ChangeListener {

    private static final long EXTENT = 20000L; // 20 seconds

    private final ViewBar viewbar;
    private final JScrollBar scrollbar;
    private final ViewportModel viewportModel;
    private boolean isAdjusting;

    public ViewportManager() {
        super(new BorderLayout());

        viewportModel = new DefaultViewportModel();
        viewportModel.setLimits(new Range<Long>(0L, 0L));
        viewportModel.setViewport(new Range<Long>(0L, EXTENT));
        viewportModel.addChangeListener(this);

        viewbar = new ViewBar(15);

        scrollbar = new JScrollBar(JScrollBar.HORIZONTAL);
        adjust();
        scrollbar.addAdjustmentListener(this);

        add(viewbar, BorderLayout.CENTER);
        add(scrollbar, BorderLayout.SOUTH);
    }

    public void addManagedComponent(ViewportAware component) {
        component.setViewportModel(viewportModel);
    }

    private void adjust() {
        Range<Long> limits = viewportModel.getLimits();
        Range<Long> viewport = viewportModel.getViewport();
        long start = Math.min(limits.getStart(), viewport.getStart());
        long end = Math.max(limits.getEnd(), viewport.getEnd());
        isAdjusting = true;
        scrollbar.setMinimum((int)TimeUnit.MILLISECONDS.toSeconds(start));
        scrollbar.setMaximum((int)TimeUnit.MILLISECONDS.toSeconds(end));
        scrollbar.setValue((int)TimeUnit.MILLISECONDS.toSeconds(viewport.getStart()));
        scrollbar.setVisibleAmount((int)TimeUnit.MILLISECONDS.toSeconds(viewport.getEnd() - viewport.getStart()));
        isAdjusting = false;

        viewbar.setStart((float) viewport.getStart() / (end - start));
        viewbar.setEnd((float) viewport.getEnd() / (end - start));
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == viewportModel) {
            UIThread.invoke(new Runnable() {
                public void run() {
                    adjust();
                }
            });
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        if (!isAdjusting) {
            long viewportStart = TimeUnit.SECONDS.toMillis(e.getValue());
            viewportModel.setViewport(new Range<Long>(viewportStart, viewportStart + EXTENT));
        }
    }

    private static class ViewBar extends JComponent {
        private final int margin;
        private float start;
        private float end;

        public ViewBar(int margin) {
            this.margin = margin;
            this.start = 0f;
            this.end = 1f;
            setMinimumSize(new Dimension(50, 30));
            setPreferredSize(new Dimension(50, 30));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        }

        public void setStart(float start) {
            this.start = start;
            repaint();
        }

        public void setEnd(float end) {
            this.end = end;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(Color.BLACK);
            int startx = margin + (int)((getWidth() - 2 * margin - 1) * start);
            g.drawLine(startx, 0, startx, getHeight());
            int endx = margin + (int)((getWidth() - 2 * margin - 1) * end);
            g.drawLine(endx, 0, endx, getHeight());
        }
    }
}
