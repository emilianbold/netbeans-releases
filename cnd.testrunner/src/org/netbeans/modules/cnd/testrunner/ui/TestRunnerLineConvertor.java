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
package org.netbeans.modules.cnd.testrunner.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.RerunHandler;
import org.netbeans.modules.gsf.testrunner.api.TestSession;

/**
 *
 * @author Erno Mononen
 */
public final class TestRunnerLineConvertor implements LineConvertor {

    private static final Logger LOGGER = Logger.getLogger(TestRunnerLineConvertor.class.getName());
    private final Manager manager;
    private TestSession session;
    private final List<TestRecognizerHandler> handlers;

    public TestRunnerLineConvertor(Manager manager, TestSession session, TestHandlerFactory handlerFactory) {
        this.manager = manager;
        this.session = session;
        this.handlers = handlerFactory.createHandlers();
    }

    public synchronized void refreshSession() {
        RerunHandler handler = this.session.getRerunHandler();
        this.session = new TestSession(session.getName(), session.getProject(), session.getSessionType(), session.getNodeFactory());
        session.setRerunHandler(handler);
    }

    public synchronized List<ConvertedLine> convert(String line) {

        for (TestRecognizerHandler handler : handlers) {
            if (handler.matches(line)) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Handler [" + handler + "] matched line: " + line);
                }
                try {
                    handler.updateUI(manager, session);
                    return asConvertedLines(handler.getRecognizedOutput());
                } catch (IllegalStateException ise) {
                    // ISE is thrown when mathing a group fails, should be enough to log a warning
                    LOGGER.log(Level.WARNING, "Failed to process line: " + line + " with handler: " + handler, ise);
                } catch (IndexOutOfBoundsException ioobe) {
                    // IOOBE is thrown when there is no group with the expected index.
                    LOGGER.log(Level.WARNING, "Failed to process line: " + line + " with handler: " + handler, ioobe);
                }
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "No handler for line: " + line);
        }
        session.addOutput(line);
        manager.displayOutput(session, line, false);
        return null;
    }

    private List<ConvertedLine> asConvertedLines(List<String> lines) {
        List<ConvertedLine> result = new ArrayList<ConvertedLine>(lines.size());

        boolean handled = false;
        for (String line : lines) {
//            for (LineConvertor convertor : PythonLineConvertorFactory.getStandardConvertors(session.getFileLocator())) {
//                List<ConvertedLine> converted = convertor.convert(line);
//                if (converted != null) {
//                    result.addAll(converted);
//                    handled = true;
//                    break;
//                }
//            }
            if (!handled) {
                result.add(ConvertedLine.forText(line, null));
            }
        }
        return result;
    }
}
