/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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
        } else {
            sb.append(argument.toString());
        }
    }

    
}
