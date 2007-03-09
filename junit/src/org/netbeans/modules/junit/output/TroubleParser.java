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
    private Trouble currTrouble;
    /** */
    private List<String> callstackBuffer;

    /**
     */
    TroubleParser(Trouble trouble, RegexpUtils regexp) {
        this.trouble = trouble;
        this.regexp = regexp;
        
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
    boolean processMessage(final String msg) {
        Matcher matcher;
        if (trouble.exceptionClsName == null) {
            matcher = regexp.getTestcaseExceptionPattern().matcher(msg);
            if (matcher.matches()) {
                trouble.exceptionClsName = matcher.group(1);
                String exceptionMsg = matcher.group(2);
                if (exceptionMsg != null) {
                    trouble.message = exceptionMsg;
                }
            }
            return WANT_MORE;     //ignore other texts until
                                  //we get exception class name
        }
        
        String trimmed = RegexpUtils.specialTrim(msg);
        if (trimmed.length() == 0) {
            finishProcessing();
            return DONE;
        }
        
        if (msg.startsWith(NESTED_EXCEPTION_PREFIX)) {
            if (callstackBuffer != null) {
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
        } else {
            if (trimmed.startsWith(CALLSTACK_LINE_PREFIX_CATCH)) {
                trimmed = trimmed.substring(CALLSTACK_LINE_PREFIX_CATCH.length());
            }
            if (trimmed.startsWith(CALLSTACK_LINE_PREFIX)) {
                matcher = regexp.getCallstackLinePattern().matcher(msg);
                if (matcher.matches()) {
                    if (callstackBuffer == null) {
                        callstackBuffer = new ArrayList<String>(8);
                    }
                    callstackBuffer.add(
                            trimmed.substring(CALLSTACK_LINE_PREFIX.length()));
                    return WANT_MORE;
                }
            }
        }
        if ((callstackBuffer == null) && (currTrouble.message != null)) {
            currTrouble.message = currTrouble.message + '\n' + msg;
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
    }

    /**
     */
    private void fixateStackTrace() {
        currTrouble.stackTrace = callstackBuffer.toArray(
                                new String[callstackBuffer.size()]);
        callstackBuffer = null;
    }
    
}
