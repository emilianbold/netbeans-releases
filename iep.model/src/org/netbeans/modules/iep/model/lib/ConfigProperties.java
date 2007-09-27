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


package org.netbeans.modules.iep.model.lib;

import java.io.PrintStream;
import java.io.PrintWriter;

import java.util.Enumeration;

/*
    > system.properties
    NETBEANS_HOME=C:/netbeans
    > pdt.properties
    netbeans_home=%NETBEANS_HOME%
    usrdir=%netbeans_home%/usrdir
*/
/**
 * Description of the Class
 *
 * @author    Bing Lu
 * @created   June 4, 2003
 */
public class ConfigProperties extends java.util.Properties {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ConfigProperties.class.getName());

    /** Description of the Field */
    public final static boolean DEBUG = Boolean.getBoolean("Properties.debug");

    /** Description of the Field */
    private String begPattern = "${";
    /** Description of the Field */
    private String endPattern = "}";

    /** Constructor for the Properties object */
    public ConfigProperties() {
        super(System.getProperties());
    }

    /**
     * Constructor for the Properties object
     *
     * @param begPattern  Description of the Parameter
     * @param endPattern  Description of the Parameter
     */
    public ConfigProperties(String begPattern, String endPattern) {
        this();
        this.begPattern = begPattern;
        this.endPattern = endPattern;
    }

    /**
     * Gets the begPattern attribute of the Properties object
     *
     * @return   The begPattern value
     */
    public String getBegPattern() {
        return begPattern;
    }

    /**
     * Gets the endPattern attribute of the Properties object
     *
     * @return   The endPattern value
     */
    public String getEndPattern() {
        return endPattern;
    }

    /**
     * Gets the property attribute of the Properties object
     *
     * @param key  Description of the Parameter
     * @return     The property value
     */
    public String getProperty(String key) {
        String val = super.getProperty(key);
        try {
            if (val != null) {
                val = resolve(key, val);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return val;
    }

    /**
     * Description of the Method
     *
     * @param out  Description of the Parameter
     */
    public void list(PrintStream out) {
        for (Enumeration e = propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            out.println(key + "=" + getProperty(key));
        }
        out.flush();
    }

    /**
     * Description of the Method
     *
     * @param out  Description of the Parameter
     */
    public void list(PrintWriter out) {
        for (Enumeration e = propertyNames(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            out.println(key + "=" + getProperty(key));
        }
        out.flush();
    }

    /*
        @todo check for cycles
    */
    /**
     * Description of the Method
     *
     * @param key  Description of the Parameter
     * @param val  Description of the Parameter
     * @return     Description of the Return Value
     */
    private String resolve(String key, String val /*, java.util.List visited*/) {
        StringBuffer ret = new StringBuffer();

        int idx = 0;

        debug("resolve(key: " + key + " val:" + val + ")");

        while (idx < val.length()) {
            debug("idx: " + idx);

            int beg = val.indexOf(begPattern, idx);
            if (beg == -1) {
                debug("beg == -1 '" + val.substring(idx) + "'");
                ret.append(val.substring(idx));

                break;
            } else {
                debug("beg != -1 '" + val.substring(idx, beg) + "'");
                ret.append(val.substring(idx, beg));

                if (beg + begPattern.length() >= val.length()) {
                    // last char(s)
                    debug("beg+begPattern.length() >= val.length() '" + val.substring(beg) + "'");
                    ret.append(val.substring(beg));
                    break;
                } else {
                    int end = val.indexOf(endPattern, beg + begPattern.length());
                    if (end == -1) {
                        debug("end == -1 '" + val.substring(beg) + "'");
                        ret.append(val.substring(beg));

                        break;
                    } else {
                        debug("end != -1 '" + val.substring(beg + begPattern.length(), end) + "'");
                        //ret.append(val.substring(beg+begPattern.length(), end);

                        String k = val.substring(beg + begPattern.length(), end);

                        // Detects some cycles
                        if (key.equals(k)) {
                            throw new RuntimeException("cycle detected: " + key + "=" + val);
                        }
                        //visited.put(k);
                        ret.append(getProperty(k, begPattern + k + endPattern));

                        idx = end + endPattern.length();
                    }
                }
            }
        }
        return ret.toString();
    }

    /**
     * Description of the Method
     *
     * @param s  Description of the Parameter
     */
    private void debug(String s) {
        if (DEBUG) {
            mLog.warning(s);
        }
    }

    /**
     * The main program for the Properties class
     *
     * @param args           The command line arguments
     * @exception Exception  Description of the Exception
     */
    public static void main(String[] args) throws Exception {
        ConfigProperties p = new ConfigProperties();
        
        p.setProperty("netbeans_home", p.getBegPattern() + "NETBEANS_HOME" +
            p.getEndPattern() + p.getEndPattern());

        p.setProperty("usrdir", p.getBegPattern() + "netbeans_home" +
            p.getEndPattern() + "/usrdir");

        p.setProperty("usrdir2", p.getBegPattern() + "netbeans_home" +
            p.getEndPattern() + "/" +
            p.getBegPattern() + "usrdirmissing" + p.getEndPattern());

        p.setProperty("usrdir3", p.getBegPattern() + "netbeans_home" +
            p.getEndPattern() + "/" +
            p.getBegPattern() + "usrdir3" + p.getEndPattern());

        mLog.info("NETBEANS_HOME=" + p.getProperty("NETBEANS_HOME"));
        mLog.info("netbeans_home=" + p.getProperty("netbeans_home"));
        mLog.info("usedir=" + p.getProperty("usrdir"));
        mLog.info("usedirmissing=" + p.getProperty("usrdirmissing"));
        mLog.info("usrdir2=" + p.getProperty("usrdir2"));
        mLog.info("usrdir3=" + p.getProperty("usrdir3"));
        p.list(System.out);

        p = new ConfigProperties();
        p.load(new java.io.FileInputStream(args[0]));
        p.list(System.out);
    }
}

