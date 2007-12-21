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
package org.netbeans.modules.xslt.mapper.model.targettree;

import java.awt.Image;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xslt.mapper.model.PredicatedAxiComponent;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.DeletePredicateAction;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.EditPredicateAction;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.PredicateManager;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;

/**
 * A schema node with predicates
 *
 * @author nk160297
 */
public class PredicatedSchemaNode extends SchemaNode {
    
    /** Creates a new instance of PlaceholderNode */
    public PredicatedSchemaNode(PredicatedAxiComponent component,  XsltMapper mapper) {
        super(component, mapper);
    }
    
    public PredicatedAxiComponent getPredicatedAxiComp() {
        return (PredicatedAxiComponent)getDataObject();
    }
    
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public AXIComponent getType() {
        return ((PredicatedAxiComponent)getDataObject()).getType();
    }
    
    public String toString(){
        XPathPredicateExpression[] predArr = getPredicatedAxiComp().getPredicates();
        String predicatesText = PredicateManager.toString(predArr);
        if (predicatesText.length() == 0) {
            return super.toString();
        } else {
            return super.toString() + " " + predicatesText;
        }
    }
    
    // TODO rewrite. The icon should be a bit different.
    public Image getIcon() {
        return super.getIcon();
    }
    
    public String getName() {
        XPathPredicateExpression[] predArr = getPredicatedAxiComp().getPredicates();
        String predicatesText = PredicateManager.toString(predArr);
        if (predicatesText.length() == 0) {
            return super.getName();
        } else {
            return super.getName() + " " + predicatesText;
        }
    }
    
    public String getTooltipText() {
        XPathPredicateExpression[] predArr = getPredicatedAxiComp().getPredicates();
        String predicatesText = PredicateManager.toString(predArr);
        if (predicatesText.length() == 0) {
            return super.getTooltipText();
        } else {
            return super.getTooltipText() + " " + predicatesText;
        }
    }
    
    // TODO rewrite. It should have specific popup menu
    public JPopupMenu constructPopupMenu() {
        JPopupMenu rootMenu = new JPopupMenu();
        //
        Action newAction;
        //
        if (isSourceViewNode()) {
            AXIComponent sc = getType();
            if (sc instanceof Element) {
                newAction = new EditPredicateAction(getMapper(), this);
                rootMenu.add(newAction);
                newAction = new DeletePredicateAction(getMapper(), this);
                rootMenu.add(newAction);
            }
        }
        //
        return rootMenu;
    }
    
    
}
