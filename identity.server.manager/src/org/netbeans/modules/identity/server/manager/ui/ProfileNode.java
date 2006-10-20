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

package org.netbeans.modules.identity.server.manager.ui;

import javax.swing.Action;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;
import org.netbeans.modules.identity.server.manager.api.ServerInstance;
import org.netbeans.modules.identity.server.manager.ui.actions.EditProfileAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

/**
 * This class represents a Profile node in the the Runtime tab.
 *
 * Created on July 11, 2006, 12:22 AM
 *
 * @author ptliu
 */
public class ProfileNode extends AbstractNode {
    
    private static final String PROFILE_NODE_ICON = "org/netbeans/modules/identity/server/manager/ui/resources/ProfileNode.png";//NOI18N
    
    private static final String HELP_ID = "idmtools_am_config_am_sec_mech";     //NOI18N
    
    private SecurityMechanism secMech;
    private ServerInstance instance;
    
    /** Creates a new instance of ProfileNode */
    public ProfileNode(SecurityMechanism secMech, ServerInstance instance) {
        super(Children.LEAF);
        
        setName("");     //NOI18N
        setDisplayName(secMech.getName());
        setIconBaseWithExtension(PROFILE_NODE_ICON);
        
        this.secMech = secMech;
        this.instance = instance;
    }
    
     public Action[] getActions(boolean context) {
        Action[] actions = new Action[] {
            SystemAction.get(EditProfileAction.class),
        };
        
        return actions;
    }
     
    public SecurityMechanism getSecurityMechanism() {
        return secMech;
    }
    
    public ServerInstance getServerInstance() {
        return instance;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
}
