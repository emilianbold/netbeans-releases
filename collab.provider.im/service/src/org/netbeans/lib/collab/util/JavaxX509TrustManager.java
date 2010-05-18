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

import java.io.*;

import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;



/**
 *
 * @author Jacques Belissent
 * @author Rahul Shah
 * @author Vijayakumar Palaniappan
 * 
 */
public class JavaxX509TrustManager implements X509TrustManager {
    
    private static KeyStore ks = null;
    private static TrustManagerFactory tmFactory = null;

    //member variables
    TrustManager[] m_trustManagers = null;
    CertificateVerify ci;
    SSLContext ctx;
    
    public JavaxX509TrustManager(CertificateVerify ci)
    throws NoSuchAlgorithmException, KeyStoreException,
	FileNotFoundException, java.security.KeyManagementException,
	IOException,
	CertificateException{
        this (ci , "SSLv3");
    }

    public JavaxX509TrustManager(CertificateVerify ci , String algo)
    throws NoSuchAlgorithmException, KeyStoreException,
	FileNotFoundException, java.security.KeyManagementException,
	IOException,
	CertificateException
    {        

	if (tmFactory == null) init();

        this.ci = ci;
        m_trustManagers = tmFactory.getTrustManagers();

	ctx = SSLContext.getInstance(algo);
	TrustManager[] a_tm = new TrustManager[1];
	a_tm[0] = this;
	ctx.init(null, a_tm, null);
    }
    
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException, IllegalArgumentException {
        //delegate to default trust managers
	Exception failure = null;
        for (int i=0; i<m_trustManagers.length; i++) {
            X509TrustManager tm = (X509TrustManager)m_trustManagers[i];
	    try {
		tm.checkClientTrusted(chain, authType);
		return;
	    } catch(Exception e) {
		failure = e;
		// try next
	    }
        }
        
        //if it used on server side, we can filter client
        //certificate decisions also
        //but for now we can ignore this
        
        if (failure != null) {
	    if (failure instanceof CertificateException) {
		throw (CertificateException)failure;
	    } else {
		throw (IllegalArgumentException)failure;
	    }
	}
    }
    
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException, IllegalArgumentException 
    {        
	Exception failure = null;
        //System.out.println("trustmanagers " + m_trustManagers.length);
        for (int i=0; i<m_trustManagers.length; i++) {
            X509TrustManager tm = (X509TrustManager)m_trustManagers[i];
            //System.out.println("tm " + tm);
            //System.out.println("chain " + chain);
	    try {
		tm.checkServerTrusted(chain, authType);
                return;
	    } catch (Exception e) {
		failure = e;
		// try next
	    }
        }
        
        if (ci == null) { 
            if (failure != null) {
		if (failure instanceof CertificateException) {
		    throw (CertificateException)failure;
		} else {
		    throw (IllegalArgumentException)failure;
		}
	    }

        } else {
            if (!ci.doYouTrustCertificate(chain)) {
		throw new CertificateException("Rejected");
	    }
        }
    }
    
    
    public X509Certificate[] getAcceptedIssuers() {
        for (int i=0; i<m_trustManagers.length; i++) {
            if (m_trustManagers[i] instanceof X509TrustManager) {
                return ((X509TrustManager)m_trustManagers[i]).getAcceptedIssuers();
            }
        }
        return null;
    }    
    
    
    public SSLSocketFactory getSocketFactory() throws IOException
    {
	return ctx.getSocketFactory();
    }
    
    private static void init() throws NoSuchAlgorithmException, KeyStoreException,
	FileNotFoundException,
	IOException,
	CertificateException
    {
        String libdir = System.getProperty("java.home");
        libdir += File.separator;
        libdir += "lib";
        libdir += File.separator;
        libdir += "security";
        libdir += File.separator;
        //System.out.println(libdir);
        File f = new File(libdir, "nlcacerts");
        if (!f.exists()) {
            f = new File(libdir,"cacerts");
            if (!f.exists()) {
                f = null;
            }
        }
        
        ks = null;
        if (f == null) {
	    ks = null;
        } else {
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(f), null);
            
        }

	tmFactory = TrustManagerFactory.getInstance("SunX509");
	tmFactory.init(ks);
    }

    public static SSLServerSocketFactory getServerSocketFactory(KeyStore ks, String passphrase) throws NoSuchAlgorithmException, KeyStoreException, java.security.KeyManagementException, java.security.UnrecoverableKeyException
    {
	char[] c = passphrase.toCharArray();

	KeyManagerFactory kmf;
	kmf = KeyManagerFactory.getInstance("SunX509");
	kmf.init(ks, c);

	SSLContext ctx;
	ctx = SSLContext.getInstance("SSLv3");
	ctx.init(kmf.getKeyManagers(), null, null);
	return ctx.getServerSocketFactory();
    }
    
}






