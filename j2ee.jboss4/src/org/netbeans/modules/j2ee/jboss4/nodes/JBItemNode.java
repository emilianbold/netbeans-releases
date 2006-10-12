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
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.j2ee.jboss4.nodes.actions.Refreshable;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 * Default Node which can have refresh action enabled and which has deafault icon.
 *
 * @author Michal Mocnak
 */
public class JBItemNode extends AbstractNode {
    
    private ModuleType moduleType;
    
    public JBItemNode(Children children, String name){
        super(children);
        setDisplayName(name);
        if(getChildren() instanceof Refreshable)
            getCookieSet().add(new RefreshModulesCookieImpl((Refreshable)getChildren()));
    }
    
    public JBItemNode(Children children, String name, ModuleType moduleType) {
        this(children, name);
        this.moduleType = moduleType;
    }
    
    public Image getIcon(int type) {
        if (ModuleType.WAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.WAR_FOLDER);
        } else if (ModuleType.EAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EAR_FOLDER);
        } else if (ModuleType.EJB.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EJB_FOLDER);
        } else {
            return getIconDelegate().getIcon(type);
        }
    }
    
    public Image getOpenedIcon(int type) {
        if (ModuleType.WAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.WAR_OPENED_FOLDER);
        } else if (ModuleType.EAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EAR_OPENED_FOLDER);
        } else if (ModuleType.EJB.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EJB_OPENED_FOLDER);
        } else {
            return getIconDelegate().getOpenedIcon(type);
        }
    }
    
    private Node getIconDelegate() {
        return DataFolder.findFolder(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
    }
    
    public javax.swing.Action[] getActions(boolean context) {
        if(getChildren() instanceof Refreshable)
            return new SystemAction[] {
                SystemAction.get(RefreshModulesAction.class)
            };
        
        return new SystemAction[] {};
    }
    
    /**
     * Implementation of the RefreshModulesCookie
     */
    private static class RefreshModulesCookieImpl implements RefreshModulesCookie {
        Refreshable children;
        
        public RefreshModulesCookieImpl(Refreshable children){
            this.children = children;
        }
        
        public void refresh() {
            children.updateKeys();
        }
    }
}