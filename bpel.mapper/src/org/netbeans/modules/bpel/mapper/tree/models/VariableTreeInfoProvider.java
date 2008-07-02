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
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.soa.ui.tree.TreeItem;
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
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.bpel.mapper.model.MapperTreeContext;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTestType;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.openide.util.NbBundle;
import org.netbeans.modules.soa.ui.schema.SchemaIcons;
import org.netbeans.modules.soa.ui.schema.SchemaTreeInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.xml.schema.model.Any;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;

/**
 * The implementation of the TreeItemInfoProvider for the variables' tree.
 * 
 * @author nk160297
 * @author AlexanderPermyakov
 */
public class VariableTreeInfoProvider 
        implements TreeItemInfoProvider, TreeItemActionsProvider {
    
    private static VariableTreeInfoProvider singleton = new VariableTreeInfoProvider();
    
    public static VariableTreeInfoProvider getInstance() {
        return singleton;
    }

    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return getDisplayByDataObj(dataObj);
    }

    private String getDisplayByDataObj(Object dataObj) {
        if (dataObj instanceof SchemaComponent) {
            return SchemaTreeInfoProvider.getInstance().getDisplayByDataObj(dataObj);
        }
        if (dataObj instanceof Process) {
            return NodeType.VARIABLE_CONTAINER.getDisplayName();
        } 
        if (dataObj instanceof Named) {
            return ((Named)dataObj).getName();
        }
        if (dataObj instanceof AbstractPredicate) {
            return ((AbstractPredicate)dataObj).getDisplayName();
        }
        if (dataObj instanceof BpelEntity) {
            if (dataObj instanceof Variable) {
                return ((Variable)dataObj).getVariableName();
            }
            //
            Class<? extends BpelEntity> bpelInterface = 
                    ((BpelEntity)dataObj).getElementType();
            NodeType nodeType = EditorUtil.getBasicNodeType(bpelInterface);
            if (nodeType != null && nodeType != NodeType.UNKNOWN_TYPE) {
                return nodeType.getDisplayName();
            }
        }
        if (dataObj instanceof AbstractVariableDeclaration) {
            return ((AbstractVariableDeclaration)dataObj).getVariableName();
        }
        //
        if (dataObj instanceof AbstractTypeCast) {
            Object castableObject = ((AbstractTypeCast)dataObj).getCastedObject();
            GlobalType gType = ((AbstractTypeCast)dataObj).getType();
            return "(" + gType.getName() + ")" + getDisplayByDataObj(castableObject);
        }
        //
        if (dataObj instanceof XPathPseudoComp) {
            return AbstractPseudoComp.getDisplayName((XPathPseudoComp)dataObj);
        }
        //
        return null;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return getIconByDataObj(dataObj);
    }    
        
    private Icon getIconByDataObj(Object dataObj) {
        if (dataObj instanceof BpelEntity) {
            if (dataObj instanceof Variable) {
                Variable var = (Variable)dataObj;
                VariableStereotype vst = EditorUtil.getVariableStereotype(var);
                Image img = NodeType.VARIABLE.getImage(vst);
                return new ImageIcon(img);
            }
            //
            if (dataObj instanceof Process) {
                return NodeType.VARIABLE_CONTAINER.getIcon();
            }
            //
            Class<? extends BpelEntity> bpelInterface = 
                    ((BpelEntity)dataObj).getElementType();
            NodeType nodeType = EditorUtil.getBasicNodeType(bpelInterface);
            if (nodeType != null && nodeType != NodeType.UNKNOWN_TYPE) {
                Icon icon = nodeType.getIcon();
                if (icon != null) {
                    return icon;
                }
            }
        }
        //
        if (dataObj instanceof AbstractVariableDeclaration) {
            AbstractVariableDeclaration var = (AbstractVariableDeclaration)dataObj;
            VariableStereotype vst = EditorUtil.getVariableStereotype(var);
            Image img = NodeType.VARIABLE.getImage(vst);
            return new ImageIcon(img);
        }
        //
        if (dataObj instanceof SchemaComponent) {
            return SchemaTreeInfoProvider.getInstance().getIconByDataObj(dataObj);
        } 
        //
        if (dataObj instanceof Part) {
            return NodeType.MESSAGE_PART.getIcon();
        }
        //
        if (dataObj instanceof AbstractPredicate) {
            SchemaCompHolder sCompHolder = 
                    ((AbstractPredicate)dataObj).getSCompHolder();
            return getIconByDataObj(sCompHolder.getHeldComponent());
        }
        //
        if (dataObj instanceof AbstractTypeCast) {
            Object castableObject = ((AbstractTypeCast)dataObj).getCastedObject();
            return getIconByDataObj(castableObject);
        }
        //
        if (dataObj instanceof XPathPseudoComp) {
            XPathPseudoComp pseudo = (XPathPseudoComp)dataObj;
            if (pseudo.isAttribute()) {
                return SchemaIcons.ATTRIBUTE.getIcon();
            } else {
                return SchemaIcons.ELEMENT_OPTIONAL.getIcon();
            }
        }
        //
        return null;
    }

    private void populateActionsList(Object dataObj, List<Action> result, 
            MapperTcContext mapperTcContext, 
            boolean inLeftTree, TreePath treePath, 
            TreeItem treeItem) {
        //
        boolean isProcessed = false;
        if (dataObj instanceof SchemaComponent) {
            if (!(dataObj instanceof Any)) {
                //
                // if (BpelMapperUtils.isRepeating((SchemaComponent)dataObj)) {
                Action action = new AddPredicateAction(
                        mapperTcContext, inLeftTree, treePath, treeItem);
                result.add(action);
                //
                // Only for components which have any children!
                boolean notLeaf = XPathUtils.hasSubcomponents((SchemaComponent)dataObj);
                if (notLeaf) {
                    addSpecialStepActions(result, mapperTcContext, inLeftTree, 
                            treePath, treeItem);
                }
            }
            isProcessed = true;
        } else if (dataObj instanceof AbstractPredicate) {
            Action action = new EditPredicateAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(action);
            //
            action = new DeletePredicateAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(action);
            isProcessed = true;
        } else if (dataObj instanceof AbstractTypeCast) {
            Action action = new DeleteCastAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(action);
            //
            Object castableObject = ((AbstractTypeCast)dataObj).getCastedObject();
            populateActionsList(castableObject, result, 
                    mapperTcContext, inLeftTree, treePath, 
                    treeItem);

            
            isProcessed = true;
        } else if (dataObj instanceof AbstractPseudoComp) {
            Action action = new DeletePseudoCompAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(action);
            //
            GlobalType pseudoType = ((AbstractPseudoComp)dataObj).getType();
            populateActionsList(pseudoType, result, 
                    mapperTcContext, inLeftTree, treePath, 
                    treeItem);

            
            isProcessed = true;
        }
        //
        if (!isProcessed) {
            // If the tree item is a variable or a part then use its schema type!
            SchemaComponent sComp = null;
            if (dataObj instanceof VariableDeclarationScope) {
                // nothing to add
            } else if (dataObj instanceof VariableDeclarationWrapper) {
                VariableDeclaration varDecl = 
                        ((VariableDeclarationWrapper)dataObj).getDelegate();
                sComp = EditorUtil.getVariableSchemaType(varDecl);
            } else if (dataObj instanceof VariableDeclaration) {
                sComp = EditorUtil.getVariableSchemaType((VariableDeclaration)dataObj);
            } else if (dataObj instanceof Part) {
                Part part = (Part)dataObj;
                sComp = EditorUtil.getPartType(part);
            } 
            if (sComp != null) {
                //
                // Only for components which have any children!
                boolean notLeaf = XPathUtils.hasSubcomponents((SchemaComponent)sComp);
                if (notLeaf) {
                    addSpecialStepActions(result, mapperTcContext, inLeftTree, 
                            treePath, treeItem);
                }
            } 
        }
    }
    
    public List<Action> getMenuActions(TreeItem treeItem, 
            Object context, TreePath treePath) {
        //
        Object dataObj = treeItem.getDataObject();
        //
        assert context instanceof MapperTreeContext;
        MapperTreeContext mapperContext = (MapperTreeContext)context;
        MapperTcContext mapperTcContext = mapperContext.getMapperTcContext();
        boolean inLeftTree = mapperContext.inLeftTree();
        Mapper mapper = mapperTcContext.getMapper();
        BpelDesignContext designContext = mapperTcContext.
                getDesignContextController().getContext();
        if (mapper == null || designContext == null) {
            return Collections.EMPTY_LIST;
        }
        //
        List<Action> result = new ArrayList<Action>();
        //
        if (!(dataObj instanceof LocationStep || 
                dataObj instanceof AbstractPredicate || 
                dataObj instanceof AbstractTypeCast || 
                dataObj instanceof AbstractPseudoComp)) {
            GlobalType gType = BpelMapperUtils.getGlobalType(dataObj);
            if (gType != null) {
                Action action = new AddCastAction(gType, mapperTcContext, 
                        inLeftTree, treePath, treeItem);
                result.add(action);
            }
        }
        //
        if  (dataObj instanceof AnyElement) {
            AnyElement anyElement = (AnyElement)dataObj;
            Action action = new AddPseudoCompAction(anyElement, mapperTcContext, 
                    inLeftTree, treePath, treeItem);
            result.add(action);
        }
        //
        if  (dataObj instanceof AnyAttribute) {
            AnyAttribute anyAttr = (AnyAttribute)dataObj;
            Action action = new AddPseudoCompAction(anyAttr, mapperTcContext, 
                    inLeftTree, treePath, treeItem);
            result.add(action);
        }
        //
        populateActionsList(dataObj, result, mapperTcContext, inLeftTree, 
                treePath, treeItem);
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
            TreeItem treeItem) {
        Action action;
        action = new AddSpecialStepAction(StepNodeTestType.NODETYPE_NODE, 
                mapperTcContext, inLeftTree, treePath, treeItem);
        result.add(action);
        //
        action = new AddSpecialStepAction(StepNodeTestType.NODETYPE_TEXT, 
                mapperTcContext, inLeftTree, treePath, treeItem);
        result.add(action);
        //
        action = new AddSpecialStepAction(StepNodeTestType.NODETYPE_COMMENT, 
                mapperTcContext, inLeftTree, treePath, treeItem);
        result.add(action);
        //
        action = new AddSpecialStepAction(StepNodeTestType.NODETYPE_PI, 
                mapperTcContext, inLeftTree, treePath, treeItem);
        result.add(action);
    }

    public String getToolTipText(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        String name = getDisplayByDataObj(dataObj);
        return getToolTipTextByDataObj(dataObj, name);
    }

    private String getToolTipTextByDataObj(Object dataObj, String name) {
        String nameSpace = null;
        if (dataObj instanceof SchemaComponent) {
            String result = SchemaTreeInfoProvider.getInstance().
                    getToolTipTextByDataObj(dataObj, name);
            if (result != null) {
                return result;
            }
        }
        
        if (dataObj instanceof Part) {
            String partLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class, "MESSAGE_PART"); // NOI18N
            
            if (((Part) dataObj).getType() != null) {
                return SchemaTreeInfoProvider.getColorTooltip(
                        partLbl, name, ((Part) dataObj).getType().
                        getRefString(), nameSpace);
            }
            if (((Part) dataObj).getElement() != null) {
                return SchemaTreeInfoProvider.getColorTooltip(
                        partLbl, name, ((Part) dataObj).
                        getElement().getRefString(), nameSpace);
            }
        }

        if (dataObj instanceof Variable) {
            if (((Variable) dataObj).getMessageType() != null) {
                String varLbl = NbBundle.getMessage(
                        VariableTreeInfoProvider.class, 
                        "MESSAGE_VARIABLE"); // NOI18N
                return SchemaTreeInfoProvider.getColorTooltip(
                        varLbl, name, ((Variable) dataObj).
                        getMessageType().getRefString(), nameSpace);
            }
            //
            if (((Variable) dataObj).getType() != null) {
                String varLbl = NbBundle.getMessage(
                        VariableTreeInfoProvider.class, 
                        "TYPE_VARIABLE"); // NOI18N
                return SchemaTreeInfoProvider.getColorTooltip(
                        varLbl, name, ((Variable) dataObj).
                        getType().getRefString(), nameSpace);
            }
            //
            if (((Variable) dataObj).getElementType() != null) {
                String varLbl = NbBundle.getMessage(
                        VariableTreeInfoProvider.class, 
                        "ELEMENT_VARIABLE"); // NOI18N
                return SchemaTreeInfoProvider.getColorTooltip(
                        varLbl, name, ((Variable) dataObj).
                        getElementType().getName(), nameSpace);
            }
        }

//        if (dataObj instanceof Process) {
//            return SchemaTreeInfoProvider.getColorTooltip(
//                    null, name, ((Process) dataObj).getName(), null);
//        }

        if (dataObj instanceof AbstractTypeCast) {
            Object castableObject = ((AbstractTypeCast) dataObj).getCastedObject();
            String castedObjName = getDisplayByDataObj(castableObject);
            String baseTooltip = getToolTipTextByDataObj(castableObject, castedObjName);
            GlobalType gType = ((AbstractTypeCast) dataObj).getType();
            String castedToLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class, "CASTED_TO"); // NOI18N
            return baseTooltip + " " + castedToLbl + " " + gType.getName();
        }
        //
        if (dataObj instanceof AbstractPseudoComp) {
            AbstractPseudoComp pseudo = (AbstractPseudoComp)dataObj;
            //
            String title = null;
            if (pseudo.isAttribute()) {
                title = NbBundle.getMessage(this.getClass(), "PSEUDO_ATTRIBUTE"); // NOI18N
            } else {
                title = NbBundle.getMessage(this.getClass(), "PSEUDO_ELEMENT"); // NOI18N
            }
            //
            String body = SchemaTreeInfoProvider.getColorTooltip(title ,pseudo.getName(), 
                    pseudo.getType().getName(), pseudo.getNamespace());
            //
            return body;
        }
        //
        return null;
    }

}
