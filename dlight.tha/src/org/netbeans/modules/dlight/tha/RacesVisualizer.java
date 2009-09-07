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

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.Renderer;
import org.netbeans.module.dlight.threads.api.Datarace;
import org.netbeans.module.dlight.threads.dataprovider.ThreadAnalyzerDataProvider;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.ui.CallStackUISupport;
import org.netbeans.modules.dlight.core.stack.ui.MultipleCallStackPanel;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.api.DefaultVisualizerContainer;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author mt154047
 */
public class RacesVisualizer implements Visualizer<RacesVisualizerConfiguration> {

    private final RacesVisualizerConfiguration configuration;
    private final ThreadAnalyzerDataProvider dataProvider;
    private MasterSlaveView<Datarace, DataraceTHANodeFactory> msview;
    private Task refreshTask;

    public RacesVisualizer(RacesVisualizerConfiguration configuration, ThreadAnalyzerDataProvider dataProvider) {
        this.configuration = configuration;
        this.dataProvider = dataProvider;
    }

    public RacesVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    public synchronized JComponent getComponent() {
        if (msview == null) {
            msview = new MasterSlaveView<Datarace, DataraceTHANodeFactory>(new DataraceTHANodeFactory());
            msview.setSlaveRenderer(new RacesRenderer());
        }
        return msview;
    }

    public VisualizerContainer getDefaultContainer() {
        return DefaultVisualizerContainer.getInstance();
    }

    public synchronized void refresh() {
        if (refreshTask == null) {
            final MasterSlaveView<Datarace, DataraceTHANodeFactory> view = (MasterSlaveView<Datarace, DataraceTHANodeFactory>) getComponent();
            refreshTask = RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    final List<? extends Datarace> dataraces = dataProvider.getDataraces();
                    UIThread.invoke(new Runnable() {

                        public void run() {
                            view.setMasterData(dataraces);
                        }
                    });
                    refreshTask = null;
                }
            });
        }
    }

    private class RacesRenderer implements Renderer {

        private List<ThreadDump> threadDumps;
        private final MultipleCallStackPanel stackPanel = MultipleCallStackPanel.createInstance(RacesVisualizer.this.dataProvider);

        public void setValue(Object aValue, boolean isSelected) {
            if (aValue instanceof Datarace) {
                Datarace d = (Datarace) aValue;
                this.threadDumps = d.getThreadDumps();
            } else if (aValue instanceof ThreadDump) {
                this.threadDumps = Arrays.asList((ThreadDump) aValue);
            }
        }

        public Component getComponent() {
            stackPanel.clean();
            for (ThreadDump threadDump : threadDumps) {
                List<ThreadSnapshot> threads = threadDump.getThreadStates();
                for (ThreadSnapshot snap : threads) {
                    stackPanel.add("Access  " + (snap.getMemoryAccessType() == ThreadSnapshot.MemoryAccessType.READ ? " [R]" : " [W]"), ImageUtilities.image2Icon(CallStackUISupport.downBadge), snap.getStack());//NOI18N
                }
            }
            stackPanel.expandAll();
            return stackPanel;
        }
    }

    private final class DataraceNode extends THANode<Datarace> {

        public final Image icon = ImageUtilities.loadImage("org/netbeans/modules/dlight/tha/resources/races_active16.png"); // NOI18N
        private final Datarace race;

        DataraceNode(Datarace race) {
            super(race);
            this.race = race;
            setChildren(new DataraceNodeChildren(race));
        }

        @Override
        public String getDisplayName() {
            return "Address " + race.getAddress() + ": " + race.getThreadDumps().size() + " concurrent accesses";//NOI18N
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

    private final class RaceNode extends THANode<ThreadDump> {

        private final ThreadDump threadDump;
        private String displayName;

        RaceNode(ThreadDump threadDump) {
            super(threadDump);
            this.threadDump = threadDump;
            List<ThreadSnapshot> snapshots = threadDump.getThreadStates();
            displayName = "";
            for (ThreadSnapshot s : snapshots) {
                List<FunctionCall> stack = s.getStack();
                displayName += stack.get(stack.size() - 1).getFunction().getName() + (s.getMemoryAccessType() == ThreadSnapshot.MemoryAccessType.READ ? " [R]" : " [W]") + " | "; // NOI18N
            }
            if (displayName.length() > 1) {
                displayName = displayName.substring(0, displayName.length() - 2);
            }
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Image getIcon(int type) {
            return CallStackUISupport.functionIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }

    private final class DataraceNodeChildren extends Children.Keys<ThreadDump> {

        private final Datarace datarace;

        DataraceNodeChildren(Datarace datarace) {
            this.datarace = datarace;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(datarace.getThreadDumps());
        }

        @Override
        protected Node[] createNodes(ThreadDump key) {
            return new Node[]{new RaceNode(key)};
        }
    }

    private final class DataraceTHANodeFactory implements THANodeFactory<Datarace> {

        public THANode<Datarace> create(Datarace object) {
            return new DataraceNode(object);
        }
    }

    static class StackIcon implements Icon {

        protected Color threadStateColor;
        protected int height;
        protected int width;

        public StackIcon(int width, int height) {
            this.threadStateColor = Color.red;
            this.width = width;
            this.height = height;
        }

        public int getIconHeight() {
            return height;
        }

        public int getIconWidth() {
            return width;
        }

        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            if (threadStateColor != null) {
                g.setColor(threadStateColor);
                g.fillRect(x, y, width, height);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, width - 1, height - 1);
            }
        }
    }
}

