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
package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.impl.ejb.EjbJarProxy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * @author pfiala
 */
public class DDUtils {
    public static EjbJarProxy createEjbJarProxy(InputStream inputStream) throws IOException {
        try {
            InputSource inputSource = new InputSource(inputStream);
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

    public static void merge(EjbJarProxy ejbJarProxy, InputStream is) {
        try {
            EjbJarProxy newEjbJarProxy = createEjbJarProxy(is);
            BigDecimal newVersion = newEjbJarProxy.getVersion();
            if (newVersion.equals(ejbJarProxy.getVersion())) {// the same version
                // merging original in proxy EjbJar
                ejbJarProxy.getOriginal().merge(newEjbJarProxy.getOriginal(), EjbJar.MERGE_UPDATE);
            } else {
                // replacing original in proxy EjbJar
                ejbJarProxy.setOriginal(newEjbJarProxy.getOriginal());
            }
            ejbJarProxy.setProxyVersion(newVersion.toString());
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
            ejbJarProxy.setProxyVersion(null);
        }
    }
}
