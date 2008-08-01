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
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.openide.util.NbBundle;

/**
 * The implementation of the MapperTreeModel for a tree of partner links.
 *
 * @author nk160297
 */
public class PartnerLinkTreeExtModel implements SoaTreeExtensionModel, 
        TreeItemInfoProvider, TreeStructureProvider, 
        TreeItemActionsProvider, MapperConnectabilityProvider {

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
    
    public List getChildren(TreeItem treeItem) {
        Object parent = treeItem.getDataObject();
        if (parent == SoaTreeModel.TREE_ROOT) {
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
    
    public Boolean isLeaf(TreeItem treeItem) {
        return null;
    }

    public Boolean isConnectable(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (mShowEndpointRef) {
            return dataObj instanceof Roles;
        } else {
            return dataObj instanceof PartnerLink;
        }
    }

    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return this;
    }

    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return this;
    }

    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        if (dataObj instanceof PartnerLinkContainer) {
            return NbBundle.getMessage(PartnerLinkTreeExtModel.class,
                    "PARTNER_LINK_CONTAINER"); // NOI18N
        }
        //
        if (dataObj instanceof Roles) {
            return dataObj.toString();
        }
        //
        return null;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof PartnerLinkContainer) {
            return NodeType.VARIABLE_CONTAINER.getIcon();
        } 
        return null;
    }

    public List<Action> getMenuActions(TreeItem treeItem, Object context, 
            TreePath treePath) {
        return null;
    }

    public String getToolTipText(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();

//        if (dataObj instanceof PartnerLinkContainer) {
//            String type = ((PartnerLinkContainer) dataObj).getBpelModel().
//                    getProcess().getName();
//            String name = getDisplayName(treeItem);
//            return SchemaTreeInfoProvider.getColorTooltip(
//                    null, name, type, null);
//        }

        if (dataObj instanceof PartnerLink) {
            PartnerLink pLink = (PartnerLink) dataObj;
            String result;
            result = "<html> <body> Partner Link ";
            if (pLink.getName() != null) {
                result = result + "<b><font color =#7C0000>" + pLink.getName() + 
                        "</font></b>";
            }
            if (pLink.getDocumentation() != null) {
                result = result + "<hr><p width = 486>" + pLink.getDocumentation() + "</p>";
            }
            if (pLink.getMyRole() != null) {
                result = result + "<hr><p><b><font color =#000099> myRole= </font></b>" 
                        + pLink.getMyRole().getRefString() + "</p>";
            }
            if (pLink.getPartnerRole() != null) {
                result = result + "<b><font color =#000099> partnerRole= </font></b>" +
                        pLink.getPartnerRole().getRefString();
            }
            result = result + " </body>";
            return result;
        }

        if (dataObj instanceof Roles) {
            Object parent = treeItem.getParent().getDataObject();
            String value = null;
            String nameSpace = null;
            
            if (parent instanceof PartnerLink) {
                PartnerLink pLink = (PartnerLink) parent;
                if (Roles.MY_ROLE.equals(dataObj)) {
                    value = pLink.getMyRole().getRefString();
                    nameSpace = pLink.getMyRole().getEffectiveNamespace();
                }
                if (Roles.PARTNER_ROLE.equals(dataObj)) {
                    value = pLink.getPartnerRole().getRefString();
                    nameSpace = pLink.getPartnerRole().getEffectiveNamespace();
                }
            }
            String result;
            result ="<html><body><b><font color =#000099>" + 
                    ((Roles) dataObj).toString() + " = </font></b>";
            if (value != null) {
                result = result + value;
            }
            if (nameSpace != null && nameSpace.length() > 0) {
                result = result + "<hr> NameSpace= " +nameSpace;
            }
            result = result + "</body>";
            
            return result;
        }
        //
        return null;
    }

}
