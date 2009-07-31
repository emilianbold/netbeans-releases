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
package org.netbeans.modules.dlight.tha;

import java.awt.Component;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.Renderer;
import org.netbeans.modules.dlight.api.stack.Deadlock;
import org.netbeans.modules.dlight.core.stack.dataprovider.ThreadAnalyzerDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.DefaultVisualizerContainer;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * @author Alexey Vladykin
 */
public final class DeadlockVisualizer implements Visualizer<DeadlockVisualizerConfiguration> {

    private final DeadlockVisualizerConfiguration configuration;
    private final ThreadAnalyzerDataProvider dataProvider;
    private MasterSlaveView msview;
    private Task refreshTask;

    public DeadlockVisualizer(DeadlockVisualizerConfiguration configuration, ThreadAnalyzerDataProvider dataProvider) {
        this.configuration = configuration;
        this.dataProvider = dataProvider;
    }

    public DeadlockVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public synchronized JComponent getComponent() {
        if (msview == null) {
            msview = new MasterSlaveView<Deadlock>();
            msview.setSlaveRenderer(new DeadlockRenderer());
        }
        return msview;
    }

    public VisualizerContainer getDefaultContainer() {
        return DefaultVisualizerContainer.getInstance();
    }

    public synchronized void refresh() {
        if (refreshTask == null) {
            final MasterSlaveView view = (MasterSlaveView) getComponent();
            refreshTask = RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    final List<? extends Deadlock> deadlocks = dataProvider.getDeadlocks();
                    UIThread.invoke(new Runnable() {
                        public void run() {
                            view.setMasterData(deadlocks);
                        }
                    });
                    refreshTask = null;
                }
            });
        }
    }

    private static class DeadlockRenderer extends JTextArea implements Renderer {

        public void setValue(Object aValue, boolean isSelected) {
            setText(aValue.toString());
        }

        public Component getComponent() {
            return this;
        }
    }
}
