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

package org.netbeans.modules.glassfish.common.nodes;

import java.awt.Component;
import java.util.Collections;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.actions.RemoveServerAction;
import org.netbeans.modules.glassfish.common.actions.StartServerAction;
import org.netbeans.modules.glassfish.common.actions.StopServerAction;
import org.netbeans.modules.glassfish.common.actions.ViewAdminConsoleAction;
import org.netbeans.modules.glassfish.common.nodes.actions.RefreshModulesAction;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class Hk2InstanceNode extends AbstractNode { // implements Node.Cookie {

    private static final String ICON_BASE = 
            "org/netbeans/modules/glassfish/common/resources/server.gif"; // NOI18N

    private volatile String displayName = null;
    private volatile String shortDesc = null;
    
    public Hk2InstanceNode(Lookup lookup, boolean showChildren) {
        super(showChildren ? new Children.Array() : Children.LEAF, lookup);
        setIconBaseWithExtension(ICON_BASE);
        
        if(showChildren) {
            getChildren().add(new Node[] {
                new Hk2ItemNode(lookup, 
                        new Hk2ApplicationsChildren(lookup),
                        NbBundle.getMessage(Hk2InstanceNode.class, "LBL_Apps"),
                        Hk2ItemNode.ItemType.J2EE_APPLICATION_FOLDER)
            });
        }
    }

    @Override
    public String getDisplayName() {
        if(displayName == null) {
            displayName = buildDisplayName();
        }
        return displayName;
    }

    @Override
    public String getShortDescription() {
        if(shortDesc == null) {
            shortDesc = NbBundle.getMessage(Hk2InstanceNode.class, "LBL_ServerInstanceNodeDesc", 
                    GlassfishInstance.GLASSFISH_SERVER_NAME, getAdminUrl());
        }
        return shortDesc;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(StartServerAction.class),
            SystemAction.get(StopServerAction.class),
            SystemAction.get(RefreshModulesAction.class),
            null,
            SystemAction.get(RemoveServerAction.class),
            null,
            SystemAction.get(ViewAdminConsoleAction.class)
        };
        
        // Instance node candidate actions (borrowed list from j2eeserver module
//        SystemAction.get(StartAction.class),
//        SystemAction.get(DebugAction.class)
//        SystemAction.get(ProfileAction.class)
//        SystemAction.get(RestartAction.class),
//        SystemAction.get(StopAction.class),
//        SystemAction.get(RefreshAction.class),
//        null,
//        SystemAction.get(RemoveInstanceAction.class)
        // Target node candidat actions (again from j2eeserver module)
        // ... TBD ...
        // Properties action
//        SystemAction.get(CustomizerAction.class)
        
        
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public Component getCustomizer() {
    //        CustomizerDataSupport dataSup = new CustomizerDataSupport(getDeploymentManager());
    //        return new Customizer(dataSup, new Hk2J2eePlatformFactory().getJ2eePlatformImpl(getDeploymentManager()));
        return new javax.swing.JPanel();
    }

//    public Hk2DeploymentManager getDeploymentManager() {
//        return ((Hk2DeploymentManager) lookup.lookup(Hk2DeploymentManager.class));
//    }

    private Map<String, String> getInstanceProperties() {
        Map<String, String> ip = null;
        GlassfishModule commonSupport = getLookup().lookup(GlassfishModule.class);
        if(commonSupport != null) {
            ip = commonSupport.getInstanceProperties();
        }

        if(ip == null) {
            ip = Collections.emptyMap();
        }
        
        return ip;
    }
    
    private String buildDisplayName() {
        Map<String, String> ip = getInstanceProperties();
        String dn = ip.get(GlassfishModule.DISPLAY_NAME_ATTR);
        return dn != null ? dn : NbBundle.getMessage(Hk2InstanceNode.class, "TXT_GlassfishInstanceNode");
    }

    public String getAdminUrl() {
        String result = null;
        
        Map<String, String> ip = getInstanceProperties();
        String host = ip.get(GlassfishModule.HOSTNAME_ATTR);
        String httpPort = ip.get(GlassfishModule.HTTPPORT_ATTR);
        if(host != null && host.length() > 0) {
            result = "http://" + host + ":" + httpPort;
        }
        
        return result;
    }

}
