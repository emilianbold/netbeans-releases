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
 * NodeFactory.java
 *
 * Created on December 21, 2003, 8:19 AM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;

/**
 *
 * @author  ludo
 */
public class NodeFactory implements org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory {
    
    /** Creates a new instance of NodeFactory */
    public NodeFactory() {
    }
       
    public org.openide.nodes.Node getManagerNode(org.openide.util.Lookup lookup) {
        DeploymentManager depManager = (DeploymentManager) lookup.lookup(DeploymentManager.class);
        //SunDeploymentManagerInterface dm = (SunDeploymentManagerInterface)depManager;
        //System.out.println("User Name " + dm.getUserName() + " Host " + dm.getHost());
        
        if (depManager == null ) {
            System.out.println("WARNING: getManagerNode lookup returned "+depManager);//NOI18N
            return null;
        }
        return new ManagerNode(depManager);
    }
    
    public org.openide.nodes.Node getTargetNode(org.openide.util.Lookup lookup) {
        Target target = (Target) lookup.lookup(Target.class);
        DeploymentManager depManager = (DeploymentManager) lookup.lookup(DeploymentManager.class);
                        
        if (depManager == null ) {
            System.out.println("WARNING: getManagerNode lookup returned "+depManager);//NOI18N
        }
        if (target == null ) {
            System.out.println("WARNING: getTargetNode lookup returned "+target);//NOI18N
            return null;
        }
        
        
        try{
            return initializePluginTree(depManager);
        } catch (Exception e){
            System.out.println("Cannot create the instance node in the " +
                    "factory " + e);//NOI18N
        }

        
        return null;//too bad
    }
    
    
    /**
     *
     *
     */
    private org.openide.nodes.Node initializePluginTree( final DeploymentManager deployMgr) throws Exception {
        ClassLoader origClassLoader=Thread.currentThread().getContextClassLoader();
        try{
	    SunDeploymentManagerInterface sdm = (SunDeploymentManagerInterface)deployMgr;
	    ClassLoader loader = ServerLocationManager.getNetBeansAndServerClassLoader(sdm.getPlatformRoot());
            Class pluginRootFactoryClass =loader.loadClass("org.netbeans.modules.j2ee.sun.util.PluginRootNodeFactory");//NOI18N
            Constructor constructor =pluginRootFactoryClass.getConstructor(new Class[] {DeploymentManager.class});
            Object pluginRootFactory =constructor.newInstance(new Object[] {deployMgr});
            Class factoryClazz = pluginRootFactory.getClass();
            Method method =factoryClazz.getMethod("getPluginRootNode", (Class[])null);
            
            
            Thread.currentThread().setContextClassLoader( loader);
            
            
            return (org.openide.nodes.Node)method.invoke(pluginRootFactory, (Object[]) null);
        } catch (Exception e){
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }
    
    
    
}
