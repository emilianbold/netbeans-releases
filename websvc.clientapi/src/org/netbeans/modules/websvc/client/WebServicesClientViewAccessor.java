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
package org.netbeans.modules.websvc.client;

import org.netbeans.modules.websvc.api.client.WebServicesClientView;
import org.netbeans.modules.websvc.spi.client.WebServicesClientViewImpl;

/* This class provides access to the {@link WebServicesClientView}'s private constructor 
 * from outside in the way that this class is implemented by an inner class of 
 * {@link WebServicesClientView} and the instance is set into the {@link DEFAULT}.
 */
public abstract class WebServicesClientViewAccessor {

    public static WebServicesClientViewAccessor DEFAULT;
    
    // force loading of WebServicesClientView class. That will set DEFAULT variable.
    static {
        Object o = WebServicesClientView.class;
    }
    
    public abstract WebServicesClientView createWebServicesClientView(WebServicesClientViewImpl spiWebServicesClientView);

    public abstract WebServicesClientViewImpl getWebServicesClientViewImpl(WebServicesClientView wscv);

}
