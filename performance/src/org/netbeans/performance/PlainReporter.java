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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance;

/**
 * A reporter that will directly print out the results in raw form.
 *
 * @author  Petr Nejedly
 */
public class PlainReporter implements Reporter {

    /** Creates new PlainReporter */
    public PlainReporter() {
    }

    public void flush() {
        System.out.flush();
    }

    public void addSample(String className, String methodName, Object argument, float value) {
        System.out.println( className + '.' + methodName + "@" +
        argument2String(argument) + ": " + formatTime( value ) );
    }
    
    /** Formats a time */
    private static String formatTime(float time) {        
        if (time < 1e-3) {
            return (time * 1e6) + "[micro s]";
        } else if (time < 1) {
            return (time * 1e3) + "[ms]";
        } else {
            return time + "[s]";
        }        
    }

    /** Handles arrays */
    private static String argument2String( Object argument ) {
        StringBuffer sb = new StringBuffer(1000);
        argument2String(argument, sb);
        return sb.toString();
    }

    private static void argument2String( Object argument, StringBuffer sb ) {
        if (argument instanceof Object[]) {
            Object[] arg = (Object[]) argument;
            sb.append('[');
            for (int i = 0; i < arg.length - 1; i++) {
                argument2String(arg[i], sb);
                sb.append(',').append(' ');
            }
            argument2String(arg[arg.length - 1], sb);
            sb.append(']');
        } else if (argument instanceof int[]) {
            int[] arg = (int[]) argument;
            sb.append('[');
            for (int i = 0; i < arg.length - 1; i++) {
                sb.append(Integer.toString(arg[i]));
                sb.append(',').append(' ');
            }
            sb.append(Integer.toString(arg[arg.length - 1]));
            sb.append(']');
        } else {
            sb.append(argument.toString());
        }
    }

    
}
