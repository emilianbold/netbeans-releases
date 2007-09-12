/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
