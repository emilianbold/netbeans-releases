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
import org.netbeans.modules.extexecution.api.input.InputProcessor;
import org.netbeans.modules.extexecution.api.input.InputProcessors;
import org.netbeans.modules.extexecution.api.print.LineConvertor;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Petr Hejl
 */
public final class ExecutionDescriptorBuilder {

    private final DescriptorData descriptorData = new DescriptorData();

    public ExecutionDescriptorBuilder() {
        super();
    }

    public ExecutionDescriptorBuilder inputOutput(InputOutput io) {
        descriptorData.inputOutput = io;
        return this;
    }

    public ExecutionDescriptorBuilder controllable(boolean controllable) {
        descriptorData.controllable = controllable;
        return this;
    }

    public ExecutionDescriptorBuilder frontWindow(boolean frontWindow) {
        descriptorData.front = frontWindow;
        return this;
    }

    public ExecutionDescriptorBuilder inputVisible(boolean inputVisible) {
        descriptorData.input = inputVisible;
        return this;
    }

    public ExecutionDescriptorBuilder showProgress(boolean showProgress) {
        descriptorData.progress = showProgress;
        return this;
    }

    public ExecutionDescriptorBuilder showSuspend(boolean showSuspend) {
        descriptorData.suspend = showSuspend;
        return this;
    }

    public ExecutionDescriptorBuilder outProcessor(InputProcessor outProcessor) {
        descriptorData.outProcessor = outProcessor;
        return this;
    }

    public ExecutionDescriptorBuilder errProcessor(InputProcessor errProcessor) {
        descriptorData.errProcessor = errProcessor;
        return this;
    }

    public ExecutionDescriptorBuilder outConvertor(LineConvertor convertor) {
        descriptorData.outConvertor = convertor;
        return this;
    }

    public ExecutionDescriptorBuilder errConvertor(LineConvertor convertor) {
        descriptorData.errConvertor = convertor;
        return this;
    }

    public ExecutionDescriptorBuilder preExecution(Runnable preExcetion) {
        descriptorData.preExecution = preExcetion;
        return this;
    }

    public ExecutionDescriptorBuilder postExecution(Runnable postExecution) {
        descriptorData.postExecution = postExecution;
        return this;
    }

    public ExecutionDescriptorBuilder rerunCondition(ExecutionDescriptor.RerunCondition rerunCondition) {
        descriptorData.rerunCondition = rerunCondition;
        return this;
    }

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

        public boolean isControlable() {
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

        public InputProcessor getOutProcessor(OutputWriter writer) {
            InputProcessor outProcessor = InputProcessors.ansiStripping(
                    InputProcessors.printing(writer, descriptorData.outConvertor, true));
            if (descriptorData.outProcessor != null) {
                outProcessor = InputProcessors.proxy(outProcessor, descriptorData.outProcessor);
            }

            return outProcessor;
        }

        public InputProcessor getErrProcessor(OutputWriter writer) {
            InputProcessor errProcessor = InputProcessors.ansiStripping(
                    InputProcessors.printing(writer, descriptorData.errConvertor, false));
            if (descriptorData.errProcessor != null) {
                errProcessor = InputProcessors.proxy(errProcessor, descriptorData.errProcessor);
            }

            return errProcessor;
        }

        public InputProcessor getInProcessor(Writer writer) {
            return InputProcessors.copying(writer);
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
            this.rerunCondition = rerunCondition;
        }

    }
}
