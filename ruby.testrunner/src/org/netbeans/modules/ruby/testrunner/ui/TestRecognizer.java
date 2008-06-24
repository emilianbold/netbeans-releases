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
package org.netbeans.modules.ruby.testrunner.ui;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer;
import org.netbeans.modules.ruby.testrunner.ui.TestSession.SessionType;

/**
 *
 * @author Erno Mononen
 */
public final class TestRecognizer extends OutputRecognizer {

    private static final Logger LOGGER = Logger.getLogger(TestRecognizer.class.getName());
    
    private final Manager manager;
    private final TestSession session;
    private final FileLocator fileLocator;
    private final SessionType sessionType;
            
    private final List<TestRecognizerHandler> handlers;

    public TestRecognizer(Manager manager, 
            FileLocator fileLocator, 
            List<TestRecognizerHandler> handlers, 
            SessionType sessionType) {
        
        this.manager = manager;
        this.fileLocator = fileLocator;
        this.handlers = handlers;
        this.sessionType = sessionType;
        this.session = new TestSession(fileLocator, sessionType);
    }

    @Override
    public void start() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Session starting: " + session);
        }
        manager.testStarted(session);
    }

    @Override
    public RecognizedOutput processLine(String line) {
        
        for (TestRecognizerHandler handler : handlers) {
            if (handler.matches(line)) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Handler [" + handler + "] matched line: " + line);
                }

                handler.updateUI(manager, session);
                return handler.getRecognizedOutput();
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "No handler for line: " + line);
        }

        manager.displayOutput(session, line, false);
        return null;
    }

    @Override
    public void finish() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Session finished: " + session);
        }
        manager.sessionFinished(session);
    }
}
