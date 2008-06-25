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
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.mapper.cast.AbstractTypeCast;
import org.netbeans.modules.bpel.mapper.predicates.AbstractPredicate;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.CastSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SimpleSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.VariableSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathPseudoComp;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * The auxiliary class to convert tree path or path iterator to other forms.
 * 
 * @author nk160297
 */
public class PathConverter {

    private static enum ParsingStage {
        SCHEMA, PART, VARIABLE;
    };

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
    public static List<Object> constructObjectLocationtList(
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
            if (obj instanceof SchemaComponent || obj instanceof XPathPseudoComp) {
                if (!(stage == null || stage == ParsingStage.SCHEMA)) {
                    return null;
                }
                stage = ParsingStage.SCHEMA;
                toAdd = obj;
            } else if (stage != null && obj instanceof AbstractPredicate) {
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
                    treeItemList.addFirst(toAdd); 
                } else {
                    treeItemList.addLast(toAdd);
                }
            }
            //
            if (needBreak) {
                break;
            }
        }
        //
        return treeItemList;
    }

    public static XPathBpelVariable constructXPathBpelVariable(
            List<Object> pathList) {
        //
        AbstractVariableDeclaration var = null;
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
                var = (AbstractVariableDeclaration)obj;
                stage = ParsingStage.VARIABLE;
                //
                // Everything found!
                break;
            } else {
                return null;
            }
        }
        if (var == null) {
            return null;
        }
        //
        return new XPathBpelVariable(var, part);
    }

    public static List<Object> constructObjectLocationtList(
            XPathExpression exprPath) {
        //
        ArrayList<Object> treeItemList = new ArrayList<Object>();
        //
        if (exprPath instanceof AbstractLocationPath) {
            LocationStep[] stepArr = ((AbstractLocationPath)exprPath).getSteps();
            for (int index = stepArr.length - 1; index >= 0; index--) {
                LocationStep step = stepArr[index];
                XPathSchemaContext sContext = step.getSchemaContext();
                if (sContext != null) {
                    SchemaComponent sComp = XPathSchemaContext.Utilities.
                            getSchemaComp(sContext);
                    if (sComp != null) {
                        treeItemList.add(sComp);
                        continue;
                    }
                }
                //
                // Unresolved step --> the location list can't be built
                return null;
            }
        }
        //
        XPathVariableReference varRefExpr = null;
        if (exprPath instanceof XPathExpressionPath) {
            XPathExpression expr = ((XPathExpressionPath)exprPath).getRootExpression();
            if (expr instanceof XPathVariableReference) {
                varRefExpr = (XPathVariableReference)expr;
            }
        } else if (exprPath instanceof XPathVariableReference) {
            varRefExpr = (XPathVariableReference)exprPath;
        }
        //
        if (varRefExpr != null) {
            XPathVariable var = varRefExpr.getVariable();
            assert var instanceof XPathBpelVariable;
            XPathBpelVariable bpelVar = (XPathBpelVariable)var;
            //
            Part part = bpelVar.getPart();
            if (part != null) {
                treeItemList.add(part);
            } 
            //
            AbstractVariableDeclaration varDecl = bpelVar.getVarDecl();
            if (varDecl != null) {
                treeItemList.add(varDecl);
            }
        }
        //
        return treeItemList;
    }
    
    public static XPathExpression constructXPath(BpelEntity base, 
            TreeItem treeItem, boolean skipFirst) {
        //
        // It's necessary to have the order, oposite to the iterator. 
        // It's required for correct buildeing the XPath expression
        List<Object> objList = constructObjectLocationtList(treeItem, false, skipFirst);
        //
        if (objList == null || objList.isEmpty()) {
            return null;
        }
        //
        XPathModel xPathModel = BpelXPathModelFactory.create(base); 
        XPathModelFactory factory = xPathModel.getFactory();
        //
        VariableDeclaration varDecl = null;
        Part part = null;
        LinkedList<LocationStep> stepList = new LinkedList<LocationStep>();
        SchemaModelsStack sms = new SchemaModelsStack();
        //
        // Process the path
        for (Object stepObj : objList) {
            SchemaComponent sComp = null;
            if (stepObj instanceof SchemaComponent) {
                sComp = (SchemaComponent)stepObj;
                LocationStep ls = constructLStep(xPathModel, sComp, null, sms);
                stepList.add(0, ls);
            } else if (stepObj instanceof AbstractPredicate) {
                AbstractPredicate pred = (AbstractPredicate)stepObj;
                XPathPredicateExpression[] predArr = pred.getPredicates();
                SchemaCompHolder sCompHolder = pred.getSCompHolder();
                sComp = sCompHolder.getSchemaComponent();
                LocationStep ls = constructLStep(xPathModel, sCompHolder, predArr, sms);
                stepList.add(0, ls);
            } else if (stepObj instanceof AbstractTypeCast) {
                AbstractTypeCast typeCast = (AbstractTypeCast)stepObj;
                sComp = typeCast.getSComponent();
                LocationStep ls = constructLStep(xPathModel, sComp, null, sms);
                //
                stepList.add(0, ls);
            } else if (stepObj instanceof LocationStep) {
                //
                // TODO: It would be more correct to do a copy of the stepObj
                // because of it is owned by another XPathModel. 
                LocationStep ls = (LocationStep)stepObj;
                stepList.add(0, ls);
                //
                XPathSchemaContext sContext = ls.getSchemaContext();
                if (sContext != null) {
                    sComp = XPathSchemaContext.Utilities.getSchemaComp(sContext);
                }
            } else if (stepObj instanceof XPathPseudoComp) {
                XPathPseudoComp pseudo = (XPathPseudoComp)stepObj;
                LocationStep ls = constructLStep(xPathModel, pseudo, null, sms);
                stepList.add(0, ls);
            } else if (stepObj instanceof Part) {
                part = (Part)stepObj;
            } else if (stepObj instanceof AbstractVariableDeclaration) {
                AbstractVariableDeclaration var = (AbstractVariableDeclaration)stepObj;
                //
                if (var instanceof VariableDeclaration) {
                    varDecl = (VariableDeclaration)var;
                } else if (var instanceof VariableDeclarationWrapper) {
                    varDecl = ((VariableDeclarationWrapper)var).getDelegate();
                }
                //
                if (varDecl == null) {
                    return null;
                }
            } else {
                break;
            }
            //
            if (sComp != null) {
                sms.appendSchemaComponent(sComp);
            } else {
                sms.discard();
            }
        }
        //
        XPathBpelVariable xPathVar = new XPathBpelVariable(varDecl, part);
        QName varQName = xPathVar.constructXPathName();
        XPathVariableReference xPathVarRef = 
                xPathModel.getFactory().newXPathVariableReference(varQName);
        //
        if (stepList.isEmpty()) {
            return xPathVarRef;
        } else {
            LocationStep[] steps = stepList.toArray(new LocationStep[stepList.size()]);
            XPathExpressionPath result = factory.newXPathExpressionPath(xPathVarRef, steps);
            return result;
        } 
    }
    
    //--------------------------------------------------------------
    
    public static List<LocationStep> constructLSteps(XPathModel xPathModel, 
            List<Object> sCompList, Set<AbstractTypeCast> typeCastCollector, 
            Set<XPathPseudoComp> pseudoCollector, SchemaModelsStack sms) {
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
            } else if (stepObj instanceof AbstractPredicate) {
                AbstractPredicate pred = (AbstractPredicate)stepObj;
                XPathPredicateExpression[] predArr = pred.getPredicates();
                SchemaCompHolder sCompHolder = pred.getSCompHolder();
                sComp = sCompHolder.getSchemaComponent();
                newLocationStep = constructLStep(xPathModel, sCompHolder, predArr, sms);
            } else if (stepObj instanceof AbstractTypeCast) {
                AbstractTypeCast typeCast = (AbstractTypeCast)stepObj;
                sComp = typeCast.getSComponent();
                newLocationStep = constructLStep(xPathModel, sComp, null, sms);
                //
                if (typeCastCollector != null) {
                    typeCastCollector.add(typeCast);
                }
            } else if (stepObj instanceof LocationStep) {
                //
                // TODO: It would be more correct to do a copy of the stepObj
                // because of it is owned by another XPathModel. 
                newLocationStep = (LocationStep)stepObj;
                if (newLocationStep != null) {
                    XPathSchemaContext sContext = newLocationStep.getSchemaContext();
                    if (sContext != null) {
                        sComp = XPathSchemaContext.Utilities.getSchemaComp(sContext);
                    }
                }
            } else if (stepObj instanceof XPathPseudoComp) {
                XPathPseudoComp pseudo = (XPathPseudoComp)stepObj;
                newLocationStep = constructLStep(xPathModel, pseudo, null, sms);
                //
                if (pseudoCollector != null) {
                    pseudoCollector.add(pseudo);
                }
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
            StepNodeNameTest nameTest = new StepNodeNameTest(xPathModel, sCompHolder, sms);
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
        StepNodeNameTest nameTest = new StepNodeNameTest(xPathModel, sComp, sms);
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
        StepNodeNameTest nameTest = new StepNodeNameTest(xPathModel, pseudo, sms);
        LocationStep newLocationStep = xPathModel.getFactory().
                newLocationStep(axis, nameTest, predArr);
        //
        return newLocationStep;
    }

    //--------------------------------------------------------------
    
    public static String toString(Iterable<Object> pathItrb) {
        LinkedList<Object> list = new LinkedList<Object>();
        Iterator itr = pathItrb.iterator();
        while (itr.hasNext()) {
            list.addFirst(itr.next());
        }
        //
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Object obj : list) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("/"); // NOI18N
            }
            sb.append(obj.toString());
        }
        //
        return sb.toString();
    }

    /**
     * Builds the new Schema context from the location on the tree. 
     * @param pathItrb specifies the location
     * @param skipFirstItem indicates if it necessary to ignore 
     * the first location item. It is used to get context of the parent tree item.
     * @return
     */
    public static XPathSchemaContext constructContext(
            TreeItem treeItem, boolean skipFirstItem) {
        //
        SchemaContextBuilder builder = new SchemaContextBuilder();
        return builder.constructContext(treeItem, null, skipFirstItem);
    }
    
    public static class SchemaContextBuilder {
        Part part = null;
        AbstractVariableDeclaration var = null;
        
        /**
         * Builds an XPathSchemaContext by a RestartableIterator. 
         * It is implied that the RestartableIterator provides a collection of 
         * tree items' data objects in order from leafs to the tree root.
         * 
         * @param pathItr
         * @return
         */
        public XPathSchemaContext constructContext(TreeItem treeItem, 
                XPathSchemaContext initialContext, boolean skipFirst) {
            //
            Iterator itr = treeItem.iterator();
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
                varDecl = ((VariableDeclarationWrapper)var).getDelegate();
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
            } else if (obj instanceof XPathPseudoComp) {
                return new SimpleSchemaContext(
                        constructContextImpl(pathItr, baseContext), 
                        (XPathPseudoComp)obj);
            } else if (obj instanceof AbstractPredicate) {
                return new SimpleSchemaContext(
                        constructContextImpl(pathItr, baseContext), 
                        ((AbstractPredicate)obj).getSCompHolder());
            } else if (obj instanceof AbstractVariableDeclaration) {
                var = (AbstractVariableDeclaration)obj;
                return buildVariableSchemaContext();
            } else if (obj instanceof Part) {
                part = (Part)obj;
                return constructContextImpl(pathItr, baseContext);
            } else if (obj instanceof AbstractTypeCast) {
                AbstractTypeCast typeCast = (AbstractTypeCast)obj;
                Object castedObj = typeCast.getCastedObject();
                XPathSchemaContext castedContext = constructContext(
                        castedObj, pathItr, 
                        constructContextImpl(pathItr, baseContext));
                return new CastSchemaContext(castedContext, typeCast);
            } else if (obj instanceof XPathPseudoComp) {
                XPathPseudoComp pseudo = (XPathPseudoComp)obj;
                return new SimpleSchemaContext(
                        constructContextImpl(pathItr, baseContext), pseudo);
            } else {
                return baseContext;
            }
        }
        
    }
    
}
