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

package org.netbeans.modules.websvc.spi.webservices;

import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.webservices.WebServicesSupportAccessor;

/**
 * Most general way to create {@link WebServicesSupport} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link WebServicesSupportImpl} and use this factory.
 *
 * @author Peter Williams
 */
public final class WebServicesSupportFactory {

    private WebServicesSupportFactory() {
    }

    public static WebServicesSupport createWebServicesSupport(WebServicesSupportImpl spiWebServicesSupport) {
        return WebServicesSupportAccessor.DEFAULT.createWebServicesSupport(spiWebServicesSupport);
    }
	
}
