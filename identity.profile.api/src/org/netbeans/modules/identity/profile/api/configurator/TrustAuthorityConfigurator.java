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

import org.netbeans.modules.identity.profile.api.configurator.spi.TrustAuthorityConfig;

/**
 * Configurator for configuring TrustAuthorityConfig.
 *
 * Created on August 2, 2006, 3:19 AM
 *
 * @author ptliu
 */
public class TrustAuthorityConfigurator extends Configurator {
    
    public enum Type {
        DISCOVERY
    }
    
    public enum Configurable {
        ENDPOINT
    }
    
    private String name;
    private Type type;
    private TrustAuthorityConfig trustAuthConfig;
    
    /** Creates a new instance of TrustAuthorityConfigurator */
    protected TrustAuthorityConfigurator() {
        
    }

    public static TrustAuthorityConfigurator getConfigurator(String name,
            Type type, AccessMethod accessMethod, 
            Object accessToken) {
        TrustAuthorityConfigurator configurator = null;
        
        switch (type) {
            case DISCOVERY:
                configurator = new DiscoveryConfigurator();
                break;
                
        }
        
        if (configurator != null) {
            configurator.init(name, type, accessMethod, accessToken);
        }
        
        return configurator;
    }
    
  
    protected void init(String name, Type type, AccessMethod accessMethod, 
            Object accessToken) {
        this.type = type;
        this.name = name;
        
        trustAuthConfig = TrustAuthorityConfigFactory.newInstance(name,
                type, accessMethod, accessToken);
    }
    
    public Type getType() {
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    protected TrustAuthorityConfig getTrustAuthorityConfig() {
        return trustAuthConfig;
    }
    
    public Object getValue(Enum configurable) {
        if (configurable instanceof Configurable) {
            switch ((Configurable) configurable) {
                case ENDPOINT:
                    return null;
            }
        }
        
        return null;
    }

    public void setValue(Enum configurable, Object value) {
        if (configurable instanceof Configurable) {
            switch ((Configurable) configurable) {
                case ENDPOINT:
            }
        }
    }

    public String validate() {
        return null;
    }

    public void save() {
        trustAuthConfig.saveConfig();
    }

    public void enable() {
    }

    public void disable() {
    }
    
}
