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
