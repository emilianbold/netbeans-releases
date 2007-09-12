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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.netbeans.modules.soa.ui.SoaUiUtil;
import org.netbeans.modules.soa.ui.TooltipTextProvider;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.soa.ui.axinodes.NodeType;
import org.netbeans.modules.soa.ui.axinodes.NodeType.BadgeModificator;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.NumberBase;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.ActionConst;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddNestedRulesGroup;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddPredicateAction;
import org.netbeans.modules.xslt.mapper.model.nodes.actions.AddThisAxiComponentAction;
import org.netbeans.modules.xslt.mapper.model.nodes.visitor.NodeVisitor;
import org.netbeans.modules.xslt.mapper.view.PredicateManager;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey
 */
public class SchemaNode extends TreeNode implements TooltipTextProvider {
    
    //    private transient Boolean isSourceViewNode = null;
    
    /** Creates a new instance of PlaceholderNode */
    public SchemaNode(AXIComponent component,  XsltMapper mapper) {
        super(component, mapper);
    }
    
    protected SchemaNode(Object dataObject,  XsltMapper mapper) {
        super(dataObject, mapper);
    }
    
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public AXIComponent getType() {
        return (AXIComponent) getDataObject();
    }
    
    public boolean isMappable() {
        return true;
    }
    public String toString(){
        String name = ((AXIType) getType()).getName();
        return ((getType() instanceof Attribute) ? "@" : "")+ name;
    }
    
    protected List<TreeNode> loadChildren() {
        final ArrayList<TreeNode> result = new ArrayList<TreeNode>();
        
        AXIComponent axic = getType();
        if(axic instanceof Element){
            new AXIUtils.ElementVisitor(){
                public void visit(AXIComponent c){
                    TreeNode newNode = (TreeNode) NodeFactory.createNode(c, getMapper());
                    if (newNode != null){
                        newNode.setParent(SchemaNode.this);
                        result.add(newNode);
                    }
                    //
                    // Load predicated nodes for source view
                    if (isSourceViewNode()) {
                        XsltMapper mapper = getMapper();
                        PredicateManager pManager = mapper.getPredicateManager();
                        Collection<PredicatedSchemaNode> newPNodes =
                                pManager.createPredicatedNodes(newNode);
                        for (PredicatedSchemaNode newPNode : newPNodes) {
                            newPNode.setParent(SchemaNode.this);
                            result.add(newPNode);
                        }
                    }
                }
            }.visitSubelements((Element) axic);
        }
        return result;
        
    }
    
    public Image getIcon() {
        AXIComponent axiComponent = getType();
        if (axiComponent instanceof Element) {
            BadgeModificator mult = AxiomUtils.getElementBadge((Element)axiComponent);
            return NodeType.ELEMENT.getImage(mult);
        } else if (axiComponent instanceof Attribute) {
            Use attrUse = ((Attribute)axiComponent).getUse();
            if (attrUse == Use.OPTIONAL) {
                return NodeType.ATTRIBUTE.getImage(BadgeModificator.OPTIONAL);
            }
            return NodeType.ATTRIBUTE.getImage(BadgeModificator.SINGLE);
        } else {
            return null;
        }
    }
    
    public String getName() {
        AXIComponent axiComponent = getType();
        //
        String result = null;
        if (axiComponent instanceof Element) {
            result = ((Element)axiComponent).getName();
        } else if (axiComponent instanceof Attribute) {
            result = ((Attribute)axiComponent).getName();
        }
        //
        return result;
    }
    
    public String getName(boolean selected) {
        String result = getName();
        //
        if (!selected && !isSourceViewNode()) {
            result = SoaUiUtil.getFormattedHtmlString(true,
                    new SoaUiUtil.TextChunk(result, SoaUiUtil.INACTIVE_BLUE));
        }
        //
        return result;
    }
    
    public String getTooltipText() {
        AXIComponent axiComponent = getType();
        //
        if (axiComponent instanceof Element) {
            return AxiomUtils.getElementTooltip((Element)axiComponent);
        } else if (axiComponent instanceof Attribute) {
            return AxiomUtils.getAttributeTooltip((Attribute)axiComponent);
        }
        //
        return null;
    }
    
    public JPopupMenu constructPopupMenu() {
        JPopupMenu rootMenu = null;
        Action newAction;
        //
        if (isSourceViewNode()) {
            // Construct the popup menu for source tree
            //
            AXIComponent sc = getType();
            if (sc instanceof Element) {
                String max = ((Element)sc).getMaxOccurs();
                boolean isRepeating = false;
                if (NumberBase.UNBOUNDED_STRING.equals(max)) {
                    isRepeating = true;
                } else {
                    try {
                        int maxInt = Integer.parseInt(max);
                        if (maxInt > 1) {
                            isRepeating = true;
                        }
                    } catch (NumberFormatException ex) {
                        // DO NOTHING HERE
                    }
                }
                //
                if (isRepeating) {
                    newAction = new AddPredicateAction(getMapper(), this);
                    rootMenu = new JPopupMenu();
                    rootMenu.add(newAction);
                }
            }
            //
        } else {
            // Construct the popup menu for target tree
            String localizedName = NbBundle.getMessage(
                    ActionConst.class, ActionConst.ADD_MENU);
            JMenu addMenu = new JMenu(localizedName);
            //
            newAction = new AddThisAxiComponentAction(getMapper(), this);
            addMenu.add(newAction);
            //
            AddNestedRulesGroup nestedRules = new AddNestedRulesGroup(getMapper(), this);
            Action[] addNestedRuleArr = nestedRules.getActions();
            //
            if (addNestedRuleArr != null && addNestedRuleArr.length > 0) {
                addMenu.add(new JSeparator());
            }
            //
            if (addNestedRuleArr != null) {
                for (Action action : addNestedRuleArr) {
                    addMenu.add(action);
                }
            }
            // Add menu is added only if it's not empty
            if (addMenu.getMenuComponentCount() != 0) {
                rootMenu = new JPopupMenu();
                rootMenu.add(addMenu);
            }
        }
        //
        return rootMenu;
    }
    
    
}
