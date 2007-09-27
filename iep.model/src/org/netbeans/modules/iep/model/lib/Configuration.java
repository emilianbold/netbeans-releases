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


import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;

import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Reads a global configuration file and stores the variables
 *
 * @see java.util.Properties
 */
public class Configuration {
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(Configuration.class.getName());

    /**
     * Stores the global properties
     */
    private static ConfigProperties mProps;

    static {
        mProps = new ConfigProperties();
        try {
            InputStream fis = IOUtil.getResourceAsStream("config/config.properties");
            mProps.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //mLog.debug("============IEP Properties Beg===========");
            //mProps.store(System.out, "");
            //mLog.debug("============IEP Properties End===========");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an Enumeration of all keys found in the configuration file
     *
     * @return An Enumeration of all keys found in the configuration file
     */
    public static Enumeration getAllKeys() {
        return mProps.propertyNames();
    }

    /**
     * Returns a copy of the global properties
     *
     * @return The properties value
     */
    public static java.util.Properties getProperties() {
        return new java.util.Properties(mProps);
    }

    /**
     * set an entry after file load
     *
     * @param s1 property entry
     * @param s2 property entry
     */
    public static void setPropertyEntry(String s1, String s2) {
        if ((s1 != null) && (s2 != null)) {
            mProps.setProperty(s1, s2);
        }
    }

    /**
     * Returns a String value corresponding to the config variables specified
     * by name
     *
     * @param name Config variable to find in the config file
     *
     * @return Value found in the config file for the variable specified by
     *         name
     */
    public static String getVarByName(String name) {
        String val = mProps.getProperty(name);
        return (val != null) ? val.trim() : null;
    }

    /**
     * Returns a Collection of String values corresponding to the config
     * variables specified. The raw config entry is separated by the the
     * separator String.
     *
     * @param name Description of the Parameter
     *
     * @return The varListByName value
     */
    public static LinkedList getVarListByName(String name) {
        return getVarListByName(name, ",");
    }

    /**
     * Returns a Collection of String values corresponding to the config
     * variables specified. The raw config entry is separated by the the
     * separator String.
     *
     * @param name Description of the Parameter
     * @param separator Description of the Parameter
     *
     * @return The varListByName value
     */
    public static LinkedList getVarListByName(String name, String separator) {
        LinkedList varList = new LinkedList();
        String unparsedVar = getVarByName(name);

        // This means that the var didn't exist in mos.properties
        // It's better to return an empty LinkedList than null
        if (unparsedVar != null) {
            StringTokenizer token = new StringTokenizer(unparsedVar, separator);
            while (token.hasMoreTokens()) {
                varList.add(token.nextToken().trim());
            }
        }

        return varList;
    }

    /**
     * main() method this is used as a test method for this class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        try {
            Enumeration enumVar = mProps.keys();

            while (enumVar.hasMoreElements()) {
                String keyVar = (String) enumVar.nextElement();
                String valVar = (String) mProps.getProperty(keyVar);

                mLog.info(" >>> key:  " + keyVar + " >>> value:   "
                                   + valVar);
            }
        } catch (Exception de) {
            System.err.println("\r\n ERROR:   " + de.getMessage());
        }
    }
}

