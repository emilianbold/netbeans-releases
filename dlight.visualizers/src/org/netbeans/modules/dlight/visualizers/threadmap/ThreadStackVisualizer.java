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

import org.netbeans.modules.dlight.visualizers.api.ThreadStateResources;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.module.dlight.threads.api.OpenInEditor;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.visualizers.CallStackTopComponent;
import org.netbeans.modules.dlight.core.stack.ui.MultipleCallStackPanel;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class ThreadStackVisualizer extends JPanel implements Visualizer<VisualizerConfiguration> {

    private ThreadDump descriptor;
    private long startTime;
    private final MultipleCallStackPanel stackPanel;
    private JPanel emptyPanel;
    private final CardLayout cardLayout = new CardLayout();

    ThreadStackVisualizer(ThreadDump descriptor, long startTime) {
        this.descriptor = descriptor;
        this.startTime = startTime;
        stackPanel = MultipleCallStackPanel.createInstance();
        setLayout(cardLayout);
        emptyPanel = new JPanel();
        add(emptyPanel, "empty");//NOI18N
        add(stackPanel, "stack");//NOI18N
        if (descriptor == null || descriptor.getThreadStates().isEmpty()){
            setEmptyContent();
        }else{
            setNonEmptyContent();
        }
    }

    private void setEmptyContent() {
        cardLayout.show(this, "empty");//NOI18N
        emptyPanel.removeAll();
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
        long time = 0;
        if (descriptor != null){
            time = descriptor.getTimestamp();
        }
        String timeString = TimeLineUtils.getMillisValue(time);
        String message = NbBundle.getMessage(ThreadStackVisualizer.class, "ThreadStackVisualizerNoStackAt", timeString); //NOI18N
        JLabel label = new JLabel(message);
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        emptyPanel.add(label);
    }

    private void setNonEmptyContent() {
        stackPanel.clean();
        //and now add all you need
        final long time = ThreadStateColumnImpl.timeStampToMilliSeconds(descriptor.getTimestamp()) - startTime;
        String timeString = TimeLineUtils.getMillisValue(time);
        String rootName = NbBundle.getMessage(ThreadStackVisualizer.class, "ThreadStackVisualizerStackAt", timeString); //NOI18N
        stackPanel.setRootVisible(rootName);
        for (ThreadSnapshot stack : descriptor.getThreadStates()) {
            MSAState msa = stack.getState();
            ThreadStateResources res = ThreadStateResources.forState(msa);
            if (res != null) {
                stackPanel.add(res.name + " " + stack.getThreadInfo().getThreadName(), new ThreadStateIcon(msa, 10, 10), stack.getStack()); // NOI18N
            }

        }
        cardLayout.show(this, "stack");//NOI18N
    }

    void selectRootNode() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    stackPanel.getExplorerManager().setSelectedNodes(new Node[]{stackPanel.getExplorerManager().getRootContext()});
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, 500);
    }

    public String getDisplayName() {
        if (descriptor != null && !descriptor.getThreadStates().isEmpty()) {
            return descriptor.getThreadStates().get(0).getThreadInfo().getThreadName();
        }
        return NbBundle.getMessage(getDefaultContainer().getClass(), "CallStackDetails"); //NOI18N
    }

    public VisualizerConfiguration getVisualizerConfiguration() {
        return new VisualizerConfiguration() {

            public DataModelScheme getSupportedDataScheme() {
                return null;
            }

            public DataTableMetadata getMetadata() {
                return null;
            }

            public String getID() {
                return "CallStack";// NOI18N
            }
        };
    }

    public JComponent getComponent() {
        return this;
    }

    public VisualizerContainer getDefaultContainer() {
        return CallStackTopComponent.findInstance();
    }

    public void refresh() {
    }

    private static final class OpenAction extends AbstractAction {

        private final FunctionCall call;

        private OpenAction(FunctionCall call) {
            super(call.getDisplayedName());
            this.call = call;
        }

        public void actionPerformed(ActionEvent e) {
            OpenInEditor.open(call);
        }
    }
}
