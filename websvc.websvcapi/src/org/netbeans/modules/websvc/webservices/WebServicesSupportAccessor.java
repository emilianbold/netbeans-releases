/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.webservices;

import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;

/* This class provides access to the {@link WebServicesSupport}'s private constructor 
 * from outside in the way that this class is implemented by an inner class of 
 * {@link WebServicesSupport} and the instance is set into the {@link DEFAULT}.
 */
public abstract class WebServicesSupportAccessor {

    public static WebServicesSupportAccessor DEFAULT;
    
    // force loading of WebServicesSupport class. That will set DEFAULT variable.
    static {
        Class c = WebServicesSupport.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public abstract WebServicesSupport createWebServicesSupport(WebServicesSupportImpl spiWebServicesSupport);

    public abstract WebServicesSupportImpl getWebServicesSupportImpl(WebServicesSupport wss);

}
