/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.editor.tcg.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Description of the Class
 *
 * @author Bing Lu
 *
 * @since November 6, 2002
 */
public class TraceUtil {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TraceUtil.class.getName());

    /**
     * Description of the Field
     */
    public static final String P_SYSTEM_LINE_SEPARATOR = "line.separator";

    /**
     * Description of the Field
     */
    public static final String SYSTEM_LINE_SEPARATOR =
        System.getProperty(P_SYSTEM_LINE_SEPARATOR);

    /**
     * Gets the callee attribute of the TraceUtil class
     *
     * @return The callee value
     */
    public static String getCallee() {

        StringBuffer buf = new StringBuffer();
        Exception e = new Exception();

        e.fillInStackTrace();

        StackTraceElement[] elem = e.getStackTrace();
        String calleeClassName = elem[1].getClassName();
        String calleeShortName = calleeClassName.substring(Math.max(0,
                                     calleeClassName.lastIndexOf('.') + 1));
        int calleeLineNumber = elem[1].getLineNumber();
        String calleeMethodName = elem[1].getMethodName();

        buf.append("<trace>");
        buf.append(SYSTEM_LINE_SEPARATOR);
        buf.append("\t<callee class=\"");
        buf.append(calleeShortName);
        buf.append("\" method=\"");
        buf.append(calleeMethodName);
        buf.append("\" line#=\"");
        buf.append(calleeLineNumber);
        buf.append("\"/>");
        buf.append(SYSTEM_LINE_SEPARATOR);
        buf.append("</trace>");

        return buf.toString();
    }

    /**
     * Gets the caller attribute of the TraceUtil class
     *
     * @return The caller value
     */
    public static String getCaller() {

        StringBuffer buf = new StringBuffer();
        Exception e = new Exception();

        e.fillInStackTrace();

        StackTraceElement[] elem = e.getStackTrace();
        String callerClassName = elem[2].getClassName();
        String callerShortName = callerClassName.substring(Math.max(0,
                                     callerClassName.lastIndexOf('.') + 1));
        int callerLineNumber = elem[2].getLineNumber();
        String callerMethodName = elem[2].getMethodName();

        buf.append("<trace>");
        buf.append(SYSTEM_LINE_SEPARATOR);
        buf.append("\t<caller class=\"");
        buf.append(callerShortName);
        buf.append("\" method=\"");
        buf.append(callerMethodName);
        buf.append("\" line#=\"");
        buf.append(callerLineNumber);
        buf.append("\"/>");
        buf.append(SYSTEM_LINE_SEPARATOR);
        buf.append("</trace>");

        return buf.toString();
    }

    /**
     * Gets the stackTrace attribute of the TraceUtil class
     *
     * @return The stackTrace value
     */
    public static String getStackTrace() {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Exception e = new Exception();

        e.fillInStackTrace();
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();

        return sw.toString();
    }

    /**
     * Gets the trace attribute of the TraceUtil class
     *
     * @param depth Description of the Parameter
     *
     * @return The trace value
     */
    public static String getTrace(int depth) {

        StringBuffer buf = new StringBuffer();
        Exception e = new Exception();

        e.fillInStackTrace();

        StackTraceElement[] elem = e.getStackTrace();
        String calleeClassName = elem[1].getClassName();
        String calleeShortName = calleeClassName.substring(Math.max(0,
                                     calleeClassName.lastIndexOf('.') + 1));
        int calleeLineNumber = elem[1].getLineNumber();
        String calleeMethodName = elem[1].getMethodName();

        buf.append("<trace>");
        buf.append(SYSTEM_LINE_SEPARATOR);
        buf.append("\t<callee class=\"");
        buf.append(calleeShortName);
        buf.append("\" method=\"");
        buf.append(calleeMethodName);
        buf.append("\" line#=\"");
        buf.append(calleeLineNumber);
        buf.append("\"/>");
        buf.append(SYSTEM_LINE_SEPARATOR);

        for (int i = 2; i < Math.min(elem.length, depth + 2); i++) {
            String callerClassName = elem[i].getClassName();
            String callerShortName = callerClassName.substring(Math.max(0,
                                         callerClassName.lastIndexOf('.') + 1));
            String callerFileName = elem[i].getFileName();
            int callerLineNumber = elem[i].getLineNumber();
            String callerMethodName = elem[i].getMethodName();

            buf.append("\t<caller class=\"");
            buf.append(callerShortName);
            buf.append("\" method=\"");
            buf.append(callerMethodName);
            buf.append("\" line#=\"");
            buf.append(callerLineNumber);
            buf.append("\"/>");
            buf.append(SYSTEM_LINE_SEPARATOR);
        }

        buf.append("</trace>");

        return buf.toString();
    }
    
    /**
     * the main
     * @param args the args
     */
    public static void main(String[] args) {
        mLog.info(org.netbeans.modules.iep.editor.tcg.util.TraceUtil.getTrace(1));
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/
