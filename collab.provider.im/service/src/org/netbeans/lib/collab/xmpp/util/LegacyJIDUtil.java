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
