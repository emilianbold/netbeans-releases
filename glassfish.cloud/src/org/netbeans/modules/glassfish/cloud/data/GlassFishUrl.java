/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.data;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.glassfish.tools.ide.data.DataException;
import org.glassfish.tools.ide.utils.StringPrefixTree;

/**
 * GlassFish cloud URL representing key attribute in JavaEE server registry.
 * <p/>
 * GlassFish cloud URL syntax:
 * <ul>
 * <li>URL :: &lt;identifier&gt; &lt;separator&gt; &lt;name&gt;</li>
 * <li>&lt;identifier&gt; :: {@see GlassFishCloudInstance.URL_PREFIX}
 * | {@see GlassFishAccountInstance.URL_PREFIX}</li>
 * </ul></p>
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishUrl {
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Server type ID.
     * <p/>
     * <code>String</code> representation of ID values should be compliant with
     * url patterns in
     * <code>J2EE::DeploymentPlugins::GlassFish Cloud::Factory.instance</code>
     * and
     * <code>J2EE::DeploymentPlugins::GlassFish Local Server::Factory.instance
     * </code> regular expression in <code>layer.xml</code> file.
     * Actually <code>^gfc[r|l]:.*$</code> describes all strings used for local
     * GlassFish server {@see LOCAL_STR} and GlassFish cloud {@see CLOUD_STR}.
     */
    public static enum Id {
        ////////////////////////////////////////////////////////////////////////
        // Enum values                                                        //
        ////////////////////////////////////////////////////////////////////////
        
        /** Cloud user account on GlassFish server. */
        CLOUD,

        /** Local GlassFish server. */
        LOCAL;
 
        ////////////////////////////////////////////////////////////////////////
        // Class attributes                                                   //
        ////////////////////////////////////////////////////////////////////////

        /** A <code>String</code> representation of CLOUD value.
         *  Defines string used in URL prefix. */
        static final String CLOUD_STR = "gfcr";

        /** A <code>String</code> representation of LOCAL value.
         *  Defines string used in URL prefix. */
        static final String LOCAL_STR = "gfcl";
        
        /** Stored <code>String</code> values for backward <code>String</code>
         *  conversion. */
        private static final StringPrefixTree<Id> stringValues
                = new StringPrefixTree<Id>(false);
        static {
            for (Id id : Id.values()) {
                stringValues.add(id.toString(), id);
            }
        }

        /**
         * Returns <code>GlassFishUrl.Id</code> with value represented by
         * the specified <code>String</code>.
         * <p/>
         * The <code>GlassFishUrl.Id</code> returned represents existing
         * value only if specified <code>String</code> matches any
         * <code>String</code> returned by <code>toString</code> method.
         * Otherwise <code>null</code> value is returned.
         * <p/>
         * @param idStr Value containing id <code>String</code> representation.
         * @return <code>GlassFishUrl.Id</code>  value represented
         *         by <code>String</code> or <code>null</code> if value was
         *         not recognized.
         */
        public static Id toValue(String idStr) {
            return idStr != null ? stringValues.match(idStr) : null;
        }

        ////////////////////////////////////////////////////////////////////////
        // Methods                                                            //
        ////////////////////////////////////////////////////////////////////////

        /**
         * Convert <code>GlassFishUrl.Id</code> value to <code>String</code>.
         * <p/>
         * @return A <code>String</code> representation of the value of
         *         this object.
         */
        @Override
        public String toString() {
            switch (this) {
                case CLOUD: return CLOUD_STR;
                case LOCAL: return LOCAL_STR;
                // This is unrecheable. Being here means this class does not handle
                // all possible values correctly.
                default: throw new DataException(DataException.INVALID_URL);
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** URL components separator. */
    public static final char URL_SEPARATOR = ':';

    /** URL escape character. */
    public static final char URL_ESCAPE='\\';

    /** Set of characters to escape. */
    private static final Set<Character> escape = new HashSet<Character>(4);
    static {
        escape.add(URL_SEPARATOR);
        escape.add(URL_ESCAPE);
    }

    /** URL ID <cpde>String</code>s with components separator appended to
     *  test if URL belongs to this module. */
    private static final StringPrefixTree<Id> urlPrefixes
            = new StringPrefixTree<Id>(false);
    static {
        for (Id id : Id.values()) {
            urlPrefixes.add(id.toString() + URL_SEPARATOR, id);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Count length of escaped <code>String</code>.
     * <p/>
     * @param str Not yet escaped <code>String</code> used to count length.
     * @return Length of the <code>String</code> when escaped.
     */
    private static int escapedLength(String str) {
        int escapedLength = 0;
        if (str != null) {
            int strLen = str.length();
            for (int i = 0; i < strLen; i++) {
                escapedLength += escape.contains(str.charAt(i)) ? 2 : 1;
            }
        }
        return escapedLength;
    }

    /**
     * Add escaped <code>String</code> into given <code>StringBuffer</code>.
     * <p/>
     * @param sb  Target <code>StringBuffer</code> where to add escaped
     *            <code>String</code>.
     * @param str <code>String</code> to be escaped and added into
     *            <code>StringBuffer</code>.
     */
    private static void addEscaped(StringBuilder sb, String str) {
        if (sb != null && str != null) {
            int strLen = str.length();
            for (int i = 0; i < strLen; i++) {
                if (escape.contains(str.charAt(i))) {
                    sb.append(URL_ESCAPE);
                }
                sb.append(str.charAt(i));
            }
        }
    }

    /**
     * Return URL components indexes as <code>List</code> of <code>int[2]</code>
     * arrays.
     * <p/>
     * Integer at index <code>0</code> contains beginning index of URL
     * component. Integer at index <code>1</code> contains last index of URL
     * component.
     */
    private static List<int[]> componentIndexes(String url) {
        List<int[]> indexList = new LinkedList<int[]>();
        int strLen = url.length();
        int beg = 0;
        int end;
        while (beg < strLen) {
            int sep = url.indexOf(URL_SEPARATOR, beg);
            end = sep > -1 ? sep - 1 : strLen -1;
            indexList.add(new int[] {beg, end > beg ? end : beg});
            beg = sep > -1 ? sep + 1 : strLen;
        }
        return indexList;
    }

    /**
     * Build URL string from URL prefix and server name key attribute.
     * <p>
     * @param prefix URL prefix.
     * @param name   Server name (key attribute).
     * @return URL string.
     */
    public static String url(Id type, String name) {
        String typeName = type.toString();
        StringBuilder sb = new StringBuilder(escapedLength(typeName)
                + 1 + escapedLength(name));
        addEscaped(sb, typeName);
        sb.append(URL_SEPARATOR);
        addEscaped(sb, name);
        return sb.toString();
    }

    /**
     * Get server type ID from given URL.
     * <p/>
     * @param url URL to check for server type ID string in 1st component.
     * @return Server type ID in 1st component or <code>null</code> when
     *         1st component does not contain known server type ID.
     */
    public static Id urlPrefix(String url) {
        return urlPrefixes.prefixMatch(url);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Server type ID. */
    private final Id type;

    /** Server name (key attribute). */
    private final String name;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish cloud URL from URL <cpde>String</code>.
     * <p/>
     * Parses and verifies URL syntax and content to make sure it contains valid
     * server ID and at least some server name. Server name is not verified
     * against registered servers.
     * <p/>
     * @param url GlassFish cloud URL string.
     * @throws IllegalArgumentException When URL syntax and content is
     *         not valid.
     */
    public GlassFishUrl(String url) throws IllegalArgumentException {
        List<int[]> indexList = componentIndexes(url);
        int componentsCount = indexList.size();
        if (componentsCount != 2) {
            throw new IllegalArgumentException(
                    "GlassFish cloud URL should containn exactly 2 components");
        }
        int index = 0;
        String serverType = null;
        String serverName = null;
        for (int[] indexes : indexList) {
            switch(index) {
                // 1st component is server type ID
                case 0:
                    serverType = url.substring(indexes[0], indexes[1] + 1);
                    break;
                // 2nd component is server name key attribute
                case 1:
                    serverName = url.substring(indexes[0], indexes[1] + 1);
                    break;
            }
            index++;
        }
        type = Id.toValue(serverType);
        if (type == null) {
            throw new IllegalArgumentException(
                    "GlassFish cloud URL 1st component should contain valid"
                    + " server type ID");
        }
        if (serverName == null || serverName.length() == 0) {
            throw new IllegalArgumentException(
                    "GlassFish cloud URL 2st component should contain server"
                    + " name key attribute");
            
        } else {
            name = serverName;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get server type ID.
     * <p/>
     * @return Server type ID.
     */
    public Id getType() {
        return type;
    }

    /**
     * Get server name (key attribute).
     * <p/>
     * @return Server name (key attribute).
     */
    public String getName() {
        return name;
    }

}
