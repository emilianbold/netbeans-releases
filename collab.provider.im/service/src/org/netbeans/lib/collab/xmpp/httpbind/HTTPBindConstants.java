/*
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
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.lib.collab.xmpp.httpbind;

/**
 *
 * @author Mridul Muralidharan
 */
public interface HTTPBindConstants {
    public static final String MAXREQUESTS_PARAMETER = "maxrequests"; // NOI18N
    public static final String WAIT_PARAMETER = "wait"; // NOI18N
    public static final String TO_DOMAIN_PARAMETER = "to"; // NOI18N
    public static final String ROUTE_PARAMETER = "route"; // NOI18N
    public static final String XML_LANG_PARAMETER = "xml_lang"; // NOI18N
    public static final String CONTENTTYPE_PARAMETER = "contenttype"; // NOI18N

    public static final String PROXY_TYPE_PARAMETER = "proxytype"; // NOI18N
    public static final String PROXY_HOSTPORT_PARAMETER = "proxyhostport"; // NOI18N

    public static final String HTTP_PROXY_TYPE = "http";
    public static final String HTTPS_PROXY_TYPE = "https";
    public static final String SOCKS_PROXY_TYPE = "socks";

    public static final int DEFAULT_WAIT_TIME = 60;
    public static final int DEFAULT_MAX_REQUESTS = 5;
    public static final String DEFAULT_XML_LANG = "en"; // NOI18N
    public static final String DEFAULT_CONTENTTYPE = "text/xml; charset=utf-8"; // NOI18N
    public static final String REQUEST_CONTENTTYPE = "text/xml; charset=utf-8"; // NOI18N

    public static final String UTF_8 = "UTF-8"; // NOI18N

    public static final String MAX_BUFFERED_BYTES = "max_buffered_bytes";
    public static final String MAX_BUFFERED_PACKETS = "max_buffered_packets";

    public static final String CONNECTION_PROVIDER =
        "org.netbeans.lib.collab.xmpp.httpbind.ConnectionProvider"; // NOI18N

    public static final String SESSION_LISTENER =
            "org.netbeans.lib.collab.CollaborationSessionListener";
}
