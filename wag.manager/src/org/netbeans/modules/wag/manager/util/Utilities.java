/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wag.manager.util;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSocketFactory;


/**
 *
 * @author peterliu
 */
public class Utilities {

    private static SSLSocketFactory defaultSSLSocketFactory =
            HttpsURLConnection.getDefaultSSLSocketFactory();

    public static String convertToCallableName(String name) {
        if (name.startsWith("/")) {
            name = name.substring(1, name.length());
        }

        name = name.replaceAll("/", ".");

        System.out.println("converted name = " + name);
        return name;
    }

    public static void handleException(Exception ex) {
        //ex.printStackTrace();
        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getMessage());
        DialogDisplayer.getDefault().notify(msg);
    }

    public static void trustZemblyCertificate() {
        SSLContext context;
        TrustManager[] trustManagers = new TrustManager[]{new ZemblyX509TrustManager()};

        try {
            context = SSLContext.getInstance("SSL");
            context.init(null, trustManagers, new SecureRandom());
        } catch (GeneralSecurityException gse) {
            throw new IllegalStateException(gse.getMessage());
        } // catch


        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    public static void untrustZemblyCertificate() {
        HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
    }

    private static class ZemblyX509TrustManager implements X509TrustManager {

       private static final X509Certificate[] acceptedIssuers =
           new X509Certificate[] {};

       public void checkClientTrusted(X509Certificate[] chain,
           String authType) {
       }

       public void checkServerTrusted(X509Certificate[] chain,
           String authType) {
       }

       public X509Certificate[] getAcceptedIssuers() {
           return(acceptedIssuers);
       }
    }
}
