/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
