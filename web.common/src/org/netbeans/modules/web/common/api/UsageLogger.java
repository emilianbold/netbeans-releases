/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.common.api;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Helper class for <a href="http://wiki.netbeans.org/UsageLoggingSpecification">usage logging</a>.
 * <p>
 * This class is thread safe.
 * @since 1.59
 */
public final class UsageLogger {

    private final Logger logger;
    private final Class<?> srcClass;
    private final String message;
    private final boolean unrepeated;
    private final AtomicBoolean messageLogged = new AtomicBoolean();


    private UsageLogger(Builder builder) {
        assert builder != null;
        assert builder.loggerName != null;
        this.logger = Logger.getLogger(builder.loggerName);
        this.srcClass = builder.srcClass;
        this.message = builder.message;
        this.unrepeated = builder.unrepeated;
    }

    /**
     * Reset this logger to its initial state.
     * <p>
     * Currently, <tt>unrepeated</tt> flag is cleared so the next
     * message is logged.
     */
    public void reset() {
        messageLogged.set(false);
    }

    /**
     * Log the given message.
     * @param srcClass source class used to get {@link LogRecord#setResourceBundle(java.util.ResourceBundle) resource bundle}
     *        and its {@link LogRecord#setResourceBundleName(java.lang.String) name}
     * @param message message
     * @param params message parameters
     */
    public void log(Class<?> srcClass, String message, String... params) {
        Parameters.notNull("srcClass", srcClass); // NOI18N
        Parameters.notNull("message", message); // NOI18N
        logInternal(srcClass, message, params);
    }

    /**
     * Log the default message.
     * @param params message parameters
     */
    public void log(String... params) {
        if (srcClass == null) {
            throw new IllegalStateException("No srcClass set");
        }
        if (message == null) {
            throw new IllegalStateException("No message set");
        }
        logInternal(srcClass, message, params);
    }

    private void logInternal(Class<?> srcClass, String message, String... params) {
        assert srcClass != null;
        assert message != null;
        if (!isLoggingEnabled()) {
            return;
        }
        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(logger.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        logRecord.setParameters(params);
        logger.log(logRecord);
    }

    boolean isLoggingEnabled() {
        if (!unrepeated) {
            return true;
        }
        return messageLogged.compareAndSet(false, true);
    }

    //~ Factories

    /**
     * Create usage logger for project browser.
     * @param loggerName name of usage logger name, e.g. &quot;org.netbeans.ui.metrics.php"
     * @return unrepeated usage logger for project browser.
     */
    public static UsageLogger projectBrowserUsageLogger(String loggerName) {
        return new Builder(loggerName)
                .message(UsageLogger.class, "USG_PROJECT_BROWSER") // NOI18N
                .unrepeated(true)
                .create();
    }

    /**
     * Create usage logger for JS test run.
     * @param loggerName name of usage logger name, e.g. &quot;org.netbeans.ui.metrics.php"
     * @return unrepeated usage logger for JS test run.
     * @since 1.70
     */
    public static UsageLogger jsTestRunUsageLogger(String loggerName) {
        return new Builder(loggerName)
                .message(UsageLogger.class, "USG_TEST_RUN_JS") // NOI18N
                .unrepeated(true)
                .create();
    }

    //~ Inner classes

    /**
     * Builder for {@link UsageLogger}.
     */
    public static final class Builder {

        private final String loggerName;

        private Class<?> srcClass;
        private String message;
        private boolean unrepeated;


        /**
         * Create new builder.
         * @param loggerName name of usage logger name, e.g. &quot;org.netbeans.ui.metrics.php"
         */
        public Builder(String loggerName) {
            Parameters.notNull("loggerName", loggerName); // NOI18N
            this.loggerName = loggerName;
        }

        /**
         * Set usage logger to unrepeated, it means that only the first message
         * will be logged until logger is {@link UsageLogger#reset() reset}.
         * @param unrepeated {@code true} for unrepeated logger
         * @return configured builder instance.
         */
        public Builder unrepeated(boolean unrepeated) {
            this.unrepeated = unrepeated;
            return this;
        }

        /**
         * Set default message, used in {@link UsageLogger#log(java.lang.String[])}.
         * @param srcClass source class used to get {@link LogRecord#setResourceBundle(java.util.ResourceBundle) resource bundle}
         *        and its {@link LogRecord#setResourceBundleName(java.lang.String) name}
         * @param message message
         * @return configured builder instance.
         */
        public Builder message(Class<?> srcClass, String message) {
            Parameters.notNull("srcClass", srcClass); // NOI18N
            Parameters.notNull("message", message); // NOI18N
            this.srcClass = srcClass;
            this.message = message;
            return this;
        }

        /**
         * Create usage logger.
         * @return usage logger instance.
         */
        public UsageLogger create() {
            return new UsageLogger(this);
        }

    }

}
