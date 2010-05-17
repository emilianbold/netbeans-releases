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
package org.netbeans.modules.j2ee.sun.bridge.apis;

import java.util.logging.Logger;
import java.util.logging.Level;


import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 * The parent class for all appserver plugin management nodes. All nodes for
 * the NetBeans runtime tab J2EE plugin must extend this class for effectively
 * communicating via AMX API.
 */
public abstract class AppserverMgmtNode extends AbstractNode {
    
    private String nodeType;
    private AppserverMgmtController appsrvrMgmtController;
    private static Logger logger;
    
    static {
        logger = Logger.getLogger("org.netbeans.modules.j2ee.sun");
    }
    
    
    /**
     *
     */
    public AppserverMgmtNode(final Children children, final String nodeType) {
        super(children);
        setNodeProperties(nodeType);
        // getAppserverMgmtController();
    }
    
    /**
     *
     *
     */
    public AppserverMgmtNode(final AppserverMgmtController controller,
            final Children children, final String nodeType) {
        super(children);
        setNodeProperties(nodeType);
        appsrvrMgmtController = controller;
    }
    
    
    /**
     *
     *
     */
    // Shouldn't this be put into the lookup?
    public AppserverMgmtController getAppserverMgmtController() {
        try {
            if(appsrvrMgmtController == null) {
                getLogger().log(Level.FINE, "AppserverMgmtController is " +
                        "null for [" + getNodeType() + "]");
            }
        } catch(Exception e) {
            getLogger().log(Level.FINE, e.getMessage(), e);
        }
        return appsrvrMgmtController;
    }
    
    /* reset the controller from the Deployment manager
     * used when the username of the password known for the connexion is changed 
     * via the IDE for example
     **/
    public void setAppserverMgmtController(AppserverMgmtController controller) {
        appsrvrMgmtController = controller;
    }    
    /**
     *
     *
     */
    public String getNodeType(){
        return nodeType;
    }
    
    
    /**
     *
     */
    private void setNodeProperties(final String nodeType) {
        this.nodeType = nodeType;
        setDisplayName(getNodeDisplayName());
        setIconBaseWithExtension(getNodeIconPath());
        setShortDescription(getNodeShortDescription());
    }
    
    /**
     *
     */
    protected String getNodeDisplayName() {
        String s=nodeType;
        try{
            s=NbBundle.getMessage(AppserverMgmtNode.class, nodeType);
        } catch (Exception e){
            return s;
        }
        return s;
        
    }
    
    /**
     *
     */
    protected String getNodeIconPath() {
        String   s="org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.png";
        try{
             s= NbBundle.getMessage(AppserverMgmtNode.class, nodeType + "_ICON");
        } catch (Exception e){
            return s;
            
        }
        return s;
    }
    
    /**
     *
     */
    protected String getNodeShortDescription() {
        String   s="missing _SHORT_DESC for " + nodeType;
        try{
             s= NbBundle.getMessage(AppserverMgmtNode.class, nodeType + "_SHORT_DESC");
        } catch (Exception e){
            
        }
        return s; 
    }
    
    
    /**
     * Returns the logger for all nodes.
     *
     * @returns The java.util.logging.Logger impl. for this node.
     */
    protected final static Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger("org.netbeans.modules.j2ee.sun");
        }
        return logger;
    }
    
    public boolean isServerLocal(){
        return appsrvrMgmtController.isDeployMgrLocal();
    }
}
