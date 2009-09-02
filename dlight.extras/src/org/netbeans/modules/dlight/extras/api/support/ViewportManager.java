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

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.concurrent.TimeUnit;
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
public final class ViewportManager extends JScrollBar
        implements AdjustmentListener, ChangeListener {

    private static final long EXTENT = 20000L; // 20 seconds

    private final ViewportModel viewportModel;
    private boolean isAdjusting;

    public ViewportManager() {
        super(JScrollBar.HORIZONTAL);
        viewportModel = new DefaultViewportModel();
        viewportModel.setLimits(new Range<Long>(0L, 0L));
        viewportModel.setViewport(new Range<Long>(0L, EXTENT));
        viewportModel.addChangeListener(this);
        adjust();
        addAdjustmentListener(this);
    }

    public void addManagedComponent(ViewportAware component) {
        component.setViewportModel(viewportModel);
    }

    private void adjust() {
        Range<Long> limits = viewportModel.getLimits();
        Range<Long> viewport = viewportModel.getViewport();
        isAdjusting = true;
        setMinimum((int)TimeUnit.MILLISECONDS.toSeconds(Math.min(limits.getStart(), viewport.getStart())));
        setMaximum((int)TimeUnit.MILLISECONDS.toSeconds(Math.max(limits.getEnd(), viewport.getEnd())));
        setValue((int)TimeUnit.MILLISECONDS.toSeconds(viewport.getStart()));
        setVisibleAmount((int)TimeUnit.MILLISECONDS.toSeconds(viewport.getEnd() - viewport.getStart()));
        isAdjusting = false;
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
}
