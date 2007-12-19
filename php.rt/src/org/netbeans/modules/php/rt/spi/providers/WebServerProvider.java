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

package org.netbeans.modules.php.rt.spi.providers;

import java.util.List;

import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;


/**
 * This is entry point for certnain type Web server functionality.<br/>
 * This provider has responibility to manipulate Hosts, Nodes in runtime
 * explorer ( that correspond to Hosts ) , 
 * providing UI : for host configuration and project configuration 
 * ( it provide specific settings when host is chosen in project wizard ), 
 * performing commands ( commands
 * are delegated from project to this provider ).  
 * 
 * @author ads
 *
 */
public interface WebServerProvider {
    
    public static final String HOST_ID      = "host.id";            // NOI18N

    /**
     * @return list of all configured hosts supplied by this provider
     */
    List<Host> getHosts();
    
    /**
     * @return provider that responsible for configuration of new host in runtime
     * explorer. It is used in wizard iterator.
     */
    UiConfigProvider getConfigProvider();
    
    /**
     * @return provider that responsible for command execution. Commands 
     * are delegated from project.
     */
    CommandProvider getCommandProvider();
    
    /**
     * @return provider that responsible for additional properties for 
     * project configuration.  
     */
    ProjectConfigProvider getProjectConfigProvider();
    
    /**
     * Creates new Node for <code>host</code>.
     * @param host configured host.
     * @return new Node for host
     */
    Node createNode( Host host );
    
    /**
     * @return short name of web server type. It is used on first panel in wizrd
     * in list of all available web server types.
     */
    String getTypeName();
    
    /**
     * @return full description for this type of web server.
     */
    String getDescription();
    
    Host findHost( String id );
    
    public static class ServerFactory {
        
        public static WebServerProvider[] getProviders() {
            return myProviders;
        }
        
        private static void initProviders() {
            Result<WebServerProvider> result = Lookup.getDefault().lookup(
                    new Lookup.Template<WebServerProvider>(
                            WebServerProvider.class));
            myProviders = result.allInstances().toArray( 
                    new WebServerProvider[ result.allInstances().size()]);
        }
        
        static {
            initProviders();
        }
        
        private static WebServerProvider[] myProviders;
    }

}
