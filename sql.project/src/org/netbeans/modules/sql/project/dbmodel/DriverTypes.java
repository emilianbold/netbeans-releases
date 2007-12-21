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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.dbmodel;


import java.io.File;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * Encapsulates information regarding known JDBC driver types for use in establishing
 * database metadata connections.
 *
 * @author Jonathan Giron
 * @version 
 */
public class DriverTypes {

    // Master list of driver TypeInfo instances.
    private static final TypeInfo[]
        TYPES = new TypeInfo[] {
            new TypeInfo("Oracle",
                         "oracle.jdbc.OracleDriver",
                         "jdbc:oracle:thin:@<host>:<port>:<sid>"),
/*
            new TypeInfo("Oracle OCI",
                         "oracle.driver.jdbc.OracleDriver",
                         "jdbc:oracle:oci8:@<host>:<port>:<sid>"),
*/
            new TypeInfo("SeeBeyond (DataDirect)",
                         "com.SeeBeyond.oracle.jdbc.oracle.OracleDriver",
                         "jdbc:SeeBeyond:oracle://<host>:<port>;SID=<sid>"),
/*            new TypeInfo("User-defined",
                         "<fully-qualified driver classname>",
                         "jdbc:<url parameters>")
*/
            new TypeInfo("OracleOCI",
                         "oracle.jdbc.OracleDriver",
                         "jdbc:oracle:oci8:@<tns>##setTNSEntry#<tns>"),

    };

    // Map of display names to TypeInfo instances
    private static HashMap typeMap;

    static {
        typeMap = new HashMap();
        for (int i = 0; i < TYPES.length; i++) {
            TypeInfo info = TYPES[i];
            typeMap.put(info.getDisplayName(), info);
        }
        if (TYPES.length != typeMap.keySet().size()) {
            throw new IllegalStateException(
                    "Count of display names does not match count of driver TypeInfo instances!"); // NOI18N
        }
    }

    /** Creates a new instance of DriverTypes */
    public DriverTypes() {
    }

    /**
     * Gets Collection of driver display names.
     *
     * @return Collection of display names
     */
    public static Collection getDisplayNames() {
        return typeMap.keySet();
    }

    /**
     * Gets collection of known TypeInfo instances, in the form of an unmodifiable List.
     *
     * @return List of TypeInfo instances.
     */
    public static List getTypeInfoList() {
        return (TYPES != null)
            ? Collections.unmodifiableList(Arrays.asList(TYPES)) : Collections.EMPTY_LIST;
    }

    /**
     * Gets TypeInfo instance associated with the given display name.
     *
     * @param displayName display name to use as key in retrieving TypeInfo instance
     * @return TypeInfo associated with displayName, if it exists; null otherwise
     */
    public static TypeInfo getTypeInfo(String displayName) {
        Object obj = typeMap.get(displayName);
        return (obj != null) ? (TypeInfo) obj : null;
    }

    /**
     * Constructs a JDBC URL given the specified connection parameters.
     *
     * @param  driverName Driver display name
     * @param  hostname   Database hostname
     * @param  port       Database port
     * @param  sid        Database SID
     * @param  username   Database login ID
     * @param  password   Database login password
     *
     * @return Applicable JDBC URL composited from the supplied connection
     *         information, or null if the specified driver class name is not
     *         recognized or supported
     */
    public static final String makeUrl(String driverName,
                          String hostname,
                          String port,
                          String sid,
                          String username,
                          String password) {

        String url = null;
        TypeInfo info = getTypeInfo(driverName);

        if (info != null) {
            StringBuffer template = new StringBuffer(info.getURLTemplate());
            String var = getSubstVar(template);
            while (var != null) {
                if (var.equalsIgnoreCase("host")) {
                    replaceSubstVar(template, var, hostname);
                } else if (var.equalsIgnoreCase("port")) {
                    replaceSubstVar(template, var, port);
                } else if (var.equalsIgnoreCase("sid")) {
                    replaceSubstVar(template, var, sid);
                } else if (var.equalsIgnoreCase("username")) {
                    replaceSubstVar(template, var, username);
                } else if (var.equalsIgnoreCase("password")) {
                    replaceSubstVar(template, var, password);
                }
                var = getSubstVar(template);
            }
            url = template.toString();
        }
        return url;
    }

    /**
     * Constructs a JDBC URL given the specified connection parameters.
     *
     * @param  driverName Driver display name
     * @param  hostname   Database hostname
     * @param  port       Database port
     * @param  sid        Database SID
     * @param  username   Database login ID
     * @param  password   Database login password
     *
     * @return Applicable JDBC URL composited from the supplied connection
     *         information, or null if the specified driver class name is not
     *         recognized or supported
     */
    public static final String makeUrl(String driverName,
                          String hostname,
                          String port,
                          String sid,
                          String username,
                          String password,
                          String tns) {

        String url = null;
        TypeInfo info = getTypeInfo(driverName);

        if (info != null) {
            StringBuffer template = new StringBuffer(info.getURLTemplate());
            String var = getSubstVar(template);
            while (var != null) {
                if (var.equalsIgnoreCase("host")) {
                    replaceSubstVar(template, var, hostname);
                } else if (var.equalsIgnoreCase("port")) {
                    replaceSubstVar(template, var, port);
                } else if (var.equalsIgnoreCase("sid")) {
                    replaceSubstVar(template, var, sid);
                } else if (var.equalsIgnoreCase("username")) {
                    replaceSubstVar(template, var, username);
                } else if (var.equalsIgnoreCase("password")) {
                    replaceSubstVar(template, var, password);
                } else if(var.equalsIgnoreCase("tns")) {
                    replaceSubstVar(template, var, tns);
			    }
                var = getSubstVar(template);
            }
            url = template.toString();
        }
        return url;
    }


    /**
     * Get the first encountered substitution string in the buffer.
     * A string bounded on its left and right edges with the characters
     * '<' and '>', including these characters, is a substitution string.
     *
     * @param  template Container of data
     *
     * @return If a substitution string is found, the string is returned;
     *         otherwise, null is returned
     */
    private static final String getSubstVar(StringBuffer template) {
        String substVar = null;
        int start = template.indexOf("<");
        int end = template.indexOf(">");
        if (start != -1 && end != -1 && start < (end + 1)) {
            substVar = template.substring(start + 1, end);
        }
        return substVar;
    }

    /**
     * Replace the first occurence of '<' + pattern + '>' in the buffer,
     * with the replacement string,
     *
     * @param buffer      Container of data
     * @param pattern     Substring to find
     * @param replacement String to replace the pattern
     */
    private static final void replaceSubstVar(StringBuffer buffer,
            String pattern, String replacement) {

        String pat = "<" + pattern + ">";
        int position = buffer.indexOf(pat);
        if (position != -1) {
            buffer.delete(position, position + pat.length());
            buffer.insert(position, replacement);
        }
    }

    /**
     * Lightweight container class to associate a JDBC driver display name with its
     * fully-qualified Java classname and a standard JDBC URL template.
     *
     * @author Jonathan Giron
     * @version 
     */
    public static class TypeInfo {
        private String displayName;
        private String className;
        private String urlTemplate;
        private File jarFile;

        /**
         * Creates new instance of TypeInfo with the given display name, driver classname,
         * and URL template.
         *
         * @param displayStr user-friendly description of driver type
         * @param driverClass fully-qualified classname of driver
         * @param urlStr String representing URL template, in the form
         * "jdbc:{JDBC vendor type}:{vendor-specific parameters enclosed by angle brackets}"
         */
        public TypeInfo(String displayStr, String driverClass, String urlStr) {
            if (displayStr == null || displayStr.trim().length() == 0) {
                throw new IllegalArgumentException(
                    "Must supply non-empty String instance for display argument."); //NOI18N
            }

            if (driverClass == null || driverClass.trim().length() == 0) {
                throw new IllegalArgumentException(
                    "Must supply non-empty String instance for driverClass argument."); //NOI18N
            }

            if (urlStr == null || urlStr.trim().length() == 0) {
                throw new IllegalArgumentException(
                    "Must supply non-empty String instance for urlString argument."); //NOI18N
            }

            displayName = displayStr;
            className = driverClass;
            urlTemplate = urlStr;
        }

        /**
         * Gets display name for this driver type.
         *
         * @return driver display name
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets JDBC driver classname for this driver type.
         *
         * @return fully-qualified JDBC driver classname
         */
        public String getDriverClassName() {
            return className;
        }

        /**
         * Gets URL template representing required information for accessing a
         * database of this driver type.
         *
         * @return URL template
         */
        public String getURLTemplate() {
            return urlTemplate;
        }

        /**
         * Gets optional File instance indicating path to JAR file containing the associated
         * JDBC driver.
         *
         * @return File instance pointing to location of driver JAR file, or null if no
         * path information is known.
         */
        public File getDriverJarFile() {
            return jarFile;
        }

        /**
         * Sets optional File instance indicating path to JAR file containing the associated
         * JDBC driver.  A null argument indicates that the location of the JAR file within the
         * local filesystem is unknown.
         *
         * @param newLocation File instance representing location of driver JAR file within the
         * local filesystem, or null if location is unknown
         */
        public void setDriverJarFile(File newLocation) {
            jarFile = newLocation;
        }

        /**
         * Overrides parent implementation to return driver display name.
         *
         * @return display name of JDBC driver
         */
        public String toString() {
            return displayName;
        }
    }
}
