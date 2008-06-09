/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.extexecution.api;

import java.io.Writer;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Descriptor for the execution environment. To build the most common kind
 * of descriptor use {@link ExecutionDescriptorBuilder}.
 *
 * @author Petr Hejl
 * @see ExecutionDescriptorBuilder
 */
public interface ExecutionDescriptor {

    /**
     * Returns the <i>custom</i> io to use. May return <code>null</code>
     * which means that client is fine with infrustructure provided io (visible
     * as tab in output pane).
     *
     * @return the <i>custom</i> io to use; may return <code>null</code>
     */
    InputOutput getInputOutput();

    /**
     * Returns <code>true</code> if the control buttons (rerun, stop) should
     * be available in io tab.
     * <p>
     * Note that this property has no meaning when custom io
     * ({@link #getInputOutput}) is used.
     *
     * @return <code>true</code> if the control buttons (rerun, stop) should
     *             be available in io tab
     */
    boolean isControlable();

    /**
     * Returns <code>true</code> if the io should be selected before
     * the execution.
     *
     * @return <code>true</code> if the io should be selected before
     *             the execution
     */
    boolean isFrontWindow();

    /**
     * Returns <code>true</code> if the input from user is allowed.
     *
     * @return <code>true</code> if the input from user is allowed
     */
    boolean isInputVisible();

    /**
     * Returns <code>true</code> if progress bar should be visible.
     *
     * @return <code>true</code> if progress bar should be visible
     */
    boolean showProgress();

    /**
     * Returns <code>true</code> if progress bar should suspended to just
     * "running" message.
     *
     * @return <code>true</code> if progress bar should suspended to just
     *             "running" message
     */
    boolean showSuspended();

    /**
     * Returns the processor processing the data available on standard output.
     *
     * @param writer io writer used by {@link ExecutionService} for standard output
     * @return the processor processing the data available on standard output
     * @see org.netbeans.modules.extexecution.api.input.InputProcessors
     */
    InputProcessor getOutProcessor(OutputWriter writer);

    /**
     * Returns the processor processing the data available on standard error output.
     *
     * @param writer io writer used by {@link ExecutionService} for standard error output
     * @return the processor processing the data available on standard error output
     * @see org.netbeans.modules.extexecution.api.input.InputProcessors
     */
    InputProcessor getErrProcessor(OutputWriter writer);

    /**
     * Returns the processor processing the data available from user (io) as
     * input to the process.
     * <p>
     * Typically the processor should forward data to the writer (process).
     *
     * @param writer writer for standard input of the process
     * @return the processor processing the data available from user (io) as
     *             input to the process
     * @see org.netbeans.modules.extexecution.api.input.InputProcessors
     */
    InputProcessor getInProcessor(Writer writer);

    /**
     * Returns the runnable to execute <i>before</i> the external execution itself;
     * may return <code>null</code>.
     *
     * @return the runnable to execute <i>before</i> the external execution itself;
     *             may return <code>null</code>
     */
    Runnable getPreExecution();

    /**
     * Returns the runnable to execute <i>after</i> the external execution itself;
     * may return <code>null</code>.
     *
     * @return the runnable to execute <i>after</i> the external execution itself;
     *             may return <code>null</code>
     */
    Runnable getPostExecution();

    /**
     * Returns the condition to control the possibility of the rerun action;
     * may return <code>null</code>.
     *
     * @return the condition to control the possibility of the rerun action;
     *             may return <code>null</code>
     */
    RerunCondition getRerunCondition();

    /**
     * Represents the possibility of reruning the action.
     */
    interface RerunCondition {

        /**
         * Adds a listener to listen for the change in rerun possibility state.
         *
         * @param listener listener that will listen for changes in rerun possibility
         */
        void addChangeListener(ChangeListener listener);

        /**
         * Removes previously registered listener.
         *
         * @param listener listener to remove
         */
        void removeChangeListener(ChangeListener listener);

        /**
         * Returns <code>true</code> if it is possible to execute the action again.
         *
         * @return <code>true</code> if it is possible to execute the action again
         */
        boolean isRerunPossible();

    }
}
