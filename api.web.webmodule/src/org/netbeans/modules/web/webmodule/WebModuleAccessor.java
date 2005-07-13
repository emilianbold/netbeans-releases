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
package org.netbeans.modules.web.webmodule;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation;

/* This class provides access to the {@link WebModule}'s private constructor 
 * from outside in the way that this class is implemented by an inner class of 
 * {@link WebModule} and the instance is set into the {@link DEFAULT}.
 */
public abstract class WebModuleAccessor {

    public static WebModuleAccessor DEFAULT;
    
    // force loading of WebModule class. That will set DEFAULT variable.
    static {
        Class c = WebModule.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public abstract WebModule createWebModule(WebModuleImplementation spiWebmodule);

    public abstract WebModuleImplementation getWebModuleImplementation (WebModule wm);

}
