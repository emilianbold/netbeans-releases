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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.WeakHashMap;

/**
 *
 * @author mkuchtiak
 */
public class WsdlModelerFactory {
    
    private static WsdlModelerFactory factory;
    WeakHashMap<URL, WeakReference<WsdlModeler>> modelers;
    
    /** Creates a new instance of WsdlModelerFactory */
    private WsdlModelerFactory() {
        modelers = new WeakHashMap<URL, WeakReference<WsdlModeler>>(5);
    }
    
    /**
    * Accessor method for WsdlModelerFactory singleton
    * @return WsdlModelerFactory object
    */
    public static synchronized WsdlModelerFactory getDefault() {
        if (factory==null) factory = new WsdlModelerFactory();
        return factory;
    }
    
    /** Get WsdlModeler for particular WSDL
     */
    public WsdlModeler getWsdlModeler(URL wsdlUrl) {
        WsdlModeler modeler = null;
        synchronized (modelers) {
            modeler = getFromCache(wsdlUrl);
            if (modeler!=null) {
                return modeler;
            }
            modeler = new WsdlModeler(wsdlUrl);
            modelers.put(wsdlUrl, new WeakReference<WsdlModeler>(modeler));
        }
        return modeler;
    }
    
    private WsdlModeler getFromCache (URL url) {
        if (url == null) {
            return null;
        }
        WeakReference wr = modelers.get(url);
        if (wr == null) {
            return null;
        }
        WsdlModeler modeler = (WsdlModeler) wr.get();
        if (modeler == null) {
            modelers.remove(url);
        }
        return modeler;
    }
    
    int mapLength() {
        return modelers.size();
    }
    
    
}
