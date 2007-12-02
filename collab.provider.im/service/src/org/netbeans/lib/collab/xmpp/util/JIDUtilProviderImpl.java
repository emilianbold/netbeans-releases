/*
 * Copyright 2007 Sun Microsystems, Inc.  All rights reserved.
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
