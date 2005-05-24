/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * PortInfoMapping.java
 *
 * Created on October 27, 2003, 8:39 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort;


/** Class that associates a PortInfo with a string so we can do combobox
 *  selection easiser.
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class PortInfoMapping {

	private PortInfo portInfo;
	private String displayText;
	private boolean textOutOfDate;

	public PortInfoMapping(final PortInfo pi) {
		portInfo = pi;
		displayText = buildDisplayText();
	}

	public PortInfoMapping(final PortInfo pi, final String display) {
		portInfo = pi;
		displayText = display;
	}

	public String toString() {
		if(textOutOfDate) {
			displayText = buildDisplayText();
		}

		return displayText;
	}

	public PortInfo getPortInfo() {
		return portInfo;
	}

	public void updateDisplayText() {
		textOutOfDate = true;
	}

	private String buildDisplayText() {
		String sei = portInfo.getServiceEndpointInterface();
		WsdlPort wsdl = portInfo.getWsdlPort();
		String localPart = null;
		String namespaceURI = null;

		if(wsdl != null) {
			localPart = wsdl.getLocalpart();
			namespaceURI = wsdl.getNamespaceURI();
		}

		StringBuffer resultBuf = new StringBuffer(128);
		boolean separator = false;

		if(sei != null && sei.length() > 0) {
			resultBuf.append(sei);
			separator = true;
		}

		if(localPart != null && localPart.length() > 0) {
			if(separator) {
				resultBuf.append(", ");
			}

			resultBuf.append(localPart);
			separator = true;
		}

		if(namespaceURI != null && namespaceURI.length() > 0) {
			if(separator) {
				resultBuf.append(", ");
			}

			resultBuf.append(namespaceURI);
		}

		if(resultBuf.length() == 0) {
			resultBuf.append(ServiceRefCustomizer.bundle.getString("LBL_UntitledPortInfo"));
		}

		textOutOfDate = false;

		return resultBuf.toString();
	}
}
