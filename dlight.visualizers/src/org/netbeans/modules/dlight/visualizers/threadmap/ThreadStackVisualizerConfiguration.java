/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.api.visualizer.TableBasedVisualizerConfiguration;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.api.ThreadState.MSAState;
import org.netbeans.modules.dlight.core.stack.datacollector.CpuSamplingSupport;
import org.netbeans.modules.dlight.visualizers.api.ThreadStateResources;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author mt154047
 */
public final class ThreadStackVisualizerConfiguration implements TableBasedVisualizerConfiguration {

    public static enum ExpansionMode {
        ExpandAll,
        ExpandCurrent,
        CollapseAll
    }

    public static final String ID = "ThreadStackVisualizerConfiguration.id";//NOI18N
    private ThreadDump threadDump;
    private long dumpTime;
    private StackNameProvider stackNameProvider;
    private long preferredSelection;
    private ThreadStackActionsProvider actionsProvider;
    private ExpansionMode expansionMode;

    public ThreadStackVisualizerConfiguration(long dumpTime, ThreadDump threadDump, StackNameProvider stackNameProvider, long preferredSelection, ThreadStackActionsProvider actionsProvider) {
        this.dumpTime = dumpTime;
        this.threadDump = threadDump;
        this.stackNameProvider = stackNameProvider;
        this.preferredSelection = preferredSelection;
        this.actionsProvider = actionsProvider;
        int i = NbPreferences.forModule(ThreadStackVisualizerConfiguration.class).getInt("expansionMode", 1);//NOI18N
        i = Math.max(Math.min(i,2),0);
        expansionMode = ExpansionMode.values()[i];
    }

    void update(ThreadStackVisualizerConfiguration another){
        this.dumpTime = another.dumpTime;
        this.threadDump = another.threadDump;
        this.stackNameProvider = another.stackNameProvider;
        this.preferredSelection = another.preferredSelection;
        this.actionsProvider = another.actionsProvider;
        this.expansionMode = another.expansionMode;
    }

    ThreadDump getThreadDump() {
        return threadDump;
    }

    long getDumpTime() {
        return dumpTime;
    }

    ThreadStackActionsProvider getStackNodeActionsProvider(){
        return actionsProvider;
    }

    StackNameProvider getStackNameProvider() {
        if (stackNameProvider == null) {
            return defaultStackNameProvider;
        }
        return stackNameProvider;
    }

    long getPreferredSelection(){
        return preferredSelection;
    }

    ExpansionMode getPrefferedExpansion(){
        return expansionMode;
    }

    public DataModelScheme getSupportedDataScheme() {
        return DataModelSchemeProvider.getInstance().getScheme("model:stack");//NOI18N
    }

    public String getID() {
        return ID;
    }

    public DataTableMetadata getMetadata() {
        return CpuSamplingSupport.CPU_SAMPLE_TABLE;
    }

    private StackNameProvider defaultStackNameProvider = new StackNameProvider(){
        public String getStackName(ThreadSnapshot snapshot) {
            String name = "";
            MSAState msa = snapshot.getState();
            ThreadStateResources res = ThreadStateResources.forState(msa);
            if (res != null) {
                name = res.name;
            }
            long time = ThreadStateColumnImpl.timeInervalToMilliSeconds(snapshot.getTimestamp());
            String at = TimeLineUtils.getMillisValue(time);
            return NbBundle.getMessage(ThreadStackVisualizerConfiguration.class, "ThreadStackVisualizerStackAt1",  //NOI18N
                    name, snapshot.getThreadInfo().getThreadName(), at);
        }
    };

    public interface StackNameProvider {
        String getStackName(ThreadSnapshot snapshot);
    }

}
