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

package org.netbeans.modules.j2ee.jboss4.nodes;

import java.awt.Image;
import javax.enterprise.deploy.shared.ModuleType;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.j2ee.jboss4.JBDeploymentManager;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.OpenURLAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.OpenURLActionCookie;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.UndeployModuleAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.UndeployModuleCookieImpl;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 * Node which describes Web Module.
 *
 * @author Michal Mocnak
 */
public class JBWebModuleNode extends AbstractNode {

    final boolean isRemoteManagementSupported;
    final boolean isJB4x;

    public JBWebModuleNode(String fileName, Lookup lookup, String url) {
        super(new JBServletsChildren(fileName, lookup));
        setDisplayName(fileName.substring(0, fileName.indexOf('.')));
        isRemoteManagementSupported = Util.isRemoteManagementSupported(lookup);
        isJB4x = JBPluginUtils.isGoodJBServerLocation4x((JBDeploymentManager)lookup.lookup(JBDeploymentManager.class));
        if (isRemoteManagementSupported && isJB4x) {
            // we cannot find out the .war name w/o the management support, thus we cannot enable the Undeploy action
            getCookieSet().add(new UndeployModuleCookieImpl(fileName, ModuleType.WAR, lookup));
        }
        
        if(url != null)
            getCookieSet().add(new OpenURLActionCookieImpl(url));
    }
    
    public Action[] getActions(boolean context){
        if (getParentNode() instanceof JBEarApplicationNode) {
            return new SystemAction[] {
                SystemAction.get(OpenURLAction.class)
            };
        }
        else {
            if (isRemoteManagementSupported && isJB4x) {
                return new SystemAction[] {
                    SystemAction.get(OpenURLAction.class),
                    SystemAction.get(UndeployModuleAction.class)
                };
            }
            else {
                // we cannot find out the .war name w/o the management support, thus we cannot enable the Undeploy action
                return new SystemAction[] {
                    SystemAction.get(OpenURLAction.class),
                };
            }
        }
    }
    
    public Image getIcon(int type) {
        return UISupport.getIcon(ServerIcon.WAR_ARCHIVE);
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    private static class OpenURLActionCookieImpl implements OpenURLActionCookie {
        
        private String url;
        
        public OpenURLActionCookieImpl(String url) {
            this.url = url;
        }
        
        public String getWebURL() {
            return url;
        }
    }
}
