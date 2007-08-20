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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mercurial.ui.repository;

import java.net.MalformedURLException;
import org.openide.util.NbBundle;

/**
 * We could have used URL with custom protocol handler for ssh 
 * (see http://java.sun.com/developer/onlineTraining/protocolhandlers/index.html)
 * but that is overkill for what we want which is a string to represent the URL.
 *
 * @author Padraig O'Briain
 */
public class HgURL {
    
    private static final char SEGMENT_SEPARATOR = '/';
    
    private String protocol;
    private String host;
    private String password;
    private int port;
    
    public HgURL(String hgUrl)  throws MalformedURLException {
        if (hgUrl == null) 
            throw new MalformedURLException(NbBundle.getMessage(HgURL.class, "MSG_URL_NULL"));
        parseUrl(hgUrl);
    }

    /**
     * verifies that url is correct
     * @throws malformedURLException
     */
    private void parseUrl(String hgUrl) throws MalformedURLException {
        String parsed = hgUrl;

        int hostIdx = parsed.indexOf("://");                         // NOI18N
        if (hostIdx == -1)
            throw new MalformedURLException(NbBundle.getMessage(HgURL.class, "MSG_INVALID_URL", hgUrl));
        protocol = parsed.substring(0, hostIdx).toLowerCase();

        if ((!protocol.equalsIgnoreCase("http")) &&
            (!protocol.equalsIgnoreCase("https")) &&
            (!protocol.equalsIgnoreCase("file")) &&
            (!protocol.equalsIgnoreCase("static-http")) &&
            (!protocol.equalsIgnoreCase("ssh")) ) {
                throw new MalformedURLException(NbBundle.getMessage(HgURL.class, "MSG_INVALID_URL", hgUrl));
        }
        parsed = parsed.substring(hostIdx + 3);
        if (parsed.length() == 0) {
            throw new MalformedURLException(NbBundle.getMessage(HgURL.class, "MSG_INVALID_URL", hgUrl));
        }
    }

    /**
     * get the protocol
     * @return either http, https, file, static-http, ssh
     */ 
    public String getProtocol() {
        return protocol;
    }
}
