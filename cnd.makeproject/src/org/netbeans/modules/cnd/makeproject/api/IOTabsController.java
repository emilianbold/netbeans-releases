/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author akrasny
 */
final class IOTabsController {

    private static final IOTabsController instance = new IOTabsController();
    private static final TabsGroupGroupsComparator comparator = new TabsGroupGroupsComparator();
    private final List<TabsGroup> groups = new ArrayList<TabsGroup>();

    public static IOTabsController getDefault() {
        return instance;
    }

    public TabsGroup openTabsGroup(final String groupName, final boolean reuse) {
        synchronized (groups) {
            List<TabsGroup> toRemove = new ArrayList<TabsGroup>();

            // Cleanup obsolete groups
            for (TabsGroup group : groups) {
                if (group.isClosed()) {
                    toRemove.add(group);
                }
            }

            for (TabsGroup group : toRemove) {
                groups.remove(group);
            }

            if (reuse) {
                TabsGroup toReuse = null;
                for (TabsGroup group : groups) {
                    if (group.groupName.equals(groupName)) {
                        if (group.canReuse()) {
                            toReuse = group;
                            break;
                        }
                    }
                }
                if (toReuse != null) {
                    toReuse.closeAll();
                    groups.remove(toReuse);
                }
            }

            int idx = 1;
            for (TabsGroup group : groups) {
                if (!group.groupName.equals(groupName)) {
                    continue;
                }
                if (idx >= group.seqID) {
                    idx++;
                }
            }
            TabsGroup result = new TabsGroup(groupName, idx);
            groups.add(result);
            Collections.sort(groups, comparator);
            return result;
        }
    }

    public void startHandlerInTab(ProjectActionHandler handler, InputOutputTab ioTab) {
        handler.execute(ioTab.inputOutput);
    }

    public static final class TabsGroup {

        private final List<InputOutputTab> tabs = new ArrayList<InputOutputTab>();
        private final String groupName;
        private final int seqID;

        public TabsGroup(final String groupName, final int seqID) {
            this.seqID = seqID;
            this.groupName = groupName;
        }

        private boolean canReuse() {
            synchronized (tabs) {
                for (InputOutputTab tab : tabs) {
                    if (!tab.isOutputClosed()) {
                        return false;
                    }
                }
            }
            return true;
        }

        public InputOutputTab getTab(final IOProvider ioProvider, final String tabName, final Action[] actions) {
            String name = seqID == 1 ? tabName : tabName.concat(" #" + seqID); // NOI18N
            synchronized (tabs) {
                for (InputOutputTab tab : tabs) {
                    if (tab.name.equals(name)) {
                        return tab;
                    }
                }
                InputOutputTab newTab = new InputOutputTab(name, ioProvider.getIO(name, actions));
                tabs.add(newTab);
                return newTab;
            }
        }

        private void closeAll() {
            synchronized (tabs) {
                for (InputOutputTab tab : tabs) {
                    tab.closeOutput();
                    tab.inputOutput.closeInputOutput();
                }
                tabs.clear();
            }
        }

        public void resetIO() {
            synchronized (tabs) {
                for (InputOutputTab tab : tabs) {
                    tab.closeOutput();
                    try {
                        tab.outputWriter.reset();
                    } catch (IOException ex) {
                    }
                }
            }
        }

        private boolean isClosed() {
            synchronized (tabs) {
                for (InputOutputTab tab : tabs) {
                    if (!tab.isOutputClosed() || !tab.inputOutput.isClosed()) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    public static final class InputOutputTab {

        private final String name;
        private final InputOutput inputOutput;
        private final OutputWriter outputWriter;
        private final AtomicBoolean isOutputClosed = new AtomicBoolean(false);

        private InputOutputTab(final String name, final InputOutput inputOutput) {
            this.name = name;
            this.inputOutput = inputOutput;
            this.outputWriter = inputOutput.getOut();
        }

        public String getName() {
            return name;
        }

        public void select() {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    inputOutput.select();
                }
            });
        }

        public void closeOutput() {
            isOutputClosed.set(true);
            outputWriter.close();
        }

        private boolean isOutputClosed() {
            return isOutputClosed.get();
        }
    }

    private static class TabsGroupGroupsComparator implements Comparator<TabsGroup> {

        @Override
        public int compare(TabsGroup o1, TabsGroup o2) {
            return o1.seqID - o2.seqID;
        }
    }
}
