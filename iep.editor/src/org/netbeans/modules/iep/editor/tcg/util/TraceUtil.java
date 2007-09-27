/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
