/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.commandLine;

/**
 * <h1>Overview</h1>
 *
 * GetOpt provides a general means for a Java program to parse command
 * line arguments in accordance with the standard Unix conventions;
 * it is analogous to, and based on, getopt(3) for C programs.
 * (The following documentation is based on the man page for getopt(3).)

 * <h1>Description</h1>
 *
 * GetOpt is a Java class that provides one method, getopt,
 * and some variables that control behavior of or return additional
 * information from getopt.
 * <p>
 * GetOpt interprets command arguments in accordance with the standard
 * Unix conventions: option arguments of a command are introduced by "-"
 * followed by a key character, and a non-option argument terminates
 * the processing of options.  GetOpt's option interpretation is controlled
 * by its parameter optString, which specifies what characters designate
 * legal options and which of them require associated values.
 * <p>
 * The getopt method returns the next, moving left to right, option letter
 * in the command line arguments that matches a letter in optString.
 * optString must contain the option letters the command using getopt
 * will recognize.  For example, getopt("ab") specifies that the command
 * line should contain no options, only "-a", only "-b", or both "-a" and
 * "-b" in either order.  (The command line can also contain non-option
 * arguments after any option arguments.)  Multiple options per argument
 * are allowed, e.g., "-ab" for the last case above.
 * <p>
 * If a letter in optString is followed by a colon, the option is expected
 * to have an argument.  The argument may or may not be separated by
 * whitespace from the option letter.  For example, getopt("w:") allows
 * either "-w 80" or "-w80".  The variable optArg is set to the option
 * argument, e.g., "80" in either of the previous examples.  Conversion
 * functions such as Integer.parseInt(), etc., can then be applied to
 * optArg.
 * <p>
 * getopt places in the variable optIndex the index of the next command
 * line argument to be processed; optIndex is automatically initialized
 * to 1 before the first call to getopt.
 * <p>
 * When all options have been processed (that is, up to the first
 * non-option argument), getopt returns optEOF (-1).  getopt recognizes the
 * command line argument "--" (i.e., two dashes) to delimit the end of
 * the options; getopt returns optEOF and skips "--".  Subsequent,
 * non-option arguments can be retrieved using the String array passed to
 * main(), beginning with argument number optIndex.
 *
 * <h1>Diagnostics</h1>
 *
 * getopt prints an error message on System.stderr and returns a question
 * mark ('?') when it encounters an option letter in a command line argument
 * that is not included in optString.  Setting the variable optErr to
 * false disables this error message.

 * <h1>Notes</h1>
 *
 * The following notes describe GetOpt's behavior in a few interesting
 * or special cases; these behaviors are consistent with getopt(3)'s
 * behaviors.
 * -- A '-' by itself is treated as a non-option argument.
 * -- If optString is "a:" and the command line arguments are "-a -x",
 *    then "-x" is treated as the argument associated with the "-a".
 * -- Duplicate command line options are allowed; it is up to user to
 *    deal with them as appropriate.
 * -- A command line option like "-b-" is considered as the two options
 *    "b" and "-" (so "-" should appear in option string); this differs
 *    from "-b --".
 * -- Sun and DEC getopt(3)'s differ w.r.t. how "---" is handled.
 *    Sun treats "---" (or anything starting with "--") the same as "--"
 *    DEC treats "---" as two separate "-" options
 *    (so "-" should appear in option string).
 *    Java GetOpt follows the DEC convention.
 * -- An option `letter' can be a letter, number, or most special character.
 *    Like getopt(3), GetOpt disallows a colon as an option letter.
 *
 * @author Anonymous
 *****************************************************************************/
public class GetOpt {

    private String[] theArgs = null;
    private int argCount = 0;
    private String optString = null;

    public GetOpt(String[] args, String opts) {
        theArgs = args;
        argCount = theArgs.length;
        optString = opts;
    }

    // user can toggle this to control printing of error messages
    public boolean optErr = false;

    public int processArg(String arg, int n) {
        int value;
        try {
            value = Integer.parseInt(arg);
        }
        catch (NumberFormatException e) {
            if (optErr)
                System.err.println("processArg cannot process " + arg //NOI18N
                                   + " as an integer"); //NOI18N
            return n;
        }
        return value;
    }

    public int tryArg(int k, int n) {
        int value;
        try {
            value = processArg(theArgs[k], n);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            if (optErr)
                System.err.println("tryArg: no theArgs[" + k + "]"); //NOI18N
            return n;
        }
        return value;
    }

    public long processArg(String arg, long n) {
        long value;
        try {
            value = Long.parseLong(arg);
        }
        catch (NumberFormatException e) {
            if (optErr)
                System.err.println("processArg cannot process " + arg //NOI18N
                                   + " as a long"); //NOI18N
            return n;
        }
        return value;
    }

    public long tryArg(int k, long n) {
        long value;
        try {
            value = processArg(theArgs[k], n);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            if (optErr)
                System.err.println("tryArg: no theArgs[" + k + "]"); //NOI18N
            return n;
        }
        return value;
    }

    public double processArg(String arg, double d) {
        double value;
        try {
            value = Double.valueOf(arg).doubleValue();
        }
        catch (NumberFormatException e) {
            if (optErr)
                System.err.println("processArg cannot process " + arg //NOI18N
                                   + " as a double"); //NOI18N
            return d;
        }
        return value;
    }

    public double tryArg(int k, double d) {
        double value;
        try {
            value = processArg(theArgs[k], d);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            if (optErr)
                System.err.println("tryArg: no theArgs[" + k + "]"); //NOI18N
            return d;
        }
        return value;
    }

    public float processArg(String arg, float f) {
        float value;
        try {
            value = Float.valueOf(arg).floatValue();
        }
        catch (NumberFormatException e) {
            if (optErr)
                System.err.println("processArg cannot process " + arg //NOI18N
                                   + " as a float"); //NOI18N
            return f;
        }
        return value;
    }

    public float tryArg(int k, float f) {
        float value;
        try {
            value = processArg(theArgs[k], f);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            if (optErr)
                System.err.println("tryArg: no theArgs[" + k + "]"); //NOI18N
            return f;
        }
        return value;
    }

    public boolean processArg(String arg, boolean b) {
        // `true' in any case mixture is true; anything else is false
        return Boolean.valueOf(arg).booleanValue();
    }

    public boolean tryArg(int k, boolean b) {
        boolean value;
        try {
            value = processArg(theArgs[k], b);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            if (optErr)
                System.err.println("tryArg: no theArgs[" + k + "]"); //NOI18N
            return b;
        }
        return value;
    }

    public String tryArg(int k, String s) {
        String value;
        try {
            value = theArgs[k];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            if (optErr)
                System.err.println("tryArg: no theArgs[" + k + "]"); //NOI18N
            return s;
        }
        return value;
    }

    private static void writeError(String msg, char ch) {
        System.err.println("GetOpt: " + msg + " -- " + ch); //NOI18N
    }

    public static final int optEOF = -1;

    private int optIndex = 0;

    public int optIndexGet() {
        return optIndex;
    }

    public void optIndexSet(int i) {
        optIndex = i;
    }

    private String optArg = null;

    public String optArgGet() {
        return optArg;
    }

    private int optPosition = 1;

    public int getopt() {
        optArg = null;
        if (theArgs == null || optString == null)
            return optEOF;
        if (optIndex < 0 || optIndex >= argCount)
            return optEOF;
        String thisArg = theArgs[optIndex];
        int argLength = thisArg.length();
        // handle special cases
        if (argLength <= 1 || thisArg.charAt(0) != '-') {
            // e.g., "", "a", "abc", or just "-"
            return optEOF;
        }
        else if (thisArg.equals("--")) {//NOI18N
            // end of non-option args
            optIndex++;
            return optEOF;
        }
        // get next "letter" from option argument
        char ch = thisArg.charAt(optPosition);
        // find this option in optString
        int pos = optString.indexOf(ch);
        if (pos == -1 || ch == ':') {
            if (optErr) {
                writeError("illegal option", ch); //NOI18N
            }
            ch = '?';
        }
        else { // handle colon, if present
            if (pos < optString.length() - 1 && optString.charAt(pos + 1) == ':') {
                if (optPosition != argLength - 1) {
                    // take rest of current arg as optArg
                    optArg = thisArg.substring(optPosition + 1);
                    optPosition = argLength - 1; // force advance to next arg below
                }
                else { // take next arg as optArg
                    optIndex++;
                    if (optIndex < argCount
                            && (theArgs[optIndex].charAt(0) != '-' ||
                            theArgs[optIndex].length() >= 2 &&
                            (optString.indexOf(theArgs[optIndex].charAt(1)) == -1
                            || theArgs[optIndex].charAt(1) == ':'))) {
                        optArg = theArgs[optIndex];
                    }
                    else {
                        if (optErr) {
                            writeError("option requires an argument", ch); //NOI18N
                        }
                        optArg = null;
                        ch = ':'; // Linux man page for getopt(3) says : not ?
                    }
                }
            }
        }
        // advance to next option argument,
        // which might be in thisArg or next arg
        optPosition++;
        if (optPosition >= argLength) {
            optIndex++;
            optPosition = 1;
        }
        return ch;
    }

    public static void main(String[] args) {  // test the class
        GetOpt go = new GetOpt(args, "Uab:f:h:w:");
        go.optErr = true;
        int ch = -1;
        // process options in command line arguments
        boolean usagePrint = false;                 // set
        int aflg = 0;                               // default
        boolean bflg = false;                       // values
        String filename = "out";                    // of
        int width = 80;                             // options
        double height = 1;                          // here
        while ((ch = go.getopt()) != go.optEOF) {
            if ((char)ch == 'U')
                usagePrint = true;
            else if ((char)ch == 'a')
                aflg++;
            else if ((char)ch == 'b')
                bflg = go.processArg(go.optArgGet(), bflg);
            else if ((char)ch == 'f')
                filename = go.optArgGet();
            else if ((char)ch == 'h')
                height = go.processArg(go.optArgGet(), height);
            else if ((char)ch == 'w')
                width = go.processArg(go.optArgGet(), width);
            else
                System.exit(1);                     // undefined option
        }                                           // getopt() returns '?'
        if (usagePrint) {
            System.out.println("Usage: -a -b bool -f file -h height -w width"); //NOI18N
            System.exit(0);
        }
        System.out.println("These are all the command line arguments " + //NOI18N
                           "before processing with GetOpt:"); //NOI18N
        for (int i = 0; i < args.length; i++) {
            System.out.print(" " + args[i]); //NOI18N
        }
        System.out.println();
        System.out.println("-U " + usagePrint); //NOI18N
        System.out.println("-a " + aflg); //NOI18N
        System.out.println("-b " + bflg); //NOI18N
        System.out.println("-f " + filename); //NOI18N
        System.out.println("-h " + height); //NOI18N
        System.out.println("-w " + width); //NOI18N
        // process non-option command line arguments
        for (int k = go.optIndexGet(); k < args.length; k++) {
            System.out.println("normal argument " + k + " is " + args[k]); //NOI18N
        }
    }
}

/* ............... Example compile and run(s)

D:\>javac GetOpt.java

D:\>java GetOpt -aaa -b true -f theFile -w -80 -h3.33 arg1 arg2

These are all the command line arguments before processing with GetOpt:
 -aaa -b true -f theFile -w -80 -h3.33 arg1 arg2
 -U false
 -a 3
 -b true
 -f theFile
 -h 3.33
 -w -80
 normal argument 8 is arg1
 normal argument 9 is arg2

D:\>java GetOpt -aaa -x -w90

GetOpt: illegal option -- x

D:\>java GetOpt -af theFile -w -b true

GetOpt: option requires an argument -- w
                                            ... end of example run(s)  */

