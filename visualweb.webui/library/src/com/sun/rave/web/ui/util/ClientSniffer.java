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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package com.sun.rave.web.ui.util;

import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;


/**
 * This utility class parses the user agent of a HttpServletRequest
 * object to determine browser type, version, and platform.
 * <p>
 * The code of this utility class is based on "The
 * Ultimate JavaScript Client Sniffer", version 3.03 which is located
 * at the following URL.
 * </p><p>
 * http://www.mozilla.org/docs/web-developer/sniffer/browser_type.html
 * </p><p>
 * Usage Example:
 * </p><p><pre>
 * FacesContext context = FacesContext.getCurrentInstance();
 * ClientSniffer cs = new ClientSniffer(context);
 *
 * String stylesheet = CCStyle.IE6_UP_CSS;
 *
 * if (isIe6up()) {
 *     stylesheet = CCStyle.IE6_UP_CSS;
 * } else if (isIe5up()) {
 *     stylesheet = CCStyle.IE5_UP_CSS;
 * } else if (isNav6up()) {
 *     stylesheet = CCStyle.NS6_UP_CSS;
 * } else if (isNav4up() && isWin()) {
 *     stylesheet = CCStyle.NS4_WIN_CSS;
 * } else if (isNav4up() && isSun()) {
 *     stylesheet = CCStyle.NS4_SOL_CSS;
 * }
 * </pre></p><p>
 *
 * @version 1.10 02/06/04
 * @author  Sun Microsystems, Inc.
 */
public class ClientSniffer {
    // User Agent Headers (DON'T DELETE).
    //
    // Windows 2000
    // ------------
    //
    // IE 5.0		Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)
    // Netscape 4.7     Mozilla/4.7 [en] (WinNT; U)
    // Netscape 6.2.1   mozilla/5.0 (windows; u; win98; en-us; rv:0.9.4)
    //			gecko/20011128 netscape6/6.2.1
    // Netscape 7.02    Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US;
    //			rv:1.0.2) Gecko/20030208 Netscape/7.02
    // Netscape 7.1	Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.4)
    // 			Gecko/20030624 Netscape/7.1 (ax)
    // Mozilla 1.4	Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.4)
    //			Gecko/20030624
    //
    // SunOS
    // -----
    //
    // Netscape 4.78    Mozilla/4.78 [en] (X11; U; SunOS 5.10 sun4u)
    // Netscape 6.2.1   mozilla/5.0 (x11; u; sunos sun4u; en-us; rv:0.9.4)
    // 			gecko/20011206 netscape6/6.2.1
    // Netscape 6.2.2   Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:0.9.4.1)
    // 			Gecko/20020406 Netscape6/6.2.2
    // Netscape 7.0	    Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:1.0.1)
    //			Gecko/20020920 Netscape/7.0
    // Mozilla 1.1 	    Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:1.1)
    //			Gecko/20020827
    // HotJava 1.0.1    HotJava/1.0.1/JRE1.1.3
    // Generic		Profile/MIDP-1.0 Configuration/CLDC-1.0

    // User agent.
    private String agent = null;

    // User agent major version number.
    private int major = -1;

    /**
     *	Default constructor.
     *
     *	@param context	<code>FacesContext</code> which should be used to
     *			extract the user agent.
     */
    public ClientSniffer(FacesContext context) {
	String version = null;
	setUserAgent(context);
        // <RAVE>
	agent = getUserAgent();
        // </RAVE>
        
	// Parse user agent.
	if (agent != null) {
            
	    StringTokenizer st = new StringTokenizer(agent, "/");

	    // Parse out user agent name.
	    if (st.hasMoreTokens()) {
		st.nextToken();
	    }

	    // Get user agent version number.
	    if (st.hasMoreTokens()) {
		version = st.nextToken();
	    }

	    // Remove white space & extra info.
	    st = new StringTokenizer(version);

	    if (st.hasMoreTokens()) {
		version = st.nextToken();
	    }
	}

	// Parse user agent major version number.
	if (version != null) {
	    StringTokenizer st = new StringTokenizer(version, ".");

	    if (st.hasMoreTokens()) {
		try {
		    major = Integer.parseInt(st.nextToken());
		} catch (NumberFormatException ex) {
		    // Ignore
		}
	    }
	}
    }

    /**
     *	<P> This method gets an instance of this class associated with the
     *	    given <code>FacesContext</code>.  It will look in the request scope
     *	    to see if an instance already exists, if not, it will create
     *	    one.</P>
     *
     *	@param	context	The <code>FacesContext</code>
     *
     *	@return	A <code>ClientSniffer</code> instance.
     */
    public static ClientSniffer getInstance(FacesContext context) {
	// Look for a cached one
	Map requestMap = context.getExternalContext().getRequestMap();
	ClientSniffer sniffer = (ClientSniffer) requestMap.get("__sniffer");

	if (sniffer == null) {
	    // Not yet created, create one
	    sniffer = new ClientSniffer(context);
	    requestMap.put("__sniffer", sniffer);
	}

	// Return the sniffer
	return sniffer;
    }

    /**
     *	<P> This method initializes the user agent via the supplied
     *	    <code>FacesContext</code>.  It will use the
     *	    <code>ExternalContext</code> to get at the request header Map.
     *	    It will use this Map to obtain the value for
     *	    <code>USER-AGENT</code>.</P>
     *
     *	@param	context	The <code>FacesContext</code>
     */
    protected void setUserAgent(FacesContext context) {
        // <RAVE>
        Map headerMap  = context.getExternalContext().getRequestHeaderMap();
        if(null != headerMap) {
            agent = (String) headerMap.get("USER-AGENT");
            if(null != agent) {
                agent = agent.toLowerCase();
            }
        }
        // </RAVE>
    }

    /**
     * Get the user agent.
     *
     * @return The user agent.
     */
    public String getUserAgent() {
	return agent;
    }

    /**
     * Get the user agent major version number.
     *
     * @return The user agent major version number or
     * -1 if the version number was not retrieved.
     */
    public int getUserAgentMajor() {
	return major;
    }

    /**
     * Test if the user agent was generated on Windows platform.
     *
     * @return true or false
     */
    public boolean isWin() {
	boolean result = false;

	if ((agent != null) && ((agent.indexOf("win") != -1)
		|| (agent.indexOf("16bit") != -1))) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated on Sun platform.
     *
     * @return true or false
     */
    public boolean isSun() {
	boolean result = false;

	if ((agent != null) && (agent.indexOf("sunos") != -1)) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Gecko engine.
     *
     * @return true or false
     */
    public boolean isGecko() {
	boolean result = false;

	if ((agent != null) && (agent.indexOf("gecko") != -1)) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Navigator.
     *
     * @return true or false
     */
    public boolean isNav() {
	boolean result = false;

	if ((agent != null)
		&& (agent.indexOf("mozilla") != -1)
		&& (agent.indexOf("spoofer") == -1)
		&& (agent.indexOf("compatible") == -1)
		&& (agent.indexOf("opera") == -1)
		&& (agent.indexOf("webtv") == -1)
		&& (agent.indexOf("hotjava") == -1)) {
	    // The header for Netscape 4.x is similar to the header
	    // for the Mozilla browser; however, Netscape 4.x does not
	    // implement the Gecko engine.
	    if (!(isGecko() && (agent.indexOf("netscape") == -1))) {
		result = true;
	    }
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Navigator,
     * version 4.x.
     *
     * @return true or false
     */
    public boolean isNav4() {
	boolean result = false;

	if (isNav() && (major == 4)) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Navigator,
     * version 4.x or above.
     *
     * @return true or false
     */
    public boolean isNav4up() {
	boolean result = false;

	if (isNav() && (major >= 4)) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Navigator,
     * version 6.x.
     *
     * @return true or false
     */
    public boolean isNav6() {
	boolean result = false;
        // <RAVE>
	if (isNav() && (major == 5) && (null != agent) &&
                (agent.indexOf("netscape6") != -1)) {
        // </RAVE>            
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Navigator,
     * version 6.x or above.
     *
     * @return true or false
     */
    public boolean isNav6up() {
	boolean result = false;

	if (isNav() && major >= 5) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Navigator,
     * version 7.x.
     *
     * @return true or false
     */
    public boolean isNav7() {
	boolean result = false;
        // <RAVE>
	if (isNav() && major == 5 && (null != agent)
		&& (agent.indexOf("netscape/7") != -1)) {
        // </RAVE>            
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Navigator,
     * version 7.0.
     *
     * @return true or false
     */
    public boolean isNav70() {
	boolean result = false;
        // <RAVE>
	if (isNav() && major == 5 && (null != agent)
		&& (agent.indexOf("netscape/7.0") != -1)) {
        // </RAVE>            
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Navigator,
     * version 7.x or above.
     *
     * @return true or false
     */
    public boolean isNav7up() {
	boolean result = false;

	if (isNav() && (major >= 5) && !isNav4() && !isNav6()) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer.
     *
     * @return true or false
     */
    public boolean isIe() {
	boolean result = false;

	if ((agent != null)
		&& (agent.indexOf("msie") != -1)
		&& (agent.indexOf("opera") == -1)) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer,
     * version 3.x.
     *
     * @return true or false
     */
    public boolean isIe3() {
	boolean result = false;

	if (isIe() && (major < 4)) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer,
     * version 4.x.
     *
     * @return true or false
     */
    public boolean isIe4() {
	boolean result = false;
        // <RAVE>
	if (isIe() && (major == 4) && (null != agent) 
        && (agent.indexOf("msie 4") != -1)) {
        // </RAVE>            
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer,
     * version 5.x.
     *
     * @return true or false
     */
    public boolean isIe5() {
	boolean result = false;
        // <RAVE>
	if (isIe() && (major == 4) && (null != agent) && (agent.indexOf("msie 5") != -1)) {
        // </RAVE>            
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer,
     * version 5.x or above.
     *
     * @return true or false
     */
    public boolean isIe5up() {
	boolean result = false;

	if (isIe() && !isIe3() && !isIe4()) {
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer,
     * version 6.x.
     *
     * @return true or false
     */
    public boolean isIe6() {
	boolean result = false;
        // <RAVE>
	if (isIe() && (major == 4) && (null != agent) && (agent.indexOf("msie 6") != -1)) {
        // </RAVE>            
	    result = true;
	}

	return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer,
     * version 6.x or above.
     *
     * @return true or false
     */
    public boolean isIe6up() {
	boolean result = false;

	if (isIe() && !isIe3() && !isIe4() && !isIe5()) {
	    result = true;
	}

	return result;
    }

    /**
     *	This method is used by the Theme.
     *
     *	@param	context	The <code>FacesContext</code>
     *
     *	@return	The {@link ClientType}.
     */
    public static ClientType getClientType(FacesContext context) {
        // </RAVE>
        Map map = context.getExternalContext().getRequestHeaderMap();
        if(null == map) {
            return ClientType.OTHER;            
        }
        String agent = (String) map.get("USER-AGENT");

        if (agent == null) {
            return ClientType.OTHER;
        }
        // </RAVE>
        agent = agent.toLowerCase();
        if (agent.indexOf("gecko") != -1) {
            return ClientType.GECKO;
        } else if (agent.indexOf("msie 6") != -1) {
            return ClientType.IE6;
        } else if (agent.indexOf("msie 5.5") != -1) {
            return ClientType.IE5_5;
        } else {
            return ClientType.OTHER;
        }
    }
}
