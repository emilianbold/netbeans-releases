/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.lib.collab.xmpp.util;

import org.jabberstudio.jso.JID;
import org.netbeans.lib.collab.util.StringUtility;
import org.netbeans.lib.collab.xmpp.JIDUtil;

/**
 *
 * @author mridul
 */
public class LegacyJIDUtil extends JIDUtilProviderImpl {

    public JID encodedJID(JID jid) {
        if (null == jid) return null;
        try {
            return encodedJID(jid.getNode(), jid.getDomain(), jid.getResource());
        } catch (Exception e) {
            return null;
        }
    }

    public String decodedNode(JID jid) {
        return null != jid ? decodedNode(jid.getNode()) : null;
    }

    public String decodedDomain(JID jid) {
        return decodedDomain(jid.getDomain());
    }

    public String decodedResource(JID jid) {
        if (null == jid) return null;
        return decodedResource(jid.getResource());
    }
    
    protected String encodedResource(String resource) {
        try {
            if (null == resource) return null;
            return (resource == null) ? null : java.net.URLEncoder.encode(resource, "UTF-8");
        } catch (Exception e) {
            return resource;
        }        
    }

    public String decodedNode(String node) {
        try {
            if (null == node) return null;
            return java.net.URLDecoder.decode(node, "UTF-8");
        } catch (Exception e) {
            return node;
        }
    }
    
    public String encodedNode(String node) {
        try {
            if (null == node) return null;
            return java.net.URLEncoder.encode(node, "UTF-8");
        } catch (Exception e) {
            return node;
        }
    }

    public String decodedDomain(String domain) {
        try {
            if (null == domain) return null;
            return java.net.URLDecoder.decode(domain, "UTF-8");
        } catch (Exception e) {
            return domain;
        }
    }

    public String encodedDomain(String domain) {
        try {
            if (null == domain) return null;
            return java.net.URLEncoder.encode(domain, "UTF-8");
        } catch (Exception e) {
            return domain;
        }
    }
    
    protected String decodedResource(String resource){
        try{
            return java.net.URLDecoder.decode(resource, "UTF-8");
        } catch (Exception e) {
            return resource;
        }
    }
}
