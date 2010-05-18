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
/*
 * X509TrustManager.java
 *
 * Created on December 8, 2004, 2:54 PM
 */

package org.netbeans.modules.j2ee.sun.bridge;

import com.sun.enterprise.admin.jmx.remote.https.SunOneBasicX509TrustManager;

import java.security.cert.X509Certificate;


/**
 *  An implementation of X509TrustManager that provides basic support for
 *  Trust Management.  This implementation prompts for confirmation of the
 *  server certificate.
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
class X509TrustManager extends SunOneBasicX509TrustManager {

    /**
     *	Creates an instance of the X509TrustManager
     *
     *	@param alias The toString() of the alias object concatenated with a
     *	date/time stamp is used as the alias of the trusted server certificate
     *	in the client side .asadmintruststore. When null only a date /
     *	timestamp is used as an alias.
     */
//    public X509TrustManager(Object alias) {
//	super(alias);
//    }


    /**
     *	Creates an instance of the X509TrustManager.  The date/time stamp is
     *	used of the trusted server certificate in the client side
     *	.asadmintruststore
     */
    public X509TrustManager() {
//	this(null);
	_alreadyInvoked = false;
    }


    /**
     *	Displays the certificate and prompts the user whether or not it is
     *	trusted.
     *
     *	@param cert
     *	@return true if the user trusts the certificate
     */
    protected boolean isItOKToAddCertToTrustStore(X509Certificate cert) {
        // The alreadyInvoked flag keeps track of whether we have already
	// prompted the user. Unfortunately, checkServerTrusted is called 2x
	// and we want to avoid prompting the user twice.
        if (_alreadyInvoked) {
            return false;
        }
	_alreadyInvoked = true;

	// Make sure we should prompt
	if (!promptForConfirmation()) {
	    // This should not happen, b/c this TrustManager always returns
	    // true.  However, if someone extends this class to change this
	    // method, then return "false" to indicate that we will NOT
	    // blindly accept certs.
	    return false;
	}

	// Show the prompt...
	return showConfirmDialog(null, cert.toString());


       // return true;
    }
    public static boolean showConfirmDialog(java.awt.Component c, String  m)
    {
        try {
            //	java.awt.Component frame = (c != null)? c : new javax.swing.JFrame();
            //	String title = (t != null)? t : "Confirm Certificate";
            //        int sel = javax.swing.JOptionPane.showConfirmDialog(frame, m, title, javax.swing.JOptionPane.YES_NO_OPTION);
            //	return (sel == javax.swing.JOptionPane.YES_OPTION);
                    return AcceptCertificate.acceptCertificatePanel(m);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /**
     *	This implementation of promptForConfirmation always returns "true".
     *	This method is here to be consistent with the super class.
     *
     *	@return true if the cert should be displayed and the user asked to
     *	confirm it. A return value of false indicates that the cert will be
     *	implicitly trusted and added to the asadmin truststore.
     */
    protected boolean promptForConfirmation() {
        return true;
    }


    private boolean _alreadyInvoked;
}
