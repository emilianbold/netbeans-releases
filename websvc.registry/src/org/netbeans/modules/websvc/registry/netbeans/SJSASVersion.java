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

package org.netbeans.modules.websvc.registry.netbeans;

import java.io.File;

import org.openide.ErrorManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;


/** This class represents the Sun Java System Application Version installed
 *  (from which this module uses JWSDP jars for Web Service runtime support.)
 *
 * @author Peter Williams
 */
public final class SJSASVersion {
	
	private static String REGISTRY_JARS_8_0_AND_8_1_BETA [] = {
		"/lib/j2ee.jar",
		"/lib/jaxrpc-api.jar",
		"/lib/jaxrpc-spi.jar",
		"/lib/jaxrpc-impl.jar",
		"/lib/endorsed/xercesImpl.jar",
		"/lib/endorsed/dom.jar",
		"/lib/endorsed/xalan.jar",
		"/lib/activation.jar",
		"/lib/mail.jar",
		"/lib/xsdlib.jar",
		"/lib/relaxngDatatype.jar",
		"/lib/commons-logging.jar",
		"/lib/namespace.jar",
		"/lib/jaxr-impl.jar",
		"/lib/saaj-api.jar",
		"/lib/saaj-impl.jar",
		"/lib/jax-qname.jar"
	};
	
	private static String REGISTRY_JARS_8_1 [] = {
		"/lib/j2ee.jar",
		"/lib/jaxrpc-api.jar",
		"/lib/jaxrpc-spi.jar",
		"/lib/jaxrpc-impl.jar",
		"/lib/xercesImpl.jar",
		"/lib/dom.jar",
		"/lib/xalan.jar",
		"/lib/activation.jar",
		"/lib/mail.jar",
		"/lib/xsdlib.jar",
		"/lib/relaxngDatatype.jar",
		"/lib/commons-logging.jar",
		"/lib/namespace.jar",
		"/lib/jaxr-impl.jar",
		"/lib/saaj-api.jar",
		"/lib/saaj-impl.jar",
		"/lib/jax-qname.jar"
	};
	
	private static String SAX_PARSER_IMPL_8_0 = "org.apache.xerces.jaxp.SAXParserFactoryImpl"; // NOI18N
	private static String SAX_PARSER_IMPL_8_1 = "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"; // NOI18N
	
	// Appserver version strings.  We do not differ between minor versions (e.g. 8.0.0.1 is the same as 8.0)
	public static final SJSASVersion APPSERVER_VERSION_8_0 = new SJSASVersion(
		"8.0", REGISTRY_JARS_8_0_AND_8_1_BETA, SAX_PARSER_IMPL_8_0); // NOI18N
	
	// AKA SJSAS 8.1 2004Q4
	public static final SJSASVersion APPSERVER_VERSION_8_1_BETA = new SJSASVersion(
		"8.1 beta", REGISTRY_JARS_8_0_AND_8_1_BETA, SAX_PARSER_IMPL_8_1); // NOI18N	
	
	// AKA SJSAS 8.1 2005Q1
	public static final SJSASVersion APPSERVER_VERSION_8_1 = new SJSASVersion(
		"8.1", REGISTRY_JARS_8_1, SAX_PARSER_IMPL_8_1); // NOI18N
	
	// unknown defaults to 8.1 release behavior (but also prompts a warning message)
	public static final SJSASVersion APPSERVER_VERSION_UNKNOWN = new SJSASVersion(
		"unknown", REGISTRY_JARS_8_1, SAX_PARSER_IMPL_8_1); // NOI18N

	private String sjsasVersion;
	private String [] registryRuntimeLibraries;
	private String saxParserImplClass;
	
	private SJSASVersion(String version, String [] registryRuntimeLibs, String saxParserImplClass) {
		if(version == null) {
			throw new NullPointerException("Null Application Server Version is not allowed.");
		}
		
		this.sjsasVersion = version;
		this.registryRuntimeLibraries = registryRuntimeLibs;
		this.saxParserImplClass = saxParserImplClass;
	}
	
	public String toString() {
		return sjsasVersion;
	}
	
	public boolean equals(Object obj) {
		SJSASVersion target = (SJSASVersion) obj;
		return sjsasVersion.equals(target.sjsasVersion);
	}
	
	public int hashCode() {
		return sjsasVersion.hashCode();
	}
	
	public String [] getRegistryRuntimeLibraries() {
		return registryRuntimeLibraries;
	}
	
	public String getSaxParserImplClass() {
		return saxParserImplClass;
	}
	
	/** Attempt to discern the application server version we're running against.
	 *
	 * 8.0 uses sun-domain_1_0.dtd
	 * 8.1 uses sun-domain_1_1.dtd (also includes the 1_0 version for backwards compatibility)
	 *
	 */
	public static SJSASVersion getSJSAppServerVersion() {
		SJSASVersion version = APPSERVER_VERSION_UNKNOWN;	// NOI18N
                String serverInstanceIDs[] = Deployment.getDefault().getServerInstanceIDs ();
                J2eePlatform platform = null;
                for (int i = 0; i < serverInstanceIDs.length; i++) {
                    J2eePlatform p = Deployment.getDefault().getJ2eePlatform (serverInstanceIDs [i]);
                    if (p != null && p.isToolSupported ("wscompile")) {
                        platform = p;
                        break;
                    }
                }
		File asInstallRoot = platform == null ? null : platform.getPlatformRoots () [0];
		if(asInstallRoot != null && asInstallRoot.exists()) {
			File sunDomain11Dtd = new File(asInstallRoot, "lib/dtds/sun-domain_1_1.dtd"); // NOI18N
			if(sunDomain11Dtd.exists()) {
				File endorsedXerces = new File(asInstallRoot, "lib/endorsed/xercesImpl.jar");
				if(endorsedXerces.exists()) {
					// SJSAS 8.1 Beta had xercesImpl, xalan, and dom in the endorsed lib directory
					version = APPSERVER_VERSION_8_1_BETA;
				} else {
					// SJSAS 8.1 moved xercesImpl, xalan, and dom to the main lib directory
					version = APPSERVER_VERSION_8_1;
				}
			} else {
				// SJSAS 8.0 does not support sun-domain 1.1, only 1.0.
				version = APPSERVER_VERSION_8_0;
			}
		}

		if(APPSERVER_VERSION_UNKNOWN.equals(version)) {
			ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, 
				"Cannot determine version of installed Sun Java System Application Server.");
		}
		
		return version;
	}
}
