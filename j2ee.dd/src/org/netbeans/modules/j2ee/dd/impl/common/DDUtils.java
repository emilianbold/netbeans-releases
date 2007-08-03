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
package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.netbeans.modules.schema2beans.Schema2BeansRuntimeException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.netbeans.modules.schema2beans.Schema2BeansException;

/**
 * @author pfiala
 */
public class DDUtils {
    public static EjbJarProxy createEjbJarProxy(InputStream inputStream) throws IOException {
        return createEjbJarProxy(new InputSource(inputStream));
    }
    
    public static EjbJarProxy createEjbJarProxy(Reader reader) throws IOException {
        return createEjbJarProxy(new InputSource(reader));
    }
    
    public static EjbJarProxy createEjbJarProxy(InputSource inputSource) throws IOException {
        try {
            return (EjbJarProxy) DDProvider.getDefault().getDDRoot(inputSource);
        } catch (SAXException ex) {
            // XXX lets throw an exception here
            EjbJar ejbJar = org.netbeans.modules.j2ee.dd.impl.ejb.model_2_0.EjbJar.createGraph();
            EjbJarProxy ejbJarProxy = new EjbJarProxy(ejbJar, ejbJar.getVersion().toString());
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                ejbJarProxy.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                ejbJarProxy.setError((SAXParseException) ex.getException());
            }
            return ejbJarProxy;
        }
    }
    
    
    public static void merge(EjbJarProxy ejbJarProxy, Reader reader) {
        try {
            EjbJarProxy newEjbJarProxy = createEjbJarProxy(reader);
            if (newEjbJarProxy.getStatus() == EjbJar.STATE_INVALID_UNPARSABLE) {
                ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                ejbJarProxy.setError(newEjbJarProxy.getError());
                return;
            }
            ejbJarProxy.merge(newEjbJarProxy, EjbJar.MERGE_UPDATE);
            ejbJarProxy.setStatus(newEjbJarProxy.getStatus());
            ejbJarProxy.setError(newEjbJarProxy.getError());
        } catch (IOException ex) {
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            // cbw if the state of the xml file transitions from
            // parsable to unparsable this could be due to a user
            // change or cvs change. We would like to still
            // receive events when the file is restored to normal
            // so lets not set the original to null here but wait
            // until the file becomes parsable again to do a merge
            //ejbJarProxy.setOriginal(null);
        } catch (Schema2BeansRuntimeException s2bre){ // see #70286    
            ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
            ejbJarProxy.setError(new SAXParseException(null, null, s2bre));
        } catch (RuntimeException re){ // see #99047    
            if (re.getCause() instanceof Schema2BeansException){
                ejbJarProxy.setStatus(EjbJar.STATE_INVALID_UNPARSABLE);
                ejbJarProxy.setError(new SAXParseException(null, null, (Schema2BeansException) re.getCause()));
            } else {
                throw re;
            }
        }
    }

    public static WebApp createWebApp(InputStream is, String version) throws IOException, SAXException {
        try {
            if (WebApp.VERSION_2_3.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web.model_2_3.WebApp.createGraph(is);
            } else if (WebApp.VERSION_2_4.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.web .model_2_4.WebApp.createGraph(is);
            } else /*if (WebApp.VERSION_2_5.equals(version))*/ {
                return org.netbeans.modules.j2ee.dd.impl.web .model_2_5.WebApp.createGraph(is);
            }
        } catch (RuntimeException ex) {
            throw new SAXException(ex);
        }
    }
    
    public static AppClient createAppClient(InputStream is, String version) throws IOException, SAXException {
        try {
            if (AppClient.VERSION_1_3.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_1_3.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_1_4.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_1_4.ApplicationClient.createGraph(is);
            } else if (AppClient.VERSION_5_0.equals(version)) {
                return org.netbeans.modules.j2ee.dd.impl.client.model_5_0.ApplicationClient.createGraph(is);
            }
        } catch (RuntimeException ex) {
            throw new SAXException(ex);
        }
        return null;
    }
}
