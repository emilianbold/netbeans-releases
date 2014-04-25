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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.extexecution.input;

import org.netbeans.modules.extexecution.input.BaseInputProcessor;
import org.netbeans.modules.extexecution.input.DelegatingInputProcessor;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.modules.extexecution.input.LineParsingHelper;
import org.openide.windows.OutputWriter;

/**
 * Factory methods for {@link InputProcessor} classes.
 *
 * @author Petr Hejl
 */
public final class InputProcessors {

    private static final Logger LOGGER = Logger.getLogger(InputProcessors.class.getName());

    private InputProcessors() {
        super();
    }

    /**
     * Returns the processor converting characters to the whole lines passing
     * them to the given line processor.
     * <p>
     * Any reset or close is delegated to the corresponding method
     * of line processor.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param lineProcessor processor consuming parsed lines
     * @return the processor converting characters to the whole lines
     * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputProcessors#bridge(org.netbeans.api.extexecution.base.input.LineProcessor)}
     */
    @NonNull
    public static InputProcessor bridge(@NonNull LineProcessor lineProcessor) {
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessors.bridge(new LineProcessors.BaseLineProcessor(lineProcessor)));
    }

    /**
     * Returns the processor acting as a proxy.
     * <p>
     * Any action taken on this processor is distributed to all processors
     * passed as arguments in the same order as they were passed to this method.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param processors processor to which the actions will be ditributed
     * @return the processor acting as a proxy
     * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputProcessors#proxy(org.netbeans.api.extexecution.base.input.InputProcessor...)}
     */
    @NonNull
    public static InputProcessor proxy(@NonNull InputProcessor... processors) {
        org.netbeans.api.extexecution.base.input.InputProcessor[] wrapped = new org.netbeans.api.extexecution.base.input.InputProcessor[processors.length];
        for (int i = 0; i < processors.length; i++) {
            if (processors[i] != null) {
                wrapped[i] = new BaseInputProcessor(processors[i]);
            } else {
                wrapped[i] = null;
            }
        }
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessors.proxy(wrapped));
    }

    /**
     * Returns the processor that writes every character passed for processing
     * to the given writer.
     * <p>
     * Reset action on the returned processor is noop. Processor closes the
     * writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param writer processed characters will be written to this writer
     * @return the processor that writes every character passed for processing
     *             to the given writer
     * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputProcessors#copying(java.io.Writer)}
     */
    @NonNull
    public static InputProcessor copying(@NonNull Writer writer) {
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessors.copying(writer));
    }

    /**
     * Returns the processor printing all characters passed for processing to
     * the given output writer.
     * <p>
     * Reset action on the returned processor resets the writer if it is enabled
     * by passing <code>true</code> as <code>resetEnabled</code>. Processor
     * closes the output writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param out where to print received characters
     * @param resetEnabled determines whether the reset operation will work
     *             (will reset the writer if so)
     * @return the processor printing all characters passed for processing to
     *             the given output writer
     */
    @NonNull
    public static InputProcessor printing(@NonNull OutputWriter out, boolean resetEnabled) {
        return printing(out, null, resetEnabled);
    }

    /**
     * Returns the processor converting <i>whole</i> lines with convertor and
     * printing the result including unterminated tail (if present) to the
     * given output writer. If the covertor does not handle line passed to it
     * (returning <code>null</code>) raw lines are printed.
     * <p>
     * Reset action on the returned processor resets the writer if it is enabled
     * by passing <code>true</code> as <code>resetEnabled</code>. Processor
     * closes the output writer on {@link InputProcessor#close()}.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param out where to print converted lines and characters
     * @param convertor convertor converting the <i>whole</i> lines
     *             before printing, may be <code>null</code>
     * @param resetEnabled determines whether the reset operation will work
     *             (will reset the writer if so)
     * @return the processor converting the <i>whole</i> lines with convertor and
     *             printing the result including unterminated tail (if present)
     *             to the given output writer
     * @see LineConvertor
     */
    @NonNull
    public static InputProcessor printing(@NonNull OutputWriter out, @NullAllowed LineConvertor convertor, boolean resetEnabled) {
        return new PrintingInputProcessor(out, convertor, resetEnabled);
    }

    /**
     * Returns the processor that strips any
     * <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape sequences</a>
     * and passes the result to the delegate.
     * <p>
     * Reset and close methods on the returned processor invokes
     * the corresponding actions on delegate.
     * <p>
     * Returned processor is <i> not thread safe</i>.
     *
     * @param delegate processor that will receive characters without control
     *             sequences
     * @return the processor that strips any ansi escape sequences and passes
     *             the result to the delegate
     * @deprecated use {@link org.netbeans.api.extexecution.base.input.InputProcessors#ansiStripping(org.netbeans.api.extexecution.base.input.InputProcessor)}
     */
    @NonNull
    public static InputProcessor ansiStripping(@NonNull InputProcessor delegate) {
        return new DelegatingInputProcessor(org.netbeans.api.extexecution.base.input.InputProcessors.ansiStripping(new BaseInputProcessor(delegate)));
    }

    private static class PrintingInputProcessor implements InputProcessor {

        private final OutputWriter out;

        private final LineConvertor convertor;

        private final boolean resetEnabled;

        private final LineParsingHelper helper = new LineParsingHelper();

        private boolean closed;

        public PrintingInputProcessor(OutputWriter out, LineConvertor convertor,
                boolean resetEnabled) {

            assert out != null;

            this.out = out;
            this.convertor = convertor;
            this.resetEnabled = resetEnabled;
        }

        public void processInput(char[] chars) {
            assert chars != null;

            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

// TODO this does not color standard error lines :(
//            if (convertor == null) {
//                out.print(String.valueOf(chars));
//                return;
//            }

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
            if (closed) {
                throw new IllegalStateException("Already closed processor");
            }

            if (!resetEnabled) {
                return;
            }

            out.reset();
        }

        public void close() throws IOException {
            closed = true;

            out.close();
        }

        private void convert(String line) {
            if (convertor == null) {
                out.println(line);
                return;
            }

            List<ConvertedLine> convertedLines = convertor.convert(line);
            if (convertedLines == null) {
                out.println(line);
                return;
            }

            for (ConvertedLine converted : convertedLines) {
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
        }
    }
    
}
