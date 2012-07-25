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
import java.util.Set;

/**
 *
 * @author kratz
 */
public class GlassFishCloudUrl {
    
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

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
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
     * Build URL string.
     * <p>
     * URL is matching following grammar:<>
     * <ul>
     * <li>URL :: &lt;identifier&gt; &lt;separator&gt; &lt;name&gt;</li>
     * <li>&lt;identifier&gt; :: {@see GlassFishCloudInstance.URL_PREFIX}
     * | {@see GlassFishAccountInstance.URL_PREFIX}</li>
     * 
     * </ul></p>
     */
    public static String url(String prefix, String name) {        
        StringBuilder sb = new StringBuilder(escapedLength(prefix)
                + 1 + escapedLength(name));
        addEscaped(sb, prefix);
        sb.append(URL_SEPARATOR);
        addEscaped(sb, name);
        return sb.toString();
    }
    
}
