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
 *
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
 *
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.util;

import java.io.*;

/**
 * Handles the logging of communication to and from the server
 *
 * @author Robert Greig
 * @author Petr Kuzel rewriten to streams
 */
public final class Logger {
    /**
     * The output stream to use to write communication sent to the server
     */
    private static OutputStream outLogStream;

    /**
     * The output stream to use to write communication received from the server
     */
    private static OutputStream inLogStream;

    /**
     * The log files path. If the property is set to the constant "system"
     * then it uses System.err, otherwise it tries to create a file at the
     * specified path
     */
    private static final String LOG_PROPERTY = "cvsClientLog"; // NOI18N

    /**
     * Whether we are logging or not
     */
    private static boolean logging;

    static {
        setLogging(System.getProperty(LOG_PROPERTY));
    }

    public static void setLogging(String logPath) {
        logging = (logPath != null);

        try {
            if (logging) {
                if (logPath.equals("system")) { // NOI18N
                    outLogStream = System.err;
                    inLogStream = System.err;
                }
                else {
                    outLogStream = new BufferedOutputStream(new FileOutputStream(logPath + ".out")); // NOI18N
                    inLogStream = new BufferedOutputStream(new FileOutputStream(logPath + ".in"));  // NOI18N
                }
            }
        }
        catch (IOException e) {
            System.err.println("Unable to create log files: " + e); // NOI18N
            System.err.println("Logging DISABLED"); // NOI18N
            logging = false;
            try {
                if (outLogStream != null) {
                    outLogStream.close();
                }
            }
            catch (IOException ex2) {
                // ignore, if we get one here we really are screwed
            }

            try {
                if (inLogStream != null) {
                    inLogStream.close();
                }
            }
            catch (IOException ex2) {
                // ignore, if we get one here we really are screwed
            }
        }
    }


    /**
     * Log a message received from the server. The message is logged if
     * logging is enabled
     * @param received the data received from the server
     */
    public static void logInput(byte[] received) {
        logInput(received, 0 , received.length);
    }

    /**
     * Log a message received from the server. The message is logged if
     * logging is enabled
     * @param received the data received from the server
     */
    public static void logInput(byte[] received, int offset, int len) {
        if (!logging) {
            return;
        }

        try {
            inLogStream.write(received, offset, len);
            inLogStream.flush();
        }
        catch (IOException ex) {
            System.err.println("Could not write to log file: " + ex); // NOI18N
            System.err.println("Logging DISABLED."); // NOI18N
            logging = false;
        }
    }

    /**
     * Log a character received from the server. The message is logged if
     * logging is enabled
     * @param received the data received from the server
     */
    public static void logInput(char received) {
        if (!logging) {
            return;
        }

        try {
            inLogStream.write(received);
            inLogStream.flush();
        }
        catch (IOException ex) {
            System.err.println("Could not write to log file: " + ex); // NOI18N
            System.err.println("Logging DISABLED."); // NOI18N
            logging = false;
        }
    }

    /**
     * Log a message sent to the server. The message is logged if
     * logging is enabled
     * @param sent the data sent to the server
     */
    public static void logOutput(byte[] sent) {
        if (!logging) {
            return;
        }

        try {
            outLogStream.write(sent);
            outLogStream.flush();
        }
        catch (IOException ex) {
            System.err.println("Could not write to log file: " + ex); // NOI18N
            System.err.println("Logging DISABLED."); // NOI18N
            logging = false;
        }
    }
}

