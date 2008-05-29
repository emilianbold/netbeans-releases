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

package org.netbeans.modules.extexecution.api.input;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extexecution.input.LineParsingHelper;
import org.openide.util.Parameters;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Petr Hejl
 */
public final class InputProcessors {

    private static final Logger LOGGER = Logger.getLogger(InputProcessors.class.getName());

    private InputProcessors() {
        super();
    }

    public static InputProcessor bridge(LineProcessor lineProcessor, Charset charset) {
        return new Bridge(lineProcessor, charset);
    }

    public static InputProcessor proxy(InputProcessor... processors) {
        return new ProxyOutputProcessor(processors);
    }

    public static InputProcessor copying(OutputStream out) {
        return new CopyingOutputProcessor(out);
    }

    public static InputProcessor printing(OutputWriter out, Charset charset, boolean resetEnabled) {
        return new PrintingOutputProcessor(out, charset, resetEnabled);
    }

    private static class Bridge implements InputProcessor {

        private final LineProcessor lineProcessor;

        private final Charset charset;

        private final LineParsingHelper helper = new LineParsingHelper();

        public Bridge(LineProcessor lineProcessor, Charset charset) {
            Parameters.notNull("charset", charset);

            this.lineProcessor = lineProcessor;
            this.charset = charset;
        }

        public final void processInput(byte[] bytes) {
            String[] lines = helper.parse(bytes, charset);
            for (String line : lines) {
                lineProcessor.processLine(line);
            }
        }

        public final void reset() {
            String line = helper.getTrailingLine(true);
            if (line != null) {
                lineProcessor.processLine(line);
            }
            lineProcessor.reset();
        }
    }

    private static class ProxyOutputProcessor implements InputProcessor {

        private final List<InputProcessor> processors = new ArrayList<InputProcessor>();

        public ProxyOutputProcessor(InputProcessor... processors) {
            for (InputProcessor processor : processors) {
                if (processor != null) {
                    this.processors.add(processor);
                }
            }
        }

        public void processInput(byte[] bytes) throws IOException {
            for (InputProcessor processor : processors) {
                processor.processInput(bytes);
            }
        }

        public void reset() throws IOException {
            for (InputProcessor processor : processors) {
                processor.reset();
            }
        }
    }

    private static class PrintingOutputProcessor implements InputProcessor {

        private final OutputWriter out;

        private final Charset charset;

        private final boolean resetEnabled;

        private final LineParsingHelper helper = new LineParsingHelper();

        public PrintingOutputProcessor(OutputWriter out, Charset charset, boolean resetEnabled) {
            assert out != null;

            this.out = out;
            this.charset = charset;
            this.resetEnabled = resetEnabled;
        }

        public void processInput(byte[] bytes) {
            assert bytes != null;

            String[] lines = helper.parse(bytes, charset);
            for (String line : lines) {
                LOGGER.log(Level.FINEST, "{0} \\n", line);

                out.println(line);
                out.flush();
            }
            String line = helper.getTrailingLine(true);
            if (line != null) {
                LOGGER.log(Level.FINEST, line);

                out.print(line);
            }
            out.flush();
        }

        public void reset() throws IOException {
            if (!resetEnabled) {
                return;
            }

            out.reset();
        }
    }

    private static class CopyingOutputProcessor implements InputProcessor {

        private final OutputStream os;

        public CopyingOutputProcessor(OutputStream os) {
            this.os = os;
        }

        public void processInput(byte[] bytes) throws IOException {
            os.write(bytes);
            os.flush();
        }

        public void reset() {
            // noop
        }

    }
}
