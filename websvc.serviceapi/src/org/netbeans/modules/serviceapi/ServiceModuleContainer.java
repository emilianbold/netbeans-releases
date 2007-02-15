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
package org.netbeans.modules.serviceapi;

import java.util.Collection;

/**
 * Represents a container of multiple service modules.
 *
 * @author Nam Nguyen
 * @author Chris Webster
 * @author Jiri Kopsa
 */
public abstract class ServiceModuleContainer extends ServiceModule {

    /**
     * Property name of change event on child service modules.
     */
    public static final String SERVICE_MODULE_ADDED_PROPERTY = "serviceModuleAdded";
    public static final String SERVICE_MODULE_REMOVED_PROPERTY = "serviceModuleRemoved";

    /**
     * @return all child service modules; or empty collection if is not a 
     * composite service module.
     */
    public abstract Collection<ServiceModule> getServiceModules();

    /**
     * Add child service module.
     */
    public abstract void addServiceModule(ServiceModule serviceModule);
    
    /**
     * Remove child service module.
     */
    public abstract void removeServiceModule(ServiceModule serviceModule);
}
