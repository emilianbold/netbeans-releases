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
package org.netbeans.xtest.plugin.ide;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * BlacklistedClassesHandler performs processing of log messages to identify
 * which classes from a black list were loaded during application life cycle.
 *
 * The black list has to be specified in the separate text file: each line is
 * interpreted as classname, unless it is started with non-identifier character.
 * Leading and trailing spaces are ignored.
 *  
 * @author nenik, mrkam@netbeans.org
 * 
 */
public class BlacklistedClassesHandler extends Handler {

    private static BlacklistedClassesHandler instance = null;
    private boolean violation = false;
    // TODO: Is it necessary to use synchronizedMap? Should the list be synchronized?
//    private Map<String, List<Exception>> blacklist = Collections.synchronizedMap(new HashMap<String, List<Exception>>());
    private Map blacklist = Collections.synchronizedMap(new HashMap());

    private BlacklistedClassesHandler(String blacklistFileName) {
        final String[] watched = getWatched(blacklistFileName);
        for (int i = 0; i < watched.length; i++) {
            blacklist.put(watched[i], new ArrayList());
        }

        Logger.getLogger(BlacklistedClassesHandler.class.getName()).info(
                "BlacklistedClassesHandler: " + blacklist.size() + " classes loaded.");
    }

    public static BlacklistedClassesHandler getBlacklistedClassesHandler() {
        return instance;
    }

    public static synchronized BlacklistedClassesHandler getBlacklistedClassesHandler(String blacklistFileName) {
        if (instance != null) {
            throw new Error("BlacklistedClassesHandler shouldn't be initialized twice!");
        }
        instance = new BlacklistedClassesHandler(blacklistFileName);
        return instance;
    }

    public void publish(LogRecord record) {
        // We can't use logging in this method as it could cause LinkageError
        try {
            if (record != null && record.getMessage() != null) {
                if (record.getMessage().contains("initiated")) {
                    String className = (String) record.getParameters()[1];
                    if (blacklist.containsKey(className)) { //violator
                        Exception exc = new BlacklistedClassesViolationException(record.getParameters()[0].toString());
                        System.out.println("BlacklistedClassesHandler violator: " + className);
                        exc.printStackTrace();
                        synchronized (blacklist) {
                            // TODO: Probably we should synchronize by list
                            ((List) blacklist.get(className)).add(exc);
                        }
                        violation = true;
                    }
                } else if (record.getMessage().equalsIgnoreCase("LIST BLACKLIST VIOLATIONS")) {
                    logViolations();
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public void flush() {
    }

    public void close() throws SecurityException {
        /* Ugly hack to leave the handler when configuration is reset */
        Logger logger = Logger.getLogger("org.netbeans.ProxyClassLoader"); // NOI18N            
        logger.addHandler(this);
    }
    
    public void remove() {
        Logger logger = Logger.getLogger("org.netbeans.ProxyClassLoader"); // NOI18N            
        logger.removeHandler(this);
    }

    public boolean noViolations() {
        return !violation;
    }

    public boolean noViolations(boolean listViolations) {
        if (violation && listViolations) {
            logViolations();
        }
        return !violation;
    }

    public boolean noViolations(PrintStream out) {
        if (violation) {
            listViolations(out, true);
        }
        return !violation;
    }

    private static String[] getWatched(String blacklistFileName) {
        BufferedReader reader = null;
        try {
            if (blacklistFileName != null) {
                reader = new BufferedReader(new FileReader(new File(blacklistFileName)));
                ArrayList strings = new ArrayList();
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    line = line.trim();
                    if (line.length() > 0 && Character.isJavaIdentifierStart(line.charAt(0))) {
                        strings.add(line);
                    }
                }
                return (String[]) strings.toArray(new String[strings.size()]);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BlacklistedClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BlacklistedClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BlacklistedClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new String[0];
    }

    public void logViolations() {
        Logger.getLogger(BlacklistedClassesHandler.class.getName()).warning(listViolations());
    }

    public String listViolations() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        listViolations(ps, true);
        ps.flush();
        return baos.toString();
    }

    public void listViolations(PrintStream out) {
        listViolations(out, false);
    }

    public void listViolations(PrintWriter out) {
        listViolations(out, false);
    }

    public void listViolations(PrintStream out, boolean listExceptions) {
        listViolations(new PrintWriter(out), listExceptions);
    }

    public void listViolations(PrintWriter out, boolean listExceptions) {
        out.println("BlacklistedClassesHandler identified the following violations:");
        synchronized (blacklist) {
            int i = 0;
            final Set keySet = blacklist.keySet();
            Iterator iter = keySet.iterator();
            while (iter.hasNext()) {
                String violator = (String) iter.next();
                if (((List) blacklist.get(violator)).size() > 0) {
                    out.println(++i + ". " + violator);
                    if (listExceptions) {
                        final List exceptions = (List) blacklist.get(violator);
                        Iterator iter2 = exceptions.iterator();
                        while (iter2.hasNext()) {
                            Exception ex = (Exception) iter2.next();
                            ex.printStackTrace(out);
                        }
                    }
                }
            }
        }
        out.flush();
    }

    public void resetViolations() {
        synchronized (blacklist) {
            final Set keySet = blacklist.keySet();
            Iterator iter = keySet.iterator();
            while (iter.hasNext()) {
                String violator = (String) iter.next();
                ((List) blacklist.get(violator)).clear();
            }
            violation = false;
        }
    }
}
