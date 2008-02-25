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

package org.netbeans.modules.glassfish.javaee.nodes;

import java.util.logging.Logger;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.j2ee.deployment.plugins.spi.RegistryNodeFactory;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Ludo
 */
public class Hk2RegistryNodeFactory implements RegistryNodeFactory {
    
    public Node getTargetNode(Lookup lookup) {
        return new Hk2TargetNode(lookup);
    }

    public Node getManagerNode(Lookup lookup) {
        Node managerNode = null;
        
        /** Find and wrap the node being provided by the common server api
         */
        DeploymentManager manager = lookup.lookup(DeploymentManager.class);
        if(manager instanceof Hk2DeploymentManager) {
            Hk2DeploymentManager hk2mgr = (Hk2DeploymentManager) manager;
            ServerInstance instance = hk2mgr.getServerInstance();
            if(instance != null) {
                Node instanceNode = instance.getBasicNode();
                managerNode = new FilterNode(instanceNode, new ManagerChildren(instanceNode),
                        new ProxyLookup(lookup, instanceNode.getLookup()));
            } else {
                Logger.getLogger("glassfish-javaee").info(
                        "Unable to locate ServerInstance for " + hk2mgr.getUri());

            }
        }
        return managerNode;
    }
    
    private static final class ManagerChildren extends FilterNode.Children {
        
        public ManagerChildren(final Node originalNode) {
            super(originalNode);
        }       
                
    }
    
    private static final class TargetChildren extends FilterNode.Children {
        
        public TargetChildren(final Node originalNode) {
            super(originalNode);
        }       
                
    }
    
}
