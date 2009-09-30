/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.sun.manager.jbi.management;

import org.netbeans.modules.sun.manager.jbi.util.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;

/**
 * ClassLoader for AppServer interface, see org.netbeans.modules.j2ee.sun.api.ExtendedClassLoader
 */
public class JBIClassLoader extends URLClassLoader {
    
    public static final String INSTALL_ROOT_PROP_NAME = "com.sun.aas.installRoot"; // NOI18N
    
    /**
     * Creates new JbiClassLoader
     */
    public JBIClassLoader() throws MalformedURLException, RuntimeException {
        super(new URL[0]);
    }
    
    public JBIClassLoader(ClassLoader loader) throws MalformedURLException, RuntimeException {
        super(new URL[0], loader);
    }
    
    public JBIClassLoader(ServerInstance instance) throws MalformedURLException, RuntimeException {
        this();
        init(instance);
    }
    
    public JBIClassLoader(ServerInstance instance, ClassLoader loader) throws MalformedURLException, RuntimeException {
        this(loader);
        init(instance);
    }
    
    public void addURL(File f) throws MalformedURLException, RuntimeException {
        if (f.isFile()) {
            addURL(f.toURI().toURL());
        }
    }
    
    protected PermissionCollection getPermissions(CodeSource _cs) {
        Permissions p = new Permissions();
        p.add(new AllPermission());
        return p;
    }
    
    private void init(ServerInstance instance) throws MalformedURLException {
        
        // Retrieve the jar files required to make the HTTPServerConnection.
        
        // Get the server location. This is not the domain or instance location. #90749
        String serverLocation = instance.getUrlLocation(); 
        
        String appserv_rt_jar = serverLocation + "/lib/appserv-rt.jar"; // NOI18N
        
        File appserv_rt_jarFile = new File(appserv_rt_jar);
        if (appserv_rt_jarFile.exists()) {
            addURL(appserv_rt_jarFile);
        } else {
            throw new RuntimeException("JbiClassLoader: Cannot find " + appserv_rt_jar + ".");
        }
        
        String jbi_rt_jar_90 = serverLocation + "/addons/jbi/lib/jbi_rt.jar"; // NOI18N // Glassfish 9.0
        String jbi_rt_jar_91 = serverLocation + "/jbi/lib/jbi_rt.jar"; // NOI18N // Glassfish 9.1
        
        File jbi_rt_jarFile = new File(jbi_rt_jar_90);
        if (jbi_rt_jarFile.exists()) {
            addURL(jbi_rt_jarFile);
        } else {
            jbi_rt_jarFile = new File(jbi_rt_jar_91);
            if (jbi_rt_jarFile.exists()) {
                addURL(jbi_rt_jarFile);
            } else {
                throw new RuntimeException("JbiClassLoader: Cannot locate " +
                        jbi_rt_jar_90 + " or " + jbi_rt_jar_91 + ".");
            }
        }
    }
}
