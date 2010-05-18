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

package org.netbeans.lib.collab.util;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.naming.*;
import javax.naming.directory.*;


/**
 * Utility class to perform DNS lookups.
 *
 * The original version of this code was contributed to the jdev mailing
 * list by Matt Tucker
 *
 * @author Jacques Belissent
 *
 */
public class SRVLookup {

    private static DirContext context;

    static {
        try {
            Hashtable env = new Hashtable();
            env.put("java.naming.factory.initial",
		    "com.sun.jndi.dns.DnsContextFactory");
            context = new InitialDirContext(env);
        } catch (Exception e) {
            //Log.error(e);
        }
    }

    /**
     * Returns the host name and port of a service specified by prefix
     * for a given domain.
     * A DNS SRV lookup for a record in the form <i>prefix</i>domain 
     * is attempted. 
     * If that lookup fails, it's assumed that the service lives at
     * the host resolved by a DNS Address lookup at the specified
     * domain on the specified default port.
     *
     * As an example, a lookup for "example.com" may return
     * "im.example.com:5269".
     *
     * @param domain the domain to resolve
     * @param prefix DNS SRV prefix of the form service.protocol.
     * @param defaultPort port to use if none other is specified.
     * @return a SocketAddress based on the obtained host and port
     */
    public static InetSocketAddress lookup(String domain,
					   String service, String transport,
					   int defaultPort)
	throws IllegalArgumentException, UnknownHostException, NamingException
    {
        if (context == null) {
            return new InetSocketAddress(domain, defaultPort);
        }

        String host = domain;
        int port = defaultPort;

	Attributes dnsLookup = context.getAttributes("_" + service + "._" + transport + "." + domain, new String[]{"SRV"});
	String srvRecord = (String)((Attribute)dnsLookup.getAll().next()).get();
	String[] srvRecordEntries = srvRecord.split(" ");
	port = Integer.parseInt(srvRecordEntries[srvRecordEntries.length-2]);
	host = srvRecordEntries[srvRecordEntries.length-1];

        // Host entries in DNS should end with a ".".
        if (host.endsWith(".")) {
            host = host.substring(0, host.length()-1);
        }

        return new InetSocketAddress(host, port);
    }


    public static InetSocketAddress XMPPServer(String domain)
    {
        return XMPPServer(domain, 5269);
    }

    public static InetSocketAddress XMPPServer(String domain,
					       int defaultPort)
    {
	InetSocketAddress sa = null;
	try {
	    sa = lookup(domain, "xmpp-server", "tcp", defaultPort);
	} catch (Exception e) {
	    try {
		sa = lookup(domain, "jabber", "tcp", defaultPort);
	    } catch (Exception ee) {
	    }
	}

        if (sa == null) {
            return new InetSocketAddress(domain, defaultPort);
        }

	return sa;
    }

    public static InetSocketAddress XMPPClient(String domain)
    {
	return XMPPClient(domain, 5222);
    }

    public static InetSocketAddress XMPPClient(String domain,
					       int defaultPort)
    {
	InetSocketAddress sa = null;
	try {
	    sa = lookup(domain, "xmpp-client", "tcp", defaultPort);
	} catch (Exception e) {
	    try {
		sa = lookup(domain, "jabber", "tcp", defaultPort);
	    } catch (Exception ee) {
	    }
	}

	if (sa == null) {
	    return new InetSocketAddress(domain, defaultPort);
	}

	return sa;
    }

   /**
    * Usage:
    *    java org.netbeans.lib.collab.util.SRVLookup domain default-port [service transport]
    * Examples:
    *    java org.netbeans.lib.collab.util.SRVLookup jabber.org 5269
    *    java org.netbeans.lib.collab.util.SRVLookup jabber.org 5269 xmpp-server tcp
    *    java org.netbeans.lib.collab.util.SRVLookup jabber.org 5222 xmpp-client tcp
    */
    public static void main(String[] arg) 
    {
	try {
	    SocketAddress sa = null;
            if (arg.length == 2) {
                sa = SRVLookup.XMPPServer(arg[0],
				       Integer.parseInt(arg[1]));
            } else if (arg.length == 4) {
	        sa = lookup(arg[0], arg[2], arg[3],
			       Integer.parseInt(arg[1]));
            } else {
                System.out.println("Usage:\njava org.netbeans.lib.collab.util.SRVLookup domain default-port [service transport]");
                System.exit(1);
            }
	    System.out.println(sa);
	    //return 0;
	} catch (Exception e) {
	    e.printStackTrace();
	    //return 1;
	}
    }
} 
