/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.extexecution;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.WeakHashMap;
import org.openide.windows.InputOutput;

public final class InputOutputManager {

    /**
     * All tabs which were used for some process which has now ended.
     * These are closed when you start a fresh process.
     * Map from tab to tab display name.
     * @see "#43001"
     */
    private static final Map<InputOutput, InputOutputData> AVAILABLE =
            new WeakHashMap<InputOutput, InputOutputData>();

    private InputOutputManager() {
        super();
    }

    // FIXME use InputOutputData
    public static void addInputOutput(InputOutput io, String displayName,
            StopAction stopAction, RerunAction rerunAction) {

        synchronized (AVAILABLE) {
            AVAILABLE.put(io, new InputOutputData(io, displayName, stopAction, rerunAction));
        }
    }

    /**
     * Tries to find free Output Window tab for the given name.
     *
     * @param name the name of the free tab. Other free tabs are ignored.
     * @return free tab and its current display name or <tt>null</tt>
     */
    public static InputOutputData getInputOutput(String name, boolean actions) {
        InputOutputData result = null;

        TreeSet<InputOutputData> candidates = new TreeSet<InputOutputData>();

        synchronized (AVAILABLE) {
            for (Iterator<Entry<InputOutput, InputOutputData>> it = AVAILABLE.entrySet().iterator(); it.hasNext();) {
                Entry<InputOutput, InputOutputData> entry = it.next();

                final InputOutput free = entry.getKey();
                final InputOutputData data = entry.getValue();

                if (free.isClosed()) {
                    it.remove();
                    continue;
                }

                if (isAppropriateName(name, data.displayName)) {
                    if ((actions && data.rerunAction != null && data.stopAction != null)
                            || !actions && data.rerunAction == null && data.stopAction == null) {
                        // Reuse it.
                        candidates.add(data);
                    } // continue to remove all closed tabs
                }
            }
        }

        if (!candidates.isEmpty()) {
            result = candidates.first();
            AVAILABLE.remove(result.inputOutput);
        }
        return result;
    }

    public static InputOutputData getInputOutput(InputOutput inputOutput) {
        InputOutputData result = null;

        synchronized (AVAILABLE) {
            for (Iterator<Entry<InputOutput, InputOutputData>> it = AVAILABLE.entrySet().iterator(); it.hasNext();) {
                Entry<InputOutput, InputOutputData> entry = it.next();

                final InputOutput free = entry.getKey();
                final InputOutputData data = entry.getValue();

                if (free.isClosed()) {
                    it.remove();
                    continue;
                }

                if (free.equals(inputOutput)) {
                    result = data;
                    it.remove();
                }
            }
        }
        return result;
    }

    private static boolean isAppropriateName(String base, String toMatch) {
        if (!toMatch.startsWith(base)) {
            return false;
        }
        return toMatch.substring(base.length()).matches("^(\\ #[0-9]+)?$"); // NOI18N
    }

    public static class InputOutputData implements Comparable<InputOutputData> {

        private final InputOutput inputOutput;

        private final String displayName;

        private final StopAction stopAction;

        private final RerunAction rerunAction;

        public InputOutputData(InputOutput inputOutput, String displayName,
                StopAction stopAction, RerunAction rerunAction) {
            this.displayName = displayName;
            this.stopAction = stopAction;
            this.rerunAction = rerunAction;
            this.inputOutput = inputOutput;
        }

        public InputOutput getInputOutput() {
            return inputOutput;
        }

        public String getDisplayName() {
            return displayName;
        }

        public RerunAction getRerunAction() {
            return rerunAction;
        }

        public StopAction getStopAction() {
            return stopAction;
        }

        public int compareTo(InputOutputData o) {
            return displayName.compareTo(o.displayName);
        }

    }
}
