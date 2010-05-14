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

package org.netbeans.modules.xslt.tmap.nodes;

import java.awt.Image;
import org.netbeans.modules.soa.ui.images.FolderIcon;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ImportsContainerNode extends TMapComponentNode<DecoratedImportsContainer> {

    public ImportsContainerNode(TransformMap ref, Lookup lookup) {
        this(ref, Children.LEAF, lookup);
    }

    public ImportsContainerNode(TransformMap ref, Children children, Lookup lookup) {
        super(new DecoratedImportsContainer(ref), children, lookup);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.IMPORTS_CONTAINER;
    }

    @Override
    public Image getIcon(int type) {
        return FolderIcon.getClosedIcon();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return FolderIcon.getOpenedIcon();
    }
    

    @Override
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_NEWTYPES
        };
    }

    @Override
    public ActionType[] getAddActionArray() {
        return new ActionType[] {ActionType.ADD_WSDL_IMPORT};
    }
}
