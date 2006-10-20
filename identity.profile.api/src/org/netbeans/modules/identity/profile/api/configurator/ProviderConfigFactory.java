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

package org.netbeans.modules.identity.profile.api.configurator;

import org.netbeans.modules.identity.profile.api.configurator.Configurator.AccessMethod;
import org.netbeans.modules.identity.profile.api.configurator.ProviderConfigurator.Type;
import org.netbeans.modules.identity.profile.api.configurator.spi.ProviderConfig;

/**
 * Factory for creating instances of ProviderConfig based on the access method.
 *
 * Created on June 21, 2006, 7:29 PM
 *
 * @author ptliu
 */
class ProviderConfigFactory {
    
    public static ProviderConfig newInstance(AccessMethod accessMethod,
            String providerName, Type type, Object accessToken) {
        switch (accessMethod) {
            case DYNAMIC:
                return new org.netbeans.modules.identity.profile.api.configurator.impl.dynamic.ProviderConfigImpl(
                        providerName, type.toString(), (ServerProperties) accessToken);
            case FILE:
                return new org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl(
                        providerName, type.toString(), (String) accessToken);
            default:
                return null;
        }
    }
}
