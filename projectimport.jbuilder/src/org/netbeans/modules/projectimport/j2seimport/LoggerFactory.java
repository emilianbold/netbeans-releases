/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.projectimport.j2seimport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logger factory.
 *
 * @author mkrauskopf
 */
public class LoggerFactory {

    private static LoggerFactory factory = new LoggerFactory();

    // used logging level adjustable with "projectimport.logging.level" system
    // property
    private static Level LEVEL = Level.INFO;
    
    // TOTO: mkrauskopf - enhance this
    private static final int MAX_LEVEL_LENGTH = Level.WARNING.getName().length();
    
    /** Returns factory instance. */
    public static LoggerFactory getDefault() {
        return factory;
    }
    
    static {
        String levelName = System.getProperty("projectimport.logging.level"); // NOI18N
        if (levelName != null) {
            try {
                LEVEL = Level.parse(levelName);
            } catch (IllegalArgumentException iae) {
                // use default level - INFO
            }
        }
    }
    
    /**
     * Creates logger for the given class.
     */
    public Logger createLogger(Class clazz) {
        Logger logger = Logger.getLogger(clazz.getName());
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(LEVEL);
        ch.setFormatter(new BasicFormatter());
        logger.addHandler(ch);
        logger.setUseParentHandlers(false);
        logger.setLevel(LEVEL);
        return logger;
    }
    
    
    private static class BasicFormatter extends Formatter {
        private Date dat = new Date();
        private final static String format = "yyyyMMdd_HHmmss z"; // NOI18N
        private DateFormat formatter = new SimpleDateFormat(format);
        
        public String format(LogRecord record) {
            StringBuffer sb = new StringBuffer();
            String name = record.getLevel().getName();
            // indent on the base of severity to have nice and readable output
            for (int i = name.length(); i < MAX_LEVEL_LENGTH; i++) {
                sb.append(" "); // NOI18N
            }
            // append severity
            sb.append("[" + name + "]: "); // NOI18N
            // append date and time (minimize memory allocations here)
            dat.setTime(record.getMillis());
            sb.append(formatter.format(dat));
            sb.append(" - "); // NOI18N
            // append class and method names
            if (record.getSourceClassName() != null) {
                sb.append(record.getSourceClassName());
            } else {
                sb.append(record.getLoggerName());
            }
            if (record.getSourceMethodName() != null) {
                sb.append("."); // NOI18N
                sb.append(record.getSourceMethodName());
                sb.append("()"); // NOI18N
            }
            // append stacktrace if there is any
            sb.append(": "); // NOI18N
            sb.append(record.getMessage());
            if (record.getThrown() != null) {
                try {
                    sb.append("\n  "); // NOI18N
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                }
            } else {
                sb.append("\n"); // NOI18N
            }
            return sb.toString();
        }
    }
    
}
