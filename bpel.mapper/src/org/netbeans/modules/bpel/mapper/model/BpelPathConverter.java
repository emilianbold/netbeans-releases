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

package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.bpel.mapper.cast.BpelMapperTypeCast;
import org.netbeans.modules.bpel.mapper.predicates.BpelMapperPredicate;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.soa.xpath.mapper.tree.PathConverter;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SimpleSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SpecialSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.WildcardSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.netbeans.modules.xml.xpath.ext.spi.XPathSpecialStep;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * The auxiliary class to convert tree path or path iterator to other forms.
 * 
 * @author nk160297
 */
public class BpelPathConverter implements PathConverter {

    private static enum ParsingStage {
        SCHEMA, PART, VARIABLE;
    };

    private static BpelPathConverter mSingleton = new BpelPathConverter();

    public static BpelPathConverter singleton() {
        return mSingleton;
    }

    /**
     * Constructs a new list, which contains the schema elements, predicates, 
     * special steps, cast objects, part and variable from the specified iterator pathItr. 
     * The first object taken from iterator will be at the beginning of the list 
     * if the parameter sameOrder is true. The order will be oposite otherwise.
     * If the iterator has incompatible content then the null is returned. 
     * 
     * @param pathItr
     * @return
     */
    public DirectedList<Object> constructObjectLocationList(
            TreeItem treeItem, boolean sameOrder, boolean skipFirst) {
        //
        Iterator<Object> itr = treeItem.iterator();
        if (skipFirst && itr.hasNext()) {
            // move forward one step if possible
            itr.next();
        }
        //
        LinkedList<Object> treeItemList = new LinkedList<Object>();
        //
        // Process the path
        ParsingStage stage = null;
        boolean needBreak = false;
        while (itr.hasNext()) {
            Object obj = itr.next();
            Object toAdd = null;
            if (obj instanceof SchemaComponent
                    || obj instanceof XPathPseudoComp
                    || obj instanceof BpelMapperTypeCast
                    || obj instanceof BpelMapperPredicate
                    || obj instanceof XPathSpecialStep) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.SCHEMA;
                toAdd = obj;
            } else if (obj instanceof Part) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.PART;
                toAdd = obj;
            } else if (obj instanceof AbstractVariableDeclaration) {
                if (!(stage == null || 
                        stage == ParsingStage.SCHEMA || 
                        stage == ParsingStage.PART)) {
                    return null;
                }
                //
                AbstractVariableDeclaration var = (AbstractVariableDeclaration)obj;
                //
                VariableDeclaration varDecl = null;
                if (var instanceof VariableDeclaration) {
                    varDecl = (VariableDeclaration)var;
                } else if (var instanceof VariableDeclarationWrapper) {
                    varDecl = ((VariableDeclarationWrapper)var).getDelegate();
                }
                //
                if (varDecl == null) {
                    return null;
                }
                //
                stage = ParsingStage.VARIABLE;
                toAdd = varDecl;
                //
                // Everything found!
                needBreak = true;
            } else {
                if (stage == null) {
                    return null;
                }
                needBreak = true;
            }
            //
            if (toAdd == null) {
                needBreak = true;
            } else {
                if (sameOrder) {
                    treeItemList.addLast(toAdd);
                } else {
                    treeItemList.addFirst(toAdd);
                }
            }
            //
            if (needBreak) {
                break;
            }
        }
        //
        DirectedList<Object> result = new DirectedList<Object>(treeItemList, sameOrder);
        return result;
    }

    public XPathBpelVariable constructXPathVariable(
            DirectedList<Object> pathList) {
        //
        VariableDeclaration varDecl = null;
        Part part = null;
        //
        // Process the path
        ParsingStage stage = null;
        for (Object obj: pathList) {
            if (obj instanceof Part) {
                if (stage != null) {
                    return null;
                }
                stage = ParsingStage.PART;
                part = (Part)obj;
            } else if (obj instanceof AbstractVariableDeclaration) {
                if (!(stage == null || stage == ParsingStage.PART)) {
                    return null;
                }
                //
                if (obj instanceof VariableDeclaration) {
                    varDecl = (VariableDeclaration)obj;
                } else if (obj instanceof VariableDeclarationWrapper) {
                    varDecl = VariableDeclarationWrapper.class.cast(obj).getDelegate();
                }
                stage = ParsingStage.VARIABLE;
                //
                // Everything found!
                break;
            } else {
                return null;
            }
        }
        if (varDecl == null) {
            return null;
        }
        //
        return new XPathBpelVariable(varDecl, part);
    }

    public DirectedList<Object> constructObjectLocationList(
            XPathSchemaContext schemaContext, boolean sameOrder, boolean skipFirst) {
        //
        if (schemaContext == null) {
            return DirectedList.empty();
        }
        //
        if (skipFirst) {
            // move forward one step if possible
            schemaContext = schemaContext.getParentContext();
        }
        //
        LinkedList<Object> list = new LinkedList<Object>();
        //
        XPathSchemaContext context = schemaContext;
        while (context != null) { 
            if (context instanceof VariableSchemaContext) {
                XPathVariable var = ((VariableSchemaContext)context).getVariable();
                assert var instanceof XPathBpelVariable;
                XPathBpelVariable bpelVar = (XPathBpelVariable)var;
                //
                Part part = bpelVar.getPart();
                if (part != null) {
                    add(part, list, sameOrder);
                }
                //
                AbstractVariableDeclaration varDecl = bpelVar.getVarDecl();
                add(varDecl, list, sameOrder);
            } else if (context instanceof CastSchemaContext) {
                CastSchemaContext castContext = (CastSchemaContext)context;
                //
                BpelMapperTypeCast typeCast = new BpelMapperTypeCast(castContext.getTypeCast());
                add(typeCast, list, sameOrder);
                //
                // If a variable with part is casted then the variable should
                // be added to result list additionaly because it is presented
                // as a separate node in the tree. 
                XPathSchemaContext baseContext = castContext.getBaseContext();
                if (baseContext instanceof VariableSchemaContext) {
                    XPathVariable var = 
                            ((VariableSchemaContext)baseContext).getVariable();
                    assert var instanceof XPathBpelVariable;
                    XPathBpelVariable bpelVar = (XPathBpelVariable)var;
                    //
                    Part part = bpelVar.getPart();
                    if (part != null) {
                        AbstractVariableDeclaration varDecl = bpelVar.getVarDecl();
                        add(varDecl, list, sameOrder);
                    }
                }
            } else if (context instanceof PredicatedSchemaContext) {
                PredicatedSchemaContext predContext = (PredicatedSchemaContext)context;
                
                BpelMapperPredicate predicate = new BpelMapperPredicate(predContext);
                add(predicate, list, sameOrder);
            } else if (context instanceof WildcardSchemaContext) {
                WildcardSchemaContext wldContext = (WildcardSchemaContext)context;
                XPathSpecialStep sStep = wldContext.getSpecialStep();
                if (sStep == null) {
                    return DirectedList.empty();
                }
                add(sStep, list, sameOrder);
            } else if (context instanceof SpecialSchemaContext) {
                SpecialSchemaContext specContext = (SpecialSchemaContext)context;
                XPathSpecialStep sStep = specContext.getSpecialStep();
                if (sStep == null) {
                    return DirectedList.empty();
                }
                add(sStep, list, sameOrder);
            } else {
                SchemaCompHolder sCompHolder = XPathSchemaContext.Utilities.
                        getSchemaCompHolder(context, false);
                if (sCompHolder == null) {
                    return DirectedList.empty();
                }
                add(sCompHolder.getHeldComponent(), list, sameOrder);
            }
            //
            context = context.getParentContext();
        }
        //
        DirectedList<Object> result = new DirectedList<Object>(list, sameOrder);
        return result;
    }

    //--------------------------------------------------------------
    
    public static List<LocationStep> constructLSteps(XPathModel xPathModel, 
            List<Object> sCompList, SchemaModelsStack sms) {
        if (sCompList == null || sCompList.isEmpty()) {
            return null;
        } 
        //
        ArrayList<LocationStep> result = new ArrayList<LocationStep>();
        SchemaComponent sComp = null;
        //
        for (Object stepObj : sCompList) {
            LocationStep newLocationStep = null;
            if (stepObj instanceof SchemaComponent) {
                sComp = (SchemaComponent)stepObj;
                newLocationStep = constructLStep(xPathModel, sComp, null, sms);
            } else if (stepObj instanceof BpelMapperPredicate) {
                BpelMapperPredicate pred = (BpelMapperPredicate)stepObj;
                XPathPredicateExpression[] predArr = pred.getPredicates();
                SchemaCompHolder sCompHolder = pred.getSCompHolder(true);
                sComp = sCompHolder.getSchemaComponent();
                newLocationStep = constructLStep(xPathModel, sCompHolder, predArr, sms);
            } else if (stepObj instanceof BpelMapperTypeCast) {
                BpelMapperTypeCast typeCast = (BpelMapperTypeCast)stepObj;
                sComp = typeCast.getSComponent();
                newLocationStep = constructLStep(xPathModel, sComp, null, sms);
            } else if (stepObj instanceof XPathSpecialStep) {
                XPathSpecialStep sStep = (XPathSpecialStep)stepObj;
                newLocationStep = XPathSpecialStep.Utils.
                        constructLocationStep(xPathModel, sStep);
            } else if (stepObj instanceof XPathPseudoComp) {
                XPathPseudoComp pseudo = (XPathPseudoComp)stepObj;
                newLocationStep = constructLStep(xPathModel, pseudo, null, sms);
            }
            //
            if (sComp != null) {
                sms.appendSchemaComponent(sComp);
            } else {
                sms.discard();
            }
            //
            if (newLocationStep != null) {
                result.add(newLocationStep);
            }
        }
        //
        return result;
    } 
    
    /**
     * Constructs a LocationStep object by the schema component. 
     * @param xPathModel
     * @param sCompHolder
     * @return
     */
    public static LocationStep constructLStep(XPathModel xPathModel, 
            SchemaCompHolder sCompHolder, XPathPredicateExpression[] predArr, 
            SchemaModelsStack sms) {
        //
        if (sCompHolder.isPseudoComp()) {
            XPathAxis axis = null;
            switch (sCompHolder.getComponentType()) {
                case ATTRIBUTE:
                case PSEUDO_ATTRIBUTE:
                    axis = XPathAxis.ATTRIBUTE;
                    break;
                case ELEMENT:
                case PSEUDO_ELEMENT:
                case TYPE:
                    axis = XPathAxis.CHILD;
                    break;
            }
            //
            StepNodeNameTest nameTest = new StepNodeNameTest(
                    xPathModel.getNamespaceContext(), sCompHolder, sms);
            LocationStep newLocationStep = xPathModel.getFactory().
                    newLocationStep(axis, nameTest, predArr);
            //
            return newLocationStep;
        } else {
            SchemaComponent sComp = (SchemaComponent)sCompHolder.getHeldComponent();
            return constructLStep(xPathModel, sComp, predArr, sms);
        }
    }

    /**
     * Constructs a LocationStep object by the schema component. 
     * @param xPathModel
     * @param sComp
     * @return
     */
    public static LocationStep constructLStep(XPathModel xPathModel, 
            SchemaComponent sComp, XPathPredicateExpression[] predArr, 
            SchemaModelsStack sms) {
        //
        if (!(sComp instanceof Named)) {
            return null;
        }
        //
        XPathAxis axis = null;
        if (sComp instanceof Attribute) {
            axis = XPathAxis.ATTRIBUTE;
        } else {
            axis = XPathAxis.CHILD;
        }
        //
        StepNodeNameTest nameTest = new StepNodeNameTest(
                xPathModel.getNamespaceContext(), sComp, sms);
        LocationStep newLocationStep = xPathModel.getFactory().
                newLocationStep(axis, nameTest, predArr);
        //
        return newLocationStep;
    }

    /**
     * Constructs a LocationStep object by the Pseudo schema component. 
     * @param xPathModel
     * @param pseudo
     * @return
     */
    public static LocationStep constructLStep(XPathModel xPathModel, 
            XPathPseudoComp pseudo, XPathPredicateExpression[] predArr, 
            SchemaModelsStack sms) {
        //
        XPathAxis axis = null;
        if (pseudo.isAttribute()) {
            axis = XPathAxis.ATTRIBUTE;
        } else {
            axis = XPathAxis.CHILD;
        }
        //
        StepNodeNameTest nameTest = new StepNodeNameTest(
                xPathModel.getNamespaceContext(), pseudo, sms);
        LocationStep newLocationStep = xPathModel.getFactory().
                newLocationStep(axis, nameTest, predArr);
        //
        return newLocationStep;
    }

    //--------------------------------------------------------------
    
    /**
     * Builds the new Schema context from the location on the tree. 
     * @param pathItrb specifies the location
     * @param skipFirstItem indicates if it necessary to ignore 
     * the first location item. It is used to get context of the parent tree item.
     * @return
     */
    public XPathSchemaContext constructContext(
            TreeItem treeItem, boolean skipFirstItem) {
        //
        SchemaContextBuilder builder = new SchemaContextBuilder();
        return builder.constructContext(treeItem, null, skipFirstItem);
    }
    
    public static class SchemaContextBuilder {
        Part part = null;
        AbstractVariableDeclaration var = null;
        
        /**
         * Builds an XPathSchemaContext by a TreeItem, which is Iterable.
         * The iterator provides a sequence of data objects ordered from
         * leafs to the tree root. 
         * 
         * ATTENTION! Method works recursively because the chain of result
         * Schema Context implies backward order (in comparison with the order
         * provided by the iterator).
         * 
         * @param pathItr
         * @return
         */
        public XPathSchemaContext constructContext(TreeItem treeItem, 
                XPathSchemaContext initialContext, boolean skipFirst) {
            //
            Iterator<Object> itr = treeItem.iterator();
            //
            if (skipFirst) {
                itr.next();
            }
            //
            return constructContextImpl(itr, initialContext);
        }

        private VariableSchemaContext buildVariableSchemaContext() {
            VariableDeclaration varDecl = null;
            if (var instanceof VariableDeclaration) {
                varDecl = (VariableDeclaration)var;
            } else if (var instanceof VariableDeclarationWrapper) {
                varDecl = VariableDeclarationWrapper.class.cast(var).getDelegate();
            }
            XPathBpelVariable xPathVariable = new XPathBpelVariable(varDecl, part);
            if (xPathVariable != null) {
                return new VariableSchemaContext(xPathVariable);
            }
            //
            return null;
        }
        
        private XPathSchemaContext constructContextImpl(Iterator<Object> pathItr, 
                XPathSchemaContext baseContext) {
            //
            if (!pathItr.hasNext()) {
                return baseContext;
            }
            //
            Object obj = pathItr.next(); 
            return constructContext(obj, pathItr, baseContext);
        }
        
        private XPathSchemaContext constructContext(Object obj, Iterator<Object> pathItr, 
                XPathSchemaContext baseContext) {
            //
            if (obj instanceof SchemaComponent) {
                return new SimpleSchemaContext(
                        constructContextImpl(pathItr, baseContext), 
                        (SchemaComponent)obj);
            } else if (obj instanceof BpelMapperPredicate) {
                BpelMapperPredicate absPred = (BpelMapperPredicate)obj;
                // 
                // It doesn't need to construct a new context here. 
                // It has to be provided by the predicate. 
                return absPred.getSchemaContext();
            } else if (obj instanceof AbstractVariableDeclaration) {
                var = (AbstractVariableDeclaration)obj;
                return buildVariableSchemaContext();
            } else if (obj instanceof Part) {
                part = (Part)obj;
                return constructContextImpl(pathItr, baseContext);
            } else if (obj instanceof BpelMapperTypeCast) {
                BpelMapperTypeCast typeCast = (BpelMapperTypeCast)obj;
                Object castedObj = typeCast.getCastedObject();
                XPathSchemaContext castedContext = constructContext(
                        castedObj, pathItr, 
                        constructContextImpl(pathItr, baseContext));
                return new CastSchemaContext(castedContext, typeCast);
            } else if (obj instanceof XPathSpecialStep) {
                XPathSpecialStep sStep = (XPathSpecialStep)obj;
                return new SpecialSchemaContext(
                        constructContextImpl(pathItr, baseContext),
                        sStep.getType());
            } else if (obj instanceof XPathPseudoComp) {
                return new SimpleSchemaContext(
                        constructContextImpl(pathItr, baseContext), 
                        (XPathPseudoComp)obj);
            } else {
                return baseContext;
            }
        }
        
    }
    
    //--------------------------------------------------------------

    private void add(Object objToAdd, LinkedList<Object> ll, boolean toEnd) {
        if (toEnd) {
            ll.addLast(objToAdd);
        } else {
            ll.addFirst(objToAdd);
        }
    }

}
