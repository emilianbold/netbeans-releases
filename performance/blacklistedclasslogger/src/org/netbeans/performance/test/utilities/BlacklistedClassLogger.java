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

package org.netbeans.performance.test.utilities;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * BlacklistedClassLogger performs processing of log messages to identify
 * which classes from a black list were loaded during application life cycle.
 *
 * The black list has to be specified in the separate text file: each line is
 * interpreted as classname, unless it is started with non-identifier character.
 * Leading and trailing spaces are ignored.
 * 
 * To enable BlacklistedClassLogger you need to specify the following properties at JVM startup:
 * - org.netbeans.performance.test.utilities.BlacklistedClassLogger.blacklist.filename=&lt;filename&gt;
 * - java.util.logging.config.file=log.properties 
 *  
 * @author nenik, mrkam@netbeans.org
 * 
 */
public class BlacklistedClassLogger extends Handler {

    private static boolean violation = false;
    // TODO: Is it necessary to use synchronizedMap? Should the list be synchronized?
    private static Map<String, List<Exception>> blacklist = Collections.synchronizedMap(new HashMap<String, List<Exception>>());
    static {
        for (String s : getWatched()) {
            blacklist.put(s, new ArrayList<Exception>());
        }
        System.out.println("BlacklistedClassLogger: " + blacklist.size() + " classes loaded.");
    }

    public BlacklistedClassLogger() {
        defaultHandler();
    }
    
    private static void defaultHandler () {
        String home = System.getProperty("netbeans.user");
        if (home != null && !"memory".equals(home)) {
            try {
                File dir = new File(new File(new File(home), "var"), "log");
                dir.mkdirs ();
                File f = new File(dir, "messages.log");
                File f1 = new File(dir, "messages.log.1");
                File f2 = new File(dir, "messages.log.2");

                if (f1.exists()) {
                    f1.renameTo(f2);
                }
                if (f.exists()) {
                    f.renameTo(f1);
                }
                
                FileOutputStream fout = new FileOutputStream(f, false);
                fout.write("No messages.log available when BlacklistedClassLogger is enabled.".getBytes());
                fout.flush();
//                Handler h = new StreamHandler(fout, NbFormatter.FORMATTER);
//                h.setLevel(Level.ALL);
//                h.setFormatter(NbFormatter.FORMATTER);
//                defaultHandler = new NonClose(h, 5000);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
    }
    
    @Override
    public void publish(LogRecord record) {
        try {
            if (record != null && record.getMessage().contains("initiated")) {
                String className = (String) record.getParameters()[1];

                if (blacklist.containsKey(className)) { //violator
                    System.out.println("BlacklistedClassLogger violator: " + className);
                    Exception exc = new BlackListViolationException(record.getParameters()[0].toString());
                    exc.printStackTrace();
                    synchronized (blacklist) {
                        // TODO: Probably we should synchronize by list
                        blacklist.get(className).add(exc);
                    }
                    violation = true;
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    public static boolean noViolations() {
        return !violation;
    }

    public static boolean noViolations(PrintStream out) {
        if (violation) {
            out.println("The following violations are identified:");
            BlacklistedClassLogger.listViolations(out, true);
        }
        return !violation;
    }

    private static String getWatchedFileName() {
        return System.getProperty("org.netbeans.performance.test.utilities.BlacklistedClassLogger.blacklist.filename");
    }

    private static String[] getWatched() {
        BufferedReader reader = null;
        try {
            final String fileName = getWatchedFileName();
            if (fileName != null) {
                reader = new BufferedReader(new FileReader(new File(fileName)));
                ArrayList<String> strings = new ArrayList<String>();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    line = line.trim();
                    if (line.length() > 0 && Character.isJavaIdentifierStart(line.charAt(0))) {
                        strings.add(line);
                    }
                }
                return strings.toArray(new String[strings.size()]);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BlacklistedClassLogger.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BlacklistedClassLogger.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BlacklistedClassLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new String[0];
    }

    public static void listViolations(PrintStream out) {
        listViolations(out, false);
    }

    public static void listViolations(PrintWriter out) {
        listViolations(out, false);
    }

    public static void listViolations(PrintStream out, boolean listExceptions) {
        synchronized (blacklist) {
            int i = 0;
            for (String violator : blacklist.keySet()) {
                if (blacklist.get(violator).size() > 0) {
                    out.println(++i + ". " + violator);
                    if (listExceptions) {
                        for (Exception ex : blacklist.get(violator)) {
                            ex.printStackTrace(out);
                        }
                    }
                }
            }
        }
    }

    public static void listViolations(PrintWriter out, boolean listExceptions) {
        synchronized (blacklist) {
            int i = 0;
            for (String violator : blacklist.keySet()) {
                if (blacklist.get(violator).size() > 0) {
                    out.println(++i + ". " + violator);
                    if (listExceptions) {
                        for (Exception ex : blacklist.get(violator)) {
                            ex.printStackTrace(out);
                        }
                    }
                }
            }
        }
    }

    public static void resetViolations() {
        synchronized (blacklist) {
            for (String violator : blacklist.keySet()) {
                blacklist.get(violator).clear();
            }
            violation = false;
        }
    }
}
