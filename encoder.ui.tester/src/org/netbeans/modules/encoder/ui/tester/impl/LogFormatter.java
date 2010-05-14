/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.encoder.ui.tester.impl;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Special log formatter for decode/encode output in verbose mode. Compared
 * with the SimpleFormatter, it does not print timestamp and log level related
 * information.
 *
 * @author Lixin Tang
 */
public class LogFormatter extends SimpleFormatter {

    /** Line separator string.  This is the value of the line.separator
     * property at the moment that the SimpleFormatter was created.
     */
    private String lineSeparator = System.getProperty("line.separator");

    @Override
    public synchronized String format(final LogRecord record) {
        StringBuffer sb = new StringBuffer();
        // Minimize memory allocations here.
//        String srcClassName = record.getSourceClassName();
//        if (srcClassName != null) {
//            int dot = srcClassName.lastIndexOf(".");
//            if (dot > 0) {
//                srcClassName = srcClassName.substring(dot + 1);
//            }
//            sb.append(srcClassName);
//        } else {
//            sb.append(record.getLoggerName());
//        }
//        if (record.getSourceMethodName() != null) {
//            sb.append("#");
//            sb.append(record.getSourceMethodName()).append("()");
//        }
        /*
        String debugLevel = record.getLevel().getLocalizedName();
        sb.append(" [").append(debugLevel).append("]");
        */
//        sb.append(":");
//        sb.append(lineSeparator);
        String message = formatMessage(record);
        sb.append(message);
        sb.append(lineSeparator);
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
                //no-op
            }
        }
        sb.append(lineSeparator);
        return sb.toString();
    }
}
