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


/*
 * InstancePropertiesImpl.java
 *
 * Created on December 4, 2003, 6:11 PM
 */

package org.netbeans.modules.j2ee.deployment.impl;

import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.InstanceListener;
import org.openide.util.Exceptions;

/**
 *
 * @author  nn136682
 */
public class InstancePropertiesImpl extends InstanceProperties implements InstanceListener {
    private final String url;
    private transient FileObject fo;
    
    /** Creates a new instance of InstancePropertiesImpl */
    public InstancePropertiesImpl(ServerInstance instance) {
        this(instance.getUrl());
    }

    /** Creates a new instance of InstancePropertiesImpl */
    public InstancePropertiesImpl(String url) {
        this.url = url;
    }
    
    private FileObject getFO() {
        if (fo == null) {
            ServerInstance instance = ServerRegistry.getInstance().getServerInstance(url);
            if (instance == null) 
                throw new IllegalStateException(
                (NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url))); //NOI18N
            fo = ServerRegistry.getInstance().getInstanceFileObject(url);
            if (fo == null)
                throw new IllegalStateException(
                (NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url))); //NOI18N
            
        }
        return fo;
    }
    
    // InstanceListener methods
    public void instanceRemoved(String instance) {
        if (instance != null && url.equals(instance))
            fo = null;
    }
    public void instanceAdded(String instance) {}
    public void changeDefaultInstance(String oldInstance, String newInstance){
    }
    
    public String getProperty(String propname) throws IllegalStateException {
        Object propValue = getFO().getAttribute(propname);
        return propValue == null ? null : propValue.toString();
    }

    public java.util.Enumeration propertyNames() throws IllegalStateException {
        return getFO().getAttributes();
    }
    
    public void setProperty(String propname, String value) throws IllegalStateException {
        try {
            String oldValue = getProperty(propname);
            getFO().setAttribute(propname, value);
            firePropertyChange(new PropertyChangeEvent(this, propname, oldValue, value));
        } catch (IOException ioe) {
            String message = NbBundle.getMessage(InstancePropertiesImpl.class, "MSG_InstanceNotExists", url);
            throw new IllegalStateException(Exceptions.attachLocalizedMessage(ioe, message));
        }
    }
    
    public void setProperties(java.util.Properties props) throws IllegalStateException {
        java.util.Enumeration propNames = props.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = (String) propNames.nextElement();
            String propValue = props.getProperty(propName);
            setProperty(propName, propValue);
        }
    }

    public javax.enterprise.deploy.spi.DeploymentManager getDeploymentManager() {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerInstance inst = registry.getServerInstance(url);
        return inst.getDeploymentManager();
    }

    public void refreshServerInstance() {
        ServerRegistry registry = ServerRegistry.getInstance();
        ServerInstance inst = registry.getServerInstance(url);
        if (inst != null) {
            inst.refresh();
        }
    }
}
