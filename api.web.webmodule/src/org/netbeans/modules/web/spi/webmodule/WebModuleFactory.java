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

package org.netbeans.modules.web.spi.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.webmodule.WebModuleAccessor;

/**
 * Most general way to create {@link WebModule} instances.
 * You are not permitted to create them directly; instead you implement
 * {@link WebModuleImplementation} and use this factory.
 *
 * @author  Pavel Buzek
 */
public final class WebModuleFactory {

    private WebModuleFactory () {
    }

    /**
     * Create API webmodule instance for the given SPI webmodule.
     * @param spiWebmodule instance of SPI webmodule
     * @return instance of API webmodule
     */
    public static WebModule createWebModule(WebModuleImplementation spiWebmodule) {
        return WebModuleAccessor.DEFAULT.createWebModule (spiWebmodule);
    }

}
