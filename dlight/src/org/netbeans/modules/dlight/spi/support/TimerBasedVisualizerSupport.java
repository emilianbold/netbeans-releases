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
package org.netbeans.modules.dlight.spi.support;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.storage.types.TimeDuration;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.util.DLightExecutorService;

public final class TimerBasedVisualizerSupport implements ComponentListener {

    private final Visualizer visualizer;
    private final TimeDuration refreshInterval;
    private Future future;
    private boolean isShown = true;

    public TimerBasedVisualizerSupport(final Visualizer visualizer, final TimeDuration refreshInterval) {
        this.visualizer = visualizer;
        this.refreshInterval = refreshInterval;
    }

    private final void startTimer() {
        if (future != null && !future.isCancelled()) {
            return;
        }

        future = DLightExecutorService.scheduleAtFixedRate(new Runnable() {

            public void run() {
                visualizer.refresh();
            }
        }, refreshInterval.getValueIn(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS,
                "TimerBasedVisualizerSupport: timer for " + visualizer.toString()); // NOI18N
    }

    private final void stopTimer() {
        if (future != null) {
            future.cancel(false);
        }
    }

    public void componentResized(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
        if (isShown) {
            return;
        }

        isShown = visualizer.getComponent().isShowing();

        if (isShown) {
            //we should change explorerManager
            startTimer();
        }
    }

    public void componentHidden(ComponentEvent e) {
        stopTimer();
        isShown = false;
    }

    public void start() {
        startTimer();
    }

    public void stop() {
        stopTimer();
    }
}
