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

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.mapper.tree.spi.TreeItemInfoProvider;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.cast.AbstractPseudoComp;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.mapper.tree.actions.AddCastAction;
import org.netbeans.modules.bpel.mapper.tree.actions.AddPseudoCompAction;
import org.netbeans.modules.bpel.mapper.tree.actions.AddPredicateAction;
import org.netbeans.modules.bpel.mapper.tree.actions.AddSpecialStepAction;
import org.netbeans.modules.bpel.mapper.tree.actions.DeleteCastAction;
import org.netbeans.modules.bpel.mapper.tree.actions.DeletePredicateAction;
import org.netbeans.modules.bpel.mapper.tree.actions.DeletePseudoCompAction;
import org.netbeans.modules.bpel.mapper.tree.actions.EditPredicateAction;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.mapper.tree.images.NodeIcons;
import org.netbeans.modules.bpel.mapper.tree.spi.MapperTcContext;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTestType;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.openide.util.NbBundle;
import org.netbeans.modules.bpel.model.api.support.Roles;
import org.netbeans.modules.xml.schema.model.Any;

/**
 * The implementation of the TreeItemInfoProvider for the variables' tree.
 * 
 * @author nk160297
 * @author AlexanderPermyakov
 */
public class VariableTreeInfoProvider implements TreeItemInfoProvider {
    
    public static final String ANY_ELEMENT = 
            NbBundle.getMessage(VariableTreeInfoProvider.class, "ANY_ELEMENT"); // NOI18N
    
    public static final String ANY_ATTRIBUTE = 
            NbBundle.getMessage(VariableTreeInfoProvider.class, "ANY_ATTRIBUTE"); // NOI18N
    
    private static VariableTreeInfoProvider singleton = new VariableTreeInfoProvider();
    
    public static VariableTreeInfoProvider getInstance() {
        return singleton;
    }

    public String getDisplayName(Object treeItem) {
        if (treeItem instanceof Process) {
            return NodeType.VARIABLE_CONTAINER.getDisplayName();
        } 
        if (treeItem instanceof ElementReference) {
            NamedComponentReference<GlobalElement> elementRef = 
                    ((ElementReference)treeItem).getRef();
            QName qName = elementRef.getQName();
            return qName.getLocalPart();
        }
        if (treeItem instanceof Named) {
            return ((Named)treeItem).getName();
        }
        if (treeItem instanceof AbstractPredicate) {
            return ((AbstractPredicate)treeItem).getDisplayName();
        }
        if (treeItem instanceof BpelEntity) {
            if (treeItem instanceof Variable) {
                return ((Variable)treeItem).getVariableName();
            }
            //
            Class<? extends BpelEntity> bpelInterface = 
                    ((BpelEntity)treeItem).getElementType();
            NodeType nodeType = EditorUtil.getBasicNodeType(bpelInterface);
            if (nodeType != null && nodeType != NodeType.UNKNOWN_TYPE) {
                return nodeType.getDisplayName();
            }
        }
        if (treeItem instanceof AbstractVariableDeclaration) {
            return ((AbstractVariableDeclaration)treeItem).getVariableName();
        }
        //
        if (treeItem instanceof AbstractTypeCast) {
            Object castableObject = ((AbstractTypeCast)treeItem).getCastedObject();
            GlobalType gType = ((AbstractTypeCast)treeItem).getType();
            return "(" + gType.getName() + ")" + getDisplayName(castableObject);
        }
        //
        if (treeItem instanceof AbstractPseudoComp) {
            AbstractPseudoComp pseudo = ((AbstractPseudoComp)treeItem);
            if (pseudo.isAttribute()) {
                return "(" + pseudo.getName() + ")" + ANY_ATTRIBUTE;
            } else {
                return "(" + pseudo.getName() + ")" + ANY_ELEMENT;
            }
        }
        //
        if (treeItem instanceof AnyElement) {
            return ANY_ELEMENT;
        }
        //
        if (treeItem instanceof AnyAttribute) {
            return ANY_ATTRIBUTE;
        }
        //
        return null;
    }

    public Icon getIcon(Object treeItem) {
        if (treeItem instanceof BpelEntity) {
            if (treeItem instanceof Variable) {
                Variable var = (Variable)treeItem;
                VariableStereotype vst = EditorUtil.getVariableStereotype(var);
                Image img = NodeType.VARIABLE.getImage(vst);
                return new ImageIcon(img);
            }
            //
            if (treeItem instanceof Process) {
                return NodeType.VARIABLE_CONTAINER.getIcon();
            }
            //
            Class<? extends BpelEntity> bpelInterface = 
                    ((BpelEntity)treeItem).getElementType();
            NodeType nodeType = EditorUtil.getBasicNodeType(bpelInterface);
            if (nodeType != null && nodeType != NodeType.UNKNOWN_TYPE) {
                Icon icon = nodeType.getIcon();
                if (icon != null) {
                    return icon;
                }
            }
        }
        //
        if (treeItem instanceof AbstractVariableDeclaration) {
            AbstractVariableDeclaration var = (AbstractVariableDeclaration)treeItem;
            VariableStereotype vst = EditorUtil.getVariableStereotype(var);
            Image img = NodeType.VARIABLE.getImage(vst);
            return new ImageIcon(img);
        }
        //
        if (treeItem instanceof SchemaComponent) {
            if (treeItem instanceof Element) {
                Element element = (Element)treeItem;
                boolean isOptional = false;
                boolean isRepeating = false;
                String maxOccoursStr = null;
                if (element instanceof GlobalElement) {
                    return NodeIcons.ELEMENT.getIcon();
                } else if (element instanceof LocalElement) {
                    LocalElement lElement = (LocalElement)element;
                    isOptional = lElement.getMinOccursEffective() < 1;
                    //
                    maxOccoursStr = lElement.getMaxOccursEffective();
                } else if (element instanceof ElementReference) {
                    ElementReference elementRef = (ElementReference)element;
                    isOptional = elementRef.getMinOccursEffective() < 1;  
                    //
                    maxOccoursStr = elementRef.getMaxOccursEffective();
                }
                //
                if (maxOccoursStr != null) {
                    try {
                        int maxOccoursInt = Integer.parseInt(maxOccoursStr);
                        isRepeating = maxOccoursInt > 1;  
                    } catch (NumberFormatException ex) {
                        // Do Nothing
                        isRepeating = true;
                    }
                }
                //
                if (isOptional) {
                    if (isRepeating) {
                        return NodeIcons.ELEMENT_OPTIONAL_REPEATING.getIcon();
                    } else {
                        return NodeIcons.ELEMENT_OPTIONAL.getIcon();
                    }
                } else {
                    if (isRepeating) {
                        return NodeIcons.ELEMENT_REPEATING.getIcon();
                    } else {
                        return NodeIcons.ELEMENT.getIcon();
                    }
                }
            } 
            //
            if (treeItem instanceof Attribute) {
                 Attribute attribute = (Attribute)treeItem;
                if (attribute instanceof LocalAttribute) {
                    Use use = ((LocalAttribute)attribute).getUseEffective();
                    if (use == Use.OPTIONAL) {
                        return NodeIcons.ATTRIBUTE_OPTIONAL.getIcon();
                    } else {
                        return NodeIcons.ATTRIBUTE.getIcon();
                    }
                } else {
                    return NodeIcons.ATTRIBUTE.getIcon();
                }
            } 
            //
            if (treeItem instanceof GlobalType) {
                return NodeIcons.UNKNOWN_IMAGE;
            } 
            //
            if (treeItem instanceof AnyElement) {
                AnyElement anyElement = (AnyElement)treeItem;
                boolean isOptional = anyElement.getMinOccursEffective() < 1;
                String maxOccoursStr = anyElement.getMaxOccursEffective();
                //
                boolean isRepeating = false;
                //
                if (maxOccoursStr != null) {
                    try {
                        int maxOccoursInt = Integer.parseInt(maxOccoursStr);
                        isRepeating = maxOccoursInt > 1;  
                    } catch (NumberFormatException ex) {
                        // Do Nothing
                        isRepeating = true;
        } 
                }
        //
                if (isOptional) {
                    if (isRepeating) {
                        return NodeIcons.ELEMENT_OPTIONAL_REPEATING.getIcon();
                    } else {
                        return NodeIcons.ELEMENT_OPTIONAL.getIcon();
                    }
                } else {
                    if (isRepeating) {
                        return NodeIcons.ELEMENT_REPEATING.getIcon();
                    } else {
                        return NodeIcons.ELEMENT.getIcon();
                    }
                }
            }
            //
            if (treeItem instanceof AnyAttribute) {
                // The Any Attribute doesn't have multiplisity parameters
                return NodeIcons.ATTRIBUTE.getIcon();
            }
        } 
        //
        if (treeItem instanceof Part) {
            return NodeType.MESSAGE_PART.getIcon();
        }
        //
        if (treeItem instanceof AbstractPredicate) {
            SchemaComponent sComp = 
                    ((AbstractPredicate)treeItem).getSComponent();
            return getIcon(sComp);
        }
        //
        if (treeItem instanceof AbstractTypeCast) {
            Object castableObject = ((AbstractTypeCast)treeItem).getCastedObject();
            return getIcon(castableObject);
        }
        //
        if (treeItem instanceof AbstractPseudoComp) {
            AbstractPseudoComp pseudo = (AbstractPseudoComp)treeItem;
            if (pseudo.isAttribute()) {
                return NodeIcons.ATTRIBUTE.getIcon();
            } else {
                return NodeIcons.ELEMENT_OPTIONAL.getIcon();
            }
        }
        //
        return null;
    }

    private void populateActionsList(Object treeItem, List<Action> result, 
            MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            Iterable<Object> dataObjectPathItrb) {
        //
        boolean isProcessed = false;
        if (treeItem instanceof SchemaComponent) {
            if (!(treeItem instanceof Any)) {
                //
                // if (BpelMapperUtils.isRepeating((SchemaComponent)treeItem)) {
                Action action = new AddPredicateAction(
                        mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
                result.add(action);
                //
                // Only for components which have any children!
                boolean notLeaf = XPathUtils.hasSubcomponents((SchemaComponent)treeItem);
                if (notLeaf) {
                    addSpecialStepActions(result, mapperTcContext, inLeftTree, 
                            treePath, dataObjectPathItrb);
                }
            }
            isProcessed = true;
        } else if (treeItem instanceof AbstractPredicate) {
            Action action = new EditPredicateAction(
                    mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
            result.add(action);
            //
            action = new DeletePredicateAction(
                    mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
            result.add(action);
            isProcessed = true;
        } else if (treeItem instanceof AbstractTypeCast) {
            Action action = new DeleteCastAction(
                    mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
            result.add(action);
            //
            Object castableObject = ((AbstractTypeCast)treeItem).getCastedObject();
            populateActionsList(castableObject, result, 
                    mapperTcContext, inLeftTree, treePath, 
                    dataObjectPathItrb);

            
            isProcessed = true;
        } else if (treeItem instanceof AbstractPseudoComp) {
            Action action = new DeletePseudoCompAction(
                    mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
            result.add(action);
            //
            GlobalType pseudoType = ((AbstractPseudoComp)treeItem).getType();
            populateActionsList(pseudoType, result, 
                    mapperTcContext, inLeftTree, treePath, 
                    dataObjectPathItrb);

            
            isProcessed = true;
        }
        //
        if (!isProcessed) {
            // If the tree item is a variable or a part then use its schema type!
            SchemaComponent sComp = null;
            if (treeItem instanceof VariableDeclarationScope) {
                // nothing to add
            } else if (treeItem instanceof VariableDeclarationWrapper) {
                VariableDeclaration varDecl = 
                        ((VariableDeclarationWrapper)treeItem).getDelegate();
                sComp = EditorUtil.getVariableSchemaType(varDecl);
            } else if (treeItem instanceof VariableDeclaration) {
                sComp = EditorUtil.getVariableSchemaType((VariableDeclaration)treeItem);
            } else if (treeItem instanceof Part) {
                Part part = (Part)treeItem;
                sComp = EditorUtil.getPartType(part);
            } 
            if (sComp != null) {
                //
                // Only for components which have any children!
                boolean notLeaf = XPathUtils.hasSubcomponents((SchemaComponent)sComp);
                if (notLeaf) {
                    addSpecialStepActions(result, mapperTcContext, inLeftTree, 
                            treePath, dataObjectPathItrb);
                }
            } 
        }
    }
    
    public List<Action> getMenuActions(MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            Iterable<Object> dataObjectPathItrb) {
        //
        Object treeItem = dataObjectPathItrb.iterator().next();
        //
        Mapper mapper = mapperTcContext.getMapper();
        BpelDesignContext context = mapperTcContext.
                getDesignContextController().getContext();
        if (mapper == null || context == null) {
            return Collections.EMPTY_LIST;
        }
        //
        List<Action> result = new ArrayList<Action>();
        //
        if (!(treeItem instanceof LocationStep || 
                treeItem instanceof AbstractPredicate || 
                treeItem instanceof AbstractTypeCast || 
                treeItem instanceof AbstractPseudoComp)) {
            GlobalType gType = BpelMapperUtils.getGlobalType(treeItem);
            if (gType != null) {
                Action action = new AddCastAction(gType, mapperTcContext, 
                        inLeftTree, treePath, dataObjectPathItrb);
                result.add(action);
            }
        }
        //
        if  (treeItem instanceof AnyElement) {
            AnyElement anyElement = (AnyElement)treeItem;
            Action action = new AddPseudoCompAction(anyElement, mapperTcContext, 
                    inLeftTree, treePath, dataObjectPathItrb);
            result.add(action);
        }
        //
        if  (treeItem instanceof AnyAttribute) {
            AnyAttribute anyAttr = (AnyAttribute)treeItem;
            Action action = new AddPseudoCompAction(anyAttr, mapperTcContext, 
                    inLeftTree, treePath, dataObjectPathItrb);
            result.add(action);
        }
        //
        populateActionsList(treeItem, result, mapperTcContext, inLeftTree, 
                treePath, dataObjectPathItrb);
        //
        return result;
//        if (result.isEmpty()) {
//            return null;
//        } else {
//            return result;
//        }
    }
    
    private void addSpecialStepActions(List<Action> result, 
            MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            Iterable<Object> dataObjectPathItrb) {
        Action action;
        action = new AddSpecialStepAction(StepNodeTestType.NODETYPE_NODE, 
                mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
        result.add(action);
        //
        action = new AddSpecialStepAction(StepNodeTestType.NODETYPE_TEXT, 
                mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
        result.add(action);
        //
        action = new AddSpecialStepAction(StepNodeTestType.NODETYPE_COMMENT, 
                mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
        result.add(action);
        //
        action = new AddSpecialStepAction(StepNodeTestType.NODETYPE_PI, 
                mapperTcContext, inLeftTree, treePath, dataObjectPathItrb);
        result.add(action);
    }

    public String getToolTipText(Iterable<Object> dataObjectPathItrb) {
        Iterator itr = dataObjectPathItrb.iterator();
        Object treeItem = itr.next();

        if (treeItem instanceof PartnerLinkContainer) {
            String type = ((PartnerLinkContainer) treeItem).getBpelModel().
                    getProcess().getName();
            return getColorTooltip(null,"Partner Links", type, null);
        }

        if (treeItem instanceof PartnerLink) {
            PartnerLink pLink = (PartnerLink) treeItem;
            String result;
            result = "<html> <body> Partner Link ";
            if (pLink.getName() != null) {
                result = result + "<b><font color =#7C0000>" + pLink.getName() + 
                        "</font></b>";
            }
            if (pLink.getDocumentation() != null) {
                result = result + "<hr>" + pLink.getDocumentation();
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

        if (treeItem instanceof Roles) {
            Object parent = itr.next();
            String value = null;
            String nameSpace = null;
            
            if (parent instanceof PartnerLink) {
                PartnerLink pLink = (PartnerLink) parent;
                if (Roles.MY_ROLE.equals(treeItem)) {
                    value = pLink.getMyRole().getRefString();
                    nameSpace = pLink.getMyRole().getEffectiveNamespace();
                }
                if (Roles.PARTNER_ROLE.equals(treeItem)) {
                    value = pLink.getPartnerRole().getRefString();
                    nameSpace = pLink.getMyRole().getEffectiveNamespace();
                }
            }
            String result;
            result ="<html><body><b><font color =#000099>" + 
                    ((Roles) treeItem).toString() + " = </font></b>";
            if (value != null) {
                result = result + value;
            }
            if (nameSpace != null && nameSpace.length() > 0) {
                result = result + "<hr> NameSpace= " +nameSpace;
            }
            result = result + "</body>";
            
            return result;
        }

        return getToolTipText(treeItem);
    }
    
    private String getToolTipText(Object treeItem) {
        String name = getDisplayName(treeItem);
        String nameSpase = null;
        if (treeItem instanceof SchemaComponent) {
            nameSpase = ((SchemaComponent) treeItem).getModel().
                    getEffectiveNamespace((SchemaComponent) treeItem);
        }

        String type = null;

        if (treeItem instanceof GlobalElement) {
            if (((GlobalElement) treeItem).getType() != null) {
                type = ((GlobalElement) treeItem).getType().getRefString();
                return getColorTooltip(null, name, type, nameSpase);
            }
        }

        if (treeItem instanceof Part) {
            if (((Part) treeItem).getType() != null) {
                return getColorTooltip(null, name, ((Part) treeItem).getType().
                        getRefString(), nameSpase);
            }
            if (((Part) treeItem).getElement() != null) {
                return getColorTooltip(null, name, ((Part) treeItem).
                        getElement().getRefString(), nameSpase);
            }
        }

        if (treeItem instanceof LocalElement) {
            if (((LocalElement) treeItem).getType() != null) {
                return getColorTooltip(null, name, ((LocalElement) treeItem).
                        getType().getRefString(), nameSpase);
            }
        }

        if (treeItem instanceof LocalAttribute) {
            if (((LocalAttribute) treeItem).getType() != null) {
                return getColorTooltip(null, name, ((LocalAttribute) treeItem).
                        getType().getRefString(), nameSpase);
            }
        }

        if (treeItem instanceof GlobalAttribute) {
            if (((GlobalAttribute) treeItem).getType() != null) {
                return getColorTooltip(null, name, ((GlobalAttribute) treeItem).
                        getType().getRefString(), nameSpase);
            }
        }

        if (treeItem instanceof GlobalType) {
            return getColorTooltip(null, name, 
                    ((GlobalType) treeItem).getName(), nameSpase);
        }

        if (treeItem instanceof Variable) {
            if (((Variable) treeItem).getMessageType() != null) {
                return getColorTooltip(null, name, ((Variable) treeItem).
                        getMessageType().getRefString(), nameSpase);
            }
            if (((Variable) treeItem).getType() != null) {
                return getColorTooltip(null, name, ((Variable) treeItem).
                        getType().getRefString(), nameSpase);
            }
            if (((Variable) treeItem).getElementType() != null) {
                return getColorTooltip(null, name, ((Variable) treeItem).
                        getElementType().getName(), nameSpase);
            }
        }

        if (treeItem instanceof Any) {
            return getColorTooltip(null, name, "ANY_TYPE", nameSpase);
        }

        if (treeItem instanceof Process) {
            return getColorTooltip(null, name, ((Process) treeItem).getName(), null);
        }

        if (treeItem instanceof AbstractTypeCast) {
            Object castableObject = ((AbstractTypeCast) treeItem).getCastedObject();
            String baseTooltip = getToolTipText(castableObject);
            GlobalType gType = ((AbstractTypeCast) treeItem).getType();
            String castedToLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class, "CASTED_TO"); // NOI18N
            return baseTooltip + " " + castedToLbl + " " + gType.getName();
        }
        //
        if (treeItem instanceof AbstractPseudoComp) {
            AbstractPseudoComp pseudo = (AbstractPseudoComp)treeItem;
            //
            String title = null;
            if (pseudo.isAttribute()) {
                title = NbBundle.getMessage(this.getClass(), "PSEUDO_ATTRIBUTE"); // NOI18N
            } else {
                title = NbBundle.getMessage(this.getClass(), "PSEUDO_ELEMENT"); // NOI18N
            }
            //
            String body = getColorTooltip(title ,pseudo.getName(), 
                    pseudo.getType().getName(), pseudo.getNamespace());
            //
            return body;
        }
        //
        String notNamedTypeLbl = NbBundle.getMessage(
                VariableTreeInfoProvider.class, "NOT_NAMED_TYPE"); // NOI18N

        return new String("<html><body>" + name +
                "<b><font color=#7C0000>" + " " + notNamedTypeLbl +
                "</font></b> <hr> Localy define type, this type does not have name" +
                "</body>");
    }
    
    private String getColorTooltip(String title, String name, String type, String nameSpace) {
        StringBuilder result = new StringBuilder();
        result.append("<html><body>");
        //
        if (title != null) {
            result.append("<p align=\"center\"><b>" + title + "</b></p>").append("<hr>");
        }
        //
        if (name != null) {
            result.append(name);
        }
        //
        if (type != null) {
            result.append("<b><font color=#7C0000>" + " " + type + "</font></b>");
        }
        //
        if (nameSpace != null) {
            result.append("<hr>" + "Namespace: " + nameSpace);
        }
        //
        result.append("</body>");
        //
        return result.toString();
    }
}
