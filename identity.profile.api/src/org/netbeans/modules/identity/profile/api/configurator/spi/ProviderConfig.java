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

package org.netbeans.modules.identity.profile.api.configurator.spi;

import java.util.Collection;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;

/**
 * This interface is used to provide an abstraction over the ProviderConfig
 * class from the AM client sdk. It allows us to provide both a dynamic 
 * (using the client sdk API) and a file-based (using xml) implementation. 
 *
 * Created on June 9, 2006, 7:59 PM
 *
 * @author ptliu
 */
public interface ProviderConfig {
 
    public boolean isResponseSignEnabled();
    
    public void setResponseSignEnabled(boolean flag);
    
    public String getKeyAlias();
    
    public void setKeyAlias(String keyAlias);
    
    public String getKeyStoreFile();
    
    public String getKeyStorePassword();
    
    public String getKeyPassword();
    
    public void setKeyStore(String location, String password, String keyPassword);
    
    public Object getProperty(String propName);
    
    public void setProperty(String propName, Object value);
    
    /**
     * Return a collection of security mechanism URIs.
     *
     */
    public Collection<String> getSecurityMechanisms();
    
    /**
     * Set a collection of security mechanism URIs.
     *
     */
    public void setSecurityMechanisms(Collection<String> securityMechs);
   
    public void saveProvider();
    
    public void deleteProvider();
    
    public String getWSPEndpoint();
    
    public void setWSPEndpoint(String endpoint);
    
    public boolean isProviderExists();
    
    public ServerProperties getServerProperties(String id);
    
    public void setServerProperties(ServerProperties properties);
    
    public void setUserName(String userName);
    
    public String getUserName();
    
    public void setPassword(String password);
    
    public String getPassword();
    
    public Collection<Vector<String>> getUserNamePasswordPairs();
    
    public void setUserNamePasswordPairs(Collection<Vector<String>> pairs);
    
    /**
     *  Returns a collection of all the provider names. Note that the 
     *  provider type should be the type bound to the current ProviderConfig
     *  instance.
     *
     */
    public Collection<String> getAllProviderNames();
    
    public void setServiceType(String serviceType);
    
    public String getServiceType();
    
    public void setDefaultKeyStore(boolean flag);
    
    public boolean useDefaultKeyStore();

    public void setTrustAuthorityConfigList(List<TrustAuthorityConfig> trustAuthConfigs);
    
    public void close();
}

