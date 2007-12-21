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

package org.netbeans.modules.xslt.mapper.model.nodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.soa.ui.axinodes.NodeType;
import org.netbeans.modules.soa.ui.axinodes.NodeType.BadgeModificator;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xslt.mapper.model.BranchConstructor;
import org.netbeans.modules.xslt.mapper.model.ModelBridge;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author nk160297
 */
public class AddNestedAxiAttribute extends XsltNodeAction {
    
    private static final long serialVersionUID = 1L;
    
    protected Attribute myNewAxiAttribute;
    
    public AddNestedAxiAttribute(XsltMapper xsltMapper, TreeNode node,
            Attribute attribute) {
        super(xsltMapper, node);
        myNewAxiAttribute = attribute;
        postInit();
    }
    
    public String getDisplayName() {
        return myNewAxiAttribute.getName();
    }
    
    public Icon getIcon() {
        Icon icon = new ImageIcon(NodeType.ATTRIBUTE.getImage(BadgeModificator.SINGLE));
        return icon;
    }
    
    public void actionPerformed(ActionEvent e) {
        Object dataObject = myTreeNode.getDataObject();
        if (dataObject != null && dataObject instanceof XslComponent) {
            XslComponent parentComp = (XslComponent)dataObject;
            BranchConstructor.createXslElementOrAttribute(parentComp, myNewAxiAttribute, getMapper());
        }
    }
    
}
