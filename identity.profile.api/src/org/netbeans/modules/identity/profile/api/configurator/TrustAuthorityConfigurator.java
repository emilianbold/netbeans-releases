/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
