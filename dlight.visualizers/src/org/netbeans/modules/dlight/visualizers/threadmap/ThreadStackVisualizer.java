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
package org.netbeans.modules.dlight.visualizers.threadmap;

import java.beans.PropertyVetoException;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.management.api.DLightSession;
import org.netbeans.modules.dlight.management.api.DLightSession.SessionState;
import org.netbeans.modules.dlight.visualizers.api.ThreadStateResources;
import java.awt.CardLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.dataprovider.StackDataProvider;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.visualizers.CallStackTopComponent;
import org.netbeans.modules.dlight.core.stack.ui.MultipleCallStackPanel;
import org.netbeans.modules.dlight.management.api.SessionStateListener;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.UIThread;
import org.netbeans.modules.dlight.visualizers.threadmap.ThreadStackVisualizerConfiguration.ExpansionMode;
import org.netbeans.modules.dlight.visualizers.threadmap.ThreadStackVisualizerConfiguration.StackNameProvider;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public final class ThreadStackVisualizer extends JPanel implements Visualizer<ThreadStackVisualizerConfiguration>, SessionStateListener, DataFilterListener {

    private static final RequestProcessor RP = new RequestProcessor(ThreadStackVisualizer.class.getName(), 1);
    private final ThreadStackVisualizerConfiguration configuration;
    private ThreadDump descriptor;
    private StackNameProvider stackNameProvider;
    private long dumpTime;
    private final MultipleCallStackPanel stackPanel;
    private JPanel emptyPanel;
    private final CardLayout cardLayout = new CardLayout();
    private DLightSession session;
    private List<DataFilter> filters;
    private int prefferedSelection = -1;

    private static final class Lock { }
    private static final class UiLock { }
    private final Object lock = new Lock();
    private final Object uiLock = new UiLock();
    private boolean needUpdate = false;

    ThreadStackVisualizer(ThreadStackVisualizerConfiguration configuraiton, StackDataProvider sourceFileInfo) {
        this.descriptor = configuraiton.getThreadDump();
        this.dumpTime = configuraiton.getDumpTime();
        this.stackNameProvider = configuraiton.getStackNameProvider();
        this.configuration = configuraiton;
        stackPanel = MultipleCallStackPanel.createInstance(sourceFileInfo);
        setLayout(cardLayout);
        emptyPanel = new JPanel();
        add(emptyPanel, "empty");//NOI18N
        add(stackPanel, "stack");//NOI18N
        if (descriptor == null || descriptor.getThreadStates().isEmpty()) {
            setEmptyContent();
        } else {
            setNonEmptyContent();
        }
    }

    private void setEmptyContent() {
	assert SwingUtilities.isEventDispatchThread();
        cardLayout.show(this, "empty");//NOI18N
        emptyPanel.removeAll();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        long time = 0;
        if (descriptor != null) {
            time = ThreadStateColumnImpl.timeStampToMilliSeconds(descriptor.getTimestamp()) - dumpTime;
        }
        String timeString = TimeLineUtils.getMillisValue(time);
        String message = NbBundle.getMessage(ThreadStackVisualizer.class, "ThreadStackVisualizerNoStackAt", timeString); //NOI18N
        JLabel label = new JLabel(message);
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        emptyPanel.add(label);
    }

    private void setNonEmptyContent() {
        synchronized (lock) {
            //and now add all you need
            final long time = ThreadStateColumnImpl.timeStampToMilliSeconds(descriptor.getTimestamp()) - dumpTime;
            final String timeString = TimeLineUtils.getMillisValue(time);
            final String rootName = NbBundle.getMessage(ThreadStackVisualizer.class, "ThreadStackVisualizerStackAt", timeString); //NOI18N
            //collect all and then update UI
            DLightExecutorService.submit(new Runnable() {

                @Override
                public void run() {

                    //synchronized (lock) {                    
                    final ThreadSnapshot[] snapshots = descriptor.getThreadStates().toArray(new ThreadSnapshot[0]);
                    final Vector<List<FunctionCall>> stacks = new Vector<List<FunctionCall>>();
                    stacks.setSize(snapshots.length);
                    for (int i = 0, size = snapshots.length; i < size; i++) {
                        ThreadSnapshot snapshot = snapshots[i];
                        final MSAState msa = snapshot.getState();
                        final ThreadStateResources res = ThreadStateResources.forState(msa);
                        if (res != null) {
                            stacks.set(i, snapshot.getStack());

                        }
                    }
                    UIThread.invoke(new Runnable() {

                        @Override
                        public void run() {
                            synchronized(uiLock){
				assert SwingUtilities.isEventDispatchThread();
                                stackPanel.clean();
                                stackPanel.setRootVisible(rootName);
                                for (int i = 0, size = snapshots.length; i < size; i++) {
                                    ThreadSnapshot snapshot = snapshots[i];
                                    final MSAState msa = snapshot.getState();
                                    final ThreadStateResources res = ThreadStateResources.forState(msa);
                                    if (res != null) {
                                        final List<FunctionCall> functionCalls = stacks.get(i);
                                        if (functionCalls != null) {
                                            stackPanel.add(stackNameProvider.getStackName(snapshot), new ThreadStateIcon(msa, 10, 10), functionCalls,
                                                           configuration.getStackNodeActionsProvider().getStackNodeActions(snapshot.getThreadInfo().getThreadId()));
                                            if (configuration.getPreferredSelection() == snapshot.getThreadInfo().getThreadId()) {
                                                prefferedSelection = i;
                                            }
                                        }
                                    }
                                }
                                cardLayout.show(ThreadStackVisualizer.this, "stack");//NOI18N
                                selectRootNode();
                            }

                        }
                    });
                }
            }, "Fill in panel for a stack");//NOI18N
//                }
//
//            }
//            cardLayout.show(this, "stack");//NOI18N
//            selectRootNode();
        }

    }

    void selectRootNode() {

        RP.post(new Runnable() {

            @Override
            public void run() {
                if (configuration.getPrefferedExpansion() == ExpansionMode.ExpandAll) {
                    stackPanel.expandAll();
                }
                int i = 0;
                for(Node node : stackPanel.getExplorerManager().getRootContext().getChildren().getNodes()) {
                    if (i == prefferedSelection) {
                        try {
                            if (configuration.getPrefferedExpansion() == ExpansionMode.ExpandCurrent) {
                                stackPanel.expandNode(node);
                            }
                            stackPanel.getExplorerManager().setSelectedNodes(new Node[]{node});
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        break;
                    }
                    i++;
                }
            }
        }, 500);
    }

    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "CallStackDetails"); //NOI18N
    }

    @Override
    public ThreadStackVisualizerConfiguration getVisualizerConfiguration() {
        return configuration;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public VisualizerContainer getDefaultContainer() {
        return CallStackTopComponent.findInstance();
    }

    @Override
    public boolean requestFocus(boolean temporary) {
        if (stackPanel != null) {
            return stackPanel.requestFocus(temporary);
        }
        return super.requestFocus(temporary);
    }

    @Override
    public void refresh() {
        synchronized (lock) {
            if (!needUpdate) {
                return;
            }
            Collection<ThreadDumpFilter> dumpFilters = getDataFilter(ThreadDumpFilter.class);
            if (dumpFilters != null && !dumpFilters.isEmpty()) {

                //get first
                ThreadDumpFilter dumpFilter = dumpFilters.iterator().next();
                this.dumpTime = dumpFilter.getDumpTime();
                this.descriptor = dumpFilter.getThreadDump();

            }
            needUpdate = false;
            if (!EventQueue.isDispatchThread()) {
                UIThread.invoke(new Runnable() {

                    @Override
                    public void run() {
                        if (descriptor == null || descriptor.getThreadStates().isEmpty()) {
                            setEmptyContent();
                        } else {
                            setNonEmptyContent();
                        }
                    }
                });
            } else {
                if (descriptor == null || descriptor.getThreadStates().isEmpty()) {
                    setEmptyContent();
                } else {
                    setNonEmptyContent();
                }
            }
        }
    }

    private <T extends DataFilter> Collection<T> getDataFilter(Class<T> clazz) {
        synchronized (lock) {
            Collection<T> result = new ArrayList<T>();
            for (DataFilter f : filters) {
                if (f.getClass() == clazz) {
                    result.add(clazz.cast(f));
                } else {
                    try {
                        Class<? extends T> r = f.getClass().asSubclass(clazz);
                        result.add(clazz.cast(f));
                    } catch (ClassCastException e) {
                    }

                }
            }
            return result;
        }
    }

    @Override
    public void sessionStateChanged(DLightSession session, SessionState oldState, SessionState newState) {
        if (this.session == null | this.session != session) {
            if (this.session != session && this.session != null) {
                this.session.removeSessionStateListener(this);
                this.session.removeDataFilterListener(this);
            }
            this.session = session;
            this.session.addDataFilterListener(this);

        }
    }

    @Override
    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
        if (isAdjusting) {
            return;
        }
        synchronized (lock) {
            //check new and old one's
            this.filters = new ArrayList<DataFilter>(newSet);
            needUpdate = !getDataFilter(ThreadDumpFilter.class).isEmpty();
        }
    }

    @Override
    public void updateVisualizerConfiguration(ThreadStackVisualizerConfiguration aConfiguration) {
        configuration.update(aConfiguration);
    }
}
