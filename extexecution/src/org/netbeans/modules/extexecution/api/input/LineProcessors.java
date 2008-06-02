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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.extexecution.api.print.ConvertedLine;
import org.netbeans.modules.extexecution.api.print.LineConvertor;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Petr Hejl
 */
public final class LineProcessors {

    private static final Logger LOGGER = Logger.getLogger(LineProcessors.class.getName());

    private LineProcessors() {
        super();
    }

    public static LineProcessor proxy(LineProcessor... processors) {
        return new ProxyLineProcessor(processors);
    }

    public static LineProcessor printing(OutputWriter out, boolean resetEnabled) {
        return printing(out, null, resetEnabled);
    }
    
    public static LineProcessor printing(OutputWriter out, LineConvertor convertor, boolean resetEnabled) {
        return new PrintingLineProcessor(out, convertor, resetEnabled);
    }    

    public static LineProcessor patternWaiting(Pattern pattern, CountDownLatch latch) {
        return new WaitingLineProcessor(pattern, latch);
    }

    private static class ProxyLineProcessor implements LineProcessor {

        private final List<LineProcessor> processors = new ArrayList<LineProcessor>();

        public ProxyLineProcessor(LineProcessor... processors) {
            for (LineProcessor processor : processors) {
                if (processor != null) {
                    this.processors.add(processor);
                }
            }
        }

        public void processLine(String line) {
            for (LineProcessor processor : processors) {
                processor.processLine(line);
            }
        }

        public void reset() {
            for (LineProcessor processor : processors) {
                processor.reset();
            }
        }

    }

    private static class PrintingLineProcessor implements LineProcessor {

        private final OutputWriter out;
        
        private final LineConvertor convertor;

        private final boolean resetEnabled;

        public PrintingLineProcessor(OutputWriter out, LineConvertor convertor, boolean resetEnabled) {
            assert out != null;

            this.out = out;
            this.convertor = convertor;
            this.resetEnabled = resetEnabled;
        }

        public void processLine(String line) {
            assert line != null;

            LOGGER.log(Level.FINEST, line);

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
            out.flush();
        }

        public void reset() {
            if (!resetEnabled) {
                return;
            }

            try {
                out.reset();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

    }

    private static class WaitingLineProcessor implements LineProcessor {

        private final Pattern pattern;

        private final CountDownLatch latch;

        public WaitingLineProcessor(Pattern pattern, CountDownLatch latch) {
            assert pattern != null;
            assert latch != null;

            this.pattern = pattern;
            this.latch = latch;
        }

        public void processLine(String line) {
            assert line != null;

            if (pattern.matcher(line).matches()) {
                latch.countDown();
            }
        }

        public void reset() {
            // noop
        }
    }
}
