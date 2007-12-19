/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.rt.providers.impl;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.php.rt.WebServerRegistry;
import org.netbeans.modules.php.rt.spi.providers.Host;
import org.netbeans.modules.php.rt.spi.providers.UiConfigProvider;
import org.netbeans.modules.php.rt.utils.ServersUtils;

/**
 *
 * @author avk
 */
public abstract class AbstractUiConfigProvider implements UiConfigProvider {

    public abstract HostImpl copyHost(String newName, HostImpl fromHost);


    public Set instantiate( HostImpl impl ) {

        Host existing = ServersUtils.findHostById(impl.getId());
        // do not update through provider.update() because we do now know 
        // the type of 'existing'
        if (existing != null && existing instanceof HostImpl){
            getProvider((HostImpl)existing).remove(existing);
            WebServerRegistry.getInstance().removeHost(existing);
        }
        getProvider(impl).addHost( impl );
        WebServerRegistry.getInstance().addHost( impl );

        return Collections.EMPTY_SET;
    }
    
    protected void copyHostContent( HostImpl fromHost, HostImpl toHost){
        if (toHost == null){
            return;
        }
        
        copyHostProperty(fromHost, toHost, HostImpl.DOMAIN);
        copyHostProperty(fromHost, toHost, HostImpl.PORT);
        copyHostProperty(fromHost, toHost, HostImpl.BASE_DIRECTORY_PATH);
    }
    
    private void copyHostProperty(HostImpl fromHost, HostImpl toHost, 
            String key)
    {
        Object value = null;
        if (fromHost != null){
            value = fromHost.getProperty(key);
        }
        if (value != null){
            toHost.setProperty(key, value);
        }
    }
    
    private AbstractProvider getProvider(HostImpl host){
        return (AbstractProvider)host.getProvider();
    }
}
