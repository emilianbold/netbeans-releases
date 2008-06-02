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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.extexecution.api.print.ConvertedLine;
import org.netbeans.modules.extexecution.api.print.LineConvertor;
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

    public static InputProcessor bridge(LineProcessor lineProcessor) {
        return new Bridge(lineProcessor);
    }

    public static InputProcessor proxy(InputProcessor... processors) {
        return new ProxyInputProcessor(processors);
    }

    public static InputProcessor copying(Writer writer) {
        return new CopyingInputProcessor(writer);
    }

    public static InputProcessor printing(OutputWriter out, boolean resetEnabled) {
        return printing(out, null, resetEnabled);
    }
    
    public static InputProcessor ansiStripping(InputProcessor delegate) {
        return new AnsiStrippingInputProcessor(delegate);
    }

    public static InputProcessor printing(OutputWriter out, LineConvertor convertor, boolean resetEnabled) {
        return new PrintingInputProcessor(out, convertor, resetEnabled);
    }

    private static class Bridge implements InputProcessor {

        private final LineProcessor lineProcessor;

        private final LineParsingHelper helper = new LineParsingHelper();

        public Bridge(LineProcessor lineProcessor) {
            Parameters.notNull("lineProcessor", lineProcessor);

            this.lineProcessor = lineProcessor;
        }

        public final void processInput(char[] chars) {
            String[] lines = helper.parse(chars);
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

    private static class ProxyInputProcessor implements InputProcessor {

        private final List<InputProcessor> processors = new ArrayList<InputProcessor>();

        public ProxyInputProcessor(InputProcessor... processors) {
            for (InputProcessor processor : processors) {
                if (processor != null) {
                    this.processors.add(processor);
                }
            }
        }

        public void processInput(char[] chars) throws IOException {
            for (InputProcessor processor : processors) {
                processor.processInput(chars);
            }
        }

        public void reset() throws IOException {
            for (InputProcessor processor : processors) {
                processor.reset();
            }
        }
    }

    private static class PrintingInputProcessor implements InputProcessor {

        private final OutputWriter out;

        private final LineConvertor convertor;

        private final boolean resetEnabled;

        private final LineParsingHelper helper = new LineParsingHelper();

        public PrintingInputProcessor(OutputWriter out, LineConvertor convertor,
                boolean resetEnabled) {

            assert out != null;

            this.out = out;
            this.convertor = convertor;
            this.resetEnabled = resetEnabled;
        }

        public void processInput(char[] chars) {
            assert chars != null;

            String[] lines = helper.parse(chars);
            for (String line : lines) {
                LOGGER.log(Level.FINEST, "{0}\\n", line);

                convert(line);
                out.flush();
            }

            String line = helper.getTrailingLine(true);
            if (line != null) {
                LOGGER.log(Level.FINEST, line);

                out.print(line);
                out.flush();
            }
        }

        public void reset() throws IOException {
            if (!resetEnabled) {
                return;
            }

            out.reset();
        }

        private void convert(String line) {
            if (convertor != null) {
                for (ConvertedLine converted : convertor.convert(line)) {
                    if (converted.getListener() == null) {
                        out.println(converted.getText());
                    } else {
                        try {
                            out.println(converted.getText(), converted.getListener());
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                            out.println(converted.getText());
                        }
                    }
                }
            } else {
                out.println(line);
            }
        }
    }

    private static class CopyingInputProcessor implements InputProcessor {

        private final Writer writer;

        public CopyingInputProcessor(Writer writer) {
            this.writer = writer;
        }

        public void processInput(char[] chars) throws IOException {
            LOGGER.log(Level.FINEST, Arrays.toString(chars));
            writer.write(chars);
            writer.flush();
        }

        public void reset() {
            // noop
        }

    }
    
    private static class AnsiStrippingInputProcessor implements InputProcessor {
        
        private final InputProcessor delegate;

        public AnsiStrippingInputProcessor(InputProcessor delegate) {
            this.delegate = delegate;
        }

        public void processInput(char[] chars) throws IOException {
            // FIXME optimize me
            String sequence = new String(chars);
            if (containsAnsiColors(sequence)) {
                sequence = stripAnsiColors(sequence);
            }
            delegate.processInput(sequence.toCharArray());
        }

        public void reset() throws IOException {
            // noop
        }
        
        private static boolean containsAnsiColors(String sequence) {
            // RSpec will color output with ANSI color sequence terminal escapes
            return sequence.indexOf("\033[") != -1; // NOI18N
        }

        private static String stripAnsiColors(String sequence) {
            StringBuilder sb = new StringBuilder(sequence.length());
            int index = 0;
            int max = sequence.length();
            while (index < max) {
                int nextEscape = sequence.indexOf("\033[", index); // NOI18N
                if (nextEscape == -1) {
                    nextEscape = sequence.length();
                }

                for (int n = (nextEscape == -1) ? max : nextEscape; index < n; index++) {
                    sb.append(sequence.charAt(index));
                }

                if (nextEscape != -1) {
                    for (; index < max; index++) {
                        char c = sequence.charAt(index);
                        if (c == 'm') {
                            index++;
                            break;
                        }
                    }
                }
            }

            return sb.toString();
        }        
    }
}
