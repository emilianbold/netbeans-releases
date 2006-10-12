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

import javax.enterprise.deploy.shared.ModuleType;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * It describes children nodes of the Applications node
 *
 * @author Michal Mocnak
 */
public class JBApplicationsChildren extends Children.Keys {
    
    JBApplicationsChildren(Lookup lookup) {
        setKeys(new Object[] {createEarApplicationsNode(lookup),
                                createEjbModulesNode(lookup),
                                createWebApplicationsNode(lookup)});
    }
    
    protected void addNotify() {
    }
    
    protected void removeNotify() {
    }
    
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof AbstractNode){
            return new Node[]{(AbstractNode)key};
        }
        
        return null;
    }
    
    /*
     * Creates an EAR Applications parent node
     */
    public static JBItemNode createEarApplicationsNode(Lookup lookup) {
        return new  JBItemNode(new JBEarApplicationsChildren(lookup), NbBundle.getMessage(JBTargetNode.class, "LBL_EarApps"), ModuleType.EAR);
    }
    
    /*
     * Creates an Web Applications parent node
     */
    public static JBItemNode createWebApplicationsNode(Lookup lookup) {
        return new JBItemNode(new JBWebApplicationsChildren(lookup), NbBundle.getMessage(JBTargetNode.class, "LBL_WebApps"), ModuleType.WAR);
    }
    
    /*
     * Creates an EJB Modules parent node
     */
    public static JBItemNode createEjbModulesNode(Lookup lookup) {
        return new JBItemNode(new JBEjbModulesChildren(lookup), NbBundle.getMessage(JBTargetNode.class, "LBL_EjbModules"), ModuleType.EJB);
    }
}