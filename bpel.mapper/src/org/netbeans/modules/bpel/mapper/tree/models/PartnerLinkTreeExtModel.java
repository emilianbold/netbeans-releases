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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.mapper.tree.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeExtensionModel;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTreeModel;
import org.netbeans.modules.bpel.mapper.tree.spi.RestartableIterator;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemInfoProvider;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.openide.util.NbBundle;

/**
 * The implementation of the MapperTreeModel for a tree of partner links.
 *
 * @author nk160297
 */
public class PartnerLinkTreeExtModel 
        implements MapperTreeExtensionModel<Object>, TreeItemInfoProvider {

//    public static final String MY_ROLE = 
//            NbBundle.getMessage(PartnerLinkTreeExtModel.class, "MY_ROLE"); // NOI18N
//    public static final String PARTNER_ROLE = 
//            NbBundle.getMessage(PartnerLinkTreeExtModel.class, "PARTNER_ROLE"); // NOI18N

    private BpelEntity mContextEntity;
    private boolean mShowEndpointRef;
    
    public PartnerLinkTreeExtModel(BpelEntity contextEntity, boolean showEndpointRef) {
        mContextEntity = contextEntity;
        mShowEndpointRef = showEndpointRef;
    }
    
    public List getChildren(RestartableIterator<Object> dataObjectPathItr) {
        Object parent = dataObjectPathItr.next();
        if (parent == MapperTreeModel.TREE_ROOT) {
            Process process = mContextEntity.getBpelModel().getProcess();
            PartnerLinkContainer plContainer = process.getPartnerLinkContainer();
            if (plContainer != null) {
                return Collections.singletonList(plContainer);
            } else {
                return null;
            }
        } else if (parent instanceof PartnerLinkContainer) {
            PartnerLinkContainer plContainer = (PartnerLinkContainer)parent;
            if (plContainer != null) {
                return filterPartnerLink(
                        !mShowEndpointRef ? Roles.PARTNER_ROLE : null, plContainer);
            }
        } else if (mShowEndpointRef && parent instanceof PartnerLink) {
            PartnerLink pl = (PartnerLink)parent;
            List<Roles> rolesList = new ArrayList<Roles>();
            if (pl.getMyRole() != null) {
                rolesList.add(Roles.MY_ROLE);
            }
            if (pl.getPartnerRole() != null) {
                rolesList.add(Roles.PARTNER_ROLE);
            }
            return rolesList;
        }
        //
        return null;
    }

    private List<PartnerLink> filterPartnerLink(Roles filter, PartnerLinkContainer plc) {
        if (plc == null) {
            return Collections.emptyList();
        }
        PartnerLink[] pls = plc.getPartnerLinks();
        if (filter == null) {
            return Arrays.asList(pls);
        }
        List<PartnerLink> plsFiltred = new  ArrayList<PartnerLink>();
        int l = pls.length;
        if (Roles.MY_ROLE.equals(filter)) {
            for (int i = 0; i < l; i++) {
                PartnerLink pl = pls[i];
                if (pl != null && pl.getMyRole() != null) {
                    plsFiltred.add(pl);
                }
            }
        } else if (Roles.PARTNER_ROLE.equals(filter)) {
            for (int i = 0; i < l; i++) {
                PartnerLink pl = pls[i];
                if (pl != null && pl.getPartnerRole() != null) {
                    plsFiltred.add(pl);
                }
            }
        }
        
        return plsFiltred;
    }
    
    public Boolean isLeaf(Object node) {
        return null;
    }

    public Boolean isConnectable(Object node) {
        if (mShowEndpointRef) {
            return node instanceof Roles;
        } else {
            return node instanceof PartnerLink;
        }
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return this;
    }

    public String getDisplayName(Object treeItem) {
        if (treeItem instanceof PartnerLinkContainer) {
            return NbBundle.getMessage(VariableTreeInfoProvider.class,
                    "PARTNER_LINK_CONTAINER"); // NOI18N
        }
        return null;
    }

    public Icon getIcon(Object treeItem) {
        if (treeItem instanceof PartnerLinkContainer) {
            return NodeType.VARIABLE_CONTAINER.getIcon();
        } 
        return null;
    }

    public List<Action> getMenuActions(MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            RestartableIterator<Object> dataObjectPathItr) {
        return null;
    }

    public String getToolTipText(RestartableIterator<Object> dataObjectPathItr) {
        Object treeItem = dataObjectPathItr.next();
        if (treeItem instanceof Roles) {
            return ((Roles)treeItem).toString();
        }
        return null;
    }

}
