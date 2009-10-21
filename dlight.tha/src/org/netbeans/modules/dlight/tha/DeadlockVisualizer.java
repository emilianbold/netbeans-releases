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
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.module.dlight.threads.api.Deadlock;
import org.netbeans.module.dlight.threads.api.DeadlockThreadSnapshot;
import org.netbeans.module.dlight.threads.dataprovider.ThreadAnalyzerDataProvider;
import org.netbeans.modules.dlight.core.stack.ui.MultipleCallStackPanel;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.UIThread;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 * @author Alexey Vladykin
 */
public final class DeadlockVisualizer implements Visualizer<DeadlockVisualizerConfiguration> {

    public final static Image deadlockImage =
            ImageUtilities.loadImage("org/netbeans/modules/dlight/tha/resources/deadlock_active16.png"); // NOI18N
    public final static Icon deadlockIcon = ImageUtilities.image2Icon(deadlockImage);
    public final static Icon deadlockRequestIcon =
            ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/tha/resources/deadlock_request_ver1.png", false);//NOI18N
    public final static Icon deadlockHeldIcon =
            ImageUtilities.loadImageIcon("org/netbeans/modules/dlight/tha/resources/deadlock_request_ver2.png", false);//NOI18N
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
        return THAVisulaizerContainerTopComponent.findInstance();
    }

    public synchronized void refresh() {
        if (refreshTask == null) {
            @SuppressWarnings("unchecked")
            final MasterSlaveView<Deadlock, DeadlockTHANodeFactory> view =
                    (MasterSlaveView<Deadlock, DeadlockTHANodeFactory>) getComponent();
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

    public void updateVisualizerConfiguration(DeadlockVisualizerConfiguration configuration) {
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                DeadlockVisualizer.class, key, params);
    }

    private class DeadlockRenderer implements SlaveRenderer {

        final MultipleCallStackPanel stackPanel =
                MultipleCallStackPanel.createInstance(DeadlockVisualizer.this.dataProvider);
        private List<DeadlockThreadSnapshot> snapshots;

        public void setValue(Object aValue, boolean isSelected) {
            if (aValue instanceof Deadlock) {
                Deadlock d = (Deadlock) aValue;
                snapshots = d.getThreadStates();//.get(0).getHeldLockCallStack();
            }
        }

        public Component getComponent() {
            stackPanel.clean();
            for (DeadlockThreadSnapshot dts : snapshots) {
                stackPanel.add(loc("DeadlockVisualizer.LockHeld") +  " " + //NOI18N
                        Long.toHexString(dts.getHeldLockAddress()),
                        deadlockHeldIcon,
                        dts.getHeldLockCallStack());
                stackPanel.add(loc("DeadlockVisualizer.LockRequested") +  " " + //NOI18N
                        Long.toHexString(dts.getRequestedLockAddress()),
                        deadlockRequestIcon,
                        dts.getRequestedLockCallStack());
            }
            return stackPanel;
//            return StackPanelFactory.newStackPanel(stack);
        }

        public void expandAll() {
            RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    stackPanel.expandAll();
                }
            }, 500);

        }
    }

    private final static class DeadlockNode extends THANode<Deadlock> {

        private final Deadlock deadlock;

        DeadlockNode(Deadlock deadlock) {
            super(deadlock);
            this.deadlock = deadlock;
        }

        @Override
        public String getDisplayName() {
            return deadlock.isActual() ? 
                loc("DeadlockVisualizer.DeadlockNode.ActualDeadlock") ://NOI18N
                loc("DeadlockVisualizer.DeadlockNode.PotentialDeadlock");//NOI18N
        }

        @Override
        public Image getIcon(int type) {
            return deadlockImage;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }

    }

    private static final class DeadlockTHANodeFactory implements THANodeFactory<Deadlock> {

        public THANode<Deadlock> create(Deadlock object) {
            return new DeadlockNode(object);
        }
    }
}
