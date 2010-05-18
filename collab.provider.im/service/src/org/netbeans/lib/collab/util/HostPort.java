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


/**
 * Utility fo parsing host:port constructs.
 *
 */
public class HostPort
{

    private int _port;
    private String _hostName = null;

    public HostPort(String s, int defaultPort)
    {
        int n = 0;
        _port = defaultPort;
        if (s != null) {
	    s = s.trim();
	    n = s.indexOf(':');
	}
        if (n > 0) {
            String sPort = s.substring(n+1);
            try{
                _port = Integer.parseInt(sPort.trim());
            } catch(NumberFormatException nfEx){}
            _hostName = s.substring(0, n).trim();
        } else if (s != null && s.length() > 0 &&
                   Character.isDigit(s.charAt(0)) &&
                   s.indexOf(".") < 0) {
            // port number only
            try {
                _port = Integer.parseInt(s);
            } catch(NumberFormatException nfEx){}
        } else {
            // host name, IP address, or null
           if ("".equals(s))
               _hostName = null;
           else
               _hostName = s;
        }
    }

    public String getHostName()
    {
        return (_hostName != null) ? _hostName : "localhost";
    }

    public int getPort()
    {
        return _port;
    }

    /**
     * This method can be used only when the default port specified was 0
     * @param port The new port number
     * @throws IllegalStateException
     */
    public void setPort(int port) {
        if (_port != 0) throw new IllegalStateException();
        _port = port;
    }
    
    public InetAddress getHost() throws UnknownHostException
    {
        return (_hostName != null) ? InetAddress.getByName(_hostName) : null;
    }

    public String toString()
    {
        return getHostName() + ":" + _port;
    }

    public boolean equals(Object o)
    {
        if (o instanceof HostPort) {
            HostPort hp = (HostPort)o;
            try {
                return ((_port == hp.getPort()) &&
                        ((getHost() == null && hp.getHost() == null) ||
                         (getHost() != null && getHost().equals(hp.getHost()))));
            } catch (Exception e) {
                return false;
            }
        } else {
            return this.equals(new HostPort(o.toString(), 0));
        }
    }

}
