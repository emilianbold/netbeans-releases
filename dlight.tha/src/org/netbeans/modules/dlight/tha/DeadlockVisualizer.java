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
import java.awt.Image;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.Renderer;
import org.netbeans.module.dlight.threads.api.Deadlock;
import org.netbeans.module.dlight.threads.api.DeadlockThreadSnapshot;
import org.netbeans.module.dlight.threads.dataprovider.ThreadAnalyzerDataProvider;
import org.netbeans.modules.dlight.core.stack.ui.MultipleCallStackPanel;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.DefaultVisualizerContainer;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * @author Alexey Vladykin
 */
public final class DeadlockVisualizer implements Visualizer<DeadlockVisualizerConfiguration> {

    private final DeadlockVisualizerConfiguration configuration;
    private final ThreadAnalyzerDataProvider dataProvider;
    private MasterSlaveView<Deadlock, DeadlockTHANodeFactory> msview;
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
            msview = new MasterSlaveView<Deadlock, DeadlockTHANodeFactory>(new DeadlockTHANodeFactory());
            msview.setSlaveRenderer(new DeadlockRenderer());
        }
        return msview;
    }

    public VisualizerContainer getDefaultContainer() {
        return DefaultVisualizerContainer.getInstance();
    }

    public synchronized void refresh() {
        if (refreshTask == null) {
            final MasterSlaveView<Deadlock, DeadlockTHANodeFactory> view = (MasterSlaveView<Deadlock, DeadlockTHANodeFactory>) getComponent();
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

    private class DeadlockRenderer implements Renderer {

        private List<DeadlockThreadSnapshot> snapshots;

        public void setValue(Object aValue, boolean isSelected) {
            if (aValue instanceof Deadlock) {
                Deadlock d = (Deadlock) aValue;
                snapshots = d.getThreadStates();//.get(0).getHeldLockCallStack();
//                StringBuilder buf = new StringBuilder();
//                buf.append(d).append('\n');
//                for (DeadlockThreadSnapshot dts : d.getThreadStates()) {
//                    buf.append("\tThread:\n");
//                    buf.append("\t\tLock held:\t0x").append(Long.toHexString(dts.getHeldLockAddress())).append('\n');
//                    buf.append("\t\tLock requested:\t0x").append(Long.toHexString(dts.getRequestedLockAddress())).append('\n');
//                }
                //setText(buf.toString());
            }
        }

        public Component getComponent() {
            MultipleCallStackPanel stackPanel = MultipleCallStackPanel.createInstance(DeadlockVisualizer.this.dataProvider);
            for (DeadlockThreadSnapshot dts : snapshots) {
                stackPanel.add("Lock held:  " + Long.toHexString(dts.getHeldLockAddress()), true, dts.getHeldLockCallStack());//NOI18N
                stackPanel.add("Lock requested:  " + Long.toHexString(dts.getRequestedLockAddress()), true, dts.getRequestedLockCallStack());//NOI18N
            }
            stackPanel.expandAll();
            return stackPanel;
//            return StackPanelFactory.newStackPanel(stack);
        }
    }

    private final class DeadlockNode extends THANode<Deadlock> {

        private final Deadlock deadlock;
        public final Image icon = ImageUtilities.loadImage("org/netbeans/modules/dlight/tha/resources/deadlock_active16.png"); // NOI18N

        DeadlockNode(Deadlock deadlock) {
            super(deadlock);
            this.deadlock = deadlock;
        }

        @Override
        public String getDisplayName() {
            return deadlock.isActual() ? "Actual deadlock" : "Potential deadlock";//NOI18N
        }

        @Override
        public Image getIcon(int type) {
            return icon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }

    private final class DeadlockTHANodeFactory implements THANodeFactory<Deadlock> {

        public THANode create(Deadlock object) {
            return new DeadlockNode(object);
        }
    }
}
