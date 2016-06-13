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
public abstract class JIDUtilProviderImpl implements JIDUtil.Provider{

    public JID encodedJID(String s) {
        if (null == s) return null;
        try {
	    String resource = StringUtility.getResource(s);
	    String bare = StringUtility.removeResource(s);
	    String domain = getDomainFromAddress(bare, bare);
	    String node = null;
	    if (!domain.equals(bare)) {
		node = getLocalPartFromAddress(bare);
	    }
	    return encodedJID(node, domain, resource);
        } catch (Exception e) {
            return null;
        }
    }

    public JID encodedJID(String node, String domain, String resource) {
        try {
            String encodedNode = encodedNode(node);
            String encodedDomain = encodedDomain(domain);
            String encodedResource = encodedResource(resource);
            return new JID(encodedNode, encodedDomain, encodedResource);
        } catch (Exception e) {
            return new JID(node, domain, resource);
        }
    }

    public String decodedJID(JID jid) {
        if (null == jid) return null;
        try {
            String node = decodedNode(jid);
            String domain = decodedDomain(jid);
            String resource = decodedResource(jid);
            String jidString = domain;
            if (node != null && !node.equals("")) {
                jidString = node + "@" + jidString;
            }
            if (resource != null && !resource.equals("")) {
                jidString = jidString + "/" + resource;
            }
            return jidString;
        } catch (Exception e) {
            return jid.toString();
        }
    }

    public String decodedJID(String s) {
        if (null == s) return null;
        try {
            String resource = StringUtility.getResource(s);
            String bare = StringUtility.removeResource(s);
            String domain = getDomainFromAddress(bare, bare);
            String node = null;
            if (!domain.equals(bare)) node = getLocalPartFromAddress(bare);
            String jidString = decodedDomain(domain);
            if (node != null && !node.equals("")) {
                jidString = decodedNode(node) + "@" + jidString;
            }
            if (resource != null && !resource.equals("")) {
                jidString = jidString + "/" + decodedResource(resource);
            }
            return jidString;
        } catch (Exception e) {
            return s;
        }
    }


    public String getBareJIDString(String s) {
        return null != s ? StringUtility.removeResource(s) : null;
    }

    public String getLocalPartFromAddress(String in) {
        if (null == in) return null;
        int i = in.lastIndexOf('@');
        if (-1 != i) {
            if (0 == i || in.charAt(i-1) != '\\') return in.substring(0, i);
        }
        return in;
    }

    public String appendDomainToAddress(String in, String defaultDomain) {
        if (null == in) return null;
        int i = in.lastIndexOf('@');
        if (-1 != i) {
            if (0 == i || in.charAt(i-1) != '\\') return in;
        }
        return in + "@" + defaultDomain;
    }


    public String encodedString(String s) {
        return encodedNode(s);
    }

    public String decodedString(String s) {
        return decodedNode(s);
    }

    public String getDomainFromAddress(String in, String defaultDomain) {
        if (in == null) return null;
        int i = in.lastIndexOf('@');
        if (-1 != i) {
            if (0 == i || in.charAt(i-1) != '\\') return in.substring(i+1);
        }
        return defaultDomain;
    }


    public boolean hasDomain(String in) {
        if (in == null) return false;
        int i = in.lastIndexOf('@');
        if (-1 != i) {
            if (0 == i || in.charAt(i-1) != '\\') return true;
        }
        return false;
    }

    public String quoteSpecialCharacters(String in) {
        if (null == in) return in;
        int inlen = in.length();
        char[] inchars = new char[inlen];
        StringBuffer out = new StringBuffer(inlen);
        
        in.getChars(0, inlen, inchars, 0);
        for (int i = 0; i < inchars.length; i++) {
            if (inchars[i] == '@' || inchars[i] == '\\') {
                out.append('\\');
            }
            out.append(inchars[i]);
        }
        
        return out.toString();
    }

    public String unquoteSpecialCharacters(String in) {
        if (null == in) return in;
        int inlen = in.length();
        char[] inchars = new char[inlen];
        StringBuffer out = new StringBuffer(inlen);
        
        in.getChars(0, inlen, inchars, 0);
        for (int i = 0; i < inchars.length; i++) {
            if (inchars[i] == '\\') {
                if (i < inchars.length - 1) {
                    i++;
                    out.append(inchars[i]);
                }
            } else {
                out.append(inchars[i]);
            }
        }
        
        return out.toString();
    }
    
    protected abstract String encodedResource(String resource);
    
    protected abstract String decodedResource(String resource);
    
}
