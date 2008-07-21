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

import javax.swing.event.ChangeListener;
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.print.LineConvertor;
import org.openide.windows.InputOutput;

/**
 * Descriptor for the execution environment. To build the descriptor
 * use {@link Builder}.
 *
 * @author Petr Hejl
 * @see Builder
 */
public final class ExecutionDescriptor {

    private Runnable preExecution;

    private Runnable postExecution;

    private boolean suspend;

    private boolean progress;

    private boolean front;

    private boolean input;

    private boolean controllable;

    private LineConvertorFactory outConvertorFactory;

    private LineConvertorFactory errConvertorFactory;

    private InputProcessorFactory outProcessorFactory;

    private InputProcessorFactory errProcessorFactory;

    private InputOutput inputOutput;

    private RerunCondition rerunCondition;

    private String optionsPath;

    private ExecutionDescriptor(Builder builder) {
        this.preExecution = builder.preExecution;
        this.postExecution = builder.postExecution;
        this.suspend = builder.suspend;
        this.progress = builder.progress;
        this.front = builder.front;
        this.input = builder.input;
        this.controllable = builder.controllable;
        this.outConvertorFactory = builder.outConvertorFactory;
        this.errConvertorFactory = builder.errConvertorFactory;
        this.outProcessorFactory = builder.outProcessorFactory;
        this.errProcessorFactory = builder.errProcessorFactory;
        this.inputOutput = builder.inputOutput;
        this.rerunCondition = builder.rerunCondition;
        this.optionsPath = builder.optionsPath;
    }

    /**
     * Returns the <i>custom</i> io to use. May return <code>null</code>
     * which means that client is fine with infrustructure provided io (visible
     * as tab in output pane).
     * <p>
     * If returned value is not <code>null</code> methods {@link #isControllable()},
     * {@link #getRerunCondition()} and {@link #getOptionsPath()} have
     * no meaning and are ignored.
     *
     * @return the <i>custom</i> io to use; may return <code>null</code>
     */
    public InputOutput getInputOutput() {
        return inputOutput;
    }

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
    public boolean isControllable() {
        return controllable;
    }

    /**
     * Returns <code>true</code> if the io should be selected before
     * the execution.
     *
     * @return <code>true</code> if the io should be selected before
     *             the execution
     */
    public boolean isFrontWindow() {
        return front;
    }

    /**
     * Returns <code>true</code> if the input from user is allowed.
     *
     * @return <code>true</code> if the input from user is allowed
     */
    public boolean isInputVisible() {
        return input;
    }

    /**
     * Returns <code>true</code> if progress bar should be visible.
     *
     * @return <code>true</code> if progress bar should be visible
     */
    public boolean showProgress() {
        return progress;
    }

    /**
     * Returns <code>true</code> if progress bar should suspended to just
     * "running" message.
     *
     * @return <code>true</code> if progress bar should suspended to just
     *             "running" message
     */
    public boolean showSuspended() {
        return suspend;
    }

    /**
     * Returns the factory for additional processor to use for standard output.
     * {@link ExecutionService} automatically uses the printing processor
     * created by {@link org.netbeans.modules.extexecution.api.input.InputProcessors#printing(org.openide.windows.OutputWriter, boolean)}.
     *
     * @return the factory for additional processor to use for standard output
     */
    public InputProcessorFactory getOutProcessorFactory() {
        return outProcessorFactory;
    }

    /**
     * Returns the factory for additional processor to use for standard error output.
     * {@link ExecutionService} automatically uses the the printing processor
     * created by {@link org.netbeans.modules.extexecution.api.input.InputProcessors#printing(org.openide.windows.OutputWriter, boolean)}.
     *
     * @return the factory for additional processor to use for standard error output
     */
    public InputProcessorFactory getErrProcessorFactory() {
        return errProcessorFactory;
    }

    /**
     * Returns the factory for convertor to use with processor printing the standard
     * output (that used by {@link ExecutionService} automatically.
     *
     * @return the factory for convertor to use with processor printing
     *             the standard output
     */
    public LineConvertorFactory getOutConvertorFactory() {
        return outConvertorFactory;
    }

    /**
     * Returns the factory for convertor to use with processor printing the standard
     * error output (that used by {@link ExecutionService} automatically.
     *
     * @return the factory for convertor to use with processor printing
     *             the standard error output
     */
    public LineConvertorFactory getErrConvertorFactory() {
        return errConvertorFactory;
    }

    /**
     * Returns the runnable to execute <i>before</i> the external execution itself;
     * may return <code>null</code>.
     *
     * @return the runnable to execute <i>before</i> the external execution itself;
     *             may return <code>null</code>
     */
    public Runnable getPreExecution() {
        return preExecution;
    }

    /**
     * Returns the runnable to execute <i>after</i> the external execution itself;
     * may return <code>null</code>.
     *
     * @return the runnable to execute <i>after</i> the external execution itself;
     *             may return <code>null</code>
     */
    public Runnable getPostExecution() {
        return postExecution;
    }

    /**
     * Returns the condition to control the possibility of the rerun action;
     * may return <code>null</code>.
     *
     * @return the condition to control the possibility of the rerun action;
     *             may return <code>null</code>
     */
    public RerunCondition getRerunCondition() {
        return rerunCondition;
    }

    /**
     * Returns the options path, may be <code>null</code>. If not
     * <code>null</code> the {@link ExecutionService} will display the button
     * in the output tab displaying the proper options when pressed.
     * <p>
     * Format of the parameter is described in
     * {@link org.netbeans.api.options.OptionsDisplayer#open(java.lang.String)}.
     *
     * @return the options path if any, may be <code>null</code>
     */
    public String getOptionsPath() {
        return optionsPath;
    }

    /**
     * Represents the possibility of reruning the action.
     */
    public interface RerunCondition {

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

    /**
     * Factory creating the input processor.
     */
    public interface InputProcessorFactory {

        /**
         * Creates and returns new input processor.
         *
         * @return new input processor
         */
        InputProcessor newInputProcessor();

    }

    /**
     * Factory creating the line covertor.
     */
    public interface LineConvertorFactory {

        /**
         * Creates and returns new line convertor.
         *
         * @return new line convertor
         */
        LineConvertor newLineConvertor();

    }

    /**
     * This class is used to create execution descriptors.
     * <p>
     * This class in <i>not thread safe</i>.
     *
     * @author Petr Hejl
     */
    public static final class Builder {

        private Runnable preExecution;

        private Runnable postExecution;

        private boolean suspend;

        private boolean progress;

        private boolean front;

        private boolean input;

        private boolean controllable;

        private LineConvertorFactory outConvertorFactory;

        private LineConvertorFactory errConvertorFactory;

        private InputProcessorFactory outProcessorFactory;

        private InputProcessorFactory errProcessorFactory;

        private InputOutput inputOutput;

        private ExecutionDescriptor.RerunCondition rerunCondition;

        private String optionsPath;

        /**
         * Creates the new builder. All properites of the builder are configured
         * to <code>false</code> or <code>null</code>.
         */
        public Builder() {
            super();
        }

        /**
         * Sets this builder's custom io. ExecutionDescriptor subsequently created
         * by {@link #create()} method will return this io on
         * {@link ExecutionDescriptor#getInputOutput()}.
         *
         * @param io custom input output, <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getInputOutput()
         */
        public Builder inputOutput(InputOutput io) {
            this.inputOutput = io;
            return this;
        }

        /**
         * Sets this builder's controllable flag. ExecutionDescriptor subsequently
         * created by {@link #create()} method will return this flag on
         * {@link ExecutionDescriptor#isControllable()}.
         *
         * @param controllable controllable flag
         * @return this descriptor builder
         * @see ExecutionDescriptor#isControllable()
         */
        public Builder controllable(boolean controllable) {
            this.controllable = controllable;
            return this;
        }

        /**
         * Sets this builder's front window flag. ExecutionDescriptor subsequently
         * created by {@link #create()} method will return this flag on
         * {@link ExecutionDescriptor#isFrontWindow()}.
         *
         * @param frontWindow front window flag
         * @return this descriptor builder
         * @see ExecutionDescriptor#isFrontWindow()
         */
        public Builder frontWindow(boolean frontWindow) {
            this.front = frontWindow;
            return this;
        }

        /**
         * Sets this builder's input visible flag. ExecutionDescriptor subsequently
         * created by {@link #create()} method will return this flag on
         * {@link ExecutionDescriptor#isInputVisible()}.
         *
         * @param inputVisible input visible flag
         * @return this descriptor builder
         * @see ExecutionDescriptor#isInputVisible()
         */
        public Builder inputVisible(boolean inputVisible) {
            this.input = inputVisible;
            return this;
        }

        /**
         * Sets this builder's show progress flag. ExecutionDescriptor subsequently
         * created by {@link #create()} method will return this flag on
         * {@link ExecutionDescriptor#showProgress()}.
         *
         * @param showProgress show progress flag
         * @return this descriptor builder
         * @see ExecutionDescriptor#showProgress()
         */
        public Builder showProgress(boolean showProgress) {
            this.progress = showProgress;
            return this;
        }

        /**
         * Sets this builder's show suspend flag. ExecutionDescriptor subsequently
         * created by {@link #create()} method will return this flag on
         * {@link ExecutionDescriptor#showSuspended()}.
         *
         * @param showSuspended show suspended
         * @return this descriptor builder
         * @see ExecutionDescriptor#showSuspended()
         */
        public Builder showSuspended(boolean showSuspended) {
            this.suspend = showSuspended;
            return this;
        }

        /**
         * Sets this builder's factory for standard output processor. ExecutionDescriptor
         * subsequently created by {@link #create()} method will return this
         * factory on {@link ExecutionDescriptor#getOutProcessorFactory()}.
         *
         * @param outProcessorFactory factory for standard output processor,
         *             <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getOutProcessorFactory()
         */
        public Builder outProcessorFactory(InputProcessorFactory outProcessorFactory) {
            this.outProcessorFactory = outProcessorFactory;
            return this;
        }

        /**
         * Sets this builder's factory for standard error output processor. ExecutionDescriptor
         * subsequently created by {@link #create()} method will return this
         * factory on {@link ExecutionDescriptor#getErrProcessorFactory()}.
         *
         * @param errProcessorFactory factory for standard error output processor,
         *             <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getErrProcessorFactory()
         */
        public Builder errProcessorFactory(InputProcessorFactory errProcessorFactory) {
            this.errProcessorFactory = errProcessorFactory;
            return this;
        }

        /**
         * Sets this builder's factory for convertor for standard output. ExecutionDescriptor
         * subsequently created by {@link #create()} method will return this
         * convertor on {@link ExecutionDescriptor#getOutConvertorFactory()}.
         *
         * @param convertorFactory factory for convertor for standard output,
         *             <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getOutConvertorFactory()
         */
        public Builder outConvertorFactory(LineConvertorFactory convertorFactory) {
            this.outConvertorFactory = convertorFactory;
            return this;
        }

        /**
         * Sets this builder's factory for convertor for standard error output.
         * ExecutionDescriptor subsequently created by {@link #create()} method
         * will return this convertor on {@link ExecutionDescriptor#getErrConvertorFactory()}.
         *
         * @param convertorFactory factory for convertor for standard error output,
         *             <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getErrConvertorFactory()
         */
        public Builder errConvertorFactory(LineConvertorFactory convertorFactory) {
            this.errConvertorFactory = convertorFactory;
            return this;
        }

        /**
         * Sets this builder's pre execution runnable. ExecutionDescriptor
         * subsequently created by {@link #create()} method will return this
         * runnable on {@link ExecutionDescriptor#getPreExecution()}.
         *
         * @param preExecution pre exceution runnable, <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getPreExecution()
         */
        public Builder preExecution(Runnable preExecution) {
            this.preExecution = preExecution;
            return this;
        }

        /**
         * Sets this builder's post execution runnable. ExecutionDescriptor
         * subsequently created by {@link #create()} method will return this
         * runnable on {@link ExecutionDescriptor#getPostExecution()}.
         *
         * @param postExecution post execution runnable, <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getPostExecution()
         */
        public Builder postExecution(Runnable postExecution) {
            this.postExecution = postExecution;
            return this;
        }

        /**
         * Sets this builder's rerun condition. ExecutionDescriptor
         * subsequently created by {@link #create()} method will return this
         * condition on {@link ExecutionDescriptor#getRerunCondition()}.
         *
         * @param rerunCondition rerun condition, <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getRerunCondition()
         */
        public Builder rerunCondition(ExecutionDescriptor.RerunCondition rerunCondition) {
            this.rerunCondition = rerunCondition;
            return this;
        }

        /**
         * Sets this builder's options path. ExecutionDescriptor
         * subsequently created by {@link #create()} method will return this
         * path on {@link ExecutionDescriptor#getOptionsPath()}.
         *
         * @param optionsPath options path, <code>null</code> allowed
         * @return this descriptor builder
         * @see ExecutionDescriptor#getOptionsPath()
         */
        public Builder optionsPath(String optionsPath) {
            this.optionsPath = optionsPath;
            return this;
        }

        /**
         * Creates the new {@link ExecutionDescriptor} based on the properties
         * configured in this builder.
         *
         * @return the new {@link ExecutionDescriptor} based on the properties
         *             configured in this builder
         */
        public ExecutionDescriptor create() {
            return new ExecutionDescriptor(this);
        }
    }

}
