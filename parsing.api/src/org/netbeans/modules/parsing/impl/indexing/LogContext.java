/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
public class LogContext {

    public enum EventType {
        PATH,
        FILE,
        INDEXER,
        MANAGER,
        UI
    }

    public static LogContext create(
        @NonNull EventType eventType,
        @NullAllowed final String message) {
        return create(eventType, message, null);
    }

    public static LogContext create(
        @NonNull EventType eventType,
        @NullAllowed final String message,
        @NullAllowed final LogContext parent) {
        return new LogContext(
            eventType,
            Thread.currentThread().getStackTrace(),
            message,
            parent);
    }

    @Override
    public String toString() {
        final StringBuilder msg = new StringBuilder();
        createLogMessage(msg);
        return msg.toString();
    }

    void log() {
        final LogRecord r = new LogRecord(Level.INFO, LOG_MESSAGE); //NOI18N
        r.setParameters(new Object[]{this});
        r.setResourceBundle(NbBundle.getBundle(LogContext.class));
        r.setResourceBundleName(LogContext.class.getPackage().getName() + ".Bundle"); //NOI18N
        r.setLoggerName(LOG.getName());
        final Exception e = new Exception("Scan canceled.");    //NOI18N
        e.setStackTrace(stackTrace);
        r.setThrown(e);
        LOG.log(r);
    }

    synchronized void absorb(@NonNull final LogContext other) {
        Parameters.notNull("other", other); //NOI18N
        if (absorbed == null) {
            absorbed = new ArrayDeque<LogContext>();
        }
        absorbed.add(other);
    }

    private final EventType eventType;
    private final String message;
    private final StackTraceElement[] stackTrace;
    private final LogContext parent;
    //@GuardedBy("this")
    private Queue<LogContext> absorbed;

    private LogContext(
        @NonNull final EventType eventType,
        @NonNull final StackTraceElement[] stackTrace,
        @NullAllowed final String message,
        @NullAllowed final LogContext parent) {
        Parameters.notNull("eventType", eventType);     //NOI18N
        Parameters.notNull("stackTrace", stackTrace);   //NOI18N
        this.eventType = eventType;
        this.stackTrace = stackTrace;
        this.message = message;
        this.parent = parent;
    }

    private void createLogMessage(@NonNull final StringBuilder sb) {
        sb.append("Type:").append(eventType);   //NOI18N
        if (message != null) {
            sb.append(" Description:").append(message); //NOI18N
        }
        sb.append('\n');    //NOI18N
        for (StackTraceElement se : stackTrace) {
            sb.append(se).append('\n'); //NOI18N
        }
        if (parent != null) {
            sb.append("Parent {");  //NOI18N
            parent.createLogMessage(sb);
            sb.append("}\n"); //NOI18N
        }
        if (absorbed != null) {
            sb.append("Absorbed {");    //NOI18N
            for (LogContext a : absorbed) {
                a.createLogMessage(sb);
            }
            sb.append("}\n");             //NOI18N
        }
    }

    private static final Logger LOG = Logger.getLogger(LogContext.class.getName());
    private static final String LOG_MESSAGE = "SCAN_CANCELLED"; //NOI18N
}
