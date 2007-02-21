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

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 *
 * @author nk160297
 */
public class AddNestedAxiGroup implements ActionGroupConstructor {

    protected XsltMapper myXsltMapper;
    protected TreeNode myTreeNode;
    
    public AddNestedAxiGroup(XsltMapper xsltMapper, TreeNode node) {
        myXsltMapper = xsltMapper;
        myTreeNode = node;
    }

    public Action[] getActions() {
        AXIComponent type = myTreeNode.getType();
        if (type == null || !(type instanceof Element)) {
            return null;
        }
        //
        List<Action> actions = new ArrayList<Action>();
        Element parentElement = (Element)type;
        //
        List<AbstractAttribute> attributes =  parentElement.getAttributes();
        for (AbstractAttribute attribute : attributes) {
            if (attribute instanceof Attribute) {
                Action newAction = new AddNestedAxiAttribute(
                        myXsltMapper, myTreeNode, (Attribute)attribute);
                actions.add(newAction);
            }
        }
        //
        List<AbstractElement> elements = parentElement.getChildElements();
        for (AbstractElement element : elements) {
            if (element instanceof Element) {
                Action newAction = new AddNestedAxiElement(
                        myXsltMapper, myTreeNode, (Element)element);
                actions.add(newAction);
            }
        }
        //
        Action[] result = actions.toArray(new Action[actions.size()]);
        //
        return result;
    }
    
}
