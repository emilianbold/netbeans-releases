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

import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.print.LineConvertor;
import org.openide.windows.InputOutput;

/**
 * This class is used to create execution descriptors.
 * <p>
 * This class in <i>not thread safe</i>.
 *
 * @author Petr Hejl
 * @see ExecutionDescriptor
 */
public final class ExecutionDescriptorBuilder {

    private final DescriptorData descriptorData = new DescriptorData();

    /**
     * Creates the new builder. All properites of the builder are configured
     * to <code>false</code> or <code>null</code>.
     */
    public ExecutionDescriptorBuilder() {
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
    public ExecutionDescriptorBuilder inputOutput(InputOutput io) {
        descriptorData.inputOutput = io;
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
    public ExecutionDescriptorBuilder controllable(boolean controllable) {
        descriptorData.controllable = controllable;
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
    public ExecutionDescriptorBuilder frontWindow(boolean frontWindow) {
        descriptorData.front = frontWindow;
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
    public ExecutionDescriptorBuilder inputVisible(boolean inputVisible) {
        descriptorData.input = inputVisible;
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
    public ExecutionDescriptorBuilder showProgress(boolean showProgress) {
        descriptorData.progress = showProgress;
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
    public ExecutionDescriptorBuilder showSuspended(boolean showSuspended) {
        descriptorData.suspend = showSuspended;
        return this;
    }

    /**
     * Sets this builder's standard output processor. ExecutionDescriptor
     * subsequently created by {@link #create()} method will return this
     * processor on {@link ExecutionDescriptor#getOutProcessor()}.
     *
     * @param outProcessor processor for standard output, <code>null</code> allowed
     * @return this descriptor builder
     * @see ExecutionDescriptor#getOutProcessor()
     */
    public ExecutionDescriptorBuilder outProcessor(InputProcessor outProcessor) {
        descriptorData.outProcessor = outProcessor;
        return this;
    }

    /**
     * Sets this builder's standard error output processor. ExecutionDescriptor
     * subsequently created by {@link #create()} method will return this
     * processor on {@link ExecutionDescriptor#getErrProcessor()}.
     *
     * @param errProcessor processor for standard error output, <code>null</code> allowed
     * @return this descriptor builder
     * @see ExecutionDescriptor#getErrProcessor()
     */
    public ExecutionDescriptorBuilder errProcessor(InputProcessor errProcessor) {
        descriptorData.errProcessor = errProcessor;
        return this;
    }

    /**
     * Sets this builder's convertor for standard output. ExecutionDescriptor
     * subsequently created by {@link #create()} method will return this
     * convertor on {@link ExecutionDescriptor#getOutConvertor()}.
     *
     * @param convertor convertor for standard output, <code>null</code> allowed
     * @return this descriptor builder
     * @see ExecutionDescriptor#getOutConvertor()
     */
    public ExecutionDescriptorBuilder outConvertor(LineConvertor convertor) {
        descriptorData.outConvertor = convertor;
        return this;
    }

    /**
     * Sets this builder's convertor for standard error output. ExecutionDescriptor
     * subsequently created by {@link #create()} method will return this
     * convertor on {@link ExecutionDescriptor#getErrConvertor()}.
     *
     * @param convertor convertor for standard error output, <code>null</code> allowed
     * @return this descriptor builder
     * @see ExecutionDescriptor#getErrConvertor()
     */
    public ExecutionDescriptorBuilder errConvertor(LineConvertor convertor) {
        descriptorData.errConvertor = convertor;
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
    public ExecutionDescriptorBuilder preExecution(Runnable preExecution) {
        descriptorData.preExecution = preExecution;
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
    public ExecutionDescriptorBuilder postExecution(Runnable postExecution) {
        descriptorData.postExecution = postExecution;
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
    public ExecutionDescriptorBuilder rerunCondition(ExecutionDescriptor.RerunCondition rerunCondition) {
        descriptorData.rerunCondition = rerunCondition;
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
        return new Descriptor(descriptorData);
    }

    private static class Descriptor implements ExecutionDescriptor {

        private final DescriptorData descriptorData;

        public Descriptor(DescriptorData descriptorData) {
            this.descriptorData = new DescriptorData(descriptorData);
        }

        public InputOutput getInputOutput() {
            return descriptorData.inputOutput;
        }

        public boolean isControllable() {
            return descriptorData.controllable;
        }

        public boolean isFrontWindow() {
            return descriptorData.front;
        }

        public boolean isInputVisible() {
            return descriptorData.input;
        }

        public boolean showProgress() {
            return descriptorData.progress;
        }

        public boolean showSuspended() {
            return descriptorData.suspend;
        }

        public LineConvertor getErrConvertor() {
            return descriptorData.errConvertor;
        }

        public InputProcessor getErrProcessor() {
            return descriptorData.errProcessor;
        }

        public LineConvertor getOutConvertor() {
            return descriptorData.outConvertor;
        }

        public InputProcessor getOutProcessor() {
            return descriptorData.outProcessor;
        }

        public Runnable getPreExecution() {
            return descriptorData.preExecution;
        }

        public Runnable getPostExecution() {
            return descriptorData.postExecution;
        }

        public RerunCondition getRerunCondition() {
            return descriptorData.rerunCondition;
        }

    }

    private static class DescriptorData {

        private Runnable preExecution;

        private Runnable postExecution;

        private boolean suspend;

        private boolean progress;

        private boolean front;

        private boolean input;

        private boolean controllable;

        private LineConvertor outConvertor;

        private LineConvertor errConvertor;

        private InputProcessor outProcessor;

        private InputProcessor errProcessor;

        private InputOutput inputOutput;

        private ExecutionDescriptor.RerunCondition rerunCondition;

        public DescriptorData() {
            super();
        }

        public DescriptorData(DescriptorData data) {
            this.preExecution = data.preExecution;
            this.postExecution = data.postExecution;
            this.suspend = data.suspend;
            this.progress = data.progress;
            this.front = data.front;
            this.input = data.input;
            this.controllable = data.controllable;
            this.outConvertor = data.outConvertor;
            this.errConvertor = data.errConvertor;
            this.outProcessor = data.outProcessor;
            this.errProcessor = data.errProcessor;
            this.inputOutput = data.inputOutput;
            this.rerunCondition = data.rerunCondition;
        }

    }
}
