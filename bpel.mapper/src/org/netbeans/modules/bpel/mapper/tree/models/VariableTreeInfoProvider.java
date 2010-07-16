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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperPseudoComp;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperTypeCast;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.soa.xpath.mapper.tree.actions.AddSpecialStepAction;
import org.netbeans.modules.soa.xpath.mapper.tree.actions.DeleteCastAction;
import org.netbeans.modules.soa.xpath.mapper.tree.actions.DeletePredicateAction;
import org.netbeans.modules.soa.xpath.mapper.tree.actions.DeletePseudoCompAction;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.mapper.model.MapperTcContext;
import org.netbeans.modules.soa.xpath.mapper.model.MapperTreeContext;
import org.netbeans.modules.bpel.mapper.properties.PropertiesConstants;
import org.netbeans.modules.bpel.mapper.properties.PropertiesNode;
import org.netbeans.modules.bpel.mapper.properties.PropertiesUtils;
import org.netbeans.modules.bpel.mapper.tree.actions.AddNMPropertyAction;
import org.netbeans.modules.bpel.mapper.tree.actions.AddPropertyAction;
import org.netbeans.modules.bpel.mapper.tree.actions.DeleteNMPropertyAction;
import org.netbeans.modules.bpel.mapper.tree.actions.DeletePropertyAction;
import org.netbeans.modules.bpel.mapper.tree.actions.EditNMPropertyAction;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclarationScope;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.openide.util.NbBundle;
import org.netbeans.modules.soa.ui.schema.SchemaIcons;
import org.netbeans.modules.soa.ui.schema.SchemaTreeInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.context.XPathDesignContext;
import org.netbeans.modules.soa.xpath.mapper.tree.actions.AddCastAction;
import org.netbeans.modules.soa.xpath.mapper.tree.actions.AddPredicateAction;
import org.netbeans.modules.soa.xpath.mapper.tree.actions.AddPseudoCompAction;
import org.netbeans.modules.soa.xpath.mapper.tree.actions.EditPredicateAction;
import org.netbeans.modules.xml.schema.model.Any;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep.SsType;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

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
            return SoaUtil.checkHL7((Named) dataObj);
        }
        if (dataObj instanceof BpelMapperPredicate) {
            return ((BpelMapperPredicate)dataObj).getDisplayName();
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
        if (dataObj instanceof BpelMapperTypeCast) {
            BpelMapperTypeCast aTypeCast = BpelMapperTypeCast.class.cast(dataObj);
            Object castedObj = aTypeCast.getCastedObject();
            assert castedObj != null;
            String castedObjName = getDisplayByDataObj(castedObj);
            String result = "(" + aTypeCast.getType().getName() + ")" + castedObjName;
            return result;
        }
        //
        if (dataObj instanceof XPathPseudoComp) {
            return BpelMapperPseudoComp.getDisplayName((XPathPseudoComp)dataObj);
        }
        
        if (dataObj instanceof PropertiesNode) {
            return PropertiesNode.getDisplayName();
        }
        
        if (dataObj instanceof CorrelationProperty) {
            return ((CorrelationProperty) dataObj).getName();
        }
        
        if (dataObj instanceof FileObject) {
            return PropertiesUtils.getDisplayName((FileObject) dataObj);
        }
        
        if (dataObj instanceof NMProperty) {
            String displayName = ((NMProperty) dataObj).getDisplayName();
            if (displayName != null) {
                displayName = displayName.trim();
                if (displayName.length() > 0) {
                    return displayName;
                }
            }
            
            String nmPropertyName = ((NMProperty) dataObj).getNMProperty();
            if (nmPropertyName != null) {
                nmPropertyName = nmPropertyName.trim();
                if (nmPropertyName.length() == 0) {
                    nmPropertyName = null;
                }
            }
            return nmPropertyName;
        }
        //
        if (dataObj instanceof XPathSpecialStep) {
            return ((XPathSpecialStep)dataObj).getType().getDisplayName();
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
        
        if (dataObj instanceof CorrelationProperty) {
            return NodeType.CORRELATION_PROPERTY.getIcon();
        }
        
        if (dataObj instanceof FileObject) {
            FileObject fileObject = (FileObject) dataObj;
            if (fileObject.isFolder()) {
                Object iconURLAttr = fileObject.getAttribute(
                        PropertiesConstants.ICON_ATTR);
                if (iconURLAttr instanceof URL) {
                    return new ImageIcon((URL) iconURLAttr);
                }
            } else {
                return PropertiesConstants.FO_PROPERTY_ICON;
            }
        }
        
        if (dataObj instanceof NMProperty) {
            return PropertiesConstants.NM_PROPERTY_ICON;
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
        if (dataObj instanceof BpelMapperPredicate) {
            SchemaCompHolder sCompHolder = 
                    ((BpelMapperPredicate)dataObj).getSCompHolder(true);
            return getIconByDataObj(sCompHolder.getHeldComponent());
        }
        //
        if (dataObj instanceof BpelMapperTypeCast) {
            Object castableObject = ((BpelMapperTypeCast)dataObj).getCastedObject();
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
        if (dataObj instanceof XPathSpecialStep) {
            // Not defined yet
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
        if (dataObj instanceof PropertiesNode) 
        {
            if (((PropertiesNode) dataObj).isMessageTypeVariable()) {
                Action addNMPropertyAction = new AddNMPropertyAction(
                        mapperTcContext, inLeftTree, treePath, treeItem);
                Action addPropertyAction = new AddPropertyAction(
                        mapperTcContext, inLeftTree, treePath, treeItem);
                result.add(addNMPropertyAction);
                result.add(addPropertyAction);
            }
            isProcessed = true;
        } else if (dataObj instanceof CorrelationProperty) {
            if (DeletePropertyAction
                    .isApplicable((CorrelationProperty) dataObj)) 
            {
                Action deleteAction = new DeletePropertyAction(
                        mapperTcContext, inLeftTree, treePath, treeItem);
                result.add(deleteAction);
                isProcessed = true;
            }
        } else if (dataObj instanceof NMProperty) {
            Action editAction = new EditNMPropertyAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            Action deleteAction = new DeleteNMPropertyAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(editAction);
            result.add(deleteAction);
            isProcessed = true;
        } else if (dataObj instanceof SchemaComponent) {
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
        } else if (dataObj instanceof BpelMapperPredicate) {
            Action action = new EditPredicateAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(action);
            //
            action = new DeletePredicateAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(action);
            isProcessed = true;
        } else if (dataObj instanceof BpelMapperTypeCast) {
            Action action = new DeleteCastAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(action);
            //
            Object castableObject = ((BpelMapperTypeCast)dataObj).getCastedObject();
            populateActionsList(castableObject, result, 
                    mapperTcContext, inLeftTree, treePath, 
                    treeItem);

            
            isProcessed = true;
        } else if (dataObj instanceof BpelMapperPseudoComp) {
            Action action = new DeletePseudoCompAction(
                    mapperTcContext, inLeftTree, treePath, treeItem);
            result.add(action);
            //
            GlobalType pseudoType = ((BpelMapperPseudoComp)dataObj).getType();
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
        MapperStaticContext stContext = mapperContext.getStContext();
        boolean inLeftTree = mapperContext.inLeftTree();
        Mapper mapper = stContext.getMapper();
        XPathDesignContext designContext = stContext.
                getDesignContextController().getContext();
        if (mapper == null || designContext == null) {
            return Collections.EMPTY_LIST;
        }
        //
        List<Action> result = new ArrayList<Action>();
        //
        if (!(// dataObj instanceof XPathSpecialStep ||
                dataObj instanceof BpelMapperPredicate ||
                dataObj instanceof BpelMapperTypeCast ||
                dataObj instanceof BpelMapperPseudoComp)) {
            GlobalType gType = BpelMapperUtils.getGlobalType(dataObj);
            if (gType != null) {
                Action action = new AddCastAction(gType, stContext,
                        inLeftTree, treePath, treeItem);
                result.add(action);
            }
        }
        //
        if  (dataObj instanceof AnyElement) {
            AnyElement anyElement = (AnyElement)dataObj;
            Action action = new AddPseudoCompAction(anyElement, stContext,
                    inLeftTree, treePath, treeItem);
            result.add(action);
        }
        //
        if  (dataObj instanceof AnyAttribute) {
            AnyAttribute anyAttr = (AnyAttribute)dataObj;
            Action action = new AddPseudoCompAction(anyAttr, stContext,
                    inLeftTree, treePath, treeItem);
            result.add(action);
        }
        //
        MapperTcContext tcContext = MapperTcContext.class.cast(stContext);
        populateActionsList(dataObj, result, tcContext, inLeftTree,
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
        //
//        action = new AddSpecialStepAction(SsType.ALL_ELEMENTS,
//                mapperTcContext, inLeftTree, treePath, treeItem);
//        result.add(action);
//        //
//        action = new AddSpecialStepAction(SsType.ALL_ATTRIBUTES,
//                mapperTcContext, inLeftTree, treePath, treeItem);
//        result.add(action);
        //
        action = new AddSpecialStepAction(SsType.NODE,
                mapperTcContext, inLeftTree, treePath, treeItem);
        result.add(action);
        //
        action = new AddSpecialStepAction(SsType.TEXT,
                mapperTcContext, inLeftTree, treePath, treeItem);
        result.add(action);
        //
        action = new AddSpecialStepAction(SsType.COMMENT,
                mapperTcContext, inLeftTree, treePath, treeItem);
        result.add(action);
        //
        action = new AddSpecialStepAction(SsType.PROCESSING_INSTR,
                mapperTcContext, inLeftTree, treePath, treeItem);
        //
        result.add(action);
    }

    public String getToolTipText(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        String name = getDisplayByDataObj(dataObj);
        return getToolTipTextByDataObj(dataObj, name);
    }

    private String getToolTipTextByDataObj(Object dataObj, String name) {
        if (dataObj instanceof SchemaComponent) {
            String result = SchemaTreeInfoProvider.getInstance().
                    getToolTipTextByDataObj(dataObj, name);
            if (result != null) {
                return result;
            }
        }
        
        if (dataObj instanceof FileObject) {
            return PropertiesUtils.getToolTip((FileObject) dataObj);
        }
        
        if (dataObj instanceof NMProperty) {
            return PropertiesUtils.getToolTip((NMProperty) dataObj);
        }
        
        if (dataObj instanceof CorrelationProperty) {
            return PropertiesUtils.getToolTip((CorrelationProperty) dataObj);
        }
        
        if (dataObj instanceof Part) {
            String partLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class, "MESSAGE_PART"); // NOI18N
            
            if (((Part) dataObj).getType() != null) {
                return SchemaTreeInfoProvider.getColorTooltip(partLbl, name, ((Part) dataObj).getType().get());
            }
            if (((Part) dataObj).getElement() != null) {
                return SchemaTreeInfoProvider.getColorTooltip(partLbl, name, getType(((Part) dataObj).getElement().get()));
            }
        }

        if (dataObj instanceof Variable || 
                dataObj instanceof VariableDeclarationWrapper) {
            //
            AbstractVariableDeclaration varDecl = 
                    AbstractVariableDeclaration.class.cast(dataObj);
            String result = getVariableToopTip(varDecl, name);
            if (result != null) {
                return result;
            }
        }
        //
        if (dataObj instanceof BpelMapperTypeCast) {
            CastSchemaContext sContext =
                    BpelMapperTypeCast.class.cast(dataObj).getSchemaContext();
            return getToolTipTextByDataObj(sContext, name);
        }
        //
        if (dataObj instanceof BpelMapperPredicate) {
            PredicatedSchemaContext sContext =
                    BpelMapperPredicate.class.cast(dataObj).getSchemaContext();
            return getToolTipTextByDataObj(sContext, name);
        }
        //
        if (dataObj instanceof BpelMapperPseudoComp) {
            BpelMapperPseudoComp pseudo = (BpelMapperPseudoComp)dataObj;
            //
            String title = null;
            if (pseudo.isAttribute()) {
                title = NbBundle.getMessage(
                        VariableTreeInfoProvider.class, "PSEUDO_ATTRIBUTE"); // NOI18N
            } else {
                title = NbBundle.getMessage(
                        VariableTreeInfoProvider.class, "PSEUDO_ELEMENT"); // NOI18N
            }
            //
            String body = SchemaTreeInfoProvider.getColorTooltip(title, pseudo.getName(), 
                    pseudo.getType().getName(), pseudo.getNamespace());
            //
            return body;
        }
        //
        if (dataObj instanceof CastSchemaContext) {
            CastSchemaContext sContext = CastSchemaContext.class.cast(dataObj);
            XPathSchemaContext baseSContext = sContext.getBaseContext();
            name = baseSContext.toStringWithoutParent();
            String baseTooltip = getToolTipTextByDataObj(baseSContext, name);
            //
            GlobalType gType = sContext.getTypeCast().getType();
            String castedToLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class, "CASTED_TO"); // NOI18N
            return baseTooltip + "<hr>&nbsp;<b>" + castedToLbl +
                    ":</b>&nbsp;" + gType.getName() + "&nbsp;";
        }
        //
        if (dataObj instanceof PredicatedSchemaContext) {
            PredicatedSchemaContext sContext =
                    PredicatedSchemaContext.class.cast(dataObj);
            XPathSchemaContext baseSContext = sContext.getBaseContext();
            name = baseSContext.toStringWithoutParent();
            String baseTooltip = getToolTipTextByDataObj(baseSContext, name);
            //
            String withPredicateLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class, "WITH_PREDICATE"); // NOI18N
            return baseTooltip + "<hr>&nbsp;<b>" + withPredicateLbl +
                    ":</b>&nbsp;" + sContext.getPredicatesString(null) + "&nbsp;";
        }
        //
        if (dataObj instanceof VariableSchemaContext) {
            XPathVariable xPathVar =
                    VariableSchemaContext.class.cast(dataObj).getVariable();
            assert xPathVar instanceof XPathBpelVariable;
            XPathBpelVariable bpelXPathVar = XPathBpelVariable.class.cast(xPathVar);
            Part part = bpelXPathVar.getPart();
            if (part != null) {
                name = part.getName();
                return getToolTipTextByDataObj(part, name);
            } else {
                VariableDeclaration var = bpelXPathVar.getVarDecl();
                //
                AbstractVariableDeclaration varDecl =
                        AbstractVariableDeclaration.class.cast(var);
                name = varDecl.getVariableName();
                String result = getVariableToopTip(varDecl, name);
                if (result != null) {
                    return result;
                }
            }
        }
        //
        if (dataObj instanceof XPathSchemaContext) {
            SchemaCompHolder sCompHolder =
                    XPathSchemaContext.Utilities.getSchemaCompHolder(
                    XPathSchemaContext.class.cast(dataObj), true);
            if (sCompHolder != null) {
                return getToolTipTextByDataObj(sCompHolder.getHeldComponent(), name);
            }
        }
        //
        return null;
    }

    private SchemaComponent getType(SchemaComponent component) {
        if ( !(component instanceof TypeContainer)) {
            return component;
        }
        TypeContainer typeContainer = (TypeContainer) component;
        NamedComponentReference<? extends GlobalType> ref = typeContainer.getType();

        if (ref != null && ref.get() != null) {
            return ref.get();
        }
        return component;
    }

    private String getVariableToopTip(AbstractVariableDeclaration varDecl, String name) {
        if (varDecl.getMessageType() != null) {
            String varLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class,
                    "MESSAGE_VARIABLE"); // NOI18N
            WSDLReference<Message> msgRef = varDecl.getMessageType();
            String msgNamespace = null;
            Message msg = msgRef.get();
            if (msg != null) {
                WSDLModel wsdlModel = msg.getModel();
                if (wsdlModel != null) {
                    msgNamespace = wsdlModel.getDefinitions().getTargetNamespace();
                }
            }
            return SchemaTreeInfoProvider.getColorTooltip(
                    varLbl, name, msgRef.getRefString(),
                    msgNamespace);
        }
        //
        if (varDecl.getType() != null) {
            String varLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class,
                    "TYPE_VARIABLE"); // NOI18N
            return SchemaTreeInfoProvider.getColorTooltip(
                    varLbl, name, varDecl.getType().get());
        }
        //
        if (varDecl.getElement() != null) {
            String varLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class,
                    "ELEMENT_VARIABLE"); // NOI18N
            return SchemaTreeInfoProvider.getColorTooltip(
                    varLbl, name, varDecl.getElement().get());
        }
        //
        return null;
    }

}
