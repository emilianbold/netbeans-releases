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
/*
 * PortInfoMapping.java
 *
 * Created on October 27, 2003, 8:39 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;


/** Class that associates a web service endpoint with a string so we can do combobox
 *  selection easiser.
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class EndpointMapping {

	private WebserviceEndpoint endpoint;
	private String displayText;
	private boolean textOutOfDate;

	public EndpointMapping(final WebserviceEndpoint e) {
		endpoint = e;
		displayText = buildDisplayText();
	}

	public EndpointMapping(final WebserviceEndpoint e, final String display) {
		endpoint = e;
		displayText = display;
	}

	public String toString() {
		if(textOutOfDate) {
			displayText = buildDisplayText();
		}

		return displayText;
	}

	public WebserviceEndpoint getEndpoint() {
		return endpoint;
	}

	public void updateDisplayText() {
		textOutOfDate = true;
	}

	private String buildDisplayText() {
		String name = endpoint.getPortComponentName();
		StringBuffer resultBuf = new StringBuffer(128);

		if(name != null && name.length() > 0) {
			resultBuf.append(name);
		} else {
			resultBuf.append(WebServiceDescriptorCustomizer.bundle.getString("LBL_UntitledEndpoint"));
        }

		textOutOfDate = false;
		return resultBuf.toString();
	}
}
