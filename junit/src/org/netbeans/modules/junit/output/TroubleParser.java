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

package org.netbeans.modules.junit.output;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.netbeans.modules.junit.output.Report.Trouble;
import static org.netbeans.modules.junit.output.RegexpUtils.NESTED_EXCEPTION_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.CALLSTACK_LINE_PREFIX;
import static org.netbeans.modules.junit.output.RegexpUtils.CALLSTACK_LINE_PREFIX_CATCH;
        

/**
 *
 * @author  Marian Petras
 */
final class TroubleParser {

    /** */
    private static final boolean DONE = true;
    /** */
    private static final boolean WANT_MORE = false;

    /** */
    private final Trouble trouble;
    /** */
    private final RegexpUtils regexp;
    /** */
    private final boolean callstackOnly;

    /** */
    private Trouble currTrouble;
    /** */
    private List<String> callstackBuffer;
    /** */
    private int emptyLines = 0;
    /** */
    private boolean isHiddenComparisonFailure;

    /**
     */
    TroubleParser(Trouble trouble, RegexpUtils regexp) {
        this.trouble = trouble;
        this.regexp = regexp;
        this.callstackOnly = (trouble.exceptionClsName != null);

        currTrouble = trouble;
    }

    /**
     * Processes a single line of output.
     * 
     * @param  msg  line to be processed
     * @return  <code>{@value #DONE}</code> if parsing of the current
     *          trouble is finished, <code>{@value #WANT_MORE}</code>
     *          otherwise
     */
    boolean processMessage(String msg) {
        Matcher matcher;
        if (trouble.exceptionClsName == null) {
            matcher = regexp.getTestcaseExceptionPattern().matcher(msg);
            if (matcher.matches()) {
                trouble.exceptionClsName = matcher.group(1);
                trouble.message = matcher.group(2);     //possibly null

                isHiddenComparisonFailure = false;
                return WANT_MORE;
            }
        }
        
        if (msg.length() == 0) {
            if ((trouble.exceptionClsName != null) && (callstackBuffer != null)
                    || (++emptyLines == 2)) {
                finishProcessing();
                return DONE;
            }
        } else {
            emptyLines = 0;
        }
        
        if ((trouble.exceptionClsName != null)
                && (callstackBuffer != null)
                && msg.startsWith(NESTED_EXCEPTION_PREFIX)) {
            matcher = regexp.getNestedExceptionPattern().matcher(
                            msg.substring(NESTED_EXCEPTION_PREFIX.length()));
            if (matcher.matches()) {
                fixateStackTrace();
                
                Trouble nestedTrouble = new Trouble(false);
                nestedTrouble.exceptionClsName = matcher.group(1);
                nestedTrouble.message = matcher.group(2);
                
                currTrouble.nestedTrouble = nestedTrouble;
                currTrouble = nestedTrouble;
                return WANT_MORE;
            }
        }

        String trimmed = RegexpUtils.specialTrim(msg);
        if (trimmed.startsWith(CALLSTACK_LINE_PREFIX_CATCH)) {
            trimmed = trimmed.substring(CALLSTACK_LINE_PREFIX_CATCH.length());
        }
        if (trimmed.startsWith(CALLSTACK_LINE_PREFIX)) {
            matcher = regexp.getCallstackLinePattern().matcher(msg);
            if (matcher.matches()) {
                if (isHiddenComparisonFailure
                        && (trouble.exceptionClsName == null)) {
                    trouble.exceptionClsName = Trouble.COMPARISON_FAILURE_JUNIT4;
                }
                if (callstackBuffer == null) {
                    callstackBuffer = new ArrayList<String>(8);
                }
                callstackBuffer.add(
                        trimmed.substring(CALLSTACK_LINE_PREFIX.length()));
                return WANT_MORE;
            }
        }

        if (!callstackOnly
                && (currTrouble == trouble)         //not nested exception
                && (callstackBuffer == null)) {     //before callstack only

            if ((trouble.exceptionClsName == null) 
                    && (trouble.message == null)) {
                int positionOfExp = msg.indexOf("expected:<");          //NOI18N
                if (positionOfExp != -1) {
                    isHiddenComparisonFailure = true;
                    if ((positionOfExp == 5)
                             && msg.startsWith("null ")) {              //NOI18N
                         msg = msg.substring(5);
                    }
                }
            }

            trouble.message = (trouble.message != null)
                              ? trouble.message + '\n' + msg
                              : msg;
        }
        /* else: just ignore the text */
        return WANT_MORE;
    }

    /**
     */
    void finishProcessing() {
        if (callstackBuffer != null) {
            fixateStackTrace();
        }
        if (isHiddenComparisonFailure && (trouble.exceptionClsName == null)) {
            trouble.exceptionClsName = Trouble.COMPARISON_FAILURE_JUNIT4;
        }
    }

    /**
     */
    private void fixateStackTrace() {
        currTrouble.stackTrace = callstackBuffer.toArray(
                                new String[callstackBuffer.size()]);
        callstackBuffer = null;
    }
    
}
