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

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.stack.StackTrace;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadState;
import org.netbeans.modules.dlight.api.storage.threadmap.ThreadStateColumn;
import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.spi.visualizer.Visualizer;
import org.netbeans.modules.dlight.spi.visualizer.VisualizerContainer;
import org.netbeans.modules.dlight.visualizers.CallStackTopComponent;
import org.netbeans.modules.dlight.visualizers.threadmap.ThreadStateColumnImpl.StateResources;

/**
 *
 * @author Alexander Simon
 */
public class ThreadStackVisualizer extends JPanel implements Visualizer<VisualizerConfiguration> {
    private final long time;
    private final ThreadsDataManager manager;
    private final ThreadStateColumn stateCol;
    private final ThreadState state;
    private final String threadName;
    private final boolean isFullMode;
    private final int detailedState;
    private final List<Integer> filteredThreads;
    private final int index;

    ThreadStackVisualizer(ThreadsDataManager manager, int row, int index, List<Integer> filteredThreads, boolean isFullMode, int detailedState) {
        this.manager = manager;
        this.index = index;
        stateCol = manager.getThreadData(row);
        state = stateCol.getThreadStateAt(index);
        threadName = manager.getThreadName(row);
        time = ThreadStateColumnImpl.timeStampToMilliSeconds(state.getTimeStamp()) - manager.getStartTime();
        this.isFullMode = isFullMode;
        this.detailedState = detailedState;
        this.filteredThreads = filteredThreads;
        init();
    }

    @Override
    public String getName(){
        return threadName;
    }

    private void init(){
        setLayout(new BorderLayout());
        JScrollPane pane = new JScrollPane();
        add(pane, BorderLayout.CENTER);
        StringBuilder buf = new StringBuilder();
        buf.append("<html>");// NOI18N
        buf.append("Time ");// NOI18N
        buf.append(TimeLineUtils.getMillisValue(time));
        printStack(buf, state);
        for(Integer i : filteredThreads){
            if (i.intValue() != index) {

            }
        }
        buf.append("</html>");// NOI18N
        pane.setViewportView(new JLabel(buf.toString()));
    }

    private void printStack(StringBuilder buf, ThreadState aState) {
        StackTrace stack; // = state.getSamplingStackTrace();
        buf.append("<p>"); // NOI18N
        StateResources res = ThreadStateColumnImpl.getThreadStateResources(aState.getSamplingMSAState(isFullMode));
        if (res != null) {
            buf.append("<font bgcolor=#"); // NOI18N
            buf.append(Integer.toHexString(res.color.getRed()));
            buf.append(Integer.toHexString(res.color.getGreen()));
            buf.append(Integer.toHexString(res.color.getBlue()));
            buf.append(">&nbsp;&nbsp;"); // NOI18N
            buf.append("</font>"); // NOI18N
        }
        buf.append(" "); // NOI18N
        buf.append(aState.getSamplingMSAState(isFullMode).name());
        buf.append(" "); // NOI18N
        buf.append(threadName);
        for (int i = 0; i < 5; i++) {
            buf.append("<p>"); // NOI18N
            buf.append("&nbsp;&nbsp;"); // NOI18N
            buf.append("<font color=blue>"); // NOI18N
            buf.append("MyClass.Method" + i + "()"); // NOI18N
            buf.append("</font>"); // NOI18N
        }
    }


    public VisualizerConfiguration getVisualizerConfiguration() {
        return new VisualizerConfiguration(){
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
}
