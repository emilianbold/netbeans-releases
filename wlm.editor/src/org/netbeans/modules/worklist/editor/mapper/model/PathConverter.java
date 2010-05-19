/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.xpath.WlmXPathModelFactory;
import org.netbeans.modules.wlm.model.xpath.XPathWlmVariable;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.AbstractLocationPath;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
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
    public static DirectedList<Object> constructObjectLocationList(
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
            if (obj instanceof SchemaComponent) {
//                    || obj instanceof XPathPseudoComp
//                    || obj instanceof AbstractTypeCast
//                    || obj instanceof AbstractPredicate) {
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
            } else if (obj instanceof VariableDeclaration) {
                if (!(stage == null || 
                        stage == ParsingStage.SCHEMA || 
                        stage == ParsingStage.PART)) {
                    return null;
                }
                //
                VariableDeclaration varDecl = (VariableDeclaration)obj;
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
        DirectedList result = new DirectedList(treeItemList, sameOrder);
        result.checkDirection();
        return result;
    }

    public static XPathWlmVariable constructXPathWlmVariable(
            DirectedList<Object> pathList) {
        //
        VariableDeclaration var = null;
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
            } else if (obj instanceof VariableDeclaration) {
                if (!(stage == null || stage == ParsingStage.PART)) {
                    return null;
                }
                //
                var = (VariableDeclaration)obj;
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
        return new XPathWlmVariable(var, part);
    }

    /**
     * This method is used by MapperPredicate
     * @param exprPath
     * @param lastStepIndex
     * @return
     */
    public static DirectedList<Object> constructObjectLocationList(
            AbstractLocationPath exprPath, int lastStepIndex, boolean skipFirst) {
        //
        if (lastStepIndex < 0) {
            return DirectedList.EMPTY;
        }
        //
        LocationStep[] stepArr = ((AbstractLocationPath)exprPath).getSteps();
        if (stepArr.length <= lastStepIndex) {
            return DirectedList.EMPTY;
        }
        //
        LocationStep lastStep = stepArr[lastStepIndex];
        if (lastStep == null) {
            return DirectedList.EMPTY;
        }
        //
        XPathSchemaContext sContext = lastStep.getSchemaContext();
        if (sContext == null) {
            return DirectedList.EMPTY;
        }
        //
        if (skipFirst) {
            sContext = sContext.getParentContext();
        }
        //
        return constructObjectLocationList(sContext, true, false);
    }

    public static DirectedList<Object> constructObjectLocationList(
            XPathSchemaContext schemaContext, boolean sameOrder, boolean skipFirst) {
        //
        if (schemaContext == null) {
            return DirectedList.EMPTY;
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
                assert var instanceof XPathWlmVariable;
                XPathWlmVariable wlmVar = (XPathWlmVariable)var;
                //
                Part part = wlmVar.getPart();
                if (part != null) {
                    add(part, list, sameOrder);
                }
                //
                VariableDeclaration varDecl = wlmVar.getVarDecl();
                add(varDecl, list, sameOrder);
//            } else if (context instanceof CastSchemaContext) {
//                CastSchemaContext castContext = (CastSchemaContext)context;
//                //
//                TypeCast typeCast = new TypeCast(castContext.getTypeCast());
//                add(typeCast, list, sameOrder);
//                //
//                // If a variable with part is casted then the variable should
//                // be added to result list additionaly because it is presented
//                // as a separate node in the tree.
//                XPathSchemaContext baseContext = castContext.getBaseContext();
//                if (baseContext instanceof VariableSchemaContext) {
//                    XPathVariable var =
//                            ((VariableSchemaContext)baseContext).getVariable();
//                    assert var instanceof XPathWlmVariable;
//                    XPathWlmVariable bpelVar = (XPathWlmVariable)var;
//                    //
//                    Part part = bpelVar.getPart();
//                    if (part != null) {
//                        VariableDeclaration varDecl = bpelVar.getVarDecl();
//                        add(varDecl, list, sameOrder);
//                    }
//                }
//            } else if (context instanceof PredicatedSchemaContext) {
//                PredicatedSchemaContext predContext = (PredicatedSchemaContext)context;
//
//                SchemaContextPredicate predicate = new SchemaContextPredicate(predContext);
//                add(predicate, list, sameOrder);
            } else if (context instanceof WildcardSchemaContext) {
                WildcardSchemaContext wldContext = (WildcardSchemaContext)context;
                XPathSpecialStep sStep = wldContext.getSpecialStep();
                if (sStep == null) {
                    return DirectedList.EMPTY;
                }
                add(sStep, list, sameOrder);
            } else if (context instanceof SpecialSchemaContext) {
                SpecialSchemaContext specContext = (SpecialSchemaContext)context;
                XPathSpecialStep sStep = specContext.getSpecialStep();
                if (sStep == null) {
                    return DirectedList.EMPTY;
                }
                add(sStep, list, sameOrder);
            } else {
                SchemaCompHolder sCompHolder = XPathSchemaContext.Utilities.
                        getSchemaCompHolder(context);
                if (sCompHolder == null) {
                    return DirectedList.EMPTY;
                }
                add(sCompHolder.getHeldComponent(), list, sameOrder);
            }
            //
            context = context.getParentContext();
        }
        //
        DirectedList result = new DirectedList(list, sameOrder);
        result.checkDirection();
        return result;
    }

    public static XPathExpression constructXPath(WLMComponent base,
            TreeItem treeItem, boolean skipFirst) {
        //
        // It's necessary to have the order, oposite to the iterator. 
        // It's required for correct buildeing the XPath expression
        DirectedList<Object> objList = constructObjectLocationList(treeItem, true, skipFirst);
        return constructXPath(base, objList);
    }

    /**
     * Generate an XPathExpressionPath expression which represents an
     * absolute path specified by the objList parameter.
     *
     * It is implied that the generated XPath expression starts from a variable.
     * So there isn't a schema contex specified to the XPath model. The expression
     * is generated in the new XPath model. 
     *
     * @param base
     * @param objList
     * @return
     */
    public static XPathExpression constructXPath(WLMComponent base, DirectedList<Object> objList) {
        //
        if (objList == null || objList.isEmpty()) {
            return null;
        }
        //
        XPathModel xPathModel = WlmXPathModelFactory.create(base);
        XPathModelFactory factory = xPathModel.getFactory();
        //
        VariableDeclaration varDecl = null;
        Part part = null;
        LinkedList<LocationStep> stepList = new LinkedList<LocationStep>();
        SchemaModelsStack sms = new SchemaModelsStack();
        //
        // Process the path
        Iterator itr = objList.forwardIterator();
        while (itr.hasNext()) {
            Object stepObj = itr.next();
            SchemaComponent sComp = null;
            //
            // The while isn't infinite in most cases because it has a break at the end!
            while (true) {
                if (stepObj instanceof SchemaComponent) {
                    sComp = (SchemaComponent)stepObj;
                    LocationStep ls = constructLStep(xPathModel, sComp, null, sms);
                    stepList.add(0, ls);
//                } else if (stepObj instanceof AbstractPredicate) {
//                    AbstractPredicate pred = (AbstractPredicate)stepObj;
//                    XPathPredicateExpression[] predArr = pred.getPredicates();
//                    SchemaCompHolder sCompHolder = pred.getSCompHolder();
//                    sComp = sCompHolder.getSchemaComponent();
//                    LocationStep ls = constructLStep(xPathModel, sCompHolder, predArr, sms);
//                    stepList.add(0, ls);
//                } else if (stepObj instanceof AbstractTypeCast) {
//                    AbstractTypeCast typeCast = (AbstractTypeCast)stepObj;
//                    Object castedObj = typeCast.getCastedObject();
//                    stepObj = castedObj;
//                    continue;
                } else if (stepObj instanceof XPathSpecialStep) {
                    XPathSpecialStep sStep = (XPathSpecialStep)stepObj;
                    LocationStep ls = XPathSpecialStep.Utils.
                            constructLocationStep(xPathModel, sStep);
                    if (ls == null) {
                        return null;
                    }
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
                } else if (stepObj instanceof VariableDeclaration) {
                    VariableDeclaration var = (VariableDeclaration)stepObj;
                    //
                    varDecl = (VariableDeclaration)var;
                    //
                    if (varDecl == null) {
                        return null;
                    }
                } else {
                    // an unknown object
                    return null;
                }
                //
                break; // It ends infinite while
            }
            //
            if (sComp != null) {
                sms.appendSchemaComponent(sComp);
            } else {
                sms.discard();
            }
        }
        //
        if (varDecl == null) {
            return null;
        }
        XPathWlmVariable xPathVar = new XPathWlmVariable(varDecl, part);
        QName varQName = xPathVar.constructXPathName();
        XPathVariableReference xPathVarRef = 
                xPathModel.getFactory().newXPathVariableReference(varQName);
        //
        XPathExpression result = null;
        if (stepList.isEmpty()) {
            result = xPathVarRef;
        } else {
            LocationStep[] steps = stepList.toArray(new LocationStep[stepList.size()]);
            result = factory.newXPathExpressionPath(xPathVarRef, steps);
        } 
        //
        xPathModel.setRootExpression(result);
        return result;
    }

    public static XPathExpression constructXPath(WLMComponent base, XPathSchemaContext sContext) {
        DirectedList<Object> objList =
                constructObjectLocationList(sContext, true, false);
        return constructXPath(base, objList);
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
//            } else if (stepObj instanceof AbstractPredicate) {
//                AbstractPredicate pred = (AbstractPredicate)stepObj;
//                XPathPredicateExpression[] predArr = pred.getPredicates();
//                SchemaCompHolder sCompHolder = pred.getSCompHolder();
//                sComp = sCompHolder.getSchemaComponent();
//                newLocationStep = constructLStep(xPathModel, sCompHolder, predArr, sms);
//            } else if (stepObj instanceof AbstractTypeCast) {
//                AbstractTypeCast typeCast = (AbstractTypeCast)stepObj;
//                sComp = typeCast.getSComponent();
//                newLocationStep = constructLStep(xPathModel, sComp, null, sms);
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
        VariableDeclaration var = null;
        
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
//            } else if (var instanceof VariableDeclarationWrapper) {
//                varDecl = ((VariableDeclarationWrapper)var).getDelegate();
            }
            XPathWlmVariable xPathVariable = new XPathWlmVariable(varDecl, part);
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
//            } else if (obj instanceof AbstractPredicate) {
//                AbstractPredicate absPred = (AbstractPredicate)obj;
//                //
//                SimpleSchemaContext baseCtxt = new SimpleSchemaContext(
//                        constructContextImpl(pathItr, baseContext),
//                        absPred.getSCompHolder());
//                PredicatedSchemaContext predCtxt = new PredicatedSchemaContext(
//                        baseCtxt, absPred.getPredicates());
//                return predCtxt;
            } else if (obj instanceof VariableDeclaration) {
                var = (VariableDeclaration)obj;
                return buildVariableSchemaContext();
            } else if (obj instanceof Part) {
                part = (Part)obj;
                return constructContextImpl(pathItr, baseContext);
//            } else if (obj instanceof AbstractTypeCast) {
//                AbstractTypeCast typeCast = (AbstractTypeCast)obj;
//                Object castedObj = typeCast.getCastedObject();
//                XPathSchemaContext castedContext = constructContext(
//                        castedObj, pathItr,
//                        constructContextImpl(pathItr, baseContext));
//                return new CastSchemaContext(castedContext, typeCast);
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

//    /**
//     *
//     * @param sContext
//     * @return
//     * @deprecated
//     */
//    public static List<MapperLocationStepModifier> extractEeList(XPathSchemaContext sContext) {
//        LinkedList<MapperLocationStepModifier> result =
//                new LinkedList<MapperLocationStepModifier>();
//        //
//        DirectedList<Object> locationList =
//                constructObjectLocationList(sContext, true, false);
//        //
//        if (locationList != null && !locationList.isEmpty()) {
//            //
//            Iterator itr = locationList.forwardIterator();
//            while (itr.hasNext()) {
//                Object obj = itr.next();
//                if (obj instanceof AbstractPredicate) {
//                    result.addFirst((AbstractPredicate)obj);
//                } else if (obj instanceof AbstractTypeCast) {
//                    result.addFirst((AbstractTypeCast)obj);
//                } else if (obj instanceof AbstractPseudoComp) {
//                    result.addFirst((AbstractPseudoComp)obj);
//                }
//            }
//        }
//        //
//        return result;
//    }

    private static void add(Object objToAdd, LinkedList ll, boolean toEnd) {
        if (toEnd) {
            ll.addLast(objToAdd);
        } else {
            ll.addFirst(objToAdd);
        }
    }

    /**
     * A class, which wraps a list and contains a direction flag.
     * It helps resolve mess with direction.
     *
     * It is designed as strictly immutable.
     *
     * ATTANTION!!!!
     * FORWARD DIRECTION is a direction, where the variable is at the end 
     * of the list, like in the following example:
     *     Attribute -> Element -> Element -> Part -> Variable
     *
     * @param <T>
     */
    public static class DirectedList<T> implements Iterable<T> {

        public static DirectedList EMPTY = new DirectedList(Collections.EMPTY_LIST, true);

        private List<T> mList;
        private boolean mForwardDirection;

        public DirectedList(List<T> list, boolean forwardDirection) {
            mList = new ArrayList(list);
            mForwardDirection = forwardDirection;
        }

        public DirectedList(DirectedList base, T additional, boolean toEnd) {
            List<T> baseList = base.getList();
            LinkedList<T> newList = new LinkedList<T>(baseList);
            if (base.isForwardDirected()) {
                if (toEnd) {
                    newList.addLast(additional);
                } else {
                    newList.addFirst(additional);
                }
            } else {
                if (toEnd) {
                    newList.addFirst(additional);
                } else {
                    newList.addLast(additional);
                }
            }
            //
            mList = newList;
            mForwardDirection = base.isForwardDirected();
        }

        public List<T> getList() {
            return mList;
        }

        public boolean isForwardDirected() {
            return mForwardDirection;
        }

        public int size() {
            return mList.size();
        }

        public boolean isEmpty() {
            return mList == null || mList.isEmpty();
        }

        public ListIterator<T> forwardIterator() {
            if (mForwardDirection) {
                return mList.listIterator();
            } else {
                return new ReversedListIterator(mList);
            }
        }

        public ListIterator<T> backwardIterator() {
            if (mForwardDirection) {
                return new ReversedListIterator(mList);
            } else {
                return mList.listIterator();
            }
        }

        /**
         * Checks self direction. 
         * It throws assertion exception if direction is wrong.
         * It is intended to be used for testing. 
         */
        public void checkDirection() {
            // Uncomment to eliminate the check everywhere. 
//            if (true) {
//                return;
//            }
            //
            Iterator itr = backwardIterator();
            boolean schemaCompFound = false;
            boolean varFound = false;
            while (itr.hasNext()) {
                Object obj = itr.next();
//                if (obj instanceof AbstractTypeCast) {
//                    // TODO: Probably it worth improve this code to do it more general
//                    obj = ((AbstractTypeCast)obj).getCastedObject();
//                }
                if (obj instanceof VariableDeclaration) {
                    varFound = true;
                    break;
                }
                if (obj instanceof SchemaComponent) {
//                if (obj instanceof SchemaComponent ||
//                        obj instanceof AbstractPredicate ||
//                        obj instanceof AbstractPseudoComp) {
                    schemaCompFound = true;
                    break;
                }
            }
            //
            if (schemaCompFound && !varFound) {
                assert true : "Illegal order of Directed List."; // NOI18N
            }
        }

        public Iterator<T> iterator() {
            return forwardIterator();
        }

        public List<T> constructBackwardList() {
            if (!mForwardDirection) {
                return mList;
            } else {
                ArrayList<T> lList = new ArrayList<T>(mList.size());
                //
                Iterator<T> itr = backwardIterator();
                while(itr.hasNext()) {
                    T obj = itr.next();
                    lList.add(obj);
                }
                //
                return lList;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (mForwardDirection) {
                sb.append("FORWARD"); // NOI18N
            } else {
                sb.append("BACKWARD"); // NOI18N
            }
            //
            String lineSeparator = System.getProperty("line.separator");
            sb.append(lineSeparator);
            //
            sb.append(mList.toString());
            //
            return sb.toString();
        }
    }

    public static class ReversedListIterator<T> implements ListIterator<T> {
        private ListIterator<T> mItr;

        public ReversedListIterator(List<T> list) {
            mItr = list.listIterator(list.size());
        }

        public boolean hasNext() {
            return mItr.hasPrevious();
        }

        public T next() {
            return mItr.previous();
        }

        public boolean hasPrevious() {
            return mItr.hasNext();
        }

        public T previous() {
            return mItr.next();
        }

        public int nextIndex() {
            return mItr.previousIndex();
        }

        public int previousIndex() {
            return mItr.nextIndex();
        }

        public void remove() {
//            mItr.remove();
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        public void set(T o) {
//            mItr.set(o);
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        public void add(T o) {
//            mItr.add(o);
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

    }

}
